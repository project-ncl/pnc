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
package org.jboss.pnc.rest.provider;

import org.jboss.pnc.model.BuildRecordPushResult;
import org.jboss.pnc.rest.restmodel.BuildRecordPushResultRest;

import javax.ejb.Stateless;
import java.util.function.Function;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Stateless
public class BuildRecordPushResultProvider extends AbstractProvider<BuildRecordPushResult, BuildRecordPushResultRest>{

    @Override
    protected Function<? super BuildRecordPushResult, ? extends BuildRecordPushResultRest> toRESTModel() {
        return buildRecordPushResult -> new BuildRecordPushResultRest(buildRecordPushResult);
    }

    @Override
    protected Function<? super BuildRecordPushResultRest, ? extends BuildRecordPushResult> toDBModel() {
        return buildRecordPushResultRest -> buildRecordPushResultRest.toDBEntityBuilder().build();
    }
}
