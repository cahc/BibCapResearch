package Persistor;

import org.h2.mvstore.MVStore;

import java.io.File;

/**
 * Created by crco0001 on 10/24/2017.
 */
public class runCompactOnMVStore {


    public static void main(String[] arg) {


        if(arg.length != 1) {  System.out.println("Supply name of MVstore DB"); System.exit(0); }

        File check = new File(arg[0]);

        if(!check.exists()) { System.out.println("File dosent exist"); System.exit(0); }



        MVStore store = new MVStore.Builder().cacheSize(200). // 200MB read cache
                fileName(arg[0]).autoCommitBufferSize(1024). // 1MB write cache
                open(); // autoCommitBufferSize
        store.setVersionsToKeep(0);
        store.setReuseSpace(true);

        System.out.println("Running compactMoveChunks..");
        store.compactMoveChunks();
        store.close();
        System.out.println("Done");

    }


}
