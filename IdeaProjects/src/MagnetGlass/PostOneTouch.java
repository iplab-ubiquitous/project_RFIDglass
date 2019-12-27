package MagnetGlass;

//import javafx.geometry.Pos;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Scanner;

public class PostOneTouch extends HttpPostClientMagnetGlass{
    protected PostOneTouch(){
        this.numOfData = 10;
        this.currentFingerPos = 0;
        this.filter = new Filter(2, numOfData);
    }

    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        Reader reader = Reader.getInstance();
        reader.init(new PostOneTouch());
        int id = reader.addReadMagnetOperation((short) 3);

        System.out.println("カットオフ値の設定：動かないでください");
        System.out.println("準備できたらEnter");
        sc.nextLine();
        reader.start();

        sc.nextLine();
        filter.setIsCompleted(true);
        dataCount = 0;
        postData = new JSONObject();
        System.out.println("タッチ位置[" + (currentFingerPos) + "]に触れてください.");
        System.out.println("準備できたらEnter.");
        sc.nextLine();
        reader.start();
        System.out.println("currentFingerPos: " + currentFingerPos);
        System.out.println("開始.");
        sc.nextLine();
        System.out.println("終了.");
    }

}
