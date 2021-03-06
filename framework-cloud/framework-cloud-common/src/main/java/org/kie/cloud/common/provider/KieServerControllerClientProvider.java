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

package org.kie.cloud.common.provider;

import java.time.Instant;
import java.util.Collection;

import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.management.client.KieServerMgmtControllerClient;
import org.kie.server.controller.management.client.KieServerMgmtControllerClientFactory;

public class KieServerControllerClientProvider {

    public static KieServerMgmtControllerClient getKieServerMgmtControllerClient(WorkbenchDeployment workbenchDeployment) {
        KieServerMgmtControllerClient kieServerMgmtControllerClient = KieServerMgmtControllerClientFactory.newRestClient(workbenchDeployment.getUrl().toString() + "/rest/controller",
                workbenchDeployment.getUsername(), workbenchDeployment.getPassword());
        return kieServerMgmtControllerClient;
    }

    /**
     * Wait until server templates are created in controller.
     */
    public static void waitForServerTemplateCreation(WorkbenchDeployment workbenchDeployment, int numberOfServerTemplates) {
        Instant timeoutTime = Instant.now().plusSeconds(30);
        while (Instant.now().isBefore(timeoutTime)) {

            Collection<ServerTemplate> serverTemplates = getKieServerMgmtControllerClient(workbenchDeployment).listServerTemplates();
            if(serverTemplates.size() == numberOfServerTemplates) {
                return;
            }

            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for server template creation.", e);
            }
        }
        throw new RuntimeException("Timeout while waiting for 30 seconds for server template creation.");
    }
}

