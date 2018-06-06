package ru.mirea.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import java.io.*;
import java.nio.charset.Charset;
import java.util.concurrent.BlockingQueue;

public class FileByteTransmitter implements Runnable {

    private FileInputStream fileInputStream;
    private final BlockingQueue<File> qOut;
    private ChannelHandlerContext ctx;

    public FileByteTransmitter(BlockingQueue<File> qOut, ChannelHandlerContext ctx) {
        this.qOut = qOut;
        this.fileInputStream = null;
        this.ctx = ctx;
    }

    @Override
    public void run() {
        System.out.println("Transmission of bytes has started...");
        try {
            boolean isThreadActive = true;
            while (isThreadActive) {
                File file;
                synchronized (qOut){
                    file = qOut.take();
                    if (!file.getName().equals(":quit")){
                        FileInputStream fileInputStream = new FileInputStream(file);
                        byte[] bytes = new byte[64000];
                        int quantitySymbols;
                        System.out.println(file.getAbsolutePath());
                        ctx.write(Unpooled.wrappedBuffer((Long.toString(file.length()) + ":").getBytes()));
                        ctx.write(Unpooled.wrappedBuffer((file.getName() + ":").getBytes(Charset.forName("ISO-8859-1"))));
                        System.out.println(Long.toString(file.length()));
                        while ((quantitySymbols = fileInputStream.read(bytes, 0, 64000)) > 0) {
                            byte[] tmpBytes = new byte[quantitySymbols];
                            for (int i = 0; i < quantitySymbols; i++)
                                tmpBytes[i] = bytes[i];
                            ByteBuf out = Unpooled.wrappedBuffer(tmpBytes);
                            ctx.writeAndFlush(out);
                        }
                        System.out.println("Transmission of bytes completed!");
                        fileInputStream.close();
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
