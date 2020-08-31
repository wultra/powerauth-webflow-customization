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
package io.getlime.security.powerauth.app.dataadapter.impl.service;

import io.getlime.security.powerauth.app.dataadapter.exception.DataAdapterRemoteException;
import io.getlime.security.powerauth.app.dataadapter.exception.InvalidOperationContextException;
import io.getlime.security.powerauth.app.dataadapter.service.DataAdapterI18NService;
import io.getlime.security.powerauth.crypto.server.util.DataDigest;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.AuthorizationCode;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.attribute.AmountAttribute;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.SmsDeliveryResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Service for preparing and delivering SMS messages.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class SmsDeliveryService {

    private final DataAdapterI18NService dataAdapterI18NService;
    private final OperationValueExtractionService operationValueExtractionService;

    /**
     * Service constructor.
     * @param dataAdapterI18NService I18N service.
     * @param operationValueExtractionService Service for extracting values from operation.
     */
    public SmsDeliveryService(DataAdapterI18NService dataAdapterI18NService, OperationValueExtractionService operationValueExtractionService) {
        this.dataAdapterI18NService = dataAdapterI18NService;
        this.operationValueExtractionService = operationValueExtractionService;
    }

    /**
     * Generate authorization code for SMS authorization.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param operationContext Operation context.
     * @return Authorization code.
     * @throws InvalidOperationContextException Thrown when operation context is invalid.
     * @throws DataAdapterRemoteException Thrown when remote communication fails.
     */
    public AuthorizationCode generateAuthorizationCode(String userId, String organizationId, AuthMethod authMethod, OperationContext operationContext) throws InvalidOperationContextException, DataAdapterRemoteException {
        String operationName = operationContext.getName();
        List<String> digestItems = new ArrayList<>();
        switch (operationName) {
            case "login":
            case "login_sca":
                digestItems.add("login");
                break;

            case "authorize_payment":
            case "authorize_payment_sca":
                switch (authMethod) {
                    case LOGIN_SCA:
                        digestItems.add("login");
                        break;

                    case APPROVAL_SCA:
                    case SMS_KEY:
                    case POWERAUTH_TOKEN:
                        AmountAttribute amountAttribute = operationValueExtractionService.getAmount(operationContext);
                        String account = operationValueExtractionService.getAccount(operationContext);
                        BigDecimal amount = amountAttribute.getAmount();
                        String currency = amountAttribute.getCurrency();
                        digestItems.add(amount.toPlainString());
                        digestItems.add(currency);
                        digestItems.add(account);
                        break;

                    default:
                        throw new InvalidOperationContextException("Unsupported authentication method: " + authMethod);
                }
                break;

            // Add new operations here.
            default:
                throw new InvalidOperationContextException("Unsupported operation: " + operationName);
        }

        final DataDigest.Result digestResult = new DataDigest().generateDigest(digestItems);
        if (digestResult == null) {
            throw new InvalidOperationContextException("Digest generation failed");
        }
        return new AuthorizationCode(digestResult.getDigest(), digestResult.getSalt());
    }

    /**
     * Generate text for SMS authorization message.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param operationContext Operation context.
     * @param authorizationCode Authorization code.
     * @param lang Language for localization.
     * @return Generated SMS text with authorization code.
     * @throws InvalidOperationContextException Thrown when operation context is invalid.
     * @throws DataAdapterRemoteException Thrown when remote communication fails.
     */
    public String generateSmsText(String userId, String organizationId, AuthMethod authMethod, OperationContext operationContext, AuthorizationCode authorizationCode, String lang) throws InvalidOperationContextException, DataAdapterRemoteException {
        String operationName = operationContext.getName();
        String[] messageArgs;
        String messageResourcePrefix;
        switch (operationName) {
            case "login":
            case "login_sca":
                messageResourcePrefix = "login";
                messageArgs = new String[]{authorizationCode.getCode()};
                break;

            case "authorize_payment":
            case "authorize_payment_sca":
                switch (authMethod) {
                    case LOGIN_SCA:
                        messageResourcePrefix = "login";
                        messageArgs = new String[]{authorizationCode.getCode()};
                        break;

                    case APPROVAL_SCA:
                    case SMS_KEY:
                    case POWERAUTH_TOKEN:
                        messageResourcePrefix = "authorize_payment";
                        AmountAttribute amountAttribute = operationValueExtractionService.getAmount(operationContext);
                        String account = operationValueExtractionService.getAccount(operationContext);
                        BigDecimal amount = amountAttribute.getAmount();
                        String currency = amountAttribute.getCurrency();
                        messageArgs = new String[]{amount.toPlainString(), currency, account, authorizationCode.getCode()};
                        break;

                    default:
                        throw new InvalidOperationContextException("Unsupported authentication method: " + authMethod);
                }
                break;

            // Add new operations here.
            default:
                throw new InvalidOperationContextException("Unsupported operation: " + operationName);
        }

        return dataAdapterI18NService.messageSource().getMessage(messageResourcePrefix + ".smsText", messageArgs, new Locale(lang));
    }

    /**
     * Send an authorization SMS with generated authorization code.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param messageId Message ID.
     * @param messageText Text of SMS message.
     * @param operationContext Operation context.
     * @throws InvalidOperationContextException Thrown when operation context is invalid.
     * @throws DataAdapterRemoteException Thrown when remote communication fails or SMS message could not be delivered.
     */
    public SmsDeliveryResult sendAuthorizationSms(String userId, String organizationId, String messageId, String messageText, OperationContext operationContext) throws InvalidOperationContextException, DataAdapterRemoteException {
        // Add here code to send the SMS OTP message to user identified by userId with messageText.
        // The message entity can be extracted using message ID from table da_sms_authorization.
        // In case message delivery fails, throw a DataAdapterRemoteException.
        return SmsDeliveryResult.SUCCEEDED;
    }

}
