package BibCap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Created by crco0001 on 10/11/2017.
 */
public class PlayGroundBibCapWriter {


    public static void main(String[] arg) throws IOException {


        long start = System.currentTimeMillis();

        BibCapParser bibCapParser = new BibCapParser();


        //on disk
       BibCapRecordStoreMVStore recordStore = new BibCapRecordStoreMVStore();

        //in memory
        //BibCapRecordStore recordStore = new BibCapRecordStoreInMemory();

        System.out.println("start parsing");

        System.out.println("OBS! Not using n-grams");

        bibCapParser.parse(recordStore, false);

        System.out.println("Done. That took: " + (System.currentTimeMillis()-start)/1000.0 + ". Now writing to file");

       recordStore.saveChanges();
       //recordStore.compact(); //todo check: memory explodes here!

    //    BufferedWriter writer = new BufferedWriter( new FileWriter( new File("rawOutput.txt") ));

        System.out.println("# mappings:  " + recordStore.size());



        //orderedSet

        List<String> problematicUTs = new ArrayList<>(); // se Wos_refvalues_debugging.xlsx
        problematicUTs.add("000267281800014");
        problematicUTs.add("000265827300008");
        problematicUTs.add("000272998000008");
        problematicUTs.add("000270754400012");

        //TODO and add those UT:s that are not in the 2018 citation update.
        //here thay are!

        problematicUTs.add("000282630200001");
        problematicUTs.add("000282630200002");
        problematicUTs.add("000282630200003");


        for (Integer key : bibCapParser.getKeySet() ) {

            BibCapRecord record = recordStore.getRecord(key);

            if(!record.isConsideredRecord()) {recordStore.remove(key) ; continue; }

            if(problematicUTs.contains( record.getUT() )) { recordStore.remove(key); System.out.println("1 of the 7 problmatic records removed"); continue;  }

           // writer.write(record.toString());
          //  writer.newLine();

        }

        //writer.flush();
       // writer.close();

        recordStore.saveChanges();
        System.out.println("new # mappings:  " + recordStore.size());
       recordStore.close();
    }




    }