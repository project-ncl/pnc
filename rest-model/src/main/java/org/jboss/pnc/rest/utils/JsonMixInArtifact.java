/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.utils;

import org.jboss.pnc.model.ArtifactType;
import org.jboss.pnc.model.BuiltArtifact;
import org.jboss.pnc.model.ImportedArtifact;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

/**
 * Provides annotations for serializing and deserializing from
 * org.jboss.pnc.model.Artifact subclasses into JSON
 */
@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "type")  
@JsonSubTypes({  
        @Type(value = BuiltArtifact.class, name = ArtifactType.BUILT),  
        @Type(value = ImportedArtifact.class, name = ArtifactType.IMPORTED) })
public abstract class JsonMixInArtifact {

}
