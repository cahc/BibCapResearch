package BibCap;

import java.util.Map;
import java.util.Set;

/**
 * Created by Cristian on 2017-10-16.
 */
public interface BibCapRecordStore {

    void putRecord(Integer key, BibCapRecord record);

    BibCapRecord getRecord(Integer key);

    int size();

    Set<Map.Entry<Integer, BibCapRecord>> entrySetOfRecords();

}
