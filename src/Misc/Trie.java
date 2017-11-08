package Misc;

import edu.princeton.cs.algs4.TrieSET;

/**
 * Created by crco0001 on 11/6/2017.
 */
public class Trie {


    public static void main(String[] arg) {

        TrieSET set = new TrieSET();

        set.add("apanola");
        set.add("apanb");
        set.add("lever");


        Iterable<String> matches = set.keysThatMatch("apan..");


        for(String s : matches ) {


            System.out.println(s);


        }





    }

}
