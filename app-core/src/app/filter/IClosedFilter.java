package app.filter;

import app.core.Connection;
import app.filter.IFilterChain.FilterChain;

public interface IClosedFilter extends IFilter {

	void sessionClosed(Connection session,
			FilterChain<IClosedFilter> filterChain);

}
