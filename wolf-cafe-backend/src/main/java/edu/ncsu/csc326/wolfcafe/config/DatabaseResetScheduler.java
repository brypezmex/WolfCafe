package edu.ncsu.csc326.wolfcafe.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import edu.ncsu.csc326.wolfcafe.service.DatabaseResetService;
import lombok.AllArgsConstructor;

/**
 * Runs the daily database reset.
 *
 * The schedule defaults to midnight America/New_York and is controlled by
 * {@code app.db-reset.cron} and {@code app.db-reset.zone}. Setting
 * {@code app.db-reset.enabled=false} removes this bean entirely, which is what
 * the test configuration does so tests never wipe a database.
 */
@Component
@AllArgsConstructor
@ConditionalOnProperty ( name = "app.db-reset.enabled", havingValue = "true", matchIfMissing = true )
public class DatabaseResetScheduler {

    /** Logger for scheduled reset failures */
    private static final Logger  LOGGER = LoggerFactory.getLogger( DatabaseResetScheduler.class );

    /** Service that performs the wipe and reseed */
    private DatabaseResetService databaseResetService;

    /**
     * Wipes and reseeds the database on the configured schedule. Failures are
     * logged rather than rethrown so one bad run does not stop the scheduler
     * from trying again the next night.
     */
    @Scheduled ( cron = "${app.db-reset.cron:0 0 0 * * *}", zone = "${app.db-reset.zone:America/New_York}" )
    public void resetDatabaseNightly () {
        try {
            databaseResetService.resetDatabase();
        }
        catch ( final Exception e ) {
            LOGGER.error( "action=DB_RESET_FAILED", e );
        }
    }
}
