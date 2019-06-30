package edu.northeastern.ccs.im.server;


import java.io.IOException;

import java.net.InetSocketAddress;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.northeastern.ccs.im.*;
import edu.northeastern.ccs.im.dao.*;
import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackMessage;

import javax.naming.NamingException;

/**
 * A network server that communicates with IM clients that connect to it. This
 * version of the server spawns a new thread to handle each client that connects
 * to it. At this point, messages are broadcast to all of the other clients.
 * It does not send a response when the user has gone off-line.
 * <p>
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 *
 * @version 1.3
 */
public abstract class Prattle {

    /**
     * Amount of time we should wait for a signal to arrive.
     */
    private static final int DELAY_IN_MS = 50;

    /**
     * Number of threads available in our thread pool.
     */
    private static final int THREAD_POOL_SIZE = 20;

    /**
     * Delay between times the thread pool runs the client check.
     */
    private static final int CLIENT_CHECK_DELAY = 200;

    /**
     * Collection of threads that are currently being used.
     */
    private static ConcurrentLinkedQueue<ClientRunnable> active;

    private static ServerSocketChannel serverSocket;

    private static final Logger logger = Logger.getLogger(ScanNetNB.class.getName());

    private static ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);

    private static Map<String, Group> groupMap;

    private static Map<String, ClientRunnable> userMap;

    private static Map<String, ClientRunnable> userAgentMap;

    private static Map<String, ClientRunnable> groupAgentMap;

    private static ParentalControl parentalControl;

    private static String msg = "User: {0} is not in the system.";
    
    private static boolean isBlocked = false;


    // Link to our slack channel
    private static final SlackApi api = new SlackApi("https://hooks.slack" +
            ".com/services/T2CR59JN7/BEE922DFU/duUie9jYv4Hih09FyeeXCgIp");

    /** All of the static initialization occurs in this "method" */
    static {
        // Create the new queue of active threads.
        active = new ConcurrentLinkedQueue<>();
        groupMap = new HashMap<>();
        userMap = new HashMap<>();
        userAgentMap = new HashMap<>();
        groupAgentMap = new HashMap<>();
    }

    /**
     * Broadcast a given message to all the other IM clients currently on the
     * system. This message _will_ be sent to the client who originally sent it.
     *
     * @param message Message that the client sent.
     */
    public static void broadcastMessage(Message message) {
        // Loop through all of our active threads
        for (ClientRunnable tt : active) {
            // Do not send the message to any clients that are not ready to receive it.
            if (tt.isInitialized()) {
                tt.enqueueMessage(message);
            }
        }
    }


    /**
     * Start up the threaded talk server. This class accepts incoming connections on
     * a specific port specified on the command-line. Whenever it receives a new
     * connection, it will spawn a thread to perform all of the I/O with that
     * client. This class relies on the server not receiving too many requests -- it
     * does not include any code to limit the number of extant threads.
     *
     * @param args String arguments to the server from the command line. At present
     *             the only legal (and required) argument is the port on which this
     *             server should list.
     * @throws IOException Exception thrown if the server cannot connect to the port
     *                     to which it is supposed to listen.
     */
    public static void main(String[] args) throws IOException {
        // Connect to the socket on the appropriate port to which this server connects.
        try {

            serverSocket = ServerSocketChannel.open();
            serverSocket.configureBlocking(false);
            serverSocket.socket().bind(new InetSocketAddress(ServerConstants.PORT));
            // Create the Selector with which our channel is registered.
            Selector selector = SelectorProvider.provider().openSelector();
            // Register to receive any incoming connection messages.
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
            // Create our pool of threads on which we will execute.

            // Listen on this port until ...
            boolean done = false;
            while (!done) {
                // Check if we have a valid incoming request, but limit the time we may wait.

                while (selector.select(DELAY_IN_MS) != 0) {
                    // Get the list of keys that have arrived since our last check
                    Set<SelectionKey> acceptKeys = selector.selectedKeys();
                    // Now iterate through all of the keys
                    Iterator<SelectionKey> it = acceptKeys.iterator();
                    while (it.hasNext()) {
                        // Get the next key; it had better be from a new incoming connection
                        SelectionKey key = it.next();
                        it.remove();
                        // Assert certain things I really hope is true
                        assert key.isAcceptable();
                        assert key.channel() == serverSocket;
                        // Create a new thread to handle the client for which we just received a
                        // request.
                        try {
                            // Accept the connection and create a new thread to handle this client.
                            SocketChannel socket = serverSocket.accept();
                            // Make sure we have a connection to work with.
                            addActive(socket);
                        } catch (AssertionError ae) {
                            logger.info("Caught Assertion: " + ae.toString());
                        } catch (Exception e) {
                            logger.info("Caught Exception: " + e.toString());
                        }

                    }
                }
            }
        } catch (Exception e) {
            logger.info(e.getMessage());
        } finally {
            stopPort();
        }


    }


    public static Queue<ClientRunnable> getActive() {
        return active;
    }

    /**
     * Remove the given IM client from the list of active threads.
     *
     * @param dead Thread which had been handling all the I/O for a client who has
     *             since quit.
     */
    public static void removeClient(ClientRunnable dead) {
        // Test and see if the thread was in our list of active clients so that we
        // can remove it.
        if (!active.remove(dead)) {
            logger.info("Could not find a thread that I tried to remove!\n");
        }
    }

    /**
     * Close the port after we're done
     *
     * @throws IOException
     */
    public static void stopPort() throws IOException {
        serverSocket.close();
    }

    /**
     * Check if the socket is running, return true if is open.
     *
     * @return
     */
    public static boolean isRunning() {
        return serverSocket.isOpen();
    }

    /**
     * Decouple the main method by adding a helper function here
     * Added a new active runnable given socket
     *
     * @param socket
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static void addActive(SocketChannel socket) throws IOException {
        if (socket != null) {
            ClientRunnable tt = new ClientRunnable(socket);
            // Add the thread to the queue of active threads
            active.add(tt);
            // Have the client executed by our pool of threads.
            @SuppressWarnings("rawtypes")
            ScheduledFuture clientFuture = threadPool.scheduleAtFixedRate(tt, CLIENT_CHECK_DELAY,
                    CLIENT_CHECK_DELAY, TimeUnit.MILLISECONDS);
            tt.setFuture(clientFuture);
        }
    }

    /**
     * Overload the previous method by initialized the newly added runnable and give it a userName
     *
     * @param socket
     * @param userName
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static void addActive(SocketChannel socket, String userName) throws IOException {
        if (socket != null) {
            ClientRunnable tt = new ClientRunnable(socket, userName);
            // Add the thread to the queue of active threads
            active.add(tt);
            userMap.put(userName, tt);
            // Have the client executed by our pool of threads.
            @SuppressWarnings("rawtypes")
            ScheduledFuture clientFuture = threadPool.scheduleAtFixedRate(tt, CLIENT_CHECK_DELAY,
                    CLIENT_CHECK_DELAY, TimeUnit.MILLISECONDS);
            tt.setFuture(clientFuture);
        }
    }

    /**
     * Send a Message to all the group members, persist the message after done.
     *
     * @param message the message we want to send
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws NoSuchElementException
     * @throws NamingException
     */
    public static void sendToGroup(Message message) throws IOException, NamingException{
        parentalControl = new ParentalControl();
        String groupName = message.getMsgReceiver();
        if (!message.isGroupMessage() || !groupMap.containsKey(groupName)) {
            throw new IllegalArgumentException("Any message that sends to a group should be a group Message.");
        }else {

            if (groupAgentMap.containsKey(groupName)) {
                groupAgentMap.get(groupName).enqueueMessage(addIPToMessage(message));
            }
            Group group = groupMap.get(groupName);

            List<User> users = group.getUsers();
            for (User user : users) {
                if (userMap.containsKey(user.getUsername())) {
                    if(userMap.get(user.getUsername()) == null){
                        enqueOffLineMessage(user.getUsername(), message);
                    }
                    else {
                        userMap.get(user.getUsername()).enqueueMessage(message);
                    }
                } else {
                    logger.log(Level.INFO, "User: {0} is not in the system.", user.getUsername());
                }
            }
            parentalControlCheck(message.getText()); 
            persistMessage(message);
        }
    }

    /**
     * Send an Individual message given message sender, receiver and text. Persist the message after done.
     *
     * @param sender
     * @param receiver
     * @param text
     * @throws IOException
     */
    public static void sendIndividualMessage(String sender, String receiver, String text) throws IOException, NamingException {
        parentalControl = new ParentalControl();
        Message individualMessage = Message.makeIndividualMessage(sender, receiver, text);
        if (userMap.containsKey(sender) && userMap.containsKey(receiver)) {
                if(userMap.get(receiver) == null){
                    enqueOffLineMessage(receiver, individualMessage);
                }
                else {
                    userMap.get(receiver).enqueueMessage(individualMessage);
                }
                // Check if the sender/ receiver is in our list of inspecting
                if (userAgentMap.containsKey(sender)) {
                    userAgentMap.get(sender).enqueueMessage(addIPToMessage(individualMessage));
                }
                if (userAgentMap.containsKey(receiver)) {
                    userAgentMap.get(receiver).enqueueMessage(addIPToMessage(individualMessage));
                }
                
                parentalControlCheck(text);             
                persistMessage(individualMessage);    
            
        } else {
            api.call(new SlackMessage("Client is trying to send a message to a non existing user " + receiver));
            logger.log(Level.INFO, msg, receiver);
        }


    }


    /**
     * Checks words for parental control and sets isBlocked flag to true
     * if message contains vulgar words
     * @param text
     */
	private static void parentalControlCheck(String text) {
		if (parentalControl.hasBadWord(text)) {
		    logger.warning("Message blocked due to inappropriate content");
		    isBlocked = true;
		}
	}

    public static void enqueOffLineMessage(String userName, Message message) throws NamingException {
        try {
            UserService us = new UserService();
            if (!us.findUserByUsername(userName).isBacklogBeingLoaded()) {
                userMap.get(userName).enqueueMessage(message);
            } else {
                us.updateUserQueuedMsgs(userName, message);
            }
        } catch (Exception e) {
            logger.info(e.getMessage());
        }

    }


    /**
     * Save the message in LDAP
     *
     * @param message
     */
    public static void persistMessage(Message message) {
        try {
            MessageService service = new MessageService();
            service.setBlocked(isBlocked);
            service.saveMessage(message);
        } catch (Exception e) {
            logger.warning("Not able to save message.");
            logger.warning(e.getMessage());
        }

    }

    /**
     * Added a new Group and put it in the GroupMap
     *
     * @param group
     */
    protected static void addGrouptoMap(Group group) {
        groupMap.put(group.getGroupName(), group);
    }

    protected static void addUserAgent(String userName, ClientRunnable agentChannel) {
        if (!userMap.containsKey(userName)) {
            logger.info("Specified user is not in the system.");
            api.call(new SlackMessage("Severity: Informational"
            		+ "\nFailed: Specified user not in the system\nSynopsis of failure: User not in system"));
        } else {
            userAgentMap.put(userName, agentChannel);
        }
    }

    protected static void addGroupAgent(String groupName, ClientRunnable agentChannel) {
        if (!groupMap.containsKey(groupName)) {
            logger.info("Specified group is not in the system.");
        } else {
            groupAgentMap.put(groupName, agentChannel);
        }
    }

    protected static Message addIPToMessage(Message message) {

        if (message.isIndividualMessage()) {
            ClientRunnable senderChannel = userMap.get(message.getName());
            ClientRunnable receiverChannel = userMap.get(message.getMsgReceiver());

            String senderIP = senderChannel.getRemoteAddress();
            String receiverIP = receiverChannel.getRemoteAddress();

            String wrapedText = message.getText() + "SenderIP: " + senderIP + " ReceiverIP" + receiverIP;

            return Message.makeIndividualMessage(message.getName(), message.getMsgReceiver(), wrapedText);
        } else if (message.isGroupMessage()) {
            ClientRunnable senderChannel = userMap.get(message.getName());

            String senderIP = senderChannel.getRemoteAddress();
            String wrapedText = message.getText() + "SenderIP: " + senderIP;

            return Message.makeIndividualMessage(message.getName(), message.getMsgReceiver(), wrapedText);
        } else return null;
    }

    protected static AgentRunnable createNewAgent(SocketChannel socketChannel, String agentName, long wireTapDuration) throws IOException {
        AgentRunnable agentRunnable = new AgentRunnable(socketChannel, agentName);
        agentRunnable.setValidTime(wireTapDuration);
        return agentRunnable;
    }

}
