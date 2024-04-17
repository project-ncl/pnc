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
package org.jboss.pnc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.jboss.pnc.model.utils.DelimitedStringListType;

import javax.persistence.Embeddable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@TypeDef(name = "delimited_strings", typeClass = DelimitedStringListType.class, defaultForType = List.class)
public class AlignConfig {

    @Type(type = "delimited_strings")
    private List<String> ranks = new ArrayList<>();

    @Type(type = "delimited_strings")
    private List<String> idRanks = new ArrayList<>();

    private String denyList;

    private String idDenyList;

    private String allowList;

    private String idAllowList;
}
