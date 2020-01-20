/*
 * Copyright 2017 Wultra s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getlime.security.powerauth.app.dataadapter.controller;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.app.dataadapter.configuration.DataAdapterConfiguration;
import io.getlime.security.powerauth.lib.dataadapter.model.response.ServiceStatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Date;

/**
 * Class representing controller used for service and maintenance purpose.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@RestController
@RequestMapping(value = "/api/service")
public class ServiceController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);

    private final DataAdapterConfiguration dataAdapterConfiguration;
    private final BuildProperties buildProperties;

    /**
     * Controller constructor.
     * @param dataAdapterConfiguration Data adapter configuration.
     * @param buildProperties Build info.
     */
    @Autowired
    public ServiceController(DataAdapterConfiguration dataAdapterConfiguration, BuildProperties buildProperties) {
        this.dataAdapterConfiguration = dataAdapterConfiguration;
        this.buildProperties = buildProperties;
    }

    /**
     * Controller resource with system information.
     * @return System status info.
     */
    @GetMapping(value = "status")
    public ObjectResponse<ServiceStatusResponse> getServiceStatus() {
        logger.info("Received getServiceStatus request");
        ServiceStatusResponse response = new ServiceStatusResponse();
        response.setApplicationName(dataAdapterConfiguration.getApplicationName());
        response.setApplicationDisplayName(dataAdapterConfiguration.getApplicationDisplayName());
        response.setApplicationEnvironment(dataAdapterConfiguration.getApplicationEnvironment());
        response.setTimestamp(new Date());
        response.setVersion(buildProperties.getVersion());
        response.setBuildTime(Date.from(buildProperties.getTime()));
        logger.debug("The getServiceStatus request succeeded");
        return new ObjectResponse<>(response);
    }
}