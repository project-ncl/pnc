/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.enums;

/**
 * Contains the various possible support levels, such as UNRELEASED, SUPPORTED, EOL, etc..
 *
 * Rome wasn't built in a day, nor is PNC. This feature will come in near future.
 * 
 * @deprecated use pnc-api
 */
@Deprecated
public enum SupportLevel {
    UNRELEASED, EARLYACCESS, SUPPORTED, EXTENDED_SUPPORT, EOL

}
