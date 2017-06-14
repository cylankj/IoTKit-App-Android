package com.cylan.jiafeigou.support.badge;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hds on 17-6-13.
 */

public class TreeNode {

    private String nodeName;
    private String parentName;
    private Object data;
    private List<TreeNode> childNodeList;

    public List<TreeNode> getChildNodeList() {
        return childNodeList;
    }


    public List<TreeNode> traversal() {
        List<TreeNode> nodeList = new ArrayList<>();
        nodeList.add(this);
        if (childNodeList != null)
            for (TreeNode node : childNodeList) {
                nodeList.addAll(node.traversal());
            }
        return nodeList;
    }

    public void setChildNodeList(List<TreeNode> childNodeList) {
        this.childNodeList = childNodeList;
    }

    public boolean addNodeToChild(TreeNode node) {
        if (childNodeList == null) childNodeList = new ArrayList<>();
        return childNodeList.add(node);
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getParentName() {
        return parentName;
    }

    public Object getData() {
        return data;
    }

    public TreeNode setNodeName(String nodeName) {
        this.nodeName = nodeName;
        return this;
    }

    public TreeNode setParentName(String parentName) {
        this.parentName = parentName;
        return this;
    }

    public TreeNode setData(Object data) {
        this.data = data;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TreeNode node = (TreeNode) o;

        if (nodeName != null ? !nodeName.equals(node.nodeName) : node.nodeName != null)
            return false;
        return parentName != null ? parentName.equals(node.parentName) : node.parentName == null;

    }

    @Override
    public int hashCode() {
        int result = nodeName != null ? nodeName.hashCode() : 0;
        result = 31 * result + (parentName != null ? parentName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TreeNode{" +
                "nodeName='" + nodeName + '\'' +
                ", parentName='" + parentName + '\'' +
                ", data=" + data +
                ", childNodeList=" + childNodeList +
                '}';
    }
}
