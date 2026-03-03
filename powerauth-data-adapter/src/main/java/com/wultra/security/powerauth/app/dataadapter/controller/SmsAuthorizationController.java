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
package com.wultra.security.powerauth.app.dataadapter.controller;

import com.wultra.core.rest.model.base.request.ObjectRequest;
import com.wultra.core.rest.model.base.response.ObjectResponse;
import com.wultra.security.powerauth.app.dataadapter.api.DataAdapter;
import com.wultra.security.powerauth.app.dataadapter.exception.DataAdapterRemoteException;
import com.wultra.security.powerauth.app.dataadapter.exception.InvalidOperationContextException;
import com.wultra.security.powerauth.app.dataadapter.impl.validation.AuthorizationSmsRequestValidator;
import com.wultra.security.powerauth.lib.dataadapter.model.entity.AuthenticationContext;
import com.wultra.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import com.wultra.security.powerauth.lib.dataadapter.model.enumeration.AccountStatus;
import com.wultra.security.powerauth.lib.dataadapter.model.request.CreateSmsAuthorizationRequest;
import com.wultra.security.powerauth.lib.dataadapter.model.request.SendAuthorizationSmsRequest;
import com.wultra.security.powerauth.lib.dataadapter.model.request.VerifySmsAndPasswordRequest;
import com.wultra.security.powerauth.lib.dataadapter.model.request.VerifySmsAuthorizationRequest;
import com.wultra.security.powerauth.lib.dataadapter.model.response.CreateSmsAuthorizationResponse;
import com.wultra.security.powerauth.lib.dataadapter.model.response.SendAuthorizationSmsResponse;
import com.wultra.security.powerauth.lib.dataadapter.model.response.VerifySmsAndPasswordResponse;
import com.wultra.security.powerauth.lib.dataadapter.model.response.VerifySmsAuthorizationResponse;
import com.wultra.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller class which handles SMS OTP authorization.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("/api/auth/sms")
public class SmsAuthorizationController {

    private static final Logger logger = LoggerFactory.getLogger(SmsAuthorizationController.class);

    private final AuthorizationSmsRequestValidator requestValidator;
    private final DataAdapter dataAdapter;

