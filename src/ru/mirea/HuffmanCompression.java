package ru.mirea;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

public class HuffmanCompression{

    private static class Node implements Comparable<Node> {
        private int frequency;
        private Character character;
        private Node leftChild;
        private Node rightChild;
        private Node parent;

        public int compareTo(Node treeRight) {
            return frequency - treeRight.frequency;
        }

        public Node(int frequency, char character) {
            this.frequency = frequency;
            this.character = character;
        }

        public Node(){}

        public Node(Node left, Node right) {
            frequency = left.frequency + right.frequency;
            leftChild = left;
            rightChild = right;
        }

        public String toString() {
            return "[id=" + frequency + ", data =" + character + "]";
        }
    }

    private static class frequenciesTable{
        private int frequency;
        private Character character;

        public frequenciesTable(int frequency, char character){
            this.frequency = frequency;
            this.character = character;
        }

        public void inc(){
            frequency++;
        }
    }

    public static String compression(String block) {
        PriorityQueue<Node> trees = new PriorityQueue<>();
        HashMap<Character, Integer> hashMap = new HashMap<>();
        ArrayList<frequenciesTable> frequencies = new ArrayList<>();
        int length = block.length();
        int iSymbols = 0;
        for (int i = 0; i < length; i++) {
            if (!hashMap.containsKey(block.charAt(i))) {
                hashMap.put(block.charAt(i), iSymbols);
                frequencies.add(new frequenciesTable(1, block.charAt(i)));
                iSymbols++;
            } else {
                frequencies.get(hashMap.get(block.charAt(i))).inc();
            }
        }

        if (length / iSymbols < 5 || iSymbols > 150){
           return "-1";
        }

        if (frequencies.size() == 1){
            trees.offer(new Node(frequencies.get(0).frequency,frequencies.get(0).character));
        }
        else {
            for (int i = 0; i < frequencies.size(); i++) {
                trees.offer(new Node(frequencies.get(i).frequency, frequencies.get(i).character));
            }
            while (trees.size() > 1) {
                Node a = trees.poll();
                Node b = trees.poll();
                trees.offer(new Node(a, b));
            }
        }
        Node root = trees.poll();

        StringBuilder bytes;
        StringBuilder result = new StringBuilder();
        StringBuilder tree = new StringBuilder();
        StringBuilder meta = new StringBuilder();
        bytes = encode(block, root);
        getMeta(root, bytes, meta, tree);
        tree = changeTree(tree);
        result.append(meta.length() + ":" + meta + ":" + bitsToString(tree.toString()).length()
                + ":" + bitsToString(tree.toString()) + ":" + (bytes.length()) + ":");
        result.append(bytes.toString());
        return result.toString();
    }

    public static StringBuilder changeTree(StringBuilder tree){
        StringBuilder tmp = new StringBuilder();
        boolean isZero = false;
        for (int i = 0; i < tree.length(); i++){
            if (tree.toString().charAt(i) == '1'){
                isZero = false;
                tmp.append("1");
            }
            else {
                if (!isZero){
                    isZero = true;
                    tmp.append("0");
                }
            }
        }
        return tmp;
    }

