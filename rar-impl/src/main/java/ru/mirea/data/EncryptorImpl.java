package ru.mirea.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class EncryptorImpl extends Cryptor implements Encryptor {
    private Integer globalPriority = 0;

    @Override
    public int encryption(String password, File outputFile) throws IOException, InterruptedException {
        ArrayList<Integer> lengthSubblock = createSizeBlock(password);
        BlockingQueue<Cryptor.BlockProperties> qIn = new LinkedBlockingQueue<>();
        PriorityQueue<Cryptor.BlockProperties> qOut = new PriorityQueue<>();
        int BUFFER_SIZE = 64000;
        byte[] bytes = new byte[BUFFER_SIZE];
        byte[][] groupBytes = new byte[15][BUFFER_SIZE];
        int quantitySymbols;
        globalPriority = 0;

        String path1 = outputFile.getAbsolutePath() + ".afk";
        FileInputStream fileInputStream = new FileInputStream(path1);

        String path2 = outputFile.getAbsolutePath() + ".afkenc";
        FileOutputStream fileOutputStream = new FileOutputStream(path2);

        BlockEncryptor blockEncryptor = new BlockEncryptor(qIn, qOut, lengthSubblock);
        Cryptor.Printer printer = new Cryptor.Printer(1, qOut, fileOutputStream);
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

        int i = 0;
        while ((quantitySymbols = fileInputStream.read(bytes, 0, BUFFER_SIZE)) > 0){
            groupBytes[i%15] = bytes.clone();
            while (qIn.size() > 5)
                Thread.sleep(1);
            qIn.put(new Cryptor.BlockProperties(quantitySymbols, groupBytes[i%15]));
            ++i;
        }

        while (!qIn.isEmpty()){
            Thread.sleep(10);
        }
        blockEncryptor.close();

        for (int j = 0; j < 4; j++){
            qIn.put(new Cryptor.BlockProperties(-1, bytes));
        }

        thread1.join();
        thread2.join();
        thread3.join();
        thread4.join();

        while (!qOut.isEmpty()){
            Thread.sleep(10);
        }
        printer.close();

        threadPrinter.join();

        fileInputStream.close();
        fileOutputStream.close();

        File fileOriginal = new File(path1);
        if (!fileOriginal.delete()){
            return -9;
        }

        File fileEncryption = new File(path2);
        if (!fileEncryption.renameTo(new File(path1))){
            return -10;
        }
        return 0;
    }

    private class BlockEncryptor implements Runnable{
        private boolean isThreadActive = true;
        BlockingQueue<Cryptor.BlockProperties> qIn;
        PriorityQueue<Cryptor.BlockProperties> qOut;
        ArrayList<Integer> lengthSubblock;

        BlockEncryptor(BlockingQueue<Cryptor.BlockProperties> qIn, PriorityQueue<Cryptor.BlockProperties> qOut, ArrayList<Integer> lengthSubblock) {
            this.qIn = qIn;
            this.qOut = qOut;
            this.lengthSubblock = lengthSubblock;
        }

        @Override
        public void run() {
            while (isThreadActive) {
                try {
                    Cryptor.BlockProperties block;
                    synchronized (qIn) {
                        synchronized (globalPriority) {
                            block = qIn.take();
                            block.priority = globalPriority;
                            ++globalPriority;
                            if (globalPriority % 50 == 0)
                                System.gc();
                        }
                    }

                    if (block.length == -1)
                        continue;

                    StringBuilder binarySequence = byteToStr(block.length, block.bytes);
                    StringBuilder reverseSequence = crypt(binarySequence, lengthSubblock).reverse();
                    StringBuilder result = binStrToStr(reverseSequence);

                    binarySequence.delete(0, binarySequence.length());
                    reverseSequence.delete(0, reverseSequence.length());

                    byte[] tmpBytes = new byte[result.length()];
                    for (int j = 0; j < result.length(); j++) {
                        tmpBytes[j] = (byte) result.charAt(j);
                    }

                    result.delete(0, result.length());

                    block.bytes = tmpBytes;
                    block.length = tmpBytes.length;
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
