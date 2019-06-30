package edu.northeastern.ccs.im.dao;


/** This class implements implements UserRepository interface and all methods
 * necessary for CRUD operation on users
 * @author Pratik Devikar
 * @author farha
 */

import edu.northeastern.ccs.im.Message;
import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackMessage;

import java.security.MessageDigest;
import java.util.ArrayList;
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

import java.util.Base64;

public class UserService implements UserRepository{

	private DirContext context;
	private List<User> userList;
	private GroupService groupService = new GroupService();
	private static final String USER_PW = "userpassword";
	private static final String USER_OU = "ou=users,dc=example,dc=com";
	private static final Logger logger = Logger.getLogger(UserService.class.getName());
	private String exceptionMsg= "Exception: ";
	private String successMsg = "Success editing ";

	// Link to our slack channel
	private static final SlackApi api = new SlackApi("https://hooks.slack" +
			".com/services/T2CR59JN7/BEE922DFU/duUie9jYv4Hih09FyeeXCgIp");

	/**
	 * Connects our program to the LDAP database
	 * @throws NamingException
	 */
	public UserService() throws NamingException {
		this.context = DirectoryUtil.getContext();
	}

	/**
	 * Create an entry of an user in our database
	 * @param user A client who wishes to join our servers
	 * @return True if a user is successfully registered false otherwise
	 */
	public boolean createUser(User user) {
		try {
			// Create a container set of attributes
			Attributes attributes = new BasicAttributes();

			// Create the objectclass to add
			Attribute attribute = new BasicAttribute("objectClass");
			attribute.add("inetOrgPerson");
			attributes.put(attribute);

			//add attributes to container
			attributes.put("uid",user.getUsername());
			attributes.put("cn",user.getFullName());
			attributes.put("sn",user.getLastName());
			attributes.put(USER_PW,encryptLdapPassword("SHA", user.getPassword()));
			attributes.put("mail",user.getEmail());
			attributes.put("info", new ArrayList<String>());
			String name = "uid=" + user.getUsername() + ","+USER_OU;

			//create entry
			context.createSubcontext(name,attributes);
			logger.log(Level.INFO, "Success");
			return true;
		}
		catch(Exception e) {
			logger.log(Level.WARNING, "Message", e);
			logger.log(Level.SEVERE,"Username already exists!");
			return false;
		}
	}


	/** 
	 * @return a list of all users in the database
	 * @throws NamingException
	 */
	@Override
	public List<User> findAllUsers() throws NamingException {
		String searchFilter = "(objectclass=inetOrgPerson)";
		String[] requiredAttributes = {"sn","cn","uid",USER_PW,"mail"};
		SearchControls controls = new SearchControls();
		controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		controls.setReturningAttributes(requiredAttributes);
		NamingEnumeration<?> users = context.search(USER_OU, searchFilter, controls);
		SearchResult searchResult = null;
		userList = new ArrayList<>();
		while(users.hasMore()) {
			searchResult = (SearchResult) users.next();
			Attributes attr = searchResult.getAttributes();
			String username = attr.get("uid").get(0).toString();
			String fullname = attr.get("cn").get(0).toString();
			String lastname = attr.get("sn").get(0).toString();
			String email = attr.get("mail").get(0).toString();
			User user = new User(fullname,lastname,username,email);
			userList.add(user);
		}
		printUsers();
		if(userList.isEmpty()) {
			logger.log(Level.INFO,"No users found!");
		}
		return userList;
	}

	/**
	 * Prints all the users information
	 */
	private void printUsers() {
		for(User user : userList) {
			logger.log(Level.INFO,"Username: "+user.getUsername());
			logger.log(Level.INFO,"Full Name: "+user.getFullName());
			logger.log(Level.INFO,"Last Name: "+user.getLastName());
			logger.log(Level.INFO,"Email: "+user.getEmail());
		}
	}

	/**
	 * Finds the information of the user by its username
	 * @param usernameToSearch unique username of an user
	 * @return Information of an user in User format
	 */
	@Override
	public User findUserByUsername(String usernameToSearch){
		try {

			String searchFilter = "(&(objectclass=inetOrgPerson)(uid="+usernameToSearch+"))";

			String[] requiredAttributes = {"sn","cn","uid",USER_PW,"mail"};
			SearchControls controls = new SearchControls();
			controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			controls.setReturningAttributes(requiredAttributes);
			NamingEnumeration<?> users = context.search("ou=users,o=Company", searchFilter, controls);
			SearchResult searchResult = null;
			User user = null;
			while(users.hasMore()) {
				searchResult = (SearchResult) users.next();
				Attributes attr = searchResult.getAttributes();
				String username = attr.get("uid").get(0).toString();
				String fullname = attr.get("cn").get(0).toString();
				String lastname = attr.get("sn").get(0).toString();
				String email = attr.get("mail").get(0).toString();
				ArrayList<Message> msglist = (ArrayList<Message>) attr.get("info").get(0);
				user= new User(fullname,lastname,username,email, msglist);
				logger.log(Level.INFO, "User found! \nFullname: "+user.getFullName()+"\nEmail: "+user.getEmail());
				return user;
			}

			logger.log(Level.INFO,"User not found!");

			return null;
		}
		catch(Exception e) {
			logger.log(Level.SEVERE,exceptionMsg,e);
			return null;
		}
	}

