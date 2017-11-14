package Misc;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.StringUtils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Ngram {


    public static int countWords(String string) {

        return ( StringUtils.countMatches(string," ") + 1 );
    }


    public static String[] wordNgrams(String s, int len) {
        String[] parts = s.split(" ");
        String[] result = new String[parts.length - len + 1];
        for(int i = 0; i < parts.length - len + 1; i++) {
            StringBuilder sb = new StringBuilder();
            for(int k = 0; k < len; k++) {
                if(k > 0) sb.append(' ');
                sb.append(parts[i+k]);
            }
            result[i] = sb.toString();
        }
        return result;
    }



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


public static List<String> normalizedBeforeNgram(int n, String str) {

        str = str.replaceAll(" ","");

        return ngramsOrInput(n, str);
}


    public static void main(String[] arg) {

        System.out.println( countWords("HELLO") );
        System.out.println( Arrays.toString(Ngram.wordNgrams("HELLO ASS MONGER TESTER",2) ));

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
