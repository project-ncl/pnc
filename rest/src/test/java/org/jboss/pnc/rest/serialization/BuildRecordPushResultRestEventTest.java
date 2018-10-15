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
package org.jboss.pnc.rest.serialization;

import org.assertj.core.api.Assertions;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.model.BuildRecordPushResult;
import org.jboss.pnc.rest.notifications.websockets.BuildRecordPushResultRestEvent;
import org.jboss.pnc.rest.restmodel.BuildRecordPushResultRest;
import org.junit.Test;

import java.util.Collections;

import org.jboss.pnc.enums.BuildPushStatus;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildRecordPushResultRestEventTest {

    @Test
    public void shouldHaveEventType() {
        //given
        BuildRecordPushResultRest buildRecordPushResultRest = new BuildRecordPushResultRest(
                1,
                2,
                BuildPushStatus.SUCCESS,
                "He's in the best selling show",
                Collections.emptyList(),
                3,
                "http://live.mars.un/"
        );
        BuildRecordPushResultRestEvent buildRecordPushResultRestEvent = new BuildRecordPushResultRestEvent(buildRecordPushResultRest);

        //when
        String json = JsonOutputConverterMapper.apply(buildRecordPushResultRestEvent);

        //then
        Assertions.assertThat(json).contains("BREW_PUSH_RESULT");

    }
}
