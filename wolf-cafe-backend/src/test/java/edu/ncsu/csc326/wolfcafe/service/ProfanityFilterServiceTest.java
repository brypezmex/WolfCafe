package edu.ncsu.csc326.wolfcafe.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import edu.ncsu.csc326.wolfcafe.exception.WolfCafeAPIException;
import edu.ncsu.csc326.wolfcafe.service.impl.ProfanityFilterServiceImpl;

/**
 * Unit tests for the profanity filter. These run without Spring so the real
 * word lists are exercised directly.
 */
public class ProfanityFilterServiceTest {

    /** Filter under test. */
    private ProfanityFilterServiceImpl filter;

    /**
     * Builds the filter and loads the classpath word lists.
     */
    @BeforeEach
    public void setUp () {
        filter = new ProfanityFilterServiceImpl();
        filter.loadWordLists();
    }

    /**
     * Ordinary cafe wording must not be flagged. Each of these contains a
     * blocked term as a substring and is the kind of false positive a naive
     * substring filter would produce.
     *
     * @param text
     *            clean text that should be accepted
     */
    @ParameterizedTest
    @ValueSource ( strings = { "Pumpkin Spice Latte", "Lemongrass Tea", "Grape Juice", "Assam Black Tea",
            "Shiitake Mushroom Broth", "Classic Cold Brew", "Soda Pop", "Passionfruit Cooler", "Espresso",
            "Cocktail Bitters", "Analysis Blend", "Bobs Morning Roast" } )
    public void testCleanTextIsAccepted ( final String text ) {
        assertFalse( filter.containsProfanity( text ), "should be clean: " + text );
    }

    /**
     * Plain and obfuscated profanity must be caught.
     *
     * @param text
     *            text that should be rejected
     */
    @ParameterizedTest
    @ValueSource ( strings = { "shit", "Sh1t Latte", "s.h.i.t", "f u c k", "shiiiit", "@sshole", "you ass",
            "fucking coffee", "b!tch", "ass@example.com", "MotherFucker", "n1gger" } )
    public void testProfanityIsRejected ( final String text ) {
        assertTrue( filter.containsProfanity( text ), "should be flagged: " + text );
    }

    /**
     * Null and blank text are always treated as clean so the filter never
     * duplicates required-field validation.
     */
    @Test
    public void testNullAndBlankAreClean () {
        assertFalse( filter.containsProfanity( null ) );
        assertFalse( filter.containsProfanity( "" ) );
        assertFalse( filter.containsProfanity( "   " ) );
    }

    /**
     * validate() throws for profanity and names the offending field, and stays
     * quiet for clean text.
     */
    @Test
    public void testValidateThrowsWithFieldLabel () {
        final WolfCafeAPIException exception = assertThrows( WolfCafeAPIException.class,
                () -> filter.validate( "Drink name", "shit latte" ) );
        assertTrue( exception.getMessage().startsWith( "Drink name" ), exception.getMessage() );

        assertDoesNotThrow( () -> filter.validate( "Drink name", "Vanilla Latte" ) );
    }
}
