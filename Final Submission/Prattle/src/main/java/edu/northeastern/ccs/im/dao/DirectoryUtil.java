package edu.northeastern.ccs.im.dao;

/**
 * LDAP Database Server Connection
 * @author Farha Jawed
 */

import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

class DirectoryUtil {
    /**
     * Constants for the Server Connection
     */
    private static final String FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
    private static final String PROVIDER_URL = "ldap://localhost:10389";
    private static DirContext context = null;

    private DirectoryUtil() {}

    /**
     * Creates a database context using the declared server constants
     * @return LDAP Database context
     * @throws NamingException
     */
    public static DirContext getContext() throws NamingException {
        try {
            Properties properties = new Properties();
            properties.put(Context.INITIAL_CONTEXT_FACTORY, FACTORY);
            properties.put(Context.PROVIDER_URL, PROVIDER_URL);
            return new InitialDirContext(properties);
        }
        catch (Exception e){
            return context;
        }
    }
}