package Experiments;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class SimChecker {

    public class simVectorSimple {

        int[] dims;
        float[] values;

        public simVectorSimple(String s) {


            String[] parts = s.trim().split(" ");
            dims = new int[parts.length/2];
            values = new float[parts.length/2];

            int j=0;
            for(int i=0; i<(parts.length-1); i++) {


                dims[j] = Integer.valueOf(parts[i]);
                values[j] = Float.valueOf(parts[i+1]);
                i++;
                j++;

            }


        }

        @Override
        public String toString() {
            return "simVectorSimple{" +
                    "dims=" + Arrays.toString(dims) +
                    ", values=" + Arrays.toString(values) +
                    '}';
        }
    }


    public static void main(String[] arg) throws IOException {

        //this class return top-x for inspection based on titles, for sanity checking..

        if(arg.length != 1) {System.out.println("supply a similarity matrix in clu format.."); System.exit(0); }

        BufferedReader reader = new BufferedReader( new FileReader( new File("RecordsInTextFormat.txt")));

        String line;
        List<String> titles = new ArrayList<>();

        while(   (line = reader.readLine()) != null  ) {

            String[] parts = line.split("\t");
            titles.add( parts[2] );
        }

        reader.close();
        System.out.println("# titles read: " + titles.size());


        BufferedReader reader2 = new BufferedReader( new FileReader( new File(arg[0])));

        String metadata = reader2.readLine().trim();
        if(metadata.split(" ").length != 3) {System.out.println("wrong start of clu-file!"); System.exit(0); }

        SimChecker simChecker = new SimChecker();
        List<simVectorSimple> simVectorSimples = new ArrayList<>();
        while(   (line = reader2.readLine()) != null  ) {

           simVectorSimples.add(simChecker.new simVectorSimple(line) );
        }

        System.out.println("# vectors read: " + simVectorSimples.size());

        reader2.close();
        Scanner scanner = new Scanner(System.in);

        while (true) {

            System.out.print("Best match for (1-indexed): ");
            String number = scanner.nextLine();

            simVectorSimple  simVectorSimple = simVectorSimples.get( Integer.valueOf(number)-1  );
            System.out.print("input:");
            System.out.println(titles.get( (Integer.valueOf(number)) -1) );
            System.out.println("best match:");
            System.out.println(titles.get( (simVectorSimple.dims[0])-1 )  ); //?
            System.out.println("second best match:");
            System.out.println(titles.get( (simVectorSimple.dims[1])-1 )  ); //?
            System.out.println();



        }


    }

}
