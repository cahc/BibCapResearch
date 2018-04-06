package Experiments;

import BibCap.BibCapRecord;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        List<BibCapRecord> bibCapRecordList = new ArrayList<>();


        //mappy.db only contains records with at least one WC
        for(Map.Entry<Integer, BibCapRecord> entry : map.entrySet()) {

            BibCapRecord fullRecord = entry.getValue();
            BibCapRecord reducedRecord = new BibCapRecord();



            reducedRecord.setUT(  fullRecord.getUT() );
            reducedRecord.setCitationsExclSelf( fullRecord.getCitationsExclSelf()  );
            reducedRecord.setSubjectCategories(fullRecord.getSubjectCategories() ) ;

            bibCapRecordList.add( reducedRecord );

        }

        store.close();

        System.out.println("Read the whole database into memory");

        for(BibCapRecord record : bibCapRecordList) {

            List<String> categories = record.getSubjectCategories();
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

        System.out.println("calculating records per ctegory (non frac)");

        for(Object2IntMap.Entry<String> set : catToFreq.object2IntEntrySet() ) {

         writer1.write( set.getKey() + "\t" + set.getIntValue() );
        writer1.newLine();

        }

        writer1.flush();
        writer1.close();

        System.out.println("calculating field norms based on WoS");

        BufferedWriter writer0 = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(new File("WoS_based_FieldValues.txt")), StandardCharsets.UTF_8) );

        for(Map.Entry<String,FieldNorm> entry : catToFieldNorm.entrySet()) {


            writer0.write(entry.getKey() + "\t" + entry.getValue().getFieldNorm());
            writer0.newLine();


        }

        writer0.flush();
        writer0.close();

        System.out.println("calculating norms per doc with harmonic mean");

        BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(new File("WoS_based_refValues.txt")), StandardCharsets.UTF_8) );

        for(BibCapRecord record : bibCapRecordList) {

            List<String> categories = record.getSubjectCategories();
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



            //step one partition the records based on the subject categories combination profiles

            Object2ObjectOpenHashMap<List<String>,IntList> uniqeCombosOfSubCatToIndices = new Object2ObjectOpenHashMap<>();

            for(int i=0; i<bibCapRecordList.size(); i++ ) {

                List<String> key =  bibCapRecordList.get(i).getSubjectCategories();

                IntList intList = uniqeCombosOfSubCatToIndices.get( key );

                if(intList == null) {

                    intList = new IntArrayList();
                    intList.add(i);
                    uniqeCombosOfSubCatToIndices.put(key,intList);

                } else {

                    intList.add(i);
                }


            }


            //step two create a inverted index that maps from one (1) subject category to an list of indices of records that har in the category

            Object2ObjectOpenHashMap<String,IntList> subjectCategoryToIndices = new Object2ObjectOpenHashMap<>();





        }




}
