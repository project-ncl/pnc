package org.jboss.pnc.model;

public class ModelTestDataFactory {

    private static ModelTestDataFactory singleton = new ModelTestDataFactory();

    private ModelTestDataFactory() {
        // private constructor to prevent multiple instances
    }

    public static ModelTestDataFactory getInstance() {
        return singleton;
    }

    ////  Products, Versions, Records ////

    public Product getProduct1() {
        return Product.Builder.newBuilder()
                .name("Test Product 1")
                .description("Test Product 1 Description")
                .abbreviation("TP1")
                .build();
    }

    public Product getProduct2() {
        return Product.Builder.newBuilder()
                .name("Test Product 2")
                .description("Test Product 2 Description")
                .abbreviation("TP2")
                .build();
    }

    public ProductVersion getProductVersion1() {
        return ProductVersion.Builder.newBuilder()
                .product(getProduct1())
                .version("1.0")
                .build();
    }

    public ProductVersion getProductVersion2() {
        return ProductVersion.Builder.newBuilder()
                .product(getProduct2())
                .version("1.0")
                .build();
    }

    public BuildRecordSet getBuildRecordSet() {
        return BuildRecordSet.Builder.newBuilder()
                .buildSetContentId("contentid")
                .build();
    }

    public ProductMilestone getProductMilestone1version1() {
        return ProductMilestone.Builder.newBuilder()
                .version("1.0.0.ER1")
                .productVersion(getProductVersion1())
                .performedBuildRecordSet(getBuildRecordSet())
                .build();
    }

    public ProductMilestone getProductMilestone1version2() {
        return ProductMilestone.Builder.newBuilder()
                .version("1.0.0.ER2")
                .productVersion(getProductVersion1())
                .performedBuildRecordSet(getBuildRecordSet())
                .build();
    }

    public ProductRelease getProductRelease1() {
        return ProductRelease.Builder.newBuilder()
                .version("1.0.0.Beta1")
                .productMilestone(getProductMilestone1version1())
                .build();
    }


    ////  Projects, Configurations, Environments ////

    public License getLicenseApache20() {
        return License.Builder.newBuilder()
                .fullName("Apache License Version 2.0")
                .shortName("Apache 2.0")
                .refUrl("http://apache.org/licenses/LICENSE-2.0.txt")
                .build();
    }

    public License getLicenseGPLv3() {
        return License.Builder.newBuilder()
                .fullName("GNU General Public License Version 3")
                .shortName("GPLv3")
                .refUrl("http://www.gnu.org/licenses/gpl.txt")
                .build();
    }

    public Environment getEnvironmentDefault() {
        return Environment.Builder.defaultEnvironment()
                .build();
    }

    public Project getProject1() {
        return Project.Builder.newBuilder()
                .name("Test Project 1")
                .description("Test Project 1 Description")
                .issueTrackerUrl("http://isssues.jboss.org")
                .license(getLicenseApache20())
                .build();
    }

    public Project getProject2() {
        return Project.Builder.newBuilder()
                .name("Test Project 2")
                .description("Test Project 2 Description")
                .issueTrackerUrl("http://isssues.jboss.org")
                .license(getLicenseGPLv3())
                .build();
    }

    public BuildConfiguration getBuildConfiguration1() {
        return BuildConfiguration.Builder.newBuilder()
                .name("Build Configuration 1")
                .description("Build Configuration 1 Description")
                .project(getProject1())
                .scmRepoURL("http://www.github.com")
                .buildScript("mvn install")
                .environment(getEnvironmentDefault())
                .build();
    }

    public BuildConfiguration getBuildConfiguration2() {
        return BuildConfiguration.Builder.newBuilder()
                .name("Build Configuration 2")
                .description("Build Configuration 2 Description")
                .project(getProject2())
                .scmRepoURL("http://www.github.com")
                .buildScript("mvn install")
                .environment(getEnvironmentDefault())
                .build();
    }

}
