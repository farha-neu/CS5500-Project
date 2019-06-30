package edu.northeastern.ccs.im.dao;

import edu.northeastern.ccs.im.Message;

import java.io.IOException;
import java.util.List;

import javax.naming.NamingException;

/**
 * This interface provides methods for saving a message, listing all the messages of
 * a sender and a receiver
 * @author Farha Jawed
 * @version 1.0
 */
public interface MessageRepository {

    /**
     * Saves a new message
     * @param message the message content
     * @throws IOException 
     */
    boolean saveMessage(Message message);

    /**
     * It lists all the messages sent by a user
     * @param sender i.e. the user name
     * @return list of messages sent by the sender
     * @throws NamingException
     */
    List<Message> getMessageBySender(String sender) throws NamingException;

    /**
     * It lists all the messages received by a user or group
     * @param receiver i.e. the user name or group name
     * @return list of messages received by user or group
     * @throws NamingException
     */
    List<Message> getMessageByReceiver(String receiver) throws NamingException;

    /**
     * It lists all the messages by timestamp
     * @param timestamp of sent message
     * @return list of messages sent on that timestamp(format: yyyy.MM.dd HH:mm:ss)
     * @throws NamingException
     */
	List<Message> getMessageByTimestamp(String timestamp) throws NamingException;


}