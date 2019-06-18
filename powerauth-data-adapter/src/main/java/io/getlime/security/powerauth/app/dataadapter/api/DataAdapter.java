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
package io.getlime.security.powerauth.app.dataadapter.api;

import io.getlime.security.powerauth.app.dataadapter.exception.*;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.*;
import io.getlime.security.powerauth.lib.dataadapter.model.response.*;

import java.util.List;

/**
 * Interface defines methods which should be implemented for integration of Web Flow with 3rd parties.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public interface DataAdapter {

    /**
     * Authenticate user using provided credentials.
     *
     * @param username Username for user authentication.
     * @param password Password for user authentication.
     * @param organizationId Organization ID.
     * @param operationContext Operation context.
     * @return UserDetailResponse Response with user details.
     * @throws DataAdapterRemoteException Thrown when remote communication fails.
     * @throws AuthenticationFailedException Thrown when authentication fails.
     */
    UserDetailResponse authenticateUser(String username, String password, String organizationId, OperationContext operationContext) throws DataAdapterRemoteException, AuthenticationFailedException;

    /**
     * Fetch user detail for given user.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @return Response with user details.
     * @throws DataAdapterRemoteException Thrown when remote communication fails.
     * @throws UserNotFoundException Thrown when user does not exist.
     */
    UserDetailResponse fetchUserDetail(String userId, String organizationId) throws DataAdapterRemoteException, UserNotFoundException;

    /**
     * Decorate operation form data.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param operationContext Operation context.
     * @return Response with decorated operation form data
     * @throws DataAdapterRemoteException Thrown when remote communication fails.
     * @throws UserNotFoundException Thrown when user does not exist.
     */
    DecorateOperationFormDataResponse decorateFormData(String userId, String organizationId, OperationContext operationContext) throws DataAdapterRemoteException, UserNotFoundException;

    /**
     * Receive notification about form data change.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param formDataChange Form data change.
     * @param operationContext Operation context.
     * @throws DataAdapterRemoteException Thrown when remote communication fails.
     */
    void formDataChangedNotification(String userId, String organizationId, FormDataChange formDataChange, OperationContext operationContext) throws DataAdapterRemoteException;

    /**
     * Receive notification about operation change.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param operationChange Operation change.
     * @param operationContext Operation context.
     * @throws DataAdapterRemoteException Thrown when remote communication fails.
     */
    void operationChangedNotification(String userId, String organizationId, OperationChange operationChange, OperationContext operationContext) throws DataAdapterRemoteException;

    /**
     * Generate authorization code for SMS authorization.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param operationContext Operation context.
     * @return Authorization code.
     * @throws InvalidOperationContextException Thrown when operation context is invalid.
     */
    AuthorizationCode generateAuthorizationCode(String userId, String organizationId, OperationContext operationContext) throws InvalidOperationContextException;

    /**
     * Generate text for SMS authorization.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param operationContext Operation context.
     * @param authorizationCode Authorization code.
     * @param lang Language for localization.
     * @return Generated SMS text with OTP authorization code.
     * @throws InvalidOperationContextException Thrown when operation context is invalid.
     */
    String generateSMSText(String userId, String organizationId, OperationContext operationContext, AuthorizationCode authorizationCode, String lang) throws InvalidOperationContextException;

    /**
     * Send an authorization SMS with generated OTP.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param messageText Text of SMS message.
     * @param operationContext Operation context.
     * @throws DataAdapterRemoteException Thrown when remote communication fails.
     * @throws SMSAuthorizationFailedException Thrown when message could not be created.
     */
    void sendAuthorizationSMS(String userId, String organizationId, String messageText, OperationContext operationContext) throws DataAdapterRemoteException, SMSAuthorizationFailedException;

    /**
     * Create OAuth 2.0 consent form - prepare HTML text of consent form and add form options.
     * @param userId User ID.
     * @param operationContext Operation context.
     * @param lang Language to use for the text of the consent form.
     * @return Consent form contents with HTML text and form options.
     * @throws DataAdapterRemoteException Thrown when remote communication fails.
     * @throws InvalidOperationContextException Thrown when operation context is invalid.
     */
    CreateConsentFormResponse createConsentForm(String userId, OperationContext operationContext, String lang) throws DataAdapterRemoteException, InvalidOperationContextException;

    /**
     * Validate consent form values and generate response with validation result with optional error messages in case validation fails.
     * @param userId User ID.
     * @param operationContext Operation context.
     * @param lang Language to use for error messages.
     * @param options Options selected by the user.
     * @return Consent form validation result with optional error messages in case validation fails.
     * @throws DataAdapterRemoteException Thrown when remote communication fails.
     * @throws InvalidOperationContextException Thrown when operation context is invalid.
     * @throws InvalidConsentDataException In case consent options are invalid.
     */
    ValidateConsentFormResponse validateConsentForm(String userId, OperationContext operationContext, String lang, List<ConsentOption> options) throws DataAdapterRemoteException, InvalidOperationContextException, InvalidConsentDataException;

    /**
     * Save consent form options selected by the user for an operation.
     * @param userId User ID.
     * @param operationContext Operation context.
     * @param options Options selected by the user.
     * @throws DataAdapterRemoteException Thrown when remote communication fails.
     * @throws InvalidOperationContextException Thrown when operation context is invalid.
     * @throws InvalidConsentDataException In case consent options are invalid.
     */
    SaveConsentFormResponse saveConsentForm(String userId, OperationContext operationContext, List<ConsentOption> options) throws DataAdapterRemoteException, InvalidOperationContextException, InvalidConsentDataException;

}
