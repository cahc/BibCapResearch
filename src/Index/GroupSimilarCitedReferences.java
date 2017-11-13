package Index;

import BibCap.BibCapCitedReferenceWithSearchKey;
import BibCap.BibCapRecord;
import Misc.OptimalStringAlignment;
import Misc.ProgressBar;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import edu.princeton.cs.algs4.TST;
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


import static com.google.common.collect.MultimapBuilder.*;

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


        //System.out.println("Counting frequencies..");


        System.out.println("Reading BibCapRefWithSearchKeys..");
        System.out.println("And counting occurrences..");


       // Set<BibCapCitedReferenceWithSearchKey> citedRefObjSet = new HashSet<>();

        for (Map.Entry<Integer, BibCapRecord> entry : map.entrySet()) {


            for (BibCapCitedReferenceWithSearchKey s : entry.getValue().getCitedReferences()) referenceCounter.addTo(s, 1);


        }

        System.out.println("Closing database");
        store.close();

        Object2IntMap.FastEntrySet<BibCapCitedReferenceWithSearchKey> entrySet = referenceCounter.object2IntEntrySet();

        List<Object2IntMap.Entry<BibCapCitedReferenceWithSearchKey>> list = new ArrayList<>( entrySet );

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

        referenceCounter = null;
        System.gc();


        LinkedList<BibCapCitedReferenceWithSearchKey> orderedLinkedListUnique = new LinkedList<>();

        for(Object2IntMap.Entry<BibCapCitedReferenceWithSearchKey> obj : list) {

            orderedLinkedListUnique.add(obj.getKey());
        }

        list = null;
        System.gc();


        System.out.println("Getting and sorting searchKeys");

        TreeSet<String> uniqueKeys = new TreeSet<>();

        for(BibCapCitedReferenceWithSearchKey bibCapCitedReferenceWithSearchKey : orderedLinkedListUnique) {

            for(String s : bibCapCitedReferenceWithSearchKey.getKeys()) uniqueKeys.add( s ) ;
        }


        String[] keys = new String[uniqueKeys.size()];
        Iterator<String> iter = uniqueKeys.iterator();
        int index = 0;
        while(iter.hasNext()) {

            keys[index] = iter.next();
            index++;
        }

        uniqueKeys = null;
        System.gc();

        System.out.println("Sorted keys now in an array of length: " + keys.length);

        LinkedList[] collectionOfRefObjects = new LinkedList[ keys.length ];

        for(int i=0; i<collectionOfRefObjects.length; i++) collectionOfRefObjects[i] = new LinkedList<BibCapCitedReferenceWithSearchKey>();

        System.out.println("now creating simple index structure..");


        for(BibCapCitedReferenceWithSearchKey bibCapCitedReferenceWithSearchKey : orderedLinkedListUnique) {


            for(String key : bibCapCitedReferenceWithSearchKey.getKeys() ) {

                int indexIntoArray = Arrays.binarySearch(keys,key);

                collectionOfRefObjects[indexIntoArray].add( bibCapCitedReferenceWithSearchKey   );


            }

        }


        // LinkedList set of all Refenrence objects

        // keyes : an String[] array for length 2759610 containing the unique search Strings e.g.,  s[aa]200e[ws]

        //collectionOfRefObjects : an set{] array of same length as keyes, binarySearch(key) on keys gives index into this array given a set of matching collectionOfRefObjects


        System.out.println("Candidate generation");


        ProgressBar progressBar = new ProgressBar();
        int N = orderedLinkedListUnique.size();
        int dummy = 0;
        progressBar.update(0,N);


        HashSet<BibCapCitedReferenceWithSearchKey> alreadyRemoved = new HashSet<>();


        BufferedWriter writer = new BufferedWriter( new FileWriter( new File("referencesToIntegers.txt") ));
        int integerCounter = 0;

        while(!orderedLinkedListUnique.isEmpty() ) {


            BibCapCitedReferenceWithSearchKey target = orderedLinkedListUnique.getFirst();

            if(alreadyRemoved.contains(target)) {

                orderedLinkedListUnique.removeFirst();
                dummy++;
                continue;
            }

            List<List<BibCapCitedReferenceWithSearchKey>> listsOfCandidares = new ArrayList<>();


            for(String key : target.getKeys()) {

                int ind = Arrays.binarySearch(keys,key);

                listsOfCandidares.add(  collectionOfRefObjects[ind]    );

            }

            //check if they are above threshold

            Set<BibCapCitedReferenceWithSearchKey> uniqueMatches = new HashSet<>();

            for(int i=0; i<listsOfCandidares.size(); i++) {


               List<BibCapCitedReferenceWithSearchKey> matches = listsOfCandidares.get(i).stream().parallel().filter( object -> OptimalStringAlignment.editDistance( object.getCitedRefString(),target.getCitedRefString(),2 ) > -1  ).collect( Collectors.toList() );

               //REMOVE FROM INDEX STRUCTURE
              Iterator<BibCapCitedReferenceWithSearchKey> iterator = listsOfCandidares.get(i).iterator();

               while(iterator.hasNext()) {

                   BibCapCitedReferenceWithSearchKey bibCapCitedReferenceWithSearchKey = iterator.next();

                   if( matches.contains( bibCapCitedReferenceWithSearchKey ) ) iterator.remove();

               }


               uniqueMatches.addAll( matches );
            }


            //REMOVE FROM citedRefObjSet
        //    linkedListUnique.removeIf( obj -> uniqueMatches.contains(obj) );


            dummy++;
            if( dummy % 1000 == 0) {

                progressBar.update(alreadyRemoved.size(), N);
                System.out.println(target +" " + uniqueMatches.size() + " was the uniq size. Iterator index now: " +dummy);
            }


            alreadyRemoved.addAll(  uniqueMatches );
            orderedLinkedListUnique.removeFirst();



            for(BibCapCitedReferenceWithSearchKey refs : uniqueMatches) {


                writer.write(refs.getCitedRefString() +"\t" + integerCounter);

            }

            integerCounter++;

        }



        writer.flush();
        writer.close();
        
        System.exit(0);

