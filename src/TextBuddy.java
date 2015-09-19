import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

/**
 * This class is used to manipulate text in a file.
 * The command format is given by the example interaction below:

	Welcome to TextBuddy. mytextfile.txt is ready for use
	command: add little brown fox
	added to mytextfile.txt: “little brown fox”
	command: display
	1. little brown fox
	command: add jumped over the moon
	added to mytextfile.txt: “jumped over the moon”
	command: display
	1. little brown fox
	2. jumped over the moon
	command: sort
	1. jumped over the moon
	2. little brown fox
	command: search over
	1. jumped over the moon
	command: display
	1. jumped over the moon
	2. little brown fox
	command: delete 2
	deleted from mytextfile.txt: “little brown fox”
	command: display
	1. jumped over the moon
	command: clear
	all content deleted from mytextfile.txt
	command: display
	mytextfile.txt is empty
	command: exit
	
 * Assumptions: 
 * 1. Every time we execute TextBuddy, it will create a new file named by the input, 
 * 	  if the file of this name exists, it will overwrite it.
 * 2. The content will only be written to the file when executing exit command.
 * 3. When creating the file, it will ignore extra arguments.
 * 4. If enter an invalid command, it shows error message but the program won't exit.
 * 5. The content written to the file will include the index of line.
 * 

 * @author Liu Yang
 */
public class TextBuddy {
	
	// Messages constants used to display to user
	private static final String MESSAGE_WELCOME = "Welcome to TextBuddy. %1s is ready for use";
	private static final String MESSAGE_ADDED = "added to %1s: \"%2s\"";
	private static final String MESSAGE_ADD_EMPTY = "Cannot add empty message";
	private static final String MESSAGE_NO_SEARCH_RESULT = "No item contains \"%1s\"";
	private static final String MESSAGE_DELETED = "deleted from %1s: \"%2s\"";
	private static final String MESSAGE_DELETE_NOT_EXIST = "this line does not exist";
	private static final String MESSAGE_CLEARED = "all content deleted from %1s";
	private static final String MESSAGE_EMPTY = "%1s is empty";
	private static final String MESSAGE_INVALID_FORMAT = "invalid command format: %1s";
	private static final String MESSAGE_NO_ARGUMENT = "We expect one argument";
	private static final String MESSAGE_WRONG_ARGUMENT_FORMAT = "Argument must end with .txt";
	private static final String MESSAGE_WAIT_COMMAND = "command:";
	
	// This is the required length of the argument's extension name, which is .txt
	private static final int EXTENSION_LENGTH = 4;

	// These are variables used to read command and write to file
	private static Scanner scanner = new Scanner(System.in);
	private static BufferedWriter writer = null;

	// This is declared for the whole class because it is used by more than one methods
	private static String fileName = null;
	
	// This array list will be used to store the content added
	private static ArrayList<String> contentList = new ArrayList<String>();
	
	// These are the possible command types
	enum COMMAND_TYPE {
		ADD, DISPLAY, SORT, SEARCH, DELETE, CLEAR, INVALID, EXIT
	};
	
	public static void main(String[] args){
		getAndValidateFilename(args);
		initialize();
		showToUser(generateWelcomeMessage());
		execute();
	}

	/**
	 * This operation does: 
		 1. abort the program if no argument given or
		    the input file name is not of .txt format
	 *   2. store the first argument if valid
	 * 
	 * @param the arguments of the entire program
	 */
	static void getAndValidateFilename(String[] args) {
		if(args.length < 1) {
			abort(MESSAGE_NO_ARGUMENT);
		} else if(!validFileName(args[0])) {
			abort(MESSAGE_WRONG_ARGUMENT_FORMAT);
		} else {
			// set the class variable after ensuring it is valid
			fileName = args[0];
		}
	}
	
	static void initialize() {
		createFile();
		// empty the list that storing content
		contentList = new ArrayList<String>();
	}
	
	private static void showToUser(String text) {
		System.out.println(text);
	}
	
