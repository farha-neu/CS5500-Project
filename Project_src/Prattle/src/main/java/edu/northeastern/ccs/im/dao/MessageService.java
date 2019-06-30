package edu.northeastern.ccs.im.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;

import edu.northeastern.ccs.im.Message;

/**
 * This class implements methods for saving a message, listing all the messages of
 * a sender and a receiver
 * @author Farha Jawed and Pratik Devikar
 * @version 1.0
 */
public class MessageService implements MessageRepository{

	private DirContext context;
	private static final Logger logger = Logger.getLogger(UserService.class.getName());
	private static final String MESSAGE_OU = "ou=message,dc=example,dc=com";
	private static final String USER_OU = "ou=users,dc=example,dc=com";
	private static final String CONTENT = "description";
	private static final String TYPE = "title";
	private static final String TIMESTAMP = "l";
	private static final String FLAG = "employeeType";
	private boolean isBlocked;
	private UserService us;

	/**
	 * It constructs a group service with initial directory context of LDAP
	 * @throws NamingException
	 */
	public MessageService() throws NamingException {
		this.context = DirectoryUtil.getContext();
	}



	/**
	 * Loads all the quewued messages for a user
	 * @return
	 * @throws NamingException
	 */
	public boolean saveQueuedMessageForAUser() throws NamingException {
		Boolean flag=false;

		List<User> users;
		users = us.findAllUsers();
		for (User usr: users){
			if (usr.isOnline()){
				usr.setBacklogBeingLoaded(true);
				flag=true;
				//Empty all the queued messages
				List<Message> msgListQueue;
				msgListQueue = usr.getQueuedMsgs();
				for (Message msg:msgListQueue){
					saveMessage(msg);
				}
				usr.setBacklogBeingLoaded(false);
			}
		}
		return flag;
	}

