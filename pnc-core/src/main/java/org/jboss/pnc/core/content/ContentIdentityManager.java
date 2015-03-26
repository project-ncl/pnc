package org.jboss.pnc.core.content;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.ProductVersion;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Component that contains the rules for generating various content ID's which are used to uniquely associate content stored in
 * external services with builds, build-sets, products, etc.
 */
public class ContentIdentityManager {

    private static final String PRODUCT_CONTENT_ID_FORMAT = "product+%s+%s";

    private static final String BUILD_SET_CONTENT_ID_FORMAT = "set+%s+%s";

    private static final String BUILD_CONTENT_ID_FORMAT = "build+%s+%s";

    private static final String TIMESTAMP_FORMAT = "yyyyMMdd.HHmm";

    public String getProductContentId(ProductVersion productVersion) {
        if (productVersion == null) {
            return null;
        }

        return String.format(PRODUCT_CONTENT_ID_FORMAT, safeIdPart(productVersion.getProduct().getName()),
                safeIdPart(productVersion.getVersion()));
    }

    public String getBuildSetContentId(BuildConfigurationSet buildConfigurationSet) {
        String timestamp = generateTimestamp();
        return String.format(BUILD_SET_CONTENT_ID_FORMAT, safeIdPart(buildConfigurationSet.getName()), timestamp);
    }

    public String getBuildContentId(BuildConfiguration buildConfiguration) {
        String timestamp = generateTimestamp();
        return String.format(BUILD_CONTENT_ID_FORMAT, safeIdPart(buildConfiguration.getName()), timestamp);
    }

    private String generateTimestamp() {
        return new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
    }

    /**
     * Sift out spaces, pipe characters and colons (things that don't play well in URLs) from the project name, and convert them
     * to dashes. This is only for naming repositories, so an approximate match to the project in question is fine.
     */
    private String safeIdPart(String name) {
        return name.replaceAll("\\W+", "-").replaceAll("[|:]+", "-");
    }

}
