package MagnetGlass;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;


public class HttpPostClientMagnetGlass {
    JSONObject postData = new JSONObject();
    JSONObject collectedData;
    String prevJson;

    static int currentFingerPos;
    final static int numOfPosition = 11; // NO TOUCHも含む
    static int dataCount = 0;
    final static int numOfData = 3;
    static ArrayList<Integer> positionList = new ArrayList<Integer>();

    static boolean hasReceived45 = false;
    static boolean hasReceived47 = false;
    static boolean hasReceived49 = false;

    double[] cutoffValues45 = new double[3];
    double[] cutoffValues47 = new double[3];
    double[] cutoffValues49 = new double[3];
    double alpha = 0.8;
    private static boolean isDecidedHipassCutoffValue = false;

    double[] sum45 = new double[3];
    double[] sum47 = new double[3];
    double[] sum49 = new double[3];

    int num45 = 0;
    int num47 = 0;
    int num49 = 0;
    final int numOfHipassData = 3;


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

    public double hipassFilter(double value, int index, int tagNum){
        double result = 0.0;
        switch (tagNum){
            case -45:
                result = value - (alpha * value + (1-alpha) * cutoffValues45[index]);
                break;
            case -47:
                result = value - (alpha * value + (1-alpha) * cutoffValues47[index]);
                break;
            case -49:
                result = value - (alpha * value + (1-alpha) * cutoffValues49[index]);
                break;
        }
        result = Math.round(result * 100) / (double)100; //小数第2位まで表示（第3位を丸める）
        return result;
    }


    public void magnetTagReadHandler(short[] values) throws IOException {
        collectedData = new JSONObject();
        if(isDecidedHipassCutoffValue){
            // ハイパスフィルタがある
            datapost(values);
        }else{
            // ハイパスフィルタがない
            settingHipassFilter(values);

        }

    }

    private void settingHipassFilter(short[] values) {
        switch (values[3]){
            case -45:
                if(num45 < numOfHipassData) {
                    System.out.println("num45: " + num45);
                    sum45[0] += (double)values[0];
                    sum45[1] += (double)values[1];
                    sum45[2] += (double)values[2];
                    num45++;
                }
                break;
            case -47:
                if(num47 < numOfHipassData) {
                    System.out.println("num47: " + num47);
                    sum47[0] += (double)values[0];
                    sum47[1] += (double)values[1];
                    sum47[2] += (double)values[2];
                    num47++;
                }
                break;
            case -49:
                if(num49 < numOfHipassData){
                    System.out.println("num49: " + num49);
                    sum49[0] += (double)values[0];
                    sum49[1] += (double)values[1];
                    sum49[2] += (double)values[2];
                    num49++;
                }
                break;
        }
        if(num45 == numOfHipassData && num47 == numOfHipassData && num49 == numOfHipassData){
            num45++;
            num47++;
            num49++;
            for (int i = 0; i < 3; i++){
                cutoffValues45[i] = sum45[i]/(double)numOfHipassData;
                cutoffValues47[i] = sum47[i]/(double)numOfHipassData;
                cutoffValues49[i] = sum49[i]/(double)numOfHipassData;
                System.out.println(cutoffValues45[i]);
                System.out.println(cutoffValues47[i]);
                System.out.println(cutoffValues49[i]);
            }

            Reader.getInstance().stop();

            System.out.println("カットオフ値設定終了");
            System.out.println("Enterキーで測定を開始します");

        }
    }

    private void datapost(short[] values) {
        if(dataCount == numOfData) {
            dataCount++;
            Reader.getInstance().stop();
            //dataCount = 0;
//                    System.out.println("currentFingerPos: " + currentFingerPos + "終了　次の指の位置を入力してください");
            System.out.println("currentFingerPos: " + currentFingerPos + " 終了");
            System.out.println("Enterを押してください．");
            return;
        }
        else if(dataCount < numOfData) {
            collectedData.put("x", hipassFilter((double) (values[0]), 0, (int) (values[3])));
            collectedData.put("y", hipassFilter((double) (values[1]), 1, (int) (values[3])));
            collectedData.put("z", hipassFilter((double) (values[2]), 2, (int) (values[3])));

            switch (values[3]) {
                case -45:
                    postData.put("45", collectedData);
                    System.out.println("45");
                    hasReceived45 = true;
                    break;
                case -47:
                    postData.put("47", collectedData);
                    System.out.println("47");
                    hasReceived47 = true;
                    break;
                case -49:
                    postData.put("49", collectedData);
                    System.out.println("49");
                    hasReceived49 = true;
                    break;

            }

            if (hasReceived45 && hasReceived47 && hasReceived49) {
                dataCount++;

                postData.put("label", currentFingerPos);
                postJson(postData);
                System.out.println((dataCount - 1) + ":" + postData);
                hasReceived45 = false;
                hasReceived47 = false;
                hasReceived49 = false;
                //            prevJson = postData.toString();   //前の磁気データ保持
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
        isDecidedHipassCutoffValue = true;
//        System.out.println("FingerPosを入力");
//        currentFingerPos = sc.nextInt();
//        reader.start();
//        System.out.println("開始.");
//        sc.nextLine();

        for(int i = 0; i < numOfPosition; i++){
            positionList.add(i);
        }

        Collections.shuffle(positionList);

        for (int positon : positionList) {
            currentFingerPos = positon;
            System.out.println("タッチ位置[" + (currentFingerPos) + "]に触れてください.");
            System.out.println("準備できたらEnter.");
            sc.nextLine();
            dataCount = 0;
            hasReceived45 = false;
            hasReceived47 = false;
            hasReceived49 = false;
            reader.start();
            System.out.println("currentFingerPos: " + currentFingerPos);
            System.out.println("開始.");
            sc.nextLine();
        }
        System.out.println("終了.");
        return;
    }
}

