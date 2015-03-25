package org.jboss.pnc.core.test.mock;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.spi.content.ContentIdentifierManager;

public class MockContentIdentifierManager implements ContentIdentifierManager {

    @Override
    public String getProductContentId(ProductVersion productVersion) {
        return productVersion.getProduct().getName() + "-" + productVersion.getVersion();
    }

    @Override
    public String getBuildSetContentId(BuildConfigurationSet buildConfigurationSet) {
        return buildConfigurationSet.getName();
    }

    @Override
    public String getBuildContentId(BuildConfiguration buildConfiguration) {
        return buildConfiguration.getName();
    }

}
