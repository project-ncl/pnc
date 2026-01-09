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
package org.jboss.pnc.remotecoordinator.builder;

import org.jboss.pnc.bifrost.upload.BifrostLogUploader;
import org.jboss.pnc.bifrost.upload.BifrostUploadException;
import org.jboss.pnc.bifrost.upload.LogMetadata;

import javax.enterprise.inject.Alternative;
import java.io.File;
import java.net.URI;

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
