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
import io.getlime.security.powerauth.app.dataadapter.api.DataAdapter;
import io.getlime.security.powerauth.app.dataadapter.exception.DataAdapterRemoteException;
import io.getlime.security.powerauth.app.dataadapter.exception.UserNotFoundException;
import io.getlime.security.powerauth.app.dataadapter.impl.validation.AuthenticationRequestValidator;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.AuthenticationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.request.UserAuthenticationRequest;
import io.getlime.security.powerauth.lib.dataadapter.model.request.UserDetailRequest;
import io.getlime.security.powerauth.lib.dataadapter.model.request.UserLookupRequest;
import io.getlime.security.powerauth.lib.dataadapter.model.response.UserAuthenticationResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.UserDetailResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller class which handles user authentication.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("/api/auth/user")
public class AuthenticationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    private final AuthenticationRequestValidator requestValidator;
    private final DataAdapter dataAdapter;

    /**
     * Controller constructor.
     * @param requestValidator Validator for authentication requests.
     * @param dataAdapter Data adapter.
     */
    @Autowired
    public AuthenticationController(AuthenticationRequestValidator requestValidator, DataAdapter dataAdapter) {
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
     * Lookup user account.
     *
     * @param request Lookup user account request.
     * @return Response with user detail.
     * @throws DataAdapterRemoteException Thrown in case of remote communication errors.
     * @throws UserNotFoundException Thrown in case that user does not exist.
     */
    @PostMapping(value = "/lookup")
    public ObjectResponse<UserDetailResponse> lookupUser(@Valid @RequestBody ObjectRequest<UserLookupRequest> request) throws DataAdapterRemoteException, UserNotFoundException {
        logger.info("Received user lookup request, username: {}, organization ID: {}, operation ID: {}",
                request.getRequestObject().getUsername(), request.getRequestObject().getOrganizationId(),
                request.getRequestObject().getOperationContext().getId());
        UserLookupRequest lookupRequest = request.getRequestObject();
        String username = lookupRequest.getUsername();
        String organizationId = lookupRequest.getOrganizationId();
        String clientCertificate = lookupRequest.getClientCertificate();
        OperationContext operationContext = lookupRequest.getOperationContext();
        UserDetailResponse response = dataAdapter.lookupUser(username, organizationId, clientCertificate, operationContext);
        logger.info("The user lookup request succeeded, user ID: {}, organization ID: {}, operation ID: {}",
                response.getId(), response.getOrganizationId(), request.getRequestObject().getOperationContext().getId());
        return new ObjectResponse<>(response);
    }

    /**
     * Authenticate user with given credentials.
     *
     * @param request Authenticate user request.
     * @return Response with authenticated user ID.
     * @throws DataAdapterRemoteException Thrown in case of remote communication errors.
     */
    @PostMapping(value = "/authenticate")
    public ObjectResponse<UserAuthenticationResponse> authenticate(@Valid @RequestBody ObjectRequest<UserAuthenticationRequest> request) throws DataAdapterRemoteException {
        logger.info("Received authenticate request, user ID: {}, organization ID: {}, operation ID: {}",
                request.getRequestObject().getUserId(), request.getRequestObject().getOrganizationId(),
                request.getRequestObject().getOperationContext().getId());
        UserAuthenticationRequest authenticationRequest = request.getRequestObject();
        String userId = authenticationRequest.getUserId();
        String password = authenticationRequest.getPassword();
        AuthenticationContext authenticationContext = authenticationRequest.getAuthenticationContext();
        String organizationId = authenticationRequest.getOrganizationId();
        OperationContext operationContext = authenticationRequest.getOperationContext();
        UserAuthenticationResponse response = dataAdapter.authenticateUser(userId, password, authenticationContext, organizationId, operationContext);
        logger.info("The authenticate request succeeded, user ID: {}, organization ID: {}, operation ID: {}", userId,
                organizationId, request.getRequestObject().getOperationContext().getId());
        return new ObjectResponse<>(response);
    }

    /**
     * Fetch user details based on user ID.
     *
     * @param request Request with user ID.
     * @return Response with user details.
     * @throws DataAdapterRemoteException Thrown in case of remote communication errors.
     * @throws UserNotFoundException Thrown in case user is not found.
     */
    @PostMapping(value = "/info")
    public ObjectResponse<UserDetailResponse> fetchUserDetail(@RequestBody ObjectRequest<UserDetailRequest> request) throws DataAdapterRemoteException, UserNotFoundException {
        logger.info("Received fetchUserDetail request, user ID: {}", request.getRequestObject().getUserId());
        UserDetailRequest userDetailRequest = request.getRequestObject();
        String userId = userDetailRequest.getUserId();
        String organizationId = userDetailRequest.getOrganizationId();
        UserDetailResponse response = dataAdapter.fetchUserDetail(userId, organizationId, null);
        logger.info("The fetchUserDetail request succeeded");
        return new ObjectResponse<>(response);
    }


}
