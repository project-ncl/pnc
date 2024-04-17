/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.mapper.abstracts;

import org.jboss.pnc.api.constants.Defaults;
import org.jboss.pnc.api.enums.Qualifier;
import org.jboss.pnc.common.alignment.ranking.AlignmentPredicate;
import org.jboss.pnc.common.alignment.ranking.AlignmentRanking;
import org.jboss.pnc.common.alignment.ranking.tokenizer.QualifierToken;
import org.jboss.pnc.common.alignment.ranking.tokenizer.Token;
import org.jboss.pnc.dto.AlignmentConfig;
import org.jboss.pnc.mapper.IntIdMapper;
import org.jboss.pnc.mapper.api.AlignConfigMapper;
import org.jboss.pnc.mapper.api.IdMapper;
import org.jboss.pnc.mapper.api.MapperCentralConfig;
import org.jboss.pnc.model.AlignConfig;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.predicates.BuildConfigurationSetPredicates;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.withName;
import static org.jboss.pnc.spi.datastore.predicates.ProductMilestonePredicates.*;
import static org.jboss.pnc.spi.datastore.predicates.ProductPredicates.withAbbrev;
import static org.jboss.pnc.spi.datastore.predicates.ProductVersionPredicates.withProductAbbreviation;
import static org.jboss.pnc.spi.datastore.predicates.ProductVersionPredicates.withVersion;

@Mapper(config = MapperCentralConfig.class, imports = { Defaults.class })
public abstract class AbstractAlignConfigMapper implements AlignConfigMapper {

    @Inject
    protected BuildConfigurationRepository bcRepository;

    @Inject
    protected BuildConfigurationSetRepository bcsRepository;

    @Inject
    protected ProductRepository productRepository;

    @Inject
    protected ProductMilestoneRepository productMilestoneRepository;

    @Inject
    protected ProductVersionRepository productVersionRepository;

    @Override
    public AlignConfig toModel(AlignmentConfig dto) {
        List<String> dtoRanks = dto.getRanks();
        List<List<Token>> dtoRanksTokens = Collections.emptyList();
        if (dtoRanks != null && !dtoRanks.isEmpty()) {
            AlignmentRanking compiledDtoRanks = new AlignmentRanking(dtoRanks, null);
            dtoRanksTokens = compiledDtoRanks.getRanksAsTokens();
        }
        List<String> idRanks = generateIdVersion_(dtoRanksTokens);

        String dtoDeny = dto.getDenyList();
        List<Token> dtoDenyTokens = Collections.emptyList();
        if (dtoDeny != null && !dtoDeny.trim().isEmpty()) {
            AlignmentPredicate compiledDtoDeny = new AlignmentPredicate(dtoDeny);
            dtoDenyTokens = compiledDtoDeny.getTokens();
        }
        String idDenies = generateIdVersion(dtoDenyTokens);

        String dtoAllow = dto.getAllowList();
        List<Token> dtoAllowTokens = Collections.emptyList();
        if (dtoAllow != null && !dtoAllow.trim().isEmpty()) {
            AlignmentPredicate compiledDtoAllow = new AlignmentPredicate(dtoAllow);
            dtoAllowTokens = compiledDtoAllow.getTokens();
        }
        String idAllows = generateIdVersion(dtoAllowTokens);

        return AlignConfig.builder()
                .ranks(dtoRanks)
                .idRanks(idRanks)
                .denyList(dtoDeny)
                .idDenyList(idDenies)
                .allowList(dtoAllow)
                .idAllowList(idAllows)
                .build();
    }

    private List<String> generateIdVersion_(List<List<Token>> rankTokens) {
        return rankTokens.stream()
                .map(this::convertToRanksWithIdReferences)
                .map(this::convertTokensToString)
                .collect(Collectors.toList());
    }

    private String generateIdVersion(List<Token> rankTokens) {
        return convertTokensToString(convertToRanksWithIdReferences(rankTokens));
    }

    private String convertTokensToString(List<Token> listOfTokens) {
        return listOfTokens.stream().map(Token::toString).collect(Collectors.joining(" "));
    }

    private List<Token> convertToRanksWithIdReferences(List<Token> rankTokens) {
        List<Token> idRank = new ArrayList<>();
        for (Token token : rankTokens) {
            Token toAdd = token;
            if (token instanceof QualifierToken) {
                QualifierToken qToken = (QualifierToken) token;

                toAdd = convertToIdToken(qToken);
            }
            idRank.add(toAdd);
        }
        return idRank;
    }

