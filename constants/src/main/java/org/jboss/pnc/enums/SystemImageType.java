/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
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
