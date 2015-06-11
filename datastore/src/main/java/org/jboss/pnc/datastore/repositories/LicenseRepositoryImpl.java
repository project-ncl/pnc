package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.datastore.repositories.internal.LicenseSpringRepository;
import org.jboss.pnc.model.License;
import org.jboss.pnc.spi.datastore.repositories.LicenseRepository;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class LicenseRepositoryImpl extends AbstractRepository<License, Integer> implements LicenseRepository {

    /**
     * @deprecated Created for CDI.
     */
    @Deprecated
    public LicenseRepositoryImpl() {
        super(null, null);
    }

    @Inject
    public LicenseRepositoryImpl(LicenseSpringRepository licenseSpringRepository) {
        super(licenseSpringRepository, licenseSpringRepository);
    }
}
