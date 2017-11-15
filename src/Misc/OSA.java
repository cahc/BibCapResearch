package Misc;

/// Computes and returns the Damerau-Levenshtein edit distance between two strings,
/// i.e. the number of insertion, deletion, sustitution, and transposition edits
/// required to transform one string to the other. This value will be >= 0, where 0
/// indicates identical strings. Comparisons are case sensitive, so for example,
/// "Fred" and "fred" will have a distance of 1. This algorithm is basically the
/// Levenshtein algorithm with a modification that considers transposition of two
/// adjacent characters as a single edit.
/// http://blog.softwx.net/2015/01/optimizing-damerau-levenshtein_15.html
/// </summary>
/// <remarks>See http://en.wikipedia.org/wiki/Damerau%E2%80%93Levenshtein_distance
/// Note that this is based on Sten Hjelmqvist's "Fast, memory efficient" algorithm, described
/// at http://www.codeproject.com/Articles/13525/Fast-memory-efficient-Levenshtein-algorithm.
/// This version differs by including some optimizations, and extending it to the Damerau-
/// Levenshtein algorithm.
/// Note that this is the simpler and faster optimal string alignment (aka restricted edit) distance
/// that difers slightly from the classic Damerau-Levenshtein algorithm by imposing the restriction
/// that no substring is edited more than once. So for example, "CA" to "ABC" has an edit distance
/// of 2 by a complete application of Damerau-Levenshtein, but a distance of 3 by this method that
/// uses the optimal string alignment algorithm. See wikipedia article for more detail on this
/// distinction.
/// </remarks>
/// <param name="s">String being compared for distance.</param>
/// <param name="t">String being compared against other string.</param>
/// <param name="maxDistance">The maximum edit distance of interest.</param>
/// <returns>int edit distance, >= 0 representing the number of edits required
/// to transform one string to the other, or -1 if the distance is greater than the specified maxDistance.</returns>

import static java.lang.Math.abs;

public class OSA {

    public static int DamLev(String s, String t, int maxDistance) {


        //if (String.IsNullOrEmpty(s)) return (t ? ? "").Length;
        //if (String.IsNullOrEmpty(t)) return s.Length;

        // if strings of different lengths, ensure shorter string is in s. This can result in a little
        // faster speed by spending more time spinning just the inner loop during the main processing.

        if (s.length() > t.length()) {
            String temp = s;
            s = t;
            t = temp; // swap s and t
        }

        int sLen = s.length(); // this is also the minimun length of the two strings
        int tLen = t.length();


        if (abs(sLen - tLen) > maxDistance) return -1;



        // suffix common to both strings can be ignored
        while ((sLen > 0) && (s.charAt(sLen - 1) == t.charAt(tLen - 1))) {
            sLen--;
            tLen--;
        }

        int start = 0;

        if ((s.charAt(0) == t.charAt(0)) || (sLen == 0)) { // if there's a shared prefix, or all s matches t's suffix
            // prefix common to both strings can be ignored
            while ((start < sLen) && (s.charAt(start) == t.charAt(start))) start++;
            sLen -= start; // length of the part excluding common prefix and suffix
            tLen -= start;

            // if all of shorter string matches prefix and/or suffix of longer string, then
            // edit distance is just the delete of additional characters present in longer string
            if (sLen == 0) return tLen;

            /*



            Substring behave differently in C# and java..



             */


            t = t.substring(start, start+tLen); // faster than t[start+j] in inner loop below
        }
        int lenDiff = tLen - sLen;
        if ((maxDistance < 0) || (maxDistance > tLen)) {
            maxDistance = tLen;
        } else if (lenDiff > maxDistance) return -1;

        int[] v0 = new int[tLen];
        int[] v2 = new int[tLen]; // stores one level further back (offset by +1 position)
        int j;
        for (j = 0; j < maxDistance; j++) v0[j] = j + 1;
        for (; j < tLen; j++) v0[j] = maxDistance + 1;

        int jStartOffset = maxDistance - (tLen - sLen);
        boolean haveMax = maxDistance < tLen;
        int jStart = 0;
        int jEnd = maxDistance;
        char sChar = s.charAt(0);
        int current = 0;
        for (int i = 0; i < sLen; i++) {
            char prevsChar = sChar;
            sChar = s.charAt(start + i);
            char tChar = t.charAt(0);
            int left = i;
            current = left + 1;
            int nextTransCost = 0;
            // no need to look beyond window of lower right diagonal - maxDistance cells (lower right diag is i - lenDiff)
            // and the upper left diagonal + maxDistance cells (upper left is i)
            jStart += (i > jStartOffset) ? 1 : 0;
            jEnd += (jEnd < tLen) ? 1 : 0;
            for (j = jStart; j < jEnd; j++) {
                int above = current;
                int thisTransCost = nextTransCost;
                nextTransCost = v2[j];
                v2[j] = current = left; // cost of diagonal (substitution)
                left = v0[j];    // left now equals current cost (which will be diagonal at next iteration)
                char prevtChar = tChar;
                tChar = t.charAt(j);
                if (sChar != tChar) {
                    if (left < current) current = left;   // insertion
                    if (above < current) current = above; // deletion
                    current++;
                    if ((i != 0) && (j != 0)
                            && (sChar == prevtChar)
                            && (prevsChar == tChar)) {
                        thisTransCost++;
                        if (thisTransCost < current) current = thisTransCost; // transposition
                    }
                }
                v0[j] = current;
            }
            if (haveMax && (v0[i + lenDiff] > maxDistance)) return -1;
        }
        return (current <= maxDistance) ? current : -1;
    }

