package SimilarityValues;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by crco0001 on 11/23/2017.
 */
public class QuantileFun implements Serializable{


    double[] values;
    int n;
    //quantile function, linear interpolation of the empirical cdf. ("type 4 in R");



    public QuantileFun() {}

    public QuantileFun(double[] values) {


        this.values = values;
        Arrays.sort(this.values);

        n = this.values.length;
    }

    public QuantileFun(List<Double> values) {

        this.values = new double[values.size()];
        for(int i=0; i<values.size(); i++) this.values[i] = values.get(i);
        Arrays.sort(this.values);
        n = this.values.length;
    }


    public double getQuantile(double prob) {

        if(prob > 1 || prob < 0) throw new NumberFormatException("getProb outside [0,1]");

        if( prob < 1/(double)n ) return values[0];
        if( prob == 1 ) return values[ n-1 ];

        double h = n*prob;
        int hfloor = (int)Math.floor(h);

        return ( values[hfloor-1] + (h-hfloor)*(  values[hfloor+1-1] - values[hfloor-1]   ) );

    }



    public static void main(String[] arg) {


        List<Double> values = new ArrayList<>();
        for (int i = 1; i <= 833; i++) {
            values.add((double)i);
        }

        Collections.shuffle(  values );

        QuantileFun quantileFun = new QuantileFun(values);

        System.out.println( quantileFun.getQuantile(0.9813) ); // in R: 817.4229



    }


}
