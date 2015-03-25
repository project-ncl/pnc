package org.jboss.pnc.mavenrepositorymanager;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.spi.content.ContentIdentifierManager;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MavenContentIdentifierManager implements ContentIdentifierManager {

    private static final String PRODUCT_CONTENT_ID_FORMAT = "%s-%s";

    private static final String NAME_AND_DATESTAMP = "%s-%s";

    private static final String BUILD_SET_CONTENT_ID_FORMAT = NAME_AND_DATESTAMP;

    private static final String BUILD_CONTENT_ID_FORMAT = NAME_AND_DATESTAMP;

    private static final String TIMESTAMP_FORMAT = "yyyyMMdd.HHmm";

    @Override
    public String getProductContentId(ProductVersion productVersion) {
        if (productVersion == null) {
            return ContentIdentifierManager.GLOBAL_CONTENT_ID;
        }

        return String.format(PRODUCT_CONTENT_ID_FORMAT, safeIdPart(productVersion.getProduct().getName()),
                safeIdPart(productVersion.getVersion()));
    }

    @Override
    public String getBuildSetContentId(BuildConfigurationSet buildConfigurationSet) {
        String timestamp = generateTimestamp();
        return String.format(BUILD_SET_CONTENT_ID_FORMAT, safeIdPart(buildConfigurationSet.getName()), timestamp);
    }

    @Override
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
