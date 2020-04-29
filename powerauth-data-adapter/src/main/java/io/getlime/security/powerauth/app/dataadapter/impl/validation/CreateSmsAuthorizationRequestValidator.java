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
package io.getlime.security.powerauth.app.dataadapter.impl.validation;

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.security.powerauth.app.dataadapter.exception.InvalidOperationContextException;
import io.getlime.security.powerauth.app.dataadapter.impl.service.OperationValueExtractionService;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.attribute.AmountAttribute;
import io.getlime.security.powerauth.lib.dataadapter.model.request.CreateSmsAuthorizationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.math.BigDecimal;

/**
 * Validator for SMS OTP authorization requests.
 *
 * Additional validation logic can be added if applicable.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
public class CreateSmsAuthorizationRequestValidator implements Validator {

    private static final String OPERATION_CONTEXT_FIELD = "requestObject.operationContext";
    private static final String AMOUNT_EMPTY_ERROR_CODE = "smsAuthorization.amount.empty";
    
    private final OperationValueExtractionService operationValueExtractionService;

    /**
     * Validator constructor.
     * @param operationValueExtractionService Operation form data service.
     */
    @Autowired
    public CreateSmsAuthorizationRequestValidator(OperationValueExtractionService operationValueExtractionService) {
        this.operationValueExtractionService = operationValueExtractionService;
    }

    /**
     * Return whether validator can validate given class.
     * @param clazz Validated class.
     * @return Whether validator can validate given class.
     */
    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return ObjectRequest.class.isAssignableFrom(clazz);
    }

    /**
     * Validate object and add validation errors.
     * @param o Validated object.
     * @param errors Errors object.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void validate(@Nullable Object o, @NonNull Errors errors) {
        ObjectRequest<CreateSmsAuthorizationRequest> requestObject = (ObjectRequest<CreateSmsAuthorizationRequest>) o;
        if (requestObject == null) {
            errors.rejectValue(OPERATION_CONTEXT_FIELD, "operationContext.missing");
            return;
        }
        CreateSmsAuthorizationRequest authRequest = requestObject.getRequestObject();

        // update validation logic based on the real Data Adapter requirements
        String userId = authRequest.getUserId();
        String organizationId = authRequest.getOrganizationId();
        OperationContext operationContext = authRequest.getOperationContext();
        if (operationContext == null) {
            errors.rejectValue(OPERATION_CONTEXT_FIELD, "operationContext.missing");
        }

        String operationName = authRequest.getOperationContext().getName();

        // Allow null user ID for case when fake SMS message is sent
        if (userId != null && userId.length() > 40) {
            errors.rejectValue("requestObject.userId", "smsAuthorization.userId.long");
        }

        // Allow null organization ID for case when fake SMS message is sent
        if (organizationId != null && organizationId.length() > 256) {
            errors.rejectValue("requestObject.organizationId", "smsAuthorization.organizationId.long");
        }

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "requestObject.operationContext.name", "smsAuthorization.operationName.empty");
        if (operationName != null && operationName.length() > 32) {
            errors.rejectValue("requestObject.operationContext.name", "smsAuthorization.operationName.long");
        }

        if (operationName == null) {
            return;
        }
        
        switch (operationName) {
            case "login":
            case "login_sca":
                // no field validation required
                break;
            case "authorize_payment":
            case "authorize_payment_sca":
                validateFieldsForPayment(authRequest, errors);
                break;
            default:
                throw new IllegalStateException("Unsupported operation in validator: " + operationName);
        }
    }
    
    private void validateFieldsForPayment(CreateSmsAuthorizationRequest authRequest, Errors errors) {
        AmountAttribute amountAttribute;
        try {
            amountAttribute = operationValueExtractionService.getAmount(authRequest.getOperationContext());
            if (amountAttribute == null) {
                errors.rejectValue(OPERATION_CONTEXT_FIELD, AMOUNT_EMPTY_ERROR_CODE);
            } else {
                BigDecimal amount = amountAttribute.getAmount();
                String currency = amountAttribute.getCurrency();

                if (amount == null) {
                    errors.rejectValue(OPERATION_CONTEXT_FIELD, AMOUNT_EMPTY_ERROR_CODE);
                } else if (amount.doubleValue() <= 0) {
                    errors.rejectValue(OPERATION_CONTEXT_FIELD, "smsAuthorization.amount.invalid");
                }

                if (currency == null || currency.isEmpty()) {
                    errors.rejectValue(OPERATION_CONTEXT_FIELD, "smsAuthorization.currency.empty");
                }
            }
        } catch (InvalidOperationContextException ex) {
            errors.rejectValue(OPERATION_CONTEXT_FIELD, AMOUNT_EMPTY_ERROR_CODE);
        }
        String account;
        try {
            account = operationValueExtractionService.getAccount(authRequest.getOperationContext());
            if (account == null || account.isEmpty()) {
                errors.rejectValue(OPERATION_CONTEXT_FIELD, "smsAuthorization.account.empty");
            }
        } catch (InvalidOperationContextException ex) {
            errors.rejectValue(OPERATION_CONTEXT_FIELD, "smsAuthorization.account.empty");
        }
    }
}
