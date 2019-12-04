package MagnetGlass;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Filter {
    private double alpha = 0;
    private int numOfHipassData = 100;
    private int numOfTag = 3;
    private boolean isCompleted;
    TreeMap<Integer, double[]> cutoffValues = new TreeMap<Integer, double[]>();
    Map<Integer, double[][]> DataMap = new HashMap<Integer, double[][]>();
    Map<Integer, Integer> numOfData = new HashMap<Integer, Integer>();


    protected Filter(){
        isCompleted = false;
    }
    protected Filter(int numOfTag){
        this.numOfTag = numOfTag;
        isCompleted = false;
    }

    protected Filter(int numOfTag, int numOfHipassData){
        this.numOfTag = numOfTag;
        this.numOfHipassData = numOfHipassData;
        isCompleted = false;
    }

    protected void setFilter(short[] values){
        Integer taglabel = -(int)values[3];
        numOfData.putIfAbsent(taglabel, 0);
        DataMap.putIfAbsent(taglabel, new double[3][numOfHipassData]);

//        各タグのデータ収集
        if(numOfData.get(taglabel) < numOfHipassData){
            System.out.println("Tag"+ taglabel + ": "+ numOfData.get(taglabel));
            double[][] tagdata = DataMap.get(taglabel);
            for(int i = 0; i < 3; i++){
                tagdata[i][numOfData.get(taglabel)] = (double)values[i];
            }
            DataMap.put(taglabel, tagdata);
            numOfData.put(taglabel, numOfData.get(taglabel)+1);
        }

//        データ数の合計を計算
        int datanum = 0;
        for(Integer v : numOfData.values()){
            datanum += v;
        }

//        データが揃った場合カットオフ値計算
        if(datanum == numOfHipassData * numOfTag){
            for(Integer key : numOfData.keySet()){
                numOfData.put(key, numOfHipassData + 1);
            }
            for(Map.Entry<Integer, double[][]> map : DataMap.entrySet()){
                double[] magnetdata = new double[3];
                for(int i = 0; i < 3; i++){
                    Arrays.sort(map.getValue()[i]);
                    magnetdata[i] = (map.getValue()[i][numOfHipassData/2 - 1] + map.getValue()[i][numOfHipassData/2] ) / 2;
                }
                cutoffValues.put(map.getKey(), magnetdata);
            }
            Reader.getInstance().stop();
            System.out.println("カットオフ値設定終了します");
            for(int key : cutoffValues.keySet()){
                System.out.println(key + ": " + Arrays.toString(cutoffValues.get(key)));
            }
            System.out.println("Enterキーで測定を開始します");

        }
    }

    protected boolean getIsCompleted(){
        return isCompleted;
    }

    protected void setIsCompleted(boolean bool){
        isCompleted = bool;
    }

    protected TreeMap<Integer, double[]> getcutoffValues(){
        return cutoffValues;
    }

    protected void setAlpha(double alpha){
        this.alpha = alpha;
    }

    protected double[] passFilter(short[] values){
        double[] tagdata = new double[3];
        for(int i = 0; i < 3; i++) {
            tagdata[i] = (double)values[i] - (alpha * (double)values[i] + (1 - alpha) * cutoffValues.get((int)values[3])[i]);
        }
        return tagdata;
    }
}
