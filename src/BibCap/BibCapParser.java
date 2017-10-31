package BibCap;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by crco0001 on 10/11/2017.
 */
public class BibCapParser {



    File publ;
    File citedRefs;
    File abstracts;

    File citationData;


    //for the persistence stores..
    private final static Set<Integer> orderedKeySet = new TreeSet<Integer>();


    static final Pattern TAB = Pattern.compile("\t");
    final static Pattern ARKIVX = Pattern.compile(".*?ARXIV.*[0,9]+.*");

    public BibCapParser(String publFile, String citedRefsFile, String abstractsFile, String citationDataFile) throws IOException {

        //Doc_id	UT	Title	Source	TC

        this.publ = new File(publFile);

        //Doc_id	UT	Title	Source	TC	Cited_author	Cited_ref_year	Cited_work	Cited_volume	Cited_page	Document_year
        this.citedRefs = new File(citedRefsFile);

        //Doc_id	Text
        this.abstracts = new File(abstractsFile);

        //UT C_sciwo (citations inc self) ... C_scxwo (citations exl. self)
        this.citationData = new File(citationDataFile);


        if(!this.publ.exists()) throw new IOException("publFile missing..");
        if(!this.citedRefs.exists()) throw new IOException("publFile missing..");
        if(!this.abstracts.exists()) throw new IOException("publFile missing..");
        if(!this.citationData.exists()) throw new IOException("publFile missing..");


    }


    public Set<Integer> getKeySet() {

       return Collections.unmodifiableSet(orderedKeySet);

    }


    public BibCapParser() throws IOException {


        this("publ.txt","cited_refs_new.txt","abstracts.txt","wos_cwts_kth_cit-data_V2.txt");

    }



    public void parse(BibCapRecordStore bibCapRecordStore) throws IOException {



        System.out.println("Getting doc_id, UT, title and source in pass one..");
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
            //ignoring TC

            BibCapRecord biBCapRecord = new BibCapRecord();
            biBCapRecord.setInternalId(doc_id);
            biBCapRecord.setUT(UT);
            biBCapRecord.setTitle(title);
            biBCapRecord.setSource(source);

            bibCapRecordStore.putRecord(doc_id, biBCapRecord);

            orderedKeySet.add(doc_id);

        }


        System.out.println("Records created in pass 1: " + bibCapRecordStore.size());

        System.out.println("Getting abstracts in pass two..");

        reader.close();

        reader = new BufferedReader(new FileReader(this.abstracts));

        firstline = true;
        HashSet<Integer> countRecordsWitAbstract = new HashSet<>();

        int idOfCurrentFetechedDoc = -99;
        BibCapRecord bibCapRecord = null;

        while ((line = reader.readLine()) != null) {
            if (firstline) {
                firstline = false;
                continue;
            }


            String[] splitted = TAB.split(line);

            Integer id = Integer.valueOf(splitted[0]);
            countRecordsWitAbstract.add(id);
            String text = splitted[1];

            if(idOfCurrentFetechedDoc == id) {

                bibCapRecord.addPartOfAbstract(text);



            } else {

                //for persitance store, overwrite
                if(idOfCurrentFetechedDoc != -99) bibCapRecordStore.putRecord(idOfCurrentFetechedDoc, bibCapRecord);

                bibCapRecord = bibCapRecordStore.getRecord(id);
                bibCapRecord.addPartOfAbstract(text);
                idOfCurrentFetechedDoc = id;
            }


        }

        //last uppdated record may not have been writen to disk
        bibCapRecordStore.putRecord(idOfCurrentFetechedDoc,bibCapRecord);


        reader.close();



        for (Integer key : orderedKeySet) {

            BibCapRecord record = bibCapRecordStore.getRecord(key);

            record.createFullAbstract();

            bibCapRecordStore.putRecord(key, record);


        }





        System.out.println("Abstracts added to " + countRecordsWitAbstract.size() + " records in pass two");

        countRecordsWitAbstract = null;


        System.out.println("Getting cited references in pass 3..");

        reader = new BufferedReader(new FileReader(this.citedRefs));
        firstline = true;
        HashSet<Integer> countRecordsWitRef = new HashSet<>();

        BufferedWriter badReferencesWriter= new BufferedWriter( new FileWriter( new File("badReferences.txt")));
        BufferedWriter goodReferenceWriter = new BufferedWriter( new FileWriter( new File("goodReferences.txt")) );

        int badRefCount = 0;

        idOfCurrentFetechedDoc = -99;
        bibCapRecord = null;


