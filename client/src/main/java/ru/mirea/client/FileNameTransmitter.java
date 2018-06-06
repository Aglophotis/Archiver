package ru.mirea.client;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.BlockingQueue;

public class FileNameTransmitter implements Runnable {

    private FileInputStream fileInputStream;
    private final BlockingQueue<File> qIn;
    private ChannelHandlerContext ctx;

    public FileNameTransmitter(BlockingQueue<File> qIn, ChannelHandlerContext ctx){
        this.qIn = qIn;
        this.fileInputStream = null;
        this.ctx = ctx;
    }

    @Override
    public void run() {
        System.out.println("Transmission of file name has started...");
        try {
            boolean isThreadActive = true;
            while (isThreadActive) {
                File file;
                synchronized (qIn){
                    file = qIn.take();
                    if (!file.getName().equals(":quit")){
                        ctx.writeAndFlush(Unpooled.wrappedBuffer(("a" + file.getName()).getBytes(Charset.forName("ISO-8859-1"))));
                    }
                }
            }
            System.out.println("Transmission of file name completed.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
/*
    public void close(){
        isThreadActive = false;
    }
*/
}
