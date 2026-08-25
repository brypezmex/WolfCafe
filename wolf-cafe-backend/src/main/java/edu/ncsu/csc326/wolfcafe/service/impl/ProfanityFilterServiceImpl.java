package edu.ncsu.csc326.wolfcafe.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import edu.ncsu.csc326.wolfcafe.exception.WolfCafeAPIException;
import edu.ncsu.csc326.wolfcafe.service.ProfanityFilterService;
import jakarta.annotation.PostConstruct;

/**
 * Profanity filter backed by word lists on the classpath.
 *
 * Screening happens in two passes so obfuscated profanity is caught without
 * rejecting ordinary words that happen to contain a blocked term:
 *
 * <ol>
 * <li>A whole-word pass over every term in {@code profanity/terms.txt}. Because
 * it matches whole words only, "Lemongrass Tea" and "Grape Juice" pass
 * cleanly.</li>
 * <li>A substring pass over the smaller {@code profanity/strict-terms.txt},
 * run against the text with all separators removed, which catches padding such
 * as "s.h.i.t" and "f u c k". Benign words listed in
 * {@code profanity/allowed-words.txt} are removed before this pass.</li>
 * </ol>
 *
 * Both passes lowercase the text and fold leetspeak, so "@ss" and "sh1t" are
 * treated as "ass" and "shit". Each term is compiled into a pattern that
 * tolerates repeated letters ("shiiiit" matches "shit"), rather than collapsing
 * repeats in the text itself, which would wrongly equate "pop" with "poop".
 */
@Service
public class ProfanityFilterServiceImpl implements ProfanityFilterService {

    /** Classpath location of the whole-word term list. */
    private static final String                    TERMS_RESOURCE         = "profanity/terms.txt";

    /** Classpath location of the substring-matched term list. */
    private static final String                    STRICT_TERMS_RESOURCE  = "profanity/strict-terms.txt";

    /** Classpath location of the benign-word exclusion list. */
    private static final String                    ALLOWED_WORDS_RESOURCE = "profanity/allowed-words.txt";

    /**
     * Characters commonly substituted for letters, folded before matching so
     * that "sh1t" and "$h!t" are treated the same as "shit".
     */
    private static final Map<Character, Character> LEET_MAP               = Map.ofEntries( Map.entry( '0', 'o' ),
            Map.entry( '1', 'i' ), Map.entry( '3', 'e' ), Map.entry( '4', 'a' ), Map.entry( '5', 's' ),
            Map.entry( '6', 'g' ), Map.entry( '7', 't' ), Map.entry( '8', 'b' ), Map.entry( '9', 'g' ),
            Map.entry( '@', 'a' ), Map.entry( '$', 's' ), Map.entry( '!', 'i' ), Map.entry( '|', 'l' ),
            Map.entry( '+', 't' ) );

    /**
     * Splits text into candidate words, keeping leetspeak substitutes attached
     * so "@ss" stays a single word.
     */
    private static final Pattern                   LEET_WORD_SPLITTER     = Pattern.compile( "[^a-zA-Z0-9@$!|+]+" );

    /**
     * Splits text on every non-alphanumeric character. Run in addition to
     * LEET_WORD_SPLITTER so symbols that double as leetspeak still act as
     * boundaries, catching cases such as the local part of "ass@example.com".
     */
    private static final Pattern                   PLAIN_WORD_SPLITTER    = Pattern.compile( "[^a-zA-Z0-9]+" );

    /** Matches a whole word that is a blocked term. */
    private Pattern                                wordPattern;

    /** Matches a blocked term anywhere in the collapsed text. */
    private Pattern                                strictPattern;

    /** Benign words removed before the substring pass, normalized. */
    private Set<String>                            allowedWords;

    /**
     * Whether filtering is active. Disabling it makes every check pass; the
     * field defaults to true so the service is also usable outside Spring.
     */
    @Value ( "${app.profanity.enabled:true}" )
    private boolean                                enabled                = true;

    /**
     * Loads and compiles the word lists once the bean is constructed.
     */
    @PostConstruct
    public void loadWordLists () {
        wordPattern = compileTerms( readResource( TERMS_RESOURCE ), true );
        strictPattern = compileTerms( readResource( STRICT_TERMS_RESOURCE ), false );
        allowedWords = readResource( ALLOWED_WORDS_RESOURCE );
    }

    /**
     * Returns true if the given text contains a blocked term.
     *
     * @param text
     *            text to screen; null and blank text are always clean
     * @return true if the text contains profanity
     */
    @Override
    public boolean containsProfanity ( final String text ) {
        if ( !enabled || text == null || text.isBlank() ) {
            return false;
        }

        return containsBlockedWord( text ) || containsStrictTerm( text );
    }

