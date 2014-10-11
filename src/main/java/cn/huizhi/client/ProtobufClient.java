package cn.huizhi.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.huizhi.net.CheckSumStream;
import cn.huizhi.net.MessageHelper;

import com.google.protobuf.Message;

public class ProtobufClient {

	private int pid;
	private String ip;
	private int port;
	
	Bootstrap bootstrap;
	Channel channel;
	ChannelFuture future;

	public boolean is = true;
	public AtomicInteger msgOffset = new AtomicInteger(0);
	public final CheckSumStream checkSumStream = new cn.huizhi.net.CheckSumStream();
	
	private static ChannelGroup allChannels = new DefaultChannelGroup(new DefaultEventExecutorGroup(1).next());
	private static Logger logger = LoggerFactory.getLogger(ProtobufClient.class);

	private ProtobufClient(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}
	public static ProtobufClient start(String ip, int port) {
		ProtobufClient protobufClient = new ProtobufClient(ip, port);
		protobufClient.start();
		return protobufClient;
	}
	
	public static ProtobufClient start(int pid, String ip, int port) {
		ProtobufClient protobufClient = new ProtobufClient(ip, port);
		protobufClient.pid = pid;
		protobufClient.start();
		return protobufClient;
	}
	
	public static ProtobufClient asynStart(String ip, int port, int pid, int cmd, Message.Builder builder) {
		ProtobufClient protobufClient = new ProtobufClient(ip, port);
		protobufClient.pid = pid;
		protobufClient.asynStart(cmd, builder);
		return protobufClient;
	}
	private void start() {
		// Configure the server.
		EventLoopGroup workerGroup = new NioEventLoopGroup(1);
		
		try {
			bootstrap = new Bootstrap();
			
			bootstrap.group(workerGroup)
				.channel(NioSocketChannel.class)
				.option(ChannelOption.TCP_NODELAY, true)
				.option(ChannelOption.SO_KEEPALIVE, true)
				.option(ChannelOption.MAX_MESSAGES_PER_READ, 1024)
				.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline()
							.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(4096, 0, 2, 0, 0))
							.addLast(new ClientDecoderHandler(ProtobufClient.this, pid))
							.addLast("frameEncoder", new LengthFieldPrepender(2))
							.addLast(new SimpleChannelOutboundHandler())
							;
					}
				});
			channel = bootstrap.connect(ip, port).sync().channel();
			logger.info("连接上游戏服务器端口-{}...", port);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
	
	private void asynStart(final int cmd, final Message.Builder builder) {
		// Configure the server.
		EventLoopGroup workerGroup = new NioEventLoopGroup(1);
		
		try {
			bootstrap = new Bootstrap();
			
			bootstrap.group(workerGroup)
				.channel(NioSocketChannel.class)
				.option(ChannelOption.TCP_NODELAY, true)
				.option(ChannelOption.SO_KEEPALIVE, true)
				.option(ChannelOption.MAX_MESSAGES_PER_READ, 1024)
				.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline()
							.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(4096, 0, 2, 0, 0))
							.addLast(new ClientDecoderHandler(ProtobufClient.this, pid))
							.addLast("frameEncoder", new LengthFieldPrepender(2))
							.addLast(new SimpleChannelOutboundHandler())
							;
					}
				});
			ChannelFuture future = bootstrap.connect(ip, port);
			future.addListener(new ChannelFutureListener() {
				
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if(future.isSuccess()) {
						channel = future.channel();
						ProtobufClient.this.send(cmd, builder);
					}
				}
			});
			logger.info("连接上游戏服务器端口-{}...", port);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	public static void shutdown() throws Exception {
		try {
			/**
			 * 主动关闭服务
			 */
			ChannelGroupFuture future = allChannels.close();
			future.awaitUninterruptibly();// 阻塞，直到服务器关闭
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		} finally {
			logger.info("server is shutdown on port ");
			System.exit(1);
		}
	}
	
	public static ChannelGroup getChannelGroup() {
		return allChannels;
	}

	public void send(final int cmd, final Message.Builder builder) {
		if(channel == null || !channel.isActive()) {
			msgOffset = new AtomicInteger(1);
			start();
		} 
		channel.eventLoop().execute(new Runnable() {
			
			@Override
			public void run() {
				if(is) {
					sendCheck(cmd, builder);
				} else {
					ByteBuf byteBuf = MessageHelper.newDynamicMessage(cmd);
					if(builder != null) {
						byteBuf.writeBytes(builder.build().toByteArray());
					}
					channel.write(byteBuf);
				}
			}
		});
	}
	
	private void sendCheck(final int cmd, Message.Builder builder) {
		ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer(64);
		byteBuf.writeByte(0);
		int msgOffset = ProtobufClient.this.msgOffset.get();
		byteBuf.writeByte(calculateVerificationBytes(ProtobufClient.this.msgOffset.incrementAndGet()));
		short offset = (short)(msgOffset & 7);
		short cmdTmp =(short) (offset << 13 | 0x1fff & cmd);
		byteBuf.writeShort(cmdTmp);
		if(builder != null) {
			byteBuf.writeBytes(builder.build().toByteArray());
		}
		byteBuf.skipBytes(1);
		try {
			checkSumStream.clearSum();
			byteBuf.getBytes(byteBuf.readerIndex(), checkSumStream,
					byteBuf.readableBytes());
			byteBuf.setByte(0, checkSumStream.getCheckSum());
			channel.write(byteBuf.resetReaderIndex());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static int calculateVerificationBytes(int offset){
        int v = offset;
        v ^= v >> 8;
        v ^= v >> 4;
        v &= 0xff;
        return v;
    }
	public void send(final int cmd, final Message message) {
		if(channel == null || !channel.isActive()) {
			start();
		} 
		channel.eventLoop().execute(new Runnable() {
			
			@Override
			public void run() {
				if(is) {
					sendCheck(cmd, message.toBuilder());
				} else {
					ByteBuf byteBuf = MessageHelper.newDynamicMessage(cmd);
					if(message != null) {
						byteBuf.writeBytes(message.toByteArray());
					}
					channel.write(byteBuf);
				}
			}
		});
	}
}
