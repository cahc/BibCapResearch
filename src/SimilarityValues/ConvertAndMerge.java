package SimilarityValues;

import jnr.ffi.Struct;
import jsat.classifiers.linear.SPA;
import jsat.linear.IndexValue;
import jsat.linear.SparseMatrix;
import jsat.linear.SparseVector;
import jsat.linear.Vec;

import java.io.*;
import java.text.NumberFormat;
import java.util.Iterator;

/**
 * Created by crco0001 on 12/4/2017.
 */
public class ConvertAndMerge {

        final static String sep = " ";


    public static SparseMatrix readClutoFromFile(String clutofile) throws IOException {

        File file = new File(clutofile);

        if(!file.exists()) { System.out.println("Missing cluto file"); System.exit(0); }

        BufferedReader reader = new BufferedReader( new FileReader( file ));

        String[] header = reader.readLine().trim().split(" ");

        if(header.length != 3) { System.out.println("Wrong header in cluto file.."); System.exit(0); }

        int rows = Integer.valueOf( header[0] );
        int cols = Integer.valueOf( header[1] );
        int nnz = Integer.valueOf( header[2] );
        // e.g., 955491 955491 43379457

        System.out.println("Metadata from cluto file:");
        System.out.println("rows: " + rows);
        System.out.println("col: " + cols);
        System.out.println("nnz: " +nnz);

        SparseMatrix sparseMatrix = new SparseMatrix(rows,cols,   (int)(nnz / ((double)rows)) );

        String line;



        int row = 0;  //CLUTO FILES ARE 1-BASED INDEXED!!

        while(    (line = reader.readLine()) != null   ) {


            String[] lineParts = line.trim().split(" ");

            for(int i=0; i< lineParts.length-1; i++) {


               int col = Integer.valueOf(lineParts[i]);
               double value = Double.valueOf( lineParts[i+1] );
                i++;

                sparseMatrix.set(row,col-1,value); //CLUTO FILES ARE 1-BASED INDEXED!!

            }

            row++;


        }

        reader.close();


        return sparseMatrix;
    }


    public static void saveSparseMatrixToClutoFile(SparseMatrix sparseMatrix, String clutoFileName) throws IOException {

        BufferedWriter writer = new BufferedWriter( new FileWriter( new File(clutoFileName)));
        writer.write(sparseMatrix.rows() + sep + sparseMatrix.cols() + sep + sparseMatrix.nnz()  );
        writer.newLine();

        //CLUTO FILES ARE 1-BASED INDEXED!!

        for(int i=0; i<sparseMatrix.rows(); i++) {

            int n = sparseMatrix.getRowView(i).nnz();

         Iterator<IndexValue> iter = sparseMatrix.getRowView(i).getNonZeroIterator();

         int counter = 0;
            while(iter.hasNext()) {

                 IndexValue indexValue = iter.next();

                 writer.write(indexValue.getIndex()+1 + sep +indexValue.getValue()); //CLUTO FILES ARE 1-BASED INDEXED!!

                 counter++;
                 if(counter<n) writer.write(sep);

             }

             writer.newLine();
        }



        writer.flush();
        writer.close();

    }



    //TODO make a non Mutate version also..
    public static SparseMatrix combine2SparseMatricesMutate(SparseMatrix A, double weightA, SparseMatrix B) {

        if(weightA < 0 || weightA > 1) throw new NumberFormatException("WeightA must be within [0,1]");

        double weightB = 1-weightA;

        A.mutableMultiply(weightA);

        B.mutableMultiply(weightB);

        A.mutableAdd(1.0, B);

        return A;

    }


    public static void makeSymetric(SparseMatrix A) {

        if(A.rows() != A.cols()) {System.out.println("Only square matrices supported"); System.exit(0); }

        SparseMatrix C = new SparseMatrix(A.rows(),A.cols(),(int)(A.nnz() / ((double)A.rows())) );
        A.transpose(C);

        A.mutableAdd(1,C);

    }


    public static void saveToLinkedList(SparseMatrix A,boolean onlyUpperTriangle, String fileName) throws IOException {

        if(A.rows() != A.cols()) {System.out.println("Only square matrices supported"); System.exit(0); }

        System.out.println("### " + A.rows());

        BufferedWriter writer = new BufferedWriter( new FileWriter( new File(fileName)));

        int rows = A.rows();

        if(onlyUpperTriangle) {


            for(int i=0; i<rows; i++) {


                Vec row = A.getRowView(i);

                Iterator<IndexValue> iter = row.getNonZeroIterator();
                while(iter.hasNext()) {

                    IndexValue indexValue = iter.next();

                    if( indexValue.getIndex() > i ) { writer.write(i +" " + indexValue.getIndex() + " " +indexValue.getValue()); writer.newLine(); }

                }

            }

            writer.flush();
            writer.close();


        } else {


            System.out.println("n*n write not supported yet"); System.exit(0);
            writer.close();


        }


        Iterator<IndexValue> iterator = A.getRowView(955490).getNonZeroIterator();

        int count = 0;
        while (iterator.hasNext()) {

            iterator.next();
            count++;


        }

    }


    public static void main(String[] arg) throws IOException {

        SparseMatrix sparseMatrix1 = readClutoFromFile(arg[0]);
        SparseMatrix sparseMatrix2 = readClutoFromFile(arg[1]);


        System.out.println("info from matrix 1");
        System.out.println("rows:" +sparseMatrix1.rows() );
        System.out.println("col:" +sparseMatrix1.cols() );
        System.out.println("nnz:" +sparseMatrix1.nnz() );


        System.out.println("#");

        System.out.println("info from matrix 2");
        System.out.println("rows:" +sparseMatrix2.rows() );
        System.out.println("col:" +sparseMatrix2.cols() );
        System.out.println("nnz:" +sparseMatrix2.nnz() );


        System.out.println("Combining weight = 0.5");

        SparseMatrix sparseMatrix3 = combine2SparseMatricesMutate(sparseMatrix1,0.5,sparseMatrix2);

        System.out.println("info from combined matrix 3");
        System.out.println("rows:" +sparseMatrix3.rows() );
        System.out.println("col:" +sparseMatrix3.cols() );
        System.out.println("nnz:" +sparseMatrix3.nnz() );

        


        System.out.println("#");
        System.out.println("Writing to file..");

        saveSparseMatrixToClutoFile(sparseMatrix3,"combinedSimilarityValues.clu");

        System.out.println("Symmetrising...");
        makeSymetric(sparseMatrix3);

        System.out.println("info from combined matrix 4");
        System.out.println("rows:" +sparseMatrix3.rows() );
        System.out.println("col:" +sparseMatrix3.cols() );
        System.out.println("nnz:" +sparseMatrix3.nnz() );

        saveSparseMatrixToClutoFile(sparseMatrix3,"combinedSimilarityValuesSymmetriced.clu");

        saveToLinkedList(sparseMatrix3,true,"combinedLinkedList.txt");





    }



}
