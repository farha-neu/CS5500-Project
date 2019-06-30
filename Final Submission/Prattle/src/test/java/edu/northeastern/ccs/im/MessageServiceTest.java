package edu.northeastern.ccs.im;
/**
 * @author Pratik Devikar
 */

import edu.northeastern.ccs.im.dao.MessageService;
import edu.northeastern.ccs.im.dao.User;
import edu.northeastern.ccs.im.dao.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;


class MessageServiceTest {

    @InjectMocks
    MessageService mockService;

    @Mock
    private DirContext mockContext;

    @Mock
    private UserService mockus;

    @Mock
    private SearchResult mockSearchResult;

    @Mock
    private User mockUser;

    @Mock
    private NamingEnumeration ldapAttributeEnum;



    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    void saveMessage() throws NamingException {
        doReturn(null).when(mockContext).createSubcontext(anyString(), any(Attributes.class));
        Message message = Message.makeIndividualMessage("Steve", "SomeOne", "Hello");
        assertTrue(mockService.saveMessage(message));
    }

    @Test
    void saveMessage2() throws NamingException {
        doReturn(null).when(mockContext).createSubcontext(anyString(), any(Attributes.class));
        Message message = Message.makeIndividualMessage("Steve","SomeOne","Hello");
        when(mockContext.createSubcontext(anyString(), any(Attributes.class))).thenThrow(Exception.class);
        assertFalse(mockService.saveMessage(message));
    }

    @Test
    void getMessageBySender() throws NamingException {
        doReturn(null).when(mockContext).createSubcontext(anyString(), any(Attributes.class));
        doReturn(ldapAttributeEnum).when(mockContext).search(anyString(), anyString(), any(SearchControls.class));
        when(ldapAttributeEnum.hasMore()).thenReturn(false);
        assertEquals(Collections.emptyList(), mockService.getMessageBySender("Kit"));
    }

    @Test
    void getMessageBySender2() throws NamingException {

        Message message = Message.makeIndividualMessage("Steve","SomeOne","Hello");

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
        attributes.put("description",message.getText());
        attributes.put("title",message.getType());
        attributes.put("userpassword",user.getPassword());
        attributes.put("mail",user.getEmail());


        doReturn(null).when(mockContext).createSubcontext(anyString(), any(Attributes.class));
        doReturn(ldapAttributeEnum).when(mockContext).search(anyString(), anyString(), any(SearchControls.class));
        when(ldapAttributeEnum.next()).thenReturn(mockSearchResult);
        doReturn(attributes).when (mockSearchResult).getAttributes();
        when(ldapAttributeEnum.hasMore()).thenReturn(true, false);
        assertEquals("INDV 14 Pratik Devikar 7 Devikar 18 description: Hello", mockService.getMessageBySender("Kit")
                .get(0).toString());
    }

    @Test
    void getMessageBySender4() throws NamingException {

        Message message = Message.makeGroupMessage("pratikd","My Family", "how to kill");

        User user = new User("Pratik Devikar","Devikar","pratikd","b94","pd@gmail.com");

        mockService.setBlocked(true);
        // Create a container set of attributes
        Attributes attributes = new BasicAttributes();

        // Create the objectclass to add 
        Attribute attribute = new BasicAttribute("objectClass");
        attribute.add("inetOrgPerson");
        attributes.put(attribute);

        //add attributes to container
        attributes.put("uid",user.getUsername());
        attributes.put("cn",user.getFullName());
        attributes.put("sn",message.getMsgReceiver());
        attributes.put("description",message.getText());
        attributes.put("title",message.getType());
        attributes.put("userpassword",user.getPassword());
        attributes.put("mail",user.getEmail());
        attributes.put("employeeNumber","Flagged");

        doReturn(null).when(mockContext).createSubcontext(anyString(), any(Attributes.class));
        doReturn(ldapAttributeEnum).when(mockContext).search(anyString(), anyString(), any(SearchControls.class));
        when(ldapAttributeEnum.next()).thenReturn(mockSearchResult);
        doReturn(attributes).when (mockSearchResult).getAttributes();
        when(ldapAttributeEnum.hasMore()).thenReturn(true, false);

        assertEquals("My Family", mockService.getMessageBySender("Kit")
                .get(0).getMsgReceiver() );
    }

