package server.database;

import java.sql.*;

import server.classes.Util;

public class DBConnection {
	
	Connection connection = null;
	
	public DBConnection (String username, String password, String db_name) throws SQLException {	
		
		this.connection = DriverManager.getConnection("jdbc:postgresql://localhost/" + db_name, username, password);
	}
	
	public DBResult query(String query, Object... args) throws SQLException {

		PreparedStatement pst = null;
		ResultSet rs = null;
		
		pst = this.connection.prepareStatement(query);
		
		for (int i = 0; i < args.length; i++) {
			String type = args[i].getClass().toString();
			if (type.contains("String")) {
				pst.setString(i+1, (String) args[i]);
			} else if (type.contains("Integer")) {
				pst.setInt(i+1, (int) args[i]);
			} else if (type.contains("Boolean")) {
				pst.setBoolean(i+1, (boolean)args[i]);
			} else if (type.contains("Date")) {
				java.util.Date normal = (java.util.Date)args[i];
				java.sql.Timestamp date = new java.sql.Timestamp(normal.getTime());
				pst.setTimestamp(i+1, date);
			}
		}
		
		Util.print(pst.toString());
		
		rs = pst.executeQuery();
		
		return new DBResult(pst, rs);
	}
	
	public void noResponseQuery (String query, Object... args) throws SQLException {
		
		PreparedStatement pst = this.connection.prepareStatement(query);
		
		for (int i = 0; i < args.length; i++) {
			String type = args[i].getClass().toString();
			if (type.contains("String")) {
				pst.setString(i+1, (String) args[i]);
			} else if (type.contains("Integer")) {
				pst.setInt(i+1, (Integer) args[i]);
			} else if (type.contains("Boolean")) {
				pst.setBoolean(i+1, (boolean) args[i]);
			}  else if (type.contains("Date")) {
				java.util.Date normal = (java.util.Date)args[i];
				java.sql.Timestamp date = new java.sql.Timestamp(normal.getTime());
				pst.setTimestamp(i+1, date);
			}
		}
		
		Util.print(pst.toString());
		pst.executeUpdate();
	}
	
	public void close() throws SQLException {
		this.connection.close();
	}
	
	public Connection getConnection() {
		return this.connection;
	}
}
