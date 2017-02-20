package org.chronopolis.medic.client;

import org.chronopolis.rest.models.repair.Fulfillment;
import org.chronopolis.rest.models.repair.Repair;

/**
 * Interface defining how our repair manager will be handled
 *
 * Created by shake on 2/17/17.
 */
public interface RepairManager {

    /**
     * Backup files which will be repaired
     *
     */
    void backup(Repair repair);

    /**
     * When a repair is complete (and successful), backed up files
     * can be removed
     *
     */
    void removeBackup(Repair repair);

    /**
     * Replicate files from a remote node
     *
     */
    void replicate(Fulfillment fulfillment);

    /**
     * Check replicated files
     *
     */
    void validateFiles(Repair repair);

}
