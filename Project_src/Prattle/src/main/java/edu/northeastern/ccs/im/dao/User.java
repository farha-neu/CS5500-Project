package edu.northeastern.ccs.im.dao;

import edu.northeastern.ccs.im.Message;
import java.util.List;

/**
 * @author Pratik Devikar
 * @version 1.0
 */
public class User {

	private String fullName;
	private String lastName;
	private String username;
	private String password;
	private String email;
	private List<Message> queuedMsgs;
	private boolean isOnline=false;
	private boolean isBacklogBeingLoaded=false;

	/**
	 * An empty constructor
	 */
	public User() {}

	/**
	 * Constructor of the class which initializes username
	 * @param username unique user name of a client
	 */
	public User(String username) {
		this.username = username;
	}

	/**
	 * Constructor of the class which initializes username and password
	 * @param username unique username of a client
	 * @param password password for the above username
	 */
	public User(String username,String password) {
		this.username = username;
		this.password = password;
	}

	public void setQueuedMsgs(List<Message> queuedMsgs) {
		this.queuedMsgs = queuedMsgs;
	}

	/**
	 * Constructor of the class which initializes username, password, full name and email addrress
	 * @param fullName First and last name of a client
	 * @param lastName lastname of a client
	 * @param username unique username of a client
	 * @param email email address of a client
	 */
	public User(String fullName, String lastName, String username,String email) {
		this.fullName = fullName;
		this.username = username;
		this.lastName = lastName;
		this.email = email;
	}

	public User(String fullName, String lastName, String username, String password, String email) {
		this(fullName,lastName,username,email);
		this.password = password;
	}


	public User(String fullName, String lastName, String username, String password, String email, List<Message>
			queuedMsgs) {
		this(fullName,username,password,email);
		this.lastName = lastName;
		this.queuedMsgs = queuedMsgs;
	}

	public User(String fullName, String lastName, String username, String email, List<Message>
			queuedMsgs) {
		this(fullName,lastName,username,email);
		this.lastName = lastName;
		this.queuedMsgs = queuedMsgs;
	}

	/**
	 * @return first and last name of a client
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 * @return last name of a client
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @return unique username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return password for the given username of a client
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return email address of a client
	 */
	public String getEmail() {
		return email;
	}
	
	
	public List<Message> getQueuedMsgs() {
		return queuedMsgs;
	}

	public boolean isOnline() {
		return isOnline;
	}

	public void setOnline(boolean online) {
		isOnline = online;
	}

	public boolean isBacklogBeingLoaded() {
		return isBacklogBeingLoaded;
	}

	public void setBacklogBeingLoaded(boolean backlogBeingLoaded) {
		isBacklogBeingLoaded = backlogBeingLoaded;
	}
}
