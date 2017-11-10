package Index;

import BibCap.BibCapCitedReferenceWithSearchKey;
import BibCap.BibCapRecord;
import Misc.LevenshteinDistance;
import Misc.OptimalStringAlignment;
import Misc.ProgressBar;
import it.unimi.dsi.fastutil.objects.*;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.type.ObjectDataType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by crco0001 on 10/31/2017.
 */
public class GroupSimilarCitedReferences {


    public static void main(String[] arg) throws IOException {


        Object2IntOpenHashMap<BibCapCitedReferenceWithSearchKey> referenceCounter = new Object2IntOpenHashMap();
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


            List<BibCapCitedReferenceWithSearchKey> references = entry.getValue().getCitedReferences();

            if (references.size() == 0) continue;

            for (BibCapCitedReferenceWithSearchKey s : references) referenceCounter.addTo(s, 1);


        }

        System.out.println("Closing database");
        store.close();

        Object2IntMap.FastEntrySet<BibCapCitedReferenceWithSearchKey> entrySet = referenceCounter.object2IntEntrySet();


        List<Object2IntMap.Entry<BibCapCitedReferenceWithSearchKey>> list = new ArrayList<Object2IntMap.Entry<BibCapCitedReferenceWithSearchKey>>(entrySet);


        System.out.println("Sorting..");
        Collections.sort(list, new Comparator<Object2IntMap.Entry<BibCapCitedReferenceWithSearchKey>>() {
            @Override
            public int compare(Object2IntMap.Entry<BibCapCitedReferenceWithSearchKey> o1, Object2IntMap.Entry<BibCapCitedReferenceWithSearchKey> o2) {

                int val_o1 = o1.getIntValue();
                int val_o2 = o2.getIntValue();

                if (val_o1 < val_o2) return 1;
                if (val_o1 > val_o2) return -1;

                return 0;


            }
        });


        //sorted by insertion order
        ObjectLinkedOpenHashSet<BibCapCitedReferenceWithSearchKey> sortedSet = new ObjectLinkedOpenHashSet<>(list.size() + 1);

        for (Object2IntMap.Entry<BibCapCitedReferenceWithSearchKey> s : list) sortedSet.add(s.getKey() );

        //for GC
        map = null;
        list = null;
        System.gc();

        System.out.println("Unique references: " + sortedSet.size());

        /*
        System.out.println("max:");
        String targetRef = sortedSet.first();
        System.out.println(targetRef);


        List<String> matches = sortedSet.parallelStream().filter(otherRef -> LevenshteinDistance.isAboveSimilarityThreshold(otherRef, targetRef, 0.90, true)).collect(Collectors.toList());

        for (String s : matches) System.out.println(s);
*/

        System.out.println("Lets try to build an index..");

        Object2ObjectOpenHashMap<String,ObjectOpenHashSet<BibCapCitedReferenceWithSearchKey>> multimap = new Object2ObjectOpenHashMap<>();

        for(BibCapCitedReferenceWithSearchKey s : sortedSet) {


            for (String prefix : s.getKeys()) {

                ObjectOpenHashSet<BibCapCitedReferenceWithSearchKey> set = multimap.get(prefix);

                if (set != null) {

                    set.add(s);
                } else {

                    ObjectOpenHashSet<BibCapCitedReferenceWithSearchKey> newSet = new ObjectOpenHashSet<>();
                    newSet.add(s);
                    multimap.put(prefix, newSet);
                }


            }

        }



        System.out.println("Mappings in inverted index: " + multimap.size());

        ProgressBar progressBar = new ProgressBar();
        int N = sortedSet.size();
        int dummy = 0;
        progressBar.update(0,N);


        while( !sortedSet.isEmpty() ) {

            BibCapCitedReferenceWithSearchKey targetRef = sortedSet.first();

            Set<BibCapCitedReferenceWithSearchKey> candidates = new HashSet<>();

            for(String key : targetRef.getKeys()) {

                candidates.addAll(  multimap.get(key)  );
            }

           // List<BibCapCitedReferenceWithSearchKey> matches = candidates.parallelStream().filter(ref -> OptimalStringAlignment.editDistance(ref.getCitedRefString(), targetRef.getCitedRefString(),2) > -1    ).collect(Collectors.toList());

            List<BibCapCitedReferenceWithSearchKey> matches = candidates.parallelStream().filter(ref -> LevenshteinDistance.isAboveSimilarityThreshold(ref.getCitedRefString(), targetRef.getCitedRefString(),0.90,false)     ).collect(Collectors.toList());


            int trueMatches = matches.size();
           // System.out.println("above sim level: " + matches2.size());

            //remove matches from sortedSet
            sortedSet.removeAll( matches );

            //remove from multimap

            //TODO

            if(dummy % 300 == 0) {System.out.println("candidate set size was: " +candidates.size() +" and true matches was: " + trueMatches); for(BibCapCitedReferenceWithSearchKey s : matches) System.out.println(s); System.out.println(); } progressBar.update(N-sortedSet.size(),N);

        }


    }
}
