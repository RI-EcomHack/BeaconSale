# Commercetools Platform Example for Android

This Hello World project shows how to:
- [x] Connect to the [SPHERE.IO](http://dev.sphere.io/) API with OAuth2
- [x] Search and sort products
- [x] Create a cart
- [x] Put a product into a cart

## Getting started
* `git clone` this repo
```bash
git clone git@github.com:sphereio/commercetools-android-example.git
```
* If you don't have a SPHERE.IO project yet, register at the [Merchant Center](https://admin.sphere.io/) and setup a project. If you add sample data, the app will work with that out of the box!
* In the [Merchant Center](https://admin.sphere.io/), go to the `Developers` section and select `API Clients`
* Copy `credentials.xml.template` to `credentials.xml`
```bash
cp commercetools-android-example/app/src/main/res/values/credentials.xml.template 
commercetools-android-example/app/src/main/res/values/credentials.xml
```
* Uncomment the keys and enter your credentials
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="project">your-project-name</string>
    <string name="clientId">your-client-id</string>
    <string name="clientSecret">your-client-secret</string>
</resources>
```
* Now you can open the project in Android Studio and play around with it ;)
