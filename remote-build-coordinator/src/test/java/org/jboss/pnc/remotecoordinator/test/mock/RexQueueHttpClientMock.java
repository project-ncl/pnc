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
package org.jboss.pnc.remotecoordinator.test.mock;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.pnc.remotecoordinator.rexclient.RexQueueHttpClient;
import org.jboss.pnc.rex.dto.responses.LongResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@ApplicationScoped
@Alternative
@RestClient
public class RexQueueHttpClientMock implements RexQueueHttpClient {
    @Override
    public void setConcurrent(@NotNull @Min(0L) Long amount) {
    }

    @Override
    public LongResponse getConcurrent() {
        return LongResponse.builder().number(5L).build();
    }

    @Override
    public LongResponse getRunning() {
        return LongResponse.builder().number(5L).build();
    }
}
