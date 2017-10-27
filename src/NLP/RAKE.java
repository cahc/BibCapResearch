package NLP;

import NLP.RDRPOSTagger.InitialTagger;
import NLP.RDRPOSTagger.RDRPOSTagger;
import NLP.RDRPOSTagger.Utils;
import NLP.RDRPOSTagger.WordTag;
import jnr.ffi.Struct;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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


    private final static String whiteSpaceNormalize(String input) {

        return StringUtils.normalizeSpace(input);
    }

    private final static Pattern phraseAndWordDelimiter = Pattern.compile("[\\.\\/\\,\\!\\?\\{\\}\\[\\]\\;\\:\\(\\)\\_\\@\\ ]+");

    private final static RDRPOSTagger rdrposTagger;


    static {

        rdrposTagger = new RDRPOSTagger();
        try {
            rdrposTagger.constructTreeFromRulesFile("E:\\RDRPOSTagger-master\\Models\\POS\\English.RDR");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private final static HashMap<String, String> FREQDICT = Utils.getDictionary("E:\\RDRPOSTagger-master\\Models\\POS\\English.DICT");

    public void getTokens(String input) {

        List<String> tokens = Tokenizer(input);

        for(String s : tokens) {

            System.out.println(s);

        }
    }

    public void getKeyWords(String input, boolean cleanAbstractFromCopyright) {

        if(cleanAbstractFromCopyright) {

           input = RemoveCopyRightFromAbstract.cleanedAbstract(input);

        }

        List<String> tokens = Tokenizer(input);

        //pre tagg
        List<WordTag> wordtags = InitialTagger.SimplePreTaggerForEnglish(this.FREQDICT, tokens);


        //post tagg

        List<String> finaltags = this.rdrposTagger.finalTags(wordtags);

        int diff = 0;
        for (int i = 0; i < finaltags.size(); i++) {

            System.out.println(wordtags.get(i).word + " " + wordtags.get(i).tag + " " + finaltags.get(i));


         //   if (!wordtags.get(i).tag.equals(finaltags.get(i))) {

                //System.out.println(wordtags.get(i).word +" " + wordtags.get(i).tag +" " + finaltags.get(i) );

                diff++;
           // }

        }


      //  System.out.println("diffs : " + diff);


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


        String test = "A similarity-oriented approach for deriving reference values used in citation normalization is explored and contrasted with the dominant approach of utilizing database-defined journal sets as a basis for deriving such values. In the similarity-oriented approach, an assessed article's raw citation count is compared with a reference value that is derived from a reference set, which is constructed in such a way that articles in this set are estimated to address a subject matter similar to that of the assessed article. This estimation is based on second-order similarity and utilizes a combination of 2 feature sets: bibliographic references and technical terminology. The contribution of an article in a given reference set to the reference value is dependent on its degree of similarity to the assessed article. It is shown that reference values calculated by the similarity-oriented approach are considerably better at predicting the assessed articles' citation count compared to the reference values given by the journal-set approach, thus significantly reducing the variability in the observed citation distribution that stems from the variability in the articles' addressed subject matter.";


        RAKE rake = new RAKE();

        rake.getKeyWords(test, false);

    }

}

