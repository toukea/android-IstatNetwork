# android-IstatNetwork
And Android Library to make Http Query Easy.

```java
       //Create an callback for your Async Query: HttpQueryCallback or use HttpCallback class.
       HttpAsyncQuery.HttpQueryCallBack callback = new HttpAsyncQuery.HttpQueryCallBack (){
       //Your own implementation
       };
       //create your downloader. optional, (it exist default one). it allow to specify how the queryer would read and build body response. default read and return plain text
        HttpAsyncQuery.HttpDownloadHandler<Integer> downloader = new HttpAsyncQuery.HttpDownloadHandler<Integer> (){
        //Your own implementation
        };
        //create an Http Query
        SimpleHttpQuery http = new SimpleHttpQuery();//can also use MutipartHttpQuery or your own HttpQuery<?> child instance
        //add http parm to your http query
        http.addParam("parm1","value1");
        http.addParam("param2","value2");
        //build and call your Async Http Query. for example a GET
        HttpAsyncQuery.from(http)
                .useEncoding("UTF-8")//optional default =UTF-8
                .useBufferSize(1024)//optional default=1024
                .useDownloader(downloader)//optional par defaut la lib telechargera et retournera du Text(qui peut etre du Json ou du XML).
                .setQueryCallBack(callback)//add a callback to call when request completed
                .doGet("http://www.google.com");
                //.doGet(callback, "http://www.google.com"); //make a GET Request with a specific callback