	/**
	 * Execute the program until exit command is entered
	 */
	private static void execute() {
		String command, feedback;
		while(true) {
			waitForCommand();
			command = readCommand();
			feedback = executeCommand(command);
			showToUser(feedback);
		}
	}

	/**
	 * Check whether this name ends with .txt
	 */
	private static boolean validFileName(String fileName) {
		if(fileName.length() <= EXTENSION_LENGTH) {
			return false;
		}
		String extensionName = fileName.substring(fileName.length() - EXTENSION_LENGTH);
		return extensionName.equalsIgnoreCase(".txt");
	}
	
	/**
	 * Create or overwirte file named as fileName in this class
	 */
	private static void createFile() {
		try {
			File file = new File(fileName);
			if(!file.exists()) {
				// if file with this name exists, overwrite this file, starting from empty
				file.createNewFile();
			}
			// initialize a writer to the created file
			writer = new BufferedWriter(new FileWriter(file));
		} catch (IOException e) {
			// stop running if error occur during creating the file
			abort(e.getMessage());
		}
	}
	
	/**
	 * Ask user to type in command, this is different from showToUser since it does not
	 * create a new line
	 */
	private static void waitForCommand() {
		System.out.print(MESSAGE_WAIT_COMMAND);
	}
	
	private static String readCommand() {
		return scanner.nextLine();
	}

	/**
	 * Parse the command, and then execute command based on its type
	 */
	static String executeCommand(String command) {
		if (command.trim().equals("")) {
			return String.format(MESSAGE_INVALID_FORMAT, command);
		}
		
		String commandTypeString = getFirstWord(command), 
			   commandArgs = removeFirstWord(command),
			   feedback;
		
		COMMAND_TYPE commandType = determineCommandType(commandTypeString);
				
		switch (commandType) {
			case ADD:
				feedback = processAddCommand(commandArgs);
				break;
			case DISPLAY:
				feedback = processDisplayCommand();
				break;
			case SORT:
				feedback = processSortCommand();
				break;
			case SEARCH:
				feedback = processSearchCommand(commandArgs);
				break;
			case DELETE:
				feedback = processDeleteCommand(commandArgs);
				break;
			case CLEAR:
				feedback = processClearCommand();
				break;
			case EXIT:
				feedback = processExitCommand();
				break;
			default: 
				feedback = processInvalidCommand(command);
		}
		return feedback;
	}
	
	/**
	 * This operation determines which of the supported command types the user
	 * wants to perform
	 * 
	 * @param commandTypeString is the first word of the user command
	 */
	private static COMMAND_TYPE determineCommandType(String commandTypeString) {

		if (commandTypeString.equalsIgnoreCase("add")) {
			return COMMAND_TYPE.ADD;
		}
		
		if (commandTypeString.equalsIgnoreCase("display")) {
			return COMMAND_TYPE.DISPLAY;
		} 
		
		if (commandTypeString.equalsIgnoreCase("sort")) {
			return COMMAND_TYPE.SORT;
		} 
		
		if (commandTypeString.equalsIgnoreCase("search")) {
			return COMMAND_TYPE.SEARCH;
		} 
		
		if (commandTypeString.equalsIgnoreCase("delete")) {
		 	return COMMAND_TYPE.DELETE;
		}
		
		if (commandTypeString.equalsIgnoreCase("clear")) {
		 	return COMMAND_TYPE.CLEAR;
		}
		
		if (commandTypeString.equalsIgnoreCase("exit")) {
		 	return COMMAND_TYPE.EXIT;
		}
		
		return COMMAND_TYPE.INVALID;
	}
	
	/**
	 * Add a new entry to contentList if the argument is not empty
	 */
	private static String processAddCommand(String args) {
		if(args == null || args.equals("")) {
			return MESSAGE_ADD_EMPTY;
		}
		contentList.add(args);
		return String.format(MESSAGE_ADDED, fileName, args);
	}
	
	/**
	 * Show the current content in contentList if the list is not empty
	 */
	private static String processDisplayCommand() {
		if(contentList == null || contentList.size() < 1) {
			return String.format(MESSAGE_EMPTY, fileName);
		} else {
			return generateContentList(contentList);
		}
	}
	
