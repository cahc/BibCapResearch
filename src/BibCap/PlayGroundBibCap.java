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


        BibCapParser bibCapParser = new BibCapParser();

        BibCapRecordStore recordStore = new BibCapRecordStoreInMemory();

        System.out.println("start parsing");

        bibCapParser.parse(recordStore);


        System.out.println("Done. Now writing to file");

        BufferedWriter writer = new BufferedWriter( new FileWriter( new File("rawOutput.txt") ));
        for (Map.Entry<Integer, BibCapRecord> record : recordStore.entrySetOfRecords()) {

            writer.write(record.toString());
            writer.newLine();

        }

    }




    }