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
package org.jboss.pnc.facade.providers;

import org.assertj.core.api.Condition;
import org.jboss.pnc.api.enums.AttachmentType;
import org.jboss.pnc.dto.Attachment;
import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.dto.BuildRef;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.attachments.BuildAttachmentAddedEvent;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.datastore.repositories.AttachmentRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import javax.enterprise.event.Event;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class AttachmentProviderTest extends AbstractIntIdProviderTest<org.jboss.pnc.model.Attachment> {

    @Mock
    private AttachmentRepository repository;

    @Mock
    Event<BuildAttachmentAddedEvent> event;

    @Spy
    @InjectMocks
    private AttachmentProviderImpl provider;

    @Override
    protected AbstractProvider provider() {
        return provider;
    }

    @Override
    protected AttachmentRepository repository() {
        return repository;
    }

    private org.jboss.pnc.model.Attachment attachment1;
    private org.jboss.pnc.model.Attachment attachment2;
    private org.jboss.pnc.model.Attachment attachment3;

    @Before
    public void fill() {
        Base32LongID buildId1 = new Base32LongID(Sequence.nextId());
        Base32LongID buildId2 = new Base32LongID(Sequence.nextId());
        attachment1 = prepareAttachment("build-log", "askdj", buildId1);
        attachment2 = prepareAttachment("alignment-log", "sdkjfha", buildId1);
        attachment3 = prepareAttachment("build-log", "kjadhf", buildId2);
        List<org.jboss.pnc.model.Attachment> attachments = new ArrayList<>(
                Arrays.asList(attachment1, attachment2, attachment3));
        fillRepository(attachments);
    }

    @Test
    public void testStore() {
        final String name = "Build Log";
        final String checksum = "ao3ud9031j0j1k123j";
        final String description = "description";
        final String url = "http://url.com/file.txt";
        final String buildId = "10";

        Attachment att = Attachment.builder()
                .name(name)
                .url(url)
                .md5(checksum)
                .build(BuildRef.refBuilder().id(buildId).build())
                .type(AttachmentType.LOG)
                .description(description)
                .build();

        Attachment stored = provider.store(att);
        assertThat(stored).isNotNull();
        assertThat(stored.getId()).isNotNull();
        assertThat(stored.getName()).isEqualTo(name);
        assertThat(stored.getUrl()).isEqualTo(url);
        assertThat(stored.getMd5()).isEqualTo(checksum);
        assertThat(stored.getDescription()).isEqualTo(description);
        assertThat(stored.getType()).isEqualTo(AttachmentType.LOG);
        assertThat(stored.getBuild()).isNotNull();
        assertThat(stored.getBuild().getId()).isEqualTo(buildId);
    }

    @Test
    public void testGetAll() {
        Page<Attachment> all = provider.getAll(0, 10, null, null);

        assertThat(all.getContent()).hasSize(3)
                .haveExactly(1, new Condition<>(a -> a.getMd5().equals("sdkjfha"), "Attachment present"));

    }

    @Test
    public void testUpdate() {
        Attachment toUpdate = Attachment.builder()
                .id(attachment1.getId().toString())
                .name(attachment1.getName())
                .url(attachment1.getUrl())
                .md5(attachment1.getMd5())
                .type(attachment1.getType())
                .creationTime(attachment1.getCreationTime().toInstant())
                .description("Changed description")
                .build(BuildRef.refBuilder().id(attachment1.getBuildRecord().getId().toString()).build())
                .build();

        assertThat(attachment1.getDescription()).isNotEqualTo(toUpdate.getDescription());

        Attachment updated = provider.update(toUpdate.getId(), toUpdate);

        assertThat(updated.getId()).isEqualTo(attachment1.getId().toString());
        assertThat(updated.getName()).isEqualTo(attachment1.getName());
        assertThat(updated.getUrl()).isEqualTo(attachment1.getUrl());
        assertThat(updated.getMd5()).isEqualTo(attachment1.getMd5());
        assertThat(updated.getDescription()).isEqualTo("Changed description");
        assertThat(updated.getType()).isEqualTo(attachment1.getType());
        assertThat(updated.getCreationTime()).isEqualTo(attachment1.getCreationTime().toInstant());
    }

    @Test
    public void testGetSpecific() {
        Attachment attachment = provider.getSpecific(attachment1.getId().toString());

        assertThat(attachment).isNotNull();
        assertThat(attachment.getId()).isEqualTo(attachment1.getId().toString());
        assertThat(attachment.getName()).isEqualTo(attachment1.getName());
        assertThat(attachment.getUrl()).isEqualTo(attachment1.getUrl());
        assertThat(attachment.getMd5()).isEqualTo(attachment1.getMd5());
        assertThat(attachment.getType()).isEqualTo(attachment1.getType());
        assertThat(attachment.getCreationTime()).isEqualTo(attachment1.getCreationTime().toInstant());
    }

    private org.jboss.pnc.model.Attachment prepareAttachment(String name, String checksum, Base32LongID buildId) {
        return org.jboss.pnc.model.Attachment.builder()
                .id(entityId.getAndIncrement())
                .url("http://url.com/" + name + ".txt")
                .md5(checksum)
                .name(name)
                .type(AttachmentType.LOG)
                .buildRecord(BuildRecord.Builder.newBuilder().id(buildId).build())
                .build();
    }
}