    public static int DamLev(String s, int s_len, String t, int t_len, int maxDistance) {

        if (abs(s_len - t_len) > maxDistance) return -1;

        //if (String.IsNullOrEmpty(s)) return (t ? ? "").Length;
        //if (String.IsNullOrEmpty(t)) return s.Length;

        // if strings of different lengths, ensure shorter string is in s. This can result in a little
        // faster speed by spending more time spinning just the inner loop during the main processing.

        if (s_len > t_len ) {
            String temp = s;
            s = t;
            t = temp; // swap s and t
        }

        int sLen = s.length(); // this is also the minimun length of the two strings
        int tLen = t.length();


        // suffix common to both strings can be ignored
        while ((sLen > 0) && (s.charAt(sLen - 1) == t.charAt(tLen - 1))) {
            sLen--;
            tLen--;
        }

        int start = 0;

        if ((s.charAt(0) == t.charAt(0)) || (sLen == 0)) { // if there's a shared prefix, or all s matches t's suffix
            // prefix common to both strings can be ignored
            while ((start < sLen) && (s.charAt(start) == t.charAt(start))) start++;
            sLen -= start; // length of the part excluding common prefix and suffix
            tLen -= start;

            // if all of shorter string matches prefix and/or suffix of longer string, then
            // edit distance is just the delete of additional characters present in longer string
            if (sLen == 0) return tLen;

            /*



            Substring behave differently in C# and java..



             */


            t = t.substring(start, start+tLen); // faster than t[start+j] in inner loop below
        }
        int lenDiff = tLen - sLen;
        if ((maxDistance < 0) || (maxDistance > tLen)) {
            maxDistance = tLen;
        } else if (lenDiff > maxDistance) return -1;

        int[] v0 = new int[tLen];
        int[] v2 = new int[tLen]; // stores one level further back (offset by +1 position)
        int j;
        for (j = 0; j < maxDistance; j++) v0[j] = j + 1;
        for (; j < tLen; j++) v0[j] = maxDistance + 1;

        int jStartOffset = maxDistance - (tLen - sLen);
        boolean haveMax = maxDistance < tLen;
        int jStart = 0;
        int jEnd = maxDistance;
        char sChar = s.charAt(0);
        int current = 0;
        for (int i = 0; i < sLen; i++) {
            char prevsChar = sChar;
            sChar = s.charAt(start + i);
            char tChar = t.charAt(0);
            int left = i;
            current = left + 1;
            int nextTransCost = 0;
            // no need to look beyond window of lower right diagonal - maxDistance cells (lower right diag is i - lenDiff)
            // and the upper left diagonal + maxDistance cells (upper left is i)
            jStart += (i > jStartOffset) ? 1 : 0;
            jEnd += (jEnd < tLen) ? 1 : 0;
            for (j = jStart; j < jEnd; j++) {
                int above = current;
                int thisTransCost = nextTransCost;
                nextTransCost = v2[j];
                v2[j] = current = left; // cost of diagonal (substitution)
                left = v0[j];    // left now equals current cost (which will be diagonal at next iteration)
                char prevtChar = tChar;
                tChar = t.charAt(j);
                if (sChar != tChar) {
                    if (left < current) current = left;   // insertion
                    if (above < current) current = above; // deletion
                    current++;
                    if ((i != 0) && (j != 0)
                            && (sChar == prevtChar)
                            && (prevsChar == tChar)) {
                        thisTransCost++;
                        if (thisTransCost < current) current = thisTransCost; // transposition
                    }
                }
                v0[j] = current;
            }
            if (haveMax && (v0[i + lenDiff] > maxDistance)) return -1;
        }
        return (current <= maxDistance) ? current : -1;











    }


    public static double DamuLevSim(String s, String t, double similarityThreshold) {

        double x = 1-similarityThreshold;

        int s_len = s.length();
        int t_len = t.length();

        int max = Math.max(s_len, t_len );

        //threshold for similarity to be >= similarityThreshold
        int threshold =  (int)(Math.ceil(x*max));

        int edits =  DamLev(s,s_len,t, t_len,threshold);

        if(edits == -1 || edits >= threshold) return -1;

        return 1-(edits/(double)max);


    }

    public static void main(String[] arg) {


       String s1 =  "SHELDRICK, G 2008 ACTA CRYSTALLOGR A 64 A112";
        String s2 =  "SHELDRICK, G 2008 ACTA CRYSTALLOGR A 64 211";
        String s3 =  "SHELDRICK, G 2008 ACTA CRYSTALLOGR A 64 122";

        String orig = "SHELDRICK, G 2008 ACTA CRYSTALLOGR A 1 64 112";

        System.out.println( OSA.DamLev(s2,orig,5) );

        System.out.println( OSA.DamuLevSim(s2,orig,0.90) );

    }


}