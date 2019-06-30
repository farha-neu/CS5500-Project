package edu.northeastern.ccs.im;

import edu.northeastern.ccs.im.server.ClientRunnable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
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
 * @author Pratik Devikar
 */
class SocketNBTest {

    private static final Logger logger = Logger.getLogger(ClientRunnable.class.getName());
    SocketNB snb;

    @BeforeAll
    public static void openPort(){
        try {
            int port = 4546;
            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.configureBlocking(false);
            serverSocket.socket().bind(new InetSocketAddress(port));
            // Create the Selector with which our channel is registered.
            Selector selector = SelectorProvider.provider().openSelector();
            // Register to receive any incoming connection messages.
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        } catch (IOException e) {
            System.out.println(e.getMessage());
            logger.warning(e.getMessage());
        }
    }
 
    /** 
     * Construct a new ClientRunnable for testing purpose
     */
    @BeforeEach
    public void setUp() {
        try {
            int port = 4546;
            String host = "127.0.0.1";
            SocketChannel channel;
            // Open a new channel
            channel = SocketChannel.open();
            // Make this channel a non-blocking channel
            channel.configureBlocking(false);
            // Connect the channel to the remote port
            channel.connect(new InetSocketAddress("127.0.0.1", port));
            snb = new SocketNB(host, port);

        } catch (IOException e) {
            System.out.println(e.getMessage());
            logger.warning(e.getMessage());
        } 
    }  
    /**
     * It tests whether Socket Channel is connected to the network
     * Expected: true
     */
    @Test
    void getSocket() throws IOException, NoSuchFieldException, IllegalAccessException {
        assertEquals(Boolean.TRUE, snb.getSocket().isConnected());
        snb.close();
    }
    
    /**
     * 
     * It tests whether Socket Channel is connected to the network
     * Expected: false as connection was closed
     * @throws IOException
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     */
    @Test
    void getSocketFalse() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        snb.close();
        assertEquals(Boolean.FALSE, snb.getSocket().isConnected());
        String host = "127.0.0.1";
        int port  = 4546;
        snb = new SocketNB(host, port);
        final Field field = snb.getClass().getDeclaredField("channel");
        field.setAccessible(true);
        SocketChannel channel = null;
        field.set(snb, channel);
    }
}