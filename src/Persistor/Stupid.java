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
public class Stupid implements DataType,Serializable, Comparable<Stupid> {

    public static final Stupid INSTANCE = new Stupid();


    int a;
    int b;

    List<String> stringList;


    public Stupid() { }

    public Stupid(int a, int b) {

        this.a = a;
        this.b = b;

        stringList = new ArrayList<>();
        stringList.add("röv!");
    }

    @Override
    public int compareTo(Stupid o) {

        if(this.a < this.b) return -1;
        if(this.b < this.a) return  1;

        return 0;

    }


    ///////////////////////////////////////////////////////////////////////

    @Override
    public int compare(java.lang.Object a, java.lang.Object b) {

       return ((Stupid)a).compareTo((Stupid)b);
    }

    @Override
    public int getMemory(java.lang.Object obj) {

        return 100;
    }

    @Override
    public void write(WriteBuffer buff, java.lang.Object obj) {

        byte[] serialized = StupidSerializer.getBytes( (Stupid)obj );
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
    public Stupid read(ByteBuffer buff) {

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

        return StupidSerializer.getObject(serialized);

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