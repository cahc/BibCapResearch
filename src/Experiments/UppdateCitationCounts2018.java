package Experiments;

import BibCap.BibCapRecord;
import BibCap.MockBibCapRecord;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.type.ObjectDataType;

import java.io.*;
import java.util.*;

/**
 * Created by crco0001 on 9/10/2018.
 */
public class UppdateCitationCounts2018 {


    public class UTtoCIT {

        String UT;
        int citIncSelfCit;
        int citExlSelfCit;


        public String getUT() {
            return UT;
        }

        public void setUT(String UT) {
            this.UT = UT;
        }

        public int getCitIncSelfCit() {
            return citIncSelfCit;
        }

        public void setCitIncSelfCit(int citIncSelfCit) {
            this.citIncSelfCit = citIncSelfCit;
        }

        public int getCitExlSelfCit() {
            return citExlSelfCit;
        }

        public void setCitExlSelfCit(int citExlSelfCit) {
            this.citExlSelfCit = citExlSelfCit;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UTtoCIT uTtoCIT = (UTtoCIT) o;

            return UT != null ? UT.equals(uTtoCIT.UT) : uTtoCIT.UT == null;
        }

        @Override
        public int hashCode() {
            return UT != null ? UT.hashCode() : 0;
        }
    }

    public static void main(String[] arg) throws IOException {


        MVStore store = new MVStore.Builder().cacheSize(200). // 200MB read cache
                fileName("E:\\RESEARCH2018\\mappy.db").autoCommitBufferSize(1024). // 1MB write cache
                open(); // autoCommitBufferSize
        store.setVersionsToKeep(0);
        store.setReuseSpace(true);

        MVMap<Integer, BibCapRecord> map = store.openMap("mymap", new MVMap.Builder<Integer, BibCapRecord>().keyType(new ObjectDataType()).valueType(new BibCapRecord()));

        HashMap<String, UTtoCIT> uTtoNewCitationCounts2018 = new HashMap<>();



        //uppdate UT with new citation information
        BufferedReader reader = new BufferedReader( new FileReader( new File("E:\\RESEARCH2018\\NYA_CITERINGSVÄRDEN_2018\\cit_class_au.txt")));

        String line2;
        boolean firstLine = true;
        while(  (line2 = reader.readLine()) != null   ) {

            if(firstLine) {firstLine=false; continue;}

            String[] parts = line2.split("\t");

            UTtoCIT uTtoCIT = new UppdateCitationCounts2018().new UTtoCIT();

            uTtoCIT.setUT( parts[0].trim() );
            uTtoCIT.setCitIncSelfCit(Integer.valueOf(parts[1]) );
            uTtoCIT.setCitExlSelfCit(Integer.valueOf(parts[2]));

            uTtoNewCitationCounts2018.put(uTtoCIT.getUT(), uTtoCIT);


        }

        reader.close();
        System.out.println("new mappings: " + uTtoNewCitationCounts2018.size());


        for(Map.Entry<Integer,BibCapRecord> entry : map.entrySet()) {


            int keyInMap = entry.getKey();

            BibCapRecord bibCapRecord = entry.getValue();

            UTtoCIT newCit = uTtoNewCitationCounts2018.get(bibCapRecord.getUT());




        }

        store.close();




    }



}
