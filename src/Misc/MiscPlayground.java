package Misc;

public class MiscPlayground {



    public static void main(String[] arg) {


        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();



      // System.out.println(dist);


       System.out.println( levenshteinDistance.getNormalizedLevenshteinSimilarity("SWENY TOD","SWENYTOD",0.89F) );

    }


}
