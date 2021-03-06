/*
 * Copyright 2017 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.cloud.openshift.deployment;

import static org.kie.cloud.openshift.util.CommandUtil.runCommandImpl;

import io.fabric8.kubernetes.client.KubernetesClientException;
import org.kie.cloud.api.deployment.CommandExecutionResult;
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.openshift.OpenShiftController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenShiftInstance implements Instance {

    private static final Logger logger = LoggerFactory.getLogger(OpenShiftInstance.class);

    private OpenShiftController openShiftController;
    private String name;
    private String namespace;

    public void setName(String name) {
        this.name = name;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public OpenShiftController getOpenShiftController() {
        return openShiftController;
    }

    public void setOpenShiftController(OpenShiftController openShiftController) {
        this.openShiftController = openShiftController;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override public CommandExecutionResult runCommand(String... command) {
        return runCommandImpl(openShiftController.getClient().pods().inNamespace(namespace).withName(name), command);
    }

    @Override
    public String getLogs() {
        try {
            return openShiftController.getClient().pods().inNamespace(namespace).withName(name).getLog();
        } catch (KubernetesClientException e) {
            logger.info("Exception while retrieving OpenShift log for pod with name " + name, e);
            return "";
        }
    }
}
