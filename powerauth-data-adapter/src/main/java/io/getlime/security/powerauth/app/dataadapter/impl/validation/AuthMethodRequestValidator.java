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
package io.getlime.security.powerauth.app.dataadapter.impl.validation;

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.request.InitAuthMethodRequest;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Defines validations for input fields in authentication method requests.
 *
 * Additional validation logic can be added if applicable.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
public class AuthMethodRequestValidator implements Validator {

    private static final String OPERATION_CONTEXT_FIELD = "requestObject.operationContext";
    private static final String MISSING_OPERATION_CONTEXT_ERROR_CODE = "operationContext.missing";
    private static final String USER_ID_FIELD = "requestObject.userId";
    private static final String ERROR_USER_ID_LONG = "login.userId.long";
    private static final String ORGANIZATION_ID_FIELD = "requestObject.organizationId";
    private static final String ERROR_ORGANIZATION_ID_LONG = "login.organizationId.long";
    private static final String AUTH_METHOD_FIELD = "requestObject.authMethod";
    private static final String INVALID_REQUEST_MESSAGE = "error.invalidRequest";

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
    public void validate(@Nullable Object o, @NonNull Errors errors) {
        ObjectRequest objectRequest = (ObjectRequest) o;
        if (objectRequest == null) {
            errors.rejectValue(OPERATION_CONTEXT_FIELD, MISSING_OPERATION_CONTEXT_ERROR_CODE);
            return;
        }
        if (objectRequest.getRequestObject() instanceof InitAuthMethodRequest) {
            validateInitAuthMethodRequest((InitAuthMethodRequest) objectRequest.getRequestObject(), errors);
        }
    }

    /**
     * Validate authentication method initialization request.
     * @param authRequest Authentication method initialization request.
     * @param errors Errors object.
     */
    private void validateInitAuthMethodRequest(InitAuthMethodRequest authRequest, Errors errors) {
        // update validation logic based on the real Data Adapter requirements
        String userId = authRequest.getUserId();
        String organizationId = authRequest.getOrganizationId();
        AuthMethod authMethod = authRequest.getAuthMethod();
        OperationContext operationContext = authRequest.getOperationContext();
        // allow empty user ID and organization ID before user is authenticated
        if (userId != null && userId.length() > 40) {
            errors.rejectValue(USER_ID_FIELD, ERROR_USER_ID_LONG);
        }
        if (organizationId != null && organizationId.length() > 256) {
            errors.rejectValue(ORGANIZATION_ID_FIELD, ERROR_ORGANIZATION_ID_LONG);
        }
        if (operationContext == null) {
            errors.rejectValue(OPERATION_CONTEXT_FIELD, MISSING_OPERATION_CONTEXT_ERROR_CODE);
        }
        if (authMethod == null) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, AUTH_METHOD_FIELD, INVALID_REQUEST_MESSAGE);
        }
    }
}
