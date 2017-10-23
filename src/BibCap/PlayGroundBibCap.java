package BibCap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Created by crco0001 on 10/11/2017.
 */
public class PlayGroundBibCap {


    public static void main(String[] arg) throws IOException {


        long start = System.currentTimeMillis();

        BibCapParser bibCapParser = new BibCapParser();


        //on disk
        BibCapRecordStoreMVStore recordStore = new BibCapRecordStoreMVStore();

        //in memory
        //BibCapRecordStore recordStore = new BibCapRecordStoreInMemory();

        System.out.println("start parsing");

        bibCapParser.parse(recordStore);

        System.out.println("Done. That took: " + (System.currentTimeMillis()-start)/1000.0 + ". Now writing to file");

        recordStore.saveChanges();
        recordStore.compact();

        BufferedWriter writer = new BufferedWriter( new FileWriter( new File("rawOutput.txt") ));

        System.out.println("# mappings:  " + recordStore.size());


        for (Map.Entry<Integer, BibCapRecord> entry : recordStore.entrySetOfRecords()) {

            BibCapRecord record = entry.getValue();
            if(!record.isConsideredRecord()) continue;

            writer.write(record.toString());
            writer.newLine();

        }

        writer.flush();
        writer.close();


        recordStore.close();
    }




    }