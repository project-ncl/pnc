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

package org.jboss.pnc.messaging.spi;

import lombok.Getter;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;

import java.util.List;
import java.util.Optional;

/**
 * @author jakubvanko
 */
@Getter
public class AnalysisStatusMessage implements Message {

    private final String attribute;
    private final Optional<String> milestoneId;
    private final String status;
    private final List<String> deliverablesUrls;

    public AnalysisStatusMessage(
            String attribute,
            Optional<String> milestoneId,
            String status,
            List<String> deliverablesUrls) {
        this.attribute = attribute;
        this.milestoneId = milestoneId;
        this.status = status;
        this.deliverablesUrls = deliverablesUrls;
    }

    @Override
    public String toJson() {
        return JsonOutputConverterMapper.apply(this);
    }
}
