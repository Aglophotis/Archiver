package ru.mirea;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Encryptor extends Cryptor{
    private final static int BUFFER_SIZE = 64000;
    private static Integer globalPriority = 0;

    public static void encryption(String password, String filename) throws IOException, InterruptedException {
        ArrayList<Integer> lengthSubblock = createSizeBlock(password);
        BlockingQueue<BlockProperties> qIn = new LinkedBlockingQueue<>();
        PriorityQueue<BlockProperties> qOut = new PriorityQueue<>();
        byte[] bytes = new byte[BUFFER_SIZE];
        int quantitySymbols;
        globalPriority = 0;

        String path1 = new File(".").getCanonicalPath() + "\\original\\" + filename + ".afk";
        if (!(new File(path1).exists()))
            throw new IllegalArgumentException("Unknown name of file");
        final FileInputStream fileInputStream = new FileInputStream(path1);

        String path2 = new File(".").getCanonicalPath() + "\\original\\" + filename + "enc";
        final FileOutputStream fileOutputStream = new FileOutputStream(path2);

        BlockEncryptor blockEncryptor = new BlockEncryptor(qIn, qOut, lengthSubblock);
        Printer printer = new Printer(1, qOut, fileOutputStream);
        Thread thread1 = new Thread(blockEncryptor);
        Thread thread2 = new Thread(blockEncryptor);
        Thread thread3 = new Thread(blockEncryptor);
        Thread thread4 = new Thread(blockEncryptor);
        Thread threadPrinter = new Thread(printer);


        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
        threadPrinter.start();


        while ((quantitySymbols = fileInputStream.read(bytes, 0, BUFFER_SIZE)) > 0){
            byte[] tmpBytes = bytes.clone();
            qIn.put(new BlockProperties(quantitySymbols, tmpBytes));
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

        while (!qOut.isEmpty()){
            Thread.sleep(1);
        }
        printer.close();

        threadPrinter.join();

        fileInputStream.close();
        fileOutputStream.close();

        File fileOriginal = new File(path1);
        if (!fileOriginal.delete()){
            throw new IOException("file cannot be deleted");
        }

        File fileEncryption = new File(path2);
        if (!fileEncryption.renameTo(new File(path1))){
            throw new IOException("file cannot be renamed");
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

                    StringBuilder binarySequence = byteToStr(block.length, block.bytes);
                    StringBuilder reverseSequence = crypt(binarySequence, lengthSubblock).reverse();
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
}
