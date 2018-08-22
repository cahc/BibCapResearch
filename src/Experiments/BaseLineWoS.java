package Experiments;

import BibCap.BibCapRecord;
import BibCap.MockBibCapRecord;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.type.ObjectDataType;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by crco0001 on 3/13/2018.
 */
public class BaseLineWoS {


    static class FieldNorm {

        double citTimesFracSum = 0;
        double fracSum = 0;

        public FieldNorm() {}


        public double getCitTimesFracSum() {
            return citTimesFracSum;
        }

        public void addToCitTimesFracSum(double citTimesFracSum) {
            this.citTimesFracSum += citTimesFracSum;
        }

        public double getFracSum() {
            return fracSum;
        }

        public void addToFracSum(double fracSum) {
            this.fracSum += fracSum;
        }


        public double getFieldNorm() {

            return this.citTimesFracSum / this.fracSum;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FieldNorm fieldNorm = (FieldNorm) o;

            if (Double.compare(fieldNorm.citTimesFracSum, citTimesFracSum) != 0) return false;
            return Double.compare(fieldNorm.fracSum, fracSum) == 0;
        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            temp = Double.doubleToLongBits(citTimesFracSum);
            result = (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(fracSum);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }
    }


    public static float jaccardSim(MockBibCapRecord record1, MockBibCapRecord record2 ) {

        Set<String> record1_scs =   record1.getSubCats();
        int record1_size = record1_scs.size();

        Set<String> record2_scs =   record2.getSubCats();
        int record2_size = record2_scs.size();

        float intersection =0;
        if(record1_size < record2_size) {

            for( String cat : record1_scs) {  if( record2_scs.contains(cat) ) intersection++;  }


        } else {

            for( String cat : record2_scs) {  if( record1_scs.contains(cat) ) intersection++;  }
        }


        return (   intersection / (record1_size + record2_size - intersection)  );

    }


    public static float jaccardSim(Set<String> subcatsForRecord1, MockBibCapRecord record2 ) {

        Set<String> record1_scs =   subcatsForRecord1;
        int record1_size = record1_scs.size();

        Set<String> record2_scs =   record2.getSubCats();
        int record2_size = record2_scs.size();

        float intersection =0;
        if(record1_size < record2_size) {

            for( String cat : record1_scs) {  if( record2_scs.contains(cat) ) intersection++;  }


        } else {

            for( String cat : record2_scs) {  if( record1_scs.contains(cat) ) intersection++;  }
        }


        return (   intersection / (record1_size + record2_size - intersection)  );

    }


    public static void main(String[] arg) throws IOException {

        if(arg.length != 1) { System.out.println("Supply db"); System.exit(0); }

        MVStore store = new MVStore.Builder().cacheSize(200). // 200MB read cache
                fileName(arg[0]).autoCommitBufferSize(1024). // 1MB write cache
                open(); // autoCommitBufferSize
        store.setVersionsToKeep(0);
        store.setReuseSpace(true);

        MVMap<Integer, BibCapRecord> map = store.openMap("mymap", new MVMap.Builder<Integer, BibCapRecord>().keyType(new ObjectDataType()).valueType(new BibCapRecord()));


        Object2IntMap<String> catToFreq = new Object2IntOpenHashMap<>();
        Object2ObjectMap<String,FieldNorm> catToFieldNorm = new Object2ObjectOpenHashMap<>();

        //List<BibCapRecord> bibCapRecordList = new ArrayList<>();

        List<MockBibCapRecord> bibCapRecordList = new ArrayList<>();

        //refactor, read in internalIdForOrderedAccess.txt and use that order
        BufferedReader idreader = new BufferedReader( new FileReader( new File("internalIdForOrderedAccess.txt")));
        List<Integer> orderedIds = new ArrayList<>();
        String line2;
        while(  (line2 = idreader.readLine()) != null   ) {

            String[] parts = line2.split("\t");

            orderedIds.add( Integer.valueOf(parts[0]));
        }

        idreader.close();

        //mappy.db only contains records with at least one WC
       // for(Map.Entry<Integer, BibCapRecord> entry : map.entrySet()) {

        for(Integer id : orderedIds) {

            BibCapRecord fullRecord =  map.get(id); //entry.getValue();
            MockBibCapRecord reducedRecord = new MockBibCapRecord();



            reducedRecord.setUT(  fullRecord.getUT() );
            reducedRecord.setCitationsExclSelf( fullRecord.getCitationsExclSelf()  );
            reducedRecord.addSubCats(fullRecord.getSubjectCategories() );

            bibCapRecordList.add( reducedRecord );

        }

        store.close();

        System.out.println("Read the whole database into memory");

        for(MockBibCapRecord record : bibCapRecordList) {

            Set<String> categories = record.getSubCats();

            int TC_exlSelfCit = record.getCitationsExclSelf();
            int n_cat = categories.size();

            for(String s : categories) {


                int occurences = catToFreq.getOrDefault(s,-1);

                if(occurences == -1) { catToFreq.put(s, 1);  FieldNorm newFieldNorm = new FieldNorm(); newFieldNorm.addToCitTimesFracSum( TC_exlSelfCit* (1/((double)n_cat)) );  newFieldNorm.addToFracSum((1/((double)n_cat))); catToFieldNorm.put(s,newFieldNorm);  } else {

                    catToFreq.replace(s,occurences,occurences+1);

                    FieldNorm oldFieldNorm = catToFieldNorm.get(s);
                    oldFieldNorm.addToCitTimesFracSum(   TC_exlSelfCit* (1/((double)n_cat))  );
                    oldFieldNorm.addToFracSum( (1/((double)n_cat))  );

                }

            }

        }



        BufferedWriter writer1 = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(new File("WoS_Records_Per_Category.txt")), StandardCharsets.UTF_8) );

