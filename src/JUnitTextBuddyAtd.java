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
