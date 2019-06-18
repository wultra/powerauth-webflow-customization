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
package io.getlime.security.powerauth.app.dataadapter.service;

import io.getlime.security.powerauth.app.dataadapter.configuration.DataAdapterConfiguration;
import io.getlime.security.powerauth.app.dataadapter.exception.InvalidOperationContextException;
import io.getlime.security.powerauth.app.dataadapter.exception.SMSAuthorizationFailedException;
import io.getlime.security.powerauth.app.dataadapter.impl.service.DataAdapterService;
import io.getlime.security.powerauth.app.dataadapter.repository.SMSAuthorizationRepository;
import io.getlime.security.powerauth.app.dataadapter.repository.model.entity.SMSAuthorizationEntity;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.AuthorizationCode;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for generating SMS with OTP authorization code and verification of authorization code.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class SMSPersistenceService {

    private final DataAdapterService dataAdapterService;
    private final SMSAuthorizationRepository smsAuthorizationRepository;
    private final DataAdapterConfiguration dataAdapterConfiguration;

    /**
     * SMS persistence service constructor.
     * @param dataAdapterService Data adapter service.
     * @param smsAuthorizationRepository SMS authorization repository.
     * @param dataAdapterConfiguration Data adapter configuration.
     */
    @Autowired
    public SMSPersistenceService(DataAdapterService dataAdapterService, SMSAuthorizationRepository smsAuthorizationRepository, DataAdapterConfiguration dataAdapterConfiguration) {
        this.dataAdapterService = dataAdapterService;
        this.smsAuthorizationRepository = smsAuthorizationRepository;
        this.dataAdapterConfiguration = dataAdapterConfiguration;
    }

    /**
     * Create an authorization SMS message with OTP authorization code.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param operationContext Operation context.
     * @param lang Language for message text.
     * @return Created entity with SMS message details.
     */
    public SMSAuthorizationEntity createAuthorizationSMS(String userId, String organizationId, OperationContext operationContext, String lang) throws InvalidOperationContextException {
        String operationId = operationContext.getId();
        String operationName = operationContext.getName();

        // messageId is generated as random UUID, it can be overridden to provide a real message identification
        String messageId = UUID.randomUUID().toString();

        // generate authorization code
        AuthorizationCode authorizationCode = dataAdapterService.generateAuthorizationCode(userId, organizationId, operationContext);

        // generate message text, include previously generated authorization code
        String messageText = dataAdapterService.generateSMSText(userId, organizationId, operationContext, authorizationCode, lang);

        SMSAuthorizationEntity smsEntity = new SMSAuthorizationEntity();
        smsEntity.setMessageId(messageId);
        smsEntity.setOperationId(operationId);
        smsEntity.setUserId(userId);
        smsEntity.setOrganizationId(organizationId);
        smsEntity.setOperationName(operationName);
        smsEntity.setAuthorizationCode(authorizationCode.getCode());
        smsEntity.setSalt(authorizationCode.getSalt());
        smsEntity.setMessageText(messageText);
        smsEntity.setVerifyRequestCount(0);
        smsEntity.setTimestampCreated(new Date());
        smsEntity.setTimestampExpires(new DateTime().plusSeconds(dataAdapterConfiguration.getSmsOtpExpirationTime()).toDate());
        smsEntity.setTimestampVerified(null);
        smsEntity.setVerified(false);

        // store entity in database
        smsAuthorizationRepository.save(smsEntity);

        return smsEntity;
    }

    /**
     * Verify an OTP authorization code.
     * @param messageId Message ID.
     * @param authorizationCode Authorization code.
     * @throws SMSAuthorizationFailedException Thrown when SMS authorization fails.
     */
    public void verifyAuthorizationSMS(String messageId, String authorizationCode) throws SMSAuthorizationFailedException {
        Optional<SMSAuthorizationEntity> smsEntityOptional = smsAuthorizationRepository.findById(messageId);
        if (!smsEntityOptional.isPresent()) {
            throw new SMSAuthorizationFailedException("smsAuthorization.invalidMessage");
        }
        SMSAuthorizationEntity smsEntity = smsEntityOptional.get();
        // increase number of verification tries and save entity
        smsEntity.setVerifyRequestCount(smsEntity.getVerifyRequestCount() + 1);
        smsAuthorizationRepository.save(smsEntity);

        final Integer remainingAttempts = dataAdapterConfiguration.getSmsOtpMaxVerifyTriesPerMessage() - smsEntity.getVerifyRequestCount();

        if (smsEntity.getAuthorizationCode() == null || smsEntity.getAuthorizationCode().isEmpty()) {
            SMSAuthorizationFailedException ex = new SMSAuthorizationFailedException("smsAuthorization.invalidCode");
            ex.setRemainingAttempts(remainingAttempts);
            throw ex;
        }
        if (smsEntity.isExpired()) {
            throw new SMSAuthorizationFailedException("smsAuthorization.expired");
        }
        if (smsEntity.isVerified()) {
            throw new SMSAuthorizationFailedException("smsAuthorization.alreadyVerified");
        }
        if (smsEntity.getVerifyRequestCount() > dataAdapterConfiguration.getSmsOtpMaxVerifyTriesPerMessage()) {
            throw new SMSAuthorizationFailedException("smsAuthorization.maxAttemptsExceeded");
        }
        String authorizationCodeExpected = smsEntity.getAuthorizationCode();
        if (!authorizationCode.equals(authorizationCodeExpected)) {
            SMSAuthorizationFailedException ex = new SMSAuthorizationFailedException("smsAuthorization.failed");
            ex.setRemainingAttempts(remainingAttempts);
            throw ex;
        }

        // SMS OTP authorization succeeded when this line is reached, update entity verification status
        smsEntity.setVerified(true);
        smsEntity.setTimestampVerified(new Date());
        smsAuthorizationRepository.save(smsEntity);
    }

}
