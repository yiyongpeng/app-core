package app.filter;

import org.apache.log4j.Logger;

import app.filter.IFilterChain.INextFilter;
import app.filter.IFilterChain.IPrevFilter;

public class FilterAdapter implements IFilter {
	protected Logger log = Logger.getLogger(getClass());

	@Override
	public void onAdded(IFilterChain filterChain, IPrevFilter prevFilter) {
	}

	@Override
	public void onRemoved(IFilterChain filterChain, IPrevFilter prevFilter) {
	}

	@Override
	public boolean onPrevFilterAdd(IFilterChain filterChain,
			IPrevFilter prevFilter) {
		return true;
	}

	@Override
	public boolean onNextFilterAdd(IFilterChain filterChain,
			INextFilter nextFilter) {
		return true;
	}

	@Override
	public boolean onNextFilterRemove(IFilterChain filterChain,
			INextFilter nextFilter) {
		return true;
	}

	@Override
	public boolean onPrevFilterRemove(IFilterChain filterChain,
			IPrevFilter prevFilter) {
		return true;
	}

}
