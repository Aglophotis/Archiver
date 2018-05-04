package ru.mirea;

import java.io.IOException;
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

        Node(int frequency, char character) {
            this.frequency = frequency;
            this.character = character;
        }

        Node(){
            frequency = 0;
            character = null;
            leftChild = null;
            rightChild = null;
            parent = null;
        }

        Node(Node left, Node right) {
            frequency = left.frequency + right.frequency;
            leftChild = left;
            rightChild = right;
        }
    }

    private static class FrequenciesTable{
        private int frequency;
        private Character character;

        FrequenciesTable(int frequency, char character){
            this.frequency = frequency;
            this.character = character;
        }

        void inc(){
            frequency++;
        }
    }

    public static String compression(String block) {
        PriorityQueue<Node> tree = new PriorityQueue<>();
        HashMap<Character, Integer> hashMap = new HashMap<>();
        ArrayList<FrequenciesTable> frequencies = new ArrayList<>();
        int length = block.length();
        int iSymbols = 0;
        for (int i = 0; i < length; i++) {
            if (!hashMap.containsKey(block.charAt(i))) {
                hashMap.put(block.charAt(i), iSymbols);
                frequencies.add(new FrequenciesTable(1, block.charAt(i)));
                iSymbols++;
            } else {
                frequencies.get(hashMap.get(block.charAt(i))).inc();
            }
        }

        if ((length / iSymbols) < 5 || iSymbols > 150){
           return "-1";
        }

        if (iSymbols == 1){
            return "-2";
        }

        if (frequencies.size() == 1){
            tree.offer(new Node(frequencies.get(0).frequency,frequencies.get(0).character));
        }
        else {
            for (FrequenciesTable item: frequencies) {
                tree.offer(new Node(item.frequency, item.character));
            }
            while (tree.size() > 1) {
                Node a = tree.poll();
                Node b = tree.poll();
                tree.offer(new Node(a, b));
            }
        }
        Node root = tree.poll();

        StringBuilder bytes;
        StringBuilder result = new StringBuilder();
        StringBuilder codeTree = new StringBuilder();
        StringBuilder meta = new StringBuilder();
        bytes = encode(block, root);
        getMeta(root, bytes, meta, codeTree);
        codeTree = changeTree(codeTree);
        result.append(meta.length() + ":" + meta + ":" + bitsToString(codeTree.toString()).length()
                + ":" + bitsToString(codeTree.toString()) + ":" + (bytes.length()) + ":");
        result.append(bytes.toString());
        return result.toString();
    }

    private static StringBuilder changeTree(StringBuilder tree){
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

    private static Node createTree(String alphabet, StringBuilder tree) throws IOException {
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
                if (node.parent == null)
                    throw new IOException("Archive is bit");
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

    public static String decompression(String[] strData, StringBuilder codedBlock) throws IOException {
        StringBuilder trees = new StringBuilder(Integer.toBinaryString(strData[2].charAt(0)));

        for (int i = 1; i < strData[2].length(); i++){
            StringBuilder s1 = new StringBuilder(Integer.toBinaryString(strData[2].charAt(i)));
            StringBuilder s2 = new StringBuilder();
            while (s2.length() + s1.length() != 8){
                s2.append("0");
            }
            trees.append(s2.append(s1));
        }

        Node root = createTree(strData[1], trees);
        return decode(codedBlock, root);
    }

    private static String decode(StringBuilder block, Node node) throws IOException {
        StringBuilder binarySequence = new StringBuilder();
        StringBuilder result = new StringBuilder();
        int sizeFirstByte = block.charAt(block.length() - 1) - '0';

        String tmpStr = Integer.toBinaryString(block.charAt(0));
        while (binarySequence.length() + tmpStr.length() +sizeFirstByte < 8)
            binarySequence.append("0");
        binarySequence.append(tmpStr);

        for (int i = 1; i < block.length() - 1; i++){
            String str1;
            int tmpByte = (block.charAt(i) > 127) ? (256 + (byte)block.charAt(i)): (int)block.charAt(i);
            str1 = Integer.toBinaryString(tmpByte);
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
                    throw new IOException("Archive is bit");
                tmpNode = tmpNode.leftChild;
            }
            else {
                if (tmpNode.rightChild == null)
                    throw new IOException("Archive is bit");
                tmpNode = tmpNode.rightChild;
            }
            if (tmpNode.character != null) {
                result.append(tmpNode.character);
                tmpNode = node;
            }
        }
        return result.toString();
    }

    private static String createCodeSequence(String text, Node node){
        String[] codes = codeTable(node);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            result.append(codes[text.charAt(i)]);
        }

        return result.toString();
    }

    private static StringBuilder bitsToString(String binaryBlock){
        int codeSet = 8;
        StringBuilder charsetBytes = new StringBuilder();
        int charByte = 0;
        int length = binaryBlock.length() - 1;
        for (int i = length; i >= 0; i--) {
            if ((length - i) % codeSet == codeSet-1){
                charByte += (binaryBlock.charAt(i) - '0')*Math.pow(2, (length - i) % codeSet);
                charsetBytes.append((char)charByte);
                charByte = 0;
            }
            else{
                charByte += (binaryBlock.charAt(i) - '0')*Math.pow(2, (length - i) % codeSet);
            }
        }

        if ((binaryBlock.length() - 1) % codeSet != codeSet-1)
            charsetBytes.append((char)charByte);
        charsetBytes.reverse();
        return charsetBytes;
    }

    private static StringBuilder encode(String block, Node node) {
        String codeSequence = createCodeSequence(block, node);
        StringBuilder charsetBytes = bitsToString(codeSequence);
        int sizeLastByte = 8 - (codeSequence.length() % 8);
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

    private static void getMeta(Node current, StringBuilder code, StringBuilder alphabet, StringBuilder tree) {
        if (current.character != null) {
            if (current.character > 127){
                char tmp = current.character;
                int tmpByte = (256 + (byte)tmp);
                StringBuilder str = bitsToString(Integer.toBinaryString(tmpByte));
                alphabet.append(str.toString());
            }
            else
                alphabet.append(current.character);
        } else {
            tree.append("1");
            getMeta(current.leftChild, code.append('0'), alphabet, tree);
            code.deleteCharAt(code.length() - 1);
            tree.append("01");
            getMeta(current.rightChild, code.append('1'), alphabet, tree);
            code.deleteCharAt(code.length() - 1);
            tree.append("0");
        }
    }
}