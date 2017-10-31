package Misc;

public class MiscPlayground {



    public static void main(String[] arg) {


        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();

       System.out.println( levenshteinDistance.getNormalizedLevenshteinSimilarity("The stupid doggy","the stupid dog",0.80) );

        System.out.println( levenshteinDistance.isAboveSimilarityThreshold("The stupid doggy","the stupid dog",0.85) );

    }


}
