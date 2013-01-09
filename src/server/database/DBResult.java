package server.database;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class DBResult {

	private PreparedStatement 	m_pst;
	private ResultSet 			m_rs;
	
	public DBResult(PreparedStatement pst, ResultSet rs) {
		m_pst = pst;
		m_rs  = rs;
	}
	
	public void close() {
		try {
			m_rs.close();
			m_pst.close();
		} catch (SQLException e) {
		}
	}
	
	public ResultSet getResultSet() {
		return m_rs;
	}	
}