        System.out.println("calculating records per category (non frac)");

        for(Object2IntMap.Entry<String> set : catToFreq.object2IntEntrySet() ) {

         writer1.write( set.getKey() + "\t" + set.getIntValue() );
        writer1.newLine();

        }

        writer1.flush();
        writer1.close();

        System.out.println("calculating field norms based on WoS");

        BufferedWriter writer0 = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(new File("WoS_based_FieldValues_harmonicMean.txt")), StandardCharsets.UTF_8) );

        for(Map.Entry<String,FieldNorm> entry : catToFieldNorm.entrySet()) {


            writer0.write(entry.getKey() + "\t" + entry.getValue().getFieldNorm());
            writer0.newLine();


        }

        writer0.flush();
        writer0.close();

        System.out.println("calculating norms per doc with harmonic mean");

        BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(new File("WoS_based_refValues_harmonicMean.txt")), StandardCharsets.UTF_8) );

        for(MockBibCapRecord record : bibCapRecordList) {

            Set<String> categories = record.getSubCats();
            int TC_exlSelfCit = record.getCitationsExclSelf();
            int n_cat = categories.size();

            List<FieldNorm> fieldNormList = new ArrayList<>();
            List<Double> fieldNormValues = new ArrayList<>();

            for(String s : categories) fieldNormList.add(catToFieldNorm.get(s));
            for(FieldNorm fieldNorm : fieldNormList) fieldNormValues.add(fieldNorm.getFieldNorm());

                   double finalFieldNorm = 0;
                   boolean zeroValueFieldNorm = false;

                   for(Double fieldValue : fieldNormValues) {

                       if(fieldValue.equals(0D)) {

                           zeroValueFieldNorm = true;
                           break;

                       }

                     finalFieldNorm += (1/fieldValue);


                    }

                    if(zeroValueFieldNorm) { finalFieldNorm = -99; } else {

                        finalFieldNorm = n_cat/finalFieldNorm;
                    }


                 writer.write(record.getUT() +"\t" + TC_exlSelfCit +"\t" + finalFieldNorm + "\t" +categories.toString() );
                 writer.newLine();


            }

            writer.flush();
            writer.close();


            ////////////////////////////////////////////////////////////////////
            ///////////////////////Jacard based weights wij..//////////////////
            ///////////////////////////////////////////////////////////////////


            //Step 1: unique combos of subject categories

        HashSet<Set<String>> uniqueCombinationsOfSubjectCategories = new HashSet<>();
        for(int i=0; i<bibCapRecordList.size(); i++ ) {

            Set<String> subcats = bibCapRecordList.get(i).getSubCats();
            uniqueCombinationsOfSubjectCategories.add(subcats);


        }


        System.out.println("# unique combinations of subject categories: " + uniqueCombinationsOfSubjectCategories.size());






        //step 2: inverted index, subject category to document ids..
        System.out.println("building inverted index for single subcat to docIdList");
        Object2ObjectOpenHashMap<String,List<Integer>> singleSubCatToDocIndex = new Object2ObjectOpenHashMap<>();

        for(int i=0; i<bibCapRecordList.size(); i++ ) {

            Set<String> subcats = bibCapRecordList.get(i).getSubCats();

            for(String s : subcats) {

                List<Integer> docids = singleSubCatToDocIndex.get(s);

                if(docids == null) {

                    docids = new ArrayList<Integer>();
                    docids.add(i);
                    singleSubCatToDocIndex.put(s,docids);

                } else {

                    docids.add(i);


                }

            }

        }



        //Step  3: map each unique combo of subcats to potential target doc ids (i.e., all ids sharing at least one subcat)

        HashMap<Set<String>,IntOpenHashSet> subCatCombosToPotentialRefDocs = new HashMap<>();
        HashSet<String> test = new HashSet<>();
        int v=0;
        for(Set<String> combo : uniqueCombinationsOfSubjectCategories) {

            IntOpenHashSet potentialTargetsForEachCombo = new IntOpenHashSet();

            for(String s: combo) potentialTargetsForEachCombo.addAll( singleSubCatToDocIndex.get(s) );

            subCatCombosToPotentialRefDocs.put(combo,potentialTargetsForEachCombo);

            if(v==0) test.addAll(combo);
            v++;
        }

        System.out.println("map size: " + subCatCombosToPotentialRefDocs.size());
        System.out.println("first combo potential size: " + subCatCombosToPotentialRefDocs.get(test).size());





        //step 4 calculat referense values for each subcat combo based on jaccard weights..

        System.out.println("Starting jaccardbased refvalculations calculations..");

        HashMap<Set<String>, Double> combosToRefValues = new HashMap<>();
        for(Map.Entry<Set<String>,IntOpenHashSet> entry : subCatCombosToPotentialRefDocs.entrySet() ) {

            Set<String>  subcatCombo = entry.getKey();
            IntOpenHashSet targetDocs = entry.getValue();

            int[] citations = new int[targetDocs.size()];
            float[] weights = new float[targetDocs.size()];
            int j=0;
            for(int i : targetDocs) {

                MockBibCapRecord targetDoc = bibCapRecordList.get(i);
                int cit = targetDoc.getCitationsExclSelf();
                citations[j] = cit;
                float weight = jaccardSim(subcatCombo,targetDoc);
                weights[j] = weight;
                j++;
            }

            double sumweights = 0;
            for(int i=0; i<weights.length; i++) sumweights=weights[i]+sumweights;

            double wightedCitationsSum =0;

            for(int i=0; i<weights.length; i++) wightedCitationsSum = (weights[i]*citations[i])+wightedCitationsSum;

            combosToRefValues.put(subcatCombo,  (wightedCitationsSum/sumweights)  );
        }

        //  for(Map.Entry<Set<String>,Double> fieldValue : combosToRefValues.entrySet()) {


        //   System.out.println(fieldValue.getKey() +"\t" + fieldValue.getValue());
        //     }


        System.out.println("calculating norms per doc with jaccard");

        BufferedWriter writer2 = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(new File("WoS_based_refValues_jaccard.txt")), StandardCharsets.UTF_8) );

        for(MockBibCapRecord record : bibCapRecordList) {

           Double refvalue = combosToRefValues.get(record.getSubCats());

           writer2.write(record.getUT() +"\t" + record.getCitationsExclSelf() +"\t" +refvalue +"\t" + record.getSubCats());
           writer2.newLine();
        }

        writer2.flush();
        writer2.close();

        System.exit(0);

        for(int i=0; i<bibCapRecordList.size(); i++ ) {

            MockBibCapRecord targetDoc = bibCapRecordList.get(i);
            Set<String> subcats = targetDoc.getSubCats();
            HashSet<Integer> docIdsInReferenceSet = new HashSet<>();

            for(String s : subcats) docIdsInReferenceSet.addAll( singleSubCatToDocIndex.get(s) );
            docIdsInReferenceSet.remove(i);  //don't include yourself

            //now we know which docids that have a positive weight, but not we dont know which weight..

            for(Integer idInRefSet : docIdsInReferenceSet) {

                MockBibCapRecord referenceDoc = bibCapRecordList.get(idInRefSet);

                float sim = jaccardSim(targetDoc, referenceDoc );

                //debug
               // System.out.println(sim +" " +targetDoc.getSubCats() + "****" + referenceDoc.getSubCats());

            }

            //todo: all docs that share unique combos (some 12 thousand or so) of subcats must share the same set of potential refdocs
            //and we can pre-calculate the weights of these and also the reference value..


            //System.out.println("potentials: " + docIdsInReferenceSet.size()) ;

            if( (i % 5000) == 0) System.out.println(i);


        }



        /*
                    //pre calculate for all observed variations

        HashSet<List<String>> uniqueVariations = new HashSet<>();
        for(int i=0; i<bibCapRecordList.size(); i++ ) {

            List<String> subcats = bibCapRecordList.get(i).getSubjectCategories();
            uniqueVariations.add(subcats);


        }

        for(List<String> versions : uniqueVariations) {

         System.out.println(versions);

        }

        System.out.println("unique versions: " + uniqueVariations.size());


         */






        }




}
