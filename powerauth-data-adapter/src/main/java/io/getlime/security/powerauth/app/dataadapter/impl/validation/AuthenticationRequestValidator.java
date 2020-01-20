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
import io.getlime.security.powerauth.lib.dataadapter.model.entity.AuthenticationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.PasswordProtectionType;
import io.getlime.security.powerauth.lib.dataadapter.model.request.UserAuthenticationRequest;
import io.getlime.security.powerauth.lib.dataadapter.model.request.UserLookupRequest;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Defines validations for input fields in user lookup and authentication requests.
 *
 * Additional validation logic can be added if applicable.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
public class AuthenticationRequestValidator implements Validator {

    private static final String OPERATION_CONTEXT_FIELD = "requestObject.operationContext";
    private static final String MISSING_OPERATION_CONTEXT_ERROR_CODE = "operationContext.missing";
    private static final String PASS_FIELD = "requestObject.password";
    private static final String ORGANIZATION_ID_FIELD = "requestObject.organizationId";
    
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
        if (objectRequest.getRequestObject() instanceof UserLookupRequest) {
            validateUserLookupRequest(objectRequest, errors);
        } else if (objectRequest.getRequestObject() instanceof UserAuthenticationRequest) {
            validateUserAuthenticationRequest(objectRequest, errors);
        }
    }
    
    private void validateUserLookupRequest(ObjectRequest objectRequest, Errors errors) {
        UserLookupRequest authRequest = (UserLookupRequest) objectRequest.getRequestObject();

        // update validation logic based on the real Data Adapter requirements
        String username = authRequest.getUsername();
        String organizationId = authRequest.getOrganizationId();
        OperationContext operationContext = authRequest.getOperationContext();
        if (operationContext == null) {
            errors.rejectValue(OPERATION_CONTEXT_FIELD, MISSING_OPERATION_CONTEXT_ERROR_CODE);
        }
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "requestObject.username", "login.username.empty");
        if (username != null && username.length() > 30) {
            errors.rejectValue("requestObject.username", "login.username.long");
        }

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, ORGANIZATION_ID_FIELD, "login.organizationId.empty");
        if (organizationId != null && organizationId.length() > 256) {
            errors.rejectValue(ORGANIZATION_ID_FIELD, "login.organizationId.long");
        }
    }
    
    private void validateUserAuthenticationRequest(ObjectRequest objectRequest, Errors errors) {
        UserAuthenticationRequest authRequest = (UserAuthenticationRequest) objectRequest.getRequestObject();

        // update validation logic based on the real Data Adapter requirements
        String userId = authRequest.getUserId();
        String password = authRequest.getPassword();
        String organizationId = authRequest.getOrganizationId();
        OperationContext operationContext = authRequest.getOperationContext();
        if (operationContext == null) {
            errors.rejectValue(OPERATION_CONTEXT_FIELD, MISSING_OPERATION_CONTEXT_ERROR_CODE);
        }
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "requestObject.userId", "login.userId.empty");
        if (userId != null && userId.length() > 30) {
            errors.rejectValue("requestObject.userId", "login.userId.long");
        }

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, PASS_FIELD, "login.password.empty");
        AuthenticationContext authenticationContext = authRequest.getAuthenticationContext();
        PasswordProtectionType passwordProtection = authenticationContext.getPasswordProtection();
        if (passwordProtection == PasswordProtectionType.NO_PROTECTION) {
            if (password != null && password.length() > 30) {
                errors.rejectValue(PASS_FIELD, "login.password.long");
            }
        } else {
            // Allow longer values in password field when password is encrypted
            if (password != null && password.length() > 256) {
                errors.rejectValue(PASS_FIELD, "login.password.long");
            }
        }

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, ORGANIZATION_ID_FIELD, "login.organizationId.empty");
        if (userId != null && organizationId.length() > 256) {
            errors.rejectValue(ORGANIZATION_ID_FIELD, "login.organizationId.long");
        }

        if (passwordProtection != PasswordProtectionType.NO_PROTECTION && passwordProtection != PasswordProtectionType.PASSWORD_ENCRYPTION_AES) {
            errors.rejectValue("requestObject.authenticationContext", "login.type.unsupported");
        }
    }
}
