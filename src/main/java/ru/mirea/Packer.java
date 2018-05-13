package ru.mirea;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Packer {
    private final static int BUFFER_SIZE_PACK = 64000;
    private static Integer globalPriority = 0;

    public static void pack(ArrayList<String> files, boolean isPack) throws IOException, InterruptedException {
        final byte[] bytes = new byte[BUFFER_SIZE_PACK];
        int quantitySymbols;
        String path = new File(".").getCanonicalPath() + "\\original\\" + files.get(files.size() - 1) + ".afk";
        FileOutputStream fileOutputStream = new FileOutputStream(path);

        if (!isPack) {
            for (int i = 0; i < files.size() - 1; i++) {
                byte[] meta = getInfo(files.get(i)).getBytes();
                fileOutputStream.write(meta, 0, meta.length);

                path = new File(".").getCanonicalPath() + "\\original\\" + files.get(i);
                final FileInputStream fileInputStream = new FileInputStream(path);

                while ((quantitySymbols = fileInputStream.read(bytes, 0, BUFFER_SIZE_PACK)) > 0) {
                    fileOutputStream.write(bytes, 0, quantitySymbols);
                }
            }
            fileOutputStream.close();
        } else {
            for (int i = 0; i < files.size() - 1; i++) {
                path = new File(".").getCanonicalPath() + "\\original\\" + files.get(i);
                FileInputStream fileInputStream = new FileInputStream(path);
                BlockingQueue<BlockProperties> qIn = new LinkedBlockingQueue<>();
                PriorityQueue<BlockProperties> qOut = new PriorityQueue<>();
                globalPriority = 0;

                String fileName = files.get(i) + ":";

                BlockPacker packer = new BlockPacker(qIn, qOut);
                Printer printer = new Printer(1, qOut, fileOutputStream);

                Thread thread1 = new Thread(packer);
                Thread thread2 = new Thread(packer);
                Thread thread3 = new Thread(packer);
                Thread thread4 = new Thread(packer);
                Thread thread5 = new Thread(packer);
                Thread thread6 = new Thread(packer);
                Thread threadPrinter = new Thread(printer);


                thread1.start();
                thread2.start();
                thread3.start();
                thread4.start();
                thread5.start();
                thread6.start();
                threadPrinter.start();

                while ((quantitySymbols = fileInputStream.read(bytes, 0, BUFFER_SIZE_PACK)) > 0) {
                    byte[] byteBlock = bytes.clone();

                    qIn.put(new BlockProperties(fileName, quantitySymbols, byteBlock));
                    fileName = "";
                }


                while (!qIn.isEmpty()){
                    Thread.sleep(5);
                }
                packer.close();


                for (int j = 0; j < 6; j++){
                    qIn.put(new BlockProperties("-1", -1, bytes));
                }


                thread1.join();
                thread2.join();
                thread3.join();
                thread4.join();
                thread5.join();
                thread6.join();

                while (!qOut.isEmpty()){
                    Thread.sleep(5);
                }
                printer.close();

                threadPrinter.join();

                fileInputStream.close();
            }
            fileOutputStream.close();
        }
    }

    private static class BlockProperties implements Comparable<BlockProperties>{
        private String fileName;
        private String meta;
        private int length;
        private int priority;
        private byte[] byteBlock;

        BlockProperties(String fileName, int length, byte[] byteBlock){
            this.fileName = fileName;
            this.length = length;
            this.byteBlock = byteBlock;
        }

        public int compareTo(BlockProperties block) {
            return priority - block.priority;
        }
    }


    private static class BlockPacker implements Runnable {
        private boolean isThreadActive = true;
        BlockingQueue<BlockProperties> qIn;
        PriorityQueue<BlockProperties> qOut;

        BlockPacker(BlockingQueue<BlockProperties> qIn, PriorityQueue<BlockProperties> qOut) {
            this.qIn = qIn;
            this.qOut = qOut;
        }

        @Override
        public void run() {
            while (isThreadActive) {
                try {
                    BlockProperties block;
                    synchronized (qIn){
                        synchronized (globalPriority){
                            block = qIn.take();
                            block.priority = globalPriority;
                            ++globalPriority;
                        }
                    }

                    if ("-1".equals(block.fileName))
                        continue;

                    char[] tmpChar = new char[block.length];
                    for (int j = 0; j < block.length; j++) {
                        tmpChar[j] = (char) block.byteBlock[j];
                    }
                    String strBlock = new String(tmpChar);

                    String s2 = Compressor.compression(strBlock);
                    String meta;

                    if (s2.equals("-1")) {
                        meta = (block.fileName.length() != 0) ? ("0" + block.fileName + block.length + ":") : ("3" + block.length + ":");
                        block.meta = meta;
                        synchronized (qOut) {
                            qOut.add(block);
                        }
                        continue;
                    }

                    if (s2.equals("-2")) {
                        meta = (block.fileName.length() != 0) ? ("4" + block.fileName + block.length  + ":") : ("5" + block.length + ":");
                        block.meta = meta;
                        byte[] tmpByte = new byte[1];
                        tmpByte[0] = block.byteBlock[0];
                        block.length = 1;
                        synchronized (qOut) {
                            qOut.add(block);
                        }
                        continue;
                    }

                    char[] characters;
                    characters = s2.toCharArray();
                    byte[] tmpBytes = new byte[characters.length];
                    for (int j = 0; j < characters.length; j++) {
                        tmpBytes[j] = (byte) characters[j];
                    }

                    meta = (block.fileName.length() != 0) ? ("1" + block.fileName) : "2";
                    block.meta = meta;
                    block.byteBlock = tmpBytes;
                    block.length = characters.length;
                    synchronized (qOut) {
                        qOut.add(block);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        void close(){
            isThreadActive = false;
        }
    }

    private static class Printer implements Runnable{
        private int sleepTime;
        private boolean isThreadActive = true;
        PriorityQueue<BlockProperties> qOut;
        FileOutputStream fileOutputStream;
        int priority = 0;

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
                                fileOutputStream.write(block.meta.getBytes(), 0, block.meta.length());
                                fileOutputStream.write(block.byteBlock, 0, block.length);
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

    private static String getInfo(String fileName) throws IOException {
        String metaInfo = "0";
        String path = new File(".").getCanonicalPath() + "\\original\\" + fileName;
        if (!(new File(path).exists()))
            throw new IllegalArgumentException("Unknown name of file: " + fileName);
        long bytes = Files.size(Paths.get(path));
        metaInfo +=  fileName + ":" + bytes + ":";
        return metaInfo;
    }

}
