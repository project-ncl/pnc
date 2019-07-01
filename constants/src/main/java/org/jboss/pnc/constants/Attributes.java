package org.jboss.pnc.constants;

/**
 * Represents attribute keys.
 *
 * @author Jan Michalov <jmichalo@redhat.com>
 */
public class Attributes {

    /**
     * Attribute key for {@link org.jboss.pnc.dto.ProductVersion} representing Brew tag prefix for a
     * Version.
     */
    public static final String BREW_TAG_PREFIX = "BREW_TAG_PREFIX";

    /**
     * Attribute key for {@link org.jboss.pnc.dto.Build} representing Brew name of the build.
     */
    public static final String BUILD_BREW_NAME = "BREW_BUILD_NAME";

    /**
     * Attribute key for {@link org.jboss.pnc.dto.Build} representing Brew version of the build.
     */
    public static final String BUILD_BREW_VERSION = "BREW_BUILD_VERSION";
}
