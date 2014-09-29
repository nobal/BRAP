package brap.tool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.openqa.selenium.WebElement;

import brap.form.Form;
import brap.form.FormElement;


public class Utils {

	public static Date getCurrentTime() {
		Calendar cal = Calendar.getInstance();
		return cal.getTime();
	}

	public static String convertStreamToString(InputStream inputStream) {
		return (new Scanner(inputStream)).useDelimiter("\\A").next();
	}
	
	
	/**
	 * Reads the lines in supplied file and returns as list
	 * 
	 * @param file
	 * @return
	 */
	public static List<String> readLines(String file) {
		List<String> lines = new ArrayList<String>();
		BufferedReader br = null;
		try {

			br = new BufferedReader(new FileReader(file));
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				lines.add(sCurrentLine);
			}
			br.close();
		} catch (Exception e) {
			System.out.println(e.toString());
			lines = null;
		}
		return lines;

	}

	public static HashMap<String, List<String>> readFromCSVValues(String file) {
		BufferedReader br = null;
		int namecol = 0, idcol = 0, valcol = 0, formcol = 0;
		HashMap<String, List<String>> forms = new HashMap<String, List<String>>();

		try {

			br = new BufferedReader(new FileReader(file));
			String sCurrentLine = br.readLine();
			String[] colheads = sCurrentLine.split("\\t");
			for (int i = 0; i < colheads.length; i++) {

				if (colheads[i].equalsIgnoreCase("name"))
					namecol = i;
				else if (colheads[i].equalsIgnoreCase("form"))
					formcol = i;
				else if (colheads[i].equalsIgnoreCase("id"))
					idcol = i;
				else if (colheads[i].equalsIgnoreCase("selectedvalue"))
					valcol = i;
			}

			while ((sCurrentLine = br.readLine()) != null) {
				String[] cols = sCurrentLine.split("\\t", colheads.length);
				if (cols.length != colheads.length)
					throw new IOException("Need colnum columns per line! "
							+ cols.length + " " + sCurrentLine);
				if (cols[formcol].equals(""))
					throw new IOException("Form id cannot be empty!");
				if (forms.get(cols[formcol]) == null)
					forms.put(cols[formcol], new ArrayList<String>());

				List<String> values = forms.get(cols[formcol]);

				if (!cols[valcol].equals("")) {
					if (!cols[idcol].equals("")) {
						values.add("id_" + cols[idcol] + "=" + cols[valcol]);
					} else if (!cols[namecol].equals("")) {
						values.add("name_" + cols[namecol] + "=" + cols[valcol]);
					} else {
						System.err.println("named and id both nothing "
								+ sCurrentLine);
					}
					if(values.size()>0){
						System.out.println(values.get(values.size() - 1));
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		return forms;
	}

	public static void printToCSVFormExtractionResults(String fileName,
			Form form,boolean append) {

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileName,
					append));
			bw.write("Form\tLabel\tTag\tType\tName\tId\tDisplayed?\tLocation\tText\tValues\tselectedValue\n");
			for (FormElement fe : form.getElements()) {
				String label = fe.getLabel();
				label = label.replaceAll("\\s+", " ");
				label = label.replaceAll("\\n|\\r", "");
				bw.write(form.getFormId() + "\t" + label + "\t"
						+ fe.getCSDescription() + "\n");
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void printToCSVFormExtractionResults(String fileName,
			List<Form> forms) {

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileName,
					false));
			bw.write("Form\tLabel\tTag\tType\tName\tId\tDisplayed?\tLocation\tText\tValues\tselectedValue\n");
			for (Form form : forms) {
				for (FormElement fe : form.getElements()) {
					String label = fe.getLabel();
					label = label.replaceAll("\\s+", " ");
					label = label.replaceAll("\\n|\\r", "");
					bw.write(form.getFormId() + "\t" + label + "\t"
							+ fe.getCSDescription() + "\n");
				}
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void printToFileFormExtractionResults(String url,
			int formNumber, String fileName,
			Map<FormElement, String> labelsByForAttr,
			Map<FormElement, String> labelsByBom, List<FormElement> unlabeledYet) {

		try {
			String TAB = "\t";
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileName,
					true));
			bw.write("\n--------;--------------;-----------------;--------------;----------\n\n");
			bw.write("Current Time: " + getCurrentTime().toString() + TAB
					+ "\n");
			bw.write("URL: " + url + TAB + "\n");
			bw.write("Form Number : " + formNumber + TAB + "\n");

			Map<String, List<FormElement>> elementsByType;
			// 1
			elementsByType = groupFormElementByType(labelsByForAttr);
			bw.write("* Using FOR attribute" + "\n");
			int i = 1;
			String predictedLabel = "";
			for (String type : elementsByType.keySet()) {
				List<FormElement> elements = elementsByType.get(type);
				bw.write(";Type:" + type + "\n");
				for (FormElement fe : elements) {
					predictedLabel = labelsByForAttr.get(fe);
					bw.write(TAB + ";" + i + ";" + predictedLabel + ";"
							+ fe.getCSDescription() + "\n");
					i++;
				}
			}
			// 2
			elementsByType = groupFormElementByType(labelsByBom);
			bw.write("\n** Using BOM " + "\n");
			i = 1;
			for (String type : elementsByType.keySet()) {
				List<FormElement> elements = elementsByType.get(type);
				bw.write(";Type:" + type + "\n");
				for (FormElement fe : elements) {
					predictedLabel = labelsByBom.get(fe);
					bw.write(TAB + ";" + i + ";" + predictedLabel + ";"
							+ fe.getCSDescription() + "\n");
					i++;
				}
			}
			// 3
			elementsByType = groupFormElementByType(unlabeledYet);
			bw.write("\n*** Not labelled Yet " + "\n");
			i = 1;
			for (String type : elementsByType.keySet()) {
				List<FormElement> elements = elementsByType.get(type);
				bw.write(";Type:" + type + "\n");
				for (FormElement fe : elements) {
					predictedLabel = "N/A";
					bw.write(TAB + ";" + i + ";" + predictedLabel + ";"
							+ fe.getCSDescription() + "\n");
					i++;
				}
			}

			bw.write("\n--------;--------------;-----------------;--------------;----------\n\n");
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static Map<String, List<FormElement>> groupFormElementByType(
			Map<FormElement, String> elementMap) {
		Map<String, List<FormElement>> group = new HashMap<String, List<FormElement>>();

		for (FormElement fe : elementMap.keySet()) {
			String type = fe.getType();
			if (group.containsKey(type)) {
				group.get(type).add(fe);
			} else {
				List<FormElement> l = new ArrayList<FormElement>();
				l.add(fe);
				group.put(type, l);
			}
		}
		return group;
	}

	public static Map<String, List<FormElement>> groupFormElementByType(
			List<FormElement> elementList) {
		Map<String, List<FormElement>> group = new HashMap<String, List<FormElement>>();

		for (FormElement fe : elementList) {
			String type = fe.getType();
			if (group.containsKey(type)) {
				group.get(type).add(fe);
			} else {
				List<FormElement> l = new ArrayList<FormElement>();
				l.add(fe);
				group.put(type, l);
			}
		}
		return group;
	}


	/**
	 * Prints the form elements
	 * 
	 * @param elements
	 *            -
	 * @param name
	 *            - type of field
	 */
	public static void printFormElements(List<FormElement> elements, String name) {
		System.out.println(name + ":");
		for (int i = 0; i < elements.size(); i++) {
			System.out.println("     " + (i + 1) + ". "
					+ elements.get(i).getCSDescription());
		}

	}

	/**
	 * Return the value of the attribute of the supplied web element. Note that
	 * the attribute maynot be present in the element. In this case it will
	 * return null.
	 * 
	 * @param we
	 * @param attrName
	 * @return
	 */
	public static String getAttribute(WebElement we, String attrName) {
		String attrVal = null;
		try {
			attrVal = we.getAttribute(attrName);
		} catch (Exception e) {

		}
		return attrVal;
	}

	/**
	 * Prints the List of lines to given file name
	 * 
	 * @param results
	 * @param outputFile
	 * @throws IOException
	 */
	public static void printToFile(List<String> results, String outputFile)
			throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
		for (String line : results) {
			bw.write(line + "\n");
		}
		bw.close();
	}

	/**
	 * As name suggests, this removes all non-alpha numeric chars from a given
	 * string.
	 * 
	 * @param input
	 * @return
	 */
	public static String removeNonAlphaNumericChars(String input) {
		return input.replaceAll("[^A-Za-z0-9 ]", "").trim();
	}

	public static String squeezeMultipleWhiteSpaceToOne(String input) {
		return input.replaceAll("\\s+", " ");
	}
	
	public static void printToFile(String fileName,String text) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
			bw.write(text);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
