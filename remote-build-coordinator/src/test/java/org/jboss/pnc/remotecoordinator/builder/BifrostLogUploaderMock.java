/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.builder;

import java.io.File;
import java.net.URI;

import javax.enterprise.inject.Alternative;

import org.jboss.pnc.api.bifrost.dto.Checksums;
import org.jboss.pnc.bifrost.upload.BifrostLogUploader;
import org.jboss.pnc.bifrost.upload.BifrostUploadException;
import org.jboss.pnc.bifrost.upload.LogMetadata;
import org.jboss.pnc.bifrost.upload.TagOption;

@Alternative
public class BifrostLogUploaderMock extends BifrostLogUploader {
    public BifrostLogUploaderMock() {
        super(URI.create("http://example.com"), () -> "", 1, 1);
    }

    @Override
    public void uploadFile(File logfile, LogMetadata metadata) throws BifrostUploadException {

    }

    @Override
    public void uploadFile(File logfile, LogMetadata metadata, String md5sum) throws BifrostUploadException {
    }

    @Override
    public void uploadString(String log, LogMetadata metadata) throws BifrostUploadException {
    }

    @Override
    public void uploadString(String log, LogMetadata metadata, String md5sum) throws BifrostUploadException {
    }

    @Override
    public Checksums getChecksums(String processContext, TagOption tag) throws BifrostUploadException {
        return Checksums.builder()
                .md5("a57ec266854321dd2205727281a8a7d8")
                .sha256("845acbfedd45309dfd9d11f5900dbf65289cfa37f5f683957c6784d3da46755f")
                .build();
    }
}
