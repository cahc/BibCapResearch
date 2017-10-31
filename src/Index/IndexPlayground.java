package Index;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * Created by crco0001 on 10/31/2017.
 */
public class IndexPlayground {


    public static void main(String[] arg) {


        Int2IntOpenHashMap map = new Int2IntOpenHashMap();
        map.defaultReturnValue(0);


        System.out.println( map.get(2) );

        map.addTo(2,1);

        System.out.println(map.get(2));



    }


}
