package edu.northeastern.ccs.im.dao;
/**
 *
 * @author Pratik Devikar
 */

import java.util.List;

import javax.naming.NamingException;

public interface UserRepository {
	
	public boolean createUser(User user);
	
	public List<User> findAllUsers() throws NamingException;
	
	public User findUserByUsername(String username);
	
	public boolean login(User user);
	
	public boolean updateUser(User user) throws NamingException;
	
	public boolean deleteUserByUsername(String username) throws NamingException;
	
	

}