/*

        System.out.println("Creating multimap");


       SetMultimap<String,BibCapCitedReferenceWithSearchKey> multimap  = MultimapBuilder.hashKeys().hashSetValues().build();

       for(BibCapCitedReferenceWithSearchKey bibCapCitedReferenceWithSearchKey : citedRefObjSet ) {


           for(String s : bibCapCitedReferenceWithSearchKey.getKeys() ) multimap.put(s,bibCapCitedReferenceWithSearchKey);


       }

        System.out.println("unique mappings: " + multimap.asMap().size());

        System.out.println("non unique mappings: " + multimap.size());



        ProgressBar progressBar2 = new ProgressBar();
        int N = citedRefObjSet.size();
        int dummy = 0;
        progressBar2.update(0,N);

       Iterator<BibCapCitedReferenceWithSearchKey> iterator = citedRefObjSet.iterator();


       while (!citedRefObjSet.isEmpty()) {

           BibCapCitedReferenceWithSearchKey target = citedRefObjSet.iterator().next();

           ArrayList<BibCapCitedReferenceWithSearchKey> candidates = new ArrayList<>(200);

           //TODO this is what is taking time..
          for(String s : target.getKeys() ) candidates.addAll(   multimap.get( s )    );


         Set<BibCapCitedReferenceWithSearchKey> matchedObject = candidates.parallelStream().filter( obj -> OptimalStringAlignment.editDistance(obj.getCitedRefString(), target.getCitedRefString(),2) > -1 ).collect(Collectors.toSet());

           int size = candidates.size();

           //remove objects in fake from set
           citedRefObjSet.removeIf( obj -> matchedObject.contains(obj));

           //TODO remove object from fale in multimap to

           for(BibCapCitedReferenceWithSearchKey matchedRefObj : matchedObject) {


               for(String key : matchedRefObj.getKeys()) multimap.remove(key,matchedRefObj);

           }


          // iterator.remove();

          dummy++;
          if( dummy % 200 == 0) {

              progressBar2.update(N-citedRefObjSet.size(),N);


          }
       }




  */
        /*
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
*/

       // System.out.println("Unique references: " + sortedSet.size());

        /*
        System.out.println("max:");
        String targetRef = sortedSet.first();
        System.out.println(targetRef);


        List<String> matches = sortedSet.parallelStream().filter(otherRef -> LevenshteinDistance.isAboveSimilarityThreshold(otherRef, targetRef, 0.90, true)).collect(Collectors.toList());

        for (String s : matches) System.out.println(s);
*/




        /*
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


            if(dummy % 300 == 0) {System.out.println("candidate set size was: " +candidates.size() +" and true matches was: " + trueMatches); for(BibCapCitedReferenceWithSearchKey s : matches) System.out.println(s); System.out.println(); } progressBar.update(N-sortedSet.size(),N);



        }
*/


    }
}
