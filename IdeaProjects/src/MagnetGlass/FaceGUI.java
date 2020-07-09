package MagnetGlass;


import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;


class MyFrame extends JFrame {
    BackgroundPanel backgroundPanel = new BackgroundPanel();
    public MyFrame() {
        setTitle("Touch point visualizer");
        setSize(900, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().add(backgroundPanel, BorderLayout.CENTER);
    }
}

class BackgroundPanel extends JPanel{
    Image img;
    Ellipse2D pos[];
    public BackgroundPanel(){
        img = Toolkit.getDefaultToolkit().getImage("img/face.png");
        pos = new Ellipse2D[7];
        for (int i = 1; i < pos.length; i++){
            pos[i] = new Ellipse2D.Double(200+((i-1)%3)*(205 - ((i-1)/3)*20) + ((i-1)/3)*20, 250+((i-1)/3)*180, 80, 80);
        }
    }
    int currentPosition = 0;
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        //画像の表示
        Graphics2D g2 = (Graphics2D)g;
        g2.drawImage(img, 50, 50, 800, 700, this);

        for(int i = 1; i < pos.length; i++){
            g2.setColor(Color.white);
            if(i == currentPosition){
                g2.setColor(Color.red);
            }
            g2.fill(pos[i]);
            g2.setColor(Color.black);
            g2.draw(pos[i]);
        }
    }
}


public class FaceGUI {
    static JSONObject postData = new JSONObject();

    final static int numOfPosition = 7; // NO TOUCHは含まない
    final static int numOfData = 10;
    final static int numOfTag = 2;

    double[][] data47 = new double[3][numOfData];
    boolean data47isCollected = false;
    double[][] data49 = new double[3][numOfData];
    boolean data49isCollected = false;

    static int dataCount;
    static Filter filter = new Filter(numOfTag, 10);

    static MyFrame f = new MyFrame();




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
                JSONObject response = new JSONObject(String.valueOf(result));
                System.out.println(response.getInt("predict"));
                f.backgroundPanel.currentPosition = response.getInt("predict");
                f.repaint();
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
        // ハイパスフィルタがある
        if(filter.getIsCompleted()){
            datapost(values);
        }else{
            // ハイパスフィルタがない
            filter.setFilter(values);

        }
    }



    private void datapost(short[] values) {
        String st_tagId = String.valueOf(-values[3]);
//        System.out.println(st_tagId);  // 通信したタグNo.を表示

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
            data47isCollected = false;
            data49isCollected = false;
            dataCount++;
//            System.out.println("Remain Frames :" + (numOfData - dataCount));

            if (dataCount >= numOfData) { //データの中央値を送信
                dataCount=0;
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


                postJson(postData);
                System.out.println(postData);


                return;
            }
        }
    }


    public static void main(String[] args)  {
        f.setVisible(true);
        Scanner sc = new Scanner(System.in);
        Reader reader = Reader.getInstance();
        reader.init(new FaceGUI());
        int id = reader.addReadMagnetOperation((short) 3);

        System.out.println("カットオフ値の設定：動かないでください");
        System.out.println("準備できたらEnter");
        sc.nextLine();
        reader.start();

        sc.nextLine();
        filter.setIsCompleted(true);
        System.out.println("開始");

        dataCount = 0;
        postData = new JSONObject();
        reader.start();

        sc.nextLine();
        reader.stop();
        System.out.println("終了.");
        System.exit(0);
        return;
    }
}
