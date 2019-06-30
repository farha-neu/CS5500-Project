package edu.northeastern.ccs.im;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import edu.northeastern.ccs.im.Message.MessageType;

/**
 *
 * It tests all the methods for Message class
 * @author Farha Jawed
 * @version 1.0
 */
public class TestMessage {

    /**
     * Test for short name of the message sent by the user attempting
     * to login using a specified user name.
     * Expected output: HLO
     */
    @Test
    public void testForEnumHello() {
        assertEquals("HLO",MessageType.HELLO.toString());
    }

    /**
     * Test for short name of message sent by the server
     * acknowledging a successful log in.
     * Expected output: ACK
     */
    @Test
    public void testForEnumAcknowledge() {
        assertEquals("ACK",MessageType.ACKNOWLEDGE.toString());
    }

    /**
     * Test for short name of message sent by the user to start the
     * logging out process and sent by the
     * server once the logout process completes.
     * Expected output: NAK
     */
    @Test
    public void testForEnumNoAcknowledge() {
        assertEquals("NAK",MessageType.NO_ACKNOWLEDGE.toString());
    }

    /**
     * Test for short name of message sent by the user to start
     * the logging out process and sent by the
     * server once the logout process completes.
     * Expected output: BCT
     */
    @Test
    public void testForEnumBye() {
        assertEquals("BYE",MessageType.QUIT.toString());
    }

    /**
     * Test for short name of message whose contents is
     * broadcast to all connected users.
     * Expected output: BCT
     */
    @Test
    public void testForEnumBroadcast() {
        assertEquals("BCT",MessageType.BROADCAST.toString());
    }
    
    /**
     * Test for short name of message whose contents is
     * broadcast to all connected users.
     * Expected output: BCT
     */
    @Test
    public void testForEnumIndividualMessage() {
        assertEquals("INDV",MessageType.INDIVIDUAL_MESSAGE.toString());
    }

    /**
     * Test for sender name when creating a new message
     * to continue the logout process.
     * Expected output: Farha
     */
    @Test
    public void testForLogOutMessageSender() {
        Message message = Message.makeQuitMessage("Farha");
        assertEquals("Farha",message.getName());
    }

    /**
     * Test to determine if this message is a message signing
     * off from the IM server.
     * Expected output: true
     */
    @Test
    public void testForLogOutMessageType() {
        Message message = Message.makeQuitMessage("Farha");
        assertEquals(true,message.terminate());
    }

    /**
     * Test for message text when creating a new message
     * to continue the logout process.
     * Expected output: null
     */
    @Test
    public void testForLogOutMessageText() {
        Message message = Message.makeQuitMessage("Farha");
        assertEquals(null,message.getText());
    }

    /**
     * Test for sender name when creating a new message
     * broadcasting an announcement to the world.
     * Expected output: Farha
     */
    @Test
    public void testForBroadcastMessageSender() {
        Message message = Message.makeBroadcastMessage("Farha","Hello!!");
        assertEquals("Farha",message.getName());
    }

    /**
     * Test to determine if this message is broadcasting text to everyone.
     * Expected output: true
     */
    @Test
    public void testForBroadcastMessageType() {
        Message message = Message.makeBroadcastMessage("Farha","Hello!!");
        assertEquals(true,message.isBroadcastMessage());
    }

    @Test
    public void testForBroadcastMessageTypeDisplay() {
        Message message = Message.makeBroadcastMessage("Farha","Hello!!");
        assertEquals(true,message.isDisplayMessage());
    }

    /**
     * Test for message text when creating a new message
     * broadcasting an announcement to the world.
     * Expected output: Hello!!
     */
    @Test
    public void testForBroadcastMessageText() {
        Message message = Message.makeBroadcastMessage("Farha","Hello!!");
        assertEquals("Hello!!",message.getText());
    }

