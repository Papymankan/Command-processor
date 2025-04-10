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

	public static String checkNumberType(String s) {
		if (s.matches("^-?\\d+$")) {
			return "int";
		} else if (s.matches("^-?\\d+\\.\\d+$")) {
			return "dbl";
		} else {
			return "none";
		}
	}

	public static void parseCommand(String input, Map<String, Map<String, Map<String, Object>>> myTypes,
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

					createType(Type, JSONInput, myTypes, myTypesInstances);
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

					insertType(Type, JSONInput, myTypes, myTypesInstances);

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
			Map<String, Map<String, Map<String, Object>>> myTypes,
			Map<String, ArrayList<Map<String, Object>>> myTypesInstances) {

		if (myTypes.containsKey(Type)) {
			ErrorMessage("The Type { " + Type + " } already exists");
		} else {

			JSONInput = JSONInput.substring(1, JSONInput.length() - 1).replace(" ", ""); // "key":{...},"key":{...}

			Map<String, Map<String, Object>> map = new HashMap<>();

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

				Map<String, Object> InnerMap = new HashMap<>();
				String[] innerPairs = inner.split(","); // ["type":"int","unique":false]

				for (int j = 0; j < innerPairs.length; j++) {
					String[] innerInnerPairs = innerPairs[j].split(":"); // ["type","int"]

					String innerKey = innerInnerPairs[0].substring(1, innerInnerPairs[0].length() - 1);

					if (!innerKey.equals("type") && !innerKey.equals("unique") && !innerKey.equals("required")) {
						ErrorMessage("Key { " + innerKey + " } is not Valid !!  (type , unique , required)");
						return;
					}

					if (innerInnerPairs[1].startsWith("\"")) {
						String str = innerInnerPairs[1].substring(1, innerInnerPairs[1].length() - 1);

						if (str.equals("int") || str.equals("dbl") || str.equals("string") || str.equals("bool")) {

							InnerMap.put(innerKey, str);
						}

					} else if (innerInnerPairs[1].equals("true")) {
						InnerMap.put(innerKey, true);
					} else if (innerInnerPairs[1].equals("false")) {
						InnerMap.put(innerKey, false);
					} else {
						ErrorMessage("Something went wrong !!!");
						return;
					}

				}

				if (InnerMap.containsKey("type"))
					map.put(Key, InnerMap);
				else {
					ErrorMessage("{ Type } field is required for each type keys !!");
					return;
				}

			}

			myTypes.put(Type, map);

			myTypesInstances.put(Type, new ArrayList<Map<String, Object>>());

		}

	}

	public static void insertType(String Type, String JSONInput, Map<String, Map<String, Map<String, Object>>> myTypes,
			Map<String, ArrayList<Map<String, Object>>> myTypesInstances) {

		if (!myTypes.containsKey(Type)) {
			ErrorMessage("There is no { " + Type + " } type !!");
			return;
		}

		if (!isFlatJSONValid(JSONInput.replace(" ", ""))) {
			ErrorMessage("The Json Input is not valid !!");
			return;
		}

		JSONInput = JSONInput.substring(1, JSONInput.length() - 1).replace(" ", "");

		Map<String, Object> arrayInnerObjects = new HashMap<>();

		String[] pairs = JSONInput.split(",");

		for (int i = 0; i < pairs.length; i++) {

			int braceIndex = pairs[i].indexOf(":");
			String inner = pairs[i].substring(braceIndex + 1, pairs[i].length());
			String Key = pairs[i].substring(1, braceIndex - 1);

			if (inner.startsWith("\"")) {
				arrayInnerObjects.put(Key, inner.substring(1, inner.length() - 1));
			} else if (inner.equals("true")) {
				arrayInnerObjects.put(Key, true);
			} else if (inner.equals("false")) {
				arrayInnerObjects.put(Key, false);

			} else {
				switch (checkNumberType(inner.substring(0, inner.length()))) {
					case "int":
						arrayInnerObjects.put(Key, Integer.parseInt(inner.substring(0, inner.length())));
						break;
					case "dbl":
						arrayInnerObjects.put(Key, Double.parseDouble(inner.substring(0, inner.length())));
						break;
					case "none":
						ErrorMessage("Something went wrong !!");
						return;
				}
			}
		}

		Map<String, Map<String, Object>> typeFields = myTypes.get(Type);

		for (Map.Entry<String, Map<String, Object>> fieldEntry : typeFields.entrySet()) {
			String fieldName = fieldEntry.getKey();
			Map<String, Object> attributes = fieldEntry.getValue();

			if (attributes.containsKey("required") && attributes.get("required").equals(true)) {
				if (!arrayInnerObjects.containsKey(fieldName)) {
					ErrorMessage("The { " + fieldName + " } key is required !");
					return; // here
				}

			}

			if (arrayInnerObjects.containsKey(fieldName)) {
				switch ((String) attributes.get("type")) {
					case "string":
						if (!(arrayInnerObjects.get(fieldName) instanceof String)) {
							ErrorMessage("The " + fieldName + " value should be String !! ");
							return;
						}
						break;

					case "int":
						if (!(arrayInnerObjects.get(fieldName) instanceof Integer)) {
							ErrorMessage("The " + fieldName + " value should be  Integer !! ");
							return;
						}
						break;

					case "dbl":
						if (!(arrayInnerObjects.get(fieldName) instanceof Double)) {
							ErrorMessage("The " + fieldName + " value should be Double !! ");
							return;
						}
						break;
					case "bool":
						if (!(arrayInnerObjects.get(fieldName) instanceof Boolean)) {
							ErrorMessage("The type key's value should be Boolean !! ");
							return;
						}
						break;

					default:
						break;
				}

				if (attributes.containsKey("unique") && attributes.get("unique").equals(true)) {
					for (int i = 0; i < myTypesInstances.get(Type).size(); i++) {
						if (myTypesInstances.get(Type).get(i).containsKey(fieldName)
								&& arrayInnerObjects.containsKey(fieldName) && myTypesInstances.get(Type)
										.get(i).get(fieldName).equals(arrayInnerObjects.get(fieldName))) {
							ErrorMessage("The { " + fieldName + " } value must be unique !!");
							return;
						}
					}

				}

			} else {
				switch ((String) attributes.get("type")) {
					case "string":
						arrayInnerObjects.put(fieldName, "");
						break;
					case "int":
						arrayInnerObjects.put(fieldName, 0);
						break;

					case "dbl":
						arrayInnerObjects.put(fieldName, 0.0);
						break;
					case "bool":
						arrayInnerObjects.put(fieldName, false);
						break;

					default:
						break;
				}
			}
		}

		myTypesInstances.get(Type).add(arrayInnerObjects);
	}

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);

		String input = "";

		Map<String, Map<String, Map<String, Object>>> myTypes = new HashMap<>();
		Map<String, ArrayList<Map<String, Object>>> myTypesInstances = new HashMap<>();

		while (true) {
			System.out.println("Type => " + myTypes);
			System.out.println("Instances => " + myTypesInstances);
			input = scanner.nextLine();

			if (input.equals("exit"))
				break;

			parseCommand(input.trim(), myTypes, myTypesInstances);
		}

	}
}
