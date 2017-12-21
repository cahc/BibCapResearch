package Referensvalues;

import jsat.linear.SparseMatrix;

import java.io.IOException;

/**
 * Created by crco0001 on 12/14/2017.
 */
public class CalculateValues {




    public static void main(String[] arg) throws IOException {

        SparseMatrix A = SimilarityValues.ConvertAndMerge.readClutoFromFile(arg[0]);
        SparseMatrix B = SimilarityValues.ConvertAndMerge.readClutoFromFile(arg[1]);


    }







}
