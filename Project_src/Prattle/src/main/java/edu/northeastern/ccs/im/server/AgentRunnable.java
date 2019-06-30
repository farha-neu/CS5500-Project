package edu.northeastern.ccs.im.server;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class AgentRunnable extends ClientRunnable {
    public AgentRunnable(SocketChannel client) throws IOException {
        super(client);
    }

    public AgentRunnable(SocketChannel client, String agentName) throws IOException {
        super(client, agentName);
    }

    public static void setValidTime(long duration){
        terminateTime = duration;
    }

}
