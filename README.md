# android-IstatNetwork
And Android Library to make Http Query Easy.

```java
    //Create a callback for your Async Query: HttpQueryCallback.
    HttpAsyncQuery.HttpQueryCallback callback = new HttpAsyncQuery.HttpQueryCallback (){
    //Your own implementation
    };
    /*
        create your downloader. optional, (it exist default one).
        It allow to specify how the queryer would read and build body response.
        Default read and return plain text
     */
    HttpAsyncQuery.HttpDownloadHandler downloader = new HttpAsyncQuery.HttpDownloadHandler (){
    //Your own implementation
    };
    /*
        create an Http Query
        can also use MutipartHttpQuery, BodyPartHttpQuery or your own HttpQuery<?> child class instance
     */
    SimpleHttpQuery http = new SimpleHttpQuery();
    //add http parm to your http httpAsyncQuery
    http.addParam("parma1","value1");
    http.addParam("param2","value2");
    //build and call your Async Http Query. for example a GET
    AsyncHttp.from(http)
            .useEncoding("UTF-8")//optional default =UTF-8
            .useBufferSize(1024)//optional default=1024
            .useDownloader(downloader)//optional default download plain/text.
            .setQueryCallback(callback)//add a callback to call when request completed
            .doGet("http://www.google.com");
            //.doGet(callback, "http://www.google.com"); //make a GET Request with a specific callback
```

Usage
-----
Just add the dependency to your `build.gradle`:

```groovy
dependencies {
   compile 'istat.android.network.http:istat-network:2.6.0'
}
```

minSdkVersion = 10
------------------
Library is compatible with Android 2.3 and newer.

Download
--------
add the dependency to your pom.xml:

```xml
<dependency>
  <groupId>istat.android.network.http</groupId>
  <artifactId>istat-network</artifactId>
  <version>2.6.0</version>
  <type>pom</type>
</dependency>
```
