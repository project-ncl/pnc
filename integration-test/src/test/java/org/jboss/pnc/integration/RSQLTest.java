package org.jboss.pnc.integration;

import cz.jirutka.rsql.parser.RSQLParserException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.pnc.datastore.predicates.RSQLPredicateProducer;
import org.jboss.pnc.datastore.repositories.UserRepository;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.model.User;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.rest.provider.StreamHelper.nullableStreamOf;

@RunWith(Arquillian.class)
@Transactional(TransactionMode.ROLLBACK)
public class RSQLTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Inject
    private UserRepository userRepository;


    @Deployment
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEarWithTestDependencies();
        WebArchive war = enterpriseArchive.getAsType(WebArchive.class, "/pnc-web.war");
        war.addClass(RSQLTest.class);
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Test
    public void shouldSelectDemoUser() throws RSQLParserException {
        // given
        String rsqlQuery = "username==demo-user";

        // when
        List<User> users = selectUsers(rsqlQuery);

        //then
        assertThat(users).hasSize(1);
    }

    @Test
    public void shouldNotSelectNotExistingUser() throws RSQLParserException {
        // given
        String rsqlQuery = "username==not-existing";

        // when
        List<User> users = selectUsers(rsqlQuery);

        //then
        assertThat(users).isEmpty();
    }

    @Test
    public void shouldSelectDemoUserWhenUserNameAndEmailIsProvided() throws RSQLParserException {
        // given
        String rsqlQuery = "username==demo-user;email==demo-user@pnc.com";

        // when
        List<User> users = selectUsers(rsqlQuery);

        //then
        assertThat(users).hasSize(1);
    }

    @Test
    public void shouldNotSelectDemoUserWhenUserNameAndBadEmailIsProvided() throws RSQLParserException {
        // given
        String rsqlQuery = "username==demo-user;email==bad-email@pnc.com";

        // when
        List<User> users = selectUsers(rsqlQuery);

        //then
        assertThat(users).isEmpty();
    }

    private List<User> selectUsers(String rsqlQuery) throws RSQLParserException {
        return nullableStreamOf(userRepository.findAll(RSQLPredicateProducer.fromRSQL(User.class, rsqlQuery).toPredicate())).collect(Collectors.toList());
    }

}
