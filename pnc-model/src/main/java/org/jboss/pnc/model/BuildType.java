package org.jboss.pnc.model;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 * 
 * Class that indicates the build type, whether is standard Java, a native build, a Docker build
 * 
 * TODO: this class needs to be further discussed (i.e. Docker stands for the builds of Docker images?) and specialized (i.e.
 * what kind of Native builds?)
 */
@Entity
@Table(name = "build_type")
@NamedQuery(name = "BuildType.findAll", query = "SELECT b FROM BuildType b")
public enum BuildType {

    JAVA,

    DOCKER,

    NATIVE;

}
