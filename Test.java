package ir.ac.kntu;

import java.util.*;

public class Test {

	public static boolean isValidFormat(String input) {
		String regex = "^(\\S+)\\s(\\S+)(\\s\\(.*?\\))?\\s*\\{.*\\}$";
		return input.matches(regex);
	}

	public static void parseCommand(String input) {
		String CommandType = "";
		String Type = "";
		String Parameter = null;
		String JSONInput = "";

		input = input.trim().replaceAll("\\s+", " ").toLowerCase();

		if (isValidFormat(input)) {
			CommandType = input.substring(0, input.indexOf(" "));
			input = input.substring(input.indexOf(" ") + 1);
			Type = input.substring(0, input.indexOf(" "));
			input = input.substring(input.indexOf(" ") + 1);

			if (input.charAt(0) == '(') {
				Parameter = input.substring(0, input.indexOf(")") + 1);
				input = input.substring(input.indexOf(")") + 2);
			}

			JSONInput = input;

			System.out.println(
					"CommandType = " + CommandType + ".\n" + "Type = " + Type + ".\n" + "Parameter = " + Parameter
							+ ".\n" + "JSONInput = " + JSONInput + ".");

			switch (CommandType) {
				case "create":

					break;
				case "insert":

					break;
				case "update":

					break;
				case "delete":

					break;
				case "search":

					break;
			}

		} else {
			System.out.println("ERROR");
		}

	}

	public static void createType(String Type , String JSONInput) {
		
	}

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);

		String input = "";

		Map<String, Map<String, List<String>>> myTypes = new HashMap<>();

		while (true) {
			input = scanner.nextLine();

			if (input.equals("exit"))
				break;

			parseCommand(input);
		}

	}
}
