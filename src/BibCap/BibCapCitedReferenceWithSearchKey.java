package BibCap;

import java.io.Serializable;
import java.time.Year;
import java.util.Arrays;
import java.util.List;

public class BibCapCitedReferenceWithSearchKey implements Serializable {

    private static String prefix = "s";
    private static String suffix = "e";

    String reference;
    String[] keys;



    //TODO if candidate sets are to big condider +-1 year or even no variation on year

   /*

    A cited reference is represented by 4 compressed keys like this:


   "VANDERBILT, D 1990 PHYS REV B 41 7892"

    S[VA] 199 S[PH]
    S[VA] 199 E[VB]
    E[TD] 199 E[VB]
    E[TD] 199 S[VB]


    "LEE, E 2007 INT J CANCER 120 1046"

    S[LE] 200 S[IN]
    S[LE] 200 E[ER]
    E[EE] 200 E[ER]
    E[EE] 200 S[IN]


    These are then used in an inverted index like structure to generate cadidates for edit distance calculation

    */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BibCapCitedReferenceWithSearchKey that = (BibCapCitedReferenceWithSearchKey) o;

        return reference.equals(that.reference);
    }

    @Override
    public int hashCode() {
        return reference.hashCode();
    }



    public String getCitedRefString() {

        return this.reference;

    }


    public String[] getKeys() {

        return this.keys;
    }

    public BibCapCitedReferenceWithSearchKey(String citedRef, String authorPart, String yearPart, String workPart) {

        boolean makeNoKey = false;
        this.reference = citedRef;

        String author = authorPart.replaceAll("[, ]","");
        if(author.length() < 3) makeNoKey = true;
        if(yearPart.length() < 4) makeNoKey = true;

        String work = workPart.replaceAll("[, ]","");

        if(work.length() < 3) makeNoKey = true;


        if(!makeNoKey) {


            String aPrefix = author.substring(0,2);
            String aSuffix = author.substring(author.length()-2,author.length());

            String truncYear = yearPart.substring(0,yearPart.length()-1);

            String wPrefix = work.substring(0,2);
            String wSuffix = work.substring(work.length()-2,work.length());


            this.keys = new String[4];

            StringBuilder stringBuilder1 = new StringBuilder();

              keys[0] =  stringBuilder1.append(prefix).append(aPrefix).append(truncYear).append(prefix).append(wPrefix).toString();

            StringBuilder stringBuilder2 = new StringBuilder();

            keys[1] =  stringBuilder2.append(prefix).append(aPrefix).append(truncYear).append(suffix).append(wSuffix).toString();

            StringBuilder stringBuilder3 = new StringBuilder();

            keys[2] =  stringBuilder3.append(suffix).append(aSuffix).append(truncYear).append(suffix).append(wSuffix).toString();

            StringBuilder stringBuilder4 = new StringBuilder();

            keys[3] =  stringBuilder4.append(suffix).append(aSuffix).append(truncYear).append(prefix).append(wPrefix).toString();


        } else {


            this.keys = new String[1];

            keys[0] = citedRef;

            //the compressed key will simply be the cited reference as it is and only exact matching will be done in later phases
        }





















    }



    @Override
    public String toString() {


        return this.reference + " || KEYS: " + Arrays.toString(this.getKeys());

    }


    }
