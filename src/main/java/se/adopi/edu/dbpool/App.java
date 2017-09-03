package se.adopi.edu.dbpool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class App {
	private static void populate(Connection conn) throws SQLException {
		PreparedStatement st = conn.prepareStatement("INSERT INTO public.keyvalues ( kv_key, kv_value) VALUES ( ?, ?)");
		for (int i = 1000; i < 100000; i++) {
			st.setString(1, "straw"+i);
			st.setString(2, "This is not the needle you are looking for.");
			st.execute();
		}
	}
	private static String findNeedle(Connection conn) throws SQLException {
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("SELECT kv_value FROM keyvalues WHERE kv_key LIKE 'needle'");
		
		if (rs.next()) {
			return rs.getString("kv_value");
		} else {
			return "Couldn't find the needle. :(";
		}
	}
	
	public static Connection getConnection() throws SQLException {
		String url = "jdbc:postgresql://localhost/edu";
		Properties props = new Properties();
		props.setProperty("user","edu");
		props.setProperty("password","password");
		return DriverManager.getConnection(url, props);
	}
	public static void main( String[] args ) throws SQLException, ClassNotFoundException {
		Class.forName("org.postgresql.Driver");

		//Have already populated the table, don't execute again. :)
		/*
		Connection conn = getConnection();
		populate(conn);
		conn.close();
		*/

		long start = System.currentTimeMillis();
		Connection conn = getConnection();
		System.out.println("First connection: " + (System.currentTimeMillis() - start) + "ms");
		findNeedle(conn);
		conn.close();
		System.out.println("Finding first needle (incl. getConnection()): " + (System.currentTimeMillis() - start) + "ms");

		start = System.currentTimeMillis();
		conn = getConnection();
		System.out.println("Second connection: " + (System.currentTimeMillis() - start) + "ms");
		findNeedle(conn);
		conn.close();
		System.out.println("Finding another needle (not using pool): " + (System.currentTimeMillis() - start) + "ms");

		start = System.currentTimeMillis();
		for (int i = 0; i < 1000; i++) {
			conn = getConnection();
			findNeedle(conn);
			conn.close();
		}
		System.out.println("Finding 100 needles (not using pool): " + (System.currentTimeMillis() - start) + "ms");

		start = System.currentTimeMillis();
		SimpleConnectionPool.init();
		System.out.println("Init time for the connection pool: " + (System.currentTimeMillis() - start) + "ms");

		start = System.currentTimeMillis();
		for (int i = 0; i < 1000; i++) {
			conn = SimpleConnectionPool.getConnection();
			findNeedle(conn);
			SimpleConnectionPool.returnConnection(conn);
		}
		System.out.println("Finding 100 needles using pool: " + (System.currentTimeMillis() - start) + "ms");
	}
}

/*
 * Inte säker varför, men första gången man kör DriverManager.getConnection() tar det ~10x längre tid.
 * Jag kan inte se att DriverManager har någon Pool, så antar att det är själva Postgres-drivrutinen
 * som gör nån magi vid första initieringen.
 */
