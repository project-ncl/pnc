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

import java.text.MessageFormat;
import java.util.IllegalFormatException;

public class PromotionValidationException extends Exception {

    private static final long serialVersionUID = 8236842740038103177L;

    private Object[] params;

    private transient String formatted;

    public PromotionValidationException(String format, Object... params) {
        super(format);
        this.params = params;
    }

    public PromotionValidationException(String format, Throwable error, Object... params) {
        super(format, error);
        this.params = params;
    }

    @Override
    public String getMessage() {
        if (formatted == null) {
            formatted = super.getMessage();

            if (params != null) {
                try {
                    formatted = String.format(formatted.replaceAll("\\{\\}", "%s"), params);
                } catch (final IllegalFormatException ife) {
                    try {
                        formatted = MessageFormat.format(formatted, params);
                    } catch (final IllegalArgumentException iae) {
                    }
                }
            }
        }

        return formatted;
    }

}
