package edu.northeastern.ccs.im.server;

import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.dao.Group;
import edu.northeastern.ccs.im.dao.User;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

class PrattleTest {


    @AfterEach
    void terminateSocket(){
        try {
            Prattle.stopPort();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Not able to stop.");
        }
    }


    private SocketChannel setUpChannel() {
        try {
            // Open a new channel
            SocketChannel channel = SocketChannel.open();
            // Make this channel a non-blocking channel
            channel.configureBlocking(false);
            // Connect the channel to the remote port
            channel.connect(new InetSocketAddress("127.0.0.1", ServerConstants.PORT));

            Selector selector = Selector.open();
            SelectionKey key = channel.register(selector, SelectionKey.OP_CONNECT);

            selector.select(0);
            assert key.isConnectable();
            if(!channel.finishConnect()){
                fail("Cannot connect");
            }

            selector.close();
            return channel;

        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Test
    public void testBroadCast() throws IOException {
        Job1Thread job1 = new Job1Thread();

        job1.start();

        await().atMost(5, TimeUnit.SECONDS).until(Prattle::isRunning);

        SocketChannel channel = setUpChannel();
        Selector selector = null;
        SelectionKey key = null;
        try {
            selector = Selector.open();
            key = channel.register(selector, SelectionKey.OP_READ);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

        Queue<ClientRunnable> runnables = Prattle.getActive();
        String userName = "A";
        Prattle.addActive(channel, userName);
        for(ClientRunnable runnable: runnables){
            System.out.println(runnable.getName());
            if(runnable.getName() != null && runnable.getName().equals(userName)){
                assertEquals(0, runnable.getWaitingList().size());
            }
        }
        Message broadcast = Message.makeBroadcastMessage("Me", "Hello");
        Prattle.broadcastMessage(broadcast);
        for(ClientRunnable runnable: runnables){
            System.out.println(runnable.getName());
            if(runnable.getName() != null && runnable.getName().equals(userName)){
                assertEquals(broadcast.toString(), runnable.getWaitingList().peek().toString());
            }
        }
    }

    @Test
    public void testSendMessage() throws Exception {
        Job1Thread job1 = new Job1Thread();

        job1.start();

        await().atMost(5, TimeUnit.SECONDS).until(Prattle::isRunning);

        SocketChannel channel = setUpChannel();
        Selector selector = null;
        SelectionKey key = null;
        try {
            selector = Selector.open();
            key = channel.register(selector, SelectionKey.OP_READ);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

        Queue<ClientRunnable> runnables = Prattle.getActive();
        String sender = "Yang";
        String receiver = "Xia";
        Prattle.addActive(channel, sender);
        Prattle.addActive(channel, receiver);
        for(ClientRunnable runnable: runnables){
            System.out.println(runnable.getName());
            if(runnable.getName() != null && runnable.getName().equals(receiver)){
                assertEquals(0, runnable.getWaitingList().size());
            }
        }
        Prattle.sendIndividualMessage("Someone", receiver, "Hello Yang!");
        Prattle.sendIndividualMessage(sender, receiver, "Hello Yang!");

        while(!runnables.isEmpty()){
            ClientRunnable runnable = runnables.poll();
            System.out.println(runnable.getName());
            if(runnable.getName() != null && runnable.getName().equals(receiver)){
                assertEquals("Hello Yang!", runnable.getWaitingList().peek().getText());
            }
        }

    }

    @Test
    public void testSendToGroup() throws Exception {
        Job1Thread job1 = new Job1Thread();

        job1.start();

        await().atMost(5, TimeUnit.SECONDS).until(Prattle::isRunning);

        SocketChannel channel = setUpChannel();
        Selector selector = null;
        SelectionKey key = null;
        try {
            selector = Selector.open();
            key = channel.register(selector, SelectionKey.OP_READ);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

        String userName = "Pat";
        Prattle.addActive(channel, userName);

        String groupName = "newGroup";
        List<User> userList = new ArrayList<>();
        User user = new User(userName, "123456");
        userList.add(user);
        Group newGroup = new Group(groupName, userList);

        Prattle.addGrouptoMap(newGroup);


        Queue<ClientRunnable> runnables = Prattle.getActive();
        for(ClientRunnable runnable: runnables){
            if(runnable.getName() != null && runnable.getName().equals(userName)){
                assertEquals(0, runnable.getWaitingList().size());
            }
        }

        Message testMessage = Message.makeGroupMessage("Prattle", groupName,  "Hello Yang!");
        Prattle.sendToGroup(testMessage);

        System.out.println(runnables.size());
        for(ClientRunnable runnable: runnables){
            System.out.println(runnable.getName());

            if(runnable.getName() != null && runnable.getName().equals(userName)){
                System.out.println("HAHAH");
                assertEquals(testMessage.toString(), runnable.getWaitingList().peek().toString());
            }
        }

    }

    @Test
    public void testUserAgent() throws Exception {
        Job1Thread job1 = new Job1Thread();

        job1.start();

        await().atMost(2, TimeUnit.SECONDS).until(Prattle::isRunning);

        SocketChannel channel = setUpChannel();
        Selector selector = null;
        SelectionKey key = null;
        try {
            selector = Selector.open();
            key = channel.register(selector, SelectionKey.OP_READ);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

        String sender = "Yang";
        String receiver = "Xia";
        String remoteAddress = channel.getRemoteAddress().toString();
        Prattle.addActive(channel, sender);
        Prattle.addActive(channel, receiver);

        AgentRunnable agentRunnable = Prattle.createNewAgent(channel, "someAgent", 5000);
        assertEquals(0, agentRunnable.getWaitingList().size());

        Prattle.addUserAgent(sender, agentRunnable);
        Prattle.addUserAgent("NonExsist", agentRunnable);

        Prattle.sendIndividualMessage(sender, receiver, "Hello Yang!");

        assertEquals(1, agentRunnable.getWaitingList().size());
        assertEquals("Hello Yang!" + "SenderIP: " + remoteAddress + " ReceiverIP" + remoteAddress,
                agentRunnable.getWaitingList().poll().getText());
    }

    @Test
    public void testGroupAgent() throws Exception {
        Job1Thread job1 = new Job1Thread();

        job1.start();

        await().atMost(2, TimeUnit.SECONDS).until(Prattle::isRunning);

        SocketChannel channel = setUpChannel();
        Selector selector = null;
        SelectionKey key = null;
        try {
            selector = Selector.open();
            key = channel.register(selector, SelectionKey.OP_READ);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

        String userName = "Pat";
        String remoteAddress = channel.getRemoteAddress().toString();
        Prattle.addActive(channel, userName);

        String groupName = "newGroup";
        List<User> userList = new ArrayList<>();
        User user = new User(userName, "123456");
        userList.add(user);
        Group newGroup = new Group(groupName, userList);

        Prattle.addGrouptoMap(newGroup);

        AgentRunnable agentRunnable = Prattle.createNewAgent(channel, "someGroupAgent", 5000);
        assertEquals(0, agentRunnable.getWaitingList().size());

        Prattle.addGroupAgent(groupName, agentRunnable);
        Prattle.addGroupAgent("NonExsist", agentRunnable);

        Message testMessage = Message.makeGroupMessage(userName, groupName,  "Hello Yang!");
        Prattle.sendToGroup(testMessage);

        assertEquals(1, agentRunnable.getWaitingList().size());
        assertEquals("Hello Yang!" + "SenderIP: " + remoteAddress,
                agentRunnable.getWaitingList().poll().getText());
    }

    public class Job1Thread extends Thread {

        @Override
        public void run() {
            try { 
                Prattle.main(new String[]{""});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}