    private static Node createTree(String alphabet, String trees){
        Node node = new Node();
        int j = 0;
        for (int i = 0; i < trees.length(); i++){
            if (trees.charAt(i) == '1'){
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
                node.character = alphabet.charAt(j);
                node.frequency = 0;
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

    public static String uncompression(String[] strData, String codedStr){
        String trees = Integer.toBinaryString(strData[2].charAt(0));

        for (int i = 1; i < strData[2].length(); i++){
            String s1 = Integer.toBinaryString(strData[2].charAt(i));
            String s2 = "";
            while (s2.length() + s1.length() != 8){
                s2 += '0';
            }
            s2 += s1;
            trees += s2;
        }

        Node root = createTree(strData[1], trees);
        return decode(codedStr, root);
    }

    public static String decode(String tmp, Node node) {
        int sizeLastByte = tmp.charAt(tmp.length() - 1) - '0';
        String binary = "";
        String str = Integer.toBinaryString(tmp.charAt(0));
        while (binary.length() + str.length() +sizeLastByte < 8)
            binary += '0';
        binary += str;
        str = "";

        for (int i = 1; i < tmp.length() - 1; i++){
            if (tmp.charAt(i) > 127) {
                int tmpByte = (256 + (byte)tmp.charAt(i));
                String str1 = Integer.toBinaryString(tmpByte);
                String str2 = "";
                while (str1.length() + str2.length() < 8){
                    str2 += '0';
                }
                binary += str2 + str1;
            }
            else {
                String str1 = Integer.toBinaryString(tmp.charAt(i));
                String str2 = "";
                while (str1.length() + str2.length() < 8) {
                    str2 += '0';
                }
                binary += str2 + str1;
            }
        }

        Node tmpNode = node;
        for (int i = 0; i < binary.length(); i++){
            if (binary.charAt(i) == '0')
                tmpNode = tmpNode.leftChild;
            else
                tmpNode = tmpNode.rightChild;

            if (tmpNode.character != null) {
                str += tmpNode.character;
                tmpNode = node;
            }
        }

        return str;
    }

    public static String createCodeSequence(String text, Node node){
        String[] codes = codeTable(node);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            result.append(codes[text.charAt(i)]);
        }

        return result.toString();
    }

    private static StringBuilder bitsToString(String tmp){
        int codeSet = 8;
        StringBuilder charsetBytes = new StringBuilder();
        int charByte = 0;
        int length = tmp.length() - 1;
        for (int i = length; i >= 0; i--) {
            if ((length - i) % codeSet == codeSet-1){
                charByte += (tmp.charAt(i) - '0')*Math.pow(2, (length - i) % codeSet);
                charsetBytes.append((char)charByte);
                charByte = 0;
            }
            else{
                charByte += (tmp.charAt(i) - '0')*Math.pow(2, (length - i) % codeSet);
            }
        }

        if ((tmp.length() - 1) % codeSet != codeSet-1)
            charsetBytes.append((char)charByte);
        charsetBytes.reverse();
        return charsetBytes;
    }

    public static StringBuilder encode(String text, Node node) {
        String tmp = createCodeSequence(text, node);
        StringBuilder charsetBytes = bitsToString(tmp);
        int sizeLastByte = 8 - (tmp.length() % 8);
        if (sizeLastByte == 8)
            sizeLastByte = 0;
        charsetBytes.append(Integer.toString(sizeLastByte));
        return charsetBytes;
    }


    private static String[] codeTable(Node node) {
        String[] codeTable = new String[65536];
        codeTable(node, new StringBuilder(), codeTable);
        return codeTable;
    }


    private static void codeTable(Node node, StringBuilder code, String[] codeTable) {
        if (node.character != null) {
            codeTable[node.character] = code.toString();
            return;
        }
        codeTable(node.leftChild, code.append('0'), codeTable);
        code.deleteCharAt(code.length() - 1);
        codeTable(node.rightChild, code.append('1'), codeTable);
        code.deleteCharAt(code.length() - 1);
    }

    private static void getMeta(Node current, StringBuilder code, StringBuilder metaChar, StringBuilder tree) {
        if (current.character != null) {
            if (current.character > 127){
                char tmp = current.character;
                int tmpByte = (256 + (byte)tmp);
                StringBuilder str = bitsToString(Integer.toBinaryString(tmpByte));
                metaChar.append(str.toString());
            }
            else
                metaChar.append(current.character);
        } else {
            tree.append("1");
            getMeta(current.leftChild, code.append('0'), metaChar, tree);
            code.deleteCharAt(code.length() - 1);
            tree.append("01");
            getMeta(current.rightChild, code.append('1'), metaChar, tree);
            code.deleteCharAt(code.length() - 1);
            tree.append("0");
        }
    }
}