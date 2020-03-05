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
import io.getlime.security.powerauth.lib.dataadapter.model.request.VerifyCertificateRequest;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for certificate verification requests.
 *
 * Additional validation logic can be added if applicable.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
public class CertificateRequestValidator implements Validator {

    private static final String OPERATION_CONTEXT_FIELD = "requestObject.operationContext";

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
        ObjectRequest<VerifyCertificateRequest> requestObject = (ObjectRequest<VerifyCertificateRequest>) o;
        if (requestObject == null) {
            errors.reject("error.invalidRequest");
            return;
        }
        VerifyCertificateRequest authRequest = requestObject.getRequestObject();

        // update validation logic based on the real Data Adapter requirements
        OperationContext operationContext = authRequest.getOperationContext();
        if (operationContext == null) {
            errors.rejectValue(OPERATION_CONTEXT_FIELD, "operationContext.missing");
        }

        // TODO - add validations

    }
        
}
