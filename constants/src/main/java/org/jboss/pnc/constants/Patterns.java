/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.constants;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class Patterns {
    
    /**
     * Version that consists of a major, minor and micro numeric version followed by an
     * alphanumeric qualifier. Micro version can be left out in special cases.
     * For example 1.0.0.ER1, 1.2.10.CR1, 1.0.CD1.
     */
    public static final String PRODUCT_VERSION = "^[0-9]+\\.[0-9]+(\\.[0-9]+)?\\.[\\w]+$";
    
    /**
     * Version that consists of a major and minor numeric version.
     * For example 1.0, 1.2.
     */
    public static final String PRODUCT_STREAM_VERSION = "^[0-9]+\\.[0-9]+$";
    
    
    /**
     * Product name abbreviation. May consists of letters, numbers and dash.
     * For example AB-Foo
     */
    public static final String PRODUCT_ABBREVIATION = "[a-zA-Z0-9-]+";
    
}
