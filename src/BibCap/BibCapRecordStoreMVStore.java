package BibCap;

import Persistor.FooObject;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.type.ObjectDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by crco0001 on 10/23/2017.
 */
public class BibCapRecordStoreMVStore implements BibCapRecordStore {


    private MVStore store;
    private MVMap<Integer, BibCapRecord> map;


    public void saveChanges()
    {

        store.commit();


    }


    public void compact() {

        store.compactMoveChunks();
    }


    public void close() {


        store.close();
    }


    public BibCapRecordStoreMVStore() {


        store = new MVStore.Builder().cacheSize(200). // 200MB read cache
                fileName("mappy.db").autoCommitBufferSize(1024). // 1MB write cache
                open(); // autoCommitBufferSize
        store.setVersionsToKeep(0);
        store.setReuseSpace(true);


        map = store.openMap("mymap", new MVMap.Builder<Integer,BibCapRecord>().keyType(new ObjectDataType()).valueType( new BibCapRecord() ));

    }



    @Override
    public void putRecord(Integer key, BibCapRecord record) {

       map.put(key, record);

    }

    @Override
    public BibCapRecord getRecord(Integer key) {


        return map.get(key);
    }


    @Override
    public void remove(Integer key) {

        map.remove(key);

    }

    @Override
    public int size() {

        return map.size();
    }

    @Override
    public Set<Map.Entry<Integer, BibCapRecord>> entrySetOfRecords() {


        return map.entrySet();

    }
}
