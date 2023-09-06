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
package org.jboss.pnc.dto.requests;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * This type of request differs from {@link DeliverablesAnalysisRequest} in a way that this request is used to start
 * only "SCRATCH" analysis.
 */
@Data
@Jacksonized
@Builder
public class ScratchDeliverablesAnalysisRequest {

    @NotEmpty
    private final List<@URL String> deliverablesUrls;
}
