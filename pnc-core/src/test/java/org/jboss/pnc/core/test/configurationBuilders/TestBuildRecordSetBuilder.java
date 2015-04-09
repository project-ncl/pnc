package org.jboss.pnc.core.test.configurationBuilders;

import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductVersion;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-10.
 */
public class TestBuildRecordSetBuilder {
    public BuildRecordSet build(String name, String description, String version) {
        Product product = new Product(name, description);
        ProductVersion productVersion = ProductVersion.Builder.newBuilder().product(product).version(version).build();
        product.addVersion(productVersion);
        ProductMilestone productMilestone = ProductMilestone.Builder.newBuilder().productVersion(productVersion).build(); 
        BuildRecordSet buildRecordSet = new BuildRecordSet();
        buildRecordSet.setProductMilestone(productMilestone);
        return buildRecordSet;
    }


}
