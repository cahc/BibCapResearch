package Index;

import BibCap.BibCapCitedReferenceWithNgram;
import BibCap.BibCapRecord;
import Misc.LevenshteinDistance;
import Misc.ProgressBar;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
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


        Object2IntOpenHashMap<BibCapCitedReferenceWithNgram> referenceCounter = new Object2IntOpenHashMap();
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


        System.out.println("Counting frequencies..");
        for(Map.Entry<Integer,BibCapRecord> entry : map.entrySet()) {


            List<BibCapCitedReferenceWithNgram> references = entry.getValue().getCitedReferencesWithNgram();

            if(references.size() == 0) continue;

            for(BibCapCitedReferenceWithNgram s : references) referenceCounter.addTo(s,1);


        }

        System.out.println("Closing database");
        store.close();

        Object2IntMap.FastEntrySet<BibCapCitedReferenceWithNgram> entrySet = referenceCounter.object2IntEntrySet();


        List<Object2IntMap.Entry<BibCapCitedReferenceWithNgram>> list = new ArrayList<Object2IntMap.Entry<BibCapCitedReferenceWithNgram>>(entrySet);


        System.out.println("Sorting..");
        Collections.sort(list, new Comparator<Object2IntMap.Entry<BibCapCitedReferenceWithNgram>>() {
            @Override
            public int compare(Object2IntMap.Entry<BibCapCitedReferenceWithNgram> o1, Object2IntMap.Entry<BibCapCitedReferenceWithNgram> o2) {

                int val_o1 = o1.getIntValue();
                int val_o2 = o2.getIntValue();

                if(val_o1 < val_o2) return 1;
                if(val_o1 > val_o2) return -1;

                return 0;


            }
        });


        //sorted by insertion order
        ObjectLinkedOpenHashSet<BibCapCitedReferenceWithNgram> sortedSet = new ObjectLinkedOpenHashSet<>();

        for(Object2IntMap.Entry<BibCapCitedReferenceWithNgram> s: list) sortedSet.add( s.getKey() );

        //for GC
        map = null;
        list = null;

        //System.out.println("Unique references: " + referenceCounter.size());

        //System.out.println("max:");
        //System.out.println(list.get(0).getKey() + " -->" + list.get(0).getIntValue());

        //System.out.println("min:");
        //System.out.println(list.get( list.size()-1 ).getKey() + " -->" + list.get( list.size()-1 ).getIntValue());


            String targetRef = sortedSet.first().getReference();

            List<BibCapCitedReferenceWithNgram> matches = sortedSet.parallelStream().filter(otherRef -> LevenshteinDistance.isAboveSimilarityThreshold(otherRef.getReference(), targetRef, 0.90, true)).collect(Collectors.toList());

            for(BibCapCitedReferenceWithNgram s : matches) System.out.println(s);

        }


    }
