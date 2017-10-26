package BibCap;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.type.ObjectDataType;

import java.io.File;

/**
 * Created by crco0001 on 10/26/2017.
 */
public class PlayGroundBibCapReader {



    public static void main(String[] arg) {


        if(arg.length != 1) {  System.out.println("Supply name of MVstore DB"); System.exit(0); }

        File check = new File(arg[0]);

        if(!check.exists()) { System.out.println("File dosent exist"); System.exit(0); }


        MVStore store = new MVStore.Builder().cacheSize(200). // 200MB read cache
                fileName(arg[0]).autoCommitBufferSize(1024). // 1MB write cache
                open(); // autoCommitBufferSize
        store.setVersionsToKeep(0);
        store.setReuseSpace(true);

        MVMap<Integer, BibCapRecord> map = store.openMap("mymap", new MVMap.Builder<Integer,BibCapRecord>().keyType(new ObjectDataType()).valueType( new BibCapRecord() ));


      System.out.println("Records in store: # " + map.size() );





    }










}
