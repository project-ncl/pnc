/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
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
