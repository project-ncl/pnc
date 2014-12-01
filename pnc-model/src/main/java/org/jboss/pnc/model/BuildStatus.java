package org.jboss.pnc.model;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 * 
 * This class indicates the progress of a build: finished with success, in progress, cancelled or finished with errors
 */

@Entity
@Table(name = "build_status")
@NamedQuery(name = "BuildStatus.findAll", query = "SELECT b FROM BuildStatus b")
public enum BuildStatus {

    SUCCESS,

    IN_PROGRESS,

    CANCELLED,

    FAILED;

}
