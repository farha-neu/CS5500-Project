package edu.northeastern.ccs.im;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.northeastern.ccs.im.dao.Group;
import edu.northeastern.ccs.im.dao.User;

/**
 * This class tests the functionality of Group class
 * @author farha
 *
 */
public class GroupTest {
	
	    private Group group;
	    private List<User>users;

	    @BeforeEach
	    public void Before(){
	    	users = new ArrayList<>();
	    	User user = new User("farha","1232");
	    	users.add(user);
	        group = new Group("MSD",users);
	    }
	    
	    @Test
	    public void testGetGroupName() {
	    	assertEquals("MSD",group.getGroupName());
	    }
	    

	    
	    @Test
	    public void testGetUsers() {
	    	int i = 0;
	    	for(User user: users) {
	    		assertEquals(user.getUsername(),group.getUsers().get(i).getUsername());
	    		i++;
	    	}
	    	
	    }

	    
}

