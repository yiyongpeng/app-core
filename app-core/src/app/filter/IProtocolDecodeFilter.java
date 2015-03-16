package app.filter;

import java.nio.ByteBuffer;

import app.core.Connection;
import app.core.MessageOutput;
import app.filter.IFilterChain.FilterChain;

public interface IProtocolDecodeFilter extends IFilter {

	/**
	 * 协议解码过滤
	 * 
	 * @param session
	 * @param in
	 * @param out
	 * @param chain
	 * @return 是否解码到一个完整消息
	 */
	boolean messageDecode(Connection session, ByteBuffer in, MessageOutput out,
			FilterChain<IProtocolDecodeFilter> chain);

}
