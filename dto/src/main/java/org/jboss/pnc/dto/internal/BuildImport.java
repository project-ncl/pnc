package org.jboss.pnc.dto.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.jboss.pnc.dto.validation.groups.WhenImporting;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.PastOrPresent;
import java.util.Date;

@Getter
@Jacksonized
@Builder(builderClassName = "Builder", toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildImport {

    @NotNull(groups = WhenImporting.class)
    private final @Valid BuildMeta metadata;

    @NotNull(groups = WhenImporting.class)
    private final @Valid BuildResultRest result;

    @NotNull(groups = WhenImporting.class)
    @Past(groups = WhenImporting.class)
    private final Date startTime;

    @NotNull(groups = WhenImporting.class)
    @PastOrPresent(groups = WhenImporting.class)
    private final Date endTime;
}
