package edu.northeastern.ccs.im.dao;

import edu.northeastern.ccs.im.dao.GroupRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

/**
 * GroupService class implements GroupRepository interface and all methods
 * necessary for CRUD operation on group of users.
 * @author Farha Jawed
 * @version 1.0
 */
public class GroupService implements GroupRepository {

	private DirContext context;
	private static final String GROUP_OU = "ou=groups,dc=example,dc=com";
	private static final String UNIQUE_MEMBER = "uniqueMember";
	private static final Logger logger = Logger.getLogger(UserService.class.getName());

	/**
	 * Constructor which connects our program to the Ldap server
	 * @throws NamingException
	 */
	public GroupService() throws NamingException {
		this.context = DirectoryUtil.getContext();
	}

	/**
	 * Creates a new group if there is not a group which exists by the same name
	 * @param group A group is a collection of users
	 * @return true if a new group is created, false otherwise
	 */
	@Override
	public boolean createGroup(Group group) {
		try {
			// Create a container set of attributes
			Attributes attributes = new BasicAttributes();

			// Create the objectclass to add
			Attribute attribute = new BasicAttribute("objectClass");
			attribute.add("groupOfUniqueNames");
			attributes.put(attribute);

			//add attributes to container
			attributes.put("cn",group.getGroupName());
			attributes.put(UNIQUE_MEMBER,GROUP_OU);
			String name = "cn=" + group.getGroupName() + ","+GROUP_OU;

			//create entry
			context.createSubcontext(name,attributes);
			logger.log(Level.INFO, "Group creation successful");
			return true;
		}
		catch(Exception e) {
			logger.log(Level.SEVERE,e.toString());
			return false;
		}
	}

	/**
	 * Iterates through all the groups present in the database and returns them
	 * @return List of groups present in the database
	 * @throws NamingException
	 */
	@Override
	public List<Group> findAllGroups() throws NamingException {
		String searchFilter = "(objectclass=groupOfUniqueNames)";
		String[] requiredAttributes = {"cn"};
		SearchControls controls = new SearchControls();
		controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		controls.setReturningAttributes(requiredAttributes);
		NamingEnumeration<?> groups = context.search(GROUP_OU, searchFilter, controls);
		SearchResult searchResult = null;
		List<Group>groupList = new ArrayList<>();
		while(groups.hasMore()) {
			searchResult = (SearchResult) groups.next();
			Attributes attr = searchResult.getAttributes();
			String groupname = attr.get("cn").get(0).toString();
			List<User>users = findAllUsersInGroup(groupname);
			Group group = new Group(groupname,users);
			groupList.add(group);
		}

		if(groupList.isEmpty()) {
			logger.log(Level.INFO,"No groups found!");
		}
		return groupList;
	}


	/**
	 * Finds a group by its name and returns it if it is present
	 * @param groupName Name of a group
	 * @return A Group data type which contains all the users present in it
	 */
	@Override
	public Group findGroupByName(String groupName){
		try {
			String searchFilter = "(&(objectclass=groupOfUniqueNames)(cn="+groupName+"))";
			String[] requiredAttributes = {"cn"};
			SearchControls controls = new SearchControls();
			controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			controls.setReturningAttributes(requiredAttributes);
			NamingEnumeration<?> groups = context.search(GROUP_OU, searchFilter, controls);
			SearchResult searchResult = null;
			Group group = null;
			while(groups.hasMore()) {
				searchResult = (SearchResult) groups.next();
				Attributes attr = searchResult.getAttributes();
				String groupname = attr.get("cn").get(0).toString();
				List<User>users = findAllUsersInGroup(groupname);
				group = new Group(groupname,users);
				logger.log(Level.INFO, "Group found! \nGroup name: "+group.getGroupName());
				return group;
			}
			logger.log(Level.INFO, "Group Not Found!");
			return null;
		}
		catch(Exception e) {
			logger.log(Level.SEVERE,"Exception:",e);
			return null;
		}
	}

