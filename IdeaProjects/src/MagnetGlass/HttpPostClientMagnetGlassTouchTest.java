package MagnetGlass;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class HttpPostClientMagnetGlassTouchTest {
    JSONObject postData = new JSONObject();
    String prevJson;

    final static int numOfPosition = 6; // NO TOUCHも含む
    final static int numOfData = 10;
    final static int numOfTag = 3;

    static int currentFingerPos;
    static int dataCount;
    static ArrayList<Integer> positionList = new ArrayList<Integer>();
    static Map<String, Boolean> hasReceived = new HashMap<String, Boolean>();
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
//        if(isDecidedHipassCutoffValue){
        if(filter.getIsCompleted()){
            // ハイパスフィルタがある
            datapost(values);
        }else{
            // ハイパスフィルタがない
            filter.setFileter(values);
//            settingHipassFilter(values);

        }

    }



    private void datapost(short[] values) {
        if(dataCount == numOfData && !hasReceived.containsValue(false)) {     //最後のデータのみ送信
            dataCount++;

            JSONObject collectedData = new JSONObject();
            double[] tagdata = filter.passFilter(values);
            collectedData.put("x", tagdata);
            collectedData.put("y", tagdata);
            collectedData.put("z", tagdata);

            String st_tagId = String.valueOf(-values[3]);
            postData.put(st_tagId, collectedData);
            System.out.println(st_tagId);
            hasReceived.put(st_tagId, true);

            postData.put("label", currentFingerPos);
            postJson(postData);
            System.out.println((dataCount - 1) + ":" + postData);
            for(String key : hasReceived.keySet()){
                hasReceived.put(key, false);
            }

            Reader.getInstance().stop();
            System.out.println("currentFingerPos: " + currentFingerPos + " 終了");
            System.out.println("Enterを押してください．");
            return;
        }
        else if(dataCount < numOfData) { // 最後以外のフレームは無視（現在何フレーム目かだけ出力）
            String st_tagId = String.valueOf(-values[3]);
            System.out.println(st_tagId);
            hasReceived.put(st_tagId, true);

            if(!hasReceived.containsValue(false)){
                for(String key : hasReceived.keySet()) {
                    dataCount++;
                    System.out.println("Frames remain:" + (numOfData - dataCount));
                    hasReceived.put(key, false);
                }
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
        reader.init(new HttpPostClientMagnetGlassTouchTest());
        int id = reader.addReadMagnetOperation((short) 3);

        System.out.println("カットオフ値の設定：動かないでください");
        System.out.println("準備できたらEnter");
        sc.nextLine();
        reader.start();

        sc.nextLine();
        filter.setIsCompleted(true);

        for(int i = 0; i < 10; i++){
            for(int j = 0; j < numOfPosition; j++){
                positionList.add(j);
            }
        }

        Collections.shuffle(positionList);

        for (int positon : positionList) {
            currentFingerPos = positon;
            dataCount = 0;
            for(String key : hasReceived.keySet()){
                hasReceived.put(key, false);
            }
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