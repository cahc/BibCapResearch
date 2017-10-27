package NLP;

/**
 * Created by crco0001 on 10/27/2017.
 */
import java.util.regex.Pattern;

public class PatternMatcher
{
    private Pattern pattern;
    private String replacement;

    public PatternMatcher(final String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    public PatternMatcher(final String pattern, final String replacement) {
        this.pattern = Pattern.compile(pattern);
        this.replacement = replacement;
    }

    public boolean isMatch(final String text) {
        return this.pattern.matcher(text).find();
    }

    public String replace(final String text) {
        return (this.replacement != null) ? this.pattern.matcher(text).replaceAll(this.replacement) : null;
    }

    public String[] split(final String text) {
        return this.pattern.split(text);
    }
}
