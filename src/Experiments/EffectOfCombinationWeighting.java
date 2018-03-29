package Experiments;

import BibCap.BibCapRecord;
import SimilarityValues.ECDF;
import SimilarityValues.QuantileFun;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.type.ObjectDataType;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.Math.pow;

/**
 * Created by crco0001 on 3/6/2018.
 */
public class EffectOfCombinationWeighting {

   public static class SimpleVector {

        int indice;
        IntList indices = new IntArrayList();
        DoubleList values = new DoubleArrayList();

        SimpleVector() {
        }


        public void addValueAndIndice(int indice, double value) {

            indices.add(indice);
            values.add(value);

        }

        public double getValueAtPosition(int position) {

            return values.getDouble(position);
        }


        public int size() {

            return indices.size();
        }

        public void addSelfIndice(int indice) {


            this.indice = indice;

        }


        public void setValueAtPosition(int indice, double value) {

            this.values.set(indice,value);

        }


        public Int2DoubleOpenHashMap combineWithOtherVector(SimpleVector other, double weighOther) {

            if(weighOther > 1 || weighOther < 0) throw new NumberFormatException("weight must be less than 1 but larger than 0");

            Int2DoubleOpenHashMap vec = new Int2DoubleOpenHashMap();

            for(int i=0; i<this.values.size(); i++) vec.addTo(this.indices.getInt(i), ( this.values.getDouble(i) * (1-weighOther) ) ) ;

            for(int i=0; i<other.values.size(); i++) vec.addTo(other.indices.getInt(i), (other.values.getDouble(i) * (weighOther) ) ) ;


            return vec;
        }

    }

    public static List<List<SimpleVector>> readl2knngOrderedClustoFiles(String clutoFileCitation, String clutoFileTerms) throws IOException {





        File fileCitations = new File(clutoFileCitation);

        File fileTerms = new File(clutoFileTerms);

        if(!fileCitations.exists()) { System.out.println("Missing cluto file (citations)"); System.exit(0); }


        if(!fileTerms.exists()) { System.out.println("Missing cluto file (terms)"); System.exit(0); }


        BufferedReader readerCitations = new BufferedReader( new FileReader( fileCitations ));
        BufferedReader readerTerms = new BufferedReader( new FileReader( fileTerms ));

        String[] headerCitations = readerCitations.readLine().trim().split(" ");
        String[] headerTerms = readerTerms.readLine().trim().split(" ");

        if(headerCitations.length != 3) { System.out.println("Wrong header in cluto file.."); System.exit(0); }
        if(headerTerms.length != 3) { System.out.println("Wrong header in cluto file.."); System.exit(0); }

        int rowsC = Integer.valueOf( headerCitations[0] );
        int colsC = Integer.valueOf( headerCitations[1] );
        int nnzC = Integer.valueOf( headerCitations[2] );
        // e.g., 955491 955491 43379457

        System.out.println("Metadata from cluto (citations) file:");
        System.out.println("nnz: " +nnzC);

        int rowsT = Integer.valueOf( headerTerms[0] );
        int colsT = Integer.valueOf( headerTerms[1] );
        int nnzT = Integer.valueOf( headerTerms[2] );
        // e.g., 955491 955491 43379457

        System.out.println("Metadata from cluto (terms) file:");
        System.out.println("nnz: " +nnzT);

        if(rowsC != rowsT || colsC != colsT || rowsC != colsT || rowsT != colsC) {

            System.out.println("dimension mishmatch!"); System.exit(0);

        }

        System.out.println("rows/columns: "  + colsT);

        String lineCitation;
        String lineTerms;

        int row = 0;  //CLUTO FILES ARE 1-BASED INDEXED!!
        List<SimpleVector> simpleVectorListCitations = new ArrayList<>();
        List<SimpleVector> simpleVectorListTerms = new ArrayList<>();

        DoubleList simValCitations = new DoubleArrayList(1000);
        DoubleList simValTerms = new DoubleArrayList(1000);

        while(   true  ) {

            lineCitation = readerCitations.readLine();
            lineTerms = readerTerms.readLine();

            if(lineCitation == null || lineTerms == null) break;


            SimpleVector simpleVectorCitations = new SimpleVector();
            SimpleVector simpleVectorTerms = new SimpleVector();

            simpleVectorCitations.addSelfIndice(row);
            simpleVectorTerms.addSelfIndice(row);

            String[] linePartsCitations = lineCitation.trim().split(" ");
            String[] linePartsTerms = lineTerms.trim().split(" ");

            for(int i=0; i< linePartsCitations.length-1; i++) {


                int col = Integer.valueOf(linePartsCitations[i]);
                double value = Double.valueOf( linePartsCitations[i+1] );
                simValCitations.add(value);
                i++;

               simpleVectorCitations.addValueAndIndice(col-1,value); //CLUTO FILES ARE 1-BASED INDEXED!!

            }

            for(int i=0; i< linePartsTerms.length-1; i++) {


                int col = Integer.valueOf(linePartsTerms[i]);
                double value = Double.valueOf( linePartsTerms[i+1] );
                i++;
                simValTerms.add(value);
                simpleVectorTerms.addValueAndIndice(col-1,value); //CLUTO FILES ARE 1-BASED INDEXED!!

            }

            row++;

            simpleVectorListCitations.add(simpleVectorCitations);
            simpleVectorListTerms.add(simpleVectorTerms);
        }


        System.out.println("SimpleVectorList (citations) #: " + simpleVectorListCitations.size());
        System.out.println("SimpleVectorList (terms) #: " + simpleVectorListTerms.size());

        readerCitations.close();
        readerTerms.close();

        System.out.println("Creating quantile and cumulative distribution function for normalization..");

        QuantileFun quantileFuncTerms = new QuantileFun(simValTerms);

        ECDF ecdfCitations = new ECDF(simValCitations);

        System.out.println("Normalizing similarity values derived from citations with respect to terms");

        for(SimpleVector citationBasedSim : simpleVectorListCitations) {

            int n = citationBasedSim.size();

                for(int i=0; i<n; i++) {

                    double prob = ecdfCitations.getProbBinarySearch( citationBasedSim.getValueAtPosition(i) );
                    double normedVal = quantileFuncTerms.getQuantile( prob );

                    citationBasedSim.setValueAtPosition(i,normedVal);


                }



        }

        System.out.println("Mean similarly terms: " + simValTerms.stream().mapToDouble(x -> x.doubleValue()).average().getAsDouble() );
        System.out.println("BEFORE NORMALIZATION:");
        System.out.println("Mean similarly citation: " + simValCitations.stream().mapToDouble(x -> x.doubleValue()).average().getAsDouble() );
        System.out.println("AFTER NORMALIZATION:");

        DoubleList normedCitbasedSimVals =  new DoubleArrayList();
        for(SimpleVector simpleVector : simpleVectorListCitations) {

            normedCitbasedSimVals.addAll(simpleVector.values);
        }

        System.out.println("Mean similarly citation: " + normedCitbasedSimVals.stream().mapToDouble(x -> x.doubleValue()).average().getAsDouble() );
        System.out.println();
        System.out.println();

        ArrayList<List<SimpleVector>> results = new ArrayList<>();


        results.add(simpleVectorListCitations);
        results.add(simpleVectorListTerms);

        return results;

    }


