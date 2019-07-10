# Implementing the Data Adapter Interface

Data Adapter is used for connecting Web Flow to client backend systems. It allows to interact with backends for user authentication, SMS authorization, read additional data required for the operation as well as notify client backend about operation changes.
Furthermore, the Data Adapter can be used to customize text and options for the OAuth 2.0 consent screen.

## DataAdapter Interface

The interface methods are defined in the [DataAdapter interface](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java):

- [lookupUser](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L38) - lookup user account based on username
- [authenticateUser](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L52) - perform user authentication with remote backend based on provided credentials
- [fetchUserDetail](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L63) - retrieve user details for given user ID
- [decorateFormData](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L74) - retrieve operation form data and decorate it
- [formDataChangedNotification](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L84) - method is called when operation form data changes to allow notification of client backends
- [operationChangedNotification](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L94) - method is called when operation status changes to allow notification of client backends
- [generateAuthorizationCode](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L104) - generate authorization code for authorization SMS message
- [generateSmsText](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L116) - generate SMS text for authorization SMS message
- [sendAuthorizationSms](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L127) - send authorization SMS message
- [createConsentForm](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L139) - create an OAuth 2.0 consent form
- [validateConsentForm](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L153) - validate the OAuth 2.0 consent form options
- [saveConsentForm](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L165) - save the OAuth 2.0 consent form options

## Customizing Data Adapter

Following steps are required for customization of Data Adapter.

### 1. Implement Interface Methods

Consider which of the following methods need to be implemented in your project:

- [lookupUser](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L38) - (required) - provides mapping of username to user ID which is used by other methods
- [authenticateUser](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L52) (optional) - implementation is required in case any Web Flow operation needs to authenticate the user using a username/password login form
- [fetchUserDetail](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L63) (required) - provides information about the user (user ID and name) for the OAuth 2.0 protocol
- [decorateFormData](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L74) (optional) - implementation is required in case any Web Flow operation form data needs to be updated after authentication (e.g. add information about user bank accounts)
- [formDataChangedNotification](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L84) (optional) - implementation is required in case the client backends need to be notified about user input during an operation
- [operationChangedNotification](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L94) (optional) - implementation is required in case the client backends need to be notified about operation status changes
- [generateAuthorizationCode](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L104) (optional) - implementation is required in case any Web Flow operation needs to authorize the user using SMS authorization
- [generateSmsText](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L116) (optional) - implementation is required in case any Web Flow operation needs to authorize the user using SMS authorization
- [sendAuthorizationSms](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L127) (optional) - implementation is required in case any Web Flow operation needs to authorize the user using SMS authorization
- [createConsentForm](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L139) (optional) - implementation is required in case the OAuth 2.0 consent step is enabled
- [validateConsentForm](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L153) (optional) - implementation is required in case the OAuth 2.0 consent step is enabled
- [saveConsentForm](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L165) (optional) - implementation is required in case the OAuth 2.0 consent step is enabled

### 2. Implement the `DataAdapter` Interface

Implement the actual changes in Data Adapter so that it connects to an actual data source.

  - Clone project [powerauth-webflow-customization](https://github.com/wultra/powerauth-webflow-customization#docucheck-keep-link) from GitHub.
  - Update the `pom.xml` to add any required additional dependencies.
  - Create a proprietary client (+ client config) for your web services.
  - Implement the Data Adapter interface by providing your own implementation in the [DataAdapterService class](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/impl/service/DataAdapterService.java). You can override the sample implementation.