	/**
	 * @returns true if user name and password matches
	 * @param user A unique user
	 */
	@Override
	public boolean login(User user) {
		try {
			String password = encryptLdapPassword("SHA", user.getPassword());
			String searchFilter = "(&(objectclass=inetOrgPerson)"
					+ "(uid="+user.getUsername()+")(userpassword="+password+"))";
			String[] requiredAttributes = {"sn","cn","uid",USER_PW,"mail"};
			SearchControls controls = new SearchControls();
			controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			controls.setReturningAttributes(requiredAttributes);
			NamingEnumeration<?> users = context.search(USER_OU, searchFilter, controls);
			SearchResult searchResult = null;
			while(users.hasMore()) {
				searchResult = (SearchResult) users.next();
				Attributes attr = searchResult.getAttributes();
				if(attr.get("uid")!=null) {
					logger.log(Level.INFO,"Login is successful");
					user.setOnline(true);
					return true;
				}
			}
			logger.log(Level.WARNING,"Username and password don't match");
			api.call(new SlackMessage("Failed login by the user:- "+user.getUsername()));
			return false;
		}
		catch(Exception e) {
			logger.log(Level.SEVERE,exceptionMsg,e);
			return false;
		}
	}

	/**
	 * @param user A unique client
	 * @return True if the changes are successfully done false otherwise
	 * @throws NamingException
	 */
	@Override
	public boolean updateUser(User user) throws NamingException {
		ModificationItem[] mods = new ModificationItem[4];
		Attribute mod0 = new BasicAttribute("mail", user.getEmail());
		Attribute mod1 = new BasicAttribute(USER_PW, encryptLdapPassword("SHA", user.getPassword()));
		Attribute mod2 = new BasicAttribute("cn", user.getFullName());
		Attribute mod3 = new BasicAttribute("sn", user.getLastName());
		mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, mod0);
		mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, mod1);
		mods[2] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, mod2);
		mods[3] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, mod3);
		context.modifyAttributes("uid=" + user.getUsername() + ","+USER_OU, mods);
		logger.log(Level.INFO,successMsg,user.getUsername());
		return true;
	}

	/**
	 * Update the users' queue messages
	 * @param username
	 * @param msg
	 * @return
	 */
	public boolean updateUserQueuedMsgs(String username, Message msg) {
		try {
			ModificationItem[] mods = new ModificationItem[1];
			User user = this.findUserByUsername(username);
			List<Message> queuedMsgs = user.getQueuedMsgs();
			queuedMsgs.add(msg);
			update(mods, queuedMsgs, user);
			return true;
		} catch (Exception e) {
			logger.log(Level.SEVERE,exceptionMsg,e);
			return false;
		}
	}

	/**
	 * Recalls the message for a user
	 * @param username
	 * @param msgList
	 * @return
	 */
	public boolean updateUserQueuedMsgsRecall(String username, List<Message> msgList) {
		try {
			ModificationItem[] mods = new ModificationItem[1];
			User user = this.findUserByUsername(username);
			List<Message> queuedMsgs = msgList;
			update(mods, queuedMsgs, user);
			return true;
		} catch (Exception e) {
			logger.log(Level.SEVERE,exceptionMsg,e);
			return false;
		}
	}

	private void update(ModificationItem[] mods, List<Message> queuedMsgs, User user) throws NamingException {
		Attribute mod0 = new BasicAttribute("info", queuedMsgs);
		mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, mod0);
		context.modifyAttributes("uid=" + user.getUsername() + ","+USER_OU, mods);
		logger.log(Level.INFO,successMsg, user.getUsername());
	}

	/**
	 * @param username
	 * @return True if the user is successfully deleted from the database false otherwise
	 * @throws NamingException
	 */
	@Override
	public boolean deleteUserByUsername(String username) throws NamingException {
		context.destroySubcontext("uid=" + username + ",ou=users,dc=example,dc=com");
		List<Group>groups = groupService.findGroupsByUsername(username);
		for(Group group:groups) {
			groupService.deleteFromGroup(group.getGroupName(), new User(username));
		}
		logger.log(Level.INFO,"Success deleting {0}",username);
		return true;
	}

	/**
	 * Encrypts the password
	 * @param algorithm Choice between the algorithm used to encrypt the passsword: SHA or MD5
	 * @param password password in plain text
	 * @return encrypted password
	 */
	public String encryptLdapPassword(String algorithm, String password) {
		String sEncrypted = password;
		if ((password != null) && (password.length() > 0)) {
			boolean bMD5 = algorithm.equalsIgnoreCase("MD5");
			boolean bSHA = algorithm.equalsIgnoreCase("SHA")
					|| algorithm.equalsIgnoreCase("SHA1")
					|| algorithm.equalsIgnoreCase("SHA-1");
			if (bSHA || bMD5) {
				String sAlgorithm = "MD5";
				if (bSHA) {
					sAlgorithm = "SHA";
				}
				try {
					MessageDigest md = MessageDigest.getInstance(sAlgorithm);
					md.update(password.getBytes("UTF-8"));
					sEncrypted = "{" + sAlgorithm + "}" + (Base64.getEncoder().encodeToString(md.digest()));
				} catch (Exception e) {
					sEncrypted = null;
				}
			}
		}
		return sEncrypted;
	}


}
 