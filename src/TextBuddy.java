import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
	command: delete 2
	deleted from mytextfile.txt: “jumped over the moon”
	command: display
	1. little brown fox
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
	private static final String MESSAGE_WELCOME = "Welcome to TextBuddy. %1s is ready for use";
	private static final String MESSAGE_ADDED = "added to %1s: \"%2s\"";
	private static final String MESSAGE_ADD_EMPTY = "Cannot add empty message";
	private static final String MESSAGE_DELETED = "deleted from %1s: \"%2s\"";
	private static final String MESSAGE_DELETE_NOT_EXIST = "this line does not exist";
	private static final String MESSAGE_CLEARED = "all content deleted from %1s";
	private static final String MESSAGE_EMPTY = "%1s is empty";
	private static final String MESSAGE_INVALID_FORMAT = "invalid command format: %1$s";
	private static final String MESSAGE_NO_ARGUMENT = "We expect one argument";
	private static final String MESSAGE_WRONG_ARGUMENT_FORMAT = "Arugment must end with .txt";
	
	private static final String COMMAND = "command:";
	
	private static final int EXTENSION_LENGTH = 4;

	private static Scanner scanner = new Scanner(System.in);
	private static BufferedWriter writer = null;

	private static String fileName = null;
	
	// This array list will be used to store the content added
	private static ArrayList<String> content_list = new ArrayList<String>();
	
	// These are the possible command types
	enum COMMAND_TYPE {
		ADD, DISPLAY, DELETE, CLEAR, INVALID, EXIT
	};
	
	public static void main(String[] args){
		getAndValidateFilename(args);
		createFile();
		showToUser(generateWelcomeMessage());
		run();
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
			fileName = args[0];
		}
	}
	
	/**
	 * Create or overwirte file named as fileName in this class
	 */
	static void createFile() {
		try {
			File file = new File(fileName);
			if(!file.exists()) {
				file.createNewFile();
			}
			writer = new BufferedWriter(new FileWriter(file));
		} catch (IOException e) {
			abort(e.getMessage());
		}
	}
	
	private static void showToUser(String text) {
		System.out.println(text);
	}
	
	private static void waitForCommand() {
		System.out.print(COMMAND);
	}
	
	/**
	 * Execute the program until exit command is entered
	 */
	private static void run() {
		String command, feedback;
		while(true) {
			waitForCommand();
			command = scanner.nextLine();
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
	 * Add a new entry to content_list if the argument is not empty
	 */
	private static String processAddCommand(String args) {
		if(args == null || args.equals("")) {
			return MESSAGE_ADD_EMPTY;
		}
		content_list.add(args);
		return String.format(MESSAGE_ADDED, fileName, args);
	}
	
	/**
	 * Show the current content in content_list if the list is not empty
	 */
	private static String processDisplayCommand() {
		if(content_list == null || content_list.size() < 1) {
			return String.format(MESSAGE_EMPTY, fileName);
		} else {
			return generateContentList();
		}
	}

	/**
	 * Delete an entry based on the argument, if there is a valid argument
	 */
	private static String processDeleteCommand(String arg) {
		int indexToDelete = Integer.parseInt(arg) - 1;
		String deletedContent;
		if(content_list == null || indexToDelete < 0 || indexToDelete >= content_list.size()) {
			return MESSAGE_DELETE_NOT_EXIST;
		} else {
			deletedContent = content_list.get(indexToDelete);
			content_list.remove(indexToDelete);
			return String.format(MESSAGE_DELETED, fileName, deletedContent);
		}
	}
	
	/**
	 * Clear the content_list
	 */
	private static String processClearCommand() {
		content_list.clear();
		return String.format(MESSAGE_CLEARED, fileName);
	}
	
	/**
	 * Write the content into the created file, stop running
	 */
	private static String processExitCommand() {
		try {
			if(content_list != null && content_list.size() > 0) {
				writer.write(generateContentList());
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
//		System.exit(1);
		Thread.currentThread().interrupt();
	}
	
	private static String removeFirstWord(String userCommand) {
		return userCommand.replace(getFirstWord(userCommand), "").trim();
	}

	private static String getFirstWord(String command) {
		return command.trim().split("\\s+")[0];
	}

	/**
	 * Organize the content stored in this class, give each entry an index
	 */
	private static String generateContentList() {
		String text = "";
		int i;
		
		for(i=1; i<content_list.size(); i++) {
			text += i + ". " + content_list.get(i-1) + "\n";
		}
		text += i + ". " + content_list.get(i-1); 	
		return text;
	}
}
