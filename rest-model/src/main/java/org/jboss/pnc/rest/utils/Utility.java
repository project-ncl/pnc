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
package org.jboss.pnc.rest.utils;

public class Utility {

    /**
     * Perform the given action if the object is not null
     * 
     * @param obj The object to check for null
     * @param action The action to perform if object is not null
     */
    public static void performIfNotNull(Object obj, Runnable action) {
        if(obj != null) {
            action.run();
        }
    }
    
    /**
     * Perform the given action if the object is null
     * 
     * @param obj The object to check for null
     * @param action The action to perform if object is null
     */
    public static void performIfNull(Object obj, Runnable action) {
        if(obj == null) {
            action.run();
        }
    }

}
