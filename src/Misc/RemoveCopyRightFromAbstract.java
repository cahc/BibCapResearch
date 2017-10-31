package Misc;

/**
 * From VoSviewer
 */
public class RemoveCopyRightFromAbstract {

    public static final PatternMatcher[] CLEAN_PATTERN_ABSTRACT;

    static {

        CLEAN_PATTERN_ABSTRACT = new PatternMatcher[] { new PatternMatcher("^(.*?)((?i)copyright(?-i).{0,3})?(\\xA9.{0,5}[0-9][0-9][0-9][0-9])(.{0,400})$", "$1"), new PatternMatcher("^(.*?)((?i)copyright(?-i).{0,3})?(\\xA9)([^a-z].{0,3}[A-Z].{0,300})$", "$1"), new PatternMatcher("^(.*?)((?i)copyright(?-i).{0,3})?(\\xA9)(.{0,200})$", "$1"), new PatternMatcher("^(.*?)((?i)copyright(?-i).{0,3}[(].{0,1}[Cc].{0,1}[)])(.{0,300})$", "$1"), new PatternMatcher("^(.*?)((?i)copyright(?-i).{0,3})?([(][Cc][)])([^=]{0,6})((?i)[0-9][0-9][0-9][0-9]|crown|author|elsevier|wiley(?-i))(.{0,200})$", "$1"), new PatternMatcher("^(.*?)((?i)copyright(?-i).{0,3})?([(][Cc][)])(.{0,200})((?i)\\b[12][09][0-9][0-9]\\b|\\breserved\\b|\\bltd\\b|\\binc\\b|paris|basel(?-i))(.{0,3})$", "$1"), new PatternMatcher("^(.*?)((?i)copyright(?-i).{0,3})?([(][Cc][)])((?i).{0,7}author|.{0,7}published|.{0,200}open[ ]access|.{0,200}creative[ ]commons(?-i))(.{0,500})$", "$1"), new PatternMatcher("^(.*?)((?i)copyright(?-i))(.{0,100})((?i)[(]c[)]|[12][09][0-9][0-9]|\\breserved\\b|\\bdistributed\\b|\\bltd\\b|\\binc\\b|\\bpublished\\b|\\bpress\\b|american|institute|society|association|administration|elsevier|wiley(?-i))(.{0,200})$", "$1"), new PatternMatcher("^(.*?)((?i)copy;[ ]|[(].{0,1}c.{0,1}[)].{0,2}|\\bthe\\b[ ]\\bauthor.{0,5}|\\bpublished\\b[ ]\\bby\\b.{0,100}(?-i))?([12][09][0-9][0-9])(.{0,200})((?i)\\breserved\\b|\\bltd\\b|\\binc\\b(?-i))(.{0,3})$", "$1"), new PatternMatcher("^(.*?)([.][ ]+\\b[Pp]ublished\\b[ ]\\bby\\b.{0,300}|[ ]+\\bPublished\\b[ ]\\bby\\b[ ]+[A-Z].{0,300}|[ ]+\\bpublished\\b[ ]\\bby\\b[ ]+[A-Z].{0,100})$", "$1"), new PatternMatcher("^(.*?)([.][ ]+[(][C][)].{0,100}|[ ]+[(][C][)][ ]+[A-Z].{0,100}|[ ]+[(][c][)][ ]+[A-Z].{0,50})$", "$1"), new PatternMatcher("^(.*?)([(].{0,1}[Cc].{0,1}[)])((?i).{0,3}[0-9][0-9][0-9][0-9]|.{0,80}society|.{0,80}association|.{0,80}elsevier|.{0,80}wiley|.{0,80}wilkins|.{0,80}blackwell|.{0,80}ksbb|.{0,80}rsna(?-i))(.{0,80})$", "$1"), new PatternMatcher("^(.*?)(Copyright.{0,100})$", "$1") };

    }

    public static String cleanedAbstract(String text) {
        int i = 0;


        for (boolean cleaned = false; i < RemoveCopyRightFromAbstract.CLEAN_PATTERN_ABSTRACT.length && !cleaned; ++i) {
            if (RemoveCopyRightFromAbstract.CLEAN_PATTERN_ABSTRACT[i].isMatch(text)) {
                text = RemoveCopyRightFromAbstract.CLEAN_PATTERN_ABSTRACT[i].replace(text);
                cleaned = true;
            }
        }

        return text;

    }


}
