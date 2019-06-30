package edu.northeastern.ccs.im;
/**
 * @author Pratik Devikar
 */

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import edu.northeastern.ccs.im.dao.User;
import edu.northeastern.ccs.im.dao.UserService;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @InjectMocks
    UserService mockService;

    @Mock
    private User mockUser;

    @Mock
    private DirContext mockContext;

    @Mock
    private NamingEnumeration ldapAttributeEnum;

    @Mock
    private SearchResult mockSearchResult;

    @Mock
    private Attributes mockAttributes;

    @Mock
    private List<User> userList;


    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    void createUser() throws NamingException {
        doReturn(null).when(mockContext).createSubcontext(anyString(), any(Attributes.class));
        User user = new User("Pratik Devikar","Devikar","aaa","b94","pd@gmail.com");
        assertTrue(mockService.createUser(user));
    }

    @Test
    void createUser2() throws NamingException {
        doReturn(null).when(mockContext).createSubcontext(anyString(), any(Attributes.class));
        User user = new User("Pratik Devikar","Devikar","aaa","b94","pd@gmail.com");
        when(mockService.createUser(user)).thenThrow(Exception.class);
        assertFalse(mockService.createUser(user));
    }

    @Test
    void findAllUsers() throws NamingException {
        doReturn(ldapAttributeEnum).when (mockContext).search(anyString(), anyString(), any(SearchControls.class));
        when(ldapAttributeEnum.hasMore()).thenReturn(false);
        assertEquals(Collections.emptyList(), mockService.findAllUsers());
    }

    @Test
    void findAllUsers2() throws NamingException {
        User user = new User("Pratik Devikar","Devikar","pratikd","b94","pd@gmail.com");

        // Create a container set of attributes
        Attributes attributes = new BasicAttributes();

        // Create the objectclass to add
        Attribute attribute = new BasicAttribute("objectClass");
        attribute.add("inetOrgPerson");
        attributes.put(attribute);

        //add attributes to container
        attributes.put("uid",user.getUsername());
        attributes.put("cn",user.getFullName());
        attributes.put("sn",user.getLastName());
        attributes.put("mail",user.getEmail());



        doReturn(ldapAttributeEnum).when (mockContext).search(anyString(), anyString(), any(SearchControls.class));
        when(ldapAttributeEnum.hasMore()).thenReturn(true, false);
        when(ldapAttributeEnum.next()).thenReturn(mockSearchResult);
        doReturn(attributes).when (mockSearchResult).getAttributes();

        ArrayList<User> userlist= new ArrayList<>();
        userlist.add(user);
        assertEquals(userlist.get(0).getUsername(), mockService.findAllUsers().get(0).getUsername());
    }

    @Test
    void findUserByUsername() throws NamingException {
        doReturn(ldapAttributeEnum).when (mockContext).search(anyString(), anyString(), any(SearchControls.class));
        when(ldapAttributeEnum.hasMore()).thenReturn(false);
        mockService.findUserByUsername("pratikd");
    }

    @Test
    void findUserByUsername2() throws NamingException {
        doReturn(ldapAttributeEnum).when (mockContext).search(anyString(), anyString(), any(SearchControls.class));
        when(ldapAttributeEnum.hasMore()).thenReturn(false);
        when(mockService.findUserByUsername("pratikd")).thenThrow(Exception.class);
        assertEquals(null, mockService.findUserByUsername("pratikd7"));
    }

    //    @Test
