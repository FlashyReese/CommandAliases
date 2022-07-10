package me.flashyreese.mods.commandaliases.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TreeNode<T> implements Iterable<TreeNode<T>>{
    private final T data;
    private TreeNode<T> parent;
    private final List<TreeNode<T>> children;

    public TreeNode(T data) {
        this.parent = this;
        this.data = data;
        this.children = new ArrayList<>();
    }

    public TreeNode<T> addChild(T child) {
        return addChildTreeNode(new TreeNode<>(child));
    }

    public TreeNode<T> addChildTreeNode(TreeNode<T> childTreeNode) {
        childTreeNode.parent = this;
        this.children.add(childTreeNode);
        return childTreeNode;
    }

    public T getData() {
        return data;
    }

    public TreeNode<T> getParent() {
        return parent;
    }

    public List<TreeNode<T>> getChildren() {
        return children;
    }

    @NotNull
    @Override
    public Iterator<TreeNode<T>> iterator() {
        return this.children.iterator();
    }
}