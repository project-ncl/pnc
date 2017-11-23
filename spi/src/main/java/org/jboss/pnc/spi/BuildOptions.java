package org.jboss.pnc.spi;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Class used to store all available build options of a BuildConfiguration or BuildConfigurationSet
 *
 * @author Jakub Bartecek
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Getter
@Setter
public class BuildOptions {

    /**
     * Temporary build or standard build?
     */
    private boolean temporaryBuild = false;

    /**
     * Should we force the rebuild?
     */
    private boolean forceRebuild = false;

    /**
     * Should we build also dependencies of this BuildConfiguration? Valid only for BuildConfiguration
     */
    private boolean buildDependencies = true;

    /**
     * Should we keep the build container running, if the build fails?
     */
    private boolean keepPodOnFailure = false;

    /**
     * Should we add a timestamp during the alignment?
     */
    private boolean timestampAlignment = false;

    public boolean checkBuildOptionsValidity() {
        if(!temporaryBuild && timestampAlignment) {
            // Combination timestampAlignment + standard build is not allowed
            return false;
        }
        return true;
    }

}
