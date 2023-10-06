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
import io.getlime.security.powerauth.lib.dataadapter.model.entity.ConsentOption;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.request.CreateConsentFormRequest;
import io.getlime.security.powerauth.lib.dataadapter.model.request.InitConsentFormRequest;
import io.getlime.security.powerauth.lib.dataadapter.model.request.SaveConsentFormRequest;
import io.getlime.security.powerauth.lib.dataadapter.model.request.ValidateConsentFormRequest;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.List;

/**
 * Validator for request to create OAuth 2.0 consent form.
 * <p>
 * Additional validation logic can be added if applicable.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
public class ConsentFormRequestValidator implements Validator {

    private static final String INVALID_REQUEST_MESSAGE = "consent.invalidRequest";
    
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
        final ObjectRequest<?> objectRequest = (ObjectRequest<?>) o;
        if (objectRequest == null) {
            errors.rejectValue("requestObject.operationContext", "operationContext.missing");
            return;
        }

        // update validation logic based on the real Data Adapter requirements
        if (objectRequest.getRequestObject() instanceof InitConsentFormRequest) {
            ObjectRequest<InitConsentFormRequest> requestObject = (ObjectRequest<InitConsentFormRequest>) o;
            InitConsentFormRequest request = requestObject.getRequestObject();
            validateOperationContext(request.getOperationContext(), errors);
            validateUserId(request.getUserId(), errors);
            if (request.getOperationContext() != null) {
                validateOperationName(request.getOperationContext().getName(), errors);
            }
        } else if (objectRequest.getRequestObject() instanceof CreateConsentFormRequest) {
            ObjectRequest<CreateConsentFormRequest> requestObject = (ObjectRequest<CreateConsentFormRequest>) o;
            CreateConsentFormRequest request = requestObject.getRequestObject();
            validateOperationContext(request.getOperationContext(), errors);
            validateUserId(request.getUserId(), errors);
            if (request.getOperationContext() != null) {
                validateOperationName(request.getOperationContext().getName(), errors);
            }
            validateLanguage(errors);
        } else if (objectRequest.getRequestObject() instanceof ValidateConsentFormRequest) {
            ObjectRequest<ValidateConsentFormRequest> requestObject = (ObjectRequest<ValidateConsentFormRequest>) o;
            ValidateConsentFormRequest request = requestObject.getRequestObject();
            validateOperationContext(request.getOperationContext(), errors);
            validateUserId(request.getUserId(), errors);
            if (request.getOperationContext() != null) {
                validateOperationName(request.getOperationContext().getName(), errors);
            }
            validateLanguage(errors);
            validateOptions(request.getOptions(), errors);
        } else if (objectRequest.getRequestObject() instanceof SaveConsentFormRequest) {
            ObjectRequest<SaveConsentFormRequest> requestObject = (ObjectRequest<SaveConsentFormRequest>) o;
            SaveConsentFormRequest request = requestObject.getRequestObject();
            validateOperationContext(request.getOperationContext(), errors);
            validateUserId(request.getUserId(), errors);
            if (request.getOperationContext() != null) {
                validateOperationName(request.getOperationContext().getName(), errors);
            }
            validateOptions(request.getOptions(), errors);
        }
    }

    private void validateOperationContext(OperationContext operationContext, Errors errors) {
        if (operationContext == null) {
            errors.rejectValue("requestObject.operationContext", "operationContext.missing");
        }
    }

    private void validateUserId(String userId, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "requestObject.userId", INVALID_REQUEST_MESSAGE);
        if (userId != null && userId.length() > 40) {
            errors.rejectValue("requestObject.userId", INVALID_REQUEST_MESSAGE);
        }
    }

    private void validateOperationName(String operationName, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "requestObject.operationContext.name", INVALID_REQUEST_MESSAGE);
        if (operationName != null && operationName.length() > 32) {
            errors.rejectValue("requestObject.operationContext.name", INVALID_REQUEST_MESSAGE);
        }
    }

    private void validateLanguage(Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "requestObject.lang", INVALID_REQUEST_MESSAGE);
        // Do not validate lang parameter, fallback to "en" instead, see: https://github.com/wultra/powerauth-webflow-customization/issues/104
    }

    private void validateOptions(List<ConsentOption> options, Errors errors) {
        // Allow empty options, but do not allow null value
        if (options == null) {
            errors.rejectValue("requestObject.options", INVALID_REQUEST_MESSAGE);
        }
    }
}
