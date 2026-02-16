/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.integration.endpoints;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.api.enums.AttachmentType;
import org.jboss.pnc.client.AttachmentClient;
import org.jboss.pnc.client.BuildClient;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.dto.Attachment;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildRef;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.integration.setup.RestClientConfiguration;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class AttachmentEndpointTest {

    private static final Logger logger = LoggerFactory.getLogger(AttachmentEndpointTest.class);
    private static Build build1;

    @BeforeClass
    public static void prepareData() throws Exception {
        BuildClient bc = new BuildClient(RestClientConfiguration.asAnonymous());
        RemoteCollection<Build> builds = bc.getAll(null, null);

        Iterator<Build> it = builds.getAll()
                .stream()
                .sorted(Comparator.comparingLong(build -> new Base32LongID(build.getId()).getLongId()))
                .iterator();

        build1 = it.next();
    }

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @Test
    @InSequence(10)
    public void testGetAll() throws RemoteResourceException {
        // given
        AttachmentClient client = new AttachmentClient(RestClientConfiguration.asAnonymous());

        // when
        Collection<Attachment> all = client.getAll(null).getAll();

        // then
        assertThat(all).hasSize(11);
        assertThat(all).extracting("build").extracting("id").doesNotContainNull();
    }

    @Test
    public void testGetByMd5() throws RemoteResourceException {
        // given
        String searchedChecksum = "md5-fake-abcdefhijklmno"; // from DemoDataInitializer
        AttachmentClient client = new AttachmentClient(RestClientConfiguration.asAnonymous());

        // when
        Collection<Attachment> all = client.getAll(searchedChecksum).getAll();

        // then
        assertThat(all).hasSize(1);

        Attachment att = all.iterator().next();
        assertThat(att.getId()).isNotNull().isNotBlank();
        assertThat(att.getName()).isEqualTo("Build Log");
        assertThat(att.getMd5()).isEqualTo(searchedChecksum);
    }

    @Test
    public void testGetWithRsqlByUrl() throws RemoteResourceException {
        // given
        String searchedUrl = "http://url.com/attachments/build-cccc/alignment-log.log"; // from DemoDataInitializer
        AttachmentClient client = new AttachmentClient(RestClientConfiguration.asAnonymous());

        // when
        Collection<Attachment> all = client.getAll(null, Optional.empty(), Optional.of("url==\"" + searchedUrl + "\""))
                .getAll();

        // then
        assertThat(all).hasSize(1);

        Attachment att = all.iterator().next();
        assertThat(att.getId()).isNotNull().isNotBlank();
        assertThat(att.getName()).isEqualTo("Alignment Log");
        assertThat(att.getUrl()).isEqualTo(searchedUrl);
    }

    @Test
    @InSequence(10)
    public void testGetByRecord() throws RemoteResourceException {
        // given
        AttachmentClient client = new AttachmentClient(RestClientConfiguration.asAnonymous());
        String buildId1 = build1.getId();

        // when
        Collection<Attachment> all = client.getAll(null, Optional.empty(), Optional.of("build.id==" + buildId1))
                .getAll();

        // then
        assertThat(all).hasSize(2);
        assertThat(all).extracting("build").extracting("id").allMatch(buildId1::equals);
    }

    @Test
    public void testGetByType() throws RemoteResourceException {
        // given
        AttachmentType searchedType = AttachmentType.SBOM; // from DemoDataInitializer
        AttachmentClient client = new AttachmentClient(RestClientConfiguration.asAnonymous());

        // when
        Collection<Attachment> all = client.getAll(null, Optional.empty(), Optional.of("type==" + searchedType))
                .getAll();

        // then
        assertThat(all).hasSize(1);

        Attachment att = all.iterator().next();
        assertThat(att.getId()).isNotNull().isNotBlank();
        assertThat(att.getName()).isEqualTo("SBOM");
        assertThat(att.getMd5()).isEqualTo("md5-fake-abcdefhijklxcs144");
    }

    @Test
    @InSequence(20)
    public void testCreate() throws RemoteResourceException {
        // given
        BuildRef build = BuildRef.refBuilder().id(build1.getId()).build();
        String name = "Provenance Attestation"; // from DemoDataInitializer
        String description = "This is a provenance attestation"; // from DemoDataInitializer
        String md5 = "md5-asdfghjkl-000111"; // from DemoDataInitializer
        String url = "https://secure.com/attestations/xxxxx"; // from DemoDataInitializer
        Attachment attachment = Attachment.builder()
                .name(name)
                .description(description)
                .md5(md5)
                .url(url)
                .type(AttachmentType.PROVENANCE)
                .build(build)
                .build();
        AttachmentClient client = new AttachmentClient(RestClientConfiguration.asSystem());

        // when
        Attachment saved = client.create(attachment);

        // then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull().isNotBlank();
        assertThat(saved.getName()).isEqualTo(name);
        assertThat(saved.getDescription()).isEqualTo(description);
        assertThat(saved.getMd5()).isEqualTo(md5);
        assertThat(saved.getUrl()).isEqualTo(url);
        assertThat(saved.getType()).isEqualTo(AttachmentType.PROVENANCE);

        BuildRef ref = saved.getBuild();
        assertThat(ref).isNotNull();
        assertThat(ref.getId()).isEqualTo(build.getId());
        assertThat(ref.getScmRevision()).isEqualTo(build1.getScmRevision());
        assertThat(ref.getStartTime()).isEqualTo(build1.getStartTime());
        assertThat(ref.getSubmitTime()).isEqualTo(build1.getSubmitTime());
        assertThat(ref.getEndTime()).isEqualTo(build1.getEndTime());
    }

    @Test
    public void testGetSpecific() throws RemoteResourceException {
        // given
        String searchedId = "100"; // from DemoDataInitializer
        AttachmentClient client = new AttachmentClient(RestClientConfiguration.asAnonymous());

        // when
        Attachment attachment = client.getSpecific(searchedId);

        // then
        assertThat(attachment).isNotNull();
        assertThat(attachment.getId()).isEqualTo(searchedId);
        assertThat(attachment.getName()).isEqualTo("Build Log");
        assertThat(attachment.getType()).isEqualTo(AttachmentType.LOG);

    }

    @Test
    @InSequence(20)
    public void testUpdate() throws RemoteResourceException {
        // given
        String searchedId = "100"; // from DemoDataInitializer
        AttachmentClient client = new AttachmentClient(RestClientConfiguration.asSystem());
        Attachment attachment = client.getSpecific(searchedId);

        String updatedDesc = "Something something";
        Attachment toUpdate = attachment.toBuilder().description(updatedDesc).build();

        // when
        client.update(toUpdate.getId(), toUpdate);
        Attachment updated = client.getSpecific(searchedId);

        // then
        assertThat(updated).isNotNull();
        assertThat(updated.getId()).isEqualTo(searchedId);
        assertThat(updated.getDescription()).isEqualTo(updatedDesc);
        assertThat(updated.getDescription()).isNotEqualTo(attachment.getDescription());
        assertThat(updated.getName()).isEqualTo("Build Log");
        assertThat(updated.getType()).isEqualTo(AttachmentType.LOG);

    }
}
