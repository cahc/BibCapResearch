package BibCap;

import Misc.Ngram;
import NLP.RAKE;
import NLP.Stemmer.UEALite;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by crco0001 on 10/11/2017.
 */
public class BibCapParser {


    /*

    Datakälla: Bibmet version 2017 Q3.

•	I filen publ.txt finns 955661 publikationer från WoS representerade (två publikationer från studien av relationen mellan egenskaper hos citerade referenser och fältnormerad citeringsgrad kunde inte hittas i Bibmet). 1 publikation per rad med Doc_id, UT, Title, Full_source_title, C_sciwo och C_scxwo som kolumner. PY=2009. Endast publikationer av dokumenttypen ’Article’ registrerade i tidskriftsindexen i WoS samt publicerade i CWTS core journals (gällande CWTS Leiden Ranking 2015). Varje publikation har en titel och minst en citerad referens.

•	I filen abstract.txt, vilken gäller artiklarna i filen publ.txt, finns Doc_id och Text (abstract) som kolumner. Observera att ett abstract i Bibmets tabell 'Abstract' kan vara uppdelat på flera rader. I vissa fall gäller att ett Doc_id i filen kan förekomma på två eller flera konsekutiva rader i kolumnen Doc_id, och kolumnen Text har i dessa rader delarna av Doc_id:s abstract. Även artiklar utan abstract representeras i filen. Dessa har strängen NO_ABSTRACT kolumnen Text. 17896 publikationer saknar abstract.

•	cited_refs.txt innehåller citerade referenser för artiklarna i filen publ.txt, där även referenser, vilka inte gäller publikationer registrerade i WoS, finns med.

•	Filen WCs.txt rapporterar WoS Subject Categories för publikationerna representerade i filen publ.txt. En rad per publikation. 15 publikationer saknar kategori. Dessa är uppmärkta med 'NO-CATEGORY-NAME' i kolumnen 'WCs'.



     */



    File publ;
    File citedRefs;
    File abstracts;

    File subjectCategories;


    //for the persistence stores..
    private final static Set<Integer> orderedKeySet = new TreeSet<Integer>();


    static final Pattern TAB = Pattern.compile("\t");
    final static Pattern ARKIVX = Pattern.compile(".*?ARXIV.*[0,9]+.*");








    public BibCapParser(String publFile, String citedRefsFile, String abstractsFile, String citationDataFile) throws IOException {

        //Doc_id	UT	Title	Source	TC(inc self cit) TC(exl self cit)

        this.publ = new File(publFile);

        //Doc_id	UT	Title	Full_source_title	C_sciwo	C_scxwo	Cited_author	Cited_ref_year	Cited_work	Cited_volume	Cited_page	Document_year
        this.citedRefs = new File(citedRefsFile);

        //Doc_id	Text
        this.abstracts = new File(abstractsFile);

        //UT	WCs
        this.subjectCategories = new File(citationDataFile);


        if(!this.publ.exists()) throw new IOException("publFile missing..");
        if(!this.citedRefs.exists()) throw new IOException("citedRefs missing..");
        if(!this.abstracts.exists()) throw new IOException("abstracts missing..");
        if(!this.subjectCategories.exists()) throw new IOException("subCats missing..");


    }


    public Set<Integer> getKeySet() {

       return Collections.unmodifiableSet(orderedKeySet);

    }


    public BibCapParser() throws IOException {


        this("DATA//publ.txt","DATA//cited_refs.txt","DATA//abstracts.txt","DATA//WCs.txt");

    }



