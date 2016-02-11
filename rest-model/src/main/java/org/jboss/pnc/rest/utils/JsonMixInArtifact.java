package org.jboss.pnc.rest.utils;

import org.jboss.pnc.model.ArtifactType;
import org.jboss.pnc.model.BuiltArtifact;
import org.jboss.pnc.model.ImportedArtifact;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "type")  
@JsonSubTypes({  
        @Type(value = BuiltArtifact.class, name = ArtifactType.BUILT),  
        @Type(value = ImportedArtifact.class, name = ArtifactType.IMPORTED) })
public abstract class JsonMixInArtifact {

}
