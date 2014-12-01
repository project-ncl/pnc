package org.jboss.pnc.model;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * The Enum OperationalSystem maps the different environment available for the builds
 */
@Entity
@Table(name = "operational_system")
@NamedQuery(name = "OperationalSystem.findAll", query = "SELECT o FROM OperationalSystem o")
public enum OperationalSystem {

    WINDOWS,

    LINUX,

    OSX
}
