package MagnetGlass;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Filter {
    private static final int numOfHipassData = 10;
    private static final double alpha = 0;
    private boolean isCompleted;
    int numOfTag;
    private static boolean isDecidedHipassCutoffValue = false;
    Map<Integer, double[]> cutoffValues = new HashMap<Integer, double[]>();
    Map<Integer, double[][]> sum = new HashMap<Integer, double[][]>();
    Map<Integer, Integer> num = new HashMap<Integer, Integer>();


    protected Filter(int numOfTag){
        this.numOfTag = numOfTag;
        isCompleted = false;
    }

    protected void setFileter(short[] values){
        Integer taglabel = (int)values[3];
        num.putIfAbsent(taglabel, 0);
        sum.putIfAbsent(taglabel, new double[3][numOfHipassData]);

//        各タグのデータ収集
        if(num.get(taglabel) < numOfHipassData){
            System.out.println("num"+ -taglabel + ": "+ num.get(taglabel));
            double[][] tagdata = sum.get(taglabel);
            for(int i = 0; i < 3; i++){
                tagdata[i][num.get(taglabel)] = (double)values[i];
            }
            sum.put(taglabel, tagdata);
            num.put(taglabel, num.get(taglabel)+1);
        }

//        データ数の合計を計算
        int datanum = 0;
        for(Integer v : num.values()){
            datanum += v;
        }

//        データが揃った場合カットオフ値計算
        if(datanum == numOfHipassData * numOfTag){
            for(Integer key : num.keySet()){
                num.put(key, numOfHipassData + 1);
            }
            for(Map.Entry<Integer, double[][]> map : sum.entrySet()){
                double[] magnetdata = new double[3];
                for(int i = 0; i < 3; i++){
                    Arrays.sort(map.getValue()[i]);
                    magnetdata[i] = (map.getValue()[i][numOfHipassData/2 - 1] + map.getValue()[i][numOfHipassData/2] ) / 2;
                }
                cutoffValues.put(map.getKey(), magnetdata);
            }
            Reader.getInstance().stop();
            System.out.println("カットオフ値設定終了します");
            System.out.println("Enterキーで測定を開始します");

        }
    }

    protected boolean getIsCompleted(){
        return isCompleted;
    }

    protected void setIsCompleted(boolean bool){
        isCompleted = bool;
    }


    protected double[] passFilter(short[] values){
        double[] tagdata = new double[3];
        for(int i = 0; i < 3; i++) {
            tagdata[i] = (double)values[i] - (alpha * (double)values[i] + (1 - alpha) * cutoffValues.get((int)values[3])[i]);
        }
        return tagdata;
    }
}