        while ((line = reader.readLine()) != null) {
            if (firstline) {
                firstline = false;
                continue;
            }


            String[] splitted = line.split("\t");
            Integer id = Integer.valueOf(splitted[0]);


            String cited_author = splitted[5];
            String cited_year = splitted[6];
            String cited_work = splitted[7];
            String cited_volume = splitted[8];
            String cited_page = splitted[9];

            StringBuilder citedString = new StringBuilder();

            int missing = 0;
            if(cited_author.length() > 1)  {citedString.append(cited_author); } else {missing++;}
            if(cited_year.length() > 1)  {citedString.append(" ").append(cited_year); } else {missing++;}
            if(cited_work.length() > 1)  {citedString.append(" ").append(cited_work); } else {missing++;}
            if(cited_volume.length() > 1) {citedString.append(" ").append(cited_volume); } else {missing++;}
            if(cited_page.length() > 1)  {citedString.append(" ").append(cited_volume); } else {missing++;}


            if(missing > 2) {

                //CHECK IF ARXIV
                String check = citedString.toString();
                Matcher m = ARKIVX.matcher(check);

                if(m.find()) {

                    if(idOfCurrentFetechedDoc == id) {

                        bibCapRecord.addCitedReferenceString(m.group(0).trim());
                        countRecordsWitRef.add(id);

                    } else {


                        if(idOfCurrentFetechedDoc != -99) bibCapRecordStore.putRecord(idOfCurrentFetechedDoc, bibCapRecord);

                        //now new
                        bibCapRecord = bibCapRecordStore.getRecord(id);
                        bibCapRecord.addCitedReferenceString(m.group(0).trim());
                        countRecordsWitRef.add(id);
                        idOfCurrentFetechedDoc = id;

                    }


                } else {

                    badReferencesWriter.write( citedString.toString() );  badReferencesWriter.newLine(); badRefCount++;

                }

            } else {

                if(idOfCurrentFetechedDoc == id) {
                    String okRef = citedString.toString();
                    bibCapRecord.addCitedReferenceString( okRef );
                    goodReferenceWriter.write(okRef);
                    goodReferenceWriter.newLine();
                    countRecordsWitRef.add(id);


                } else {

                    if(idOfCurrentFetechedDoc != -99) bibCapRecordStore.putRecord(idOfCurrentFetechedDoc, bibCapRecord);


                    bibCapRecord = bibCapRecordStore.getRecord(id);
                    String okRef = citedString.toString();
                    goodReferenceWriter.write(okRef);
                    goodReferenceWriter.newLine();

                    bibCapRecord.addCitedReferenceString( okRef ) ;
                    countRecordsWitRef.add(id);
                    idOfCurrentFetechedDoc = id;


                }

            }


        }

        //last uppdated record may not have been writen to disk
        bibCapRecordStore.putRecord(idOfCurrentFetechedDoc,bibCapRecord);



        System.out.println("References added to " + countRecordsWitRef.size() + " records in pass 3");
        System.out.println(badRefCount +" reference ignored: noise, in press etc. See badReferences.txt");
        reader.close();
        badReferencesWriter.flush();
        badReferencesWriter.close();

        System.out.println("Getting citation count including/exkluding self cit in pass 4. Also mark if it is a considered article (core journal an at least 1 cited ref) ");

        reader = new BufferedReader(new FileReader(this.citationData));

        Map<String,CitationInformation>  UTtoCitationInformation = new HashMap<>();


        firstline = true;
        while ((line = reader.readLine()) != null) {
            if (firstline) {
                firstline = false;
                continue;
            }


            String[] splitted = TAB.split(line);
            String UT = splitted[0];
            CitationInformation cit = new CitationInformation();
            cit.citIncludingSelf = Integer.valueOf( splitted[1] );
            cit.citExcludingSelf = Integer.valueOf (splitted[3]  );

            UTtoCitationInformation.put(UT, cit);


        }

        reader.close();

        System.out.println(UTtoCitationInformation.size() + " UT:s with citation information read, now matching..");
        HashSet<Integer> countConsideredArticles = new HashSet<>();






        for(Integer key : orderedKeySet ) {

            bibCapRecord = bibCapRecordStore.getRecord(key);
            String UT = bibCapRecord.getUT();

            CitationInformation citationInformation = UTtoCitationInformation.get(UT);

            if(citationInformation == null) {

                bibCapRecord.setConsideredRecord(false);


                //not supported by mvstore
                //entry.setValue(bibCapRecord);

                //not allowed?
                bibCapRecordStore.putRecord(key,bibCapRecord );

            } else {


                bibCapRecord.setCitationsIncSelf( citationInformation.citIncludingSelf );
                bibCapRecord.setCitationsExclSelf( citationInformation.citExcludingSelf);
                bibCapRecord.setConsideredRecord(true);
                //entry.setValue(bibCapRecord);

                //not allowed?
                bibCapRecordStore.putRecord(key, bibCapRecord);

                countConsideredArticles.add( bibCapRecord.internalId );


            }


        }





        System.out.println(countConsideredArticles.size() +" records matched = final data set ");

        System.out.println("total size: " + bibCapRecordStore.size());


        //System.out.println("Writing the whole (considered records) database to a text file..");
        // BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter( new File("ParsedBibCapRecords.txt")));

        // for(Map.Entry<Integer, BibCapRecord> entry : recordHashMap.entrySet()) {

        //     BibCapRecord record = entry.getValue();
        //     if(!record.isConsideredRecord()) continue;

        //     bufferedWriter.write( entry.getValue().toString() );
        //     bufferedWriter.newLine();


        //  }


        // bufferedWriter.flush();
        // bufferedWriter.close();



    }


    private class CitationInformation {

        int citIncludingSelf;
        int citExcludingSelf;


    }
}
