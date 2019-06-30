package edu.northeastern.ccs.im;

import edu.northeastern.ccs.im.server.ClientRunnable;
import edu.northeastern.ccs.im.server.Prattle;
import edu.northeastern.ccs.im.server.ServerConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests the public functions of PrintNetNB
 * @author Pratik Devikar
 */
class PrintNetNBTest {

    private static final Logger logger = Logger.getLogger(ClientRunnable.class.getName());
    private SocketNB snb;
    private static ServerSocketChannel serverSocket;
    
    
    /**
     * Opens a socket to create a selector and register any incoming connection
     * messages
     */
    @BeforeAll
    public static void openPort(){
        try {
            serverSocket = ServerSocketChannel.open();
            serverSocket.configureBlocking(false);
            serverSocket.socket().bind(new InetSocketAddress(4546));
            // Create the Selector with which our channel is registered.
            Selector selector = SelectorProvider.provider().openSelector();
            // Register to receive any incoming connection messages.
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        } catch (IOException e) {
            System.out.println(e.getMessage());
            logger.warning(e.getMessage());
        }
    }

//    @AfterEach
//    public void closePort() throws IOException{
//        serverSocket.close();
//    }
 
    /**
     * Construct a new ClientRunnable for testing purpose
     */
    @BeforeEach
    public void setUp() {
        String host = "127.0.0.1";
        int port  = 4546;
        try {
            snb = new SocketNB(host, port);

        } catch (IOException e) {
            System.out.println(e.getMessage());
            logger.warning(e.getMessage());
        }
    }

    /**
     * It tests whether attempt to send the message over network was
	 * successful.
     */
    @Test
    public void print() throws IOException {
    	String text = "Hello World !";
    	Message msg = Message.makeHelloMessage(text);
    	PrintNetNB pnb = new PrintNetNB(snb);
        assertEquals(Boolean.TRUE, pnb.print(msg));
        pnb = new PrintNetNB(snb.getSocket());
        assertEquals(Boolean.TRUE, pnb.print(msg));
        snb.close();
    }
    

    /**
     * It tests whether attempt to send the message over network was
	 * successful.
	 * Expected: false as connection was closed.
     * @throws IOException
     */
    @Test
    public void connectionClosed() throws IOException {
    	snb.close();
    	String text = "Hello World !";
    	Message msg = Message.makeHelloMessage(text);
    	PrintNetNB pnb = new PrintNetNB(snb);
        assertEquals(Boolean.FALSE, pnb.print(msg));
        pnb = new PrintNetNB(snb.getSocket());
        assertEquals(Boolean.FALSE, pnb.print(msg));
    }
    
   
     
}