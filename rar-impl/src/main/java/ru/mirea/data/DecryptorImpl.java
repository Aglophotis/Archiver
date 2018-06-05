package ru.mirea.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DecryptorImpl extends Cryptor implements Decryptor{
    private final static int BUFFER_SIZE = 64000;
    private Integer globalPriority = 0;

    public int decryption(String password, File inputFile) throws IOException, InterruptedException {
        ArrayList<Integer> arrOfSizeSubblocks = createSizeSubblocks(password);
        BlockingQueue<BlockProperties> qIn = new LinkedBlockingQueue<>();
        PriorityQueue<BlockProperties> qOut = new PriorityQueue<>();
        byte[] bytes = new byte[BUFFER_SIZE];
        byte[][] groupBytes = new byte[15][BUFFER_SIZE];
        int quantitySymbols;
        Thread[] threads = new Thread[4];
        globalPriority = 0;

        String path1 = inputFile.getAbsolutePath();
        FileInputStream fileInputStream = new FileInputStream(path1);

        String path2 = inputFile.getAbsolutePath() + "dec";
        FileOutputStream fileOutputStream = new FileOutputStream(path2);

        BlockDecryptor blockDecryptor = new BlockDecryptor(qIn, qOut, arrOfSizeSubblocks);
        Printer printer = new Printer(1, qOut, fileOutputStream);
        for (int i = 0; i < threads.length; i++)
            threads[i] = new Thread(blockDecryptor);
        Thread threadPrinter = new Thread(printer);

        for (int i = 0; i < threads.length; i++)
            threads[i].start();
        threadPrinter.start();

        int i = 0;
        while ((quantitySymbols = fileInputStream.read(bytes, 0, BUFFER_SIZE)) > 0){
            groupBytes[i%15] = bytes.clone();
            while (qIn.size() > 5)
                Thread.sleep(1);
            qIn.put(new BlockProperties(quantitySymbols, groupBytes[i%15]));
            ++i;
        }

        while (!qIn.isEmpty()){
            Thread.sleep(10);
        }
        blockDecryptor.close();

        for (int j = 0; j < threads.length; j++){
            qIn.put(new Cryptor.BlockProperties(-1, groupBytes[i%15]));
        }

        for (int j = 0; j < threads.length; j++)
            threads[j].join();

        while (!qOut.isEmpty()){
            Thread.sleep(1);
        }
        printer.close();

        threadPrinter.join();

        fileOutputStream.close();
        fileInputStream.close();

        return 0;
    }

    private class BlockDecryptor implements Runnable{
        private boolean isThreadActive = true;
        BlockingQueue<BlockProperties> qIn;
        PriorityQueue<BlockProperties> qOut;
        ArrayList<Integer> arrOfSizeSubblocks;

        BlockDecryptor(BlockingQueue<BlockProperties> qIn, PriorityQueue<BlockProperties> qOut, ArrayList<Integer> arrOfSizeSubblocks) {
            this.qIn = qIn;
            this.qOut = qOut;
            this.arrOfSizeSubblocks = arrOfSizeSubblocks;
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
                    binarySequence = binarySequence.reverse();

                    StringBuilder reverseSequence = crypt(binarySequence, arrOfSizeSubblocks);
                    StringBuilder result = binStrToStr(reverseSequence);

                    block.bytes = new byte[result.length()];
                    for (int j = 0; j < result.length(); j++) {
                        block.bytes[j] = (byte) result.charAt(j);
                    }
                    block.length = block.bytes.length;

                    binarySequence.delete(0, binarySequence.length());
                    reverseSequence.delete(0, reverseSequence.length());
                    result.delete(0, result.length());

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