    /*

     read in cit per doc

     read in similarity matrix per feature set

       loop through, k and gamma (weighting for

        k     gamma

        10    0.1, 0.2, 0.3, 0.4, 0.5, 0.6-...
        20
        30
        ..
        ..

     */


    public static void main(String[] arg) throws IOException {


        if(arg.length != 1) { System.out.println("Supply db"); System.exit(0); }

        MVStore store = new MVStore.Builder().cacheSize(200). // 200MB read cache
                fileName(arg[0]).autoCommitBufferSize(1024). // 1MB write cache
                open(); // autoCommitBufferSize
                store.setVersionsToKeep(0);
                store.setReuseSpace(true);

        MVMap<Integer, BibCapRecord> map = store.openMap("mymap", new MVMap.Builder<Integer, BibCapRecord>().keyType(new ObjectDataType()).valueType(new BibCapRecord()));

        IntList TC_exlSelfCit = new IntArrayList();
        IntList TC_incSelfCit = new IntArrayList();

        for(Map.Entry<Integer, BibCapRecord> entry : map.entrySet()) {

            int TC1 = entry.getValue().getCitationsExclSelf();
            int TC2 = entry.getValue().getCitationsIncSelf();

            TC_exlSelfCit.add(TC1);
            TC_incSelfCit.add(TC2);


        }

        System.out.println("Citation data for "+ TC_exlSelfCit.size() +" read");
        store.close();

        List<List<SimpleVector>> vectorSets = readl2knngOrderedClustoFiles("SimilarityMatrices\\simk80cit.clu","SimilarityMatrices\\simk80term.clu");

        List<SimpleVector> simpleVectorsCitations = vectorSets.get(0);

        List<SimpleVector> simpleVectorsTerms = vectorSets.get(1);


      //  BufferedWriter writer = new BufferedWriter( new FileWriter( new File("TC_AND_REF_VALS.TXT")));


        double unconditional_mean = TC_incSelfCit.stream().mapToInt(x -> x).average().getAsDouble();
        double unconditional_variation = 0;
        double conditional_variation = 0;

        for(int i=0; i<TC_incSelfCit.size(); i++) {

            unconditional_variation = unconditional_variation +  Math.pow(TC_incSelfCit.getInt(i) - unconditional_mean,2);

        }


        for(int i=0; i<simpleVectorsCitations.size(); i++) {

            Int2DoubleOpenHashMap combinedVector = simpleVectorsCitations.get(i).combineWithOtherVector(simpleVectorsTerms.get(i), 0.01);

            double refValue = 0;
            double denominator = 0;

            if(combinedVector.int2DoubleEntrySet().size() == 0) System.out.println("WARNING!!: combinedVector of length 0 for vecor with indece: " + i);

            for(Int2DoubleMap.Entry entrySet : combinedVector.int2DoubleEntrySet() ) {

                int indice = entrySet.getIntKey();
                double simValue = entrySet.getDoubleValue();

                int otherCitation = TC_incSelfCit.getInt(indice);

                denominator = denominator + simValue;
                refValue = refValue + (otherCitation*simValue);


            }


            //calculate conditional variation
            double valueForConditionalNormalization = (denominator == 0 ) ? TC_incSelfCit.getInt(i) : (refValue/denominator); // temp fix for articles with no comparison groups

            conditional_variation = conditional_variation + Math.pow(TC_incSelfCit.getInt(i)-valueForConditionalNormalization,2);



          //  writer.write(TC_incSelfCit.getInt(i) +"\t" + valueForNormalization);
        //    writer.newLine();


        }

        System.out.printf("indicator: %.2f", + ((unconditional_variation-conditional_variation)/unconditional_variation) * 100 );


         //   writer.flush();
        //    writer.close();

    }



}
