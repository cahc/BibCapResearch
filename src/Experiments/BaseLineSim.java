package Experiments;

import BibCap.BibCapRecord;
import BibCap.MockBibCapRecord;
import SimilarityValues.ECDF;
import SimilarityValues.QuantileFun;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
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
import java.util.ArrayList;
import java.util.List;

public class BaseLineSim {
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


        @Override
        public String toString() {
            return "SimpleVector{" +
                    "indices=" + indices +
                    ", values=" + values +
                    '}';
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

        while(   true  ) {

            lineCitation = readerCitations.readLine();
            lineTerms = readerTerms.readLine();

            if(lineCitation == null || lineTerms == null) break;


            BaseLineSim.SimpleVector simpleVectorCitations = new BaseLineSim.SimpleVector();
            BaseLineSim.SimpleVector simpleVectorTerms = new BaseLineSim.SimpleVector();

            simpleVectorCitations.addSelfIndice(row);
            simpleVectorTerms.addSelfIndice(row);

            String[] linePartsCitations = lineCitation.trim().split(" ");
            String[] linePartsTerms = lineTerms.trim().split(" ");

            for(int i=0; i< linePartsCitations.length-1; i++) {


                int col = Integer.valueOf(linePartsCitations[i]);
                double value = Double.valueOf( linePartsCitations[i+1] );

                i++;

                simpleVectorCitations.addValueAndIndice(col-1,value); //CLUTO FILES ARE 1-BASED INDEXED!!

            }

            for(int i=0; i< linePartsTerms.length-1; i++) {


                int col = Integer.valueOf(linePartsTerms[i]);
                double value = Double.valueOf( linePartsTerms[i+1] );
                i++;

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

       List<List<SimpleVector>> simpleVectorList = new ArrayList<>();
       simpleVectorList.add(simpleVectorListCitations);
       simpleVectorList.add(simpleVectorListTerms);
       return simpleVectorList;

    }


        public static void main(String[] arg) throws IOException {

            if(arg.length != 3) { System.out.println("Supply db RefbasedSimMatrix.clu TermBasedSimMatrix.clu"); System.exit(0); }

        System.out.println("Working with log1p(x) of citations!");

        MVStore store = new MVStore.Builder().cacheSize(200). // 200MB read cache
                fileName(arg[0]).autoCommitBufferSize(1024). // 1MB write cache
                open(); // autoCommitBufferSize
        store.setVersionsToKeep(0);
        store.setReuseSpace(true);

        MVMap<Integer, BibCapRecord> map = store.openMap("mymap", new MVMap.Builder<Integer, BibCapRecord>().keyType(new ObjectDataType()).valueType(new BibCapRecord()));


        Object2IntMap<String> catToFreq = new Object2IntOpenHashMap<>();
        Object2ObjectMap<String,BaseLineWoS.FieldNorm> catToFieldNorm = new Object2ObjectOpenHashMap<>();

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
            //log!
            reducedRecord.setCitationExclSefLog1p(  Math.log1p(fullRecord.getCitationsExclSelf() )   );
            reducedRecord.addSubCats(fullRecord.getSubjectCategories() );

            bibCapRecordList.add( reducedRecord );

        }

        DoubleList citationDistribution = new DoubleArrayList();

        for(MockBibCapRecord record : bibCapRecordList) citationDistribution.add(  record.getCitationExclSefLog1p()  );

        store.close();

        System.out.println("Read the whole database into memory");


        List<List<SimpleVector>> clutofiles = readl2knngOrderedClustoFiles(arg[1],arg[2]);

        //OBSERVE THAT INDICES ARE NOW ZERO-BASED!!!!!!!!!!
        List<SimpleVector> citationBased = clutofiles.get(0);
        List<SimpleVector> termBased = clutofiles.get(1);

       // System.out.println("first vector from cit: " + citationBased.get(0));
       // System.out.println();
       // System.out.println("first vector from term: " + termBased.get(0));



       BufferedWriter writer = new BufferedWriter( new FileWriter( new File("ParameterSearch.txt")));

        //The order here correspond to the indices in List<Simplevector>

        double D= 0;
        double mean = 0;

        for(double cit : citationDistribution) mean=mean+cit;

        mean = mean/citationDistribution.size();

        for(double cit : citationDistribution) D = D+Math.pow(cit-mean,2);

        System.out.println("mean: " +mean);
        System.out.println("D: " + D);



        //todo loop here, over weight parameter

         DoubleList delta = new DoubleArrayList();
         delta.add(0.0);
         delta.add(0.05);
         delta.add(0.1);
         delta.add(0.15);
         delta.add(0.2);
         delta.add(0.25);
         delta.add(0.3);
         delta.add(0.35);
         delta.add(0.4);
         delta.add(0.45);
         delta.add(0.5);
         delta.add(0.55);
         delta.add(0.6);
         delta.add(0.65);
         delta.add(0.7);
         delta.add(0.75);
         delta.add(0.8);
         delta.add(0.85);
         delta.add(0.9);
         delta.add(0.95);
         delta.add(1.0);

         IntList neighbours = new IntArrayList();
         neighbours.add(10); neighbours.add(15); neighbours.add(20); neighbours.add(25); neighbours.add(30); neighbours.add(35); neighbours.add(40); neighbours.add(45); neighbours.add(50); neighbours.add(55); neighbours.add(60); neighbours.add(65); neighbours.add(70); neighbours.add(75); neighbours.add(80);
            neighbours.add(85); neighbours.add(90); neighbours.add(95); neighbours.add(100); neighbours.add(105);neighbours.add(110); neighbours.add(115); neighbours.add(120); neighbours.add(125); neighbours.add(130); neighbours.add(135); neighbours.add(140); neighbours.add(145); neighbours.add(150);

       for(int k : neighbours) {
           System.out.println("Using k=" + k);

           for (double deltaWeight : delta) {

               double Dx = 0;
               double oneMinusDelta = 1 - deltaWeight;

               int targetIndice = 0;
               for (MockBibCapRecord record : bibCapRecordList) {

                   double cit = record.getCitationExclSefLog1p();
                   SimpleVector targetVectorCitBased = citationBased.get(targetIndice);
                   SimpleVector targetVectorTermBased = termBased.get(targetIndice);

                   //get reference values:

                   DoubleList refCit = new DoubleArrayList();
                   DoubleList refCitWeights = new DoubleArrayList();

                   for (int i = 0; i < targetVectorCitBased.indices.size(); i++) {

                       refCit.add(citationDistribution.getDouble(targetVectorCitBased.indices.getInt(i)));
                       refCitWeights.add(targetVectorCitBased.values.getDouble(i));


                       if (i == k) break;
                   }

                   //normalize cit weight distribution

                   double normalizeWeightsRef = 0;

                   for (double weight : refCitWeights) normalizeWeightsRef = normalizeWeightsRef + weight;
                   normalizeWeightsRef = normalizeWeightsRef / refCitWeights.size();

                   for (int i = 0; i < refCitWeights.size(); i++) {

                       refCitWeights.set(i, refCitWeights.getDouble(i) / normalizeWeightsRef);
                   }


                   DoubleList refTerm = new DoubleArrayList();
                   DoubleList termWeights = new DoubleArrayList();

                   for (int i = 0; i < targetVectorTermBased.indices.size(); i++) {

                       refTerm.add(citationDistribution.getDouble(targetVectorTermBased.indices.getInt(i)));
                       termWeights.add(targetVectorTermBased.values.getDouble(i));

                       if (i == k) break;
                   }

                   //normalize term weights


                   double normalizeWeightsTerms = 0;

                   for (double weight : termWeights) normalizeWeightsTerms = normalizeWeightsTerms + weight;
                   normalizeWeightsTerms = normalizeWeightsTerms / termWeights.size();

                   for (int i = 0; i < termWeights.size(); i++) {

                       termWeights.set(i, termWeights.getDouble(i) / normalizeWeightsTerms);
                   }


                   //apply delta weighting..

                   for (int i = 0; i < refCitWeights.size(); i++) {

                       refCitWeights.set(i, refCitWeights.getDouble(i) * deltaWeight);
                   }

                   for (int i = 0; i < termWeights.size(); i++) {

                       termWeights.set(i, termWeights.getDouble(i) * oneMinusDelta);
                   }


                   double totalWeightedCitationSum = 0;
                   double totalWeighs = 0;

                   //create ref value, weight with delta

                   for (int i = 0; i < refCit.size(); i++) {

                       totalWeightedCitationSum = totalWeightedCitationSum + (refCit.getDouble(i) * refCitWeights.getDouble(i));
                       totalWeighs = totalWeighs + refCitWeights.getDouble(i);
                   }


                   for (int i = 0; i < refTerm.size(); i++) {

                       totalWeightedCitationSum = totalWeightedCitationSum + (refTerm.getDouble(i) * termWeights.getDouble(i));
                       totalWeighs = totalWeighs + termWeights.getDouble(i);
                   }

                   //FALLBACK, if one weight is 1, that it can be the case that we dont have any refvalue!

                   if (totalWeighs <= 0.0) {

                       Dx = Dx + Math.pow((cit - (mean)), 2); //just guess the mean of the dist

                   } else {

                       Dx = Dx + Math.pow((cit - (totalWeightedCitationSum / totalWeighs)), 2);
                   }


                   // writer.write(record.getUT() +"\t" + record.getCitationExclSefLog1p() +"\t" + (totalWeightedCitationSum/totalWeighs) );
                   // writer.newLine();


                   targetIndice++;
               }

               //System.out.println("Dx: " + Dx);

               System.out.println(deltaWeight +"\t" + k + "\t" + (D - Dx) / D );
                writer.write(deltaWeight +"\t" + k + "\t" + (D - Dx) / D );
               writer.newLine();


           }

       }

        writer.flush();
        writer.close();




        //explain 9.7%
        //explain 21.1%







    }

}
