package BibCap;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MockBibCapRecord extends BibCapRecord {

    HashSet<String> subjectCategories = new HashSet<>();

    double citationExclSefLog1p;

    public void addSubCats(List<String> subcats) {

        subjectCategories.addAll(subcats);


    }

    public Set getSubCats() {

        return subjectCategories;
    }

    public void setCitationExclSefLog1p(double citationExclSefLog1p1) {

        this.citationExclSefLog1p = citationExclSefLog1p1;
    }

    public double getCitationExclSefLog1p() {

        return this.citationExclSefLog1p;
    }

    public MockBibCapRecord() { super(); }
}
