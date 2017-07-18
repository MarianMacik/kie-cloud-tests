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

package org.kie.cloud.openshift.scenario;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.scenario.WorkbenchWithKieServerScenario;
import org.kie.cloud.openshift.OpenShiftController;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.deployment.WorkbenchDeploymentImpl;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.resource.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkbenchWithKieServerScenarioImpl implements WorkbenchWithKieServerScenario {

    private OpenShiftController openshiftController;
    private String projectName;
    private WorkbenchDeploymentImpl workbenchDeployment;
    private KieServerDeploymentImpl kieServerDeployment;

    private Map<String, String> envVariables;

    private static final Logger logger = LoggerFactory.getLogger(WorkbenchWithKieServerScenarioImpl.class);

    public WorkbenchWithKieServerScenarioImpl(OpenShiftController openShiftController, Map<String, String> envVariables) {
        this.openshiftController = openShiftController;
        this.envVariables = envVariables;
    }

    @Override public String getNamespace() {
        return projectName;
    }

    @Override public void deploy() {
        // OpenShift restriction: Hostname must be shorter than 63 characters
        projectName = UUID.randomUUID().toString().substring(0, 10);

        logger.info("Generated project name is " + projectName);

        logger.info("Creating project " + projectName);
        Project project = openshiftController.createProject(projectName);

        logger.info("Creating secrets from " + OpenShiftConstants.getKieAppSecret());
        project.createResources(OpenShiftConstants.getKieAppSecret());

        logger.info("Creating image streams from " + OpenShiftConstants.getKieImageStreams());
        project.createResources(OpenShiftConstants.getKieImageStreams());

        logger.info("Processing template and creating resources from " + OpenShiftConstants.getKieAppTemplate());
        envVariables.put(OpenShiftTemplateConstants.IMAGE_STREAM_NAMESPACE, projectName);
        project.processTemplateAndCreateResources(OpenShiftConstants.getKieAppTemplate(), envVariables);

        waitUntilAllPodsAreReady(project);

        String routeHostWorkbench = project.getService(OpenShiftConstants.getWorkbenchServiceName()).getRoute().getRouteHost();
        String urlWorkbench = "http://" + routeHostWorkbench;

        workbenchDeployment = new WorkbenchDeploymentImpl();
        workbenchDeployment.setOpenShiftController(openshiftController);
        workbenchDeployment.setNamespace(projectName);
        try {
            workbenchDeployment.setUrl(new URL(urlWorkbench));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed URL for workbench", e);
        }
        workbenchDeployment.setUsername(OpenShiftConstants.getWorkbenchUser());
        workbenchDeployment.setPassword(OpenShiftConstants.getWorkbenchPassword());
        workbenchDeployment.setServiceName(OpenShiftConstants.getWorkbenchServiceName());

        String routeHostKieServer = project.getService(OpenShiftConstants.getKieServerServiceName()).getRoute().getRouteHost();
        String urlKieServer = "http://" + routeHostKieServer;

        kieServerDeployment = new KieServerDeploymentImpl();
        kieServerDeployment.setOpenShiftController(openshiftController);
        kieServerDeployment.setNamespace(projectName);
        try {
            kieServerDeployment.setUrl(new URL(urlKieServer));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed URL for kie server", e);
        }
        kieServerDeployment.setUsername(OpenShiftConstants.getKieServerUser());
        kieServerDeployment.setPassword(OpenShiftConstants.getKieServerPassword());
        kieServerDeployment.setServiceName(OpenShiftConstants.getKieServerServiceName());
    }

    @Override public void undeploy() {
        Project project = openshiftController.getProject(projectName);
        project.delete();
    }

    private void waitUntilAllPodsAreReady(Project project) {
        for (Service service : project.getServices()) {
            if (service.getDeploymentConfig() != null) {
                logger.info("Waiting for pods in deployment config to become ready: " + service.getName());
                service.getDeploymentConfig().waitUntilAllPodsAreReady();
            }
        }
    }

    public OpenShiftController getOpenshiftController() {
        return openshiftController;
    }

    @Override public WorkbenchDeployment getWorkbenchDeployment() {
        return workbenchDeployment;
    }

    @Override public KieServerDeployment getKieServerDeployment() {
        return kieServerDeployment;
    }
}