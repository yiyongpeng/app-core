package app.core;

import java.util.Collection;

public interface MessageOutput {

	void putLast(Object message);

	void putLastAll(Collection<Object> msg);

	boolean isFulled();
}
