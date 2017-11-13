package Index;

import edu.princeton.cs.algs4.TST;

public class TSTindex {


    public static void main(String[] arg) {

        TST<Integer> hej = new TST();

        hej.put("hello", 100);
        hej.put("hello", 12);
        hej.put("hello1",2);


        System.out.println( hej.get("hello") );





    }


}
