package Misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Ngram {


    public static List<String> ngramsOrInput(int n, String str) {
        List<String> ngrams = new ArrayList<String>();

        for (int i = 0; i < str.length() - n + 1; i++)
            ngrams.add(str.substring(i, i + n));

        //return the string if no ngrams possible
        if(ngrams.size() == 0) {

            return Arrays.asList(str);

        }

        return ngrams;
    }



    public static void main(String[] arg) {

        System.out.println( Ngram.ngramsOrInput(3,"helo"));

    }


    /*


    import java.util.Iterator;
    class NgramIterator implements Iterator<String> {
    private final String str;
    private final int n;
    int pos = 0;
    public NgramIterator(int n, String str) {
        this.n = n;
        this.str = str;
    }
    public boolean hasNext() {
        return pos < str.length() - n + 1;
    }
    public String next() {
        return str.substring(pos, pos++ + n);
    }
}

     */



}
