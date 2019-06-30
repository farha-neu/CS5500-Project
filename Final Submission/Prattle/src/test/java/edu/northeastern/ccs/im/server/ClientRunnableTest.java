package edu.northeastern.ccs.im.server;

import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.ScanNetNB;
import edu.northeastern.ccs.im.SocketNB;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class test the public functions for ClientRunnable
 *
 * @author Yang Xia
 * 10/31/2018
 */
public class ClientRunnableTest {
    private static final Logger logger = Logger.getLogger(ClientRunnable.class.getName());
    ClientRunnable clientRunnable = null;
    ServerSocketChannel serverSocket = null;
    SocketChannel channel;

    @BeforeEach
    public void openPort(){
        try {
            serverSocket = ServerSocketChannel.open();
            serverSocket.configureBlocking(false);
            serverSocket.socket().bind(new InetSocketAddress(ServerConstants.PORT));
            // Create the Selector with which our channel is registered.
            Selector selector = SelectorProvider.provider().openSelector();
            // Register to receive any incoming connection messages.
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        } catch (IOException e) {
            System.out.println(e.getMessage());
            logger.warning(e.getMessage());
        }
    }

    @AfterEach
    public void closePort() throws IOException {
        serverSocket.close();
    }

    /**
     * Construct a new ClientRunnable for testing purpose
     */
    @BeforeEach
    public void setUp() {
        try {
            // Open a new channel
            channel = SocketChannel.open();
            // Make this channel a non-blocking channel
            channel.configureBlocking(false);
            // Connect the channel to the remote port
            channel.connect(new InetSocketAddress("127.0.0.1", ServerConstants.PORT));
            //to avoid channel not ready exception
            if(channel.finishConnect()){
            	 clientRunnable = new ClientRunnable(channel);
            }


        } catch (IOException e) {
            System.out.println(e.getMessage());
            logger.warning(e.getMessage());
        }
    }

    /**
     * Test enqueueMessage method using reflection to access the list
     *
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IOException
     */
    @Test
    void enqueueMessage() throws NoSuchFieldException, IllegalAccessException, IOException {
        final Field field = clientRunnable.getClass().getDeclaredField("waitingList");

        field.setAccessible(true);
        Queue<Message> messageList = new LinkedList<>();
        field.set(clientRunnable, messageList);

        assertEquals(0, messageList.size());
        Message message = Message.makeBroadcastMessage("Farha","Hello!!");
        clientRunnable.enqueueMessage(message);
        assertEquals(1, messageList.size());

        assertEquals(message, messageList.peek());
        channel.close();
    }

