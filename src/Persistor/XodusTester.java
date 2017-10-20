package Persistor;




import jetbrains.exodus.bindings.IntegerBinding;
import jetbrains.exodus.env.*;

import java.util.Random;

/**
 * Created by Cristian on 2017-10-19.
 */
public class XodusTester {


    public static void main(String[] arg) throws InterruptedException {
        
        Random random = new Random();

        EnvironmentConfig config = new EnvironmentConfig().
                setMemoryUsage(200 * 1024 *1024).

                setGcEnabled(true);

        Environment environment = Environments.newInstance("HELLOXODUS",config);


        Transaction txn = environment.beginTransaction();


        // opening a store without key duplicates and with prefixing
        Store store = environment.openStore("MyStore", StoreConfig.WITHOUT_DUPLICATES_WITH_PREFIXING, txn);
        txn.commit();




        //IntegerBinding.intToCompressedEntry(4);
        //IntegerBinding.compressedEntryToInt( );

        System.out.println("writing 500 000 objects to disk");
        long start0 = System.currentTimeMillis();

        FooObject stupid = null;
        for (int i = 0; i < 500000; i++) {

            if (i == 9999) {

                stupid = new FooObject(random.nextInt(10000000), random.nextInt(10000000));
                stupid.stringList.add("added 1");
                stupid.stringList.add("added 2");
            } else {

                stupid = new FooObject(random.nextInt(10000000), random.nextInt(10000000));


            }

            txn = environment.beginTransaction();


          store.put(txn, IntegerBinding.intToCompressedEntry(i), FooObjectSerializer.getByteIterable(stupid) );

          txn.commit();

        }


        System.out.println("that took: " + (System.currentTimeMillis() - start0)/1000.0 );
        System.out.print("size of db: ");
        txn = environment.beginTransaction();
        long size = store.count(txn);
        txn.commit();
        System.out.println(size);


        System.out.println("100 000 random gets()");
        long start = System.currentTimeMillis();


        for(int i=0; i<100000; i++) {

            txn = environment.beginTransaction();

            FooObject tester =  FooObjectSerializer.getObject(  store.get(txn, IntegerBinding.intToCompressedEntry(i)) );
            txn.commit();

            if(i==9999) {

                System.out.println(tester.toString());
            }
        }

        System.out.println("that took: " + (System.currentTimeMillis() - start)/1000.0 );



        environment.close();




    }


}
