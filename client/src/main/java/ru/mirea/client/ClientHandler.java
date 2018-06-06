package ru.mirea.client;

import java.io.File;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import ru.mirea.archiver.FrameMenu;

public class ClientHandler extends ChannelInboundHandlerAdapter {

	private FileByteReceiver fileByteReceiver;
	private static BlockingQueue<File> qOut = new LinkedBlockingQueue<>();
	private static BlockingQueue<File> qIn = new LinkedBlockingQueue<>();
	private static BlockingQueue<byte[]> qBytes = new LinkedBlockingQueue<>();
	private static BlockingQueue<File> qFile = new LinkedBlockingQueue<>();
	private long fileSize = 0;
	private long readableSize = 0;
	private StringBuilder fileName = new StringBuilder();
	private boolean isMetaReceived = false;
	private String pathInputFile;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {

        System.out.println("Client is running");
		FileByteTransmitter fileByteTransmitter = new FileByteTransmitter(qOut, ctx);
		FileNameTransmitter fileNameTransmitter = new FileNameTransmitter(qIn, ctx);
		Thread threadFileTransmitter = new Thread(fileByteTransmitter);
		threadFileTransmitter.start();
    	Thread threadFileNameTransmitter = new Thread(fileNameTransmitter);
    	threadFileNameTransmitter.start();

    	fileByteReceiver = new FileByteReceiver(qBytes);
    	Thread threadFileByteReceiver = new Thread(fileByteReceiver);
    	threadFileByteReceiver.start();

		FrameMenu frameMenu = new FrameMenu(qOut, qIn, qFile);
		frameMenu.openFrame();
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
			if ((char)bytes[0] == 'F') {
				qFile.take();
				Thread.sleep(5);
				qFile.put(new File(":error"));
			} else
				handlePackets(ctx, bytes);
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		} finally {
			ReferenceCountUtil.release(msg);
		}
	}

	private void handlePackets(ChannelHandlerContext ctx, byte[] bytes) throws InterruptedException, IOException {
		int length = bytes.length;
		if (!isMetaReceived){
			isMetaReceived = true;
			int i = 0;
			StringBuilder sb = new StringBuilder();
			while ((char)bytes[i] != ':'){
				System.out.println((char)bytes[i]);
				sb.append((char)bytes[i]);
				++i;
			}
			++i;
			while ((char)bytes[i] != ':'){
				System.out.println((char)bytes[i]);
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
			File tmpFile = qFile.take();
			String path = tmpFile.getAbsolutePath();
			path = path.substring(0, path.length() - tmpFile.getName().length());
			File file = new File(path + fileName.toString());
			pathInputFile = file.getAbsolutePath();
			fileByteReceiver.createFile(file);
		}

		readableSize += length;
		qBytes.put(bytes);

		System.out.println("Pack size " + length + " has been read");
		if (readableSize == fileSize) {
			System.out.println("The main transmission has ended successfully.");
			readableSize = 0;
			fileSize = 0;
			isMetaReceived = false;
			fileName.delete(0, fileName.length());
			while (qBytes.size() != 0)
				Thread.sleep(1);
			fileByteReceiver.closeFile();
			qFile.put(new File(pathInputFile));
		}
	}

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
