package ru.mirea.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

public class CompressorImpl implements Compressor {
    private class Node implements Comparable<Node> {
        private int frequency;
        private Character character;
        private Node leftChild;
        private Node rightChild;

        public int compareTo(Node treeRight) {
            return frequency - treeRight.frequency;
        }

        Node(int frequency, char character) {
            this.frequency = frequency;
            this.character = character;
        }

        Node(Node left, Node right) {
            frequency = left.frequency + right.frequency;
            leftChild = left;
            rightChild = right;
        }
    }

    private class FrequenciesTable{
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

    @Override
    public StringBuilder compression(StringBuilder block) {
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
            return new StringBuilder("-1");
        }

        if (iSymbols == 1){
            return new StringBuilder("-2");
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
        getInfo(root, bytes, meta, codeTree);
        codeTree = changeTree(codeTree);

        StringBuilder tmpTree = binStrToStr(codeTree);
        result.append(meta.length());
        result.append(":");
        result.append(meta);
        result.append(":");
        result.append(tmpTree.length());
        result.append(":");
        result.append(tmpTree);
        result.append(":");
        result.append(bytes.length());
        result.append(":");
        result.append(bytes.toString());

        return result;
    }

    private StringBuilder changeTree(StringBuilder tree){
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

    private StringBuilder createCodeSequence(StringBuilder text, Node node){
        String[] codes = codeTable(node);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            result.append(codes[text.charAt(i)]);
        }

        return result;
    }

    private StringBuilder binStrToStr(StringBuilder binaryBlock){
        int codeSet = 8;
        StringBuilder charsetBytes = new StringBuilder();
        int tmp = binaryBlock.length() % codeSet;
        for (int i = binaryBlock.length(); i > tmp; i -= codeSet){
            String tmpStr = binaryBlock.substring(i - codeSet, i);
            charsetBytes.append((char)Integer.parseInt(tmpStr, 2));
        }
        if (tmp != 0)
            charsetBytes.append((char)Integer.parseInt(binaryBlock.substring(0, tmp), 2));
        charsetBytes.reverse();
        return charsetBytes;
    }

    private StringBuilder encode(StringBuilder block, Node node) {
        StringBuilder codeSequence = createCodeSequence(block, node);
        StringBuilder charsetBytes = binStrToStr(codeSequence);
        int sizeLastByte = 8 - (codeSequence.length() % 8);
        if (sizeLastByte == 8)
            sizeLastByte = 0;
        charsetBytes.append(Integer.toString(sizeLastByte));
        return charsetBytes;
    }


    private String[] codeTable(Node node) {
        String[] codeTable = new String[65536];
        codeTable(node, new StringBuilder(), codeTable);
        return codeTable;
    }

    private void codeTable(Node node, StringBuilder code, String[] codeTable) {
        if (node.character != null) {
            codeTable[node.character] = code.toString();
            return;
        }
        codeTable(node.leftChild, code.append('0'), codeTable);
        code.deleteCharAt(code.length() - 1);
        codeTable(node.rightChild, code.append('1'), codeTable);
        code.deleteCharAt(code.length() - 1);
    }

    private void getInfo(Node current, StringBuilder code, StringBuilder alphabet, StringBuilder tree) {
        if (current.character != null) {
            if (current.character > 127){
                char tmp = current.character;
                int tmpByte = (256 + (byte)tmp);
                StringBuilder str = binStrToStr(new StringBuilder(Integer.toBinaryString(tmpByte)));
                alphabet.append(str.toString());
            }
            else
                alphabet.append(current.character);
        } else {
            tree.append("1");
            getInfo(current.leftChild, code.append('0'), alphabet, tree);
            code.deleteCharAt(code.length() - 1);
            tree.append("01");
            getInfo(current.rightChild, code.append('1'), alphabet, tree);
            code.deleteCharAt(code.length() - 1);
            tree.append("0");
        }
    }
}