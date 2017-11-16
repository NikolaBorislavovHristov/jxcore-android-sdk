# jxcore-android-sdk

### [FAQ](http://www.goland.org/jxcore/)
#### Start Node.js server on Android in a background service.
#### This project use jxcore v3.1.14 compiled with SpiderMonkey. If you want to use V8 instead replace the content of the `jxcore-binaries` directory with the compiled binaries.
#### Compiled JXCore binaries used from [thaliproject/jxbuild](https://github.com/thaliproject/jxbuild/blob/master/distribute.md)
#### [Example project](https://github.com/NikolaBorislavovHristov/jxcore-service-example)

### How to:

1. Add the JitPack repository to your build file 
```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

2. Add the JXCore SDK dependency
```gradle
dependencies {
    implementation 'com.github.NikolaBorislavovHristov:jxcore-android-sdk:master-SNAPSHOT'
}
```

3. Declare the service in the manifest file

```xml
<service android:name="io.jxcore.node.JXCoreService"
    android:enabled="true"
    android:permission="android.permission.INTERNET"
    android:exported="true"
/>
```

4. Add `server.js` asset in the `assets/www/jxcore` directory

```javascript
var http = require('http');
var port = 8082;
var server = http.createServer(function(request, response) {
    response.end('Hello Node.js Server!');
});

server.listen(port, function(err) {
    if (err) {
        console.error(err);
        return;
    }
    
    console.log('server is listening on ' + port);
});
```

5. Start the jxcore service

```java
final Intent startJXCoreIntent = new Intent(getApplicationContext(), JXCoreService.class);
startService(startJXCoreIntent);
```
