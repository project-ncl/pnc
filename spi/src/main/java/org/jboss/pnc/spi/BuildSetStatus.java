package org.jboss.pnc.spi;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-05-15.
 */
public enum BuildSetStatus {
    NEW,
    DONE(true),
    REJECTED(true);

    private final boolean isFinal;

    BuildSetStatus() {
        isFinal = false;
    }

    BuildSetStatus(boolean isFinal) {
        this.isFinal = isFinal;
    }

    public boolean isCompleted() {
        return isFinal;
    }
}
