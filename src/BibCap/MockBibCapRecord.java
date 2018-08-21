package BibCap;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MockBibCapRecord extends BibCapRecord {

    HashSet<String> subjectCategories = new HashSet<>();

    public void addSubCats(List<String> subcats) {

        subjectCategories.addAll(subcats);


    }

    public Set getSubCats() {

        return subjectCategories;
    }


    public MockBibCapRecord() { super(); }
}
