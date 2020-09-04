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
package org.jboss.pnc.notification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@JsonDeserialize(builder = TypedMessage.TypedMessageBuilder.class)
@AllArgsConstructor
@XmlRootElement
public class TypedMessage<T> implements Serializable {

    @Getter
    private final MessageType messageType;

    @Getter
    private final T data;

    public static <T> TypedMessageBuilder<T> builder() {
        return new TypedMessageBuilder<>();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class TypedMessageBuilder<T> {

        private MessageType messageType;
        private T data;

        private TypedMessageBuilder() {
        }

        public TypedMessageBuilder messageType(MessageType messageType) {
            this.messageType = messageType;
            return this;
        }

        public TypedMessageBuilder data(T data) {
            this.data = data;
            return this;
        }

        public TypedMessage build() {
            return new TypedMessage(messageType, data);
        }
    }
}