    /**
     * Test for message sender name when creating a new message
     * stating the name with which the user would like to
     * login.
     * Expected output: null
     */
    @Test
    public void testForHelloMessageSender() {
        Message message = Message.makeHelloMessage("Hello!!");
        assertEquals(null,message.getName());
    }

    /**
     * Test to determine if message is sent by a new client to log-in
     * to the server.
     * Expected output: true
     */
    @Test
    public void testForHelloMessageType() {
        Message message = Message.makeHelloMessage("Hello!!");
        assertEquals(true,message.isInitialization());
    }
    
    /**
     * Test to determine if message is an individual message
     * Expected output: true
     */
    @Test
    public void testForIndividualMessageType() {
        Message message = Message.makeIndividualMessage("Farha", "SomeOne","Hello");
        assertEquals(true,message.isIndividualMessage());
    }
    
    /**
     * Test to determine if message is a group message
     * Expected output: true
     */
    @Test
    public void testForGroupMessageType() {
        Message message = Message.makeGroupMessage("MSD", "Farha", "Hello");
        assertEquals(true,message.isGroupMessage());
    }
    
    

    /**
     * Test for message text when creating a new message.
     * Expected output: Hello!!!
     */
    @Test
    public void testForHelloMessageText() {
        Message message = Message.makeHelloMessage("Hello!!");
        assertEquals("Hello!!",message.getText());
    }

    /**
     * Test for message sender name when creating
     * a new message to reject the bad login attempt.
     * Expected output: null
     */
    @Test
    public void testForNoAcknowledgeMessageSender() {
        Message message = Message.makeNoAcknowledgeMessage();
        assertEquals(null,message.getName());
    }

    /**
     * Test for message type when creating
     * a new message to reject the bad login attempt.
     * Expected output: message broadcasting, initialization
     * termination will be false
     */
    @Test
    public void testForNoAcknowledgeMessageType() {
        Message message = Message.makeNoAcknowledgeMessage();
        assertEquals(false,message.isInitialization());
        assertEquals(false,message.terminate());
        assertEquals(false,message.isBroadcastMessage());
        assertEquals(false,message.isAcknowledge());
        assertEquals(false,message.isDisplayMessage());
        assertEquals(false,message.isIndividualMessage());
        assertEquals(false,message.isGroupMessage());
    }

    /**
     * Test for message text when creating
     * a new message to reject the bad login attempt.
     * Expected output: null
     */
    @Test
    public void testForNoAcknowledgeMessageText() {
        Message message = Message.makeNoAcknowledgeMessage();
        assertEquals(null,message.getText());
    }

    /**
     * Test for message sender name when creating
     * a new message to acknowledge that the user successfully logged in.
     * Expected output: Farha
     */
    @Test
    public void testForAcknowledgeMessageSender() {
        Message message = Message.makeAcknowledgeMessage("Farha");
        assertEquals("Farha",message.getName());
    }

    /**
     * Test to determine if this message is an acknowledgement message.
     * Expected output: true
     */
    @Test
    public void testForAcknowledgeMessageType() {
        Message message = Message.makeAcknowledgeMessage("Farha");
        assertEquals(true,message.isAcknowledge());
    }

    /**
     * Test for message text when creating
     * a new message to acknowledge that the user successfully logged in.
     * Expected output: null
     */
    @Test
    public void testForAcknowledgeMessageText() {
        Message message = Message.makeAcknowledgeMessage("Farha");
        assertEquals(null,message.getText());
    }

    /**
     * Test for message sender name when creating a new message for the
     * early stages when the user logs in without all
     * the special stuff.
     * Expected output: Farha
     */
    @Test
    public void testForSimpleLoginMessageSender() {
        Message message = Message.makeSimpleLoginMessage("Farha");
        assertEquals("Farha",message.getName());
    }

    /**
     * Test to determine if message is sent by a new client to log-in
     * to the server.
     * Expected output: true
     */
    @Test
    public void testForSimpleLoginMessageType() {
        Message message = Message.makeSimpleLoginMessage("Farha");
        assertEquals(true,message.isInitialization());
    }

