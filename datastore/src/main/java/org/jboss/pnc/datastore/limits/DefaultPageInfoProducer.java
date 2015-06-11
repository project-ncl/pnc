package org.jboss.pnc.datastore.limits;

import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.impl.DefaultPageInfo;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DefaultPageInfoProducer implements PageInfoProducer {

    @Override
    public PageInfo getPageInfo(int offset, int size) {
        return new DefaultPageInfo(offset, size);
    }
}
