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
import io.getlime.security.powerauth.app.dataadapter.exception.SMSAuthorizationFailedException;
import io.getlime.security.powerauth.app.dataadapter.impl.validation.CreateSMSAuthorizationRequestValidator;
import io.getlime.security.powerauth.app.dataadapter.repository.model.entity.SMSAuthorizationEntity;
import io.getlime.security.powerauth.app.dataadapter.service.SMSPersistenceService;
import io.getlime.security.powerauth.lib.dataadapter.model.request.CreateSMSAuthorizationRequest;
import io.getlime.security.powerauth.lib.dataadapter.model.request.VerifySMSAuthorizationRequest;
import io.getlime.security.powerauth.lib.dataadapter.model.response.CreateSMSAuthorizationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
public class SMSAuthorizationController {

    private static final Logger logger = LoggerFactory.getLogger(SMSAuthorizationController.class);

    private final SMSPersistenceService smsPersistenceService;
    private final CreateSMSAuthorizationRequestValidator requestValidator;
    private final DataAdapter dataAdapter;

    /**
     * Controller constructor.
     * @param smsPersistenceService SMS persistence service.
     * @param requestValidator Validator for SMS requests.
     * @param dataAdapter Data adapter.
     */
    @Autowired
    public SMSAuthorizationController(SMSPersistenceService smsPersistenceService, CreateSMSAuthorizationRequestValidator requestValidator, DataAdapter dataAdapter) {
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
     * @throws SMSAuthorizationFailedException Thrown in case that SMS message could not be delivered.
     */
    @RequestMapping(value = "create", method = RequestMethod.POST)
    public ObjectResponse<CreateSMSAuthorizationResponse> createAuthorizationSMS(@Valid @RequestBody ObjectRequest<CreateSMSAuthorizationRequest> request) throws DataAdapterRemoteException, SMSAuthorizationFailedException, InvalidOperationContextException {
        logger.info("Received createAuthorizationSMS request, operation ID: "+request.getRequestObject().getOperationContext().getId());
        CreateSMSAuthorizationRequest smsRequest = request.getRequestObject();

        // Create authorization SMS and persist it.
        SMSAuthorizationEntity smsEntity = createAuthorizationSMS(smsRequest);

        // Send SMS with generated text to target user.
        String userId = smsEntity.getUserId();
        String messageId = smsEntity.getMessageId();
        String messageText = smsEntity.getMessageText();
        dataAdapter.sendAuthorizationSMS(userId, messageText, smsRequest.getOperationContext());

        // Create response.
        CreateSMSAuthorizationResponse response = new CreateSMSAuthorizationResponse(messageId);
        logger.info("The createAuthorizationSMS request succeeded, operation ID: "+request.getRequestObject().getOperationContext().getId());
        return new ObjectResponse<>(response);
    }

    /**
     * Validates the request and sends SMS.
     * @param smsRequest Create SMS request.
     * @return SMS entity.
     */
    private SMSAuthorizationEntity createAuthorizationSMS(@Valid CreateSMSAuthorizationRequest smsRequest) throws InvalidOperationContextException {
        String userId = smsRequest.getUserId();
        String lang = smsRequest.getLang();
        return smsPersistenceService.createAuthorizationSMS(userId, smsRequest.getOperationContext(), lang);
    }

    /**
     * Verify a SMS OTP authorization code.
     *
     * @param request Request data.
     * @return Authorization response.
     * @throws SMSAuthorizationFailedException Thrown in case that SMS verification fails.
     */
    @RequestMapping(value = "verify", method = RequestMethod.POST)
    public Response verifyAuthorizationSMS(@RequestBody ObjectRequest<VerifySMSAuthorizationRequest> request) throws SMSAuthorizationFailedException {
        logger.info("Received verifyAuthorizationSMS request, operation ID: "+request.getRequestObject().getOperationContext().getId());
        VerifySMSAuthorizationRequest verifyRequest = request.getRequestObject();
        String messageId = verifyRequest.getMessageId();
        String authorizationCode = verifyRequest.getAuthorizationCode();
        // Verify authorization code.
        smsPersistenceService.verifyAuthorizationSMS(messageId, authorizationCode);
        logger.info("The verifyAuthorizationSMS request succeeded, operation ID: "+request.getRequestObject().getOperationContext().getId());
        return new Response();
    }

}
