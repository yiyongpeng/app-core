package app.filter.impl;

import java.util.HashMap;
import java.util.Map;

import app.filter.IFilter;
import app.filter.IFilterChain;

public class DefaultFilterChain implements IFilterChain {
	private Map<String, ChainImpl> filterMap = new HashMap<String, ChainImpl>();

	@Override
	public ChainImpl getFirstChain(String name) {
		return filterMap.get(name);
	}

	@Override
	public void addFirstFilter(String name, IFilter filter) {
		ChainImpl chain = getFirstChain(name);
		if (chain != null) {
			if (chain.find(filter)
					|| !chain.getFilter().onPrevFilterAdd(this,
							new PrevFilter(filter))) {
				throw new IllegalArgumentException(
						"This filter can not add in front of. "
								+ chain.getFilter());
			}
		}
		chain = new ChainImpl(filter, chain);
		filterMap.put(name, chain);
		filter.onAdded(this, null);
	}

	@Override
	public void addLastFilter(String name, IFilter filter) {
		if (filter == null) {
			throw new NullPointerException("add filter is null.");
		}
		ChainImpl chain = getFirstChain(name);
		if (chain == null) {
			chain = new ChainImpl(filter);
			filterMap.put(name, chain);
			filter.onAdded(this, null);
		} else {
			chain = chain.getLast();
			if (chain.find(filter)
					|| !chain.getFilter().onNextFilterAdd(this,
							new NextFilter(filter))) {
				throw new IllegalArgumentException(
						"This filter cannot load the last!");
			} else {
				chain.next = new ChainImpl(filter);
				filter.onAdded(this, new PrevFilter(chain.filter));
			}
		}
	}

	@Override
	public void removeFilter(String name, IFilter filter) {
		if (filter == null)
			throw new NullPointerException("Filter is null.");
		ChainImpl chain = getFirstChain(name);
		if (chain == null)
			throw new IllegalArgumentException("Not found!");
		IPrevFilter prevFilter = null;
		if (chain.getFilter() == filter) {
			filterMap.put(name, chain.getNext());
		} else {
			ChainImpl result = chain.findPrev(filter);
			if (result == null)
				throw new IllegalArgumentException("Not found!");
			if (result.getFilter().onNextFilterRemove(this,
					new NextFilter(filter))
					&& (result.getNext().hasNext() == false || result.getNext()
							.getNext().getFilter()
							.onPrevFilterRemove(this, new PrevFilter(filter)))) {
				prevFilter = new PrevFilter(result.getFilter());
				result.next = result.getNext().getNext();
			} else {
				throw new IllegalArgumentException(
						"This filter can't be removed.");
			}
		}
		filter.onRemoved(this, prevFilter);
	}

	private class ChainImpl implements IChain<IFilter> {
		private IFilter filter;
		private ChainImpl next;

		public ChainImpl(IFilter filter) {
			this.filter = filter;
		}

		public boolean find(IFilter filter) {
			return this.filter == filter ? true : (hasNext() ? getNext().find(
					filter) : false);
		}

		public ChainImpl(IFilter filter, ChainImpl next) {
			this.filter = filter;
			this.next = next;
		}

		@Override
		public IFilter getFilter() {
			return filter;
		}

		public ChainImpl getLast() {
			return next == null ? this : next.getLast();
		}

		public ChainImpl findPrev(IFilter filter) {
			return next != null ? (next.getFilter() == filter ? this : next
					.findPrev(filter)) : null;
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public ChainImpl getNext() {
			return next;
		}

		@Override
		public IFilter nextFilter() {
			return next != null ? next.getFilter() : null;
		}
	}

	private class PrevFilter implements IPrevFilter {
		IFilter filter;

		public PrevFilter(IFilter filter) {
			this.filter = filter;
		}

		@Override
		public IFilter getFilter() {
			return filter;
		}
	}

	private class NextFilter implements INextFilter {
		IFilter filter;

		public NextFilter(IFilter filter) {
			this.filter = filter;
		}

		@Override
		public IFilter getFilter() {
			return filter;
		}
	}
}
