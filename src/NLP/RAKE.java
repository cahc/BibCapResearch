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


Split on VERBS and
 "VB", "VBD", "VBG", "VBN", "VBP", "VBZ"


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

    public void getKeyWords(String input) {

        List<String> tokens = Tokenizer(input);

        //pre tagg
        List<WordTag> wordtags = InitialTagger.SimplePreTaggerForEnglish(this.FREQDICT, tokens);

        //post tagg

        List<String> finaltags = this.rdrposTagger.finalTags(wordtags);

        int diff = 0;
        for (int i = 0; i < finaltags.size(); i++) {

            System.out.println(wordtags.get(i).word + " " + wordtags.get(i).tag + " " + finaltags.get(i));


            if (!wordtags.get(i).tag.equals(finaltags.get(i))) {

                //System.out.println(wordtags.get(i).word +" " + wordtags.get(i).tag +" " + finaltags.get(i) );

                diff++;
            }

        }


      //  System.out.println("diffs : " + diff);


    }


    public static List<String> Tokenizer(String input) {

        List<String> tokens = new ArrayList<>();

        input = whiteSpaceNormalize(input);

        //get indices where there are phrase or word Delimiters

        Matcher m = phraseAndWordDelimiter.matcher(input);
        int start = 0;

        while (m.find()) {

            //the seperated by PhraseAmdWordDelimiters

            tokens.add(input.substring(start, m.start()));

            Character delim = input.charAt(m.start());

            if (!delim.equals(' ')) tokens.add(delim.toString());

            start = m.end();
        }


        return tokens;

    }


    public static void main(String[] arg) throws IOException {


        String test = "A similarity-oriented approach for deriving reference values used in citation normalization is explored and contrasted with the dominant approach of utilizing database-defined journal sets as a basis for deriving such values. In the similarity-oriented approach, an assessed article's raw citation count is compared with a reference value that is derived from a reference set, which is constructed in such a way that articles in this set are estimated to address a subject matter similar to that of the assessed article. This estimation is based on second-order similarity and utilizes a combination of 2 feature sets: bibliographic references and technical terminology. The contribution of an article in a given reference set to the reference value is dependent on its degree of similarity to the assessed article. It is shown that reference values calculated by the similarity-oriented approach are considerably better at predicting the assessed articles' citation count compared to the reference values given by the journal-set approach, thus significantly reducing the variability in the observed citation distribution that stems from the variability in the articles' addressed subject matter.";


        RAKE rake = new RAKE();

        rake.getKeyWords(test);

    }

}

