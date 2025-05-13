package egovframework.example.sample.service;

import java.io.Serializable;

public class LoginVO  implements Serializable {
	private static final long serialVersionUID = -8274004534207618049L;
	
	/** 아이디 */
	private String id;
	/** 이름 */
	private String name;
	private String password;
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
