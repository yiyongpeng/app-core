package app.filter;

import app.core.Connection;
import app.core.MessageOutput;
import app.filter.IFilterChain.FilterChain;

public interface IProtocolEncodeFilter extends IFilter {

	void messageEncode(Connection conn, Object message, MessageOutput out,
			FilterChain<IProtocolEncodeFilter> chain) throws Exception;

}
