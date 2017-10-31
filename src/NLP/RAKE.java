package NLP;

import Misc.RemoveCopyRightFromAbstract;
import NLP.RDRPOSTagger.InitialTagger;
import NLP.RDRPOSTagger.RDRPOSTagger;
import NLP.RDRPOSTagger.Utils;
import NLP.RDRPOSTagger.WordTag;
import NLP.Stemmer.UEALite;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by crco0001 on 10/26/2017.

 TYPE	FREQ_EXAMPLE	PERCENT	INFO
 '      25	    0.0%    DELIM
 ''	    1882	0.2%	DELIM
 $	    7	    0.0%	DELIM
 (	    1605	0.2%	DELIM
 )	    14127	1.4%	DELIM
 ,	    35370	3.4%	DELIM
 .	    44052	4.2%	DELIM
 :	    9711	0.9%	DELIM
 CC	    38014	3.7%	Coordinating conjunction
 CD	    35011	3.4%	Cardinal number
 DT	    86921	8.4%	Determiner
 EX	    1032	0.1%	Existential there
 FW	    543	    0.1%	Foreign word
 IN	    125819	12.1%	Preposition or subordinating conjunction
 JJ	    102791	9.9%	Adjective
 JJR	3564	0.3%	Adjective, comparative
 JJS	1146	0.1%	Adjective, superlative
 LS	    332	    0.0%	List item marker
 MD	    5093	0.5%	Modal
 NN	    202143	19.5%	Noun, singular or mass
 NNP	53397	5.1%	Proper noun, singular
 NNPS	1177	0.1%	Proper noun, plural
 NNS	86450	8.3%	Noun, plural
 PDT	181	    0.0%	Predeterminer
 POS	3	    0.0%	Possessive ending
 PRP	7273	0.7%	Personal pronoun
 PRP$	4135	0.4%	Possessive pronoun
 RB	    21265	2.0%	Adverb
 RBR	1415	0.1%	Adverb, comparative
 RBS	548	    0.1%	Adverb, superlative
 RP	    574	    0.1%	Particle
 SYM	2375	0.2%	Symbol
 TO	    19675	1.9%	to
 UH	    8	    0.0%	Interjection
 VB	    19639	1.9%	Verb, base form
 VBD	23108	2.2%	Verb, past tense
 VBG	17909	1.7%	Verb, gerund or present participle
 VBN	32271	3.1%	Verb, past participle
 VBP	13053	1.3%	Verb, non-3rd person singular present
 VBZ	16971	1.6%	Verb, 3rd person singular present
 WDT	4033	0.4%	Wh-determiner
 WP	    1321	0.1%	Wh-pronoun
 WP$	110	    0.0%	Possessive wh-pronoun
 WRB	2198	0.2%	Wh-adverb


 */

public class RAKE {


