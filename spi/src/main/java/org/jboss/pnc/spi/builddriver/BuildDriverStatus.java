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
package org.jboss.pnc.spi.builddriver;

import org.jboss.pnc.model.BuildStatus;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 *
 * @deprecated  Use {@link BuildStatus} instead
 */
@Deprecated
public enum BuildDriverStatus {
    SUCCESS(true), FAILED, UNSTABLE(true), BUILDING, REJECTED, CANCELLED, UNKNOWN;

    private boolean completedSuccessfully;

    private BuildDriverStatus(boolean completedSuccessfully){
        this.completedSuccessfully = completedSuccessfully;
    }

    private BuildDriverStatus(){
        this.completedSuccessfully = false;
    }

    public boolean completedSuccessfully(){
        return this.completedSuccessfully;
    }

    /**
     * Converts BuildDriverStatus to BuildStatus
     * 
     * @return Corresponding BuildStatus
     */
    public BuildStatus toBuildStatus() {
        switch (this) {
            case SUCCESS:
                return BuildStatus.SUCCESS;
            case FAILED:
                return BuildStatus.FAILED;
            case UNSTABLE:
                return BuildStatus.UNSTABLE;
            case BUILDING:
                return BuildStatus.BUILDING;
            case REJECTED:
                return BuildStatus.REJECTED;
            case CANCELLED:
                return BuildStatus.CANCELLED;
            case UNKNOWN:
                return BuildStatus.UNKNOWN;
            default:
                throw new IllegalStateException("Bad design of BuildDriverStatus enum type");

        }
    }
}
