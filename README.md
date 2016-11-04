# android-IstatNetwork
And Android Library to make Http Query Easy.

```java

 HttpAsyncQuery.HttpQueryCallBack callback = createCallback();//initialiser un HttpQueryCallBack ou un HttpCallback
        HttpAsyncQuery.HttpDownloadHandler<Integer> downloader =createDownloader();//un downloader, qui defini comment la lib telechargera les données Http
        SimpleHttpQuery http = new SimpleHttpQuery();
        HttpAsyncQuery.from(http)
                .useEncoding("UTF-8")//optionel default =UTF-8
                .useBufferSize(1024)//optionel default=1024
                .useDownloader(downloader)//optione par defaut la lib telechargera et retournera du Text(qui peut etre du Json ou du XML).
                .setQueryCallBack(callback)//ajouter un callback a utiliser pour votre requete
                .doGet("http://www.google.com");
                //.doGet(callback, "http://www.google.com"); //effectuer une requête sur une URL en précisant un callback particulier
