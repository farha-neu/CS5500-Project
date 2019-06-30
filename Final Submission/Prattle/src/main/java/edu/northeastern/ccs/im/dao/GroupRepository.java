package edu.northeastern.ccs.im.dao;


import java.util.List;

import javax.naming.NamingException;

/**
 * This interface provides methods necessary for CRUD operation on groups
 * @author Farha Jawed
 * @version 1.0
 *
 */
public interface GroupRepository {

       /**
        * It creates a group and persists it in LDAP
        * @param group
        * @return true if group is created successfully
        */
	   boolean createGroup(Group group);

	   /**
	    * It retrieves all groups stored in directory
	    * @return list of groups
	    * @throws NamingException
	    */
	   List<Group> findAllGroups() throws NamingException;

	   /**
	    * It finds a group by the group name
	    * @param groupName
	    * @return Group if found. It returns null if group is not found
	    */
	   Group findGroupByName(String groupName);

	   /**
	    * It deletes a group by the group name provided
	    * @param groupName
	    * @return true if group can be deleted. 
	    * @exception if group doesn't exist and couldn't be deleted.
	    */
	   boolean deleteGroup(String groupName);

	   /**
	    * It adds a user to a group 
	    * @param groupName is the name of the group where the uses is to be added
	    * @param user
	    * @return true if user can be added successfully
	    * @throws NamingException
	    */
	   boolean addToGroup(String groupName, User user) throws NamingException;

	   /**
	    * It deletes a user from group
	    * @param groupName is the name of the group from where the user is to be deleted
	    * @param user
	    * @return true if delete operation is successful
	    * @throws NamingException
	    */
	   boolean deleteFromGroup(String groupName, User user) throws NamingException;
	   
	   /**
	    * It lists all the groups of a user
	    * @param username
	    * @return list of groups
	    */
	   List<Group> findGroupsByUsername(String username);
	   		
}