    @Test
    void getMessageBySender5() throws NamingException {

        Message message = Message.makeGroupMessage("pratikd","My Family", "Hello");

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
        attributes.put("sn",message.getMsgReceiver());
        attributes.put("description",message.getText());
        attributes.put("title",message.getType());
        attributes.put("userpassword",user.getPassword());
        attributes.put("mail",user.getEmail());

        doReturn(null).when(mockContext).createSubcontext(anyString(), any(Attributes.class));
        doReturn(ldapAttributeEnum).when(mockContext).search(anyString(), anyString(), any(SearchControls.class));
        when(ldapAttributeEnum.next()).thenReturn(mockSearchResult);
        doReturn(attributes).when (mockSearchResult).getAttributes();
        when(ldapAttributeEnum.hasMore()).thenReturn(true, false);

        assertEquals("Pratik Devikar", mockService.getMessageBySender("Kit")
                .get(0).getName() );
    }

    @Test
    void getMessageBySender6() throws NamingException {

        Message message = Message.makeGroupMessage("pratikd","My Family", "Hello");

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
        attributes.put("sn",message.getMsgReceiver());
        attributes.put("description",message.getText());
        attributes.put("title",message.getType());
        attributes.put("userpassword",user.getPassword());
        attributes.put("mail",user.getEmail());

        doReturn(null).when(mockContext).createSubcontext(anyString(), any(Attributes.class));
        doReturn(ldapAttributeEnum).when(mockContext).search(anyString(), anyString(), any(SearchControls.class));
        when(ldapAttributeEnum.next()).thenReturn(mockSearchResult);
        doReturn(attributes).when (mockSearchResult).getAttributes();
        when(ldapAttributeEnum.hasMore()).thenReturn(true, false);

        assertEquals("description: Hello", mockService.getMessageBySender("Kit")
                .get(0).getText() );
    }

    @Test
    void getMessageByReceiver() throws NamingException {
        doReturn(null).when(mockContext).createSubcontext(anyString(), any(Attributes.class));
        doReturn(ldapAttributeEnum).when(mockContext).search(anyString(), anyString(), any(SearchControls.class));
        when(ldapAttributeEnum.hasMore()).thenReturn(false);
        assertEquals(Collections.emptyList(), mockService.getMessageByReceiver("Kit"));
    }

    @Test
    void getMessageByReceiver2() throws NamingException {

        Message message = Message.makeIndividualMessage("Steve","SomeOne","Hello");

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
        attributes.put("description",message.getText());
        attributes.put("title",message.getType());
        attributes.put("userpassword",user.getPassword());
        attributes.put("mail",user.getEmail());


        doReturn(null).when(mockContext).createSubcontext(anyString(), any(Attributes.class));
        doReturn(ldapAttributeEnum).when(mockContext).search(anyString(), anyString(), any(SearchControls.class));
        when(ldapAttributeEnum.next()).thenReturn(mockSearchResult);
        doReturn(attributes).when (mockSearchResult).getAttributes();
        when(ldapAttributeEnum.hasMore()).thenReturn(true, false);
        assertEquals("INDV 14 Pratik Devikar 7 Devikar 18 description: Hello", mockService.getMessageByReceiver("Kit")
                .get(0).toString());
    }

