package brap.action;

import java.util.HashMap;
import java.util.Map;

public class InteractionFileParser {

	/**
	 * Must call when the format is value:test\tevent:blur etc.
	 * 
	 * @param line
	 * @return
	 */
	public static Map<String, String> parseLine(String line) {
		Map<String, String> output = new HashMap<String, String>();
		String vals[] = line.split("\t");
		for (String val : vals) {
			val = val.trim();
			int index = val.indexOf(':'); // name:myName value:myValue
			// skip the case such as id: i.e. nothing after id
			if (val.trim().endsWith(":")) {
				continue;
			} else {
				output.put(val.substring(0, index).trim(),
						val.substring(index + 1, val.length()));
			}
		}
		return output;
	}

	/**
	 * 
	 * @param line
	 * @param headers
	 * @return
	 */
	public static Map<String, String> parseLine(String line, String[] headers) {
		Map<String, String> output = new HashMap<String, String>();
		String vals[] = line.split("\t");
		for(int i=0;i<vals.length;i++){
			String name= headers[i].toLowerCase();
			String value=vals[i].trim();
			output.put(name,value);
		}
		return output;
	}

}
