package edu.northeastern.ccs.im.dao;

import java.util.List;

/**
 * A group is a cluster of users(clients) which can chat together at the same time
 * @author Farha Jawed
 * @version 1.0
 *
 */

public class Group {

	private String groupName;
	private List<User>users;

	/**
	 * Constructor initializes the name of a group
	 * @param groupName name of a group of users
	 */
	public Group(String groupName) {
		this.groupName = groupName;
	}

	/**
	 * Constructor initializes the name of a group and the users in it
	 * @param groupName name of a group of users
	 * @param users List of users present in a group
	 */
	public Group(String groupName,List<User>users) {
		this.groupName = groupName;
		this.users = users;
	}

	/**
	 * @return name of the group
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * @return  list of users present in a group
	 */
	public List<User> getUsers() {
		return users;
	}

}

