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
package io.getlime.security.powerauth.app.dataadapter.impl.service;

import io.getlime.security.powerauth.app.dataadapter.exception.InvalidOperationContextException;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormData;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.attribute.AmountAttribute;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.attribute.Attribute;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.attribute.KeyValueAttribute;
import org.springframework.stereotype.Service;

/**
 * Service which extracts form data from an operation based on required input for SMS text.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class OperationValueExtractionService {

    private static final String FIELD_ACCOUNT_ID = "operation.account";

    /**
     * Extract amount from operation context.
     *
     * @param operationContext Operation context.
     * @return Operation amount attribute.
     */
    public AmountAttribute getAmount(OperationContext operationContext) throws InvalidOperationContextException {
        FormData formData = operationContext.getFormData();
        if (formData == null || formData.getParameters() == null) {
            throw new InvalidOperationContextException("Operation form data is invalid");
        }
        return formData.getAmount();
    }

    /**
     * Extract account from operation context.
     *
     * @param operationContext Operation context.
     * @return Operation to account value.
     */
    public String getAccount(OperationContext operationContext) throws InvalidOperationContextException {
        FormData formData = operationContext.getFormData();
        if (formData == null || formData.getParameters() == null) {
            throw new InvalidOperationContextException("Operation form data is invalid");
        }
        Attribute accountAttr = formData.getAttributeById(FIELD_ACCOUNT_ID);
        if (accountAttr == null) {
            return null;
        }
        if (!(accountAttr instanceof KeyValueAttribute)) {
            throw new InvalidOperationContextException("Invalid account in operation form data");
        }
        return ((KeyValueAttribute) accountAttr).getValue();
    }

}
