package org.jboss.pnc.datastore.repositories.internal;

import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.io.Serializable;
import java.util.List;

public class AbstractRepository<T extends GenericEntity<ID>, ID extends Serializable> implements Repository<T, ID> {

    protected JpaRepository<T, ID> springRepository;
    protected JpaSpecificationExecutor<T> springSpecificationsExecutor;

    public AbstractRepository() {
    }

    public AbstractRepository(JpaRepository<T, ID> springRepository, JpaSpecificationExecutor<T> springSpecificationsExecutor) {
        this.springRepository = springRepository;
        this.springSpecificationsExecutor = springSpecificationsExecutor;
    }

    @Override
    public T save(T entity) {
        return springRepository.save(entity);
    }

    @Override
    public void delete(ID id) {
        springRepository.delete(id);
    }

    @Override
    public List<T> queryAll() {
        return springRepository.findAll();
    }

    @Override
    public List<T> queryAll(PageInfo pageInfo, SortInfo sortInfo) {
        return springRepository.findAll(PageableMapper.map(pageInfo, sortInfo)).getContent();
    }

    @Override
    public T queryById(ID id) {
        return springRepository.findOne(id);
    }

    @Override
    public T queryByPredicates(Predicate<T>... predicates) {
        return springSpecificationsExecutor.findOne(SpecificationsMapper.map(predicates));
    }

    @Override
    public int count(Predicate<T>... predicates) {
        return queryWithPredicates(predicates).size();
    }

    @Override
    public List<T> queryWithPredicates(Predicate<T>... predicates) {
        return springSpecificationsExecutor.findAll(SpecificationsMapper.map(predicates));
    }

    @Override
    public List<T> queryWithPredicates(PageInfo pageInfo, SortInfo sortInfo, Predicate<T>... predicates) {
        return springSpecificationsExecutor.findAll(SpecificationsMapper.map(predicates), PageableMapper.map(pageInfo, sortInfo)).getContent();
    }


}
