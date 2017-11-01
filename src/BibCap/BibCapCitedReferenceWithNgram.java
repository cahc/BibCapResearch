package BibCap;

import java.io.Serializable;
import java.util.List;

public class BibCapCitedReferenceWithNgram implements Serializable {


    String reference;
    List<String> ngramFromAuthor;
    List<String> ngramFromWork;


    public void setNgramFromAuthor(List<String> ngramFromAuthor) {
        this.ngramFromAuthor = ngramFromAuthor;
    }

    public void setNgramFromWork(List<String> ngramFromWork) {
        this.ngramFromWork = ngramFromWork;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public List<String> getNgramFromAuthor() {
        return ngramFromAuthor;
    }

    public void addNgramFromAuthor(String ngramFromAuthor) {
        this.ngramFromAuthor.add(ngramFromAuthor);
    }

    public List<String> getNgramFromWork() {
        return ngramFromWork;
    }

    public void addNgramFromWork(String ngramFromWork) {
        this.ngramFromWork.add(ngramFromWork);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BibCapCitedReferenceWithNgram that = (BibCapCitedReferenceWithNgram) o;

        return reference.equals(that.reference);
    }

    @Override
    public int hashCode() {
        return reference.hashCode();
    }


    @Override
    public String toString() {
        return "BibCapCitedReferenceWithNgram{" +
                "reference='" + reference + '\'' +
                ", ngramFromAuthor=" + ngramFromAuthor +
                ", ngramFromWork=" + ngramFromWork +
                '}';
    }
}
