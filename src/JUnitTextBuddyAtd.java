import static org.junit.Assert.*;

import org.junit.Test;

public class JUnitTextBuddyAtd {
	
	@Test
	public void testSort() {
		String[] args = { "sample.txt" };
		
		testPreProcess(args);
		
		testCommand("sort", "1. first line\n2. forth line"
				    + "\n3. second line\n4. third line\n5. WOW OMG");	
	}
	
	@Test
	public void testSortEmpty() {
		String[] args = { "sample.txt" };
		
		TextBuddy.getAndValidateFilename(args);
		TextBuddy.initialize();
		
		testCommand("sort", "sample.txt is empty");	
	}
	
	@Test
	public void testSearch() {
		String[] args = { "sample.txt" };
		
		testPreProcess(args);
		
		testCommand("search line", "1. first line\n2. second line"
				    + "\n3. third line\n4. forth line");	
	}

	@Test
	public void testSearchOneResult() {
		String[] args = { "sample.txt" };
		
		testPreProcess(args);
		
		testCommand("search first", "1. first line");	
	}
	
	@Test
	public void testSearchNoResult() {
		String[] args = { "sample.txt" };
		
		testPreProcess(args);
		
		testCommand("search blabla", "No item contains \"blabla\"");	
	}
	
	private void testCommand(String command, String expected) {
		String feedback = TextBuddy.executeCommand(command);
		assertEquals(expected, feedback);
	}
	
	private void testPreProcess(String[] args) {
		TextBuddy.getAndValidateFilename(args);
		TextBuddy.initialize();
		
		TextBuddy.executeCommand("add first line");
		TextBuddy.executeCommand("add second line");
		TextBuddy.executeCommand("add third line");
		TextBuddy.executeCommand("add forth line");
		TextBuddy.executeCommand("add WOW OMG");
	}
}
