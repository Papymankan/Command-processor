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
			return false;

		String[] pairs = json.split(",");

		for (String pair : pairs) {
			if (!pair.matches("^\"[^\"]+\":(\"[^\"]*\"|true|false|\\d+\\.\\d+|\\d+)$")) {
				return false;
			}
		}

		return true;
	}

	public static boolean isAlphaNumUnderscore(String input) {
		return input.matches("^[a-zA-Z0-9_]+$");
	}

	public static Object[] isValidParameter(String input) {
		input = input.trim().replaceAll("\\s+", "");

		Object[] result = new Object[2];
		result[0] = false;
		result[1] = "none";

		if (input.equals("")) {
			return result;
		}

		String regex = "^([a-zA-Z_][a-zA-Z0-9_]*)\\s*(=|<|>)\\s*(.+)$";
		if (!input.matches(regex))
			return result;

		String[] parts = input.split("(=|<|>)");
		if (parts.length != 2)
			return result;

		String operator = input.replaceAll("^[^=<>]*(=|<|>).*$", "$1");
		String value = parts[1].trim();

		switch (operator) {
			case "=":
				if (value.matches("^\"[^\"]*\"$")) {
					result[0] = true;
					result[1] = "string";
				} else if (value.equals("true") || value.equals("false")) {
					result[0] = true;
					result[1] = "boolean";
				} else if (value.matches("^-?\\d+$")) {
					result[0] = true;
					result[1] = "int";
				} else if (value.matches("^-?\\d+\\.\\d+$")) {
					result[0] = true;
					result[1] = "dbl";
				}
				break;

			case "<":
			case ">":
				if (value.matches("^-?\\d+$")) {
					result[0] = true;
					result[1] = "int";
				} else if (value.matches("^-?\\d+\\.\\d+$")) {
					result[0] = true;
					result[1] = "dbl";
				}
				break;
		}

		return result;

	}

	public static Map<String, Object> parseFlatJsonToHash(String JSONInput, String Type,
			Map<String, Map<String, Map<String, Object>>> myTypes) {
		JSONInput = JSONInput.substring(1, JSONInput.length() - 1).replace(" ", "");

		Map<String, Object> arrayInnerObjects = new HashMap<>();
		Map<String, Object> finalObject = new HashMap<>();

		String[] pairs = JSONInput.split(",");

		for (int i = 0; i < pairs.length; i++) {

			int braceIndex = pairs[i].indexOf(":");
			String inner = pairs[i].substring(braceIndex + 1, pairs[i].length());
			String Key = pairs[i].substring(1, braceIndex - 1);

			if (!myTypes.get(Type).containsKey(Key)) {
				ErrorMessage("The key { " + Key + " } does not exist in { " + Type + " } !!");
				return finalObject;
			}

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
						return finalObject;
				}
			}
		}

		finalObject = arrayInnerObjects;

		return finalObject;
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

	public static Boolean passesParameter(String[] paramPairs, Map<String, Object> object, Object paramType) {
		if (paramPairs.length == 0) {
			return false;
		}

		switch ((String) paramType) {
			case "string":
				return object.get(paramPairs[0]).equals(paramPairs[2].substring(1, paramPairs[2].length() - 1));
			case "int":
				switch (paramPairs[1]) {
					case "=":
						return object.get(paramPairs[0]).equals(Integer.parseInt(paramPairs[2]));
					case ">":
						return (Integer) object.get(paramPairs[0]) > Integer.parseInt(paramPairs[2]);
					case "<":
						return (Integer) object.get(paramPairs[0]) < Integer.parseInt(paramPairs[2]);

				}
			case "dbl":
				switch (paramPairs[1]) {
					case "=":
						return object.get(paramPairs[0]).equals(Double.parseDouble(paramPairs[2]));
					case ">":
						return (Integer) object.get(paramPairs[0]) > Double.parseDouble(paramPairs[2]);
					case "<":
						return (Integer) object.get(paramPairs[0]) < Double.parseDouble(paramPairs[2]);

				}
			case "boolean":
				return object.get(paramPairs[0]).equals(Boolean.parseBoolean(paramPairs[2]));
			default:
				return false;
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

			if (input.length() > 0) {
				if (input.startsWith("(")) {
					Parameter = input.substring(0, input.indexOf(")") + 1);

					if (input.indexOf(")") + 1 < input.length())
						input = input.substring(input.indexOf(")") + 2);
					else
						input = "";
				}
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
					if (JSONInput.equals("")) {
						ErrorMessage("Create Command accepts a JSON Input !");
						break;
					}

					updateInstance(Type, JSONInput, myTypes, myTypesInstances, Parameter.replace(" ", ""));
					break;
				case "delete":

					break;
				case "search":
					if (!JSONInput.equals("")) {
						ErrorMessage("There is no need for json, for searching !!!");
						return;
					}

					searchInstances(Type, myTypes, myTypesInstances, Parameter.replace(" ", ""));
					break;
			}

		} else {
			ErrorMessage("Command pattern is not valid !!");
		}

	}

	public static void createType(String Type, String JSONInput,
			Map<String, Map<String, Map<String, Object>>> myTypes,
			Map<String, ArrayList<Map<String, Object>>> myTypesInstances) {

		if (!isAlphaNumUnderscore(Type)) {
			ErrorMessage("{ " + Type + " } Type name is not valid");
			return;
		}

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

				if (!isAlphaNumUnderscore(Key)) {
					ErrorMessage("{ " + Key + " } Key name is not valid");
					return;
				}

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
						} else {
							ErrorMessage("{ " + str + " } is not valid !!");
							return;
						}

					} else if (innerInnerPairs[1].equals("true")) {
						InnerMap.put(innerKey, true);
					} else if (innerInnerPairs[1].equals("false")) {
						InnerMap.put(innerKey, false);
					} else {
						ErrorMessage("Fields must be (int , dbl , sting , bool , true , false), but your input is { "
								+ innerInnerPairs[1] + "} !!!");
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

		Map<String, Object> arrayInnerObjects = parseFlatJsonToHash(JSONInput, Type, myTypes);

		if (arrayInnerObjects.size() == 0) {
			return;
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

	public static void updateInstance(String Type, String JSONInput,
			Map<String, Map<String, Map<String, Object>>> myTypes,
			Map<String, ArrayList<Map<String, Object>>> myTypesInstances, String Parameter) {

		if (!myTypes.containsKey(Type)) {
			ErrorMessage("There is no { " + Type + " } type !!");
			return;
		}

		if (myTypesInstances.get(Type).size() == 0) {
			ErrorMessage("There is no instance with { " + Type + " } type !!");
			return;
		}

		String param = "";
		param = !Parameter.equals("") ? Parameter.substring(1, Parameter.length() - 1) : "";

		Object[] paramStatus = isValidParameter(param);

		if (!myTypes.containsKey(Type)) {
			ErrorMessage("There is no { " + Type + " } type !!");
			return;
		}

		if (!isFlatJSONValid(JSONInput.replace(" ", ""))) {
			ErrorMessage("The Json Input is not valid !!");
			return;
		}

		Map<String, Object> arrayInnerObjects = parseFlatJsonToHash(JSONInput, Type, myTypes);

		if (arrayInnerObjects.size() == 0) {
			return;
		}

		String[] paramPairs = {};

		if (paramStatus[0].equals(true)) {

			if (param.contains("=")) {
				paramPairs = param.split("(?<=[<>=])|(?=[<>=])");
			} else if (param.contains(">")) {
				paramPairs = param.split("(?<=[<>=])|(?=[<>=])");
			} else if (param.contains("<")) {
				paramPairs = param.split("(?<=[<>=])|(?=[<>=])");
			} else {
				ErrorMessage("SomeThing went wrong !!");
				return;
			}

			if (!myTypes.get(Type).containsKey(paramPairs[0])) {
				ErrorMessage("There is no { " + paramPairs[0] + " } in { " + Type + " } Type !!");
				return;
			}

		}

		if (paramStatus[0].equals(false) && !param.equals("")) {
			ErrorMessage("The Parameter is not valid !!!");
			return;
		}

		for (Map.Entry<String, Object> fieldEntry : arrayInnerObjects.entrySet()) {
			String fieldName = fieldEntry.getKey();
			Object attributes = fieldEntry.getValue();

			if (!myTypes.get(Type).containsKey(fieldName)) {
				ErrorMessage("There is no { " + fieldName + " } key in { " + Type + " } type !!");
				return;
			}

			if (!myTypesInstances.get(Type).get(0).get(fieldName).getClass().equals(attributes.getClass())) {
				ErrorMessage("The { " + fieldName + " } key does not have a valid type !!");
				return;
			}

			if (String.class.equals(attributes.getClass()) && attributes.equals("")
					&& myTypes.get(Type).get(fieldName).containsKey("required")
					&& myTypes.get(Type).get(fieldName).get("required").equals(true)) {
				ErrorMessage("The value of { " + fieldName + " } can not be empty !!");
				return;
			}

			if (myTypes.get(Type).get(fieldName).containsKey("unique")
					&& myTypes.get(Type).get(fieldName).get("unique").equals(true)) {

				if (!param.equals("")) {

					int count = 0;
					for (int i = 0; i < myTypesInstances.get(Type).size(); i++) {
						if (passesParameter(paramPairs, myTypesInstances.get(Type).get(i), paramStatus[1])) {
							count++;
						}
					}

					if (count > 1) {
						ErrorMessage("There is more than one instance with unique { " + fieldName
								+ " } field that follow the parameter !!");
						return;
					}
				} else {
					if (myTypesInstances.get(Type).size() > 1) {
						ErrorMessage("There is more than one instancewith unique { " + fieldName
								+ " }, so you can not update them all with the same value !!");
						return;
					}
				}

			}
		}

		for (int i = 0; i < myTypesInstances.get(Type).size(); i++) {
			if (!param.equals("")) {
				if (passesParameter(paramPairs, myTypesInstances.get(Type).get(i), paramStatus[1])) {

					for (Map.Entry<String, Object> fieldEntry : arrayInnerObjects.entrySet()) {
						String fieldName = fieldEntry.getKey();
						Object attributes = fieldEntry.getValue();
						myTypesInstances.get(Type).get(i).put(fieldName, attributes);
					}
				}

			} else {
				for (Map.Entry<String, Object> fieldEntry : arrayInnerObjects.entrySet()) {
					String fieldName = fieldEntry.getKey();
					Object attributes = fieldEntry.getValue();

					myTypesInstances.get(Type).get(i).put(fieldName, attributes);
				}
			}
		}

	}

	public static void searchInstances(String Type,
			Map<String, Map<String, Map<String, Object>>> myTypes,
			Map<String, ArrayList<Map<String, Object>>> myTypesInstances, String Parameter) {

		if (!myTypes.containsKey(Type)) {
			ErrorMessage("There is no { " + Type + " } type !!");
			return;
		}

		if (myTypesInstances.get(Type).size() == 0) {
			ErrorMessage("There is no instance with { " + Type + " } type !!");
			return;
		}

		String param = "";
		param = !Parameter.equals("") ? Parameter.substring(1, Parameter.length() - 1) : "";

		Object[] paramStatus = isValidParameter(param);

		String[] paramPairs = {};

		if (paramStatus[0].equals(true)) {

			if (param.contains("=")) {
				paramPairs = param.split("(?<=[<>=])|(?=[<>=])");
			} else if (param.contains(">")) {
				paramPairs = param.split("(?<=[<>=])|(?=[<>=])");
			} else if (param.contains("<")) {
				paramPairs = param.split("(?<=[<>=])|(?=[<>=])");
			} else {
				ErrorMessage("SomeThing went wrong !!");
				return;
			}

			if (!myTypes.get(Type).containsKey(paramPairs[0])) {
				ErrorMessage("There is no { " + paramPairs[0] + " } in { " + Type + " } Type !!");
				return;
			}

		}

		if (paramStatus[0].equals(false) && !param.equals("")) {
			ErrorMessage("The Parameter is not valid !!!");
			return;
		}

		int count = 0;
		for (int i = 0; i < myTypesInstances.get(Type).size(); i++) {
			if (!param.equals("")) {

				if (passesParameter(paramPairs, myTypesInstances.get(Type).get(i), paramStatus[1])) {
					count++;
				}
			} else
				count++;
		}

		if (count > 0) {

			for (int i = 0; i < myTypesInstances.get(Type).size(); i++) {
				if (!param.equals("")) {

					if (passesParameter(paramPairs, myTypesInstances.get(Type).get(i), paramStatus[1])) {
						System.out.println(myTypesInstances.get(Type).get(i));
					}
				} else
					System.out.println(myTypesInstances.get(Type).get(i));
			}
		}else
		{
			ErrorMessage("There is no record based on your parameter !!");
			return;
		}

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
