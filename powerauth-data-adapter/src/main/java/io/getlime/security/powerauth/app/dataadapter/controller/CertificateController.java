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
import io.getlime.security.powerauth.app.dataadapter.impl.validation.CertificateRequestValidator;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AccountStatus;
import io.getlime.security.powerauth.lib.dataadapter.model.request.VerifyCertificateRequest;
import io.getlime.security.powerauth.lib.dataadapter.model.response.VerifyCertificateResponse;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
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
@RequestMapping("/api/auth/certificate")
public class CertificateController {

    private static final Logger logger = LoggerFactory.getLogger(CertificateController.class);

    private final CertificateRequestValidator requestValidator;
    private final DataAdapter dataAdapter;

    /**
     * Controller constructor.
     * @param requestValidator Validator for SMS requests.
     * @param dataAdapter Data adapter.
     */
    @Autowired
    public CertificateController(CertificateRequestValidator requestValidator, DataAdapter dataAdapter) {
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
     * Verify authorization code from SMS message.
     *
     * @param request Request data.
     * @return Authorization response.
     * @throws DataAdapterRemoteException Thrown in case communication with remote system fails.
     * @throws InvalidOperationContextException Thrown in case operation context is invalid.
     */
    @PostMapping(value = "verify")
    public ObjectResponse<VerifyCertificateResponse> verifyCertificate(@Valid @RequestBody ObjectRequest<VerifyCertificateRequest> request) throws InvalidOperationContextException, DataAdapterRemoteException {
        logger.info("Received verifyCertificate request, operation ID: {}", request.getRequestObject().getOperationContext().getId());
        VerifyCertificateRequest verifyRequest = request.getRequestObject();
        String clientCertificate = verifyRequest.getClientCertificate();
        AuthMethod authMethod = verifyRequest.getAuthMethod();
        String userId = verifyRequest.getUserId();
        String organizationId = verifyRequest.getOrganizationId();
        AccountStatus accountStatus = verifyRequest.getAccountStatus();
        OperationContext operationContext = verifyRequest.getOperationContext();
        // Verify authorization code
        VerifyCertificateResponse response = dataAdapter.verifyClientCertificate(userId, organizationId, clientCertificate, authMethod, accountStatus, operationContext);
        logger.info("The verifyCertificate request succeeded, operation ID: {}", request.getRequestObject().getOperationContext().getId());
        return new ObjectResponse<>(response);
    }

}
