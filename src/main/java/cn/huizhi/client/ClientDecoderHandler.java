package cn.huizhi.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.huizhi.client.ResponseCmd.MessageCmd;
import cn.huizhi.net.AppMessage;
import cn.huizhi.net.MessageHelper;
import cn.huizhi.util.Config;

import com.google.protobuf.Message;

public class ClientDecoderHandler extends SimpleChannelInboundHandler<ByteBuf> {

	private static final Logger LOG = LoggerFactory.getLogger("o");
	private int pid;
	
	ProtobufClient protobufClient;
	ScheduledFuture<?> skillFireFuture = null;
	ScheduledFuture<?> uploadScorefuture = null;
	
	static final int uploadScoreRate = Config.getIntValue("upload.score.rate");
	
	public ClientDecoderHandler() {
		
	}
	
	public ClientDecoderHandler(ProtobufClient protobufClient, int pid) {
		this.protobufClient = protobufClient;
		this.pid = pid;
	}
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg)
			throws Exception {
		int length = msg.readShort();
		int cmd = msg.readShort();
		if(cmd == AppMessage.CMD_PING) {
			return;
		}
		int errorCode = msg.readShort();
		MessageCmd messageCmd = ResponseCmd.getMessageCmd(cmd);
		if(messageCmd == null) {
			return;
		}
		int dataLength = 0;
		byte[] data = new byte[0];
		if(errorCode == 1) {
			dataLength = length - 4;
			data = new byte[dataLength];
			msg.readBytes(data);
			if(messageCmd.message != null && this.pid == 0 && data.length > 0) {
				try {
					Message message = messageCmd.message.getParserForType().parseFrom(data);
					StringBuilder sb = new StringBuilder();
					sb.append("====================").append(messageCmd.name).append("======================\n");
					sb.append(message.toString());
					sb.append("====================").append(messageCmd.name).append("======================\n");
					LOG.info(sb.toString());
					UIMain.jTextArea1.append(sb.toString());
				} catch(Throwable e) {
					e.printStackTrace();
				}
			}
		}
		switch (cmd) {
		case AppMessage.CMD_C2S_LOGIN_RESULT:
			heart(ctx, data);
			break;
		default:
			break;
		}
		StringBuilder sb = new StringBuilder("message[name=").append(messageCmd.name);
		sb.append(",cmd=0x").append(Integer.toHexString(cmd));
		sb.append(",errorCode=").append(errorCode);
		sb.append(",size=").append(dataLength).append((this.pid == 0 ? "" : ",pid=" + this.pid));
		sb.append("]\n");
		if(this.pid == 0) {
			UIMain.jTextArea1.append(sb.toString());
		} else {
			LOG.info(sb.toString());
		}
		
	}
	
	private void heart(final ChannelHandlerContext ctx, byte[] data) {
		ctx.executor().scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				if(protobufClient.is) {
					ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer(64);
					byteBuf.writeByte(0);
					byteBuf.writeByte(protobufClient.msgOffset.getAndIncrement());
					byteBuf.writeShort(AppMessage.CMD_PING);
					byteBuf.skipBytes(1);
					try {
						byteBuf.getBytes(byteBuf.readerIndex(), protobufClient.checkSumStream,
								byteBuf.readableBytes());
						byteBuf.setByte(0, protobufClient.checkSumStream.getCheckSum());
						ctx.channel().write(byteBuf);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					ByteBuf ping = MessageHelper.newDynamicMessage(AppMessage.CMD_PING);
					ctx.channel().write(ping);
				}
			}
		}, 5, 5, TimeUnit.SECONDS);
	}

	
	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		if(this.pid == 0) {
			UIMain.jTextArea1.append("玩家pid=" + this.pid + "掉线\n");
		} else {
			LOG.info("玩家pid=" + this.pid + "掉线\n");
		}
	}

}
