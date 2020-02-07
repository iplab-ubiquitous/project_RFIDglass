package MagnetGlass;

//import javafx.geometry.Pos;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class PostOneTouch extends HttpPostClientMagnetGlass{
    int numOfData;
    static long start;
    static List<Long> timeData = new LinkedList<>();
    static List<String> strData = new LinkedList<>();
    static List<Double> xData = new LinkedList<>();
    static List<Double> yData = new LinkedList<>();
    static List<Double> zData = new LinkedList<>();
    protected PostOneTouch(){
        this.numOfData = 2000;
        this.dataCount = 0;
    }

    public void magnetTagReadHandler(short[] values) throws IOException {
        datapost(values);
    }


    private void datapost(short[] values) {

        if(dataCount == numOfData) {
            dataCount++;
            Reader.getInstance().stop();
            System.out.println("計測終了");
            System.out.println("Enterを押してください．");
            return;
        }

        else if(dataCount < numOfData) {
            dataCount++;
            xData.add((double) values[0]);
            yData.add((double) values[1]);
            zData.add((double) values[2]);

            String st_tagId = String.valueOf(-values[3]);

            long t = System.currentTimeMillis() - start;
            postData.put(st_tagId, t);
            timeData.add(t);
            strData.add(st_tagId);
            System.out.println(dataCount+ "," + (double) values[0] + "," + (double) values[1] + "," + (double) values[2] + "," + t);

//
//            if (postData.length() == numOfTag) {
//                dataCount++;
//                strData.add("POST");
//                timeData.add(t);
//                System.out.println("POST," + t);
//                postData = new JSONObject();
//            }
        }

    }

    private static void outputCSV() {
        try {
            File file = new File("res/MagnetGlass/magnetdata/m1d20.csv");
            file.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));

            if (checkBeforeWritefile(file)) {
                for (int i = 0; i < timeData.size(); i++) {
                    bw.write(xData.get(i) + "," + yData.get(i) + "," + zData.get(i) + "," + timeData.get(i).toString() +  "\n");
                }
                bw.close();
            }else {
                System.out.println("ファイルに書き込めません");
            }
            System.out.println("csv書き込み終了");
            return;
        }catch(IOException e){
            System.out.println(e);
        }
    }

    private static boolean checkBeforeWritefile(File file){
        if (file.exists()){
            if (file.isFile() && file.canWrite()){
                return true;

            }
        }

        return false;
    }

    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        Reader reader = Reader.getInstance();
        reader.init(new PostOneTouch());
        int id = reader.addReadMagnetOperation((short) 3);
        System.out.println("計測を開始します.");
        reader.start();
        start = System.currentTimeMillis();

        sc.nextLine();
        System.out.println("終了.");
        outputCSV();
        System.exit(0);
    }

}
