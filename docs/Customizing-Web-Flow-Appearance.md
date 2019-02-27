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
