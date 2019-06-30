package edu.northeastern.ccs.im;

import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.Hashtable;

public class GroupPartition {
    public static void main(String[] args) {

        // Set up the environment for creating the initial context
        Hashtable<String, Object> env = new Hashtable<String, Object>(11);
        env
                .put(Context.INITIAL_CONTEXT_FACTORY,
                        "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://ec2-13-58-39-65.us-east-2.compute.amazonaws.com:10389/dc=example,dc=com");

        try {
            // Create the initial context
            DirContext ctx = new InitialDirContext(env);

            // Create attributes to be associated with the new context
            Attributes attrs = new BasicAttributes(true); // case-ignore
            Attribute objclass = new BasicAttribute("objectclass");
            objclass.add("top");
            objclass.add("organizationalUnit");
            attrs.put(objclass);

            // Create the context
            Context result1 = ctx.createSubcontext("ou = users", attrs);
            Context result2 = ctx.createSubcontext("ou = groups", attrs);

            // Check that it was created by listing its parent
            NamingEnumeration list = ctx.list("");

            // Go through each item in list
            while (list.hasMore()) {
                NameClassPair nc = (NameClassPair) list.next();
                System.out.println(nc);
            }

            // Close the contexts when we're done
            result1.close();
            result2.close();
            ctx.close();
        } catch (NamingException e) {
//            e.printStackTrace();
            System.out.println("Create failed: " + e);
        }
    }

}
