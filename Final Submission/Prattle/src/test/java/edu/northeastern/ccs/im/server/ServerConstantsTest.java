package edu.northeastern.ccs.im.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import edu.northeastern.ccs.im.Message;

/**
 * It tests the public functions of ServerConstants class
 * @author farha
 * @version 1.0
 *
 */
public class ServerConstantsTest {
  
	
	private static Message message=ServerConstants.REJECT_USER_MESSAGE;

	/**
	 * It tests the message type of a message sent in the early assignments 
	 * when the user tries to send a message
	 * before they login.
	 * Expected output: true as it is a broadcast message
	 */
	@Test
	public void testRejectUserMessageType() {	
		assertEquals(true,message.isBroadcastMessage());
	}
	
	/**
	 * It tests the username of a message sent in the early assignments 
	 * when the user tries to send a message
	 * before they login.
	 * Expected output: Bouncer
	 */
	@Test
	public void testRejectUserMessageName() {
		assertEquals("Bouncer",message.getName());
	}
	
	/**
	 * It tests the text of a message sent in the early assignments 
	 * when the user tries to send a message
	 * before they login.
	 */
	@Test
	public void testRejectUserMessageText() {
		assertEquals("You must login before you can send a message",message.getText());
	}
	
	/** 
	 * Tests the user name of message used to find a date.
	 */
	@Test
	public void testDateCommandUsername() {
		List<Message>listOfMessages = ServerConstants.getBroadcastResponses("What is the date?");
		assertEquals("NIST",listOfMessages.get(0).getName());
	}
	
	/** 
	 * Tests the user name of message used to find a time.
	 */
	@Test
	public void testTimeCommandUsername() {
		List<Message>listOfMessages = ServerConstants.getBroadcastResponses("What time is it?");
		assertEquals("NIST",listOfMessages.get(0).getName());
	}
	
	/** 
	 * Tests the user name of message for impatient users.
	 */
	@Test
	public void testImpatientCommand() {
		List<Message>listOfMessages = ServerConstants.getBroadcastResponses("What time is it Mr. Fox?");
		assertEquals("BBC",listOfMessages.get(0).getName());
	}
	
	/**
	 * Tests the message text of Hello command
	 */
	@Test 
	public void testHelloCommand() {
		List<Message>listOfMessages = ServerConstants.getBroadcastResponses("Hello");
		assertEquals("Hello.  How are you?",listOfMessages.get(0).getText());
		assertEquals("I can communicate with you to test your code.",listOfMessages.get(1).getText());
	}
	
	/**
	 * Tests the username of cool command.
	 */
	@Test
	public void testCommandCoolUsername() {
		List<Message>listOfMessages = ServerConstants.getBroadcastResponses("WTF");
		assertEquals("Prattle",listOfMessages.get(0).getName());
	}
	
	/**
	 * Tests list of messages when message text doesn't match with text for 
	 * defined commands: COOL, IMPATIET,TIME,DATE,QUERY,HELLO
	 */
	@Test
	public void testCommandResultNull() {
		List<Message>listOfMessages = ServerConstants.getBroadcastResponses("grhj");
		assertEquals(null,listOfMessages);
	}
	
	/**
	 * Tests the port number to listen on
	 */
	@Test
	public void testForGettingPort() {
		assertEquals(ServerConstants.PORT,ServerConstants.getPORT());
	} 

  
}
