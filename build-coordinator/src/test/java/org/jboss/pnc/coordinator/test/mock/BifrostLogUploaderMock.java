/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.coordinator.test.mock;

import java.io.File;
import java.net.URI;

import javax.enterprise.inject.Alternative;

import org.jboss.pnc.bifrost.upload.BifrostLogUploader;
import org.jboss.pnc.bifrost.upload.BifrostUploadException;
import org.jboss.pnc.bifrost.upload.LogMetadata;

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
}
