package org.jboss.pnc.spi.content;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.ProductVersion;

public interface ContentIdentifierManager {

    String GLOBAL_CONTENT_ID = "global";

    String getProductContentId(ProductVersion productVersion);

    String getBuildSetContentId(BuildConfigurationSet buildConfigurationSet);

    String getBuildContentId(BuildConfiguration buildConfiguration);

}
