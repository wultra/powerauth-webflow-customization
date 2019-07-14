# Customizing PowerAuth Web Flow Appearance

Web Flow resources which can be customized are available in the ext-resources folder:

- [ext-resources](../ext-resources)

## Overriding Default Web Flow Resource Location

The general process of updating Web Flow resources:

- Clone project [powerauth-webflow-customization](https://github.com/wultra/powerauth-webflow-customization#docucheck-keep-link) from GitHub.
- Update Web Flow resources by overriding existing texts, CSS, fonts and images or by adding additional resources.
- When deploying Web Flow, configure the following Spring Boot property:

```properties
powerauth.webflow.page.ext-resources.location=classpath:/static/resources/
```

For example, if you placed the Web Flow customization files to `/opt/webflow/ext-resources`, set the property to:

```properties
powerauth.webflow.page.ext-resources.location=file:/opt/webflow/ext-resources
```

See the documentation of your container for configuration of properties.

## Customizing Web Flow Texts

Web Flow texts are stored in `ext-resources/message_[lang].properties` files, see:

- [ext-resources/messages_en.properties](../ext-resources/messages_en.properties)

- [ext-resources/messages_cs.properties](../ext-resources/messages_cs.properties)

After you make a copy of the `powerauth-webflow-customization` project, you can update the texts and deploy changes to the folder `/path/to/your/ext-resources`.

## Customizing Web Flow CSS Styles

Web Flow CSS files are stored in `ext-resources/css` folder, see:

- [ext-resources/css](../ext-resources/css)

After you make a copy of the `powerauth-webflow-customization` project, you can update the CSS and deploy changes to the folder `/path/to/your/ext-resources/css`. Make sure to only edit the `customization.css` file. We may change CSS in `base.css` file at any time and you would have to migrate the changes we made to your customization.

## Customizing Web Flow Images

Web Flow images are stored in `ext-resources/images` folder, see:

- [ext-resources/images](../ext-resources/images)

After you make a copy of the `powerauth-webflow-customization` project, you can update the images and deploy changes to the folder `/path/to/your/ext-resources/css`.

You can also add new images and configure these images in overridden CSS files.

## Customizing Web Flow Fonts

Additional fonts for Web Flow can be stored in `ext-resources/fonts` folder, see:
- [ext-resources/fonts](../ext-resources/fonts)

After you make a copy of the `powerauth-webflow-customization` project, you can add new fonts to the folder `/path/to/your/ext-resources/fonts` and update the `customization.css` file (see above) to use the added fonts in Web Flow.

## Customizing the OAuth 2.0 Consent Form

The OAuth 2.0 consent form used by Web Flow can be customized by implementing following methods from Data Adapter interface:

### Initialize Consent Form

The [initConsentForm](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L177) method is used to
allow to decide whether consent form for should be displayed for given operation context. Based on values of parameters `userId`, `organizationId`
and `operationContext` a decision can be made whether to display the consent form or not. In case the consent form is always displayed,
return true in response unconditionally.

### Create Consent Form 
The [createConsentForm](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L189) method is used to specify
the text of consent form and define options which are available in the options form. The consent form accepts consent text as HTML, scripting of the HTML is not allowed.
The language of the consent form is specified using parameter `lang`. Each option is identified using an identifier `id`. Individual options in the form can be set as required and their default value can be set.
The form can use parameters `userId`, `organizationId` and `operationContext` including `name`, `formData` and `applicationContext` to create a customized and personalized consent form for given
user, operation name, operation parameters and application which initiated the operation. 

The response should contain following data:
- `consentHtml` - localized HTML text of OAuth 2.0 consent for given operation and its context
- `options` - list of consent options which should be checked by the user with following parameters:
  - `id` - identifier of the consent option
  - `descriptionHtml` - localized HTML text for the description of the consent option
  - `required` - whether the option must be checked in order to complete the operation
  - `defaultValue` - default value of the option
  - `value` - value specified by the user (not used yet)
  
_Note that the consent texts do not use automatic resource localization because the HTML texts are expected to be complex and dynamically generated._

### Validate Consent Form  
The [validateConsentForm](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L203) method is used to validate the OAuth 2.0 consent form options
before the response is persisted. The identifiers of consent options match identifiers created in the `createConsentForm` step. The error messages produced by this method should
take into account language specified using parameter `lang`.

The response should contain following data:
- `consentValidationPassed` - whether the consent validation passed and the operation can be completed
- `validationErrorMessage` - localized HTML text of error message for overall consent form validation used in case the consent validation failed
- `optionValidationResults` - result of validation for individual consent options:
  - `id` - identifier of the consent option
  - `validationPassed` - whether validation of the consent option passed
  - `errorMessage` - localized HTML text of error message for consent option, in case validation of consent option value failed
  
_Note that the texts of error messages do not use automatic resource localization because the HTML texts are expected to be complex and dynamically generated._
  
### Save Consent Form
The [saveConsentForm](../powerauth-data-adapter/src/main/java/io/getlime/security/powerauth/app/dataadapter/api/DataAdapter.java#L215) method is used to save the OAuth 2.0 consent form options.
This method is called only when form validation done in `validateConsentForm` method successfully passes. The sample implementation prints the consent form option values into log.
It is expected that in the real implementation the consent option values are persisted in a database or any other persistent storage of consent options.
 