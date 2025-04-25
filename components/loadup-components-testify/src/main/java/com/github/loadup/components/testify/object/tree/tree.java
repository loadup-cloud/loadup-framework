package com.github.loadup.components.testify.object.tree;

/*-
 * #%L
 * loadup-components-test
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

/**
 * 
 *
 */
public class tree<T> {

    public treeNode<T> root;

    public tree() {
    }

    public static void main(String[] args) {

        //�?�?
        /*
         * string
         *         hello
         *             sinny
         *             fredric
         *         world
         *           Hi
         *           York
         * */

        tree<String> tree = new tree();
        tree.addNode(null, "string");
        tree.addNode(tree.getNode("string"), "hello");
        tree.addNode(tree.getNode("string"), "world");
        tree.addNode(tree.getNode("hello"), "sinny");
        tree.addNode(tree.getNode("hello"), "fredric");
        tree.addNode(tree.getNode("world"), "Hi");
        tree.addNode(tree.getNode("world"), "York");
        tree.showNode(tree.root);
        System.out.println("-----------");
        tree.showNode(tree.getNode("hello"));
        System.out.println("-----------");
        tree.showNode(tree.getNode("York"));
        System.out.println("end of the test");
    }

    public void addNode(treeNode<T> node, T newNode) {
        //�??????��?????
        if (null == node) {
            if (null == root) {
                root = new treeNode(newNode);
            }
        } else {
            treeNode<T> temp = new treeNode(newNode);
            node.nodelist.add(temp);
        }
    }

    /*    ??��??newNode�?�??????? */
    public treeNode<T> search(treeNode<T> input, T newNode) {

        treeNode<T> temp = null;

        if (input.t.equals(newNode)) {
            return input;
        }

        for (int i = 0; i < input.nodelist.size(); i++) {

            temp = search(input.nodelist.get(i), newNode);

            if (null != temp) {
                break;
            }
        }

        return temp;
    }

    public treeNode<T> getNode(T newNode) {
        return search(root, newNode);
    }

    public void showNode(treeNode<T> node) {
        if (null != node) {
            //�??????????node?????????
            System.out.println(node.t.toString());

            for (int i = 0; i < node.nodelist.size(); i++) {
                showNode(node.nodelist.get(i));
            }
        }
    }

}
