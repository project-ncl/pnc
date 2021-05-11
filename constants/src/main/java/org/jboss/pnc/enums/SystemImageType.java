/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.enums;

/**
 * Enum that represents the type of the build environment system image which will be used for the build. The system
 * image type indicates which build environment driver(s) is capable of initializing the environment (container, vm,
 * etc) in which the build will run.
 * 
 * @deprecated use pnc-api
 */
@Deprecated
public enum SystemImageType {

    /**
     * A Docker-formatted image that will be used to create a container where to run the build.
     */
    DOCKER_IMAGE,

    /**
     * A raw virtual machine image.
     */
    VIRTUAL_MACHINE_RAW,

    /**
     * A virtual machine image in the qcow2 format.
     */
    VIRTUAL_MACHINE_QCOW2,

    /**
     * The local operating system will be used to run the build, Note, that this should not be used in a production
     * environment because allows for non reproducible builds if the local system changes.
     */
    LOCAL_WORKSPACE

}
