package Persistor;


import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import org.nustaq.serialization.FSTConfiguration;

/**
 * Created by Cristian on 2017-10-17.
 */
public class FooObjectSerializer {


    private static final FSTConfiguration conf;

    static {
        conf =FSTConfiguration.createDefaultConfiguration();
        conf.registerClass(FooObject.class);
    }


    public static byte[] getBytes(FooObject stupid) {

        byte barray[] = FooObjectSerializer.conf.asByteArray(stupid);
        return barray;
    }

    public static FooObject getObject(byte[] barray) {

        FooObject stupid = (FooObject) FooObjectSerializer.conf.asObject(barray);

        return stupid;
    }

    public static FooObject getObject(ByteIterable byteIterable) {

        FooObject stupid = (FooObject) FooObjectSerializer.conf.asObject( byteIterable.getBytesUnsafe() );

        return stupid;
    }


    public static ByteIterable getByteIterable(FooObject stupid) {


        ByteIterable byteIterable = new ArrayByteIterable( getBytes(stupid) );

        return byteIterable;

    }



}
