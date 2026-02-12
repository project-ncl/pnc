package org.jboss.pnc.dto.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.jboss.pnc.dto.validation.groups.WhenImporting;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Jacksonized
@Builder(builderClassName = "Builder")
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImportBuildsRequest {

    @Valid
    @NotNull(groups = WhenImporting.class)
    private final List<@NotNull(groups = WhenImporting.class) BuildImport> imports;
}
