package org.jboss.pnc.integration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.pnc.datastore.limits.RSQLPageLimitAndSortingProducer;
import org.jboss.pnc.datastore.predicates.ArtifactPredicates;
import org.jboss.pnc.datastore.predicates.rsql.RSQLNodeTravellerPredicate;
import org.jboss.pnc.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.datastore.repositories.BuildRecordSetRepository;
import org.jboss.pnc.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.rest.provider.ArtifactProvider;
import org.jboss.pnc.rest.provider.BuildRecordSetProvider;
import org.jboss.pnc.rest.restmodel.ArtifactRest;
import org.jboss.pnc.rest.restmodel.BuildRecordSetRest;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildRecordSetsTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static Integer buildRecordSetId;
    private static Integer buildRecordId;
    private static Integer productMilestoneId;

    @Inject
    private BuildRecordRepository buildRecordRepository;

    @Inject
    private BuildRecordSetRepository buildRecordSetRepository;

    @Inject
    private ProductMilestoneRepository productMilestoneRepository;

    @Inject
    private BuildRecordSetProvider buildRecordSetProvider;

    @Inject
    private ArtifactProvider artifactProvider;

    @Inject
    private ArtifactRepository artifactRepository;

    @Deployment
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEarWithTestDependencies();
        WebArchive war = enterpriseArchive.getAsType(WebArchive.class, "/pnc-rest.war");
        war.addClass(BuildRecordSetsTest.class);
        war.addClass(ArtifactProvider.class);

        JavaArchive datastoreJar = enterpriseArchive.getAsType(JavaArchive.class, "/datastore.jar");
        datastoreJar.addClass(ArtifactPredicates.class);
        datastoreJar.addPackage(RSQLNodeTravellerPredicate.class.getPackage());

        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Test
    @InSequence(-1)
    @Transactional
    public void shouldInsertValuesIntoDB() {
        BuildRecord buildRecord = buildRecordRepository.findAll().iterator().next();
        ProductMilestone productMilestone = productMilestoneRepository.findAll().iterator().next();

        buildRecordId = buildRecord.getId();
        productMilestoneId = productMilestone.getId();

        BuildRecordSet.Builder builder = BuildRecordSet.Builder.newBuilder();
        BuildRecordSet buildRecordSet = builder.buildRecord(buildRecord).productMilestone(productMilestone).build();

        buildRecordSet = buildRecordSetRepository.save(buildRecordSet);

        buildRecordSetId = buildRecordSet.getId();
    }

    @Test
    public void shouldGetAllBuildRecordSets() {
        // when
        List<BuildRecordSetRest> buildRecordSets = (List<BuildRecordSetRest>) buildRecordSetProvider.getAll(
                RSQLPageLimitAndSortingProducer.DEFAULT_OFFSET, RSQLPageLimitAndSortingProducer.DEFAULT_SIZE, null, null);

        // then
        assertThat(buildRecordSets).isNotNull();
        assertThat(buildRecordSets.size() > 1);
    }

    @Test
    public void shouldGetSpecificBuildRecordSet() {
        // when
        BuildRecordSetRest buildRecordSet = buildRecordSetProvider.getSpecific(buildRecordSetId);

        // then
        assertThat(buildRecordSet).isNotNull();
    }

    @Test
    public void shouldGetBuildRecordSetOfProductMilestone() {
        // when
        List<BuildRecordSetRest> buildRecordSetRests = buildRecordSetProvider.getAllForProductMilestone(
                RSQLPageLimitAndSortingProducer.DEFAULT_OFFSET, RSQLPageLimitAndSortingProducer.DEFAULT_SIZE, null, null,
                productMilestoneId);

        // then
        assertThat(buildRecordSetRests).hasSize(1);
    }

    @Test
    public void shouldGetBuildRecordSetOfBuildRecord() {
        // when
        List<BuildRecordSetRest> buildRecordSetRests = buildRecordSetProvider.getAllForBuildRecord(
                RSQLPageLimitAndSortingProducer.DEFAULT_OFFSET, RSQLPageLimitAndSortingProducer.DEFAULT_SIZE, null, null,
                buildRecordId);

        // then
        assertThat(buildRecordSetRests).hasSize(1);
    }

    @Test
    public void shouldGetArtifactsAssignedToBuildRecordSet() {
        // when
        List<ArtifactRest> allForBuildRecordSet = artifactProvider
                .getAllForBuildRecordSet(RSQLPageLimitAndSortingProducer.DEFAULT_OFFSET,
                        RSQLPageLimitAndSortingProducer.DEFAULT_SIZE, null, null, buildRecordSetId);

        // then
        assertThat(allForBuildRecordSet.size()).isGreaterThan(0);
        assertThat(allForBuildRecordSet).hasSize((int) artifactRepository.count());
    }

    @Test
    @InSequence(999)
    public void shouldNotCascadeDeletionOfBuildRecordSet() {
        // when
        long buildRecordCount = buildRecordRepository.count();
        long productMilestoneCount = productMilestoneRepository.count();

        buildRecordSetProvider.delete(buildRecordSetId);

        // then
        assertThat(buildRecordRepository.count()).isEqualTo(buildRecordCount);
        assertThat(productMilestoneRepository.count()).isEqualTo(productMilestoneCount);
    }

}
