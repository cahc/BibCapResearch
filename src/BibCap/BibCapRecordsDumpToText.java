package BibCap;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.type.ObjectDataType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Created by crco0001 on 4/4/2018.
 */
public class BibCapRecordsDumpToText {


    public static void main(String[] arg) throws IOException {


        if(arg.length != 1) { System.out.println("Supply db"); System.exit(0); }

        MVStore store = new MVStore.Builder().cacheSize(200). // 200MB read cache
                fileName(arg[0]).autoCommitBufferSize(1024). // 1MB write cache
                open(); // autoCommitBufferSize
        store.setVersionsToKeep(0);
        store.setReuseSpace(true);

        MVMap<Integer, BibCapRecord> map = store.openMap("mymap", new MVMap.Builder<Integer, BibCapRecord>().keyType(new ObjectDataType()).valueType(new BibCapRecord()));

        BufferedWriter writer = new BufferedWriter( new FileWriter( new File("RecordsInTextFormat.txt") ));

        for(Map.Entry<Integer, BibCapRecord> entry : map.entrySet()) {


            writer.write(entry.toString());
            writer.newLine();

        }


        writer.flush();
        writer.close();

        store.close();

    }

}