    /**
     * Test if getName returns the correct Name
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    @Test
    void getName() throws NoSuchFieldException, IllegalAccessException{
        final Field field = clientRunnable.getClass().getDeclaredField("name");

        field.setAccessible(true);
        String userName = "SomeName";
        field.set(clientRunnable, userName);
        assertEquals("SomeName", clientRunnable.getName());
    }

    /**
     * Test if setName set userName correctly
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    @Test
    void setName() throws NoSuchFieldException, IllegalAccessException{
        final Field field = clientRunnable.getClass().getDeclaredField("name");

        clientRunnable.setName("SomeName");
        field.setAccessible(true);
        String userName = (String) field.get(clientRunnable);
        assertEquals("SomeName", userName);
    }

    /**
     * Test if getUserID returns the correct number
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    @Test
    void getUserId() throws NoSuchFieldException, IllegalAccessException{
        final Field field = clientRunnable.getClass().getDeclaredField("userId");

        field.setAccessible(true);
        int userId = 1;
        field.set(clientRunnable, userId);
        assertEquals(userId, clientRunnable.getUserId());
    }

    /**
     * Test if isInitialized returns the correct boolean
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    @Test
    void isInitialized() throws NoSuchFieldException, IllegalAccessException{
        final Field field = clientRunnable.getClass().getDeclaredField("initialized");

        field.setAccessible(true);
        boolean initialized = true;
        field.set(clientRunnable, initialized);
        assertEquals(initialized, clientRunnable.isInitialized());

        field.setAccessible(true);
        initialized = false;
        field.set(clientRunnable, initialized);
        assertEquals(initialized, clientRunnable.isInitialized());
    }

    @Test
    void run() throws IOException, NoSuchFieldException, IllegalAccessException{
        ScanNetNB scanNetNB = new ScanNetNB(channel);

        final Field scanNetNBMessages = scanNetNB.getClass().getDeclaredField("messages");

        scanNetNBMessages.setAccessible(true);
        Queue<Message> messageList = new LinkedList<>();
        scanNetNBMessages.set(scanNetNB, messageList);
        Message message = Message.makeBroadcastMessage("Farha","Hello!!");
        messageList.offer(message);

        final Field clientRunnableInput = clientRunnable.getClass().getDeclaredField("input");
        clientRunnableInput.setAccessible(true);
        clientRunnableInput.set(clientRunnable, scanNetNB);

        final Field clientRunnableInitialized = clientRunnable.getClass().getDeclaredField("initialized");
        clientRunnableInitialized.setAccessible(true);
        boolean isInitialized = (boolean) clientRunnableInitialized.get(clientRunnable);

        assertFalse(isInitialized);
        clientRunnable.run();

        isInitialized = (boolean) clientRunnableInitialized.get(clientRunnable);
        assertTrue(isInitialized);
        scanNetNB.close();
    }

    @Test
    void runInitialized() throws IOException, NoSuchFieldException, IllegalAccessException{
        ScanNetNB scanNetNB = new ScanNetNB(channel);

        final Field scanNetNBMessages = scanNetNB.getClass().getDeclaredField("messages");

        scanNetNBMessages.setAccessible(true);
        Queue<Message> messageList = new LinkedList<>();
        scanNetNBMessages.set(scanNetNB, messageList);
        Message broadcastMessage = Message.makeBroadcastMessage("Farha","Hello");
        messageList.offer(broadcastMessage);

        final Field clientRunnableInput = clientRunnable.getClass().getDeclaredField("input");
        clientRunnableInput.setAccessible(true);
        clientRunnableInput.set(clientRunnable, scanNetNB);

        final Field clientRunnableInitialized = clientRunnable.getClass().getDeclaredField("initialized");
        clientRunnableInitialized.setAccessible(true);
        boolean isInitialized = true;
        clientRunnableInitialized.set(clientRunnable, isInitialized);

        final Field clientRunnableName = clientRunnable.getClass().getDeclaredField("name");
        clientRunnableName.setAccessible(true);
        clientRunnableName.set(clientRunnable, "Farha");


        clientRunnable.run();

        isInitialized = (boolean) clientRunnableInitialized.get(clientRunnable);
        assertTrue(isInitialized);
    }

    @Test
    void runInitializedNoUsername() throws IOException, NoSuchFieldException, IllegalAccessException{
        ScanNetNB scanNetNB = new ScanNetNB(channel);

        final Field scanNetNBMessages = scanNetNB.getClass().getDeclaredField("messages");

        scanNetNBMessages.setAccessible(true);
        Queue<Message> messageList = new LinkedList<>();
        scanNetNBMessages.set(scanNetNB, messageList);
        Message broadcastMessage = Message.makeBroadcastMessage(null,"Hello");
        messageList.offer(broadcastMessage);

        final Field clientRunnableInput = clientRunnable.getClass().getDeclaredField("input");
        clientRunnableInput.setAccessible(true);
        clientRunnableInput.set(clientRunnable, scanNetNB);

        final Field clientRunnableInitialized = clientRunnable.getClass().getDeclaredField("initialized");
        clientRunnableInitialized.setAccessible(true);
        boolean isInitialized = true;
        clientRunnableInitialized.set(clientRunnable, isInitialized);

        final Field clientRunnableName = clientRunnable.getClass().getDeclaredField("name");
        clientRunnableName.setAccessible(true);
        clientRunnableName.set(clientRunnable, "Farha");


        clientRunnable.run();

        isInitialized = (boolean) clientRunnableInitialized.get(clientRunnable);
        assertTrue(isInitialized);
    }

    @Test
    void runNonspecialMessage() throws IOException, NoSuchFieldException, IllegalAccessException{
        ScanNetNB scanNetNB = new ScanNetNB(channel);

        final Field scanNetNBMessages = scanNetNB.getClass().getDeclaredField("messages");

        scanNetNBMessages.setAccessible(true);
        Queue<Message> messageList = new LinkedList<>();
        scanNetNBMessages.set(scanNetNB, messageList);
        Message message = Message.makeBroadcastMessage("Farha",ServerConstants.BOMB_TEXT);
        messageList.offer(message);

        final Field clientRunnableInput = clientRunnable.getClass().getDeclaredField("input");
        clientRunnableInput.setAccessible(true);
        clientRunnableInput.set(clientRunnable, scanNetNB);

        final Field clientRunnableInitialized = clientRunnable.getClass().getDeclaredField("initialized");
        clientRunnableInitialized.setAccessible(true);
        boolean isInitialized = true;
        clientRunnableInitialized.set(clientRunnable, isInitialized);

        final Field clientRunnableName = clientRunnable.getClass().getDeclaredField("name");
        clientRunnableName.setAccessible(true);
        clientRunnableName.set(clientRunnable, "Farha");


        clientRunnable.run();

        isInitialized = (boolean) clientRunnableInitialized.get(clientRunnable);
        assertFalse(isInitialized);
    }

    @Test
    void runNonspecialMessage2() throws IOException, NoSuchFieldException, IllegalAccessException{
        ScanNetNB scanNetNB = new ScanNetNB(channel);

        final Field scanNetNBMessages = scanNetNB.getClass().getDeclaredField("messages");

        scanNetNBMessages.setAccessible(true);
        Queue<Message> messageList = new LinkedList<>();
        scanNetNBMessages.set(scanNetNB, messageList);
        Message message = Message.makeBroadcastMessage("Farha",null);
        messageList.offer(message);

        final Field clientRunnableInput = clientRunnable.getClass().getDeclaredField("input");
        clientRunnableInput.setAccessible(true);
        clientRunnableInput.set(clientRunnable, scanNetNB);

        final Field clientRunnableInitialized = clientRunnable.getClass().getDeclaredField("initialized");
        clientRunnableInitialized.setAccessible(true);
        boolean isInitialized = true;
        clientRunnableInitialized.set(clientRunnable, isInitialized);

        final Field clientRunnableName = clientRunnable.getClass().getDeclaredField("name");
        clientRunnableName.setAccessible(true);
        clientRunnableName.set(clientRunnable, "Farha");


        clientRunnable.run();

        isInitialized = (boolean) clientRunnableInitialized.get(clientRunnable);
        assertTrue(isInitialized);
    }

    @Test
    void setFuture() throws NoSuchFieldException, IllegalAccessException {
        ScheduledFuture<ClientRunnable> sf;
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        sf = executor.schedule(new Callable() {
                                   public Object call() throws Exception {
                                       System.out.println("Executed!");
                                       return "Called!";
                                   }
                               },
                5,
                TimeUnit.SECONDS);
        clientRunnable.setFuture(sf);
        assertNotEquals(null,clientRunnable.getRunnableMe());
    }

    @Test
    void terminateClient() {
        ScheduledFuture<ClientRunnable> sf;
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        sf = executor.schedule(new Callable() {
                                   public Object call() throws Exception {
                                       System.out.println("Executed!");
                                       return "Called!";
                                   }
                               },
                5,
                TimeUnit.SECONDS);
        clientRunnable.setFuture(sf);
        clientRunnable.terminateClient();
        assertFalse(clientRunnable.isInitialized());
    }

    @Test
    void broadcastMessageFromUser() throws IllegalAccessException, NoSuchFieldException {
        ScanNetNB scanNetNB = new ScanNetNB(channel);

        final Field scanNetNBMessages = scanNetNB.getClass().getDeclaredField("messages");

        scanNetNBMessages.setAccessible(true);
        Queue<Message> messageList = new LinkedList<>();
        scanNetNBMessages.set(scanNetNB, messageList);
        Message message = Message.makeQuitMessage("Farha");
        messageList.offer(message);

        final Field clientRunnableInput = clientRunnable.getClass().getDeclaredField("input");
        clientRunnableInput.setAccessible(true);
        clientRunnableInput.set(clientRunnable, scanNetNB);

        final Field clientRunnableInitialized = clientRunnable.getClass().getDeclaredField("initialized");
        clientRunnableInitialized.setAccessible(true);
        boolean isInitialized = true;
        clientRunnableInitialized.set(clientRunnable, isInitialized);

        final Field clientRunnableName = clientRunnable.getClass().getDeclaredField("name");
        clientRunnableName.setAccessible(true);
        clientRunnableName.set(clientRunnable, "Farha");


        clientRunnable.broadcastMessageFromUser();
        assertTrue(isInitialized);

    }

    /**
     * Not yet connection exception??
     * @throws IOException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    @Test
    void runForNotLegalMessage() throws IOException, NoSuchFieldException, IllegalAccessException{
        ScanNetNB scanNetNB = new ScanNetNB(channel);

        final Field scanNetNBMessages = scanNetNB.getClass().getDeclaredField("messages");

        scanNetNBMessages.setAccessible(true);
        Queue<Message> messageList = new LinkedList<>();
        scanNetNBMessages.set(scanNetNB, messageList);
        Message message = Message.makeBroadcastMessage("Farha",null);
        messageList.offer(message);

        final Field clientRunnableInput = clientRunnable.getClass().getDeclaredField("input");
        clientRunnableInput.setAccessible(true);
        clientRunnableInput.set(clientRunnable, scanNetNB);

        final Field clientRunnableInitialized = clientRunnable.getClass().getDeclaredField("initialized");
        clientRunnableInitialized.setAccessible(true);
        boolean isInitialized = true;
        clientRunnableInitialized.set(clientRunnable, isInitialized);

        final Field clientRunnableName = clientRunnable.getClass().getDeclaredField("name");
        clientRunnableName.setAccessible(true);
        clientRunnableName.set(clientRunnable, "Jawed");

        System.out.println(clientRunnable.toString());
        clientRunnable.run();
        final Field waitingList = clientRunnable.getClass().getDeclaredField("waitingList");
        waitingList.setAccessible(true);
        @SuppressWarnings("unchecked")
		Queue<Message> lst = (Queue<Message>) waitingList.get(clientRunnable);
        //messages are removed
        assertEquals(0,lst.size());
  }


    @SuppressWarnings("unchecked")
	@Test
    void runToExecuteMessageTerminate() throws IOException, NoSuchFieldException, IllegalAccessException{
    	 ScanNetNB scanNetNB = new ScanNetNB(channel);

         final Field scanNetNBMessages = scanNetNB.getClass().getDeclaredField("messages");

         scanNetNBMessages.setAccessible(true);
         Queue<Message> messageList = new LinkedList<>();
         scanNetNBMessages.set(scanNetNB, messageList);
         Message message =  Message.makeQuitMessage("Farha");
         messageList.offer(message);

         final Field clientRunnableInput = clientRunnable.getClass().getDeclaredField("input");
         clientRunnableInput.setAccessible(true);
         clientRunnableInput.set(clientRunnable, scanNetNB);

         final Field clientRunnableInitialized = clientRunnable.getClass().getDeclaredField("initialized");
         clientRunnableInitialized.setAccessible(true);
         boolean isInitialized = true;
         clientRunnableInitialized.set(clientRunnable, isInitialized);

         final Field clientRunnableName = clientRunnable.getClass().getDeclaredField("name");
         clientRunnableName.setAccessible(true);
         clientRunnableName.set(clientRunnable, "Farha");


         ScheduledFuture<ClientRunnable> sf;
	     ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	     sf = executor.schedule(new Callable() {
	                                  public Object call() throws Exception {
	                                      System.out.println("Executed!");
	                                      return "Called!";
	                                  }
	                              },
	               5,
	               TimeUnit.SECONDS);

	     clientRunnable.setFuture(sf);

         clientRunnable.run();

         isInitialized = (boolean) clientRunnableInitialized.get(clientRunnable);
         assertTrue(isInitialized);

    }

   @Test
    void runRunForNullUsename() throws IOException, NoSuchFieldException, IllegalAccessException{
    	 ScanNetNB scanNetNB = new ScanNetNB(channel);

         final Field scanNetNBMessages = scanNetNB.getClass().getDeclaredField("messages");

         scanNetNBMessages.setAccessible(true);
         Queue<Message> messageList = new LinkedList<>();
         scanNetNBMessages.set(scanNetNB, messageList);
         Message message = Message.makeBroadcastMessage(null,"How are you");
         messageList.offer(message);

         final Field clientRunnableInput = clientRunnable.getClass().getDeclaredField("input");
         clientRunnableInput.setAccessible(true);
         clientRunnableInput.set(clientRunnable, scanNetNB);

         final Field clientRunnableInitialized = clientRunnable.getClass().getDeclaredField("initialized");
         clientRunnableInitialized.setAccessible(true);
         boolean isInitialized = false;
         clientRunnableInitialized.set(clientRunnable, isInitialized);


         final Field clientRunnableName = clientRunnable.getClass().getDeclaredField("name");
         clientRunnableName.setAccessible(true);
         clientRunnableName.set(clientRunnable, "Farha");
         clientRunnable.run();
         isInitialized = (boolean) clientRunnableInitialized.get(clientRunnable);
         assertFalse(isInitialized);
    }

 @Test
 void runResponseNotEmpty() throws IOException, NoSuchFieldException, IllegalAccessException{
 	 ScanNetNB scanNetNB = new ScanNetNB(channel);

      final Field scanNetNBMessages = scanNetNB.getClass().getDeclaredField("messages");

      scanNetNBMessages.setAccessible(true);
      Queue<Message> messageList = new LinkedList<>();
      scanNetNBMessages.set(scanNetNB, messageList);

      final Field clientRunnableInput = clientRunnable.getClass().getDeclaredField("input");
      clientRunnableInput.setAccessible(true);
      clientRunnableInput.set(clientRunnable, scanNetNB);

      final Field clientRunnableInitialized = clientRunnable.getClass().getDeclaredField("initialized");
      clientRunnableInitialized.setAccessible(true);
      boolean isInitialized = true;
      clientRunnableInitialized.set(clientRunnable, isInitialized);

      final Field immediateResponse = clientRunnable.getClass().getDeclaredField("immediateResponse");
      immediateResponse.setAccessible(true);
      Message message = Message.makeBroadcastMessage("Farha","hello");

      Queue<Message> response = new LinkedList<>();
      response.offer(message);

      immediateResponse.set(clientRunnable,response);

      final Field clientRunnableName = clientRunnable.getClass().getDeclaredField("name");
      clientRunnableName.setAccessible(true);
      clientRunnableName.set(clientRunnable, "Farha");
      clientRunnable.run();
      isInitialized = (boolean) clientRunnableInitialized.get(clientRunnable);
      assertTrue(isInitialized);

   }
}