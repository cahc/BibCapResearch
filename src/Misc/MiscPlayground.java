package Misc;

public class MiscPlayground {



    public static void main(String[] arg) {


        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();

       System.out.println( levenshteinDistance.getNormalizedLevenshteinSimilarity("The stupid doggy","the stupid dog",0.80) );

        System.out.println( levenshteinDistance.isAboveSimilarityThreshold("The stupid doggy","the stupid dog",0.85,true) );



        String s = "MAIR, RJ";

       System.out.println(  s.indexOf(',') );

       System.out.println( s.substring(0,7)  );


       String authorPart = "ab, gris,p p";


        String author = authorPart.replaceAll("[, ]","");


        System.out.println(author.substring(author.length()-2,author.length()));
    }


}
