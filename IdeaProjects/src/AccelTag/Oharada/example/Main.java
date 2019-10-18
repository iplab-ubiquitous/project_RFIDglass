package AccelTag.Oharada.example;
import processing.core.*;

public class Main extends PApplet {
    ReaderOharada reader;
    int c = 0;
    int id;
    @Override
    public void setup() {
        reader = ReaderOharada.getInstance();
        reader.init(this);
        reader.start();

        id = reader.addReadAccelOperation((short)1);
    }

    @Override
    public void draw() {
    }

    public void otherTagReadHandler(byte[] data) {
        println("other: " + ++c);
    }

    @Override
    public void keyPressed() {
        reader.deleteOperation(id);
    }

    @Override
    public void dispose() {
        reader.stop();
        println("exit.");
    }

    public static void main(String[] args){
        PApplet.main("AccelTag.Oharada.example.Main");
    }
}
