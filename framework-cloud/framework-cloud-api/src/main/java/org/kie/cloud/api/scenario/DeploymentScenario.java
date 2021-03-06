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

package org.kie.cloud.api.scenario;

import java.util.List;

import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.DeploymentTimeoutException;

/**
 * Cloud deployment scenario representation.
 */
public interface DeploymentScenario {

    /**
     * Return deployment scenario namespace.
     *
     * @return deployment scenario name.
     */
    String getNamespace();

    /**
     * Create and deploy deployment scenario.
     *
     * @throws MissingResourceException If scenario is missing any required resource.
     * @throws DeploymentTimeoutException In case scenario deployment isn't started in defined timeout.
     */
    void deploy() throws MissingResourceException, DeploymentTimeoutException;

    /**
     * Undeploy and delete deployment scenario.
     */
    void undeploy();

    /**
     * Return all available deployments.
     *
     * @return All available deployments.
     */
    List<Deployment> getDeployments();
}
