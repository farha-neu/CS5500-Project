package edu.northeastern.ccs.im.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class flags messages with inappropriate content
 * Reference: https://github.com/souwoxi/Profanity/blob/master/src/BadWordFilter.java
 * @author Farha Jawed
 *
 */
public class ParentalControl {
	
	 
	    private Map<String, String[]> words = new HashMap<>();
	    
	    private int largestWordLength = 0;
	    
	    
	    /**
	     * It contructs parental control by loading file with inappropriate words.
	     * @throws IOException
	     */
	    public ParentalControl() throws IOException {
	    	  BufferedReader reader = 
	            		new BufferedReader
	            		(new InputStreamReader
	            				(new URL("https://docs.google.com/spreadsheets/d/e/2PACX-1vQDcW047XGt2GAyIfwsL343i7kUvnsColtzzQ4GIuuvVLiXHftfxPyJy7a8V0F2Wjpn_jxySc7sLgX-/pub?output=csv").openConnection().getInputStream()));
	            String line = "";
	            while((line = reader.readLine()) != null) {
	                loadWords(line);
	            }
	    }
	    
	     
	    
	    /**
	     * It maps an array of good words to a bad word in combination with which
	     * flag can be ignored.
	     * For example, it will ignore the slang Paki if it turns out that it's not a slang
	     * i.e. Pakistan
	     * @param line e.g. Paki,Pakistan
	     */

		private void loadWords(String line) {
			   String[] content = null;
			
			    content = line.split(",");
			  
			    if(content.length == 0) {
			        return;
			    }
			    String word = content[0];
			    String[] ignoreWithWords = new String[]{};
			    if(content.length > 1) {
			    	ignoreWithWords = content[1].split("_");
			    }

			    if(word.length() > largestWordLength) {
			        largestWordLength = word.length();
			    }
			    words.put(word.replaceAll(" ", ""), ignoreWithWords);		  
		}
	    
	    /**
	     * It removes leetspeak, converts to lower case and returns the list of bad words.
	     * @param input e.g. 5h!t
	     * @return list of bad words e.g. [shit]
	     */
	    protected List<String> badWordsFound(String input) {
	        if(input == null) {
	            return new ArrayList<>();
	        }
	        input = input.replaceAll("1","i");
	        input = input.replaceAll("!","i");
	        input = input.replaceAll("3","e");
	        input = input.replaceAll("4","a");
	        input = input.replaceAll("@","a");
	        input = input.replaceAll("5","s");
	        input = input.replaceAll("7","t");
	        input = input.replaceAll("0","o");
	        input = input.replaceAll("9","g");
	        input = input.toLowerCase().replaceAll("[^a-zA-Z]", "");
	        return extractBadWords(input);
	    }

	    /**
	     * It iterates over a string input,checks whether an inappropriate word is found for
	     * all the possible combinations of words within a string(instead of looping
	     * over the entire list of bad words) and then
	     * checks whether the word should be ignored. If the word is not to be
	     * ignored, it adds to the list of bad words.
	     * @param input e.g. how to kill
	     * @return [howtokill]
	     */
		private ArrayList<String> extractBadWords(String input) {
			ArrayList<String> badWords = new ArrayList<>();
	        for(int start = 0; start < input.length(); start++) {
	            for(int offset = 1; offset < (input.length()+1 - start) && offset < largestWordLength; offset++)  {
	                String wordToCheck = input.substring(start, start + offset);
	                if(words.containsKey(wordToCheck)) {
	                    addBadWordsToList(input, badWords, wordToCheck);
	                }
	            }
	        }
	        return badWords;   
		}

		/**
		 * Helper function for determining whether a word should be ignored and adding bad word to a list
		 * @param input
		 * @param badWords
		 * @param wordToCheck
		 */
		private void addBadWordsToList(String input, ArrayList<String> badWords, String wordToCheck) {
			String[] ignoreCheck = words.get(wordToCheck);
			boolean ignore = false;
			for(int s = 0; s < ignoreCheck.length; s++ ) {
			    if(input.contains(ignoreCheck[s])) {
			        ignore = true;
			        break;
			    }
			}
			if(!ignore) {
			    badWords.add(wordToCheck);
			}
		}

		
	   /**
	    * It determines whether a bad word is found
	    * @param input
	    * @return true if bad word found
		* @throws IOException 
		* @throws MalformedURLException 
	    */
	    public boolean hasBadWord(String input) {
	        ArrayList<String> badWords = (ArrayList<String>) badWordsFound(input);
	        return !badWords.isEmpty();
	   }

}
