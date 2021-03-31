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

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.security.powerauth.app.dataadapter.api.DataAdapter;
import io.getlime.security.powerauth.app.dataadapter.exception.DataAdapterRemoteException;
import io.getlime.security.powerauth.app.dataadapter.exception.InvalidOperationContextException;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationChange;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.request.CreateImplicitLoginOperationRequest;
import io.getlime.security.powerauth.lib.dataadapter.model.request.GetPAOperationMappingRequest;
import io.getlime.security.powerauth.lib.dataadapter.model.request.OperationChangeNotificationRequest;
import io.getlime.security.powerauth.lib.dataadapter.model.response.CreateImplicitLoginOperationResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.GetPAOperationMappingResponse;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Controller class which handles notifications about changes of operation state.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("/api/operation")
public class OperationChangeController {

    private static final Logger logger = LoggerFactory.getLogger(OperationChangeController.class);

    private final DataAdapter dataAdapter;

    /**
     * Controller constructor.
     * @param dataAdapter Data adapter.
     */
    @Autowired
    public OperationChangeController(DataAdapter dataAdapter) {
        this.dataAdapter = dataAdapter;
    }

    /**
     * Create a new implicit login operation with provided OAuth 2.0 context.
     * @param request Request with OAuth 2.0 attributes.
     * @return Newly created response with operation details.
     * @throws DataAdapterRemoteException In case network communication fails.
     * @throws InvalidOperationContextException In case provided information is not sufficient for creating
     * the correct implicit login operation.
     */
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ObjectResponse<CreateImplicitLoginOperationResponse> createImplicitLoginOperation(@RequestBody ObjectRequest<CreateImplicitLoginOperationRequest> request) throws DataAdapterRemoteException, InvalidOperationContextException {
        final CreateImplicitLoginOperationRequest requestObject = request.getRequestObject();
        final String clientId = requestObject.getClientId();
        final String[] scopes = requestObject.getScopes();
        logger.debug("Creating implicit login operation for client ID: {}, with scopes: {}", clientId, scopes);
        CreateImplicitLoginOperationResponse response = dataAdapter.createImplicitLoginOperation(clientId, scopes);
        if (response == null) {
            throw new InvalidOperationContextException("Unable to create an implicit login operation");
        }
        logger.debug("The createImplicitLoginOperation request succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * Receive a new operation change notification.
     *
     * @param request Request with change details.
     * @return Object response.
     * @throws DataAdapterRemoteException Thrown in case of remote communication errors.
     */
    @PostMapping(value = "/change")
    public Response operationChangedNotification(@RequestBody ObjectRequest<OperationChangeNotificationRequest> request) throws DataAdapterRemoteException {
        logger.info("Received operationChangedNotification request for user: {}, operation ID: {}",
                request.getRequestObject().getUserId(), request.getRequestObject().getOperationContext().getId());
        OperationChangeNotificationRequest notification = request.getRequestObject();
        String userId = notification.getUserId();
        String organizationId = notification.getOrganizationId();
        OperationContext operationContext = notification.getOperationContext();
        OperationChange operationChange = notification.getOperationChange();
        dataAdapter.operationChangedNotification(userId, organizationId, operationChange, operationContext);
        logger.debug("The operationChangedNotification request succeeded");
        return new Response();
    }

    /**
     * Get mapping of Next Step operation to PowerAuth operation.
     *
     * @param request Operation mapping request.
     * @return Operation mapping response.
     * @throws DataAdapterRemoteException Thrown in case of remote communication errors.
     */
    @RequestMapping(value = "/mapping", method = RequestMethod.POST)
    public ObjectResponse<GetPAOperationMappingResponse> getPAOperationMapping(@RequestBody ObjectRequest<GetPAOperationMappingRequest> request) throws DataAdapterRemoteException {
        logger.info("Received getPAOperationMapping request for user: {}, operation ID: {}",
                request.getRequestObject().getUserId(), request.getRequestObject().getOperationContext().getId());
        GetPAOperationMappingRequest mappingRequest = request.getRequestObject();
        String userId = mappingRequest.getUserId();
        String organizationId = mappingRequest.getOrganizationId();
        AuthMethod authMethod = mappingRequest.getAuthMethod();
        OperationContext operationContext = mappingRequest.getOperationContext();
        logger.debug("The getPAOperationMapping request succeeded");
        GetPAOperationMappingResponse response = dataAdapter.getPAOperationMapping(userId, organizationId, authMethod, operationContext);
        return new ObjectResponse<>(response);
    }

}
