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
package org.jboss.pnc.common.tree;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class TreeNode<T> implements Iterable<TreeNode<T>> {
    T data;
    TreeNode<T> parent;
    List<TreeNode<T>> children;

    public TreeNode(T data) {
        this.data = data;
        this.children = new LinkedList<>();
    }

    public TreeNode<T> addChild(T child) {
        TreeNode<T> childNode = new TreeNode<>(child);
        childNode.parent = this;
        this.children.add(childNode);
        return childNode;
    }

    public void addChild(TreeNode<T> childNode) {
        childNode.parent = this;
        this.children.add(childNode);
    }

    @Override
    public Iterator<TreeNode<T>> iterator() {
        return children.iterator();
    }

    @Override
    public void forEach(Consumer<? super TreeNode<T>> action) {
        children.forEach(action);
    }

    @Override
    public Spliterator<TreeNode<T>> spliterator() {
        return children.spliterator();
    }
}
