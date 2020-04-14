/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.bpm.test;

import org.jboss.pnc.bpm.causeway.Result;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.enums.BuildPushStatus;
import org.junit.Test;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ResultSerializationTest {

    @Test
    public void shouldSerializeAndDeserialize() throws Exception {
        Result result = new Result("1", BuildPushStatus.SUCCESS, "");
        String json = JsonOutputConverterMapper.apply(result);
        JsonOutputConverterMapper.readValue(json, Result.class);
    }
}
