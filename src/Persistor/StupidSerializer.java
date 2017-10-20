package Persistor;


import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import org.nustaq.serialization.FSTConfiguration;

/**
 * Created by Cristian on 2017-10-17.
 */
public class StupidSerializer {


    private static final FSTConfiguration conf;

    static {
        conf =FSTConfiguration.createDefaultConfiguration();
        conf.registerClass(Stupid.class);
    }


    public static byte[] getBytes(Stupid stupid) {

        byte barray[] = StupidSerializer.conf.asByteArray(stupid);
        return barray;
    }

    public static Stupid getObject(byte[] barray) {

        Stupid stupid = (Stupid)StupidSerializer.conf.asObject(barray);

        return stupid;
    }

    public static Stupid getObject(ByteIterable byteIterable) {

        Stupid stupid = (Stupid)StupidSerializer.conf.asObject( byteIterable.getBytesUnsafe() );

        return stupid;
    }


    public static ByteIterable getByteIterable(Stupid stupid) {


        ByteIterable byteIterable = new ArrayByteIterable( getBytes(stupid) );

        return byteIterable;

    }



}
