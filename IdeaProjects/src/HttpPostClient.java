



import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;


public class HttpPostClient {
    JSONObject postData = new JSONObject();
    JSONObject collectedData;
    String prevJson;

    static int currentFingerPos = 0;
    static int numOfData = 0;


    boolean hasReceived45 = false;
    boolean hasReceived47 = false;
    boolean hasReceived49 = false;


    public void postJson(JSONObject json){
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
            out.write(json.toString());
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
        collectedData = new JSONObject();
        collectedData.put("x", (int)(values[0]));
        collectedData.put("y", (int)(values[1]));
        collectedData.put("z", (int)(values[2]));
//

        switch (values[3]){
            case -45:
                postData.put("45", collectedData);
                hasReceived45 = true;
                break;
            case -47:
                postData.put("47", collectedData);
                hasReceived47 = true;
                break;
//            case -49:
//                postData.put("49", collectedData);
//                hasReceived49 = true;
//                break;

        }

        if(hasReceived45 && hasReceived47 && !(postData.toString().equals(prevJson))){
            postData.put("label", currentFingerPos);
            postJson(postData);
            System.out.println(postData);
            hasReceived45 = false;
            hasReceived47 = false;
            prevJson = postData.toString();
            numOfData++;
            if(numOfData > 100){
                Reader.getInstance().stop();
                numOfData = 0;
                System.out.println("currentFingerPos: " + currentFingerPos + " is ended.");
                System.out.println("終了.");
            }
        }

    }


    public void otherTagReadHandler(byte[] data){

        //System.out.println(data);
    }


    public static void main(String[] args)  {


        Scanner sc = new Scanner(System.in);
        Reader reader;
        reader = Reader.getInstance();
        reader.init(new HttpPostClient());
        int id = reader.addReadMagnetOperation((short) 3);
        System.out.println("FingerPosを入力");
        currentFingerPos = sc.nextInt();
        reader.start();
        System.out.println("開始.");
        sc.nextLine();

//        for(currentFingerPos = 0; currentFingerPos < 8; currentFingerPos++){
//            System.out.println("currentFingerPos: " + currentFingerPos);
//            reader.start();
//            System.out.println("開始.");
//            sc.nextLine();
//        }
//        System.out.println("終了.");



    }
}

