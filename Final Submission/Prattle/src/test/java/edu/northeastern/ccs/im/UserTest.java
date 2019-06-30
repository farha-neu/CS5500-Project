package edu.northeastern.ccs.im;
/**
 * @author Pratik Devikar
 */

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.northeastern.ccs.im.dao.User;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

class UserTest {

    private User user;

    @BeforeEach
    void Before(){
        user = new User("Pratik Devikar", "Devikar", "pratikdevikar", "password", "pd@gmail.com");
    }

    @Test
    void getFullName() {
        user = new User();
        assertEquals(null, user.getFullName());
    }

    @Test
    void getFullName2() {
        user = new User("Pratik Devikar","Devikar","pratikdevikar","pd@gmail.com");
        assertEquals("Pratik Devikar", user.getFullName());
    }
    
    @Test
    void getMessageSize() {
    	ArrayList<Message>msg = new ArrayList<>();
    	Message message = Message.makeHelloMessage("Hello");
    	msg.add(message);
        user = new User("Farha Jawed","Jawed","farha","1234","farhajw@gmail.com",msg);
        assertEquals(1, user.getQueuedMsgs().size());
    }
    
    
    @Test
    void getLastName() {
        assertEquals("Devikar", user.getLastName());
    }


    @Test
    void getUsername() {
        user = new User("pratikdevikar","password");
        assertEquals("pratikdevikar", user.getUsername());
    }

    @Test
    void getUsername2() {
    	user = new User("farha");
    	assertEquals("farha",user.getUsername());
    }
  

    @Test
    void getPassword() {
        assertEquals("password", user.getPassword());
    }

    @Test
    void getEmail() {
        assertEquals("pd@gmail.com", user.getEmail());
    }
    
    @Test
    void getIsOnline() {
    	assertEquals(false, user.isOnline());
    }
    
    @Test
    void getIsBacklogBeingLoaded() {
    	assertEquals(false, user.isBacklogBeingLoaded());
    }
    
    @Test
    void setIsOnline() {
    	user.setOnline(true);
    	assertEquals(true, user.isOnline());
    }
    
    @Test
    void setIsBacklogBeingLoaded() {
    	user.setBacklogBeingLoaded(true);
    	assertEquals(true, user.isBacklogBeingLoaded());
    }

}