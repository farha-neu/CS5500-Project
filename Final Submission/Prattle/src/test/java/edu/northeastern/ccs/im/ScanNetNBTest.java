package edu.northeastern.ccs.im;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.northeastern.ccs.im.server.ClientRunnable;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
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
 * @authors Pratik Devikar
 */
public class ScanNetNBTest {

    private static ScanNetNB snnb;
    private static SocketNB snb;
    private static ServerSocketChannel serverSocket;
    private Message msg1, msg2;
    private static final Logger logger = Logger.getLogger(ScanNetNB.class.getName());
    static Selector selector;

    @AfterEach
    public void closePort() throws IOException {
        serverSocket.close();
    }

    @BeforeAll
    public static void openPort() {
        try {
            serverSocket = ServerSocketChannel.open();
            serverSocket.configureBlocking(false);
            serverSocket.socket().bind(new InetSocketAddress(4545));
            // Create the Selector with which our channel is registered.
            selector = SelectorProvider.provider().openSelector();
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
        String host = "127.0.0.1";
        int port = 4545;
        try {
            snb = new SocketNB(host, port);

        } catch (IOException e) {
            System.out.println(e.getMessage());
            logger.warning(e.getMessage());
        }
        snnb = new ScanNetNB(snb);
    }


    @Test
    public void hasNextMessage() throws IOException, IllegalAccessException, NoSuchFieldException {
        final Field field = snnb.getClass().getDeclaredField("messages");
        field.setAccessible(true);
        Queue<Message> messageList = new LinkedList<>();
        field.set(snnb, messageList);
        assertEquals(Boolean.FALSE, snnb.hasNextMessage());
        serverSocket.close();
    }


    @Test
    public void hasNextMessage2() throws IOException, IllegalAccessException, NoSuchFieldException {
        msg1 = Message.makeHelloMessage("Hello World!");
        msg2 = Message.makeHelloMessage("Hello again");

        final Field field = snnb.getClass().getDeclaredField("messages");
        field.setAccessible(true);
        Queue<Message> messageList = new LinkedList<>();
        messageList.add(msg1);
        messageList.add(msg2);
        field.set(snnb, messageList);

        assertEquals(Boolean.TRUE, snnb.hasNextMessage());
        serverSocket.close();
    }

    @Test
    public void nextMessage() throws IOException, NoSuchFieldException, IllegalAccessException {


        msg1 = Message.makeHelloMessage("Hello World!");
        msg2 = Message.makeHelloMessage("Hello again");

        final Field field = snnb.getClass().getDeclaredField("messages");
        field.setAccessible(true);
        Queue<Message> messageList = new LinkedList<>();
        messageList.add(msg1);
        messageList.add(msg2);
        field.set(snnb, messageList);

        assertEquals(msg1, snnb.nextMessage());
        serverSocket.close();
    }

    @Test
    public void nextMessage2() throws IOException, NoSuchFieldException, IllegalAccessException {
        final Field field = snnb.getClass().getDeclaredField("messages");
        field.setAccessible(true);
        Queue<Message> messageList = new LinkedList<>();
        field.set(snnb, messageList);

        try {
            assertEquals("", snnb.nextMessage());
        } catch (NextDoesNotExistException e) {
            System.err.println("No more messages in the message list");
        }
        serverSocket.close();
    }

    @Test
    public void close() throws IOException {
        snnb.close();
        assertEquals(Boolean.FALSE, snnb.getSelector().isOpen());
    }

}