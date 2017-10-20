package BibCap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by crco0001 on 10/11/2017.
 */
public class BibCapRecord implements Serializable{

    //used just in parsing phase
    int internalId;
    boolean consideredRecord;

    StringBuilder accumulateAbstractText = new StringBuilder();
    String title;

    String abstractText;

    String UT;

    String source;

    List<String> citedReferences = new ArrayList<>();
    List<String> extractedTerms = new ArrayList<>();

    int citationsIncSelf;
    int citationsExclSelf;

    public BibCapRecord() {}


    @Override
    public String toString() {


        return internalId +"\t" + UT +"\t" + title +"\t" +abstractText + "\t" +citedReferences +"\t" + source +"\t" +citationsIncSelf +"\t" + citationsExclSelf;

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

    public List<String> getCitedReferences() {

        return citedReferences;

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

    public void addCitedReferenceString(String s) {

        citedReferences.add(s);
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
}