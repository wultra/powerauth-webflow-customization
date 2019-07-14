package io.getlime.security.powerauth.app.dataadapter.impl.service;

import io.getlime.security.powerauth.app.dataadapter.api.DataAdapter;
import io.getlime.security.powerauth.app.dataadapter.exception.*;
import io.getlime.security.powerauth.app.dataadapter.service.DataAdapterI18NService;
import io.getlime.security.powerauth.app.dataadapter.service.SmsPersistenceService;
import io.getlime.security.powerauth.crypto.server.util.DataDigest;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.*;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.attribute.AmountAttribute;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.attribute.FormFieldConfig;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.PasswordProtectionType;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.SmsAuthorizationResult;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.UserAuthenticationResult;
import io.getlime.security.powerauth.lib.dataadapter.model.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Sample implementation of DataAdapter interface which should be updated in real implementation.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class DataAdapterService implements DataAdapter {

    private static final Logger logger = LoggerFactory.getLogger(DataAdapterService.class);

    private static final String BANK_ACCOUNT_CHOICE_ID = "operation.bankAccountChoice";

    private final DataAdapterI18NService dataAdapterI18NService;
    private final OperationValueExtractionService operationValueExtractionService;
    private final SmsPersistenceService smsPersistenceService;

    @Autowired
    public DataAdapterService(DataAdapterI18NService dataAdapterI18NService, OperationValueExtractionService operationValueExtractionService, SmsPersistenceService smsPersistenceService) {
        this.dataAdapterI18NService = dataAdapterI18NService;
        this.operationValueExtractionService = operationValueExtractionService;
        this.smsPersistenceService = smsPersistenceService;
    }

    @Override
    public UserDetailResponse lookupUser(String username, String organizationId, OperationContext operationContext) throws DataAdapterRemoteException, UserNotFoundException {
        // The sample Data Adapter code uses 1:1 mapping of username to userId. In real implementation the userId usually differs from the username, so translation of username to user ID is required.
        // If user does not exist or user account is blocked and such error needs to be silent, return null values for user ID and organization ID.
        // The SCA login fakes SMS message delivery even for case when user ID is null to disallow fishing of usernames.
        // For case when an error should appear instead, throw a UserNotFoundException.
        return fetchUserDetail(username, organizationId, operationContext);
    }

    @Override
    public UserAuthenticationResponse authenticateUser(String userId, String password, AuthenticationContext authenticationContext, String organizationId, OperationContext operationContext) throws DataAdapterRemoteException {
        // Here will be the real authentication - call to the backend providing authentication.
        // Return a response with UserAuthenticationResult based on the actual authentication result.
        // The password is optionally encrypted, the authentication context contains information about encryption.
        // In case of combined user authentication with SMS authorization the authentication context contains information
        // about result of SMS authorization.
        PasswordProtectionType passwordProtection = authenticationContext.getPasswordProtection();
        UserAuthenticationResponse authResponse = new UserAuthenticationResponse();
        if (passwordProtection == PasswordProtectionType.NO_PROTECTION && "test".equals(password)) {
            try {
                UserDetailResponse userDetail = fetchUserDetail(userId, organizationId, operationContext);
                // The organization needs to be set in response (e.g. client authenticated against RETAIL organization or SME organization).
                userDetail.setOrganizationId(organizationId);
                authResponse.setUserDetail(userDetail);
                authResponse.setAuthenticationResult(UserAuthenticationResult.VERIFIED_SUCCEEDED);
                return authResponse;
            } catch (UserNotFoundException e) {
                authResponse.setAuthenticationResult(UserAuthenticationResult.VERIFIED_FAILED);
                authResponse.setErrorMessage("login.authenticationFailed");
                return authResponse;
            }
        }
        authResponse.setAuthenticationResult(UserAuthenticationResult.VERIFIED_FAILED);
        authResponse.setErrorMessage("login.authenticationFailed");
        // Set number of remaining attempts for this user ID in case it is available.
        // authResponse.setRemainingAttempts(5);

        // To enable showing of remaining attempts for operation, use:
        // authResponse.setShowRemainingAttempts(true);

        // Use the following code to let the user know that the account has been blocked temporarily.
        // authResponse.setErrorMessage("login.authenticationBlocked");
        // authResponse.setRemainingAttempts(0);

        return authResponse;
    }

    @Override
    public UserDetailResponse fetchUserDetail(String userId, String organizationId, OperationContext operationContext) throws DataAdapterRemoteException, UserNotFoundException {
        // Fetch user details here ...
        // In case that user is not found, throw a UserNotFoundException.
        // The operation context may be null in case the method is called outside of an active operation (e.g. OAuth user profile request).
        UserDetailResponse responseObject = new UserDetailResponse();
        responseObject.setId(userId);
        responseObject.setGivenName("John");
        responseObject.setFamilyName("Doe");
        responseObject.setOrganizationId(organizationId);
        return responseObject;
    }

    @Override
    public DecorateOperationFormDataResponse decorateFormData(String userId, String organizationId, OperationContext operationContext) throws DataAdapterRemoteException, UserNotFoundException {
        String operationName = operationContext.getName();
        FormData formData = operationContext.getFormData();
        // Fetch bank account list for given user here from the bank backend.
        // In case that user is not found, throw a UserNotFoundException.
        // Replace mock bank account data with real data loaded from the bank backend.
        // In case the bank account selection is disabled, return an empty list.

        if ((!"authorize_payment".equals(operationName) && !"authorize_payment_sca".equals(operationName))) {
            // return empty list for operations other than authorize_payment and authorize_payment_sca
            return new DecorateOperationFormDataResponse(formData);
        }

        List<BankAccount> bankAccounts = new ArrayList<>();

        BankAccount bankAccount1 = new BankAccount();
        bankAccount1.setName("Běžný účet v CZK");
        bankAccount1.setBalance(new BigDecimal("24394.52"));
        bankAccount1.setNumber("12345678/1234");
        bankAccount1.setAccountId("CZ4012340000000012345678");
        bankAccount1.setCurrency("CZK");
        bankAccounts.add(bankAccount1);

        BankAccount bankAccount2 = new BankAccount();
        bankAccount2.setName("Spořící účet v CZK");
        bankAccount2.setBalance(new BigDecimal("158121.10"));
        bankAccount2.setNumber("87654321/4321");
        bankAccount2.setAccountId("CZ4043210000000087654321");
        bankAccount2.setCurrency("CZK");
        bankAccounts.add(bankAccount2);

        BankAccount bankAccount3 = new BankAccount();
        bankAccount3.setName("Spořící účet v EUR");
        bankAccount3.setBalance(new BigDecimal("1.90"));
        bankAccount3.setNumber("44444444/1111");
        bankAccount3.setAccountId("CZ4011110000000044444444");
        bankAccount3.setCurrency("EUR");
        bankAccount3.setUsableForPayment(false);
        bankAccount3.setUnusableForPaymentReason(dataAdapterI18NService.messageSource().getMessage("operationReview.balanceTooLow", null, LocaleContextHolder.getLocale()));
        bankAccounts.add(bankAccount3);

        boolean choiceEnabled = true;
        String defaultValue = "CZ4012340000000012345678";

        List<FormFieldConfig> configs = formData.getConfig();
        for (FormFieldConfig config: configs) {
            if (BANK_ACCOUNT_CHOICE_ID.equals(config.getId())) {
                choiceEnabled = config.isEnabled();
                // You should check the default value against list of available accounts.
                defaultValue = config.getDefaultValue();
            }
        }
        formData.addBankAccountChoice(BANK_ACCOUNT_CHOICE_ID, bankAccounts, choiceEnabled, defaultValue);

        // Sample warning banner displayed above the bank account choice field.
        // Attribute attr = formData.addBankAccountChoice(BANK_ACCOUNT_CHOICE_ID, bankAccounts, choiceEnabled, defaultValue);
        // formData.addBannerBeforeField(BannerType.BANNER_WARNING, "banner.invalidAccount", attr);

        return new DecorateOperationFormDataResponse(formData);
    }

    @Override
    public void formDataChangedNotification(String userId, String organizationId, FormDataChange change, OperationContext operationContext) throws DataAdapterRemoteException {
        String operationId = operationContext.getId();
        if (change instanceof BankAccountChoice) {
            // Handle bank account choice here (e.g. send notification to bank backend).
            BankAccountChoice bankAccountChoice = (BankAccountChoice) change;
            logger.info("Bank account chosen: {}, operation ID: {}", bankAccountChoice.getBankAccountId(), operationId);
            return;
        } else if (change instanceof AuthMethodChoice) {
            // Handle authorization method choice here (e.g. send notification to bank backend).
            AuthMethodChoice authMethodChoice = (AuthMethodChoice) change;
            logger.info("Authorization method chosen: {}, operation ID: {}", authMethodChoice.getChosenAuthMethod().toString(), operationId);
            return;
        }
        throw new IllegalStateException("Invalid change entity type: " + change.getType());
    }

    @Override
    public void operationChangedNotification(String userId, String organizationId, OperationChange change, OperationContext operationContext) throws DataAdapterRemoteException {
        String operationId = operationContext.getId();
        // Handle operation change here (e.g. send notification to bank backend).
        logger.info("Operation changed, status: {}, operation ID: {}", change.toString(), operationId);
    }

    @Override
    public String createAuthorizationSms(String userId, String organizationId, OperationContext operationContext, String lang) throws InvalidOperationContextException, DataAdapterRemoteException {
        // messageId is generated as random UUID, it can be overridden to provide a real message identification
        String messageId = UUID.randomUUID().toString();

        // fake SMS message delivery for null user ID
        if (userId == null) {
            return messageId;
        }

        // generate authorization code
        AuthorizationCode authorizationCode = generateAuthorizationCode(userId, organizationId, operationContext);

        // generate message text, include previously generated authorization code
        String messageText = generateSmsText(userId, organizationId, operationContext, authorizationCode, lang);

        // persist authorization SMS message
        smsPersistenceService.createAuthorizationSms(userId, organizationId, messageId, operationContext, authorizationCode, messageText);

        // Send SMS with generated text to target user.
        sendAuthorizationSms(userId, organizationId, messageId, messageText, operationContext);

        // return generated message ID
        return messageId;
    }

    @Override
    public AuthorizationCode generateAuthorizationCode(String userId, String organizationId, OperationContext operationContext) throws InvalidOperationContextException {
        String operationName = operationContext.getName();
        List<String> digestItems = new ArrayList<>();
        switch (operationName) {
            case "login":
            case "login_sca": {
                digestItems.add(operationName);
                break;
            }
            case "authorize_payment":
            case "authorize_payment_sca": {
                AmountAttribute amountAttribute = operationValueExtractionService.getAmount(operationContext);
                String account = operationValueExtractionService.getAccount(operationContext);
                BigDecimal amount = amountAttribute.getAmount();
                String currency = amountAttribute.getCurrency();
                digestItems.add(amount.toPlainString());
                digestItems.add(currency);
                digestItems.add(account);
                break;
            }
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

    @Override
    public String generateSmsText(String userId, String organizationId, OperationContext operationContext, AuthorizationCode authorizationCode, String lang) throws InvalidOperationContextException {
        String operationName = operationContext.getName();
        String[] messageArgs;
        switch (operationName) {
            case "login":
            case "login_sca": {
                messageArgs = new String[]{authorizationCode.getCode()};
                break;
            }
            case "authorize_payment":
            case "authorize_payment_sca": {
                AmountAttribute amountAttribute = operationValueExtractionService.getAmount(operationContext);
                String account = operationValueExtractionService.getAccount(operationContext);
                BigDecimal amount = amountAttribute.getAmount();
                String currency = amountAttribute.getCurrency();
                messageArgs = new String[]{amount.toPlainString(), currency, account, authorizationCode.getCode()};
                break;
            }
            // Add new operations here.
            default:
                throw new InvalidOperationContextException("Unsupported operation: " + operationName);
        }

        return dataAdapterI18NService.messageSource().getMessage(operationName + ".smsText", messageArgs, new Locale(lang));
    }

    @Override
    public void sendAuthorizationSms(String userId, String organizationId, String messageId, String messageText, OperationContext operationContext) throws DataAdapterRemoteException {
        // Add here code to send the SMS OTP message to user identified by userId with messageText.
        // The message entity can be extracted using message ID from table da_sms_authorization.
        // In case message delivery fails, throw a DataAdapterRemoteException.
    }

    @Override
    public VerifySmsAuthorizationResponse verifyAuthorizationSms(String userId, String organizationId, String messageId, String authorizationCode, OperationContext operationContext) throws DataAdapterRemoteException, InvalidOperationContextException {
        // You can override this logic in case more complex handling of SMS verification is required.
        VerifySmsAuthorizationResponse response = smsPersistenceService.verifyAuthorizationSms(messageId, authorizationCode, false);
        // Set number of remaining attempts for verification in case it is available.
        // authResponse.setRemainingAttempts(5);
        // You can enable showing of remaining attempts for the operation.
        // response.setShowRemainingAttempts(true);
        return response;
    }

    @Override
    public VerifySmsAndPasswordResponse verifyAuthorizationSmsAndPassword(String userId, String organizationId, String messageId, String authorizationCode, OperationContext operationContext, AuthenticationContext authenticationContext, String password) throws DataAdapterRemoteException, InvalidOperationContextException {
        VerifySmsAndPasswordResponse response = new VerifySmsAndPasswordResponse();

        // Verify authorization code from SMS
        VerifySmsAuthorizationResponse smsResponse = smsPersistenceService.verifyAuthorizationSms(messageId, authorizationCode, true);
        authenticationContext.setSmsAuthorizationResult(smsResponse.getSmsAuthorizationResult());

        // Authenticate user
        UserAuthenticationResponse authResponse = authenticateUser(userId, password, authenticationContext, organizationId, operationContext);

        // Create aggregate response
        response.setSmsAuthorizationResult(smsResponse.getSmsAuthorizationResult());
        response.setUserAuthenticationResult(authResponse.getAuthenticationResult());
        if (smsResponse.getSmsAuthorizationResult() != SmsAuthorizationResult.VERIFIED_SUCCEEDED
                || authResponse.getAuthenticationResult() != UserAuthenticationResult.VERIFIED_SUCCEEDED) {
            // Provide an error message which does not allow to find out reason of failed verification.
            response.setErrorMessage("login.authenticationFailed");
        }
        // Optionally set the number of remaining attempts, e.g. using lower of the two remaining attempt counts.
        // response.setRemainingAttempts(Math.min(smsResponse.getRemainingAttempts(), authResponse.getRemainingAttempts()));
        // You can enable showing of remaining attempts for the operation.
        // response.setShowRemainingAttempts(true);
        return response;
    }

    @Override
    public InitConsentFormResponse initConsentForm(String userId, String organizationId, OperationContext operationContext) throws DataAdapterRemoteException, InvalidOperationContextException {
        // Override this logic in case consent form should be displayed conditionally for given operation context.
        return new InitConsentFormResponse(true);
    }

    @Override
    public CreateConsentFormResponse createConsentForm(String userId, String organizationId, OperationContext operationContext, String lang) throws DataAdapterRemoteException, InvalidOperationContextException {
        // Generate response with consent text and options based on requested language.
        if ("login".equals(operationContext.getName()) || "login_sca".equals(operationContext.getName())) {
            // Create default consent
            CreateConsentFormResponse response = new CreateConsentFormResponse();
            if ("cs".equals(lang)) {
                response.setConsentHtml("Tímto potvrzuji, že jsem inicioval tuto žádost o přihlášení a souhlasím s dokončením této operace.");
            } else {
                response.setConsentHtml("I consent that I have initiated this authentication request and give consent to complete the operation.<br/><br/>");
            }

            ConsentOption option1 = new ConsentOption();
            option1.setId("CONSENT_LOGIN");
            option1.setRequired(true);
            if ("cs".equals(lang)) {
                option1.setDescriptionHtml("Souhlasím s dokončením operace pro přihlášení.");
            } else {
                option1.setDescriptionHtml("I give consent to complete the authentication operation.");
            }

            response.getOptions().add(option1);
            return response;
        }
        if ("authorize_payment".equals(operationContext.getName()) || "authorize_payment_sca".equals(operationContext.getName())) {
            CreateConsentFormResponse response = new CreateConsentFormResponse();
            if ("cs".equals(lang)) {
                response.setConsentHtml("Tímto potvrzuji, že jsem inicioval tuto platební operaci a souhlasím s jejím dokončením.");
            } else {
                response.setConsentHtml("I consent that I have initiated this payment request and give consent to complete the operation.");
            }

            ConsentOption option1 = new ConsentOption();
            option1.setId("CONSENT_INIT");
            option1.setRequired(true);
            if ("cs".equals(lang)) {
                option1.setDescriptionHtml("Potvrzuji, že jsem inicioval tuto platební operaci.");
            } else {
                option1.setDescriptionHtml("I consent that I have initiated this payment operation.");
            }

            ConsentOption option2 = new ConsentOption();
            option2.setId("CONSENT_PAYMENT");
            option2.setRequired(true);
            if ("cs".equals(lang)) {
                option2.setDescriptionHtml("Souhlasím s provedením platební operace.");
            } else {
                option2.setDescriptionHtml("I give consent to complete this payment operation.");
            }

            response.getOptions().add(option1);
            response.getOptions().add(option2);
            return response;
        }
        throw new InvalidOperationContextException("Invalid operation context");
    }

    @Override
    public ValidateConsentFormResponse validateConsentForm(String userId, String organizationId, OperationContext operationContext, String lang, List<ConsentOption> options) throws DataAdapterRemoteException, InvalidOperationContextException, InvalidConsentDataException {
        // Validate consent form options and return response with result of validation and optional error messages.
        ValidateConsentFormResponse response = new ValidateConsentFormResponse();
        if (options == null || options.isEmpty()) {
            throw new InvalidConsentDataException("Missing options for consent");
        }
        if ("login".equals(operationContext.getName()) || "login_sca".equals(operationContext.getName())) {
            if (options.size() != 1) {
                throw new InvalidConsentDataException("Unexpected options count for consent");
            }
            // Validate default consent
            if (options.get(0).getValue() == ConsentOptionValue.CHECKED) {
                response.setConsentValidationPassed(true);
                return response;
            }
            response.setConsentValidationPassed(false);
            if ("cs".equals(lang)) {
                response.setValidationErrorMessage("Prosím vyplňte celý formulář se souhlasem.");
                if (options.get(0).getValue() != ConsentOptionValue.CHECKED) {
                    ConsentOptionValidationResult result = new ConsentOptionValidationResult();
                    result.setId("CONSENT_LOGIN");
                    result.setValidationPassed(false);
                    result.setErrorMessage("Pro dokončení operace odsouhlaste tuto volbu.");
                    response.getOptionValidationResults().add(result);
                }
            } else {
                response.setValidationErrorMessage("Please fill in the whole consent form.");
                if (options.get(0).getValue() != ConsentOptionValue.CHECKED) {
                    ConsentOptionValidationResult result = new ConsentOptionValidationResult();
                    result.setId("CONSENT_LOGIN");
                    result.setValidationPassed(false);
                    result.setErrorMessage("Confirm this option to complete the operation.");
                    response.getOptionValidationResults().add(result);
                }
            }
            return response;
        }
        if ("authorize_payment".equals(operationContext.getName()) || "authorize_payment_sca".equals(operationContext.getName())) {
            if (options.size() != 2) {
                throw new InvalidConsentDataException("Unexpected options count for consent");
            }
            if (options.get(0).getValue() == ConsentOptionValue.CHECKED && options.get(1).getValue() == ConsentOptionValue.CHECKED) {
                response.setConsentValidationPassed(true);
                return response;
            }
            response.setConsentValidationPassed(false);
            if ("cs".equals(lang)) {
                response.setValidationErrorMessage("Prosím vyplňte celý formulář se souhlasem.");
                if (options.get(0).getValue() != ConsentOptionValue.CHECKED) {
                    ConsentOptionValidationResult result = new ConsentOptionValidationResult();
                    result.setId("CONSENT_INIT");
                    result.setValidationPassed(false);
                    result.setErrorMessage("Pro dokončení operace odsouhlaste tuto volbu.");
                    response.getOptionValidationResults().add(result);
                }
                if (options.get(1).getValue() != ConsentOptionValue.CHECKED) {
                    ConsentOptionValidationResult result = new ConsentOptionValidationResult();
                    result.setId("CONSENT_PAYMENT");
                    result.setValidationPassed(false);
                    result.setErrorMessage("Pro dokončení operace odsouhlaste tuto volbu.");
                    response.getOptionValidationResults().add(result);
                }
            } else {
                response.setValidationErrorMessage("Please fill in the whole consent form.");
                if (options.get(0).getValue() != ConsentOptionValue.CHECKED) {
                    ConsentOptionValidationResult result = new ConsentOptionValidationResult();
                    result.setId("CONSENT_INIT");
                    result.setValidationPassed(false);
                    result.setErrorMessage("Confirm this option to complete the operation.");
                    response.getOptionValidationResults().add(result);
                }
                if (options.get(1).getValue() != ConsentOptionValue.CHECKED) {
                    ConsentOptionValidationResult result = new ConsentOptionValidationResult();
                    result.setId("CONSENT_PAYMENT");
                    result.setValidationPassed(false);
                    result.setErrorMessage("Confirm this option to complete the operation.");
                    response.getOptionValidationResults().add(result);
                }
            }
            return response;
        }
        throw new InvalidOperationContextException("Invalid operation context");
    }

    @Override
    public SaveConsentFormResponse saveConsentForm(String userId, String organizationId, OperationContext operationContext, List<ConsentOption> options) throws DataAdapterRemoteException, InvalidOperationContextException, InvalidConsentDataException {
        // Save consent form options selected by the user. The sample implementation only logs the selected options.
        logger.info("Saving consent form for user: {}, operation ID: {}", userId, operationContext.getId());
        for (ConsentOption option: options) {
            logger.info("Option {}: {}", option.getId(), option.getValue());
        }
        return new SaveConsentFormResponse(true);
    }

}