    public void parse(BibCapRecordStore bibCapRecordStore, boolean useNgram) throws IOException {


        System.out.println("Loading Rake..");
        RAKE rake = new RAKE();
        Set<String> stopwords = rake.loadStopWordList(true);
        UEALite stemmer = new UEALite();



        System.out.println("Getting doc_id, UT, title and source and citations in pass one..");
        System.out.println("Running custom RAKE keyword extraction algorithm on title");
        BufferedReader reader = new BufferedReader(new FileReader(this.publ));

        boolean firstline = true;
        String line;
        while ((line = reader.readLine()) != null) {
            if (firstline) {
                firstline = false;
                continue;
            }


            String[] splitted = TAB.split(line);

            Integer doc_id = Integer.valueOf(splitted[0]);
            String UT = splitted[1];
            String title = splitted[2];
            String source = splitted[3];

            int TC_with_self_cit = Integer.valueOf(splitted[4]);
            int TC_without_seld_cit = Integer.valueOf(splitted[5]);



            BibCapRecord biBCapRecord = new BibCapRecord();
            biBCapRecord.setInternalId(doc_id);
            biBCapRecord.setUT(UT);
            biBCapRecord.setTitle(title);
            biBCapRecord.setSource(source);
            biBCapRecord.setCitationsExclSelf( TC_without_seld_cit );
            biBCapRecord.setCitationsIncSelf( TC_with_self_cit );


            if(title != null) {
                List<List<String>> rakeKeyWordsAndSimpleTokens =  rake.getKeyWords(title,false,stopwords,stemmer);

                List<String> keywordsFromTitle = rakeKeyWordsAndSimpleTokens.get(0);


                for(String s :keywordsFromTitle) {

                    biBCapRecord.addExtractedTerm(s);


                    if (useNgram) {
                        if (Ngram.countWords(s) > 2) {

                            String[] extracted2grams = Ngram.wordNgrams(s, 2); //TODO skip this?

                            biBCapRecord.addAllExtractedTerms(Arrays.asList(extracted2grams));
                        }


                    }
                }

                biBCapRecord.addAllSimpleBagOfWordTerms( rakeKeyWordsAndSimpleTokens.get(1) );
            }




            bibCapRecordStore.putRecord(doc_id, biBCapRecord);

            orderedKeySet.add(doc_id);

        }


        System.out.println("Records created in pass 1: " + bibCapRecordStore.size());

        System.out.println("Getting abstracts in pass two..");


        reader.close();

        reader = new BufferedReader(new FileReader(this.abstracts));

        firstline = true;
        HashSet<Integer> countRecordsWitAbstract = new HashSet<>();

        int idOfCurrentFetchedDoc = -99;
        BibCapRecord bibCapRecord = null;

        while ((line = reader.readLine()) != null) {
            if (firstline) {
                firstline = false;
                continue;
            }


            String[] splitted = TAB.split(line);

            Integer id = Integer.valueOf(splitted[0]);
            String text = splitted[1];
            if( "NO_ABSTRACT".equals( text) ) continue;

            countRecordsWitAbstract.add(id);


            if(idOfCurrentFetchedDoc == id) {

                bibCapRecord.addPartOfAbstract(text);


            } else {

                //for persitance store, overwrite
                if(idOfCurrentFetchedDoc != -99) bibCapRecordStore.putRecord(idOfCurrentFetchedDoc, bibCapRecord);

                bibCapRecord = bibCapRecordStore.getRecord(id);
                bibCapRecord.addPartOfAbstract(text);
                idOfCurrentFetchedDoc = id;
            }


        }

        //last uppdated record may not have been writen to disk
        bibCapRecordStore.putRecord(idOfCurrentFetchedDoc,bibCapRecord);


        reader.close();

        System.out.println("Running RAKE on abstracts.. Also saving simple Bag-of-Words..");
        for (Integer key : orderedKeySet) {

            BibCapRecord record = bibCapRecordStore.getRecord(key);

            record.createFullAbstract();

            String summary = record.getAbstractText();

            if(summary != null) {
                List<List<String>> rakeKeyWordsAndSimpleTokens = rake.getKeyWords(summary, true, stopwords, stemmer);

                List<String> keywordsFromAbstract = rakeKeyWordsAndSimpleTokens.get(0);

                for(String s : keywordsFromAbstract)  {

                    record.addExtractedTerm(s);

                    if(useNgram) {

                        if (Ngram.countWords(s) > 2) {

                            String[] extracted2grams = Ngram.wordNgrams(s, 2);

                            record.addAllExtractedTerms(Arrays.asList(extracted2grams));

                        }

                    }

                }


                record.addAllSimpleBagOfWordTerms( rakeKeyWordsAndSimpleTokens.get(1) );

            }


            bibCapRecordStore.putRecord(key, record);


        }


        System.out.println("Abstracts added to " + countRecordsWitAbstract.size() + " records in pass two");

        countRecordsWitAbstract = null;


        System.out.println("Getting cited references in pass 3..");
        System.out.println("Creating 4 indexing keys per cited reference..");


        reader = new BufferedReader(new FileReader(this.citedRefs));
        firstline = true;
        HashSet<Integer> countRecordsWitRef = new HashSet<>();

        BufferedWriter badReferencesWriter= new BufferedWriter( new FileWriter( new File("badReferences.txt")));
        BufferedWriter goodReferenceWriter = new BufferedWriter( new FileWriter( new File("goodReferences.txt")) );

        int badRefCount = 0;

        idOfCurrentFetchedDoc = -99;
        bibCapRecord = null;


        while ((line = reader.readLine()) != null) {
            if (firstline) {
                firstline = false;
                continue;
            }


            String[] splitted = line.split("\t");
            Integer id = Integer.valueOf(splitted[0]);


            String cited_author = splitted[6].toUpperCase(); //sometimes the strings are mixed
            String cited_year = splitted[7];
            String cited_work = splitted[8].toUpperCase(); //sometimes the strings are mixed
            String cited_volume = splitted[9];
            String cited_page = splitted[10];

            StringBuilder citedString = new StringBuilder();

            int missing = 0;
            if(cited_author.length() > 1)  {


                //MAIR, RJ --> MAIR, R
               int index = cited_author.indexOf(',');

               if(index != -1) cited_author = cited_author.substring(0,index+3);

                citedString.append(cited_author);






            } else {missing++;}


            if(cited_year.length() > 1)  {citedString.append(" ").append(cited_year); } else {missing++;}
            if(cited_work.length() > 1)  {citedString.append(" ").append(cited_work); } else {missing++;}
            if(cited_volume.length() > 1) {citedString.append(" ").append(cited_volume); } else {missing++;}
            if(cited_page.length() > 1)  {citedString.append(" ").append(cited_page); } else {missing++;}


            if(missing > 2) {

                //CHECK IF ARXIV
                String check = citedString.toString();
                Matcher m = ARKIVX.matcher(check);

                if(m.find()) {

                    if(idOfCurrentFetchedDoc == id) {


                        String okRef =  m.group(0).trim();

                        BibCapCitedReferenceWithSearchKey bibCapCitedReferenceWithSearchKey = new BibCapCitedReferenceWithSearchKey(okRef, cited_author,cited_year,cited_work);

                        bibCapRecord.addCitedReference( bibCapCitedReferenceWithSearchKey  );
                        countRecordsWitRef.add(id);
                        goodReferenceWriter.write(okRef);
                        goodReferenceWriter.write(okRef);
                        goodReferenceWriter.newLine();

                    } else {


                        if(idOfCurrentFetchedDoc != -99) bibCapRecordStore.putRecord(idOfCurrentFetchedDoc, bibCapRecord);

                        //now new
                        bibCapRecord = bibCapRecordStore.getRecord(id);

                        String okRef =  m.group(0).trim();

                        BibCapCitedReferenceWithSearchKey bibCapCitedReferenceWithSearchKey = new BibCapCitedReferenceWithSearchKey(okRef, cited_author,cited_year,cited_work);

                        bibCapRecord.addCitedReference( bibCapCitedReferenceWithSearchKey  );
                        countRecordsWitRef.add(id);
                        idOfCurrentFetchedDoc = id;
                        goodReferenceWriter.write(okRef);
                        goodReferenceWriter.newLine();

                    }


                } else {

                    badReferencesWriter.write( citedString.toString() );  badReferencesWriter.newLine(); badRefCount++;

                }

            } else {

                if(idOfCurrentFetchedDoc == id) {
                    String okRef = citedString.toString();

                    BibCapCitedReferenceWithSearchKey bibCapCitedReferenceWithSearchKey = new BibCapCitedReferenceWithSearchKey(okRef, cited_author,cited_year,cited_work);

                    bibCapRecord.addCitedReference( bibCapCitedReferenceWithSearchKey );
                    goodReferenceWriter.write(okRef);
                    goodReferenceWriter.newLine();
                    countRecordsWitRef.add(id);


                } else {

                    if(idOfCurrentFetchedDoc != -99) bibCapRecordStore.putRecord(idOfCurrentFetchedDoc, bibCapRecord);


                    bibCapRecord = bibCapRecordStore.getRecord(id);
                    String okRef = citedString.toString();
                    goodReferenceWriter.write(okRef);
                    goodReferenceWriter.newLine();

                    BibCapCitedReferenceWithSearchKey bibCapCitedReferenceWithSearchKey = new BibCapCitedReferenceWithSearchKey(okRef, cited_author,cited_year,cited_work);

                    bibCapRecord.addCitedReference( bibCapCitedReferenceWithSearchKey );
                    countRecordsWitRef.add(id);
                    idOfCurrentFetchedDoc = id;


                }

            }


        }

        //last uppdated record may not have been writen to disk
        bibCapRecordStore.putRecord(idOfCurrentFetchedDoc,bibCapRecord);



        System.out.println("References added to " + countRecordsWitRef.size() + " records in pass 3");
        System.out.println(badRefCount +" reference ignored: noise, in press etc. See badReferences.txt");
        reader.close();
        badReferencesWriter.flush();
        badReferencesWriter.close();



        System.out.println("Getting Subject categories in pass 4");

        reader = new BufferedReader(new FileReader(this.subjectCategories));

        //UT	WCs
        Map<String,String>  UTtoWCs = new HashMap<>();


        firstline = true;
        while ((line = reader.readLine()) != null) {
            if (firstline) {
                firstline = false;
                continue;
            }


            String[] splitted = TAB.split(line);
            String UT = splitted[0];
            String WCs = splitted[1];


            UTtoWCs.put(UT, WCs);


        }

        reader.close();

        System.out.println(UTtoWCs.size() + " UT:s with WC:s (inc 'NO-CATEGORY-NAME' read, now matching..");
        HashSet<Integer> countConsideredArticles = new HashSet<>();



        for(Integer key : orderedKeySet ) {

            bibCapRecord = bibCapRecordStore.getRecord(key);
            String UT = bibCapRecord.getUT();

            String WCs = UTtoWCs.get(UT);

            if("NO-CATEGORY-NAME".equals(WCs)) {

                System.out.println("missing category, calling .setConsideredRecord(false)!");
                bibCapRecord.setConsideredRecord(false);

                bibCapRecordStore.putRecord(key,bibCapRecord );

            } else {


                bibCapRecord.addSubjectCategories( WCs );

                bibCapRecord.setConsideredRecord(true);
                bibCapRecordStore.putRecord(key, bibCapRecord);
                countConsideredArticles.add( bibCapRecord.internalId );


            }


        }



        System.out.println(countConsideredArticles.size() +" records matched = final data set ");

        System.out.println("total size: " + bibCapRecordStore.size());


        System.out.println("Consider running java -cp *.jar Persistor.runCompactOnMVStore");



    }

}
