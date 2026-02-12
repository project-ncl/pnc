package org.jboss.pnc.dto.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenImporting;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Past;
import java.util.Date;
import java.util.List;

@Getter
@Jacksonized
@Builder(builderClassName = "Builder", toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildMeta {

    @NotNull(groups = WhenCreatingNew.class)
    @Null(groups = WhenImporting.class)
    String id;

    @NotNull(groups = { WhenCreatingNew.class, WhenImporting.class })
    IdRev idRev;

    String contentId;

    boolean temporaryBuild;

    AlignmentPreference alignmentPreference;

    @NotNull(groups = { WhenCreatingNew.class, WhenImporting.class })
    @Past(groups = { WhenCreatingNew.class, WhenImporting.class })
    Date submitTime;

    @NotBlank(groups = { WhenCreatingNew.class, WhenImporting.class })
    String username;

    Integer productMilestoneId;

    String noRebuildCauseId;

    List<@NotNull(groups = { WhenCreatingNew.class, WhenImporting.class }) String> dependants;

    List<@NotNull(groups = { WhenCreatingNew.class, WhenImporting.class }) String> dependencies;
}