	/**
	 * Save a new message
	 * @param message the message content

	 */
	@Override
	public boolean saveMessage(Message message){
		if(context == null) throw new NullPointerException();
		try {
			String sender = message.getName();
			String receiver = message.getMsgReceiver();
			String timeStamp = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date());

			String uid = System.currentTimeMillis()+ sender;
			Attributes attributes = new BasicAttributes();

			Attribute attribute = new BasicAttribute("objectClass");
			attribute.add("inetOrgPerson");
			attributes.put(attribute);

			attributes.put("uid",uid);
			attributes.put("cn",sender);
			if(receiver!=null) {
				attributes.put("sn",receiver);
			}
			if(message.getText()!=null) {
				attributes.put(CONTENT,message.getText());
			}
			if(message.getType()!=null) {
				attributes.put(TYPE,message.getType());
			}		
			if(this.isBlocked) {
				attributes.put(FLAG,"Flagged");
			}
			attributes.put(TIMESTAMP,timeStamp);
			String name = "uid=" + uid + ","+MESSAGE_OU;
			context.createSubcontext(name,attributes);
			logger.log(Level.INFO, "Success");
			return true;
		}
		catch(Exception e) {
			logger.log(Level.WARNING, "Message", e);
			return false;
		}

	}


	/**
	 * It lists all the messages sent by a user
	 * @param sender i.e. the user name
	 * @return list of messages sent by the sender
	 * @throws NamingException
	 */
	@Override
	public List<Message> getMessageBySender(String sender) throws NamingException {
		return getMessages("cn="+sender);
	}

	/**
	 * It lists all the messages received by a user or group
	 * @param receiver i.e. the user name or group name
	 * @return list of messages received by user or group
	 * @throws NamingException
	 */
	@Override
	public List<Message> getMessageByReceiver(String receiver) throws NamingException {
		return getMessages("sn="+receiver);
	}


	/**
	 * It lists all the messages by time stamp
	 * @param timestamp of sent message
	 * @return list of messages sent on that time stamp(format: yyyy.MM.dd HH:mm:ss)
	 * @throws NamingException
	 */
	@Override
	public List<Message> getMessageByTimestamp(String timestamp) throws NamingException {
		return getMessages("l="+timestamp);
	}

	/**
	 *Recalls a message for a sender
	 * @param sender
	 * @param receiver
	 * @param message
	 * @throws NamingException
	 */
	public boolean recallMessage(String sender, String receiver, Message message) throws
			NamingException {
		String uid = sender;
		UserService usr = new UserService();
		String searchFilter = "(&(objectclass=inetOrgPerson)(" + sender + "))";
		String[] requiredAttributes = {"cn", "sn", "uid", CONTENT, TYPE};
		SearchControls controls = new SearchControls();
		controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		controls.setReturningAttributes(requiredAttributes);
		NamingEnumeration<?> messages = context.search(MESSAGE_OU, searchFilter, controls);
		SearchResult searchResult = null;
		while (messages.hasMore()) {
			searchResult = (SearchResult) messages.next();
			Attributes attr = searchResult.getAttributes();
			if (attr.get("uid").get(0).toString().equals(uid)) {
				// Update message attr of user
				ModificationItem[] mods = new ModificationItem[1];
				Attribute mod0 = new BasicAttribute("CONTENT", "Recalled message");
				mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, mod0);
				context.modifyAttributes("uid=" + uid + "," + USER_OU, mods);
				return true;
			}
		}
		if (us.findUserByUsername(receiver).getQueuedMsgs().contains(message)){
			updateUserQueue(usr, receiver, message);
			return true;
		}
		return false;
	}    

	/** Updates the queued messages of a receiver
	 *  @param us
	 * @param receiver
	 * @param message
	 */
	public int updateUserQueue(UserService us, String receiver, Message message) {
		int index=-1;
		List<Message> msgList = new ArrayList<>();
		User usr = us.findUserByUsername(receiver);
		for (int i=0;i<usr.getQueuedMsgs().size();i++){
			msgList.add(usr.getQueuedMsgs().get(i));
		}
		for (int i=0; i<msgList.size(); i++){
			if (msgList.get(i).equals(message)){
				index=i;
			}
		}
		saveMessage(msgList.get(index));
		msgList.remove(index);
		us.updateUserQueuedMsgsRecall(receiver, msgList);
		return index;
	}


	/**
	 * Returns a list of meesages related to a particular username
	 * @param name username of a client
	 * @return List of messages associated with the username
	 * @throws NamingException
	 */
	private List<Message>getMessages(String name) throws NamingException{
		String searchFilter = "(&(objectclass=inetOrgPerson)("+name+"))";
		String[] requiredAttributes = {"cn","sn","uid",CONTENT,TYPE};
		SearchControls controls = new SearchControls();
		controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		controls.setReturningAttributes(requiredAttributes);
		NamingEnumeration<?> messages = context.search(MESSAGE_OU, searchFilter, controls);
		SearchResult searchResult = null;
		List<Message>messageList = new ArrayList<>();
		while(messages.hasMore()) {
			searchResult = (SearchResult) messages.next();
			Attributes attr = searchResult.getAttributes();
			String msgSender = attr.get("cn").get(0).toString();
			String msgReceiver = attr.get("sn").get(0).toString();
			String msgType = attr.get(TYPE).get(0).toString();
			String content = attr.get(CONTENT).toString();

			if(msgReceiver!=null) {
				Message message = Message.makeMessage(msgType,msgSender,msgReceiver, content);
				messageList.add(message);
			}
			else {
				Message message = Message.makeMessage(msgType,msgSender,content);
				messageList.add(message);
			}

		}

		if(messageList.isEmpty()) {
			logger.log(Level.INFO,"No messages found!");
		}
		return messageList;
	}
	
	/**
	 * Sets flag messages with inappropriate content
	 * @param isBlocked
	 */
	public void setBlocked(boolean isBlocked) {
		this.isBlocked = isBlocked;
	}


}
