package ru.mirea;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Encryptor {
    private final static int BUFFER_SIZE = 64000;
    private static Integer globalPriority = 0;

    public static void encryption(String password, String filename) throws IOException, InterruptedException {
        ArrayList<Integer> lengthSubblock = new ArrayList<>();
        BlockingQueue<BlockProperties> qIn = new LinkedBlockingQueue<>();
        PriorityQueue<BlockProperties> qOut = new PriorityQueue<>();

        byte[] bytes = new byte[BUFFER_SIZE];
        int quantitySymbols;
        for (int i = 0; i < password.length(); i++){
            int tmp = (byte)password.charAt(i);
            if (tmp < 15 && tmp > 0)
                throw new IOException("Incorrect password");
            if (tmp > 0)
                lengthSubblock.add(tmp);
            else
                lengthSubblock.add(256 + tmp);
        }

        String path = new File(".").getCanonicalPath() + "\\original\\" + filename;
        if (!(new File(path).exists()))
            throw new IllegalArgumentException("Unknown name of file");
        final FileInputStream fileInputStream = new FileInputStream(path);
        path = new File(".").getCanonicalPath() + "\\original\\" + filename + "enc";
        final FileOutputStream fileOutputStream = new FileOutputStream(path);

        BlockEncryptor blockEncryptor = new BlockEncryptor(qIn, qOut, lengthSubblock);
        Thread thread1 = new Thread(blockEncryptor);
        Thread thread2 = new Thread(blockEncryptor);
        Thread thread3 = new Thread(blockEncryptor);
        Thread thread4 = new Thread(blockEncryptor);


        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();


        while ((quantitySymbols = fileInputStream.read(bytes, 0, BUFFER_SIZE)) > 0){
            qIn.put(new BlockProperties(quantitySymbols, bytes));
        }

        while (!qIn.isEmpty()){
            Thread.sleep(10);
        }
        blockEncryptor.close();

        for (int j = 0; j < 10; j++){
            qIn.put(new BlockProperties(-1, bytes));
        }

        thread1.join();
        thread2.join();
        thread3.join();
        thread4.join();

        int qSize = qOut.size();
        for (int j = 0; j < qSize; j++){
            BlockProperties block = qOut.poll();
            fileOutputStream.write(block.bytes, 0, block.length);
        }

        fileInputStream.close();
        fileOutputStream.close();
    }

    private static class BlockProperties implements Comparable<BlockProperties>{
        private int length;
        private byte[] bytes;
        private int priority;

        BlockProperties(int length, byte[] bytes){
            this.length = length;
            this.bytes = bytes;
        }

        public int compareTo(BlockProperties block) {
            return priority - block.priority;
        }
    }

    private static class BlockEncryptor implements Runnable{
        private boolean isThreadActive = true;
        BlockingQueue<BlockProperties> qIn;
        PriorityQueue<BlockProperties> qOut;
        ArrayList<Integer> lengthSubblock;

        BlockEncryptor(BlockingQueue<BlockProperties> qIn, PriorityQueue<BlockProperties> qOut, ArrayList<Integer> lengthSubblock) {
            this.qIn = qIn;
            this.qOut = qOut;
            this.lengthSubblock = lengthSubblock;
        }

        @Override
        public void run() {
            while (isThreadActive) {
                try {
                    BlockProperties block;
                    synchronized (qIn) {
                        synchronized (globalPriority) {
                            block = qIn.take();
                            block.priority = globalPriority;
                            ++globalPriority;
                        }
                    }

                    if (block.length == -1)
                        continue;

                    StringBuilder binarySequence = new StringBuilder();
                    for (int i = 0; i < block.length; i++) {
                        int tmpByte = (block.bytes[i] < 0) ? (256 + block.bytes[i]) : block.bytes[i];
                        String str1 = Integer.toBinaryString(tmpByte);
                        StringBuilder str2 = new StringBuilder();
                        while (str1.length() + str2.length() < 8) {
                            str2.append("0");
                        }
                        binarySequence.append(str2.append(str1));
                    }
                    StringBuilder reverseSequence = encrypt(binarySequence, lengthSubblock);
                    StringBuilder result = Compressor.bitsToString(reverseSequence.toString());
                    char[] characters = result.toString().toCharArray();
                    byte[] tmpBytes = new byte[characters.length];
                    for (int j = 0; j < characters.length; j++) {
                        tmpBytes[j] = (byte) characters[j];
                    }
                    block.bytes = tmpBytes;
                    block.length = characters.length;
                    synchronized (qOut){
                        qOut.add(block);
                    }
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }

        void close(){
            isThreadActive = false;
        }
    }

    private static StringBuilder encrypt(StringBuilder binarySequence, ArrayList<Integer> lengthSubblock){
        int i = 0;
        int k = 0;
        StringBuilder reverseSequence = new StringBuilder();
        while (i < binarySequence.length()){
            StringBuilder tmpStr = new StringBuilder();
            if (i + lengthSubblock.get(k % lengthSubblock.size()) < binarySequence.length()) {
                for (int j = 0; j < lengthSubblock.get(k % lengthSubblock.size()); ++j, ++i) {
                    tmpStr.append(binarySequence.charAt(i));
                }
            }
            else {
                for (int j = i; j < binarySequence.length(); j++, i++){
                    tmpStr.append(binarySequence.charAt(i));
                }
            }
            ++k;
            reverseSequence.append(tmpStr.reverse());
        }
        return reverseSequence.reverse();
    }
}
