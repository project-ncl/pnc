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
package org.jboss.pnc.bpm.model.causeway;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 8/24/16 Time: 3:42 PM
 */
@Data
@NoArgsConstructor
public class MilestoneReleaseRest {
    private int milestoneId;
    private String userInitiator;

    public MilestoneReleaseRest(int milestoneId, String userInitiator) {
        this.milestoneId = milestoneId;
        this.userInitiator = userInitiator;
    }

    @Override
    public String toString() {
        return JsonOutputConverterMapper.apply(this);
    }
}
