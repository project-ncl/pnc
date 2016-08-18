package org.jboss.pnc.bpm;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;

/**
 * Performs scheduled regular events on the BPM classes
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
@Singleton
public class BpmScheduler {
    
    @Inject
    private BpmManager bpmManager;
    
    /**
     * The finished BPM tasks has to be cleaned up regularly
     * Immediate cleanup is not usable because of NCL-2300
     */
    @Schedule(hour = "*")
    public void bpmTasksCleanup() {
        bpmManager.cleanup();
    }

}