	/**
	 * Sort the list alphabetically, and show the sorted list
	 */
	private static String processSortCommand() {
		if(contentList == null || contentList.size() < 1) {
			return String.format(MESSAGE_EMPTY, fileName);
		} else {
			Collections.sort(contentList, new StringIgnoreCaseComparator());
			return generateContentList(contentList);
		}
	}
	
	/**
	 * Search all the items that contain the input word
	 */
	private static String processSearchCommand(String commandArgs) {
		ArrayList<String> searchResult = new ArrayList<String>();
		
		if(contentList != null) {
			filterByKeyword(searchResult, commandArgs);
		}
		
		if(searchResult.size() < 1) {
			return String.format(MESSAGE_NO_SEARCH_RESULT, commandArgs);
		} else {
			return generateContentList(searchResult);
		}
	}

	/**
	 * Delete an entry based on the argument, if there is a valid argument
	 */
	private static String processDeleteCommand(String arg) {
		int indexToDelete = Integer.parseInt(arg) - 1;
		String deletedContent;
		
		if(contentList == null || indexToDelete < 0 || indexToDelete >= contentList.size()) {
			return MESSAGE_DELETE_NOT_EXIST;
		} else {
			deletedContent = contentList.get(indexToDelete);
			contentList.remove(indexToDelete);
			return String.format(MESSAGE_DELETED, fileName, deletedContent);
		}
	}
	
	/**
	 * Clear the contentList
	 */
	private static String processClearCommand() {
		contentList.clear();
		return String.format(MESSAGE_CLEARED, fileName);
	}
	
	/**
	 * Write the content into the created file, stop running
	 */
	private static String processExitCommand() {
		try {
			if(contentList != null && contentList.size() > 0) {
				writer.write(generateContentList(contentList));
			}
			writer.close();
			System.exit(0);
		} catch (IOException e) {
			abort(e.getMessage());
		}
		return "";
	}
	
	/**
	 * Indicate that the command cannot be processed
	 */
	private static String processInvalidCommand(String command) {
		return String.format(MESSAGE_INVALID_FORMAT, command);
	}
	
	private static String generateWelcomeMessage() {
		return String.format(MESSAGE_WELCOME, fileName);
	}
		
	/**
	 * Stop running the program can show error message
	 */
	private static void abort(String errorMessage) {
		showToUser(errorMessage);
		System.exit(1);;
	}
	
	private static String removeFirstWord(String userCommand) {
		return userCommand.replace(getFirstWord(userCommand), "").trim();
	}

	private static String getFirstWord(String command) {
		return command.trim().split("\\s+")[0];
	}

	/**
	 * Organize the content in the input list, give each entry an index
	 */
	private static String generateContentList(ArrayList<String> list) {
		String text = "";
		int contentSize = list.size();
				
		for(int i = 1; i < contentSize; i ++) {
			text += i + ". " + list.get(i - 1) + "\n";
		}
		if(contentSize > 0) {
			text += contentSize + ". " + list.get(contentSize - 1); 
		}
		return text;
	}
	
	/**
	 * Filter the content list by the given keyword
	 */
	private static ArrayList<String> filterByKeyword(ArrayList<String> searchResult, 
			String keyword) {
		
		int contentSize = contentList.size();
		String currentItem, lowerCaseContent;
		String lowerCaseKeyword = keyword.toLowerCase();
		
		for(int i = 0; i < contentSize; i ++) {
			currentItem = contentList.get(i);
			lowerCaseContent = currentItem.toLowerCase();
			
			if(lowerCaseContent.contains(lowerCaseKeyword)) {
				searchResult.add(currentItem);
			}
		}
		return searchResult;
	}
	
	/**
	 * This private nested class is used to define the way to compare to strings
	 * The default sorting is case sensitive, for example, sort([a, Z]) -> ([Z, a]),
	 * which is unexpected, so I defined this comparator to override the default one.
	 */
    private static class StringIgnoreCaseComparator implements Comparator<String> {
        public int compare(String s1, String s2) {
            return s1.toLowerCase().compareTo(s2.toLowerCase());
        }
    }
}
