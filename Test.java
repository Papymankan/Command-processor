package ir.ac.kntu;

import java.util.*;

public class Test {

	public static void WarnningMessage(String input) {
		System.out.println("WARNNING ==> " + input);
	}

	public static void ErrorMessage(String input) {
		System.out.println("ERROR ==> " + input);
	}

	public static boolean isValidFormat(String input) {
		String regex = "^(\\S+)\\s(\\S+)(?:\\s*\\(\\s*[^()]+\\s*\\))?(?:\\s*\\{.*\\})?$";
		return input.matches(regex);
	}

	// public static boolean isValidJSON(String input) {
	// input = input.trim();

	// if (!input.startsWith("{") || !input.endsWith("}")) {
	// return false;
	// }

	// String jsonRegex =
	// "\\{\\s*(\"[^\"]+\"\\s*:\\s*(\"[^\"]*\"|\\d+(\\.\\d+)?|true|false|null|\\{[^{}]*\\}))\\s*(,\\s*\"[^\"]+\"\\s*:\\s*(\"[^\"]*\"|\\d+(\\.\\d+)?|true|false|null|\\{[^{}]*\\}))*\\s*\\}";

	// return input.matches(jsonRegex);
	// }

	public static boolean isValidJSON(String json) {
		// Must start and end with curly braces
		if (!json.startsWith("{") || !json.endsWith("}")) {
			return false;
		}

		// Remove the outermost braces
		json = json.substring(1, json.length() - 1);

		// Split top-level key-value pairs
		String[] pairs = json.split("},");
		for (int i = 0; i < pairs.length; i++) {
			// Add '}' back if it was removed by split
			if (!pairs[i].endsWith("}")) {
				pairs[i] += "}";
			}

			// Each pair should match this pattern: "key":{...}
			if (!pairs[i].matches("^\"[^\"]+\":\\{.*\\}$")) {
				return false;
			}

			// Extract inner object
			int braceIndex = pairs[i].indexOf(":{");
			String inner = pairs[i].substring(braceIndex + 2, pairs[i].length() - 1);

			if (!inner.isEmpty()) {
				// Split the inner object by commas
				String[] innerPairs = inner.split(",");

				for (String pair : innerPairs) {
					if (!pair.matches("^\"[^\"]+\":(true|false|\"[^\"]*\"|\\d+\\.\\d+|\\d+)$")) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public static void parseCommand(String input, Map<String, Map<String, List<String>>> myTypes) {
		String CommandType = "";
		String Type = "";
		String Parameter = "";
		String JSONInput = "";

		input = input.trim().replaceAll("\\s+", " ").toLowerCase();

		if (isValidFormat(input)) {
			CommandType = input.substring(0, input.indexOf(" "));
			input = input.substring(input.indexOf(" ") + 1);

			if (input.indexOf(" ") != -1) {
				Type = input.substring(0, input.indexOf(" "));
				input = input.substring(input.indexOf(" ") + 1);
			} else {
				Type = input.substring(0, input.length());
				input = "";
			}

			if (input.length() > 0 && input.charAt(0) == '(') {
				Parameter = input.substring(0, input.indexOf(")") + 1);
				input = input.substring(input.indexOf(")") + 2);
			}

			JSONInput = input;

			// System.out.println(
			// 		"CommandType = " + CommandType + ".\n" + "Type = " + Type + ".\n" + "Parameter = " + Parameter
			// 				+ ".\n" + "JSONInput = " + JSONInput + ".");

			switch (CommandType) {
				case "create":

					if (!Parameter.equals("")) {
						ErrorMessage("Create Command does not accept any parameters !");
						break;
					}

					if (JSONInput.equals("")) {
						ErrorMessage("Create Command accepts a JSON Input !");
						break;
					}

					createType(Type, JSONInput, myTypes);
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

	public static void createType(String Type, String JSONInput, Map<String, Map<String, List<String>>> myTypes) {

		if (myTypes.containsKey(Type)) {
			ErrorMessage("The Type { " + Type + " } already exists");
		} else {
			System.out.println(isValidJSON(JSONInput.replace(" ", "")));
		}

	}

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);

		String input = "";

		Map<String, Map<String, List<String>>> myTypes = new HashMap<>();

		while (true) {
			input = scanner.nextLine();

			if (input.equals("exit"))
				break;

			parseCommand(input, myTypes);
		}

	}
}





