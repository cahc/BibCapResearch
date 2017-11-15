package Index;

import BibCap.BibCapCitedReferenceWithSearchKey;
import BibCap.BibCapRecord;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.type.ObjectDataType;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by crco0001 on 11/15/2017.
 */
public class ToCluto {

    //read in data and write a sparse representation according to the cluto file format
    public static void main(String[] arg) throws IOException {

        //List with SparseDoc
        final ObjectArrayList<SparseDoc> collectionOfSparseDoc = new ObjectArrayList<SparseDoc>(10000);

        //collect IDF values for each feature
        Int2IntOpenHashMap featureToGlobalFrequency = new Int2IntOpenHashMap(10000);


        if (arg.length != 2) {
            System.out.println("Supply name of MVstore DB & CitedRefToDimensions.txt");
            System.exit(0);
        }

        File check = new File(arg[0]);

        if (!check.exists()) {
            System.out.println("MVstore File dosen't exist");
            System.exit(0);
        }


        File check2 = new File(arg[1]);


        if (!check2.exists()) {
            System.out.println("CitedRefsToDimensions file dosen't exist");
            System.exit(0);
        }

        BufferedReader reader = new BufferedReader( new FileReader(new File(arg[1])  ) );

        Object2IntOpenHashMap<String> citedRefToIndices = new Object2IntOpenHashMap<>();
        citedRefToIndices.defaultReturnValue(-1);

        String line;
        while(   (line = reader.readLine()) != null  ) {

            String[] part = line.split("\t");

            //dimension indices start at 1, not zero
            citedRefToIndices.put(part[0], (int)Integer.valueOf(part[1])+1   );

        }

        System.out.println("# mappings between string and indices: " + citedRefToIndices.size());


        MVStore store = new MVStore.Builder().cacheSize(200). // 200MB read cache
                fileName(arg[0]).autoCommitBufferSize(1024). // 1MB write cache
                open(); // autoCommitBufferSize
        store.setVersionsToKeep(0);
        store.setReuseSpace(true);

        MVMap<Integer, BibCapRecord> map = store.openMap("mymap", new MVMap.Builder<Integer, BibCapRecord>().keyType(new ObjectDataType()).valueType(new BibCapRecord()));


        int maxDim = -99;
        for (Map.Entry<Integer, BibCapRecord> entry : map.entrySet()) {

           Int2FloatOpenHashMap dimToOccurences = new Int2FloatOpenHashMap(); //TODO why float here?

            BibCapRecord bibCapRecord = entry.getValue();
            List<BibCapCitedReferenceWithSearchKey> references = bibCapRecord.getCitedReferences();


            for(BibCapCitedReferenceWithSearchKey ref : references) {


                   int dim =  citedRefToIndices.getInt( ref.getCitedRefString() );
                   if(dim == -1 ) {System.out.println("Dim -1, should not have happened! Aborting"); System.exit(0); }

                   if(dim > maxDim) maxDim = dim;
                   dimToOccurences.addTo(dim,1);


            }

            SparseDoc sparseDoc = new SparseDoc(dimToOccurences,bibCapRecord.getInternalId() );

            for (int i = 0; i < sparseDoc.dimensions.length; i++) {

                         featureToGlobalFrequency.addTo(sparseDoc.dimensions[i], 1);
                     }

            collectionOfSparseDoc.add(sparseDoc);

        }

        System.out.println("Max dim (one indexed) is: " +  maxDim );


        int nrows = collectionOfSparseDoc.size();
        int ncols = featureToGlobalFrequency.size();
        int nnz = 0;

        for(SparseDoc d : collectionOfSparseDoc) nnz += d.getLength();


        System.out.println("Documents/rows: " + nrows);
        System.out.println("Features/column: " + ncols);
        System.out.println("nnz (#non zero values): " +nnz);

        //System.out.println("Sorting by dimension");


        //for(SparseDoc d : collectionOfSparseDoc) d.sortByDimensions();

        File file = new File("rawVectors.clu");

        BufferedWriter writer = new BufferedWriter(new FileWriter(file,false));

        writer.write(nrows +" " + ncols + " " + nnz);
        writer.newLine();

        for(SparseDoc d : collectionOfSparseDoc) {

            writer.write(d.toString());
            writer.newLine();

        }


        writer.flush();
        writer.close();


    }




}
