package BibCap;

import NLP.RAKE;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.type.ObjectDataType;

import java.io.File;
import java.util.Map;

/**
 * Created by crco0001 on 10/26/2017.
 */
public class PlayGroundBibCapReader {



    public static void main(String[] arg) {


        if(arg.length != 1) {  System.out.println("Supply name of MVstore DB"); System.exit(0); }

        File check = new File(arg[0]);

        if(!check.exists()) { System.out.println("File dosent exist"); System.exit(0); }


        MVStore store = new MVStore.Builder().cacheSize(200). // 200MB read cache
                fileName(arg[0]).autoCommitBufferSize(1024). // 1MB write cache
                open(); // autoCommitBufferSize
        store.setVersionsToKeep(0);
        store.setReuseSpace(true);

        MVMap<Integer, BibCapRecord> map = store.openMap("mymap", new MVMap.Builder<Integer,BibCapRecord>().keyType(new ObjectDataType()).valueType( new BibCapRecord() ));






        //running single thread rake..

        RAKE rake = new RAKE();


        String test = map.get(14241147).getAbstractText();

        System.out.println(test);


        //TODO fix outputs empty/null tokens
        rake.getTokens(test);


/*
        for(Map.Entry<Integer,BibCapRecord> entry : map.entrySet()) {


            BibCapRecord record = entry.getValue();

            String title = record.getTitle();

            String summary = record.getAbstractText();

            System.out.println("##########");
            System.out.println(record.internalId + " @@ " +record.getUT() );

            if(title != null) rake.getKeyWords(title);
            if(summary != null) rake.getKeyWords(summary);

        }


*/

        store.close();

    }










}
