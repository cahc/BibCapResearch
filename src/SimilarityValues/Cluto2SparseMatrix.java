package SimilarityValues;

import jsat.linear.SparseMatrix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by crco0001 on 12/4/2017.
 */
public class Cluto2SparseMatrix {




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

        return sparseMatrix;
    }


    public void saveSparseMatrixToClutoFile(SparseMatrix sparseMatrix) {


    }





}
