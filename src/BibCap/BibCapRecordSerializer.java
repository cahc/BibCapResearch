package BibCap;

import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import org.nustaq.serialization.FSTConfiguration;

/**
 * Created by Cristian on 2017-10-20.
 */
public class BibCapRecordSerializer {

    private static final FSTConfiguration conf;

    static {
        conf =FSTConfiguration.createDefaultConfiguration();
        conf.registerClass(BibCapRecord.class);
    }



    public static byte[] getBytes(BibCapRecord record) {

        byte barray[] = BibCapRecordSerializer.conf.asByteArray(record);
        return barray;
    }

    public static BibCapRecord getObject(byte[] barray) {

        BibCapRecord record = (BibCapRecord)BibCapRecordSerializer.conf.asObject(barray);

        return record;
    }

    public static BibCapRecord getObject(ByteIterable byteIterable) {

        BibCapRecord record = (BibCapRecord)BibCapRecordSerializer.conf.asObject( byteIterable.getBytesUnsafe() );

        return record;
    }


    public static ByteIterable getByteIterable(BibCapRecord record) {


        ByteIterable byteIterable = new ArrayByteIterable( getBytes(record) );

        return byteIterable;

    }



}
