package BibCap;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by Cristian on 2017-10-16.
 */
public class BibCapRecordStoreInMemory extends HashMap<Integer, BibCapRecord> implements BibCapRecordStore {

    @Override
    public BibCapRecord getRecord(Integer key) {
        return super.get(key);
    }

    @Override
    public void putRecord(Integer key, BibCapRecord record) {

        super.put(key,record);

    }

    @Override
    public int size() {

        return super.size();
    }


    @Override
    public void remove(Integer key) {

        super.remove(key);
    }



    @Override
    public Set<Entry<Integer, BibCapRecord>> entrySetOfRecords() {

        return super.entrySet();

    }
}
