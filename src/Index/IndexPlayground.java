package Index;

import BibCap.BibCapRecord;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.type.ObjectDataType;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by crco0001 on 10/31/2017.
 */
public class IndexPlayground {


    public static void main(String[] arg) {


        Object2IntOpenHashMap<String> referenceCounter = new Object2IntOpenHashMap();
        referenceCounter.defaultReturnValue(0);



        if(arg.length != 1) {  System.out.println("Supply name of MVstore DB"); System.exit(0); }

        File check = new File(arg[0]);

        if(!check.exists()) { System.out.println("File dosent exist"); System.exit(0); }


        MVStore store = new MVStore.Builder().cacheSize(200). // 200MB read cache
                fileName(arg[0]).autoCommitBufferSize(1024). // 1MB write cache
                open(); // autoCommitBufferSize
        store.setVersionsToKeep(0);
        store.setReuseSpace(true);

        MVMap<Integer, BibCapRecord> map = store.openMap("mymap", new MVMap.Builder<Integer,BibCapRecord>().keyType(new ObjectDataType()).valueType( new BibCapRecord() ));



        for(Map.Entry<Integer,BibCapRecord> entry : map.entrySet()) {


            List<String> references = entry.getValue().getCitedReferences();

            if(references.size() == 0) continue;


            for(String s : references) referenceCounter.addTo(s,1);


        }


        System.out.println("Unique references: " + referenceCounter.size());



    }


}
