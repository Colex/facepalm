package server.classes;

import java.io.Serializable;
import java.util.Date;

public class Comment implements Serializable {
	
	private static final long serialVersionUID = 2098401119801161981L;
	
	private int		m_comment_id;
	private int 	m_post_id;
	private int 	m_user_id;
	private Date 	m_sending;
	private String 	m_content;
	
	public Comment(int comment_id, int post_id, int user_id, Date date, String content) {
		this.m_comment_id 	= comment_id;
		this.m_post_id		= post_id;
		this.m_user_id		= user_id;
		this.m_sending		= date;
		this.m_content		= content;
	}

	public int getCommentId() {
		return m_comment_id;
	}

	public void setCommentId(int m_comment_id) {
		this.m_comment_id = m_comment_id;
	}

	public int getPostId() {
		return m_post_id;
	}

	public void setPostId(int m_post_id) {
		this.m_post_id = m_post_id;
	}

	public int getUserId() {
		return m_user_id;
	}

	public void setUserId(int m_user_id) {
		this.m_user_id = m_user_id;
	}

	public Date getSending() {
		return m_sending;
	}
	

	public void setSending(Date m_sending) {
		this.m_sending = m_sending;
	}

	public String getContent() {
		return m_content;
	}

	public void setContent(String m_content) {
		this.m_content = m_content;
	}
}
