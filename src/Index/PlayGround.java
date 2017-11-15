package Index;

import BibCap.BibCapRecord;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.type.ObjectDataType;

import java.io.*;
import java.util.HashSet;
import java.util.Map;

public class PlayGround {


    public static void main(String[] arg) throws IOException {




        //

        BufferedReader reader = new BufferedReader(  new FileReader( new File(arg[0])  ));

        HashSet<String> uniqueUT = new HashSet<>();

        String line;
        boolean isFirst = true;
        while(  (line = reader.readLine()) != null    ) {

            if(isFirst) {isFirst = false; continue; }

            String[] part = line.split("\t");

            uniqueUT.add(part[1]);

        }

        System.out.println(uniqueUT.size() + " unique UTs in .txt");


        System.out.println("Reading unique UTs from mappy.db");



        MVStore store = new MVStore.Builder().cacheSize(200). // 200MB read cache
                fileName( "mappy.db" ).autoCommitBufferSize(1024). // 1MB write cache
                open(); // autoCommitBufferSize
        store.setVersionsToKeep(0);
        store.setReuseSpace(true);

        MVMap<Integer, BibCapRecord> map = store.openMap("mymap", new MVMap.Builder<Integer,BibCapRecord>().keyType(new ObjectDataType()).valueType( new BibCapRecord() ));


        HashSet<String> uniqueUTsInMappy = new HashSet<>();
        for(Map.Entry<Integer,BibCapRecord> entry : map.entrySet()) {


            BibCapRecord record = entry.getValue();
            uniqueUTsInMappy.add( record.getUT() );

        }

        System.out.println(uniqueUTsInMappy.size() + " unique UTs in .db");

        System.out.println("Now saving UT:s in .db that don't have any references according to .txt");
        int count = 0;

        for(String ut : uniqueUTsInMappy) {


            if(  !uniqueUT.contains(ut)  ) {

                count++;


            }

        }


        System.out.println(count + " UTs dont have any cited refs..");

    }


}
