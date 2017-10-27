package NLP.RDRPOSTagger;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by crco0001 on 10/26/2017.
 */
public class RDRPOSTPlayground {

    public static void main(String[] arg) throws IOException {

        RDRPOSTagger rdrposTagger = new RDRPOSTagger();
        // Load the POS tagging model for ENGLISH
        rdrposTagger.constructTreeFromRulesFile("E:\\RDRPOSTagger-master\\Models\\POS\\English.RDR");

        // Load the lexicon for ENGLISH
        HashMap<String,String> DICT = Utils.getDictionary("E:\\RDRPOSTagger-master\\Models\\POS\\English.DICT");

        boolean useAlternativeTagger = false;
       String hej = rdrposTagger.tagEnSentence(DICT,"A similarity oriented approach for deriving reference values used in citation normalization is explored and contrasted with the dominant approach of utilizing database-defined journal sets as a basis for deriving such values. In the similarity-oriented approach, an assessed article's raw citation count is compared with a reference value that is derived from a reference set, which is constructed in such a way that articles in this set are estimated to address a subject matter similar to that of the assessed article. This estimation is based on second-order similarity and utilizes a combination of 2 feature sets: bibliographic references and technical terminology. The contribution of an article in a given reference set to the reference value is dependent on its degree of similarity to the assessed article. It is shown that reference values calculated by the similarity-oriented approach are considerably better at predicting the assessed articles' citation count compared to the reference values given by the journal-set approach, thus significantly reducing the variability in the observed citation distribution that stems from the variability in the articles' addressed subject matter.", useAlternativeTagger);

       System.out.println(hej);
    }


}
