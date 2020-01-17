package MagnetGlass;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class HttpPostClientMagnetGlass {
    static JSONObject postData = new JSONObject();
    static String prevJson;

    final static int numOfPosition = 5; // NO TOUCHも含まない
    final static int numOfTag = 2;

    static int numOfData = 100;
    static int currentFingerPos;
    static int dataCount;
    static ArrayList<Integer> positionList = new ArrayList<Integer>();
    static Filter filter = new Filter(numOfTag);



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
        if(filter.getIsCompleted()){
            // ハイパスフィルタがある
            datapost(values);
        }else{
            // ハイパスフィルタがない
            filter.setFilter(values);
        }
    }


    private void datapost(short[] values) {

        if(dataCount == numOfData) {
            dataCount++;
            Reader.getInstance().stop();
            System.out.println("currentFingerPos: " + currentFingerPos + " 終了");
            System.out.println("Enterを押してください．");
            return;
        }
        else if(dataCount < numOfData) {
 
            JSONObject collectedData = new JSONObject();
            double[] tagdata = filter.passFilter(values);

            collectedData.put("x", tagdata[0]);
            collectedData.put("y", tagdata[1]);
            collectedData.put("z", tagdata[2]);

            String st_tagId = String.valueOf(-values[3]);
            postData.put(st_tagId, collectedData);
            System.out.println(st_tagId);

            if(postData.length() == numOfTag && !(postData.toString().equals(prevJson))){
                dataCount++;
                prevJson = postData.toString();   //前の磁気データ保持
                postData.put("label", currentFingerPos);
                postJson(postData);
                System.out.println((dataCount - 1) + ":" + postData);
                postData = new JSONObject();
                return;
            }
        }
    }


    public void otherTagReadHandler(byte[] data){

        //System.out.println(data);
    }


    public static void main(String[] args)  {
        Scanner sc = new Scanner(System.in);
        Reader reader = Reader.getInstance();
        reader.init(new HttpPostClientMagnetGlass());
        int id = reader.addReadMagnetOperation((short) 3);

        System.out.println("カットオフ値の設定：動かないでください");
        System.out.println("準備できたらEnter");
        sc.nextLine();
        reader.start();

        sc.nextLine();
        filter.setIsCompleted(true);

        for(int i = 0; i < numOfPosition; i++){
            positionList.add(i);
        }

//        Collections.shuffle(positionList);

        for (int position : positionList) {
            currentFingerPos = position;
            dataCount = 0;
            postData = new JSONObject();
            System.out.println("タッチ位置[" + (currentFingerPos) + "]に触れてください.");
            System.out.println("準備できたらEnter.");
            sc.nextLine();
            reader.start();
            System.out.println("currentFingerPos: " + currentFingerPos);
            System.out.println("開始.");
            sc.nextLine();
        }
        System.out.println("終了.");
        return;
    }
}