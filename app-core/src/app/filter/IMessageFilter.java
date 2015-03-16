package app.filter;

import app.core.Session;
import app.filter.IFilterChain.FilterChain;

public interface IMessageFilter extends IFilter {

	void messageReceived(Session session, Object message,
			FilterChain<IMessageFilter> chain);

}
