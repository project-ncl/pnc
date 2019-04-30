package org.jboss.pnc.dto.internal.bpm;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;
import org.jboss.pnc.dto.SCMRepository;

import java.io.Serializable;

@Data
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = RepositoryCreationProcess.Builder.class)
public class RepositoryCreationProcess implements Serializable {
    private SCMRepository scmRepository;

    private String  revision;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder{

    }
}
