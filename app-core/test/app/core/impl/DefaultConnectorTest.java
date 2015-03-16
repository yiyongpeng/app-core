package app.core.impl;

import app.core.Connection;
import app.core.Session;
import junit.framework.TestCase;

public class DefaultConnectorTest extends TestCase {

	public void testStartup2Shutdown() {
		DefaultConnector<Connection, Session> connector = new DefaultConnector<Connection, Session>();

		connector.start();
		assertTrue(connector.isRuning());

		connector.stop();
		assertTrue(!connector.isRuning());
	}
}
