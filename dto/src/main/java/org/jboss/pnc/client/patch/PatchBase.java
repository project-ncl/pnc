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
package org.jboss.pnc.client.patch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jsonpatch.AddOperation;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.github.fge.jsonpatch.RemoveOperation;
import com.github.fge.jsonpatch.ReplaceOperation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public abstract class PatchBase<T, S> {
    protected List<JsonPatchOperation> operations = new ArrayList<>();

    protected ObjectMapper mapper = ObjectMapperProvider.getInstance();

    protected Class<S> clazz;

    public PatchBase(Class<S> clazz) {
        this.clazz = clazz;
    }

    public Class<S> getClazz() {
        return clazz;
    }

    public String getJsonPatch() throws PatchBuilderException {
        try {
            return mapper.writeValueAsString(operations);
        } catch (JsonProcessingException e) {
            throw new PatchBuilderException("Cannot serialize patch request.", e);
        }
    }

    protected T add(Collection elements, String path) {
        for (Object element : elements) {
            AddOperation operation = new AddOperation(JsonPointer.of(path, "-"), mapper.valueToTree(element));
            operations.add(operation);
        }
        return (T) this;
    }

    protected T add(Map<?, ?> elements, String path) {
        for (Map.Entry<?, ?> entry : elements.entrySet()) {
            AddOperation operation = new AddOperation(
                    JsonPointer.of(path, entry.getKey()),
                    mapper.valueToTree(entry.getValue()));
            operations.add(operation);
        }
        return (T) this;
    }

    protected T remove(Collection keys, String path) {
        for (Object key : keys) {
            RemoveOperation operation = new RemoveOperation(JsonPointer.of(path, key));
            operations.add(operation);
        }
        return (T) this;
    }

    protected T replace(Object value, String path) {
        ReplaceOperation operation = new ReplaceOperation(JsonPointer.of(path), mapper.valueToTree(value));
        operations.add(operation);
        return (T) this;
    }

}
