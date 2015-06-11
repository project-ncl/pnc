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
package org.jboss.pnc.rest.provider;

import com.google.common.base.Strings;
import org.jboss.pnc.core.builder.BuildCoordinator;
import org.jboss.pnc.core.builder.BuildTask;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.StreamingOutput;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.*;

@Stateless
public class BuildRecordProvider {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private BuildRecordRepository buildRecordRepository;

    private BuildCoordinator buildCoordinator;

    private RSQLPredicateProducer rsqlPredicateProducer;

    private SortInfoProducer sortInfoProducer;

    private PageInfoProducer pageInfoProducer;

    public BuildRecordProvider() {
    }

    @Inject
    public BuildRecordProvider(BuildRecordRepository buildRecordRepository, BuildCoordinator buildCoordinator,
            PageInfoProducer pageInfoProducer, RSQLPredicateProducer rsqlPredicateProducer, SortInfoProducer sortInfoProducer) {
        this.buildRecordRepository = buildRecordRepository;
        this.buildCoordinator = buildCoordinator;
        this.rsqlPredicateProducer = rsqlPredicateProducer;
        this.sortInfoProducer = sortInfoProducer;
        this.pageInfoProducer = pageInfoProducer;
    }

    public List<BuildRecordRest> getAllArchived(int pageIndex, int pageSize, String sortingRsql, String query) {
        Predicate<BuildRecord> rsqlPredicate = rsqlPredicateProducer.getPredicate(BuildRecord.class, query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sortingRsql);
        return nullableStreamOf(buildRecordRepository.queryWithPredicates(pageInfo, sortInfo, rsqlPredicate))
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public List<BuildRecordRest> getAllRunning(Integer pageIndex, Integer pageSize, String sortingRsql, String rsql) {
        if(!Strings.isNullOrEmpty(sortingRsql)) {
            logger.warn("Sorting RSQL is not supported, ignoring");
        }

        if(!Strings.isNullOrEmpty(rsql)) {
            logger.warn("Querying RSQL is not supported, ignoring");
        }

        return nullableStreamOf(buildCoordinator.getBuildTasks()).map(submittedBuild -> new BuildRecordRest(submittedBuild))
                .skip(pageIndex * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
    }

    public List<BuildRecordRest> getAllArchivedOfBuildConfiguration(int pageIndex, int pageSize, String sortingRsql, String query, Integer buildRecordId) {
        Predicate<BuildRecord> rsqlPredicate = rsqlPredicateProducer.getPredicate(BuildRecord.class, query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sortingRsql);
        return nullableStreamOf(buildRecordRepository.queryWithPredicates(pageInfo, sortInfo, rsqlPredicate, withBuildRecordId(buildRecordId)))
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public List<BuildRecordRest> getAllForBuildConfiguration(int pageIndex, int pageSize, String sortingRsql, String query, Integer configurationId) {
        Predicate<BuildRecord> rsqlPredicate = rsqlPredicateProducer.getPredicate(BuildRecord.class, query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sortingRsql);
        return nullableStreamOf(buildRecordRepository.queryWithPredicates(pageInfo, sortInfo, rsqlPredicate, withBuildConfigurationId(configurationId)))
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public List<BuildRecordRest> getAllForProject(int pageIndex, int pageSize, String sortingRsql, String query, Integer projectId) {
        Predicate<BuildRecord> rsqlPredicate = rsqlPredicateProducer.getPredicate(BuildRecord.class, query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sortingRsql);
        return nullableStreamOf(buildRecordRepository.queryWithPredicates(pageInfo, sortInfo, rsqlPredicate, withProjectId(projectId)))
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public BuildRecordRest getSpecific(Integer id) {
        BuildRecord buildRecord = buildRecordRepository.queryById(id);
        if (buildRecord != null) {
            return new BuildRecordRest(buildRecord);
        }
        return null;
    }
    

    public String getBuildRecordLog(Integer id) {
        BuildRecord buildRecord = buildRecordRepository.queryById(id);
        if(buildRecord != null)
            return buildRecord.getBuildLog();
        else 
            return null;
    }
    
    public StreamingOutput getLogsForBuild(String buildRecordLog) {
        if(buildRecordLog == null)
            return null;
        
        return outputStream -> {
            Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.write(buildRecordLog);
            writer.flush();
        };
    }

    public BuildRecordRest getSpecificRunning(Integer id) {
        BuildTask buildTask = getSubmittedBuild(id);
        if (buildTask != null) {
            return new BuildRecordRest(buildTask);
        }
        return null;
    }

    private BuildTask getSubmittedBuild(Integer id) {
        List<BuildTask> buildTasks = buildCoordinator.getBuildTasks().stream()
                .filter(submittedBuild -> id.equals(submittedBuild.getBuildConfiguration().getId()))
                .collect(Collectors.toList());
        if (!buildTasks.isEmpty()) {
            return buildTasks.iterator().next();
        }
        return null;
    }

    public String getSubmittedBuildLog(Integer id) {
        BuildTask buildTask = getSubmittedBuild(id);
        if (buildTask != null ) 
            return buildTask.getBuildLog();
        else
            return null;
    }

    public StreamingOutput getStreamingOutputForString(String str) {
        if(str == null)
            return null;
            
        return outputStream -> {
            Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.write(str);
            writer.flush();
        };
    }
    public Function<? super BuildRecord, ? extends BuildRecordRest> toRestModel() {
        return buildRecord -> new BuildRecordRest(buildRecord);
    }
}
