/*
 * Copyright 2020 Wultra s.r.o.
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
import io.getlime.security.powerauth.app.dataadapter.impl.validation.AuthMethodRequestValidator;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.request.InitAuthMethodRequest;
import io.getlime.security.powerauth.lib.dataadapter.model.response.InitAuthMethodResponse;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Controller class which handles authentication method configuration.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("/api/auth/method")
public class AuthMethodController {

    private static final Logger logger = LoggerFactory.getLogger(AuthMethodController.class);

    private final AuthMethodRequestValidator requestValidator;
    private final DataAdapter dataAdapter;

    /**
     * Controller constructor.
     * @param requestValidator Validator for authentication requests.
     * @param dataAdapter Data adapter.
     */
    @Autowired
    public AuthMethodController(AuthMethodRequestValidator requestValidator, DataAdapter dataAdapter) {
        this.requestValidator = requestValidator;
        this.dataAdapter = dataAdapter;
    }

    /**
     * Initializes the request validator.
     * @param binder Data binder.
     */
    @InitBinder
    private void initBinder(WebDataBinder binder) {
        binder.setValidator(requestValidator);
    }

    /**
     * Initialize an authentication method.
     * @param request Request for authentication method initialization.
     * @return Response for authentication method initialization.
     * @throws DataAdapterRemoteException Thrown in case of remote communication errors.
     * @throws InvalidOperationContextException Thrown when operation context is invalid.
     */
    @PostMapping(value = "/init")
    public ObjectResponse<InitAuthMethodResponse> initAuthMethod(@Valid @RequestBody ObjectRequest<InitAuthMethodRequest> request) throws DataAdapterRemoteException, InvalidOperationContextException {
        logger.info("Received initAuthMethod request, user ID: {}, authentication method: {}", request.getRequestObject().getUserId(), request.getRequestObject().getAuthMethod());
        InitAuthMethodRequest initRequest = request.getRequestObject();
        String userId = initRequest.getUserId();
        String organizationId = initRequest.getOrganizationId();
        AuthMethod authMethod = initRequest.getAuthMethod();
        OperationContext operationContext = initRequest.getOperationContext();
        InitAuthMethodResponse response = dataAdapter.initAuthMethod(userId, organizationId, authMethod, operationContext);
        logger.info("The initAuthMethod request succeeded");
        return new ObjectResponse<>(response);
    }

}