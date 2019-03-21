/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.cloud.performancetests.timers;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.VariableInstance;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimersAtSinglePointOfTimeIntegrationTest extends AbstractTimersCloudPerformanceTest {

    private static final Logger logger = LoggerFactory.getLogger(TimersAtSinglePointOfTimeIntegrationTest.class);

    @Test
    public void testScenario() {
        logger.info("==== STARTING SCENARIO ====");
        runSingleScenario();
        logger.info("==== SCENARIO COMPLETE ====");

        logger.info("==== GATHERING STATISTICS ====");
        gatherAndAssertStatistics();
        logger.info("==== STATISTICS GATHERED ====");
    }

    // ========== HELPER METHODS ==========

    private void runSingleScenario() {
        OffsetDateTime fireAtTime = calculateFireAtTime();

        logger.info("Starting {} processes", PROCESSES_COUNT);

        Instant startTime = Instant.now();
        // This is just to provide virtually "infinite" time to finish all iterations, i.e. maximum possible duration minus 1 hour,
        // so we can be sure there won't be overflow
        Duration maxDuration = Duration.between(startTime.plus(1, ChronoUnit.HOURS), Instant.MAX);
        Map<String, Object> params = Collections.singletonMap("fireAt", fireAtTime.toString());
        startAndWaitForStartingThreads(STARTING_THREADS_COUNT, maxDuration, PROCESSES_PER_THREAD, getStartingRunnable(CONTAINER_ID, ONE_TIMER_DATE_PROCESS_ID, params));

        logger.info("Starting processes took: {}", Duration.between(startTime, Instant.now()));

        Assertions.assertThat(Instant.now()).isBefore(fireAtTime.toInstant());

        Duration waitForCompletionDuration = Duration.between(Instant.now(), fireAtTime).plus(Duration.of(40, ChronoUnit.MINUTES));

        logger.info("Waiting for process instances to be completed, max waiting time is {}", waitForCompletionDuration);
        waitForAllProcessesToComplete(waitForCompletionDuration);
        logger.info("Process instances completed, took approximately {}", Duration.between(fireAtTime.toInstant(), Instant.now()));

    }

    private OffsetDateTime calculateFireAtTime() {
        OffsetDateTime currentTime = OffsetDateTime.now();
        long offsetInSeconds = Math.round(PROCESSES_COUNT / STARTING_THREADS_COUNT / PERF_INDEX);
        OffsetDateTime fireAtTime = currentTime.plus(offsetInSeconds, ChronoUnit.SECONDS);
        logger.info("fireAtTime set to {} which is the offset of {} seconds", fireAtTime, offsetInSeconds);

        return fireAtTime;
    }

    private void updateDistribution(Map<String, Integer> distribution, String hostName) {
        Integer count = distribution.get(hostName);
        if (count == null) {
            distribution.put(hostName, 1);
        } else {
            distribution.put(hostName, ++count);
        }
    }

    private void gatherAndAssertStatistics() {
        // to speed up pagination
        KieServicesClient kieStatisticsClient = createKieServerClient("http://" + BRMS_PERF_PREFIX + "01" + PERF_LAB_SUFFIX + ":" + KIE_SERVER_PORT + KIE_SERVER_CONTEXT, KIE_SERVER_USER, KIE_SERVER_PASSWORD);
        QueryServicesClient queryClient = kieStatisticsClient.getServicesClient(QueryServicesClient.class);

        List<ProcessInstance> activeProcesses = queryClient.findProcessInstancesByStatus(Collections.singletonList(org.jbpm.process.instance.ProcessInstance.STATE_ACTIVE), 0, 10);
        logger.debug("Active processes count: {}", activeProcesses.size());
        Assertions.assertThat(activeProcesses).isEmpty();

        /*QueryDefinition query = new QueryDefinition();
        query.setName("completedProcessInstances");
        query.setSource("java:jboss/datasources/jbpmDS");
        query.setExpression("select processinstanceid from ProcessInstanceLog where status = 2");
        query.setTarget("PROCESS");

        queryClient.registerQuery(query);*/

        int numberOfPages = 1 + (PROCESSES_COUNT / 5000);// including one additional page to check there are no more processes

        List<ProcessInstance> completedProcesses = new ArrayList<>(PROCESSES_COUNT);

        for (int i = 0; i < numberOfPages; i++) {
            logger.debug("Receiving page no. {}", i);
            List<ProcessInstance> response = queryClient.findProcessInstancesByStatus(Collections.singletonList(org.jbpm.process.instance.ProcessInstance.STATE_COMPLETED), i, 5000);
            //List<ProcessInstance> response = queryClient.query(query.getName(), QueryServicesClient.QUERY_MAP_PI, i, 5000, ProcessInstance.class);
            logger.debug("Received page no. {}", i);

            logger.debug("Adding page no. {} to collection", i);
            completedProcesses.addAll(response);
            logger.debug("Page no. {} added to collection", i);
        }

        logger.debug("Completed processes count: {}", completedProcesses.size());
        Assertions.assertThat(completedProcesses).hasSize(PROCESSES_COUNT);

        Map<String, Integer> startedHostNameDistribution = new HashMap<>();
        Map<String, Integer> completedHostNameDistribution = new HashMap<>();

        //int i = 0;
        for (int i = 0; i < completedProcesses.size();) {
            ProcessInstance pi = completedProcesses.get(i);
            long pid = pi.getId();
            List<VariableInstance> hostNameHistory = queryClient.findVariableHistory(pid, "hostName", 0, 10);
            Assertions.assertThat(hostNameHistory).hasSize(2);
            // Values are DESC by ID, i.e. time
            updateDistribution(startedHostNameDistribution, hostNameHistory.get(0).getOldValue());
            updateDistribution(completedHostNameDistribution, hostNameHistory.get(0).getValue());

            if (++i % 1000 == 0) {
                logger.debug("{} processes validated", i);
            }
        }

        logger.info("Processes were started with this distribution: {}", startedHostNameDistribution);
        logger.info("Processes were completed with this distribution: {}", completedHostNameDistribution);
    }

}