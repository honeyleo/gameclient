package cn.huizhi.client;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.protobuf.Message;

public class ResponseCmd {

	public static class MessageCmd {
		public String name;
		public int cmd;
		public Message message;
		public MessageCmd(String name, int cmd, Message message) {
			this.name = name;
			this.cmd = cmd;
			this.message = message;
		}
	}
	
	public static MessageCmd getMessageCmd(Integer cmd) {
		return CMD_MAP.get(cmd);
	}
	
	public static final Map<Integer, MessageCmd> CMD_MAP = Maps.newHashMap();
	
	public static void add(MessageCmd messageCmd) {
		CMD_MAP.put(messageCmd.cmd, messageCmd);
	}
	
	static {
		
	}
}
