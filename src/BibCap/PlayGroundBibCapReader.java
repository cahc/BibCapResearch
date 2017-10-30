package BibCap;

import NLP.RAKE;
import NLP.Stemmer.UEALite;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.type.ObjectDataType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by crco0001 on 10/26/2017.
 */
public class PlayGroundBibCapReader {



    public static void main(String[] arg) throws IOException {


        if(arg.length != 1) {  System.out.println("Supply name of MVstore DB"); System.exit(0); }

        File check = new File(arg[0]);

        if(!check.exists()) { System.out.println("File dosent exist"); System.exit(0); }


        MVStore store = new MVStore.Builder().cacheSize(200). // 200MB read cache
                fileName(arg[0]).autoCommitBufferSize(1024). // 1MB write cache
                open(); // autoCommitBufferSize
        store.setVersionsToKeep(0);
        store.setReuseSpace(true);

        MVMap<Integer, BibCapRecord> map = store.openMap("mymap", new MVMap.Builder<Integer,BibCapRecord>().keyType(new ObjectDataType()).valueType( new BibCapRecord() ));



        //running single thread rake..

        System.out.println("Loading Rake..");
        RAKE rake = new RAKE();

        Set<String> stopwords = rake.loadStopWordList(true);
        UEALite stemmer = new UEALite();


/*
        BibCapRecord record = map.get(14318771);

        System.out.println(record.getTitle());
        rake.getTokens(record.getTitle());

        System.out.println(record.abstractText);

        rake.getTokens(record.getAbstractText());
        //TODO fix outputs empty/null tokens

*/


        BufferedWriter bufferedWriter = new BufferedWriter( new FileWriter(new File("TermExtractorTest.txt")));
        for(Map.Entry<Integer,BibCapRecord> entry : map.entrySet()) {


            BibCapRecord record = entry.getValue();

            String title = record.getTitle();

            String summary = record.getAbstractText();

           // System.out.println(record.internalId + "@@" +record.getUT() );


            if(title != null) {
               List<String> keywordsFromTitle =  rake.getKeyWords(title,false,stopwords,stemmer);
               bufferedWriter.write(keywordsFromTitle.toString());
               bufferedWriter.newLine();
            }
            if(summary != null) {
                List<String> keywordsFromAbstract = rake.getKeyWords(summary, true, stopwords, stemmer);

                bufferedWriter.write(keywordsFromAbstract.toString());
                bufferedWriter.newLine();
            }
        }


        bufferedWriter.flush();
        bufferedWriter.close();
        store.close();

    }










}
