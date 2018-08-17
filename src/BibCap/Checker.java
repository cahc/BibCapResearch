package BibCap;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.type.ObjectDataType;

import java.util.Map;

/**
 * Created by crco0001 on 6/8/2018.
 */
public class Checker {



    public static void main(String[] arg) {

        MVStore store = new MVStore.Builder().cacheSize(200). // 200MB read cache
                fileName("mappy.db").autoCommitBufferSize(1024). // 1MB write cache
                open(); // autoCommitBufferSize
        store.setVersionsToKeep(0);
        store.setReuseSpace(true);

        MVMap<Integer, BibCapRecord> map = store.openMap("mymap", new MVMap.Builder<Integer, BibCapRecord>().keyType(new ObjectDataType()).valueType(new BibCapRecord()));

        BibCapRecord record = map.get(8827875);


        System.out.println(record.title  );

        BibCapRecord record2 = map.get(65838169);

        System.out.println(record2.title );


        int i=0;
        for (Map.Entry<Integer, BibCapRecord> entry : map.entrySet()) {

            BibCapRecord record1 = entry.getValue();

            if(i==0) System.out.println("0: " + record1.getTitle() + " " +record1.getInternalId() );

            if(i==284) System.out.println("284: " + record1.getTitle() +" " +record1.getInternalId());

            if(i == 850629) System.out.println("850629: " + record1.getTitle() + " " +record1.getInternalId() );

            i++;
        }


    }

}
