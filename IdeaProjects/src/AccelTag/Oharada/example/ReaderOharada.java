package AccelTag.Oharada.example;

import com.impinj.octane.*;

import java.util.ArrayList;

public class ReaderOharada implements TagOpCompleteListener {
    private static ReaderOharada _instance = new ReaderOharada();
    ImpinjReader _reader;
    Object target;

    static final String hostname = "speedwayr-12-cd-64.local";

    private ReaderOharada() {
    }

    public static ReaderOharada getInstance() {
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

            TagReadOp readOp = new TagReadOp();
            readOp.setMemoryBank(MemoryBank.User);
            readOp.setWordCount((short) (2 + num));
            readOp.setWordPointer((short) 0x100);
            seq.getOps().add(readOp);
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
        for(TagOpResult t : results.getResults()) {
            if(t instanceof TagReadOpResult){
                TagReadOpResult tr = (TagReadOpResult) t;
                if(tr.getResult() == ReadResultStatus.Success) {
                    String strData = tr.getData().toHexWordString().replace(" ","");
                    byte[] data = hexStringToByteArray(strData);

                    callReadHandler(data);
                }
            }

            if(t instanceof TagWriteOpResult) {
                TagWriteOpResult tr = (TagWriteOpResult) t;
                if(tr.getResult() == WriteResultStatus.Success) {
                    callWriteHandler();
                }
            }
        }
    }

    private void callReadHandler(byte[] data) {
        if(data[0] == 0x01) {
            callAccelTagReadHandler(data);
            System.out.println("accele comming");
        } else if(data[0] == 0x02) {
            callTempTagReadHandler(data);
        } else {
            callOtherTagReadHander(data);
        }
    }

    private void callWriteHandler() {
        try{
            target.getClass().getMethod("TagWriteHandler").invoke(target);
        } catch(ReflectiveOperationException e) {
        }
    }

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