package server.classes;

import java.io.Serializable;

public class Attach implements Serializable {

	private static final long serialVersionUID = 1171452064467305911L;
	
	private int m_attach_id;
	private int m_message_id;
	private String m_file_path;
	
	public Attach(int attach_id, int message_id, String file_path) {
		this.m_attach_id = attach_id;
		this.m_message_id = message_id;
		this.m_file_path = file_path;
	}

	public int getAttach_id() {
		return m_attach_id;
	}

	public void setAttach_id(int attach_id) {
		this.m_attach_id = attach_id;
	}

	public int getMessage_id() {
		return m_message_id;
	}

	public void setMessage_id(int message_id) {
		this.m_message_id = message_id;
	}


	public String getFile_path() {
		return m_file_path;
	}

	public void setFile_path(String file_path) {
		this.m_file_path = file_path;
	}
}