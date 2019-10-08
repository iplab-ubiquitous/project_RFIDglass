package AccelTag;

import java.util.ArrayList;
import com.impinj.octane.*;
import java.lang.reflect.*;

public class Reader implements TagOpCompleteListener {
    private static Reader _instance = new Reader();
    ImpinjReader _reader;
    Object target;

    static final String hostname = "speedwayr-12-cd-64.local";

    private Reader() {
    }

    public static Reader getInstance() {
        return _instance;
    }

    public void init(Object thiz) {
        try {
            _reader = new ImpinjReader();
            _reader.connect(hostname);

            Settings settings = _reader.queryDefaultSettings();
            ReportConfig report = settings.getReport();
            report.setIncludeAntennaPortNumber(true);
            _reader.applySettings(settings);

            _reader.setTagOpCompleteListener(this);

            target = thiz;

        } catch (OctaneSdkException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }

    public void start() {
        try {
            _reader.start();
        } catch (OctaneSdkException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }

    public void stop() {
        try {
            _reader.stop();
        } catch (OctaneSdkException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }



    public int addReadOperation(short num) {
        try {
            TagOpSequence seq = new TagOpSequence();
            seq.setOps(new ArrayList<TagOp>());
            seq.setExecutionCount((short) 0);
            seq.setState(SequenceState.Active);

            // タグのセンサ値を取得
            TagReadOp readOpForSensorValue = new TagReadOp();
            readOpForSensorValue.setMemoryBank(MemoryBank.User);
            readOpForSensorValue.setWordCount((short) (2 + num));
            readOpForSensorValue.setWordPointer((short) 0x100);
            seq.getOps().add(readOpForSensorValue);

            // タグのシリアルナンバーを取得
            TagReadOp readOpForID = new TagReadOp();
            readOpForID.setMemoryBank(MemoryBank.Tid);
            readOpForID.setWordCount((short) 3);
            readOpForID.setWordPointer((short) 0x03);
            seq.getOps().add(readOpForID);

            seq.setTargetTag(null);
            _reader.addOpSequence(seq);
            return seq.getId();
        } catch (OctaneSdkException ex) {
            return -1;
        } catch (Exception ex) {
            return -1;
        }
    }

    public int addWriteOperation(byte data) {
        try {
            TagOpSequence seq = new TagOpSequence();
            seq.setOps(new ArrayList<TagOp>());
            seq.setExecutionCount((short) 1);
            seq.setState(SequenceState.Active);

            TagWriteOp writeOp = new TagWriteOp();
            writeOp.setMemoryBank(MemoryBank.User);
            writeOp.setWordPointer((short )0x100);
            writeOp.setData(TagData.fromByteArray(new byte[]{data}));
            seq.getOps().add(writeOp);
            seq.setTargetTag(null);
            _reader.addOpSequence(seq);
            return seq.getId();
        } catch (OctaneSdkException ex) {
            return -1;
        } catch (Exception ex) {
            return -1;
        }
    }

    public int addReadAccelOperation(short dataNum) {
        return addReadOperation(dataNum);
    }
    public int addReadMagnetOperation(short dataNum) {
        return addReadOperation(dataNum);
    }

    public int addWriteAccelOperation(byte data) {
        return addWriteOperation(data);
    }

    public int addReadTempOperation() {
        return addReadOperation((short) 1);
    }

    public int addWriteTempOperation(byte data) {
        return addWriteOperation(data);
    }

    public int addWriteLCDOperation(byte data) {
        return addWriteOperation(data);
    }

    public void deleteOperation(int id) {
        try {
            _reader.deleteOpSequence(id);
        } catch (OctaneSdkException ex) {
        } catch (Exception ex) {
        }
    }

    public void onTagOpComplete(ImpinjReader reader, TagOpReport results) {
        String strData = "";
        for(TagOpResult t : results.getResults()) {
            if(t instanceof TagReadOpResult){
                TagReadOpResult tr = (TagReadOpResult) t;
                if(tr.getResult() == ReadResultStatus.Success) {
                    strData += tr.getData().toHexWordString().replace(" ","");
                }
            }

            if(t instanceof TagWriteOpResult) {
                TagWriteOpResult tr = (TagWriteOpResult) t;
                if(tr.getResult() == WriteResultStatus.Success) {
//                    callWriteHandler();
                }
            }
        }
        byte[] data = hexStringToByteArray(strData);
        callReadHandler(data);
    }

    private void callReadHandler(byte[] data) {
        for(int i = 0; i < data.length; i++){
            if(i == 2 && (int)data[i] == 17) {
                continue;
            }
            System.out.print((int)(data[i]) + ", ");
        }
        System.out.println();
        if(data[0] == -49) {
            callAccelTagReadHandler(data);
            System.out.println("Accel: " + data);
        } else if(data[0] == 0x02) {
            callTempTagReadHandler(data);
        } else if(data[0] == -86){ //(0xAA)
            System.out.println("Magnet: " + data);
            callMagnetTagReadHandler(data);
        } else {
            callOtherTagReadHander(data);
            System.out.println("Other: " + data);
        }
    }



    private void callWriteHandler() {
        try{
            target.getClass().getMethod("TagWriteHandler").invoke(target);
        } catch(ReflectiveOperationException e) {
        }
    }

    private void callMagnetTagReadHandler(byte[] data) {
        try{
            int n = (data.length-10)/2;
            short[] values = new short[n+1];
            for(int i = 0; i < n; i++)
                values[i] = (short)((data[i*2 + 2 + 1] << 8) | (data[i*2 + 2]));
            values[n] = (short)(data[15]);
            target.getClass().getMethod("magnetTagReadHandler", new Class<?>[]{short[].class}).invoke(target, values);
        } catch(ReflectiveOperationException e) {
        }
    }

//    private void callTidReadhandler(byte[] data){
//        try{
//            int n = (data.length-4)/2;
//            short value = (short)((data[0] << 8*5) | (data[1] << 8*4) | (data[2] << 8*3) | (data[3] << 8*2)| (data[4] << 8*1) | (data[0]));
//            target.getClass().getMethod("TidReadHandler", new Class<?>[]{short[].class}).invoke(target, value);
//        } catch(ReflectiveOperationException e) {
//        }
//    }

    private void callAccelTagReadHandler(byte[] data) {
        try{
            int n = (data.length-4)/2;
            short[] values = new short[n];
            for(int i = 0; i < n; i++)
                values[n] = (short)((data[n*2 + 2] << 8) | (data[n*2 + 2 + 1]));
            target.getClass().getMethod("accelTagReadHandler", new Class<?>[]{short[].class}).invoke(target, values);
        } catch(ReflectiveOperationException e) {
        }
    }

    private void callTempTagReadHandler(byte[] data) {
        try{
            short value = (short)((data[2] << 8) | (data[3]));
            target.getClass().getMethod("tempTagReadHandler", new Class<?>[]{short.class}).invoke(target, value);
        } catch(ReflectiveOperationException e) {
        }
    }

    private void callOtherTagReadHander(byte[] data) {
        try{
            target.getClass().getMethod("otherTagReadHandler", new Class<?>[]{byte[].class}).invoke(target, data);
        } catch(ReflectiveOperationException e) {
        }
    }

    public static byte[] hexStringToByteArray(String s) {
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

}
