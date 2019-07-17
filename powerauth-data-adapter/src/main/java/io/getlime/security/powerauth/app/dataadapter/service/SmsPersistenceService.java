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
import io.getlime.security.powerauth.app.dataadapter.repository.SmsAuthorizationRepository;
import io.getlime.security.powerauth.app.dataadapter.repository.model.entity.SmsAuthorizationEntity;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.AuthorizationCode;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.SmsAuthorizationResult;
import io.getlime.security.powerauth.lib.dataadapter.model.response.VerifySmsAuthorizationResponse;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

/**
 * Service class for generating SMS with OTP authorization code and verification of authorization code.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class SmsPersistenceService {

    private final SmsAuthorizationRepository smsAuthorizationRepository;
    private final DataAdapterConfiguration dataAdapterConfiguration;

    /**
     * SMS persistence service constructor.
     * @param smsAuthorizationRepository SMS authorization repository.
     * @param dataAdapterConfiguration Data adapter configuration.
     */
    @Autowired
    public SmsPersistenceService(SmsAuthorizationRepository smsAuthorizationRepository, DataAdapterConfiguration dataAdapterConfiguration) {
        this.smsAuthorizationRepository = smsAuthorizationRepository;
        this.dataAdapterConfiguration = dataAdapterConfiguration;
    }

    /**
     * Create an authorization SMS message with OTP authorization code.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param messageId Message ID
     * @param operationContext Operation context.
     * @param authorizationCode Authorization code for SMS message.
     * @param messageText Localized SMS message text.
     * @return Created entity with SMS message details.
     */
    public SmsAuthorizationEntity createAuthorizationSms(String userId, String organizationId, String messageId, OperationContext operationContext,
                                                         AuthorizationCode authorizationCode, String messageText) {

        SmsAuthorizationEntity smsEntity = new SmsAuthorizationEntity();
        smsEntity.setMessageId(messageId);
        smsEntity.setOperationId(operationContext.getId());
        smsEntity.setUserId(userId);
        smsEntity.setOrganizationId(organizationId);
        smsEntity.setOperationName(operationContext.getName());
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
     * Verify an authorization code from SMS message.
     * @param messageId Message ID.
     * @param authorizationCode Authorization code.
     * @param allowMultipleVerifications Whether authorization code can be verified multiple times.
     * @return Result of SMS verification.
     */
    public VerifySmsAuthorizationResponse verifyAuthorizationSms(String messageId, String authorizationCode, boolean allowMultipleVerifications) {
        Optional<SmsAuthorizationEntity> smsEntityOptional = smsAuthorizationRepository.findById(messageId);
        VerifySmsAuthorizationResponse response = new VerifySmsAuthorizationResponse();
        if (!smsEntityOptional.isPresent()) {
            response.setSmsAuthorizationResult(SmsAuthorizationResult.VERIFIED_FAILED);
            response.setErrorMessage("smsAuthorization.invalidMessage");
            return response;
        }
        SmsAuthorizationEntity smsEntity = smsEntityOptional.get();
        // increase number of verification tries and save entity
        smsEntity.setVerifyRequestCount(smsEntity.getVerifyRequestCount() + 1);
        smsAuthorizationRepository.save(smsEntity);

        final Integer remainingAttempts = dataAdapterConfiguration.getSmsOtpMaxVerifyTriesPerMessage() - smsEntity.getVerifyRequestCount();

        if (smsEntity.getAuthorizationCode() == null || smsEntity.getAuthorizationCode().isEmpty()) {
            response.setSmsAuthorizationResult(SmsAuthorizationResult.VERIFIED_FAILED);
            response.setRemainingAttempts(remainingAttempts);
            response.setErrorMessage("smsAuthorization.invalidCode");
            return response;
        }
        if (smsEntity.isExpired()) {
            response.setSmsAuthorizationResult(SmsAuthorizationResult.VERIFIED_FAILED);
            response.setErrorMessage("smsAuthorization.expired");
            return response;
        }
        if (!allowMultipleVerifications && smsEntity.isVerified()) {
            response.setSmsAuthorizationResult(SmsAuthorizationResult.VERIFIED_FAILED);
            response.setErrorMessage("smsAuthorization.alreadyVerified");
            return response;
        }
        if (smsEntity.getVerifyRequestCount() > dataAdapterConfiguration.getSmsOtpMaxVerifyTriesPerMessage()) {
            response.setSmsAuthorizationResult(SmsAuthorizationResult.VERIFIED_FAILED);
            response.setErrorMessage("smsAuthorization.maxAttemptsExceeded");
            return response;
        }
        String authorizationCodeExpected = smsEntity.getAuthorizationCode();
        if (!authorizationCode.equals(authorizationCodeExpected)) {
            response.setSmsAuthorizationResult(SmsAuthorizationResult.VERIFIED_FAILED);
            response.setRemainingAttempts(remainingAttempts);
            response.setErrorMessage("smsAuthorization.failed");
            return response;
        }

        // SMS OTP authorization succeeded when this line is reached, update entity verification status
        smsEntity.setVerified(true);
        smsEntity.setTimestampVerified(new Date());
        smsAuthorizationRepository.save(smsEntity);

        response.setSmsAuthorizationResult(SmsAuthorizationResult.VERIFIED_SUCCEEDED);
        return response;
    }

}