	/**
	 *
	 * @param groupName Name of a group
	 * @return True if a group is deleted successfully
	 */
	@Override
	public boolean deleteGroup(String groupName) {
		try {
			context.destroySubcontext("cn=" + groupName + ","+GROUP_OU);
			logger.log(Level.INFO,"Success deleting {0}",groupName);
			return true;
		}
		catch(Exception e) {
			logger.log(Level.WARNING,"Entry couldn't be deleted");
			return false;
		}
	}

	/**
	 * Adds an user to a group
	 * @param groupName Name of a group
	 * @param user A client which needs to be added to a  group
	 * @return true if a user is added to a group successfully false otherwise
	 * @throws NamingException
	 */
	@Override
	public boolean addToGroup(String groupName, User user) throws NamingException {

		ModificationItem[] mods = new ModificationItem[1];
		Attribute mod0 = new BasicAttribute(UNIQUE_MEMBER,
				"uid="+user.getUsername()+","+GROUP_OU);
		mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, mod0);
		String name = "cn=" + groupName + ","+GROUP_OU;
		context.modifyAttributes(name, mods);
		logger.log(Level.INFO,"success adding "+user.getUsername());
		return true;

	}

	/**
	 * Deletes an user from a group
	 * @param groupName Name of a group
	 * @param user A client which needs to be deleted from a group
	 * @return true if a user is removed from a group successfully false otherwise
	 * @throws NamingException
	 */
	@Override
	public boolean deleteFromGroup(String groupName, User user) throws NamingException{
		ModificationItem[] mods = new ModificationItem[1];
		Attribute mod0 = new BasicAttribute(UNIQUE_MEMBER, "uid="+user.getUsername()+","+GROUP_OU);
		mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, mod0);
		String name = "cn=" + groupName + ","+GROUP_OU;
		context.modifyAttributes(name, mods);
		return true;
	}

	/**
	 * Lists all users present in a group
	 * @param groupName Name of a group
	 * @return List of users present in a group
	 * @throws NamingException
	 */
	private List<User> findAllUsersInGroup(String groupName) throws NamingException{
		List<User> users = new ArrayList<>();
		UserService service = new UserService();
		// Set up attributes to search for
		String[] searchAttributes = new String[1];
		searchAttributes[0] = UNIQUE_MEMBER;

		String name = "cn=" + groupName + ","+GROUP_OU;

		Attributes attributes =context.getAttributes(name,searchAttributes);
        
		if (attributes != null) {
			Attribute uniqueMemberAtts = attributes.get(UNIQUE_MEMBER);
			if (uniqueMemberAtts != null) {
				NamingEnumeration<?> vals = uniqueMemberAtts.getAll();
				while(vals.hasMore()) {
					Object objUserDN = vals.nextElement();
					String username = getUserUID((String)objUserDN);
					User user = service.findUserByUsername(username);
					if(user!=null) {
						users.add(user);
					}
				}
			}
		}
		return users;
	}


	/**
	 * Find all the groups in which a particular user is present
	 * @param username Unique username of a client
	 * @return List of all groups in which a user is present
	 */
	@Override
	public List<Group> findGroupsByUsername(String username) {
		try {
			String searchFilter = "(&(objectclass=groupOfUniqueNames)"
					+ "(uniqueMember=uid="+username+","+GROUP_OU+"))";
			String[] requiredAttributes = {"cn"};
			SearchControls controls = new SearchControls();
			controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			controls.setReturningAttributes(requiredAttributes);
			NamingEnumeration<?> groups = context.search(GROUP_OU, searchFilter, controls);
			SearchResult searchResult = null;
			List<Group> groupLists = new ArrayList<>();
			while(groups.hasMore()) {
				searchResult = (SearchResult) groups.next();
				Attributes attr = searchResult.getAttributes();
				String groupname = attr.get("cn").get(0).toString();
				List<User>users = findAllUsersInGroup(groupname);
				Group group = new Group(groupname,users);
				groupLists.add(group);
			}
			return groupLists;
		}
		catch(Exception e) {
			logger.log(Level.SEVERE,"Exception: ",e);
			return Collections.emptyList();
		}
	}


	private String getUserUID(String userDN) {
		int start = userDN.indexOf('=');
		int end = userDN.indexOf(',');
		if (end == -1) {
			end = userDN.length();
		}
		return userDN.substring(start+1, end);
	}

}
