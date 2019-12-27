package MagnetGlass;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class HttpPostClientMagnetGlassTouchTest {
    static JSONObject postData = new JSONObject();

    final static int numOfPosition = 6; // NO TOUCHも含む
    final static int numOfData = 20;
    final static int numOfTag = 2;

    double[][] data47 = new double[3][numOfData];
    boolean data47isCollected = false;
    double[][] data49 = new double[3][numOfData];
    boolean data49isCollected = false;

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
//        if(isDecidedHipassCutoffValue){
        if(filter.getIsCompleted()){
            // ハイパスフィルタがある
            if(dataCount <= numOfData) {
                datapost(values);
            }
        }else{
            // ハイパスフィルタがない
            filter.setFilter(values);

        }
    }



    private void datapost(short[] values) {
        String st_tagId = String.valueOf(-values[3]);
        System.out.println(st_tagId);

        double[] tagdata = filter.passFilter(values);

        if(st_tagId.equals("47")){
            data47[0][dataCount] = tagdata[0];
            data47[1][dataCount] = tagdata[1];
            data47[2][dataCount] = tagdata[2];
            data47isCollected = true;
        }else if(st_tagId.equals("49")){
            data49[0][dataCount] = tagdata[0];
            data49[1][dataCount] = tagdata[1];
            data49[2][dataCount] = tagdata[2];
            data49isCollected = true;
        }

        if(data47isCollected && data49isCollected) { //全てのタグデータが揃ったら処理実行
            if (dataCount == numOfData-1) { //最後のデータのみ送信
                for(int i = 0; i < 3; i++){
                    Arrays.sort(data47[i]);
                    Arrays.sort(data49[i]);
                }

                postData = new JSONObject();

                JSONObject collectedData47 = new JSONObject();
                collectedData47.put("x", (data47[0][dataCount/2] + data47[0][1+dataCount/2]) / 2);
                collectedData47.put("y", (data47[1][dataCount/2] + data47[1][1+dataCount/2]) / 2);
                collectedData47.put("z", (data47[2][dataCount/2] + data47[2][1+dataCount/2]) / 2);
                postData.put("47", collectedData47);

                JSONObject collectedData49 = new JSONObject();
                collectedData49.put("x", (data49[0][dataCount/2] + data49[0][1+dataCount/2]) / 2);
                collectedData49.put("y", (data49[1][dataCount/2] + data49[1][1+dataCount/2]) / 2);
                collectedData49.put("z", (data49[2][dataCount/2] + data49[2][1+dataCount/2]) / 2);
                postData.put("49", collectedData49);

                dataCount++;
                postData.put("label", currentFingerPos);
                postJson(postData);
                System.out.println(postData);

                Reader.getInstance().stop();
                System.out.println("currentFingerPos: " + currentFingerPos + " 終了");
                System.out.println("Enterを押してで次のタッチ位置へ．");
                return;
            }

            if (dataCount < numOfData-1) {
                data47isCollected = false;
                data49isCollected = false;
                dataCount++;
                System.out.println("Remain Frames :" + (numOfData - dataCount));
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
            for(int j = 1; j < numOfPosition; j++){
                positionList.add(j);
            }
        }

        Collections.shuffle(positionList);

        for (int positon : positionList) {
            currentFingerPos = positon;
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