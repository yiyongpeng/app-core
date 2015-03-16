package app.filter;

import java.nio.channels.SelectableChannel;

import app.core.Connection;
import app.core.ServerContext;
import app.filter.IFilterChain.FilterChain;

public interface IAcceptorFilter extends IFilter {

	Connection sessionAccept(ServerContext serverHandler,
			SelectableChannel socket, FilterChain<IAcceptorFilter> filterChain)
			throws Exception;

	Connection sessionOpened(ServerContext serverHandler, Connection session,
			FilterChain<IAcceptorFilter> filterChain) throws Exception;

}
