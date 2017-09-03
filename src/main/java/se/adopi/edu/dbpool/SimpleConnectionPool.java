package se.adopi.edu.dbpool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class SimpleConnectionPool {
	private static ArrayList<Connection> pool = null;

	public static void init() {
		pool = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			try {
				pool.add(App.getConnection());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	public static Connection getConnection() throws SQLException {
		synchronized (pool) {
			if (pool == null) init();
			if (pool.size() < 1) {
				return App.getConnection();
			} else {
				return pool.remove(0);
			}
		}
	}
	public static void returnConnection(Connection c) {
		synchronized (pool) {
			if (pool == null) init();
			pool.add(c);
		}
	}
}
