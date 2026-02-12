package org.jboss.pnc.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
@Builder(builderClassName = "Builder")
@AllArgsConstructor
public class IdRev {

    private final Integer id;

    private final Integer rev;
}
