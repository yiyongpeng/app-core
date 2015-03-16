package app.filter;

import app.core.ServerContext;
import app.filter.IFilterChain.FilterChain;

public interface IAcceptFilter extends IFilter {

	void serverAccept(ServerContext serverHandler,
			FilterChain<IAcceptFilter> filterChain) throws Exception;

}
