package SimilarityValues;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Created by crco0001 on 11/22/2017.
 */
public class MakeComparable {

    static class NodeWithSimValue implements Comparable<NodeWithSimValue>{

        int node;
        double val;

        public NodeWithSimValue(int node, double sim) {
            this.node = node;
            this.val = sim;

        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            NodeWithSimValue that = (NodeWithSimValue) o;

            return node == that.node;
        }

        @Override
        public int hashCode() {
            return node;
        }

        @Override
        public int compareTo(@NotNull NodeWithSimValue o) {

            if(this.node > o.node) return 1;
            if(this.node < o.node) return -1;
            return 0;


        }
    }


    public static void main(String[] arg) throws IOException {

        if (arg.length != 2) {
            System.out.println("arg: clutoFile1 clutoFile2");
            System.exit(0);
        }

        File file1 = new File(arg[0]);
        File file2 = new File(arg[1]);

        if (!file1.exists() || !file2.exists()) {
            System.out.println("file(s) don't exist");
            System.exit(0);
        }

        BufferedReader reader1 = new BufferedReader(new FileReader(file1));
        BufferedReader reader2 = new BufferedReader(new FileReader(file2));


        //check that dimensions match

        String[] line1Parts = reader1.readLine().split(" ");
        String[] line2Parts = reader2.readLine().split(" ");


        if (!line1Parts[0].equals(line2Parts[0]) ) {System.out.println("missmatch dimensions.."); System.exit(0); }
        if (!line1Parts[1].equals(line2Parts[1]) ) {System.out.println("missmatch dimensions.."); System.exit(0); }
        if (!line1Parts[1].equals(line2Parts[0]) ) {System.out.println("missmatch dimensions.."); System.exit(0); }

        System.out.println("N: " + line1Parts[0]);

        String line1;
        String line2;

        BufferedWriter writerSharedNodes = new BufferedWriter( new FileWriter( new File("SharedInfo.txt") ) );
        BufferedWriter averageSimPerNearestNeighbour = new BufferedWriter( new FileWriter( new File("averageSimPerKnn.txt") ) );


        List<Double> totalDistFile1 = new ArrayList<>(1000);
        List<Double> totalDistFile2 = new ArrayList<>(1000);


        while( (line1 = reader1.readLine()) != null  && (line2 = reader2.readLine()) != null  )   {

            line1Parts = line1.trim().split(" ");
            line2Parts = line2.trim().split(" ");

            List<NodeWithSimValue> list1 = new ArrayList<>(50);
            List<NodeWithSimValue> list2 = new ArrayList<>(50);

            for(int i=0; i< line1Parts.length-1; i++) {

              NodeWithSimValue nodeWithSimValue = new NodeWithSimValue( Integer.valueOf(line1Parts[i]), Double.valueOf(line1Parts[i+1]) );
              i++;

              list1.add(nodeWithSimValue);
            }


            for(int i=0; i< line2Parts.length-1; i++) {

                NodeWithSimValue nodeWithSimValue = new NodeWithSimValue( Integer.valueOf(line2Parts[i]), Double.valueOf(line2Parts[i+1]) );
                i++;

                list2.add(nodeWithSimValue);
            }


            OptionalDouble average1 = list1
                    .stream()
                    .mapToDouble(a -> a.val)
                    .average();

            OptionalDouble average2 = list2
                    .stream()
                    .mapToDouble(a -> a.val)
                    .average();



            for(NodeWithSimValue nodeWithSimValue : list1) totalDistFile1.add(   nodeWithSimValue.val   );

            for(NodeWithSimValue nodeWithSimValue : list2) totalDistFile2.add(   nodeWithSimValue.val   );

            //sort one list and use the object in the other as search keys

            int matches = 0;
            if( list1.size() > list2.size() ) {

                Collections.sort( list1 );

                for(NodeWithSimValue nodeWithSimValue : list2 ) {

                    int ind = Collections.binarySearch(list1, nodeWithSimValue);
                    if(ind > -1) {writerSharedNodes.write( list1.get(ind).val +" " + nodeWithSimValue.val  ); writerSharedNodes.newLine(); matches++; }
                }


            } else {

                Collections.sort( list2 );

                for(NodeWithSimValue nodeWithSimValue : list1 ) {

                    int ind = Collections.binarySearch(list2, nodeWithSimValue);
                    if(ind > -1) {writerSharedNodes.write( nodeWithSimValue.val + " "  + list2.get(ind).val  ); writerSharedNodes.newLine(); matches++; }
                }


            }


         averageSimPerNearestNeighbour.write(average1.orElse( 0 ) +" " + average2.orElse(0 ) );
          averageSimPerNearestNeighbour.newLine();




            // System.out.println("NN1: " +list1.size() +" NN2: " +list2.size() +" shared: " + matches );

        }


        //make list2 reference distribution
        QuantileFun quantileFun = new QuantileFun(totalDistFile2);


        ECDF ecdf = new ECDF(totalDistFile1);

        BufferedWriter writerNormalize = new BufferedWriter( new FileWriter( new File("normedCitDist.txt") ));

        System.out.println("Total values in file1: " + totalDistFile1.size());
        for(Double val : totalDistFile1) {

            double normedValue =  quantileFun.getQuantile( ecdf.getProbBinarySearch(val) );
            writerNormalize.write(val +" " +normedValue);
            writerNormalize.newLine();

        }

        BufferedWriter writerNormalize2 = new BufferedWriter( new FileWriter( new File("TermDist.txt") ));

        for(Double val : totalDistFile2) {


            writerNormalize2.write(""+ val);
            writerNormalize2.newLine();

        }


        writerNormalize.flush();
        writerNormalize.close();
        writerNormalize2.flush();
        writerNormalize2.flush();


        averageSimPerNearestNeighbour.flush();
        averageSimPerNearestNeighbour.close();
        writerSharedNodes.flush();
        writerSharedNodes.close();
        reader1.close();
        reader2.close();

    }

}
