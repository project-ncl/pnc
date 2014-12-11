package org.jboss.pnc.core.test;

import org.jboss.pnc.model.BuildCollection;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductVersion;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-10.
 */
public class TestBuildCollectionBuilder {
    public BuildCollection build(String name, String description, String version) {
        Product product = new Product(name, description);
        ProductVersion productVersion = new ProductVersion(version, product);
        product.addVersion(productVersion);
        BuildCollection buildCollection = new BuildCollection();
        buildCollection.setProductVersion(productVersion);
        buildCollection.setProductBuildNumber(1);
        return buildCollection;
    }


}
