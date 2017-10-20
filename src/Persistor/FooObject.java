package Persistor;

import org.h2.mvstore.DataUtils;
import org.h2.mvstore.WriteBuffer;
import org.h2.mvstore.type.DataType;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cristian on 2017-10-17.
 */
public class FooObject implements DataType,Serializable, Comparable<FooObject> {

    public static final FooObject INSTANCE = new FooObject();


    int a;
    int b;

    List<String> stringList;


    public FooObject() { }

    public FooObject(int a, int b) {

        this.a = a;
        this.b = b;

        stringList = new ArrayList<>();
        stringList.add("röv!");
    }

    @Override
    public int compareTo(FooObject o) {

        if(this.a < this.b) return -1;
        if(this.b < this.a) return  1;

        return 0;

    }


    ///////////////////////////////////////////////////////////////////////

    @Override
    public int compare(java.lang.Object a, java.lang.Object b) {

       return ((FooObject)a).compareTo((FooObject)b);
    }

    @Override
    public int getMemory(java.lang.Object obj) {

        return 100;
    }

    @Override
    public void write(WriteBuffer buff, java.lang.Object obj) {

        byte[] serialized = FooObjectSerializer.getBytes( (FooObject)obj );
        buff.putVarInt(serialized.length).put(serialized);


    }

    @Override
    public void write(WriteBuffer buff, java.lang.Object[] obj, int len, boolean key) {

       // System.out.println("DEBUG: I am serializing!");

        for(int i = 0; i < len; ++i) {
            this.write(buff, obj[i]);
        }

    }

    @Override
    public FooObject read(ByteBuffer buff) {

     //   System.out.println("I am deserializing!");

        int length = DataUtils.readVarInt(buff);
     //   System.out.println("I read the capacity to: " + buff.capacity() );
   //     System.out.println("I read the length to: " + length);
        byte[] serialized = new byte[length];

     // System.out.println("the limit is; " + buff.limit( ));


        for(int i=0; i<length; i++) {

            serialized[i] = buff.get();

          //  System.out.println("YES: " + serialized[i]);
        }

        return FooObjectSerializer.getObject(serialized);

    }

    @Override
    public void read(ByteBuffer buff, java.lang.Object[] obj, int len, boolean key) {

       // System.out.println("I AM NOW I READ FUCNTION 2!!!!!!!!!!!!!!!");

        for(int i = 0; i < len; ++i) {
            obj[i] = this.read(buff);
        }

    }

    ///////////////////////////////////////////////////////////////////////



    @Override
    public String toString() {

        return a + " " +b + " " + this.stringList;
    }






}