    /**
     * Validates a single field, throwing if it contains profanity.
     *
     * @param fieldLabel
     *            human-readable field name used in the error message
     * @param text
     *            text to screen
     * @throws WolfCafeAPIException
     *             if the text contains a blocked term
     */
    @Override
    public void validate ( final String fieldLabel, final String text ) {
        if ( containsProfanity( text ) ) {
            throw new WolfCafeAPIException( HttpStatus.BAD_REQUEST,
                    fieldLabel + " contains inappropriate language. Please choose different wording." );
        }
    }

    /**
     * Runs the whole-word pass: splits the text on separators and tests each
     * normalized word against the full term list.
     *
     * @param text
     *            raw text
     * @return true if any word is a blocked term
     */
    private boolean containsBlockedWord ( final String text ) {
        if ( wordPattern == null ) {
            return false;
        }

        return hasBlockedWord( LEET_WORD_SPLITTER.split( text ) )
                || hasBlockedWord( PLAIN_WORD_SPLITTER.split( text ) );
    }

    /**
     * Tests each candidate word against the full term list.
     *
     * @param rawWords
     *            candidate words straight from a splitter
     * @return true if any normalized word is a blocked term
     */
    private boolean hasBlockedWord ( final String[] rawWords ) {
        for ( final String rawWord : rawWords ) {
            final String word = normalize( rawWord );
            if ( !word.isEmpty() && wordPattern.matcher( word ).matches() ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Runs the substring pass over the text with all separators removed, after
     * subtracting benign words that legitimately contain a strict term.
     *
     * @param text
     *            raw text
     * @return true if any strict term appears in the collapsed text
     */
    private boolean containsStrictTerm ( final String text ) {
        if ( strictPattern == null ) {
            return false;
        }

        String collapsed = normalize( text );
        for ( final String allowedWord : allowedWords ) {
            collapsed = collapsed.replace( allowedWord, "" );
        }

        return strictPattern.matcher( collapsed ).find();
    }

    /**
     * Reduces text to a canonical form for matching: lowercased, leetspeak
     * folded to letters, and every remaining non-letter dropped.
     *
     * @param text
     *            text to normalize
     * @return normalized text, possibly empty
     */
    private String normalize ( final String text ) {
        final StringBuilder builder = new StringBuilder( text.length() );

        for ( final char rawChar : text.toLowerCase( Locale.ROOT ).toCharArray() ) {
            final char folded = LEET_MAP.getOrDefault( rawChar, rawChar );
            if ( folded >= 'a' && folded <= 'z' ) {
                builder.append( folded );
            }
        }

        return builder.toString();
    }

    /**
     * Compiles a set of normalized terms into a single alternation pattern in
     * which every letter may repeat, so "shiiiit" still matches "shit".
     *
     * @param termSet
     *            normalized terms
     * @param wholeWord
     *            true to anchor the pattern to the whole input
     * @return compiled pattern, or null if there are no terms
     */
    private Pattern compileTerms ( final Set<String> termSet, final boolean wholeWord ) {
        if ( termSet.isEmpty() ) {
            return null;
        }

        final String alternation = termSet.stream().map( this::toRepeatTolerantRegex )
                .collect( Collectors.joining( "|", "(?:", ")" ) );

        return Pattern.compile( wholeWord ? "^" + alternation + "$" : alternation );
    }

    /**
     * Turns a normalized term into a regex where each letter may repeat, for
     * example "shit" becomes "s+h+i+t+".
     *
     * @param term
     *            normalized term, letters only
     * @return repeat-tolerant regex fragment
     */
    private String toRepeatTolerantRegex ( final String term ) {
        final StringBuilder builder = new StringBuilder( term.length() * 2 );
        for ( final char letter : term.toCharArray() ) {
            builder.append( letter ).append( '+' );
        }
        return builder.toString();
    }

    /**
     * Reads a word-list resource, skipping blank lines and '#' comments and
     * normalizing every entry.
     *
     * @param resourcePath
     *            classpath location of the list
     * @return normalized entries
     */
    private Set<String> readResource ( final String resourcePath ) {
        final Set<String> entries = new LinkedHashSet<>();

        try ( InputStream inputStream = new ClassPathResource( resourcePath ).getInputStream();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader( inputStream, StandardCharsets.UTF_8 ) ) ) {

            String line;
            while ( ( line = reader.readLine() ) != null ) {
                final String trimmed = line.trim();
                if ( trimmed.isEmpty() || trimmed.startsWith( "#" ) ) {
                    continue;
                }
                final String normalized = normalize( trimmed );
                if ( !normalized.isEmpty() ) {
                    entries.add( normalized );
                }
            }
        }
        catch ( final IOException e ) {
            throw new UncheckedIOException( "Unable to load profanity word list " + resourcePath, e );
        }

        return entries;
    }
}
