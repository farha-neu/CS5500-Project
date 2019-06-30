package edu.northeastern.ccs.im.server;

import static org.junit.jupiter.api.Assertions.assertEquals;


import java.io.IOException;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests for parental control features of messages
 * @author farha
 *
 */


public class ParentalControlTest {
	
	static ParentalControl parentalControl;
	
	
	@BeforeAll
    public static void loadFile() throws IOException{
        parentalControl = new ParentalControl();
    }
	 

	/**
	 * Test for bad word assertion
	 * @throws IOException
	 */
	@Test
	void testForInappropriateWordFound() throws IOException {
		assertEquals(true,parentalControl.hasBadWord("how to kill"));	
	}
	
	/**
	 * Test for word which is not bad
	 * @throws IOException
	 */
	@Test
	void testForInappropriateWordNotFound() throws IOException {
		assertEquals(false,parentalControl.hasBadWord("good to be here"));	
	}
	
	/**
	 * Test for bad word being ignored when combined with not a bad word
	 * @throws IOException
	 */
	@Test
	void testForNotInappropriateWordWhenCombined() throws IOException {
		assertEquals(false,parentalControl.hasBadWord("assassination"));	
	
	}
	
	/**
	 * Test for removal of leetspeak. Here 5h!t is converted in Shit
	 * and considered as bad word.
	 * @throws IOException
	 */
	@Test
	void testForRemovalOfLeetspeak() throws IOException {
		assertEquals(true,parentalControl.hasBadWord("5h!t"));	
	}
	
	
	/**
	 * Test for null output in badWordsFound
	 * @throws IOException
	 */
	@Test
	void testForNullBadWords() throws IOException {
		assertEquals(new ArrayList<>(),parentalControl.badWordsFound(null));	
	}
	
	
	
	
	
	
	
	
	
	
	

}