//    void findUserByUsername3() throws NamingException {
//        doReturn(ldapAttributeEnum).when (mockContext).search(anyString(), anyString(), any(SearchControls.class));
//        when(ldapAttributeEnum.hasMore()).thenReturn(true, false);
//        when(ldapAttributeEnum.next()).thenReturn(mockSearchResult);
//
//
//        User user = new User("Pratik Devikar","Devikar","pratikd","b94","pd@gmail.com");
//        // Create a container set of attributes
//        Attributes attributes = new BasicAttributes();
//        // Create the objectclass to add
//        Attribute attribute = new BasicAttribute("objectClass");
//        attribute.add("inetOrgPerson");
//        attributes.put(attribute);
//        //add attributes to container
//        attributes.put("uid",user.getUsername());
//        attributes.put("cn",user.getFullName());
//        attributes.put("sn",user.getLastName());
//        attributes.put("mail",user.getEmail());
//        doReturn(attributes).when (mockSearchResult).getAttributes();
//
//        assertEquals(user.getUsername(), mockService.findUserByUsername("pratikd").getUsername());
//    }
    @Test
    void login() throws NamingException {
        doReturn(ldapAttributeEnum).when (mockContext).search(anyString(), anyString(), any(SearchControls.class));
        when(ldapAttributeEnum.hasMore()).thenReturn(false);
        when(mockService.login(new User("Pratik Devikar","Devikar","pratikd","b94","pd@gmail.com")))
                .thenThrow(Exception.class);
        assertEquals(false, mockService.login(new User("Pratik Devikar","Devikar","pratikd","b94","pd@gmail.com")));
    }

    @Test
    void login2() throws NamingException {
        doReturn(ldapAttributeEnum).when (mockContext).search(anyString(), anyString(), any(SearchControls.class));
        when(ldapAttributeEnum.hasMore()).thenReturn(true, false);

        when(ldapAttributeEnum.next()).thenReturn(mockSearchResult);
        User user = new User("Pratik Devikar","Devikar","pratikd","b94","pd@gmail.com");
        ArrayList<Message> msglist = new ArrayList<>();
        user.setQueuedMsgs(msglist);
        // Create a container set of attributes
        Attributes attributes = new BasicAttributes();
        // Create the objectclass to add
        Attribute attribute = new BasicAttribute("objectClass");
        attribute.add("inetOrgPerson");
        attributes.put(attribute);
        //add attributes to container
        attributes.put("uid",user.getUsername());
        attributes.put("cn",user.getFullName());
        attributes.put("sn",user.getLastName());
        attributes.put("userpassword",user.getPassword());
        attributes.put("mail",user.getEmail());
        attributes.put("info", user.getQueuedMsgs());
        doReturn(attributes).when (mockSearchResult).getAttributes();


        assertEquals(true, mockService.login(new User("Pratik Devikar","Devikar","pratikd","b94","pd@gmail.com")));
    }

    @Test
    void updateUser() throws NamingException {
        User user = new User("Pratik A Devikar","Devikar","pratikd","b94","pd@gmail.com");
        assertEquals(true, mockService.updateUser(user));
    }


    @Test
    void updateUser2() throws NamingException {
        User user = new User("Pratik A Devikar","Devikar","pratikd","b94","pd@gmail.com");
        when(mockService.updateUser(user)).thenThrow(Exception.class);
        assertEquals(true, mockService.updateUser(user));
    }


    @Test
    void deleteUser() throws NamingException{
        assertEquals(true, mockService.deleteUserByUsername("pratikd"));
    }

    @Test
    void deleteUser2() throws NamingException{
        when(mockService.deleteUserByUsername("pratikd7")).thenThrow(Exception.class);
        assertEquals(true, mockService.deleteUserByUsername("pratikd"));
    }

    @Test
    void updateUserQueuedMsgs() throws NamingException {
        Message message = Message.makeHelloMessage("Hello!!");

        doReturn(ldapAttributeEnum).when (mockContext).search(anyString(), anyString(), any(SearchControls.class));
        when(ldapAttributeEnum.hasMore()).thenReturn(true, false);

        User user = new User("Pratik Devikar","Devikar","pratikd","b94","pd@gmail.com");
        ArrayList<Message> msglist = new ArrayList<>();
        user.setQueuedMsgs(msglist);
        // Create a container set of attributes
        Attributes attributes = new BasicAttributes();
        // Create the objectclass to add
        Attribute attribute = new BasicAttribute("objectClass");
        attribute.add("inetOrgPerson");
        attributes.put(attribute);
        //add attributes to container
        attributes.put("uid",user.getUsername());
        attributes.put("cn",user.getFullName());
        attributes.put("sn",user.getLastName());
        attributes.put("mail",user.getEmail());
        attributes.put("info", user.getQueuedMsgs());

        when(ldapAttributeEnum.next()).thenReturn(mockSearchResult);
        when(mockSearchResult.getAttributes()).thenReturn(attributes);

        assertEquals(true, mockService.updateUserQueuedMsgs("ab", null));
    }

    @Test
    void updateUserQueuedMsgs2() throws NamingException {
        Message message = Message.makeHelloMessage("Hello!!");

        doReturn(ldapAttributeEnum).when (mockContext).search(anyString(), anyString(), any(SearchControls.class));
        when(ldapAttributeEnum.hasMore()).thenReturn(true, false);

        User user = new User("Pratik Devikar","Devikar","pratikd","b94","pd@gmail.com");
        ArrayList<Message> msglist = new ArrayList<>();
        user.setQueuedMsgs(msglist);
        // Create a container set of attributes
        Attributes attributes = new BasicAttributes();
        // Create the objectclass to add
        Attribute attribute = new BasicAttribute("objectClass");
        attribute.add("inetOrgPerson");
        attributes.put(attribute);
        //add attributes to container
        attributes.put("uid",user.getUsername());
        attributes.put("cn",user.getFullName());
        attributes.put("sn",user.getLastName());
        attributes.put("mail",user.getEmail());
        attributes.put("info", user.getQueuedMsgs());

        when(ldapAttributeEnum.next()).thenReturn(mockSearchResult);
        when(mockSearchResult.getAttributes()).thenReturn(attributes);

        when(mockService.updateUserQueuedMsgs("jamesbond", message)).thenThrow(Exception.class);
        assertEquals(false, mockService.updateUserQueuedMsgs("ab", null));
    }

    @Test
    void updateUserQueuedMsgsRecall() throws NamingException {
        Message message = Message.makeHelloMessage("Hello!!");
        Message message2 = Message.makeHelloMessage("Good morning!!");
        ArrayList<Message> msg2list = new ArrayList<>();
        msg2list.add(message2);

        doReturn(ldapAttributeEnum).when (mockContext).search(anyString(), anyString(), any(SearchControls.class));
        when(ldapAttributeEnum.hasMore()).thenReturn(true, false);

        User user = new User("Pratik Devikar","Devikar","pratikd","b94","pd@gmail.com");
        ArrayList<Message> msglist = new ArrayList<>();
        user.setQueuedMsgs(msglist);
        // Create a container set of attributes
        Attributes attributes = new BasicAttributes();
        // Create the objectclass to add
        Attribute attribute = new BasicAttribute("objectClass");
        attribute.add("inetOrgPerson");
        attributes.put(attribute);
        //add attributes to container
        attributes.put("uid",user.getUsername());
        attributes.put("cn",user.getFullName());
        attributes.put("sn",user.getLastName());
        attributes.put("mail",user.getEmail());
        attributes.put("info", user.getQueuedMsgs());

        when(ldapAttributeEnum.next()).thenReturn(mockSearchResult);
        when(mockSearchResult.getAttributes()).thenReturn(attributes);

        assertEquals(true, mockService.updateUserQueuedMsgsRecall("ab", msg2list));
    }

    @Test
    void updateUserQueuedMsgsRecall2() throws NamingException {
        Message message = Message.makeHelloMessage("Hello!!");
        Message message2 = Message.makeHelloMessage("Good morning!!");
        ArrayList<Message> msg2list = new ArrayList<>();
        msg2list.add(message2);

        doReturn(ldapAttributeEnum).when (mockContext).search(anyString(), anyString(), any(SearchControls.class));
        when(ldapAttributeEnum.hasMore()).thenReturn(true, false);

        User user = new User("Pratik Devikar","Devikar","pratikd","b94","pd@gmail.com");
        ArrayList<Message> msglist = new ArrayList<>();
        user.setQueuedMsgs(msglist);
        // Create a container set of attributes
        Attributes attributes = new BasicAttributes();
        // Create the objectclass to add
        Attribute attribute = new BasicAttribute("objectClass");
        attribute.add("inetOrgPerson");
        attributes.put(attribute);
        //add attributes to container
        attributes.put("uid",user.getUsername());
        attributes.put("cn",user.getFullName());
        attributes.put("sn",user.getLastName());
        attributes.put("mail",user.getEmail());
        attributes.put("info", user.getQueuedMsgs());

        when(ldapAttributeEnum.next()).thenReturn(mockSearchResult);
        when(mockSearchResult.getAttributes()).thenReturn(attributes);


//        when(mockService.findUserByUsername("pratikd7")).thenReturn(user);
        when(mockService.updateUserQueuedMsgsRecall("jamesbond", msg2list)).thenThrow(Exception.class);
        assertEquals(false, mockService.updateUserQueuedMsgsRecall("ab", msg2list));
    }

}
