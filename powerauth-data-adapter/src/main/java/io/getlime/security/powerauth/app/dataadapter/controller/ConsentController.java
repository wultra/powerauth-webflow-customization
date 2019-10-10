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
import io.getlime.security.powerauth.app.dataadapter.exception.InvalidConsentDataException;
import io.getlime.security.powerauth.app.dataadapter.exception.InvalidOperationContextException;
import io.getlime.security.powerauth.app.dataadapter.impl.validation.ConsentFormRequestValidator;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.ConsentOption;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.request.CreateConsentFormRequest;
import io.getlime.security.powerauth.lib.dataadapter.model.request.InitConsentFormRequest;
import io.getlime.security.powerauth.lib.dataadapter.model.request.SaveConsentFormRequest;
import io.getlime.security.powerauth.lib.dataadapter.model.request.ValidateConsentFormRequest;
import io.getlime.security.powerauth.lib.dataadapter.model.response.CreateConsentFormResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.InitConsentFormResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.SaveConsentFormResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.ValidateConsentFormResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Controller class which handles OAuth 2.0 consent actions.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("/api/auth/consent")
public class ConsentController {

    private static final Logger logger = LoggerFactory.getLogger(ConsentController.class);

    private final DataAdapter dataAdapter;
    private final ConsentFormRequestValidator requestValidator;

    /**
     * Consent controller constructor.
     * @param dataAdapter Data adapter.
     * @param requestValidator Request validator.
     */
    @Autowired
    public ConsentController(DataAdapter dataAdapter, ConsentFormRequestValidator requestValidator) {
        this.dataAdapter = dataAdapter;
        this.requestValidator = requestValidator;
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
     * Initialize OAuth 2.0 consent form - verify that consent form is required.
     * @param request Initialize consent form request.
     * @return Initialize consent form response.
     * @throws DataAdapterRemoteException In case communication with remote system fails.
     * @throws InvalidOperationContextException In case operation context is invalid.
     */
    @PostMapping(value = "/init")
    public ObjectResponse<InitConsentFormResponse> initConsentForm(@Valid @RequestBody ObjectRequest<InitConsentFormRequest> request) throws DataAdapterRemoteException, InvalidOperationContextException {
        logger.info("Received initConsentForm request for user: {}, operation ID: {}",
                request.getRequestObject().getUserId(), request.getRequestObject().getOperationContext().getId());
        InitConsentFormRequest createRequest = request.getRequestObject();
        String userId = createRequest.getUserId();
        String organizationId = createRequest.getOrganizationId();
        OperationContext operationContext = createRequest.getOperationContext();
        InitConsentFormResponse response = dataAdapter.initConsentForm(userId, organizationId, operationContext);
        logger.debug("The initConsentForm request succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * Create OAuth 2.0 consent form.
     * @param request Create consent form request.
     * @return Create consent form response.
     * @throws DataAdapterRemoteException In case communication with remote system fails.
     * @throws InvalidOperationContextException In case operation context is invalid.
     */
    @PostMapping(value = "/create")
    public ObjectResponse<CreateConsentFormResponse> createConsentForm(@Valid @RequestBody ObjectRequest<CreateConsentFormRequest> request) throws DataAdapterRemoteException, InvalidOperationContextException {
        logger.info("Received createConsentForm request for user: {}, operation ID: {}",
                request.getRequestObject().getUserId(), request.getRequestObject().getOperationContext().getId());
        CreateConsentFormRequest createRequest = request.getRequestObject();
        String userId = createRequest.getUserId();
        String organizationId = createRequest.getOrganizationId();
        OperationContext operationContext = createRequest.getOperationContext();
        String lang = createRequest.getLang();
        CreateConsentFormResponse response = dataAdapter.createConsentForm(userId, organizationId, operationContext, lang);
        logger.debug("The createConsent request succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * Validate OAuth 2.0 consent form.
     * @param request Validate consent form request.
     * @return Validate consent form response.
     * @throws DataAdapterRemoteException In case communication with remote system fails.
     * @throws InvalidOperationContextException In case operation context is invalid.
     * @throws InvalidConsentDataException In case consent options are invalid.
     */
    @PostMapping(value = "/validate")
    public ObjectResponse<ValidateConsentFormResponse> validateConsentForm(@Valid @RequestBody ObjectRequest<ValidateConsentFormRequest> request) throws DataAdapterRemoteException, InvalidOperationContextException, InvalidConsentDataException {
        logger.info("Received validateConsentForm request for user: {}, operation ID: {}",
                request.getRequestObject().getUserId(), request.getRequestObject().getOperationContext().getId());
        ValidateConsentFormRequest validateRequest = request.getRequestObject();
        String userId = validateRequest.getUserId();
        String organizationId = validateRequest.getOrganizationId();
        OperationContext operationContext = validateRequest.getOperationContext();
        String lang = validateRequest.getLang();
        List<ConsentOption> options = validateRequest.getOptions();
        ValidateConsentFormResponse response = dataAdapter.validateConsentForm(userId, organizationId, operationContext, lang, options);
        logger.debug("The validateConsentForm request succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * Save OAuth 2.0 consent form.
     * @param request Save consent form request.
     * @return Save consent form response.
     * @throws DataAdapterRemoteException In case communication with remote system fails.
     * @throws InvalidOperationContextException In case operation context is invalid.
     * @throws InvalidConsentDataException In case consent options are invalid.
     */
    @PostMapping(value = "/save")
    public ObjectResponse<SaveConsentFormResponse> saveConsentForm(@Valid @RequestBody ObjectRequest<SaveConsentFormRequest> request) throws DataAdapterRemoteException, InvalidOperationContextException, InvalidConsentDataException {
        logger.info("Received saveConsentForm request for user: {}, operation ID: {}",
                request.getRequestObject().getUserId(), request.getRequestObject().getOperationContext().getId());
        SaveConsentFormRequest saveRequest = request.getRequestObject();
        String userId = saveRequest.getUserId();
        String organizationId = saveRequest.getOrganizationId();
        OperationContext operationContext = saveRequest.getOperationContext();
        List<ConsentOption> options = saveRequest.getOptions();
        SaveConsentFormResponse response = dataAdapter.saveConsentForm(userId, organizationId, operationContext, options);
        logger.debug("The saveConsentForm request succeeded");
        return new ObjectResponse<>(response);
    }

}
