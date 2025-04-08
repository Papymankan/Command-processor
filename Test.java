package ir.ac.kntu;

import java.security.Key;
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

	public static boolean isValidJSON(String json) {
		if (!json.startsWith("{") || !json.endsWith("}")) {
			return false;
		}

		json = json.substring(1, json.length() - 1);

		String[] pairs = json.split("},");
		for (int i = 0; i < pairs.length; i++) {
			if (!pairs[i].endsWith("}")) {
				pairs[i] += "}";
			}

			if (!pairs[i].matches("^\"[^\"]+\":\\{.*\\}$")) {
				return false;
			}

			int braceIndex = pairs[i].indexOf(":{");
			String inner = pairs[i].substring(braceIndex + 2, pairs[i].length() - 1);

			if (!inner.isEmpty()) {
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

	public static boolean isFlatJSONValid(String json) {
		json = json.replaceAll("\\s+", "");

		if (!json.startsWith("{") || !json.endsWith("}"))
			return false;

		json = json.substring(1, json.length() - 1);

		if (json.isEmpty())
			return true;

		String[] pairs = json.split(",");

		for (String pair : pairs) {
			if (!pair.matches("^\"[^\"]+\":(\"[^\"]*\"|true|false|\\d+\\.\\d+|\\d+)$")) {
				return false;
			}
		}

		return true;
	}

	public static void parseCommand(String input, Map<String, Map<String, Map<String, String>>> myTypes,
			Map<String, ArrayList<Map<String, Object>>> myTypesInstances) {
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

					if (!Parameter.equals("")) {
						ErrorMessage("Create Command does not accept any parameters !");
						break;
					}

					if (JSONInput.equals("")) {
						ErrorMessage("Create Command accepts a JSON Input !");
						break;
					}

					insertType(Type, JSONInput, myTypesInstances);

					break;
				case "update":

					break;
				case "delete":

					break;
				case "search":

					break;
			}

		} else {
			ErrorMessage("Command pattern is not valid !!");
		}

	}

	public static void createType(String Type, String JSONInput,
			Map<String, Map<String, Map<String, String>>> myTypes) {

		if (myTypes.containsKey(Type)) {
			ErrorMessage("The Type { " + Type + " } already exists");
		} else {

			JSONInput = JSONInput.substring(1, JSONInput.length() - 1).replace(" ", ""); // "key":{...},"key":{...}

			Map<String, Map<String, String>> map = new HashMap<>();

			String[] pairs = JSONInput.split("},");

			for (int i = 0; i < pairs.length; i++) {

				if (!pairs[i].endsWith("}")) {
					pairs[i] += "}";
				} // "key":{...}

				int braceIndex = pairs[i].indexOf(":{");
				String inner = pairs[i].substring(braceIndex + 2, pairs[i].length() - 1); // "type":"int","unique":false
				String Key = pairs[i].substring(1, braceIndex - 1); // id

				if (inner.equals("")) {
					ErrorMessage("Entered Object { " + Key + " } can not be empty !!");
					return;
				}

				if (map.containsKey(Key)) {
					ErrorMessage("The Key { " + Key + " } has been entered more than 1 time !!");
					return;
				}

				Map<String, String> InnerMap = new HashMap<>();
				Boolean isValid = true;
				String[] innerPairs = inner.split(","); // ["type":"int","unique":false]

				for (int j = 0; j < innerPairs.length; j++) {
					String[] innerInnerPairs = innerPairs[j].split(":"); // ["type","int"]

					String innerKey = innerInnerPairs[0].substring(1, innerInnerPairs[0].length() - 1);

					if (!innerKey.equals("type") && !innerKey.equals("unique") && !innerKey.equals("required")) {
						ErrorMessage("Key { " + innerKey + " } is not Valid !!  (type , unique , required)");
						isValid = false;
						return;
					}

					String innerValue = innerInnerPairs[1].startsWith("\"")
							? innerInnerPairs[1].substring(1, innerInnerPairs[1].length() - 1)
							: innerInnerPairs[1].substring(0, innerInnerPairs[1].length());

					InnerMap.put(innerKey, innerValue);

				}

				if (isValid)
					map.put(Key, InnerMap);

			}

			myTypes.put(Type, map);

		}

	}

	public static void insertType(String Type, String JSONInput,
			Map<String, ArrayList<Map<String, Object>>> myTypesInstances) {

		JSONInput = JSONInput.substring(1, JSONInput.length() - 1).replace(" ", "");

		if (!isFlatJSONValid(JSONInput)) {
			ErrorMessage("The Json Input is not valid !!");
			return;
		}

		Map<String, Object> arrayInnerObjects = new HashMap<>();

		if (!myTypesInstances.containsKey(Type)) {
			ErrorMessage("There is no { " + Type + " } Type created yet !!");
			return;
		}

		String[] pairs = JSONInput.split("},");

		for (int i = 0; i < pairs.length; i++) {
			if (!pairs[i].endsWith("}")) {
				pairs[i] += "}";
			}

			int braceIndex = pairs[i].indexOf(":{");
			String inner = pairs[i].substring(braceIndex, pairs[i].length());
			String Key = pairs[i].substring(1, braceIndex - 1);



		}
	}

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);

		String input = "";

		Map<String, Map<String, Map<String, String>>> myTypes = new HashMap<>();
		Map<String, ArrayList<Map<String, Object>>> myTypesInstances = new HashMap<>();

		while (true) {
			System.out.println(myTypes);
			input = scanner.nextLine();

			if (input.equals("exit"))
				break;

			parseCommand(input, myTypes, myTypesInstances);
		}

	}
}
