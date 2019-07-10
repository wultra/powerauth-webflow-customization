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
import io.getlime.security.powerauth.app.dataadapter.exception.AuthenticationFailedException;
import io.getlime.security.powerauth.app.dataadapter.exception.DataAdapterRemoteException;
import io.getlime.security.powerauth.app.dataadapter.exception.InvalidOperationContextException;
import io.getlime.security.powerauth.app.dataadapter.exception.SmsAuthorizationFailedException;
import io.getlime.security.powerauth.app.dataadapter.impl.validation.CreateSmsAuthorizationRequestValidator;
import io.getlime.security.powerauth.app.dataadapter.repository.model.entity.SmsAuthorizationEntity;
import io.getlime.security.powerauth.app.dataadapter.service.SmsPersistenceService;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AuthenticationType;
import io.getlime.security.powerauth.lib.dataadapter.model.request.CreateSmsAuthorizationRequest;
import io.getlime.security.powerauth.lib.dataadapter.model.request.VerifySmsAndPasswordRequest;
import io.getlime.security.powerauth.lib.dataadapter.model.request.VerifySmsAuthorizationRequest;
import io.getlime.security.powerauth.lib.dataadapter.model.response.CreateSmsAuthorizationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Controller class which handles SMS OTP authorization.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("/api/auth/sms")
public class SmsAuthorizationController {

    private static final Logger logger = LoggerFactory.getLogger(SmsAuthorizationController.class);

    private final SmsPersistenceService smsPersistenceService;
    private final CreateSmsAuthorizationRequestValidator requestValidator;
    private final DataAdapter dataAdapter;

    /**
     * Controller constructor.
     * @param smsPersistenceService SMS persistence service.
     * @param requestValidator Validator for SMS requests.
     * @param dataAdapter Data adapter.
     */
    @Autowired
    public SmsAuthorizationController(SmsPersistenceService smsPersistenceService, CreateSmsAuthorizationRequestValidator requestValidator, DataAdapter dataAdapter) {
        this.smsPersistenceService = smsPersistenceService;
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
     * @throws SmsAuthorizationFailedException Thrown in case that SMS message could not be delivered.
     */
    @RequestMapping(value = "create", method = RequestMethod.POST)
    public ObjectResponse<CreateSmsAuthorizationResponse> createAuthorizationSms(@Valid @RequestBody ObjectRequest<CreateSmsAuthorizationRequest> request) throws DataAdapterRemoteException, SmsAuthorizationFailedException, InvalidOperationContextException {
        logger.info("Received createAuthorizationSms request, operation ID: "+request.getRequestObject().getOperationContext().getId());
        CreateSmsAuthorizationRequest smsRequest = request.getRequestObject();

        // Create authorization SMS and persist it.
        SmsAuthorizationEntity smsEntity = createAuthorizationSms(smsRequest);

        // Send SMS with generated text to target user.
        String userId = smsEntity.getUserId();
        String organizationId = smsEntity.getOrganizationId();
        OperationContext operationContext = smsRequest.getOperationContext();
        String messageId = smsEntity.getMessageId();
        String messageText = smsEntity.getMessageText();
        dataAdapter.sendAuthorizationSms(userId, organizationId, messageText, operationContext);

        // Create response.
        CreateSmsAuthorizationResponse response = new CreateSmsAuthorizationResponse(messageId);
        logger.info("The createAuthorizationSms request succeeded, operation ID: "+request.getRequestObject().getOperationContext().getId());
        return new ObjectResponse<>(response);
    }

    /**
     * Validates the request and sends SMS.
     * @param smsRequest Create SMS request.
     * @return SMS entity.
     */
    private SmsAuthorizationEntity createAuthorizationSms(@Valid CreateSmsAuthorizationRequest smsRequest) throws InvalidOperationContextException {
        String userId = smsRequest.getUserId();
        String organizationId = smsRequest.getOrganizationId();
        OperationContext operationContext = smsRequest.getOperationContext();
        String lang = smsRequest.getLang();
        return smsPersistenceService.createAuthorizationSms(userId, organizationId, operationContext, lang);
    }

    /**
     * Verify a SMS OTP authorization code.
     *
     * @param request Request data.
     * @return Authorization response.
     * @throws SmsAuthorizationFailedException Thrown in case that SMS verification fails.
     */
    @RequestMapping(value = "verify", method = RequestMethod.POST)
    public Response verifyAuthorizationSms(@RequestBody ObjectRequest<VerifySmsAuthorizationRequest> request) throws SmsAuthorizationFailedException {
        logger.info("Received verifyAuthorizationSms request, operation ID: "+request.getRequestObject().getOperationContext().getId());
        VerifySmsAuthorizationRequest verifyRequest = request.getRequestObject();
        String messageId = verifyRequest.getMessageId();
        String authorizationCode = verifyRequest.getAuthorizationCode();
        // Verify authorization code
        smsPersistenceService.verifyAuthorizationSms(messageId, authorizationCode, false);
        logger.info("The verifyAuthorizationSms request succeeded, operation ID: "+request.getRequestObject().getOperationContext().getId());
        return new Response();
    }

    /**
     * Verify a SMS OTP authorization code and user password.
     *
     * @param request Verify SMS code and password request.
     * @return Authorization response.
     * @throws SmsAuthorizationFailedException Thrown in case that SMS verification fails.
     * @throws AuthenticationFailedException Thrown in case that password verification fails.
     * @throws DataAdapterRemoteException Thrown in case communication with remote system fails.
     */
    @RequestMapping(value = "/password/verify", method = RequestMethod.POST)
    public Response verifyAuthorizationSmsAndPassword(@RequestBody ObjectRequest<VerifySmsAndPasswordRequest> request) throws SmsAuthorizationFailedException, AuthenticationFailedException, DataAdapterRemoteException {
        logger.info("Received verifyAuthorizationSmsAndPassword request, operation ID: "+request.getRequestObject().getOperationContext().getId());
        VerifySmsAndPasswordRequest verifyRequest = request.getRequestObject();
        // Verify authorization code
        String messageId = verifyRequest.getMessageId();
        String authorizationCode = verifyRequest.getAuthorizationCode();
        smsPersistenceService.verifyAuthorizationSms(messageId, authorizationCode, true);
        // Verify user password
        String userId = verifyRequest.getUserId();
        String password = verifyRequest.getPassword();
        AuthenticationType authenticationType = verifyRequest.getAuthenticationType();
        String cipherTransformation = verifyRequest.getCipherTransformation();
        String organizationId = verifyRequest.getOrganizationId();
        OperationContext operationContext = verifyRequest.getOperationContext();
        dataAdapter.authenticateUser(userId, password, authenticationType, cipherTransformation, organizationId, operationContext);
        logger.info("The verifyAuthorizationSmsAndPassword request succeeded, operation ID: "+request.getRequestObject().getOperationContext().getId());
        return new Response();
    }

}
