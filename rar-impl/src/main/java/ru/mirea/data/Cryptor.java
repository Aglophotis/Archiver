package ru.mirea.data;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.PriorityQueue;

public abstract class Cryptor {
    protected StringBuilder crypt(StringBuilder binarySequence, ArrayList<Integer> lengthSubblock){
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
            tmpStr.delete(0, tmpStr.length());
            tmpStr = null;
        }
        return reverseSequence;
    }

    protected ArrayList<Integer> createSizeBlock(String password) throws IOException {
        ArrayList<Integer> lengthSubblock = new ArrayList<>();
        int factor = (password.length() % 8) + 1;
        for (int i = 0; i < password.length(); i++){
            int tmp = (byte)password.charAt(i);
            if (tmp < 15 && tmp > 0)
                throw new IOException("Incorrect password");
            if (tmp > 0)
                lengthSubblock.add(tmp*factor);
            else
                lengthSubblock.add((256 + tmp)*factor);
        }
        return lengthSubblock;
    }

    protected StringBuilder bitsToString(String binaryBlock){
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

    protected StringBuilder byteToStr(int length, byte[] bytes){
        StringBuilder binarySequence = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int tmpByte = (bytes[i] < 0) ? (256 + bytes[i]) : bytes[i];
            String str1 = Integer.toBinaryString(tmpByte);
            StringBuilder str2 = new StringBuilder();
            while (str1.length() + str2.length() < 8) {
                str2.append("0");
            }
            binarySequence.append(str2.append(str1));
            str2.delete(0, str2.length());
        }
        return binarySequence;
    }

    protected class BlockProperties implements Comparable<BlockProperties>{
        protected int length;
        protected byte[] bytes;
        protected int priority;

        BlockProperties(int length, byte[] bytes){
            this.length = length;
            this.bytes = bytes;
        }

        public int compareTo(BlockProperties block) {
            return priority - block.priority;
        }
    }

    protected  class Printer implements Runnable{
        private int sleepTime;
        private boolean isThreadActive = true;
        private PriorityQueue<BlockProperties> qOut;
        private FileOutputStream fileOutputStream;
        private int priority = 0;

        Printer(int sleepTime, PriorityQueue<BlockProperties> qOut, FileOutputStream fileOutputStream){
            this.sleepTime = sleepTime;
            this.qOut = qOut;
            this.fileOutputStream = fileOutputStream;
        }

        @Override
        public void run() {
            while (isThreadActive) {
                try {
                    synchronized (qOut) {
                        if (!qOut.isEmpty()) {
                            if (qOut.peek().priority == priority) {
                                BlockProperties block = qOut.poll();
                                fileOutputStream.write(block.bytes, 0, block.length);
                                ++priority;
                                block.bytes = null;
                                block = null;
                            }
                        }
                    }
                    Thread.sleep(sleepTime);
                }
                catch (IOException e){
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        void close(){
            isThreadActive = false;
        }
    }
}
