package cn.huizhi.client;

import java.util.List;
import java.util.Map;

import cn.huizhi.car.pb.player.UserPropertyReq_Protocol.UserPropertyReqPro;
import cn.huizhi.net.AppMessage;
import cn.huizhi.util.MessageBuild;
import cn.huizhi.util.MessageBuild.FieldObject;
import cn.lfyun.network.message.LoginByPidReq_Protocol.LoginByPidReq;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.Message;

public class RequestCmd {

	public static class MessageCmd {
		public String name;
		public int cmd;
		public Message.Builder builder;
		public MessageCmd(String name, int cmd, Message.Builder builder) {
			this.name = name;
			this.cmd = cmd;
			this.builder = builder;
		}
	}
	
	public static MessageCmd getMessageCmd(String name) {
		return CMD_MAP.get(name);
	}
	
	public static void add(MessageCmd messageCmd) {
		INTERFACE_LIST.add(messageCmd.name);
		CMD_MAP.put(messageCmd.name, messageCmd);
	}
	public static final List<String> INTERFACE_LIST = Lists.newArrayList();
	public static final Map<String, MessageCmd> CMD_MAP = Maps.newHashMap();
	
	static {
		add(new MessageCmd("玩家ID登陆",AppMessage.CMD_LOGIN_BY_PID_REQ, LoginByPidReq.getDefaultInstance().newBuilderForType()));
		add(new MessageCmd("用户财产信息", AppMessage.CMD_USER_PROPERTY_REQ, UserPropertyReqPro.getDefaultInstance().newBuilderForType()));
	}
	
	public static List<String> names() {
		return INTERFACE_LIST;
	}
	public static int size() {
		return CMD_MAP.size();
	}
	
	public static Object[][] getTableData(String name) {
		MessageCmd messageCmd = RequestCmd.getMessageCmd(name);
		if(messageCmd.builder != null) {
			List<FieldObject> fieldList = MessageBuild.getFields(messageCmd.builder);
			Object[][] data = new Object[fieldList.size()][3];
			for(int i = 0; i < fieldList.size(); i ++) {
				FieldObject fieldObject = fieldList.get(i);
				data[i][0] = fieldObject.name;
				data[i][1] = fieldObject.javaType.name();
				data[i][2] = null;
			}
			return data;
		}
		return null;
	}
}
