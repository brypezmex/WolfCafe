package edu.ncsu.csc326.wolfcafe.service;

/**
 * Screens user-supplied free text for profanity. Applied anywhere a user can
 * name something: account names, usernames, emails, drink and item names, item
 * descriptions, and ingredient names.
 */
public interface ProfanityFilterService {

    /**
     * Returns true if the given text contains a blocked term.
     *
     * @param text
     *            text to screen; null and blank text are always clean
     * @return true if the text contains profanity
     */
    boolean containsProfanity ( String text );

    /**
     * Validates a single field, throwing if it contains profanity.
     *
     * @param fieldLabel
     *            human-readable field name used in the error message, such as
     *            "Username" or "Recipe name"
     * @param text
     *            text to screen
     * @throws edu.ncsu.csc326.wolfcafe.exception.WolfCafeAPIException
     *             if the text contains a blocked term
     */
    void validate ( String fieldLabel, String text );
}
