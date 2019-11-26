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
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AccountStatus;
import io.getlime.security.powerauth.lib.dataadapter.model.request.AfsRequestParameters;
import io.getlime.security.powerauth.lib.dataadapter.model.response.*;

import java.util.List;
import java.util.Map;

/**
 * Interface defines methods which should be implemented for integration of Web Flow with 3rd parties.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public interface DataAdapter {

    /**
     * Lookup user account - map username to user ID.
     * @param username Username which user uses for authentication.
     * @param organizationId Organization ID for this request.
     * @param operationContext Operation context.
     * @return Detail about the user.
     * @throws DataAdapterRemoteException Thrown when remote communication fails.
     * @throws UserNotFoundException Thrown when user does not exist.
     */
    UserDetailResponse lookupUser(String username, String organizationId, OperationContext operationContext) throws DataAdapterRemoteException, UserNotFoundException;

    /**
     * Authenticate user using provided credentials.
     * @param userId User ID for user authentication.
     * @param password Password for user authentication.
     * @param authenticationContext Authentication context.
     * @param organizationId Organization ID.
     * @param operationContext Operation context.
     * @return User authentication result.
     * @throws DataAdapterRemoteException Thrown when remote communication fails.
     */
    UserAuthenticationResponse authenticateUser(String userId, String password, AuthenticationContext authenticationContext, String organizationId, OperationContext operationContext) throws DataAdapterRemoteException;

    /**
     * Fetch user detail for given user.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param operationContext Operation context which can be null in case request is initiated outside of operation scope.
     * @return Response with user details.
     * @throws DataAdapterRemoteException Thrown when remote communication fails.
     * @throws UserNotFoundException Thrown when user does not exist.
     */
    UserDetailResponse fetchUserDetail(String userId, String organizationId, OperationContext operationContext) throws DataAdapterRemoteException, UserNotFoundException;

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
     * Create a new implicit login operation. This method is used in situations when operation
     * is not created yet and default login operation needs to be created with a correct
     * application context.
     * @param clientId OAuth 2.0 client ID.
     * @param scopes OAuth 2.0 scopes.
     * @return Context of the operation that should be created.
     * @throws DataAdapterRemoteException Thrown when remote communication fails.
     */
    CreateImplicitLoginOperationResponse createImplicitLoginOperation(String clientId, String[] scopes) throws DataAdapterRemoteException;

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
     * Create authorization SMS message and send it.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param accountStatus User account status.
     * @param operationContext Operation context.
     * @param lang Language for localization.
     * @return Message ID.
     * @throws InvalidOperationContextException Thrown when operation context is invalid.
     * @throws DataAdapterRemoteException Thrown when remote communication fails or SMS message could not be delivered.
     */
    CreateSmsAuthorizationResponse createAndSendAuthorizationSms(String userId, String organizationId, AccountStatus accountStatus, OperationContext operationContext, String lang) throws InvalidOperationContextException, DataAdapterRemoteException;

    /**
     * Verify authorization code from SMS message.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param accountStatus Current user account status.
     * @param messageId Message ID.
     * @param authorizationCode Authorization code.
     * @param operationContext Operation context.
     * @return SMS authorization code verification response.
     * @throws DataAdapterRemoteException Thrown when remote communication fails.
     * @throws InvalidOperationContextException Thrown when operation context is invalid.
     */
    VerifySmsAuthorizationResponse verifyAuthorizationSms(String userId, String organizationId, AccountStatus accountStatus, String messageId, String authorizationCode, OperationContext operationContext) throws DataAdapterRemoteException, InvalidOperationContextException;

    /**
     * Verify authorization code from SMS message together with user password.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param accountStatus Current user account status.
     * @param messageId Message ID.
     * @param authorizationCode Authorization code.
     * @param operationContext Operation context.
     * @param authenticationContext Authentication context.
     * @param password User password.
     * @return SMS authorization code and password verification response.
     * @throws DataAdapterRemoteException Thrown when remote communication fails.
     * @throws InvalidOperationContextException Thrown when operation context is invalid.
     */
    VerifySmsAndPasswordResponse verifyAuthorizationSmsAndPassword(String userId, String organizationId, AccountStatus accountStatus, String messageId, String authorizationCode, OperationContext operationContext, AuthenticationContext authenticationContext, String password) throws DataAdapterRemoteException, InvalidOperationContextException;

    /**
     * Decide whether OAuth 2.0 consent form should be displayed based on operation context.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param operationContext Operation context.
     * @return Response with information whether consent form should be displayed.
     * @throws DataAdapterRemoteException Thrown when remote communication fails.
     * @throws InvalidOperationContextException Thrown when operation context is invalid.
     */
    InitConsentFormResponse initConsentForm(String userId, String organizationId, OperationContext operationContext) throws DataAdapterRemoteException, InvalidOperationContextException;

    /**
     * Create OAuth 2.0 consent form - prepare HTML text of consent form and add form options.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param operationContext Operation context.
     * @param lang Language to use for the text of the consent form.
     * @return Consent form contents with HTML text and form options.
     * @throws DataAdapterRemoteException Thrown when remote communication fails.
     * @throws InvalidOperationContextException Thrown when operation context is invalid.
     */
    CreateConsentFormResponse createConsentForm(String userId, String organizationId, OperationContext operationContext, String lang) throws DataAdapterRemoteException, InvalidOperationContextException;

    /**
     * Validate consent form values and generate response with validation result with optional error messages in case validation fails.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param operationContext Operation context.
     * @param lang Language to use for error messages.
     * @param options Options selected by the user.
     * @return Consent form validation result with optional error messages in case validation fails.
     * @throws DataAdapterRemoteException Thrown when remote communication fails.
     * @throws InvalidOperationContextException Thrown when operation context is invalid.
     * @throws InvalidConsentDataException In case consent options are invalid.
     */
    ValidateConsentFormResponse validateConsentForm(String userId, String organizationId, OperationContext operationContext, String lang, List<ConsentOption> options) throws DataAdapterRemoteException, InvalidOperationContextException, InvalidConsentDataException;

    /**
     * Save consent form options selected by the user for an operation.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param operationContext Operation context.
     * @param options Options selected by the user.
     * @return Response with result of saving the consent form.
     * @throws DataAdapterRemoteException Thrown when remote communication fails.
     * @throws InvalidOperationContextException Thrown when operation context is invalid.
     * @throws InvalidConsentDataException In case consent options are invalid.
     */
    SaveConsentFormResponse saveConsentForm(String userId, String organizationId, OperationContext operationContext, List<ConsentOption> options) throws DataAdapterRemoteException, InvalidOperationContextException, InvalidConsentDataException;

    /**
     * Execute an anti-fraud system action and return response for usage in Web Flow.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param operationContext Operation context.
     * @param afsRequestParameters Request parameters for AFS.
     * @param extras Extra parameters for AFS.
     * @return Response from AFS for usage in Web Flow.
     * @throws DataAdapterRemoteException Thrown when remote communication fails.
     * @throws InvalidOperationContextException Thrown when operation context is invalid.
     */
    AfsResponse executeAfsAction(String userId, String organizationId, OperationContext operationContext, AfsRequestParameters afsRequestParameters, Map<String, Object> extras) throws DataAdapterRemoteException, InvalidOperationContextException;

}
