# commercetools-android-example

Clone the project.
```bash
git clone git@github.com:sphereio/commercetools-android-example.git
```

Copy `credentials.xml.template` to `credentials.xml`.
```bash
cp commercetools-android-example/app/src/main/res/values/credentials.xml.template 
commercetools-android-example/app/src/main/res/values/credentials.xml
```

Uncomment the keys and enter your credentials.
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="project">your-project-name</string>
    <string name="clientId">your-client-id</string>
    <string name="clientSecret">your-client-secret</string>
</resources>
```

You can find your cedentials on [admin.sphere.io](https://admin.sphere.io/)
