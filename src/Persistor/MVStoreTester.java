package Persistor;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.type.ObjectDataType;

import java.util.Random;

/**
 * Created by Cristian on 2017-10-17.
 */
public class MVStoreTester {


    public static void main(String[] arg) {

        Random random = new Random();


        MVStore store = new MVStore.Builder().cacheSize(200). // 200MB read cache
                        fileName("mappy.db").autoCommitBufferSize(1024). // 1MB write cache
                        open(); // autoCommitBufferSize

                        store.setVersionsToKeep(0);
                        store.setReuseSpace(true);

                        //store.setReuseSpace(true);

        MVMap<Integer, FooObject> mappy = store.openMap("mymap", new MVMap.Builder<Integer, FooObject>().keyType(new ObjectDataType()).valueType(FooObject.INSTANCE));

        if(arg.length > 0) {


            System.out.println("writing 500 000 objects to disk");
            long start0 = System.currentTimeMillis();

            FooObject stupid = null;
            for (int i = 0; i < 500000; i++) {

                if (i == 9999) {

                    stupid = new FooObject(random.nextInt(10000000), random.nextInt(10000000));
                    stupid.stringList.add("added 0");
                    stupid.stringList.add("added 1");
                } else {

                    stupid = new FooObject(random.nextInt(10000000), random.nextInt(10000000));


                }

                //   byte[] serializedStupid = Persistor.FooObjectSerializer.getBytes( stupid );




                mappy.put(i, stupid);

            }


            System.out.println("that took: " + (System.currentTimeMillis() - start0)/1000.0 );
        }

        long start2 = System.currentTimeMillis();

        store.commit();

        System.out.println("commiting took: " + (System.currentTimeMillis() - start2)/1000.0 );

        System.out.println("db size: " + mappy.size());

        System.out.println("100 000 random gets()");
        long start = System.currentTimeMillis();


        for(int i=0; i<100000; i++) {

           FooObject tester =  mappy.get(random.nextInt(400000));

        }

        System.out.println("that took: " + (System.currentTimeMillis() - start)/1000.0 );

        System.out.println("Cashe size used " +   store.getCacheSizeUsed() );

        store.compactMoveChunks();

        store.close();


    }


}