    /**
     * converts Qualifiers into their ID counterparts or returns them without change
     *
     * @param qualifierToken token to be converted
     * @return ID-converted qualifier or the same token
     */
    private QualifierToken convertToIdToken(QualifierToken qualifierToken) {
        // TODO make exhaustive with switch expressions after migration to Java 17
        switch (qualifierToken.qualifier) {
            case PRODUCT:
                return genericToIdToken(
                        () -> productRepository.queryByPredicates(withAbbrev(qualifierToken.parts[0])),
                        new IntIdMapper(),
                        qualifierToken,
                        "Couldn't find Product with abbreviation " + qualifierToken.parts[0]);
            case VERSION:
                return genericToIdToken(
                        () -> productVersionRepository.queryByPredicates(
                                withProductAbbreviation(qualifierToken.parts[0]),
                                withVersion(qualifierToken.parts[1])),
                        new IntIdMapper(),
                        qualifierToken,
                        "Couldn't find ProductVersion of Product " + qualifierToken.parts[0] + " and version "
                                + qualifierToken.parts[1]);
            case MILESTONE:
                return genericToIdToken(
                        () -> productMilestoneRepository.queryByPredicates(
                                withProductAbbreviationAndMilestoneVersion(
                                        qualifierToken.parts[0],
                                        qualifierToken.parts[1])),
                        new IntIdMapper(),
                        qualifierToken,
                        "Couldn't find ProductMilestone of Product " + qualifierToken.parts[0] + " and version "
                                + qualifierToken.parts[1]);
            case BUILD_CONFIG:
                return genericToIdToken(
                        () -> bcRepository.queryByPredicates(withName(qualifierToken.parts[0])),
                        new IntIdMapper(),
                        qualifierToken,
                        "Couldn't find BuildConfiguration with name " + qualifierToken.parts[0]);
            case GROUP_CONFIG:
                return genericToIdToken(
                        () -> bcsRepository
                                .queryByPredicates(BuildConfigurationSetPredicates.withName(qualifierToken.parts[0])),
                        new IntIdMapper(),
                        qualifierToken,
                        "Couldn't find BuildConfigurationSet with name " + qualifierToken.parts[0]);

            // no need to convert to ID versions (already is ID or enum)
            case BUILD:
            case GROUP_BUILD:
            case VERSION_ID:
            case MILESTONE_ID:
            case PRODUCT_ID:
            case BUILD_CONFIG_ID:
            case GROUP_CONFIG_ID:
            case DEPENDENCY:
            case QUALITY:
                return qualifierToken;
            default:
                throw new IllegalArgumentException("Unknown qualifier present");
        }
    }

    private <ID extends Serializable> QualifierToken genericToIdToken(
            Supplier<GenericEntity<ID>> entityResolver,
            IdMapper<ID, String> idMapper,
            QualifierToken token,
            String missingMessage) {
        GenericEntity<ID> entity = entityResolver.get();
        if (entity == null) {
            throw new IllegalArgumentException(missingMessage);
        }

        Qualifier qualifier = token.qualifier.getIdVersion().get();

        return new QualifierToken(0, 0, qualifier, new String[] { idMapper.toDto(entity.getId()) });
    }

    @Override
    public void updateEntity(AlignmentConfig dto, AlignConfig model) {

        // RANKS
        List<String> dtoRanks = dto.getRanks();
        List<List<Token>> dtoRanksTokens = Collections.emptyList();
        if (dtoRanks != null && !dtoRanks.isEmpty()) {
            AlignmentRanking compiledDtoRanks = new AlignmentRanking(dtoRanks, null);
            dtoRanksTokens = compiledDtoRanks.getRanksAsTokens();
        }

        List<String> modelRanks = model.getRanks();
        List<List<Token>> modelRanksTokens = Collections.emptyList();
        if (modelRanks != null && !modelRanks.isEmpty()) {
            AlignmentRanking compiledModelRanks = new AlignmentRanking(modelRanks, null);
            modelRanksTokens = compiledModelRanks.getRanksAsTokens();
        }

        // modify and regenerate rankings if ranks have changed
        if (!ranksAreEqual(dtoRanksTokens, modelRanksTokens)) {
            model.setRanks(dtoRanks);
            model.setIdRanks(generateIdVersion_(dtoRanksTokens));
        }

        // DENY LIST
        String dtoDeny = dto.getDenyList();
        List<Token> dtoDenyTokens = Collections.emptyList();
        if (dtoDeny != null && !dtoDeny.trim().isEmpty()) {
            AlignmentPredicate compiledDtoDeny = new AlignmentPredicate(dtoDeny);
            dtoDenyTokens = compiledDtoDeny.getTokens();
        }

        String modelDeny = model.getDenyList();
        List<Token> modelDenyTokens = Collections.emptyList();
        if (modelDeny != null && !modelDeny.trim().isEmpty()) {
            AlignmentPredicate compiledModelDeny = new AlignmentPredicate(modelDeny);
            modelDenyTokens = compiledModelDeny.getTokens();
        }

        if (!dtoDenyTokens.equals(modelDenyTokens)) {
            model.setDenyList(dtoDeny);
            model.setIdDenyList(generateIdVersion(dtoDenyTokens));
        }

        // ALLOW LIST
        String dtoAllow = dto.getAllowList();
        List<Token> dtoAllowTokens = Collections.emptyList();
        if (dtoAllow != null && !dtoAllow.trim().isEmpty()) {
            AlignmentPredicate compiledDtoAllow = new AlignmentPredicate(dtoAllow);
            dtoAllowTokens = compiledDtoAllow.getTokens();
        }

        String modelAllow = dto.getAllowList();
        List<Token> modelAllowTokens = Collections.emptyList();
        if (modelAllow != null && !modelAllow.trim().isEmpty()) {
            AlignmentPredicate compiledModelAllow = new AlignmentPredicate(modelAllow);
            modelAllowTokens = compiledModelAllow.getTokens();
        }

        if (!dtoAllowTokens.equals(modelAllowTokens)) {
            model.setAllowList(dtoAllow);
            model.setIdAllowList(generateIdVersion(dtoAllowTokens));
        }
    }

    /**
     * Returns true if EVERY parsed rank and with tokens of that rank are THE SAME and have THE SAME ORDER. Difference
     * in whitespaces between first and second will not affect outcome.
     *
     * @param first list of ranks
     * @param second list of ranks
     * @return true if ranks are equal in meaning, false otherwise
     */
    public boolean ranksAreEqual(List<List<Token>> first, List<List<Token>> second) {
        return first.equals(second);
    }

    @Mapping(
            target = "dependencyOverride",
            expression = "java( Defaults.GLOBAL_SCOPE.equals(dependencyScope) ? null : dependencyScope )")
    @BeanMapping(ignoreUnmappedSourceProperties = { "idRanks", "idDenyList", "idAllowList" })
    public abstract AlignmentConfig toDto(AlignConfig config, String dependencyScope);
}
