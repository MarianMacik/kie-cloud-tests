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
package org.kie.cloud.openshift.settings.builder;

import java.util.HashMap;
import java.util.Map;

import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.settings.DeploymentSettings;
import org.kie.cloud.api.settings.builder.WorkbenchMonitoringSettingsBuilder;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.settings.DeploymentSettingsImpl;
import org.kie.cloud.openshift.template.OpenShiftTemplate;

public class WorkbenchMonitoringSettingsBuilderImpl implements WorkbenchMonitoringSettingsBuilder {

    private Map<String, String> envVariables;
    private final OpenShiftTemplate appTemplate = OpenShiftTemplate.CONSOLE;

    public WorkbenchMonitoringSettingsBuilderImpl() {
        envVariables = new HashMap<>();

        envVariables.put(OpenShiftTemplateConstants.KIE_ADMIN_USER, DeploymentConstants.getWorkbenchUser());
        envVariables.put(OpenShiftTemplateConstants.KIE_ADMIN_PWD, DeploymentConstants.getWorkbenchPassword());
    }

    @Override
    public DeploymentSettings build() {
        return new DeploymentSettingsImpl(envVariables, appTemplate);
    }

    @Override
    public WorkbenchMonitoringSettingsBuilder withAdminUser(String user, String password) {
        envVariables.put(OpenShiftTemplateConstants.KIE_ADMIN_USER, user);
        envVariables.put(OpenShiftTemplateConstants.KIE_ADMIN_PWD, password);
        return this;
    }

    @Override
    public WorkbenchMonitoringSettingsBuilder withApplicationName(String name) {
        envVariables.put(OpenShiftTemplateConstants.APPLICATION_NAME, name);
        return this;
    }

    @Override
    public WorkbenchMonitoringSettingsBuilder withControllerUser(String username, String password) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_USER, username);
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_PWD, password);
        return this;
    }

    @Override
    public WorkbenchMonitoringSettingsBuilder withKieServerUser(String username, String password) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_USER, username);
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_PWD, password);
        return this;
    }
}
