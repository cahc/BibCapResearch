package Index;

import BibCap.BibCapCitedReferenceWithSearchKey;
import BibCap.BibCapRecord;
import Misc.LevenshteinDistance;
import Misc.OSA;
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



        /*

        BufferedWriter writerUniqe = new BufferedWriter( new FileWriter(new File("UniqueRefs.txt")));
        for(BibCapCitedReferenceWithSearchKey obj : referenceCounter.keySet()) {

         writerUniqe.write( obj.getCitedRefString() );
         writerUniqe.newLine();
        }

        writerUniqe.flush();
        writerUniqe.close();
        System.exit(0);

        */



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


        String[] sortedKeys = new String[uniqueKeys.size()];
        Iterator<String> iter = uniqueKeys.iterator();
        int index = 0;
        while(iter.hasNext()) {

            sortedKeys[index] = iter.next();
            index++;
        }

        uniqueKeys = null;
        System.gc();

        System.out.println("Sorted keys now in an array of length: " + sortedKeys.length);

        LinkedList[] arrayOfLinkedLists = new LinkedList[ sortedKeys.length ];

        for(int i=0; i<arrayOfLinkedLists.length; i++) arrayOfLinkedLists[i] = new LinkedList<BibCapCitedReferenceWithSearchKey>();

        System.out.println("now creating simple index structure..");


        for(BibCapCitedReferenceWithSearchKey bibCapCitedReferenceWithSearchKey : orderedLinkedListUnique) {


            for(String key : bibCapCitedReferenceWithSearchKey.getKeys() ) {

                int indexIntoArray = Arrays.binarySearch(sortedKeys,key);

                arrayOfLinkedLists[indexIntoArray].add( bibCapCitedReferenceWithSearchKey   );


            }

        }


        // LinkedList set of all Refenrence objects

        // keyes : an String[] array for length 2759610 containing the unique search Strings e.g.,  s[aa]200e[ws]

        //collectionOfRefObjects : an set{] array of same length as keyes, binarySearch(key) on keys gives index into this array given a set of matching collectionOfRefObjects


        System.out.println("Candidate generation");
        long start = System.currentTimeMillis();

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

            List<List<BibCapCitedReferenceWithSearchKey>> listOfListsWithCandidates = new ArrayList<>();


            for(String key : target.getKeys()) {

                int ind = Arrays.binarySearch(sortedKeys,key);

                listOfListsWithCandidates.add(  arrayOfLinkedLists[ind]    );

            }

            //check if they are above threshold

            Set<BibCapCitedReferenceWithSearchKey> uniqueMatches = new HashSet<>();

            for(int i=0; i<listOfListsWithCandidates.size(); i++) {



                // List<BibCapCitedReferenceWithSearchKey> matches = listsOfCandidares.get(i).stream().parallel().filter( object -> OptimalStringAlignment.editSimilarity( object.getCitedRefString(),target.getCitedRefString(),0.90 ) > -1  ).collect( Collectors.toList() );

                // List<BibCapCitedReferenceWithSearchKey> matches = listsOfCandidares.get(i).stream().parallel().filter( object -> LevenshteinDistance.isAboveSimilarityThreshold( object.getCitedRefString(),target.getCitedRefString(),0.90, false)   ).collect( Collectors.toList() );

                //Fastest by a large margin
                List<BibCapCitedReferenceWithSearchKey> matches = listOfListsWithCandidates.get(i).stream().parallel().filter( object -> OSA.DamuLevSim( object.getCitedRefString(),target.getCitedRefString(),0.90 ) > -1  ).collect( Collectors.toList() );

               //TODO BUGG HERE! REMOVE FROM INDEX STRUCTURE

                /* //////////////////OLD REMOVE FROM INDEX STRUCTURE////////////////////////////
              Iterator<BibCapCitedReferenceWithSearchKey> iterator = listOfListsWithCandidates.get(i).iterator();

               while(iterator.hasNext()) {

                   BibCapCitedReferenceWithSearchKey bibCapCitedReferenceWithSearchKey = iterator.next();

                   if( matches.contains( bibCapCitedReferenceWithSearchKey ) ) {   iterator.remove(); }

               }

               */



               uniqueMatches.addAll( matches );
            }



            ///////////////NEW REMOVE FROM INDEX STRUCTIRE///////////////////////////////


            for(BibCapCitedReferenceWithSearchKey bibCapCitedReferenceWithSearchKey : uniqueMatches ) {


                String[] keys = bibCapCitedReferenceWithSearchKey.getKeys();

                for(String key : keys)    {


                    int ind = Arrays.binarySearch(sortedKeys,key);

                    arrayOfLinkedLists[ind].remove( bibCapCitedReferenceWithSearchKey );

                }

            }








            ////////////////////////////////////////////////////////////////////////////////





            dummy++;
            if( dummy % 2000 == 0) {

                progressBar.update(alreadyRemoved.size(), N);
                System.out.println(target +" " + uniqueMatches.size() + " was the uniq size. Iterator index now: " +dummy);
            }


            alreadyRemoved.addAll(  uniqueMatches );
            orderedLinkedListUnique.removeFirst();


            for(BibCapCitedReferenceWithSearchKey refs : uniqueMatches) {


                writer.write(refs.getCitedRefString() +"\t" + integerCounter);
                writer.newLine();

            }

            integerCounter++;

            //if(dummy >= 1000000) break;

        }



        writer.flush();
        writer.close();

        System.out.println("That took: " + (System.currentTimeMillis() -start)/1000.0 ) ;
        System.exit(0);




    }
}
