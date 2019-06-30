package edu.northeastern.ccs.im;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import edu.northeastern.ccs.im.dao.Group;
import edu.northeastern.ccs.im.dao.GroupService;
import edu.northeastern.ccs.im.dao.User;

/**
 * This class tests the methods of GroupService class.
 * @author Farha Jawed
 * @version 1.0
 *
 */
public class GroupServiceTest{
	
	@InjectMocks
    GroupService mockService;

  
    @Mock
    private DirContext mockContext;

    @SuppressWarnings("rawtypes")
	@Mock
    private NamingEnumeration ldapAttrEnum;
    
    
    @Mock
    private SearchResult searchResult;
    
    @Mock 
    private Attributes attr;
    
    @Mock 
    private Attribute mockAttr;
    
    @Mock
    Group groupMock;
    
    @Mock 
    private List<User>users;
   
   
    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void testForCreateGroup() throws NamingException {
        doReturn(null).when(mockContext).createSubcontext(anyString(), any(Attributes.class));
        Group group = new Group("MSD");
        assertTrue(mockService.createGroup(group));
    }

    @SuppressWarnings("unchecked")
	@Test
    public void testForCreateGroupException() throws NamingException {
        doReturn(null).when(mockContext).createSubcontext(anyString(), any(Attributes.class));
        Group group = new Group("MSD");
        when(mockService.createGroup(group)).thenThrow(Exception.class);
        assertFalse(mockService.createGroup(group));
   }
    
    @Test
    public void testFindAllGroups() throws NamingException {
 	   Group group = new Group("MSD");
 	   doReturn(ldapAttrEnum).when (mockContext).search(anyString(), anyString(), any(SearchControls.class));
 	   doReturn(searchResult).when(ldapAttrEnum).next();
 	   when(ldapAttrEnum.hasMore()).thenReturn(true,false);
 	   doReturn(attr).when(searchResult).getAttributes();
 	   doReturn(mockAttr).when(attr).get(anyString());
 	   doReturn(group.getGroupName()).when(mockAttr).get(0);
       assertEquals("MSD",mockService.findAllGroups().get(0).getGroupName());
   }
    
    @Test
    public void testFindAllGroupsEmpty() throws NamingException {
 	   doReturn(ldapAttrEnum).when (mockContext).search(anyString(), anyString(), any(SearchControls.class));
 	   when(ldapAttrEnum.hasMore()).thenReturn(false);
 	   assertEquals(0,mockService.findAllGroups().size());
   }
     
    
   @Test
   public void testGroupByName() throws NamingException {
	   Group group = new Group("MSD");
	   doReturn(ldapAttrEnum).when (mockContext).search(anyString(), anyString(), any(SearchControls.class));
	   doReturn(searchResult).when(ldapAttrEnum).next();
	   when(ldapAttrEnum.hasMore()).thenReturn(true,false);
	   doReturn(attr).when(searchResult).getAttributes();
	   doReturn(mockAttr).when(attr).get(anyString());
	   doReturn(group.getGroupName()).when(mockAttr).get(0);
       assertEquals("MSD",mockService.findGroupByName("MSD").getGroupName());
  }
   
   @Test
   public void testGroupByNameEmpty() throws NamingException {
	   doReturn(ldapAttrEnum).when (mockContext).search(anyString(), anyString(), any(SearchControls.class));
	   when(ldapAttrEnum.hasMore()).thenReturn(false);
       assertEquals(null,mockService.findGroupByName("MSD"));
  } 
   
   @SuppressWarnings("unchecked")
   @Test
   void testForGroupByNameException() throws NamingException {
       doReturn(ldapAttrEnum).when (mockContext).search(anyString(), anyString(), any(SearchControls.class));
       when(ldapAttrEnum.hasMore()).thenReturn(false);
       when(mockService.findGroupByName("MSD")).thenThrow(Exception.class);
       assertEquals(null, mockService.findGroupByName("MSD"));
   }
   
   @Test
   public void testGroupsByUserName() throws NamingException {
	   Group group = new Group("MSD");
	   doReturn(ldapAttrEnum).when (mockContext).search(anyString(), anyString(), any(SearchControls.class));
	   doReturn(searchResult).when(ldapAttrEnum).next();
	   when(ldapAttrEnum.hasMore()).thenReturn(true,false);
	   when(ldapAttrEnum.nextElement()).thenReturn(group);
	   doReturn(attr).when(searchResult).getAttributes();
	   doReturn(mockAttr).when(attr).get(anyString());
	   doReturn(group.getGroupName()).when(mockAttr).get(0);
       assertEquals("MSD",mockService.findGroupsByUsername("farha").get(0).getGroupName());
  }
   
   @SuppressWarnings("unchecked")
   @Test
   void testForGroupByUsernameException() throws NamingException {
       doReturn(ldapAttrEnum).when (mockContext).search(anyString(), anyString(), any(SearchControls.class));
       when(ldapAttrEnum.hasMore()).thenReturn(false);
       when(mockService.findGroupsByUsername("MSD")).thenThrow(Exception.class);
       assertEquals(0, mockService.findGroupsByUsername("MSD").size());
   }
     
   @Test
   public void testForAddToGroup() throws NamingException {
       doReturn(null).when(mockContext).createSubcontext(anyString(), any(Attributes.class));
       User user = new User("farha");
       assertTrue(mockService.addToGroup("MSD", user));
   }
   
   
   @Test
   public void testForDeleteGroup() throws NamingException {
       Group group = new Group("MSD");
       mockService.createGroup(group);
       assertTrue(mockService.deleteGroup(group.getGroupName()));  
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testForDeleteGroupException() throws NamingException {
       Group group = new Group("MSD");
       when(mockService.deleteGroup(group.getGroupName())).thenThrow(Exception.class);
       assertFalse(mockService.deleteGroup(group.getGroupName()));
  }
   
   @Test
   public void testForDeleteFromGroup() throws NamingException {
       Group group = new Group("MSD");
       User user = new User("farha");
       mockService.createGroup(group);
       mockService.addToGroup("MSD", user);
       assertTrue(mockService.deleteFromGroup(group.getGroupName(), user));  
   } 
     
}
