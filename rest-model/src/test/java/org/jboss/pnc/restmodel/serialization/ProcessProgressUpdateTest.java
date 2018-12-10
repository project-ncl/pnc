/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.restmodel.serialization;

import org.jboss.pnc.enums.BPMTaskStatus;
import org.jboss.pnc.rest.restmodel.bpm.ProcessProgressUpdate;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ProcessProgressUpdateTest {

    private final Logger log = LoggerFactory.getLogger(ProcessProgressUpdateTest.class);

    @Test
    public void serializeAndDeserialize() throws IOException {
        ProcessProgressUpdate processProgressUpdate = new ProcessProgressUpdate("repour",
                BPMTaskStatus.STARTED,
                "ws://repour/ws-endpoint");
        String serialized = JsonOutputConverterMapper.apply(processProgressUpdate);
        log.info("Serialized:" + serialized);

        ProcessProgressUpdate processProgressUpdateDeserialized = JsonOutputConverterMapper.readValue(serialized,
                ProcessProgressUpdate.class);

        Assert.assertEquals(processProgressUpdate.getDetailedNotificationsEndpointUrl(), processProgressUpdateDeserialized.getDetailedNotificationsEndpointUrl());
    }
}