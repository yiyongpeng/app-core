package app.filter;

import app.core.Session;
import app.filter.IFilterChain.FilterChain;

public interface IErrFilter extends IFilter {

	void serverExcept(Session session, Throwable e,
			FilterChain<IErrFilter> filterChain);

}
