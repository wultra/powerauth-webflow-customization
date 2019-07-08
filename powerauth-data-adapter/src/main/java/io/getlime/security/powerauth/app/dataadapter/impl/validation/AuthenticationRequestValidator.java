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
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AuthenticationType;
import io.getlime.security.powerauth.lib.dataadapter.model.request.AuthenticationRequest;
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
            errors.rejectValue("requestObject.operationContext", "operationContext.missing");
            return;
        }

        if (objectRequest.getRequestObject() instanceof UserLookupRequest) {
            UserLookupRequest authRequest = (UserLookupRequest) objectRequest.getRequestObject();

            // update validation logic based on the real Data Adapter requirements
            String username = authRequest.getUsername();
            String organizationId = authRequest.getOrganizationId();
            OperationContext operationContext = authRequest.getOperationContext();
            if (operationContext == null) {
                errors.rejectValue("requestObject.operationContext", "operationContext.missing");
            }
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "requestObject.username", "login.username.empty");
            if (username!=null && username.length() > 30) {
                errors.rejectValue("requestObject.username", "login.username.long");
            }

            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "requestObject.organizationId", "login.organizationId.empty");
            if (organizationId!=null && organizationId.length() > 256) {
                errors.rejectValue("requestObject.organizationId", "login.organizationId.long");
            }
        } else if (objectRequest.getRequestObject() instanceof AuthenticationRequest) {
            AuthenticationRequest authRequest = (AuthenticationRequest) objectRequest.getRequestObject();

            // update validation logic based on the real Data Adapter requirements
            String userId = authRequest.getUserId();
            String password = authRequest.getPassword();
            String organizationId = authRequest.getOrganizationId();
            OperationContext operationContext = authRequest.getOperationContext();
            if (operationContext == null) {
                errors.rejectValue("requestObject.operationContext", "operationContext.missing");
            }
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "requestObject.userId", "login.userId.empty");
            if (userId != null && userId.length() > 30) {
                errors.rejectValue("requestObject.userId", "login.userId.long");
            }

            AuthenticationType authType = authRequest.getAuthenticationType();
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "requestObject.password", "login.password.empty");
            if (authType == AuthenticationType.BASIC) {
                if (password != null && password.length() > 30) {
                    errors.rejectValue("requestObject.password", "login.password.long");
                }
            } else {
                // Allow longer values in password field when password is encrypted
                if (password != null && password.length() > 256) {
                    errors.rejectValue("requestObject.password", "login.password.long");
                }
            }

            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "requestObject.organizationId", "login.organizationId.empty");
            if (userId != null && organizationId.length() > 256) {
                errors.rejectValue("requestObject.organizationId", "login.organizationId.long");
            }

            if (authType != AuthenticationType.BASIC && authType != AuthenticationType.PASSWORD_ENCRYPTION_AES) {
                errors.rejectValue("requestObject.authenticationType", "login.type.unsupported");
            }
        }
    }
}
