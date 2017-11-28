package SimilarityValues;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by crco0001 on 11/23/2017.
 */
public class ECDF implements Serializable{

    double[] values;
    int k;
    //empirical cumulative distribution function


    public ECDF() {}

    public ECDF(double[] values) {


        this.values = values;
        Arrays.sort(this.values);

        k = this.values.length;
    }

    public ECDF(List<Double> values) {

        this.values = new double[values.size()];
        for(int i=0; i<values.size(); i++) this.values[i] = values.get(i);
        Arrays.sort(this.values);
        k = this.values.length;
    }



    public double getProb(double val) {
        //returns the fraction of observations less or equal to val

        int count = 0;
        for(int i=0; i<this.values.length; i++) {


            if(  this.values[i] <= val  ) { count++; } else { break;}

        }


        return count/(double)k;

    }



    public double getProbBinarySearch( double val) {

        //index of the search key, if it is contained in the array; otherwise, (-(insertion point) - 1). The insertion point is defined as the point at which the key would be inserted into the array: the index of the first element greater than the key, or a.length if all elements in the array are less than the specified key. Note that this guarantees that the return value will be >= 0 if and only if the key is found.

        int index = Arrays.binarySearch(this.values, val);

        if(index >= 0) return (index+1)/(double)k; //returns the fraction of observations less or equal to val

        return (Math.abs(index)-1) / (double)k;
    }


    public static void main(String[] arg) {


        List<Double> values = new ArrayList<>();
        for (int i = 1; i <= 200000; i++) {
            values.add(    ThreadLocalRandom.current().nextDouble()  );
        }

        //Collections.shuffle(  values );
        ECDF ecdf = new ECDF(values);

        long start = System.currentTimeMillis();

        for(int i=0; i<values.size(); i++) ecdf.getProb( ThreadLocalRandom.current().nextDouble() );

        System.out.println("Loop: " + (System.currentTimeMillis()-start)/1000.0 );


        start = System.currentTimeMillis();

        for(int i=0; i<values.size(); i++) ecdf.getProbBinarySearch( ThreadLocalRandom.current().nextDouble() );

        System.out.println("Binary search: " + (System.currentTimeMillis()-start)/1000.0 );


        System.out.println( ecdf.getProb(values.get(500) )); // should be 0.06610577

        System.out.println( ecdf.getProbBinarySearch( values.get(500) ) ); // should be 0.06610577

    }


}
