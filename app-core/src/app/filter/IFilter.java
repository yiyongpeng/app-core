package app.filter;

import app.filter.IFilterChain.INextFilter;
import app.filter.IFilterChain.IPrevFilter;

public interface IFilter {

	void onAdded(IFilterChain filterChain, IPrevFilter prevFilter);

	void onRemoved(IFilterChain filterChain, IPrevFilter prevFilter);

	boolean onPrevFilterAdd(IFilterChain filterChain, IPrevFilter prevFilter);

	boolean onNextFilterAdd(IFilterChain filterChain, INextFilter nextFilter);

	boolean onPrevFilterRemove(IFilterChain filterChain, IPrevFilter prevFilter);

	boolean onNextFilterRemove(IFilterChain filterChain, INextFilter nextFilter);

}
