package Index;

import BibCap.BibCapCitedReferenceWithNgram;
import BibCap.BibCapRecord;
import Misc.LevenshteinDistance;
import Misc.Ngram;
import Misc.ProgressBar;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.*;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.type.ObjectDataType;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by crco0001 on 10/31/2017.
 */
public class GroupSimilarCitedReferences {


    public static void main(String[] arg) {


        Object2IntOpenHashMap<String> referenceCounter = new Object2IntOpenHashMap();
        referenceCounter.defaultReturnValue(0);

        if (arg.length != 1) {
            System.out.println("Supply name of MVstore DB");
            System.exit(0);
        }

        File check = new File(arg[0]);

        if (!check.exists()) {
            System.out.println("File dosent exist");
            System.exit(0);
        }

        MVStore store = new MVStore.Builder().cacheSize(200). // 200MB read cache
                fileName(arg[0]).autoCommitBufferSize(1024). // 1MB write cache
                open(); // autoCommitBufferSize
        store.setVersionsToKeep(0);
        store.setReuseSpace(true);

        MVMap<Integer, BibCapRecord> map = store.openMap("mymap", new MVMap.Builder<Integer, BibCapRecord>().keyType(new ObjectDataType()).valueType(new BibCapRecord()));


        System.out.println("Counting frequencies..");
        for (Map.Entry<Integer, BibCapRecord> entry : map.entrySet()) {


            List<String> references = entry.getValue().getCitedReferences();

            if (references.size() == 0) continue;

            for (String s : references) referenceCounter.addTo(s, 1);


        }

        System.out.println("Closing database");
        store.close();

        Object2IntMap.FastEntrySet<String> entrySet = referenceCounter.object2IntEntrySet();


        List<Object2IntMap.Entry<String>> list = new ArrayList<Object2IntMap.Entry<String>>(entrySet);


        System.out.println("Sorting..");
        Collections.sort(list, new Comparator<Object2IntMap.Entry<String>>() {
            @Override
            public int compare(Object2IntMap.Entry<String> o1, Object2IntMap.Entry<String> o2) {

                int val_o1 = o1.getIntValue();
                int val_o2 = o2.getIntValue();

                if (val_o1 < val_o2) return 1;
                if (val_o1 > val_o2) return -1;

                return 0;


            }
        });


        //sorted by insertion order
        ObjectLinkedOpenHashSet<String> sortedSet = new ObjectLinkedOpenHashSet<>(list.size() + 1);

        for (Object2IntMap.Entry<String> s : list) sortedSet.add(s.getKey());

        //for GC
        map = null;
        list = null;
        System.gc();

        System.out.println("Unique references: " + sortedSet.size());

        System.out.println("max:");
        String targetRef = sortedSet.first();
        System.out.println(targetRef);


        List<String> matches = sortedSet.parallelStream().filter(otherRef -> LevenshteinDistance.isAboveSimilarityThreshold(otherRef, targetRef, 0.90, true)).collect(Collectors.toList());

        for (String s : matches) System.out.println(s);


        System.out.println("Lets try to build an index..");

        Object2ObjectOpenHashMap<String,ObjectOpenHashSet<String>> multimap = new Object2ObjectOpenHashMap<>();

        for(String s : sortedSet) {

            List<String> ngrams = Ngram.normalizedBeforeNgram(5,s);

                for(String ngram : ngrams) {
                    ObjectOpenHashSet<String> set = multimap.get(ngram);

                    if(set != null) {

                        set.add(s);
                    } else {

                        ObjectOpenHashSet<String> newSet = new ObjectOpenHashSet<>();
                        newSet.add(s);
                        multimap.put(ngram,newSet);
                    }

                }

        }


        System.out.println("Mappings: " + multimap.size());

        List<String> searchKeys = Ngram.normalizedBeforeNgram(5,targetRef);
        Set<String> candidates = new HashSet<>();

        for(String s :searchKeys) candidates.addAll(  multimap.get(s) );

        System.out.println("Nr candidates: " + candidates.size());


        List<String> matches2 = candidates.parallelStream().filter( ref -> LevenshteinDistance.isAboveSimilarityThreshold(ref,targetRef,0.95,true)  ).collect(Collectors.toList());

        for (String s : matches2) System.out.println(s);


    }
}
