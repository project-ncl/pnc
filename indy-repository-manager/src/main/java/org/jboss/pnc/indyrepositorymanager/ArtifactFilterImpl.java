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
package org.jboss.pnc.indyrepositorymanager;

import org.commonjava.indy.folo.dto.TrackedContentEntryDTO;
import org.commonjava.indy.model.core.StoreKey;
import org.jboss.pnc.common.json.moduleconfig.IndyRepoDriverModuleConfig.IgnoredPatterns;
import org.jboss.pnc.common.json.moduleconfig.IndyRepoDriverModuleConfig.PatternsList;

import java.util.List;
import java.util.regex.Pattern;

import static org.commonjava.indy.model.core.GenericPackageTypeDescriptor.GENERIC_PKG_KEY;
import static org.commonjava.indy.pkg.maven.model.MavenPackageTypeDescriptor.MAVEN_PKG_KEY;
import static org.commonjava.indy.pkg.npm.model.NPMPackageTypeDescriptor.NPM_PKG_KEY;

/**
 * Default implementation of artifact filter allowing filtering artifacts by path patterns and also filtering downloads
 * for promotion by store key patterns.
 *
 * @author pkocandr
 */
public class ArtifactFilterImpl implements ArtifactFilter {

    private IgnoredPatterns ignoredPathPatternsPromotion;

    private IgnoredPatterns ignoredPathPatternsData;

    private PatternsList ignoredRepoPatterns;

    public ArtifactFilterImpl(
            IgnoredPatterns ignoredPathPatternsPromotion,
            IgnoredPatterns ignoredPathPatternsData,
            List<String> ignoredRepoPatterns) {
        super();
        this.ignoredPathPatternsPromotion = ignoredPathPatternsPromotion;
        this.ignoredPathPatternsData = ignoredPathPatternsData;
        this.ignoredRepoPatterns = new PatternsList(ignoredRepoPatterns);
    }

    @Override
    public boolean acceptsForPromotion(TrackedContentEntryDTO artifact, boolean download) {
        boolean result = true;

        String path = artifact.getPath();
        StoreKey storeKey = artifact.getStoreKey();
        if (download && ignoreDependencySource(storeKey)) {
            result = false;
        } else if (ignoreContent(ignoredPathPatternsPromotion, storeKey.getPackageType(), path)) {
            result = false;
        }
        return result;
    }

    @Override
    public boolean acceptsForData(TrackedContentEntryDTO artifact) {
        boolean result = true;

        String path = artifact.getPath();
        StoreKey storeKey = artifact.getStoreKey();
        if (ignoreContent(ignoredPathPatternsData, storeKey.getPackageType(), path)) {
            result = false;
        }
        return result;
    }

    private boolean ignoreContent(IgnoredPatterns ignoredPathPatterns, String packageType, String path) {
        PatternsList patterns;
        switch (packageType) {
            case MAVEN_PKG_KEY:
                patterns = ignoredPathPatterns.getMaven();
                break;
            case NPM_PKG_KEY:
                patterns = ignoredPathPatterns.getNpm();
                break;
            case GENERIC_PKG_KEY:
                patterns = ignoredPathPatterns.getGeneric();
                break;
            default:
                throw new IllegalArgumentException(
                        "Package type " + packageType + " is not supported by Indy repository manager driver.");
        }

        return matchesOne(path, patterns);
    }

    @Override
    public boolean ignoreDependencySource(StoreKey storeKey) {
        String strSK = storeKey.toString();
        return matchesOne(strSK, ignoredRepoPatterns);
    }

    /**
     * Checks if the given string matches one of the patterns.
     *
     * @param string the string
     * @param patterns the patterns list
     * @return true if there is a matching pattern, false otherwise
     */
    private boolean matchesOne(String string, PatternsList patterns) {
        if (patterns != null) {
            for (Pattern pattern : patterns.getPatterns()) {
                if (pattern.matcher(string).matches()) {
                    return true;
                }
            }
        }
        return false;
    }

}
