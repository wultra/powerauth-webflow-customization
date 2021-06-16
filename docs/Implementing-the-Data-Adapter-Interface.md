# Implementing the Data Adapter Interface

Data Adapter is used for connecting Web Flow to client backend systems. It allows to interact with backends for user authentication, SMS authorization, read additional data required for the operation as well as notify client backend about operation changes.
Furthermore, the Data Adapter can be used to customize text and options for the OAuth 2.0 consent screen.

## DataAdapter Interface

The interface methods are defined in the [DataAdapter interface](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java):

- [lookupUser](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L45) - lookup user account based on username
- [authenticateUser](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L57) - perform user authentication with remote backend based on provided credentials
- [fetchUserDetail](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L68) - retrieve user details for given user ID
- [iniAuthMethod](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L80) - initialize an authentication method and set its parameters, e.g. client certificate configuration
- [decorateFormData](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L92) - retrieve operation form data and decorate it
- [formDataChangedNotification](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L102) - method is called when operation form data changes to allow notification of client backends
- [createImplicitLoginOperation](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#113) - create an implicit login operation automatically on authentication start
- [getPAOperationMapping](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#125) - map a complex operation into smaller operations and configure PowerAuth operation template
- [operationChangedNotification](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L135) - method is called when operation status changes to allow notification of client backends
- [createAndSendAuthorizationSms](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L150) - create an OTP code and send an authorization SMS message
- [sendAuthorizationSms](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L168) - send an authorization SMS message with previously created OTP code
- [verifyAuthorizationSms](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L182) - verify OTP authorization code from SMS message
- [verifyAuthorizationSmsAndPassword](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L198) - verify OTP authorization code from SMS message together with verifying user password
- [verifyClientCertificate](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L212) - verify a client TLS certificate
- [initConsentForm](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L223) - initialize the OAuth 2.0 consent form and decide whether consent form should be displayed
- [createConsentForm](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L235) - create the OAuth 2.0 consent form text and options
- [validateConsentForm](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L249) - validate the OAuth 2.0 consent form options
- [saveConsentForm](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L262) - save the OAuth 2.0 consent form options selected by the user
- [executeAfsAction](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L262) - execute an anti-fraud action triggered by Web Flow
  
## Customizing Data Adapter

Following steps are required for customization of Data Adapter.

### 1. Implement Interface Methods

Consider which of the following methods need to be implemented in your project:

- `lookupUser` - (required) - provides mapping of username to user ID which is used by other methods
- `authenticateUser` (optional) - implementation is required in case any Web Flow operation needs to authenticate the user using a username/password
- `fetchUserDetail` (required) - provides information about the user (user ID and name) for the OAuth 2.0 protocol
- `iniAuthMethod` (optional) - used for enabling client TLS certificate authentication
- `decorateFormData` (optional) - implementation is required in case any Web Flow operation form data needs to be updated after authentication (e.g. add information about user bank accounts)
- `formDataChangedNotification` (optional) - implementation is required in case the client backends need to be notified about user input during an operation
- `createImplicitLoginOperation` (optional) - implementation is required in case login operation can be started without a previously created operation for login
- `getPAOperationMapping` (optional) - implementation is required in case there exist complex operations which consist of multiple smaller operations or in case PowerAuth operations support is enabled  
- `operationChangedNotification` (optional) - implementation is required in case the client backends need to be notified about operation status changes
- `createAndSendAuthorizationSms` (optional) - implementation is required in case any Web Flow operation needs to authorize the user using SMS authorization and the OTP code is generated in Data Adapter
- `sendAuthorizationSms` (optional) - implementation is required in case any Web Flow operation needs to authorize the user using SMS authorization and the OTP code is generated in Next Step  
- `verifyAuthorizationSms` (optional) - implementation is required in case any Web Flow operation needs to authorize the user using SMS authorization and the OTP code is verified in Data Adapter
- `verifyAuthorizationSmsAndPassword` (optional) - implementation is required in case any Web Flow operation needs to authorize the user using SMS authorization and password and authentication is done using Data Adapter
- `verifyClientCertificate` (optional) - implementaiton is requred in case any Web Flow operation needs to authorize the user using TLS certificate  
- `initConsentForm` (optional) - implementation is required in case the OAuth 2.0 consent step is enabled
- `createConsentForm` (optional) - implementation is required in case the OAuth 2.0 consent step is enabled
- `validateConsentForm` (optional) - implementation is required in case the OAuth 2.0 consent step is enabled
- `saveConsentForm` (optional) - implementation is required in case the OAuth 2.0 consent step is enabled
- `executeAfsAction` (optional) - implementation is required in case anti-fraud integration is enabled in Web Flow

### 2. Implement the `DataAdapter` Interface

Implement the actual changes in Data Adapter so that it connects to an actual data source.

  - Clone project [powerauth-webflow-customization](https://github.com/wultra/powerauth-webflow-customization#docucheck-keep-link) from GitHub.
  - Update the `pom.xml` to add any required additional dependencies.
  - Create a proprietary client (+ client config) for your web services.
  - Implement the Data Adapter interface by providing your own implementation in the [DataAdapterService class](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/impl/service/DataAdapterService.java). You can override the sample implementation.
