package ru.mirea.data;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.PriorityQueue;

abstract class Cryptor {
    protected StringBuilder crypt(StringBuilder binarySequence, ArrayList<Integer> lengthSubblock){
        int i = 0;
        int k = 0;
        int pointer = lengthSubblock.get(0);
        int size = lengthSubblock.size();
        StringBuilder reverseSequence = new StringBuilder();
        while (pointer < binarySequence.length()){
            StringBuilder tmpStr = new StringBuilder();
            tmpStr.append(binarySequence.substring(i, pointer));
            ++k;
            i = pointer;
            reverseSequence.append(tmpStr.reverse());
            pointer = i + lengthSubblock.get(k % size);
        }
        StringBuilder tmpStr = new StringBuilder();
        tmpStr.append(binarySequence.substring(i, binarySequence.length()));
        reverseSequence.append(tmpStr.reverse());
        return reverseSequence;
    }

    protected ArrayList<Integer> createSizeSubblocks(String password){
        ArrayList<Integer> arrOfSizeSubblocks = new ArrayList<>();
        int factor = (password.length() % 8 == 0) ? password.length() + 2 : password.length();
        for (int i = 0; i < password.length(); i++){
            arrOfSizeSubblocks.add(((int)password.charAt(i)) + factor);
        }
        return arrOfSizeSubblocks;
    }

    protected StringBuilder binStrToStr(StringBuilder binaryBlock){
        int charSize = 8;
        StringBuilder charsetBytes = new StringBuilder();
        int tmp = binaryBlock.length() % charSize;
        for (int i = binaryBlock.length(); i > tmp; i -= charSize){
            String tmpStr = binaryBlock.substring(i - charSize, i);
            charsetBytes.append((char)Integer.parseInt(tmpStr, 2));
        }
        if (tmp != 0)
            charsetBytes.append((char)Integer.parseInt(binaryBlock.substring(0, tmp), 2));
        charsetBytes.reverse();
        return charsetBytes;
    }

    protected StringBuilder byteToStr(int length, byte[] bytes){
        StringBuilder binarySequence = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int tmpByte = (bytes[i] < 0) ? (256 + bytes[i]) : bytes[i];
            StringBuilder str1 = new StringBuilder(Integer.toBinaryString(tmpByte));
            StringBuilder str2 = new StringBuilder();
            while (str1.length() + str2.length() < 8) {
                str2.append("0");
            }
            binarySequence.append(str2.append(str1));
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