    /**
     * Test for message text when creating a new message for the
     * early stages when the user logs in without all
     * the special stuff.
     * Expected output: null
     */
    @Test
    public void testForSimpleLoginMessageText() {
        Message message = Message.makeSimpleLoginMessage("Farha");
        assertEquals(null,message.getText());
    }

    /**
     * Tests the message representation to continue the logout
     * process.
     */
    @Test
    public void testForMessageHandlerForQuitMessage() {
        Message message = Message.makeMessage("BYE","Farha","Hello!");
        String expectedOutput = "BYE " + 5 + " Farha "+ 2 + " --";
        assertEquals(expectedOutput,message.toString());
    }

    /**
     * Tests the message representation during log in.
     */
    @Test
    public void testForMessageHandlerForSimpleLoginMessage() {
        Message message = Message.makeMessage("HLO","Farha","Hello!");
        String expectedOutput = "HLO " + 5 + " Farha "+ 2 + " --";
        assertEquals(expectedOutput,message.toString());
    }

    /**
     * Test for message representation while broadcasting message
     * to the world
     */
    @Test
    public void testForMessageHandlerForBroadcastMessage() {
        Message message = Message.makeMessage("BCT","Farha","Hello!");
        String expectedOutput = "BCT " + 5 + " Farha "+ 6 + " Hello!";
        assertEquals(expectedOutput,message.toString());
    }

    /**
     * Test for message representation to acknowledge that the user
     * successfully logged
     */
    @Test
    public void testForMessageHandlerForAcknowledgeMessage() {
        Message message = Message.makeMessage("ACK","Farha",null);
        String expectedOutput = "ACK " + 5 + " Farha "+ 2 + " --";
        assertEquals(expectedOutput,message.toString());
    }

    /**
     * Test for message representation to reject the bad login attempt.
     */
    @Test
    public void testForMessageHandlerForNoAcknowledgeMessage() {
        Message message = Message.makeMessage("NAK",null,null);
        String expectedOutput = "NAK " + 2 + " -- "+ 2 + " --";
        assertEquals(expectedOutput,message.toString());
    }
    
  
    /**
     * Test for message representation when message type is not
     * valid i.e. other than ACK,NAK,BCT,HLO,BYE
     * Expected output: null
     */
    @Test
    public void testForMessageHandlerForInvalidMessageType() {
        Message message = Message.makeMessage("KAK",null,null);
        assertEquals(message,null);
    }
    
    /**
     * Test for message group message receiver and type
     */
    @Test
    public void testForGroupMessage() {
        Message message = Message.makeMessage("GRM","Farha", "MSD", "Hi");
        assertEquals("MSD",message.getMsgReceiver());
        assertEquals("GRM",message.getType());
    }
    
    /**
     * Test for message individual message receiver and type
     */
    @Test
    public void testForIndividualMessage() {
        Message message = Message.makeMessage("INDV","Farha", "SomeOne", "Hi");
        assertEquals("SomeOne",message.getMsgReceiver());
        assertEquals("INDV",message.getType());
    }
    
    /**
     * Test for message group message as String
     */
    @Test
    public void testForGroupMessageHandler() {
        Message message = Message.makeMessage("GRM","Farha", "MSD", "Hi");
        String expectedOutput = "GRM " + 5 + " Farha "+ 3 + " MSD "+2+" Hi";
        assertEquals(expectedOutput,message.toString());
    }
    
    /**
     * Test for message receiver
     */
    @Test
    public void testForMsgReceiver() {
    	  Message message = Message.makeQuitMessage("Farha");
    	  assertEquals("NO one", message.getMsgReceiver());
    }
    
    /**
     * Test for message group message as String
     */
    @Test
    public void testForMakeGroupMsg() {
        assertEquals(null,Message.makeMessage("GR","Farha", "MSD", "Hi"));
    }
  
}
