package Experiments;

import BibCap.BibCapRecord;
import BibCap.MockBibCapRecord;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.type.ObjectDataType;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BaseLineClusters {


    public static double removeValueFromMean(double originalMean, double removeValue, int N) {

        return( ((originalMean*N)-removeValue)/(N-1) );

    }




    public static void main(String[] arg) throws IOException {


        MVStore store = new MVStore.Builder().cacheSize(200). // 200MB read cache
                fileName("mappy.db").autoCommitBufferSize(1024). // 1MB write cache
                open(); // autoCommitBufferSize
        store.setVersionsToKeep(0);
        store.setReuseSpace(true);

        MVMap<Integer, BibCapRecord> map = store.openMap("mymap", new MVMap.Builder<Integer, BibCapRecord>().keyType(new ObjectDataType()).valueType(new BibCapRecord()));


        //refactor, read in internalIdForOrderedAccess.txt and use that order
        BufferedReader idreader = new BufferedReader(new FileReader(new File("internalIdForOrderedAccess.txt")));
        List<Integer> orderedIds = new ArrayList<>();
        String line2;
        while ((line2 = idreader.readLine()) != null) {

            String[] parts = line2.split("\t");

            orderedIds.add(Integer.valueOf(parts[0]));
        }

        idreader.close();


        Int2DoubleOpenHashMap L1sumCit = new Int2DoubleOpenHashMap();
        Int2DoubleOpenHashMap L2sumCit = new Int2DoubleOpenHashMap();
        Int2DoubleOpenHashMap L3sumCit = new Int2DoubleOpenHashMap();
        Int2DoubleOpenHashMap L4sumCit = new Int2DoubleOpenHashMap();


        Int2IntOpenHashMap L1counts = new Int2IntOpenHashMap();
        Int2IntOpenHashMap L2counts = new Int2IntOpenHashMap();
        Int2IntOpenHashMap L3counts = new Int2IntOpenHashMap();
        Int2IntOpenHashMap L4counts = new Int2IntOpenHashMap();


        int NL1 = 0;
        int NL2 = 0;
        int NL3 = 0;
        int NL4 = 0;

        double overalMean = 0;
        int n = 0;
        for (Integer i : orderedIds) {

            BibCapRecord record = map.get(i);

            int cit = record.getCitationsExclSelf();
            double citlog1p = Math.log1p(cit);
            overalMean += citlog1p;

            int L1C = record.getClusterL1();
            int L2C = record.getClusterL2();
            int L3C = record.getClusterL3();
            int L4C = record.getClusterL4();

            if (L1C >= 0) {
                L1sumCit.addTo(L1C, citlog1p);
                NL1++;
                L1counts.addTo(L1C, 1);
            }
            if (L2C >= 0) {
                L2sumCit.addTo(L2C, citlog1p);
                NL2++;
                L2counts.addTo(L2C, 1);
            }
            if (L3C >= 0) {
                L3sumCit.addTo(L3C, citlog1p);
                NL3++;
                L3counts.addTo(L3C, 1);
            }
            if (L4C >= 0) {
                L4sumCit.addTo(L4C, citlog1p);
                NL4++;
                L4counts.addTo(L4C, 1);
            }

            n++;
        }

        overalMean = overalMean / n;


        //partition granularity
        System.out.println("L1 #: " + L1sumCit.size() + " L2 #: " + L2sumCit.size() + " L3 #: " + L3sumCit.size() + " L4 #: " + L4sumCit.size());

        //valid in partition
        System.out.println("L1 valid: " + NL1 + " L2 valid: " + NL2 + " L3 valid " + NL3 + " L4 valid " + NL4);


        //convert sum to mean
        System.out.println("Converting to mean");
        for (Int2DoubleMap.Entry entry : L1sumCit.int2DoubleEntrySet()) {

            int clusterL1 = entry.getIntKey();
            int counts = L1counts.get(clusterL1);

            entry.setValue((entry.getDoubleValue() / counts));

        }

        for (Int2DoubleMap.Entry entry : L2sumCit.int2DoubleEntrySet()) {

            int clusterL2 = entry.getIntKey();
            int counts = L2counts.get(clusterL2);

            entry.setValue((entry.getDoubleValue() / counts));

        }

        for (Int2DoubleMap.Entry entry : L3sumCit.int2DoubleEntrySet()) {

            int clusterL3 = entry.getIntKey();
            int counts = L3counts.get(clusterL3);

            entry.setValue((entry.getDoubleValue() / counts));

        }

        for (Int2DoubleMap.Entry entry : L4sumCit.int2DoubleEntrySet()) {

            int clusterL4 = entry.getIntKey();
            int counts = L4counts.get(clusterL4);

            entry.setValue((entry.getDoubleValue() / counts));

        }

/*
        System.out.println();

        for (Int2DoubleMap.Entry entry : L4sumCit.int2DoubleEntrySet()) {

            System.out.println("cluster: " + entry.getIntKey() + " refVal: " + entry.getDoubleValue() + " size: " + L4counts.get(entry.getIntKey()));
        }


        System.out.println();

        for (Int2DoubleMap.Entry entry : L3sumCit.int2DoubleEntrySet()) {

            System.out.println("cluster: " + entry.getIntKey() + " refVal: " + entry.getDoubleValue() + " size: " + L3counts.get(entry.getIntKey()));
        }

        System.out.println();

        for (Int2DoubleMap.Entry entry : L2sumCit.int2DoubleEntrySet()) {

            System.out.println("cluster: " + entry.getIntKey() + " refVal: " + entry.getDoubleValue() + " size: " + L2counts.get(entry.getIntKey()));
        }


        System.out.println();

        for (Int2DoubleMap.Entry entry : L1sumCit.int2DoubleEntrySet()) {

            System.out.println("cluster: " + entry.getIntKey() + " refVal: " + entry.getDoubleValue() + " size: " + L1counts.get(entry.getIntKey()));
        }


*/

        System.out.println("calculating referensvalues");
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File("ClusterRefs.txt")));
        //UT citlog1 refl1 refl2 refl3 refl4 comment

        int missingL1=0; int missingL2=0; int missingL3=0; int missingL4 = 0;
        for(Integer i :orderedIds) {

        BibCapRecord record = map.get(i);

        int cit = record.getCitationsExclSelf();
        double citlog1p = Math.log1p(cit);

        int L1C = record.getClusterL1();
        int L2C = record.getClusterL2();
        int L3C = record.getClusterL3();
        int L4C = record.getClusterL4();



        if(L1C < 0 || L2C < 0 || L3C < 0 || L4C < 0) {

            //no cluster info use overallMean
            writer.write(record.getUT() +"\t" + citlog1p +"\t" + overalMean +"\t" +overalMean +"\t" + overalMean +"\t" +overalMean +"\t" +"NO_CLUSTER_USING_OVERALL_MEAN");
            writer.newLine();
            continue;
            }

            StringBuilder info = new StringBuilder();

            //L1
           double refL1 = L1sumCit.get(L1C);
           int clusterSizeL1  = L1counts.get(L1C);

           if(clusterSizeL1 == 1) {
               //whopse! cant remove value as this makes the cluster empty. Use overall mean

               refL1 = overalMean;
               info.append("L1-info missing");
               missingL1++;

           } else {


              refL1 = removeValueFromMean(refL1,citlog1p,clusterSizeL1);

           }

            //L2
           double refL2 = L2sumCit.get(L2C);
           int clusterSizeL2 = L2counts.get(L2C);

           if(clusterSizeL2 == 1) {
               //whoops..

               refL2 = overalMean;
               info.append(" L2-info missing");
               missingL2++;

           } else {

               refL2 = removeValueFromMean(refL2,citlog1p,clusterSizeL2);
           }

            //L3
           double refL3 = L3sumCit.get(L3C);
           int clusterSizeL3 = L3counts.get(L3C);

           if(clusterSizeL3 == 1) {
               //whoops..
               refL3 = overalMean;
               info.append(" L3-info missing");
               missingL3++;
           } else {

               refL3 = removeValueFromMean(refL3,citlog1p,clusterSizeL3);

           }

            //L4
           double refL4 = L4sumCit.get(L4C);
           int clusterSizeL4 = L4counts.get(L4C);

           if(clusterSizeL4 == 1) {
               //whoops

               refL4 = overalMean;
               info.append(" L4-info missing");
               missingL4++;
           } else {

               refL4 = removeValueFromMean(refL4,citlog1p,clusterSizeL4);
           }


            writer.write(record.getUT() +"\t" + citlog1p +"\t" + refL1 +"\t" + refL2 +"\t" + refL3 +"\t" + refL4 +"\t" + (info.length() == 0 ? "OK" : info.toString()) );
            writer.newLine();


    }

    writer.flush();
        writer.close();


        System.out.println("clusters with just 1 object L1: " + missingL1 + " L2: " + missingL2 + " L3: " + missingL3 + " L4: " + missingL4);

}

}
