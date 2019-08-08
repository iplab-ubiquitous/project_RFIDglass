

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;



public class HttpPostClient {


    public void postJson(String json){
        HttpURLConnection con = null;
        StringBuffer result = new StringBuffer();
        try {

            URL url = new URL("http://localhost:8080");
            con = (HttpURLConnection) url.openConnection();

            // HTTPリクエストコード
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept-Language", "jp");
            // データがJSONであること、エンコードを指定する
            con.setRequestProperty("Content-Type", "application/JSON; charset=utf-8");
            // POSTデータの長さを設定
            con.setRequestProperty("Content-Length", String.valueOf(json.length()));
            // リクエストのbodyにJSON文字列を書き込む
            OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
            //con.connect();
            //out.write("{\"result\":\"faiiure\"}");
            out.write(json);
            out.close();

            // HTTPレスポンスコード
            final int status = con.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                // 通信に成功した
                // テキストを取得する
                final InputStream in = con.getInputStream();
                String encoding = con.getContentEncoding();
                if (null == encoding) {
                    encoding = "UTF-8";
                }
                final InputStreamReader inReader = new InputStreamReader(in, encoding);
                final BufferedReader bufReader = new BufferedReader(inReader);
                String line = null;
                // 1行ずつテキストを読み込む
                while ((line = bufReader.readLine()) != null) {
                    result.append(line);
                }
                System.out.println(result);
                bufReader.close();
                inReader.close();
                in.close();
            } else {
                // 通信が失敗した場合のレスポンスコードを表示
                System.out.println(status);
            }

        }catch (Exception e1){

        } finally {
            if (con != null) {
                // コネクションを切断
                con.disconnect();
            }
        }
    }



    public void magnetTagReadHandler(short[] values) throws IOException {
        String resultsJson = "{" + " \"x\":\"" + values[0] + "\" , \"y\": \"" + values[1] + "\" , \"z\":\"" + values[2] + "\" }";
        postJson(resultsJson);
        System.out.println(resultsJson);


    }
    public void otherTagReadHandler(byte[] data){

        //System.out.println(data);
    }

//    private static final String WEB_API_ENDPOINT = "http://localhost:8080";
//    public String callWebAPI(short[] values)  throws IOException {
//
//        final Map<String, String> httpHeaders = new LinkedHashMap<String, String>();
//        String postJson = "{" + " \"x\":\"" + values[0] + "\" , \"y\": \"" + values[1] + "\" , \"z\":\"" + values[2] + "\" }";
//        final String resultStr = doPost(WEB_API_ENDPOINT, "UTF-8", httpHeaders, postJson);
//
//        return resultStr;
//    }
//    public String doPost(String url, String encoding, Map<String, String> headers, String jsonString) throws IOException {
//        final okhttp3.MediaType mediaTypeJson = okhttp3.MediaType.parse("application/json; charset=" + encoding);
//
//        final RequestBody requestBody = RequestBody.create(mediaTypeJson, jsonString);
//
//        final Request request = new Request.Builder()
//                .url(url)
//                .headers(Headers.of(headers))
//                .post(requestBody)
//                .build();
//
//        final OkHttpClient client = new OkHttpClient.Builder()
//                .build();
//        final Response response = client.newCall(request).execute();
//        final String resultStr = response.body().string();
//        return resultStr;
//    }

    public static void main(String[] args)  {



        Reader reader;
        reader = Reader.getInstance();
        reader.init(new HttpPostClient());
        reader.start();


        int id = reader.addReadMagnetOperation((short) 3);


        System.out.println("動いている");


    }
}

