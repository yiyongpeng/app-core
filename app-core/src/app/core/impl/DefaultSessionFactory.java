package app.core.impl;

import app.core.Connection;
import app.core.Session;
import app.core.SessionFactory;

public class DefaultSessionFactory implements SessionFactory {

	@Override
	public Session create(Connection conn, Object sessionId) {
		DefaultSession session = new DefaultSession(String.valueOf(sessionId));
		session.init(conn);
		return session;
	}

}