    private static boolean isEngLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');

    }



    private static boolean isConsideredWord(String s) {

        int N = s.length();

        if(N < 3 ) return false;

        if(s.charAt(0) == '-' ) return false;
        if(s.charAt(N -1 ) == '-') return false;

        //check "alternative" length

        int N2 = 0;
        for(int i=0; i<N; i++ ) {

            if( isEngLetter(  s.charAt(i)  ) ) N2++;

            if(N2 >= 3) return true;


        }


        return false;
    }



    private static final String space = " ";

    private final static String whiteSpaceNormalize(String input) {

        return StringUtils.normalizeSpace(input);
    }

    private final static Set<String> phraseAndWordDelimiterSet;

    static {

        phraseAndWordDelimiterSet = new HashSet<>(30);

        phraseAndWordDelimiterSet.add(".");
        phraseAndWordDelimiterSet.add(",");
        phraseAndWordDelimiterSet.add("/");
        phraseAndWordDelimiterSet.add("\\");
        phraseAndWordDelimiterSet.add("!");
        phraseAndWordDelimiterSet.add("?");
        phraseAndWordDelimiterSet.add("{");
        phraseAndWordDelimiterSet.add("}");
        phraseAndWordDelimiterSet.add("[");
        phraseAndWordDelimiterSet.add("]");
        phraseAndWordDelimiterSet.add(";");
        phraseAndWordDelimiterSet.add(":");
        phraseAndWordDelimiterSet.add("(");
        phraseAndWordDelimiterSet.add(")");
        phraseAndWordDelimiterSet.add("_");
        phraseAndWordDelimiterSet.add("@");
        phraseAndWordDelimiterSet.add(" ");
        phraseAndWordDelimiterSet.add("\"");

    }

    private final static Set<String> POStagsToIgnore;

    static {


    //     VB	    19639	1.9%	Verb, base form
    //     VBD	23108	2.2%	Verb, past tense
    //     VBG	17909	1.7%	Verb, gerund or present participle
    //     VBN	32271	3.1%	Verb, past participle
    //     VBP	13053	1.3%	Verb, non-3rd person singular present
    //     VBZ	16971	1.6%	Verb, 3rd person singular present

     //   RB	    21265	2.0%	Adverb
     //   RBR	1415	0.1%	Adverb, comparative
     //   RBS	548	    0.1%	Adverb, superlative


        POStagsToIgnore = new HashSet<>(15);

        POStagsToIgnore.add("VB");
        POStagsToIgnore.add("VBD");
        POStagsToIgnore.add("VBG");
        POStagsToIgnore.add("VBN");
        POStagsToIgnore.add("VBP");
        POStagsToIgnore.add("VBZ");

        POStagsToIgnore.add("RB");
        POStagsToIgnore.add("RBR");
        POStagsToIgnore.add("RBS");

    }


    private final static Pattern phraseAndWordDelimiter = Pattern.compile("[\\\"\\.\\/\\\\,\\!\\?\\{\\}\\[\\]\\;\\:\\(\\)\\_\\@\\ ]+");

    private final static RDRPOSTagger rdrposTagger;

    static {

        rdrposTagger = new RDRPOSTagger();

       // URL url = RAKE.class.getResource("/NLP/RDRPOSTagger/Models/English.RDR");
        InputStream in = RAKE.class.getResourceAsStream("/NLP/RDRPOSTagger/Models/English.RDR" );

       // System.out.println("THIS1: " + url.getPath() );
        //URL url = rdrposTagger.getClass().getResource("/NLP/RDRPOSTagger/Models/English.RDR");

    //    File file = new File(url.getFile());

        try {
            rdrposTagger.constructTreeFromRulesFile(in);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static List<String> nNgram(String term) {


        List<String> ngrams = new ArrayList();
        String[] individualWords  =  term.split(" ");

        if(individualWords.length <= 2) return Arrays.asList(term);

        //else

        for(int i=0; i<(individualWords.length-1); i++) {

            StringBuilder stringBuilder = new StringBuilder();

            ngrams.add(  stringBuilder.append( individualWords[i] ).append(space).append( individualWords[i+1] ).toString()   );


        }

        return ngrams;
    }

    private final static HashMap<String, String> FREQDICT;

    static {

        InputStream in = RAKE.class.getResourceAsStream("/NLP/RDRPOSTagger/Models/English.DICT" );



        //URL url = rdrposTagger.getClass().getResource("/NLP/RDRPOSTagger/Models/English.DICT");



        FREQDICT = Utils.getDictionary(in);

    }
    public void getTokens(String input) {

        List<String> tokens = Tokenizer(input);

        for(String s : tokens) {

            System.out.println(s);

        }
    }

    public List<String> getKeyWords(String input, boolean cleanAbstractFromCopyright, Set<String> stopword, UEALite stemmer) {

        if(cleanAbstractFromCopyright) {

           input = RemoveCopyRightFromAbstract.cleanedAbstract(input);

        }

        List<String> tokens = Tokenizer(input);

        //pre tagg
        List<WordTag> wordtags = InitialTagger.SimplePreTaggerForEnglish(this.FREQDICT, tokens);


        //post tagg

        List<String> finaltags = this.rdrposTagger.finalTags(wordtags);

        //lowercase

        ListIterator<String > it = tokens.listIterator();

        while (it.hasNext()) {

            String token = it.next();

            it.set(  stemmer.stem(token.toLowerCase()).getWord()  );

        }

        //indicate if a stopword from list, a POS-tag not considered or not a minimum a 3-char a-z String
        boolean[] skipToken = new boolean[tokens.size()];

        for(int i=0; i < skipToken.length; i++) {

            boolean ignoreThisToken = stopword.contains( tokens.get(i) );

            if(ignoreThisToken) {

                skipToken[ i ] = true;

            } else if(RAKE.phraseAndWordDelimiterSet.contains( tokens.get(i) )) {

                skipToken[ i ] = true;

            } else if(  RAKE.POStagsToIgnore.contains(  finaltags.get(i)  )) {

                skipToken[i] = true;
            } else if( !isConsideredWord(tokens.get(i))  ) {

                skipToken[i] = true;
            }


        }


        //now build potential key words and phrases

        List<String> keywords = new ArrayList<>();

        for(int i=0; i<skipToken.length; i++) {


            if(skipToken[i]) {

                continue;
            } else {

                StringBuilder term = new StringBuilder();

                boolean firstTerm = true;

                while(i <skipToken.length) {

                    if(skipToken[i]) break;
                    if(!firstTerm) term.append(space);
                    term.append( tokens.get(i) );
                    firstTerm = false;
                    i++;
                }


                String finalTerm = term.toString();
                if(finalTerm.length() > 2) {

                    keywords.add(finalTerm);
                }


            }


        }


   //for (int i=0; i< tokens.size(); i++ ) System.out.println( tokens.get(i) + " " + skipToken[i] + " " + finaltags.get(i));

    return keywords;

    }

    public Set<String> loadStopWordList(boolean stemWordsFromStopList) throws IOException {

        Set<String> stopwords = new HashSet<String>();

        InputStream  in = this.getClass().getResourceAsStream("/NLP/StopLists/fox.txt");

        BufferedReader reader = new BufferedReader(new InputStreamReader( in ));

        String line;
        while(  (line = reader.readLine() )  != null          ) {

            if(line.startsWith("#")) continue;
            if(line.length() == 0) continue;

            stopwords.add( line.trim() );

        }

        reader.close();
        in.close();


        in = this.getClass().getResourceAsStream("/NLP/StopLists/smart.txt");

        reader = new BufferedReader(new InputStreamReader( in ));


        while(  (line = reader.readLine() )  != null          ) {

            if(line.startsWith("#")) continue;
            if(line.length() == 0) continue;

            stopwords.add( line.trim() );

        }


        if(!stemWordsFromStopList) return stopwords;

        UEALite stemmer = new UEALite();

        Set<String> stemmedStopWords = new HashSet<>();
        for(String s : stopwords) {

                stemmedStopWords.add( (stemmer.stem(s)).getWord() );

        }




        return stemmedStopWords;
    }

    public static List<String> Tokenizer(String input) {

       // System.out.println("String length: " +input.length());
        List<String> tokens = new ArrayList<>();

        input = whiteSpaceNormalize(input);

        //get indices where there are phrase or word Delimiters

        Matcher m = phraseAndWordDelimiter.matcher(input);
        int start = 0;

        while (m.find()) {

            //the seperated by PhraseAmdWordDelimiters

            if(m.start() != 0) { //check so the start of the string isn't a delim

                String token = input.substring(start, m.start());

                tokens.add(token);

            }

            Character delim = input.charAt(m.start());

            if (!delim.equals(' ')) tokens.add(delim.toString());

        //   System.out.print("start: " + m.start() );
            start = m.end();

        //   System.out.println(" end: " + start +  delim);
        }


        //potential last token

        if(start != input.length() ) {

            tokens.add( input.substring(start,input.length())  );

        }

        return tokens;

    }


    public static void main(String[] arg) throws IOException {


        String test2 = "A similarity-oriented approach for deriving reference values\\counts used in citation normalization is explored and contrasted with the dominant approach of utilizing database-defined journal sets as a basis for deriving such values. In the similarity-oriented approach, an assessed article's raw citation count is compared with a reference value that is derived from a reference set, which is constructed in such a way that articles in this set are estimated to address a subject matter similar to that of the assessed article. This estimation is based on second-order similarity and utilizes a combination of 2 feature sets: bibliographic references and technical terminology. The contribution of an article in a given reference set to the reference value is dependent on its degree of similarity to the assessed article. It is shown that reference values calculated by the similarity-oriented approach are considerably better at predicting the assessed articles' citation count compared to the reference values given by the journal-set approach, thus significantly reducing the variability in the observed citation distribution that stems from the variability in the articles' addressed subject matter.";
        String test = "Automated structure validation was introduced in chemical crystallography about 12 years ago as a tool to assist practitioners with the exponential growth in crystal structure analyses. Validation has since evolved into an easy-to-use checkCIF/PLATON web-based IUCr service. The result of a crystal structure determination has to be supplied as a CIF-formatted computer-readable file. The checking software tests the data in the CIF for completeness, quality and consistency. In addition, the reported structure is checked for incomplete analysis, errors in the analysis and relevant issues to be verified. A validation report is generated in the form of a list of ALERTS on the issues to be corrected, checked or commented on. Structure validation has largely eliminated obvious problems with structure reports published in IUCr journals, such as refinement in a space group of too low symmetry. This paper reports on the current status of structure validation and possible future extensions.,, hello";

        RAKE rake = new RAKE();


        UEALite stemmer = new UEALite();

        Set<String> stopwords = rake.loadStopWordList(true);

        List<String> extractedKeywords =  rake.getKeyWords(test2, false,stopwords,stemmer);


       System.out.println(extractedKeywords);


       System.out.println("Testing n-grams:");

       System.out.println(nNgram("raw citation count"));


       System.out.println(  isConsideredWord("citations") );

    }

}

