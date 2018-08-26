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

            if(arg.length != 1) { System.out.println("Supply db"); System.exit(0); }

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


        List<List<SimpleVector>> clutofiles = readl2knngOrderedClustoFiles("secondOrderSimCitedRefsK150.clu","secondOrderSimTermsK150.clu");

        //OBSERVE THAT INDICES ARE NOW ZERO-BASED!!!!!!!!!!
        List<SimpleVector> citationBased = clutofiles.get(0);
        List<SimpleVector> termBased = clutofiles.get(1);

       // System.out.println("first vector from cit: " + citationBased.get(0));
       // System.out.println();
       // System.out.println("first vector from term: " + termBased.get(0));


        int maxK=50;

       // BufferedWriter writer = new BufferedWriter( new FileWriter( new File("SimBasedRefVals.txt")));

        //The order here correspond to the indices in List<Simplevector>

        double D= 0;
        double Dx = 0;

        double mean = 0;

        for(double cit : citationDistribution) mean=mean+cit;

        mean = mean/citationDistribution.size();

        for(double cit : citationDistribution) D = D+Math.pow(cit-mean,2);

        System.out.println("mean: " +mean);
        System.out.println("D: " + D);


        int targetIndice=0;
        for(MockBibCapRecord record : bibCapRecordList) {

           double cit = record.getCitationExclSefLog1p();
           SimpleVector targetVectorCitBased = citationBased.get(targetIndice);
           SimpleVector targetVectorTermBased = termBased.get(targetIndice);

           //get reference values:

           DoubleList refCit = new DoubleArrayList();
           DoubleList refCitWeights = new DoubleArrayList();

           for(int i=0; i<targetVectorCitBased.indices.size(); i++) {

               refCit.add(  citationDistribution.getDouble( targetVectorCitBased.indices.getInt(i)  )  ) ;
               refCitWeights.add(  targetVectorCitBased.values.getDouble(i)   );

               if(i==maxK) break;
            }

            DoubleList refTerm = new DoubleArrayList();
            DoubleList termWeights = new DoubleArrayList();

            for(int i=0; i<targetVectorTermBased.indices.size(); i++) {

                refTerm.add(  citationDistribution.getDouble( targetVectorTermBased.indices.getInt(i) ) );
                termWeights.add(  targetVectorTermBased.values.getDouble(i)   );

                if(i==maxK) break;
            }


            double totalWeightedCitationSum = 0;
            double totalWeighs = 0;


            for(int i=0; i<refCit.size(); i++) {

                totalWeightedCitationSum = totalWeightedCitationSum+ (refCit.getDouble(i)*refCitWeights.getDouble(i) );
                totalWeighs = totalWeighs+refCitWeights.getDouble(i);
            }


            for(int i=0; i<refTerm.size(); i++) {

                totalWeightedCitationSum = totalWeightedCitationSum+ (refTerm.getDouble(i)*termWeights.getDouble(i) );
                totalWeighs = totalWeighs + termWeights.getDouble(i);
            }


            Dx = Dx + Math.pow( (cit- (totalWeightedCitationSum/totalWeighs)  )  ,2);

           // writer.write(record.getUT() +"\t" + record.getCitationExclSefLog1p() +"\t" + (totalWeightedCitationSum/totalWeighs) );
           // writer.newLine();


            targetIndice++;
        }

        System.out.println("Dx: " + Dx);
        System.out.println("indicator: " + (D - Dx)/D );

       // writer.flush();
       // writer.close();




        //explain 9.7%
        //explain 21.1%







    }

}
