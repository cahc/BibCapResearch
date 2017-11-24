package SimilarityValues;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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


    public double getProbParalellVersion( double val) {

        //returns the fraction of observations less or equal to val

        long count = Arrays.stream(this.values).parallel().filter( values -> values <= val ).count();

        return count/(double)k;
    }


    public double getProbBinarySearch( double val) {

        //index of the search key,if it is contained in the array;
        // otherwise, (-(insertion point) - 1). The insertion point is defined as the point at which the key would be inserted into the array: the index of the first element greater than the key, or a.length if all elements in the array are less than the specified key. Note that this guarantees that the return value will be >= 0 if and only if the key is found.

        //TODO implement
        int index = Arrays.binarySearch(this.values,val);


        return -1;
    }


    public static void main(String[] arg) {

        List<Double> values = new ArrayList<>();
        for (int i = 1; i <= 832; i++) {
            values.add((double)i);
        }

        Collections.shuffle(  values );

        ECDF ecdf = new ECDF(values);

        System.out.println( ecdf.getProb(55)); // should be 0.06610577
        System.out.println( ecdf.getProbParalellVersion(55)); // should be 0.06610577


    }


}
