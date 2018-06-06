package ru.mirea.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class FileByteReceiver implements Runnable {

    private boolean isThreadActive = true;
    private BlockingQueue<byte[]> qBytes;
    private FileOutputStream fileOutputStream;

    public FileByteReceiver(BlockingQueue<byte[]> qBytes){
        this.fileOutputStream = null;
        this.qBytes = qBytes;
    }

    @Override
    public void run() {
        try {
            while (isThreadActive) {
                byte[] bytes;
                synchronized (qBytes) {
                    bytes = qBytes.take();
                }
                synchronized (fileOutputStream) {
                    try {
                        fileOutputStream.write(bytes, 0, bytes.length);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                fileOutputStream.close();
                close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void createFile(File file) throws IOException {
        if (fileOutputStream != null)
            fileOutputStream.close();
        fileOutputStream = new FileOutputStream(file);
    }

    public void closeFile() throws IOException {
        synchronized (fileOutputStream) {
            if (fileOutputStream != null)
                fileOutputStream.close();
        }
    }

    public void close(){
        isThreadActive = false;
    }
}
