package cn.huizhi.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

public class MessageHelper {

	public static ByteBuf newDynamicMessage(int msgId) {
        return newDynamicMessage(msgId, 64);
    }

    /**
     * 返回的ChannelBuffer会按需加大
     * 
     * @param msgId
     * @param approxSize
     * @return
     */
    public static ByteBuf newDynamicMessage(int msgId, int approxSize) {
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer(approxSize);
        buffer.writeShort(msgId);
        return buffer;
    }
    
	public static ByteBuf newFixedSizeMessage(int msgId, int sureSize) {
		ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer(sureSize);
        buffer.writeShort(msgId);
        return buffer;
    }

    public static ByteBuf onlySendHeaderMessage(int msgId) {
        return newFixedSizeMessage(msgId, 2);
    }
}
