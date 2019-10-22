/*
 * Copyright 2019 Wultra s.r.o.
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

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.app.dataadapter.api.DataAdapter;
import io.getlime.security.powerauth.app.dataadapter.exception.DataAdapterRemoteException;
import io.getlime.security.powerauth.app.dataadapter.exception.InvalidOperationContextException;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.request.*;
import io.getlime.security.powerauth.lib.dataadapter.model.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller class which handles OAuth 2.0 consent actions.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("/api/afs")
public class AfsController {

    private static final Logger logger = LoggerFactory.getLogger(AfsController.class);

    private final DataAdapter dataAdapter;

    /**
     * Consent controller constructor.
     * @param dataAdapter Data adapter.
     */
    @Autowired
    public AfsController(DataAdapter dataAdapter) {
        this.dataAdapter = dataAdapter;
    }

    /**
     * Execute an anti-fraud system action and return response for usage in Web Flow.
     * @param request AFS request.
     * @return AFS response.
     * @throws DataAdapterRemoteException In case communication with remote system fails.
     * @throws InvalidOperationContextException In case operation context is invalid.
     */
    @RequestMapping(value = "/action", method = RequestMethod.POST)
    public ObjectResponse<AfsResponse> executeAfsAction(@RequestBody ObjectRequest<AfsRequest> request) throws DataAdapterRemoteException, InvalidOperationContextException {
        logger.info("Received executeAfsAction request for user: {}, operation ID: {}",
                request.getRequestObject().getUserId(), request.getRequestObject().getOperationContext().getId());
        AfsRequest afsRequest = request.getRequestObject();
        String userId = afsRequest.getUserId();
        String organizationId = afsRequest.getOrganizationId();
        OperationContext operationContext = afsRequest.getOperationContext();
        AfsRequestParameters requestParameters = afsRequest.getAfsRequestParameters();
        Map<String, Object> extras = afsRequest.getExtras();
        AfsResponse response = dataAdapter.executeAfsAction(userId, organizationId, operationContext, requestParameters, extras);
        logger.debug("The executeAfsAction request succeeded");
        return new ObjectResponse<>(response);
    }

}