    /**
     * Controller constructor.
     * @param requestValidator Validator for SMS requests.
     * @param dataAdapter Data adapter.
     */
    @Autowired
    public SmsAuthorizationController(AuthorizationSmsRequestValidator requestValidator, DataAdapter dataAdapter) {
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
     * Create a new SMS OTP authorization message.
     *
     * @param request Request data.
     * @return Response with message ID.
     * @throws DataAdapterRemoteException Thrown in case of remote communication errors.
     * @throws InvalidOperationContextException Thrown in case operation context is invalid.
     */
    @PostMapping(value = "create")
    public ObjectResponse<CreateSmsAuthorizationResponse> createAuthorizationSms(@Valid @RequestBody ObjectRequest<CreateSmsAuthorizationRequest> request) throws DataAdapterRemoteException, InvalidOperationContextException {
        logger.info("Received createAuthorizationSms request, operation ID: {}", request.getRequestObject().getOperationContext().getId());
        CreateSmsAuthorizationRequest smsRequest = request.getRequestObject();

        // Create authorization SMS and persist it
        String userId = smsRequest.getUserId();
        String organizationId = smsRequest.getOrganizationId();
        AccountStatus accountStatus = smsRequest.getAccountStatus();
        AuthMethod authMethod = smsRequest.getAuthMethod();
        OperationContext operationContext = smsRequest.getOperationContext();
        String lang = smsRequest.getLang();
        CreateSmsAuthorizationResponse response = dataAdapter.createAndSendAuthorizationSms(userId, organizationId, accountStatus, authMethod, operationContext, lang);

        logger.info("The createAuthorizationSms request succeeded, operation ID: {}", request.getRequestObject().getOperationContext().getId());
        return new ObjectResponse<>(response);
    }

    /**
     * Send a new SMS OTP authorization message.
     *
     * @param request Request data.
     * @return Response with message ID.
     * @throws DataAdapterRemoteException Thrown in case of remote communication errors.
     * @throws InvalidOperationContextException Thrown in case operation context is invalid.
     */
    @PostMapping(value = "send")
    public ObjectResponse<SendAuthorizationSmsResponse> sendAuthorizationSms(@Valid @RequestBody ObjectRequest<SendAuthorizationSmsRequest> request) throws DataAdapterRemoteException, InvalidOperationContextException {
        logger.info("Received sendAuthorizationSms request, operation ID: {}", request.getRequestObject().getOperationContext().getId());
        SendAuthorizationSmsRequest smsRequest = request.getRequestObject();

        // Create authorization SMS
        String userId = smsRequest.getUserId();
        String organizationId = smsRequest.getOrganizationId();
        AccountStatus accountStatus = smsRequest.getAccountStatus();
        AuthMethod authMethod = smsRequest.getAuthMethod();
        OperationContext operationContext = smsRequest.getOperationContext();
        String messageId = smsRequest.getMessageId();
        String authorizationCode = smsRequest.getAuthorizationCode();
        String lang = smsRequest.getLang();
        SendAuthorizationSmsResponse response = dataAdapter.sendAuthorizationSms(userId, organizationId, accountStatus, authMethod, operationContext, messageId, authorizationCode, lang);

        logger.info("The sendAuthorizationSms request succeeded, operation ID: {}", request.getRequestObject().getOperationContext().getId());
        return new ObjectResponse<>(response);
    }

    /**
     * Verify authorization code from SMS message.
     *
     * @param request Request data.
     * @return Authorization response.
     * @throws DataAdapterRemoteException Thrown in case communication with remote system fails.
     * @throws InvalidOperationContextException Thrown in case operation context is invalid.
     */
    @PostMapping(value = "verify")
    public ObjectResponse<VerifySmsAuthorizationResponse> verifyAuthorizationSms(@RequestBody ObjectRequest<VerifySmsAuthorizationRequest> request) throws InvalidOperationContextException, DataAdapterRemoteException {
        logger.info("Received verifyAuthorizationSms request, operation ID: {}", request.getRequestObject().getOperationContext().getId());
        VerifySmsAuthorizationRequest verifyRequest = request.getRequestObject();
        String userId = verifyRequest.getUserId();
        String organizationId = verifyRequest.getOrganizationId();
        AccountStatus accountStatus = verifyRequest.getAccountStatus();
        String messageId = verifyRequest.getMessageId();
        String authorizationCode = verifyRequest.getAuthorizationCode();
        OperationContext operationContext = verifyRequest.getOperationContext();
        // Verify authorization code
        VerifySmsAuthorizationResponse response = dataAdapter.verifyAuthorizationSms(userId, organizationId, accountStatus, messageId, authorizationCode, operationContext);
        logger.info("The verifyAuthorizationSms request succeeded, operation ID: {}", request.getRequestObject().getOperationContext().getId());
        return new ObjectResponse<>(response);
    }

    /**
     * Verify a SMS OTP authorization code and user password.
     *
     * @param request Verify SMS code and password request.
     * @return Authorization response.
     * @throws DataAdapterRemoteException Thrown in case communication with remote system fails.
     * @throws InvalidOperationContextException Thrown in case operation context is invalid.
     */
    @PostMapping(value = "/password/verify")
    public ObjectResponse<VerifySmsAndPasswordResponse> verifyAuthorizationSmsAndPassword(@RequestBody ObjectRequest<VerifySmsAndPasswordRequest> request) throws DataAdapterRemoteException, InvalidOperationContextException {
        logger.info("Received verifyAuthorizationSmsAndPassword request, operation ID: {}", request.getRequestObject().getOperationContext().getId());
        VerifySmsAndPasswordRequest verifyRequest = request.getRequestObject();
        String userId = verifyRequest.getUserId();
        String organizationId = verifyRequest.getOrganizationId();
        AccountStatus accountStatus = verifyRequest.getAccountStatus();
        String messageId = verifyRequest.getMessageId();
        String authorizationCode = verifyRequest.getAuthorizationCode();
        OperationContext operationContext = verifyRequest.getOperationContext();
        String password = verifyRequest.getPassword();
        AuthenticationContext authenticationContext = verifyRequest.getAuthenticationContext();
        VerifySmsAndPasswordResponse response = dataAdapter.verifyAuthorizationSmsAndPassword(userId, organizationId, accountStatus, messageId, authorizationCode, operationContext, authenticationContext, password);
        logger.info("The verifyAuthorizationSmsAndPassword request succeeded, operation ID: {}", request.getRequestObject().getOperationContext().getId());
        return new ObjectResponse<>(response);
    }

}