    @Test
    void getMessageByReceiver3() throws NamingException {

        Message message = Message.makeGroupMessage("My Family", "Steve","Hello");

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
        attributes.put("description",message.getText());
        attributes.put("title",message.getType());
        attributes.put("userpassword",user.getPassword());
        attributes.put("mail",user.getEmail());

        doReturn(null).when(mockContext).createSubcontext(anyString(), any(Attributes.class));
        doReturn(ldapAttributeEnum).when(mockContext).search(anyString(), anyString(), any(SearchControls.class));
        when(ldapAttributeEnum.next()).thenReturn(mockSearchResult);
        doReturn(attributes).when (mockSearchResult).getAttributes();
        when(ldapAttributeEnum.hasMore()).thenReturn(true, false);
        assertEquals("GRM 14 Pratik Devikar 7 Devikar 18 description: Hello", mockService.getMessageByReceiver("Kit")
                .get(0)
                .toString());
    }

//    @Test
//    void testNameNull() throws NamingException {
//        mockService = new MessageService();
//    	assertEquals(Collections.emptyList(), mockService.getMessageByReceiver(""));
//    }


    @Test
    void getMessageByTimestamp() throws NamingException {
        doReturn(null).when(mockContext).createSubcontext(anyString(), any(Attributes.class));
        doReturn(ldapAttributeEnum).when(mockContext).search(anyString(), anyString(), any(SearchControls.class));
        when(ldapAttributeEnum.hasMore()).thenReturn(false);
        assertEquals(Collections.emptyList(), mockService.getMessageByTimestamp("2018.03.10 12:12:12"));
    }

    @Test
    void saveQueueMsg2() throws NamingException {
        doReturn(null).when(mockContext).createSubcontext(anyString(), any(Attributes.class));
        User user = new User("Pratik Devikar","Devikar","aaa","b94","pd@gmail.com");
        user.setOnline(true);
        Message message = Message.makeHelloMessage("Hello!!");
        ArrayList<Message> msglist = new ArrayList<>();
        msglist.add(message);
        user.setQueuedMsgs(msglist);
        ArrayList<User> ulist = new ArrayList<>();
        ulist.add(user);
        doReturn(ulist).when(mockus).findAllUsers();
        when(mockUser.isOnline()).thenReturn(true,true,true);
//        doReturn(null).when(mockUser).getQueuedMsgs();
        assertTrue(mockService.saveQueuedMessageForAUser());
    }

    @Test
    void recallMessage() throws NamingException {
        Message message = Message.makeHelloMessage("Hello!!");
        User user = new User("Pratik Devikar","Devikar","pratikd","b94","pd@gmail.com");
        doReturn(ldapAttributeEnum).when(mockContext).search(anyString(), anyString(), any(SearchControls.class));
        when(ldapAttributeEnum.next()).thenReturn(mockSearchResult);
        when(ldapAttributeEnum.hasMore()).thenReturn(true, false);
        Message message2 = Message.makeHelloMessage("Good morning!!");
        ArrayList<Message> msg2list = new ArrayList<>();
        msg2list.add(message2);
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
        attributes.put("description",message.getText());
        attributes.put("title",message.getType());
        attributes.put("userpassword",user.getPassword());
        attributes.put("mail",user.getEmail());
        when(mockSearchResult.getAttributes()).thenReturn(attributes);
        assertTrue(mockService.recallMessage("pratikd", "James", message));
    }


    @Test
    public void updateUserQueue(){
        Message message = Message.makeHelloMessage("Hello!!");
        User user = new User("Pratik Devikar","Devikar","pratikd","b94","pd@gmail.com");
        ArrayList<Message> msglist = new ArrayList<>();
        user.setQueuedMsgs(msglist);
        msglist.add(message);
//        doReturn(msglist).when(mockus).findUserByUsername(anyString()).getQueuedMsgs();
        when(mockus.findUserByUsername(anyString())).thenReturn(user);
//        when(mockus.updateUserQueuedMsgsRecall(anyString(), null)).thenReturn(true);
        assertEquals(0, mockService.updateUserQueue(mockus, "pratikd", message));
    }
}
