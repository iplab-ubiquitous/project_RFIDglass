package MagnetGlass;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class FilterCheck {

    final static int numOfTag = 3;
    static int numOfData = 50;
    static ArrayList<Integer> positionList = new ArrayList<Integer>();
    static Filter filter;


    public void magnetTagReadHandler(short[] values) throws IOException {
            // ハイパスフィルタがない
            filter.setFileter(values);
        }


    public void otherTagReadHandler(byte[] data){

        //System.out.println(data);
    }

    private static boolean checkBeforeWritefile(File file){
        if (file.exists()){
            if (file.isFile() && file.canWrite()){
                return true;
            }
        }

        return false;
    }



    private static void outputCSV() {
        try{
            TreeMap<Integer, double[]> cutoffValues = filter.getcutoffValues();
            File file = new File("res/MagnetGlass/filterCheck" + numOfData + ".csv");
            boolean iscreated = file.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            if(iscreated){
                for(int key : cutoffValues.keySet()) {
                    bw.write("," + key + ".x");
                    bw.write("," + key + ".y");
                    bw.write("," + key + ".z");
                }

            }
            if(checkBeforeWritefile(file)){
                bw.newLine();
                for(int key : cutoffValues.keySet()) {
                    for(int i = 0; i < 3; i++) {
                        bw.write(","+ cutoffValues.get(key)[i]);
                    }
                }
                bw.close();
            }else{
                System.out.println("ファイルに書き込めません");
            }

        System.out.println("csv書き込み終了");
        return;
        }catch(IOException e){
            System.out.println(e);
        }
    }

    public static void main(String[] args)  {
        Scanner sc = new Scanner(System.in);
        Reader reader = Reader.getInstance();
        reader.init(new FilterCheck());
        int id = reader.addReadMagnetOperation((short) 3);

        for(int i = 0; i < 60; i++) {
            filter = new Filter(numOfTag, numOfData);
            System.out.println("カットオフ値の設定：動かないでください");
            System.out.println("準備できたらEnter");
            sc.nextLine();
            reader.start();
            sc.nextLine();
            outputCSV();
        }
    }
}