package ru.mirea.data;

public class DecompressorImpl implements Decompressor {
    private Node createTree(String alphabet, StringBuilder tree){
        Node node = new Node();
        int j = 0;
        for (int i = 0; i < tree.length(); i++){
            if (tree.charAt(i) == '1'){
                if (node.leftChild == null){
                    Node tmp = new Node();
                    tmp.parent = node;
                    node.leftChild = tmp;
                    node = node.leftChild;
                } else{
                    Node tmp = new Node();
                    tmp.parent = node;
                    node.rightChild = tmp;
                    node = node.rightChild;
                }
            } else {
                if (node.parent == null) {
                    return null;
                }
                node.character = alphabet.charAt(j);
                ++j;
                while (node.parent != null){
                    node = node.parent;
                    if (node.rightChild == null)
                        break;
                }
            }
        }
        return node;
    }

    @Override
    public StringBuilder decompression(String[] strData, StringBuilder codedBlock){
        StringBuilder treeSequence = new StringBuilder(Integer.toBinaryString(strData[2].charAt(0)));

        for (int i = 1; i < strData[2].length(); i++){
            StringBuilder s1 = new StringBuilder(Integer.toBinaryString(strData[2].charAt(i)));
            StringBuilder s2 = new StringBuilder();
            while (s2.length() + s1.length() != 8){
                s2.append("0");
            }
            treeSequence.append(s2.append(s1));
        }

        Node root = createTree(strData[1], treeSequence);
        if (root == null){
            return new StringBuilder("-1");
        }
        return decode(codedBlock, root);
    }

    private StringBuilder decode(StringBuilder block, Node node) {
        StringBuilder binarySequence = new StringBuilder();
        StringBuilder result = new StringBuilder();
        int sizeFirstByte = block.charAt(block.length() - 1) - '0';
        int firstByte = (block.charAt(0) > 127) ? (256 + (byte)block.charAt(0)): (int)block.charAt(0);
        String tmpStr = Integer.toBinaryString(firstByte);
        while (binarySequence.length() + tmpStr.length() +sizeFirstByte < 8)
            binarySequence.append("0");
        binarySequence.append(tmpStr);

        for (int i = 1; i < block.length() - 1; i++){
            int tmpByte = (block.charAt(i) > 127) ? (256 + (byte)block.charAt(i)): (int)block.charAt(i);
            String str1 = Integer.toBinaryString(tmpByte);
            StringBuilder str2 = new StringBuilder();
            while (str1.length() + str2.length() < 8){
                str2.append("0");
            }
            binarySequence.append(str2.append(str1));
        }

        Node tmpNode = node;
        for (int i = 0; i < binarySequence.length(); i++){
            if (binarySequence.charAt(i) == '0') {
                if (tmpNode.leftChild == null)
                    return new StringBuilder("-2");
                tmpNode = tmpNode.leftChild;
            }
            else {
                if (tmpNode.rightChild == null)
                    return new StringBuilder("-2");
                tmpNode = tmpNode.rightChild;
            }
            if (tmpNode.character != null) {
                result.append(tmpNode.character);
                tmpNode = node;
            }
        }
        return result;
    }


    private class Node implements Comparable<Node> {
        private int frequency;
        private Character character;
        private Node leftChild;
        private Node rightChild;
        private Node parent;

        public int compareTo(Node treeRight) {
            return frequency - treeRight.frequency;
        }

        Node(){
            frequency = 0;
            character = null;
            leftChild = null;
            rightChild = null;
            parent = null;
        }
    }
}