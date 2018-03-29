package Experiments;

import BibCap.BibCapRecord;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.type.ObjectDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by crco0001 on 3/13/2018.
 */
public class BaseLineWoS {


    public static void main(String[] arg) {


        if(arg.length != 1) { System.out.println("Supply db"); System.exit(0); }

        MVStore store = new MVStore.Builder().cacheSize(200). // 200MB read cache
                fileName(arg[0]).autoCommitBufferSize(1024). // 1MB write cache
                open(); // autoCommitBufferSize
        store.setVersionsToKeep(0);
        store.setReuseSpace(true);

        MVMap<Integer, BibCapRecord> map = store.openMap("mymap", new MVMap.Builder<Integer, BibCapRecord>().keyType(new ObjectDataType()).valueType(new BibCapRecord()));

        IntList TC_exlSelfCit = new IntArrayList();
        IntList TC_incSelfCit = new IntArrayList();

        Object2IntMap<String> catToFreq = new Object2IntOpenHashMap<>();


        for(Map.Entry<Integer, BibCapRecord> entry : map.entrySet()) {


           List<String> categories = entry.getValue().getSubjectCategories();

           for(String s : categories) {


               int occurences = catToFreq.getOrDefault(s,-1);
               if(occurences == -1) { catToFreq.put(s, 1); } else {

                   catToFreq.replace(s,occurences,occurences+1);

               }


           }



        }


        System.out.println("# keys: " + catToFreq.size());

        for(Object2IntMap.Entry<String> set : catToFreq.object2IntEntrySet() ) {

            System.out.println( set.getKey() + " : " + set.getIntValue() );


        }


    }


}
