package Index;

import BibCap.BibCapCitedReferenceWithSearchKey;
import BibCap.BibCapRecord;
import Misc.Ngram;
import Misc.ProgressBar;
import NLP.RAKE;
import NLP.Stemmer.UEALite;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.type.ObjectDataType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by crco0001 on 10/26/2017.
 */
public class WriteTermsToIndices {


    public static void main(String[] arg) throws IOException {


        if (arg.length != 1) {
            System.out.println("Supply name of MVstore DB");
            System.exit(0);
        }

        File check = new File(arg[0]);

        if (!check.exists()) {
            System.out.println("File dosent exist");
            System.exit(0);
        }


        MVStore store = new MVStore.Builder().cacheSize(200). // 200MB read cache
                fileName(arg[0]).autoCommitBufferSize(1024). // 1MB write cache
                open(); // autoCommitBufferSize
        store.setVersionsToKeep(0);
        store.setReuseSpace(true);

        MVMap<Integer, BibCapRecord> map = store.openMap("mymap", new MVMap.Builder<Integer, BibCapRecord>().keyType(new ObjectDataType()).valueType(new BibCapRecord()));


        System.out.println("Calculating unique terms..");
        HashSet<String> uniqueTerms = new HashSet<>(10000);

        for (Map.Entry<Integer, BibCapRecord> entry : map.entrySet()) {

           uniqueTerms.addAll( entry.getValue().getExtractedSimpleBagOfWordTokens()  ); // TODO warning, simple terms!

            //uniqueTerms.addAll( entry.getValue().getExtractedTerms()  ); // TODO warning, simple terms!


        }

        System.out.println("# " + uniqueTerms.size());

        BufferedWriter writer = new BufferedWriter( new FileWriter( new File("termsToIntegers.txt")));

        int index = 0;

        Iterator<String> iter = uniqueTerms.iterator();

        while( iter.hasNext() ) {

            writer.write(iter.next() +"\t" + index);
            writer.newLine();
            index++;

        }

        writer.flush();
        writer.close();

        store.close();
    }

}
