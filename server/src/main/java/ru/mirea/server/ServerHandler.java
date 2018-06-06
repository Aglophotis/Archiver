package ru.mirea.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
//import ru.mirea.client.Client;
import ru.mirea.client.FileByteReceiver;
import ru.mirea.client.FileByteTransmitter;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerHandler extends ChannelInboundHandlerAdapter
{
	private BlockingQueue<byte[]> qBytes = new LinkedBlockingQueue<>();
	private BlockingQueue<File> qOut = new LinkedBlockingQueue<>();
	private boolean isMetaReceived = false;
	private FileByteReceiver fileByteReceiver;

	private long fileSize = 0;
	private long readableSize = 0;
	private StringBuilder fileName = new StringBuilder();

	@Override
	public void channelActive(/*final */ChannelHandlerContext ctx) {
		System.out.println("Server is running");
		fileByteReceiver = new FileByteReceiver(qBytes);
		Thread threadFBR = new Thread(fileByteReceiver);
		threadFBR.start();

		FileByteTransmitter fileByteTransmitter = new FileByteTransmitter(qOut, ctx);
		Thread threadFBT = new Thread(fileByteTransmitter);
		threadFBT.start();
	}

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
		try {
			byte[] bytes;
			ByteBuf buf = ((ByteBuf) msg);
			int length = buf.readableBytes();
			if (buf.hasArray())
				bytes = buf.array();
			else {
				bytes = new byte[length];
				buf.getBytes(buf.readerIndex(), bytes);
			}
			if (!isMetaReceived && bytes[0] == 'a')
				checkExistsFile(ctx, bytes);
			else
				handlePackets(ctx, bytes);
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		} finally {
			ReferenceCountUtil.release(msg);
		}
    }

    private void checkExistsFile(ChannelHandlerContext ctx, byte[] bytes) throws InterruptedException {
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i < bytes.length; i++)
			sb.append((char)bytes[i]);
		File file = new File("C:\\Users\\vislo\\OneDrive\\Документы\\LocalRepos" + sb.toString());
		if (file.exists())
			qOut.put(file);
		else
			ctx.writeAndFlush(Unpooled.wrappedBuffer("File doesn't exist".getBytes()));
	}

    private void handlePackets(ChannelHandlerContext ctx, byte[] bytes) throws InterruptedException, IOException {
		int length = bytes.length;
		if (!isMetaReceived){
			isMetaReceived = true;
			int i = 0;
			StringBuilder sb = new StringBuilder();
			while ((char)bytes[i] != ':'){
		//		System.out.println((char)bytes[i]);
				sb.append((char)bytes[i]);
				++i;
			}
			++i;
			while ((char)bytes[i] != ':'){
		//		System.out.println((char)bytes[i]);
				fileName.append((char)bytes[i]);
				++i;
			}
			fileSize = Long.parseLong(sb.toString());
			byte[] tmpByte = new byte[bytes.length - i - 1];
			for (int j = 0; j < tmpByte.length; j++){
				tmpByte[j] = bytes[j+i+1];
			}
			bytes = tmpByte;
			System.out.println(fileSize);
			length = tmpByte.length;
			File file = new File("C:\\Users\\vislo\\OneDrive\\Документы\\LocalRepos" + fileName.toString());
			fileByteReceiver.createFile(file);
		}

		readableSize += length;
		qBytes.put(bytes);

		System.out.println("Pack size: " + length + " has been reed");
		if (readableSize == fileSize) {
			System.out.println("Complete!");
			readableSize = 0;
			fileSize = 0;
			isMetaReceived = false;
			fileName.delete(0, fileName.length());
			while (qBytes.size() != 0)
				Thread.sleep(1);
			fileByteReceiver.closeFile();
		}
	}

	public void channelReadComplete(ChannelHandlerContext ctx){
		Scanner scanner = new Scanner(System.in);
		System.out.println("Transmitting completed.");
		String string;
		while(true){
			string = scanner.nextLine();
			if (string == "quit") {
				System.out.println("Closing server...");
				ctx.close();
				System.out.println("Server closed.");
				break;
			}
		}
		ctx.fireChannelReadComplete();
	}

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}