# Implementing the Data Adapter Interface

Data Adapter is used for connecting Web Flow to client backend systems. It allows to interact with backends for user authentication, SMS authorization, read additional data required for the operation as well as notify client backend about operation changes.
Furthermore, the Data Adapter can be used to customize text and options for the OAuth 2.0 consent screen.

## DataAdapter Interface

The interface methods are defined in the [DataAdapter interface](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java):

- [lookupUser](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L39) - lookup user account based on username
- [authenticateUser](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L51) - perform user authentication with remote backend based on provided credentials
- [fetchUserDetail](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L62) - retrieve user details for given user ID
- [decorateFormData](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L73) - retrieve operation form data and decorate it
- [formDataChangedNotification](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L83) - method is called when operation form data changes to allow notification of client backends
- [operationChangedNotification](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L93) - method is called when operation status changes to allow notification of client backends
- [createAuthorizationSms](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L105) - create and send authorization SMS message
- [generateAuthorizationCode](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L115) - generate authorization code for authorization SMS message
- [generateSmsText](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L127) - generate SMS text for authorization SMS message
- [sendAuthorizationSms](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L138) - send authorization SMS message
- [verifyAuthorizationSms](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L151) - verify authorization code from SMS message
- [verifyAuthorizationSmsAndPassword](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L166) - verify authorization code from SMS message together with verifying user password
- [initConsentForm](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L177) - initialize the OAuth 2.0 consent form and decide whether consent form should be displayed
- [createConsentForm](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L189) - create the OAuth 2.0 consent form text and options
- [validateConsentForm](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L203) - validate the OAuth 2.0 consent form options
- [saveConsentForm](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L215) - save the OAuth 2.0 consent form options selected by the user

## Customizing Data Adapter

Following steps are required for customization of Data Adapter.

### 1. Implement Interface Methods

Consider which of the following methods need to be implemented in your project:

- [lookupUser](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L39) - (required) - provides mapping of username to user ID which is used by other methods
- [authenticateUser](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L51) (optional) - implementation is required in case any Web Flow operation needs to authenticate the user using a username/password
- [fetchUserDetail](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L62) (required) - provides information about the user (user ID and name) for the OAuth 2.0 protocol
- [decorateFormData](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L73) (optional) - implementation is required in case any Web Flow operation form data needs to be updated after authentication (e.g. add information about user bank accounts)
- [formDataChangedNotification](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L83) (optional) - implementation is required in case the client backends need to be notified about user input during an operation
- [operationChangedNotification](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L93) (optional) - implementation is required in case the client backends need to be notified about operation status changes
- [createAuthorizationSms](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L105) (optional) - implementation is required in case any Web Flow operation needs to authorize the user using SMS authorization 
- [generateAuthorizationCode](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L115) (optional) - implementation is required in case any Web Flow operation needs to authorize the user using SMS authorization
- [generateSmsText](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L127) (optional) - implementation is required in case any Web Flow operation needs to authorize the user using SMS authorization
- [sendAuthorizationSms](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L138) (optional) - implementation is required in case any Web Flow operation needs to authorize the user using SMS authorization
- [verifyAuthorizationSms](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L151) (optional) - implementation is required in case any Web Flow operation needs to authorize the user using SMS authorization
- [verifyAuthorizationSmsAndPassword](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L166) (optional) - implementation is required in case any Web Flow operation needs to authorize the user using SMS authorization and password
- [initConsentForm](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L177) (optional) - implementation is required in case the OAuth 2.0 consent step is enabled
- [createConsentForm](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L139) (optional) - implementation is required in case the OAuth 2.0 consent step is enabled
- [validateConsentForm](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L153) (optional) - implementation is required in case the OAuth 2.0 consent step is enabled
- [saveConsentForm](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L165) (optional) - implementation is required in case the OAuth 2.0 consent step is enabled

### 2. Implement the `DataAdapter` Interface

Implement the actual changes in Data Adapter so that it connects to an actual data source.

  - Clone project [powerauth-webflow-customization](https://github.com/wultra/powerauth-webflow-customization#docucheck-keep-link) from GitHub.
  - Update the `pom.xml` to add any required additional dependencies.
  - Create a proprietary client (+ client config) for your web services.
  - Implement the Data Adapter interface by providing your own implementation in the [DataAdapterService class](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/impl/service/DataAdapterService.java). You can override the sample implementation.
