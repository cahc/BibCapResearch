package BibCap;

import org.h2.mvstore.DataUtils;
import org.h2.mvstore.WriteBuffer;
import org.h2.mvstore.type.DataType;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by crco0001 on 10/11/2017.
 */
public class BibCapRecord implements Serializable, DataType, Comparable<BibCapRecord> {

    //used just in parsing phase
    int internalId;
    boolean consideredRecord;

    StringBuilder accumulateAbstractText = new StringBuilder();
    String title;

    String abstractText;

    String UT;

    String source;

    List<BibCapCitedReferenceWithSearchKey> bibCapCitedReference = new ArrayList<>();
    List<String> extractedTerms = new ArrayList<>();

    List<String> simpleBagOfWordTokens = new ArrayList<>(); //IMPLEMENT THIS

    int citationsIncSelf;
    int citationsExclSelf;


    int clusterL1;
    int clusterL2;
    int clusterL3;
    int clusterL4;

    String firstAuthor;

    public int getClusterL1() {
        return clusterL1;
    }

    public void setClusterL1(int clusterL1) {
        this.clusterL1 = clusterL1;
    }

    public int getClusterL2() {
        return clusterL2;
    }

    public void setClusterL2(int clusterL2) {
        this.clusterL2 = clusterL2;
    }

    public int getClusterL3() {
        return clusterL3;
    }

    public void setClusterL3(int clusterL3) {
        this.clusterL3 = clusterL3;
    }

    public int getClusterL4() {
        return clusterL4;
    }

    public void setClusterL4(int clusterL4) {
        this.clusterL4 = clusterL4;
    }

    public String getFirstAuthor() {
        return firstAuthor;
    }

    public void setFirstAuthor(String firstAuthor) {
        this.firstAuthor = firstAuthor;
    }

    List<String> subjectCategories;

    public BibCapRecord() {}


    @Override
    public String toString() {


        return internalId +"\t" + UT +"\t" + title +"\t" +abstractText + "\t" + this.getNrCitedReferences() + "\t" + bibCapCitedReference +"\t" + source +"\t" +citationsIncSelf +"\t" + citationsExclSelf + "\t" +extractedTerms +"\t" + subjectCategories +"\t" + simpleBagOfWordTokens +"\t" + clusterL1 +"\t" + clusterL2 +"\t" + clusterL3 +"\t" + clusterL4 +"\t" + firstAuthor;

    }

    public List<String> getSubjectCategories() {
        return subjectCategories;
    }

    public void addSubjectCategories(String rawSubjectCategoriesString) {

        String[] subcats = rawSubjectCategoriesString.split(";");
        this.subjectCategories = new ArrayList<>(2);

            for(String s: subcats) {

                String subcat = s.trim().toLowerCase();
                this.subjectCategories.add(subcat);

            }

        }


        public void setSubjectCategories(List<String> subjectCategories) {

        this.subjectCategories = subjectCategories;

        }


    public void addExtractedTerm(String term) {

        this.extractedTerms.add(term);
    }

    public void addAllExtractedTerms(Collection<String> terms) {

        this.extractedTerms.addAll(terms);

    }


    public void addAllSimpleBagOfWordTerms(Collection<String> terms) {

        this.simpleBagOfWordTokens.addAll(terms);

    }


    public List<String> getExtractedTerms() {

        return this.extractedTerms;
    }

    public List<String> getExtractedSimpleBagOfWordTokens() {

        return this.simpleBagOfWordTokens;
    }



    public int getCitationsIncSelf() {
        return citationsIncSelf;
    }

    public void setCitationsIncSelf(int citationsIncSelf) {
        this.citationsIncSelf = citationsIncSelf;
    }

    public int getCitationsExclSelf() {
        return citationsExclSelf;
    }

    public void setCitationsExclSelf(int citationsExclSelf) {
        this.citationsExclSelf = citationsExclSelf;
    }



    public List<BibCapCitedReferenceWithSearchKey> getCitedReferences() {

        return bibCapCitedReference;

    }

    public boolean isConsideredRecord() {
        return consideredRecord;
    }

    public void setConsideredRecord(boolean consideredRecord) {
        this.consideredRecord = consideredRecord;
    }

    public void addPartOfAbstract(String s) {

        if(accumulateAbstractText.length() == 0) { accumulateAbstractText.append(s); } else {

            accumulateAbstractText.append(" ").append(s);
        }

    }

    public void addCitedReference(BibCapCitedReferenceWithSearchKey s) {

        bibCapCitedReference.add(s);
    }

    public int getNrCitedReferences() {

        return this.bibCapCitedReference.size();
    }


    public void createFullAbstract() {


        this.abstractText = this.accumulateAbstractText.toString();
        this.accumulateAbstractText = null;


    }

    public String getAbstractText() {

        return abstractText;
    }




    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getInternalId() {
        return internalId;
    }

    public void setInternalId(int internalId) {
        this.internalId = internalId;
    }

    public String getUT() {
        return UT;
    }

    public void setUT(String UT) {
        this.UT = UT;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BibCapRecord that = (BibCapRecord) o;

        if (internalId != that.internalId) return false;
        if (!title.equals(that.title)) return false;
        return UT.equals(that.UT);
    }

    @Override
    public int hashCode() {
        int result = internalId;
        result = 31 * result + title.hashCode();
        result = 31 * result + UT.hashCode();
        return result;
    }



    @Override
    public int compareTo(BibCapRecord other) {


        if(this.internalId < other.internalId) return -1;
        if(this.internalId > other.internalId) return 1;
        if(this.internalId == other.internalId) return 0;
        return (this.UT.compareTo(other.UT));



    }

    ///////////////////DataType overid methods//////////////////
    @Override
    public int compare(Object a, Object b) {

       return  ((BibCapRecord)a).compareTo( (BibCapRecord)b  );

    }

    @Override
    public int getMemory(Object obj) {

        return 10000; // todo fix this estimate
    }

    @Override
    public void write(WriteBuffer buff, Object obj) {

        byte[] serialized = BibCapRecordSerializer.getBytes( (BibCapRecord) obj );
        buff.putVarInt(serialized.length).put(serialized);



    }

    @Override
    public void write(WriteBuffer buff, Object[] obj, int len, boolean key) {

        for(int i = 0; i < len; ++i) {
            this.write(buff, obj[i]);
        }


    }

    @Override
    public Object read(ByteBuffer buff) {

        int length = DataUtils.readVarInt(buff);

        byte[] serialized = new byte[length];


        for(int i=0; i<length; i++) {

            serialized[i] = buff.get();


        }

        return BibCapRecordSerializer.getObject(serialized);



    }

    @Override
    public void read(ByteBuffer buff, Object[] obj, int len, boolean key) {

        for(int i = 0; i < len; ++i) {
            obj[i] = this.read(buff);
        }


    }


    ///////////////////DataType overid methods&//////////////////

}