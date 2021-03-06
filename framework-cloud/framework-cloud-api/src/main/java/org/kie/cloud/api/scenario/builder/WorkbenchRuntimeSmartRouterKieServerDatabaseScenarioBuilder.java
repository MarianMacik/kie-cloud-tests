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

package org.kie.cloud.api.scenario.builder;

import org.kie.cloud.api.scenario.WorkbenchRuntimeSmartRouterKieServerDatabaseScenario;

/**
 * Cloud builder for Workbench runtime, Smart router and Kie Server with database in project. Built setup
 * for WorkbenchRuntimeSmartRouterKieServerDatabaseScenario
 *
 * @see WorkbenchRuntimeSmartRouterKieServerDatabaseScenario
 */
public interface WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioBuilder extends DeploymentScenarioBuilder<WorkbenchRuntimeSmartRouterKieServerDatabaseScenario> {

    /**
     * Return setup builder with additional configuration of external maven
     * repo.
     *
     * @param repoUrl Maven repo URL.
     * @param repoUserName Maven repo user name.
     * @param repoPassword Maven repo user password.
     * @return Builder with configured external maven repo.
     */
    WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioBuilder withExternalMavenRepo(String repoUrl, String repoUserName, String repoPassword);

    /**
     * Return setup builder with specified Smart router id.
     *
     * @param smartRouterId Smart router id.
     * @return Builder with configured Smart router id.
     */
    WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioBuilder withSmartRouterId(String smartRouterId);
}
