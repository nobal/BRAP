package brap.form;

import static brap.form.FormConstants.A;
import static brap.form.FormConstants.BUTTON;
import static brap.form.FormConstants.CHECKBOX;
import static brap.form.FormConstants.INPUT;
import static brap.form.FormConstants.PrefixDefaultVals;
import static brap.form.FormConstants.RADIO;
import static brap.form.FormConstants.SELECT;
import static brap.form.FormConstants.TEXT;
import static brap.form.FormConstants.TEXTAREA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import brap.tool.Browser;
import brap.tool.Utils;

//import com.abmash.api.Browser;

public class FormExtractor {

	public FormExtractor() {
	}

	// Highlight the form for the viewer/user
	public static void highlight(WebElement we, RemoteWebDriver driver) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].setAttribute('style', arguments[1]);",
				we, "border: 2px solid red;");
	}

	public static void alert(String message, RemoteWebDriver driver) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("window.alert(@arguments[0])", message);
	}

	/**
	 * Returns the largest form among the list of given form elements
	 * 
	 * @param formElements
	 * @return
	 */
	public static WebElement getMainForm(List<WebElement> formElements) {
		WebElement biggestForm = null;
		int sizeBiggestForm = 0;
		for (WebElement fi : formElements) {
			// Get all input elements i.e. <input... inside this form
			List<WebElement> inputElements = extractInputFields(fi);

			if (inputElements.size() > sizeBiggestForm) {
				biggestForm = fi;
				sizeBiggestForm = inputElements.size();
			}
		}
		return biggestForm;
	}

	



	/**
	 * Extracts input elements embedded by this Form
	 * 
	 * @param form
	 */
	public static void extractFieldsAndValues(Form form) {
		form.clearElements();
		// Get all input elements i.e. <input... inside this form
		List<WebElement> inputElements = extractInputFields(form
				.getFormElement());

		for (WebElement we : inputElements) {
			FormElement fe = new FormElement(we);
			fe.setForm(form);
			try {
				extractFormElementInformation(fe);
			} catch (Exception e) {
				e.printStackTrace();
			}
			form.addElement(fe);
		}
	}

	/**
	 * Extracts input elements and their names (not other element attributes!)
	 * embedded by this Form but
	 * 
	 * @param form
	 */
	public static void extractFieldsAndBasicInfoOnly(Form form) {
		form.clearElements();
		// Get all input elements i.e. <input... inside this form
		List<WebElement> inputElements = extractInputFields(form
				.getFormElement());
		for (WebElement we : inputElements) {
			FormElement fe = new FormElement(we);
			fe.setForm(form);
			try {
				fe.setTagName(we.getTagName());
				fe.setType(Utils.getAttribute(we, "type"));
				fe.setName(Utils.getAttribute(we, "name"));
				// Compute the form element type
				fe.computeType();
				if (fe.getType().equalsIgnoreCase("hidden")) {
					fe.setDisplayed(false);
					fe.setLocation(new Point(-1, -1));
				} else {
					fe.setDisplayed(we.isDisplayed());
					fe.setLocation(new Point(1, 1));
				}
				fe.setId(Utils.getAttribute(we, "id"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			form.addElement(fe);
		}
	}

	/**
	 * Extracts all the interesting information about a form element
	 * 
	 * @param fe
	 * @throws Exception
	 */
	public static void extractFormElementInformation(FormElement fe)
			throws Exception {
		WebElement we = fe.getElement();
		if (we == null) {
			throw new Exception("WebElement in FormElement is NULL");
		}
		fe.setTagName(we.getTagName());
		fe.setType(Utils.getAttribute(we, "type"));
		fe.setName(Utils.getAttribute(we, "name"));
		// Compute the form element type
		fe.computeType();
		fe.setId(Utils.getAttribute(we, "id"));
		if (fe.getType().equalsIgnoreCase("hidden")) {
			fe.setDisplayed(false);
			fe.setLocation(new Point(-1, -1));
		} else {
			fe.setDisplayed(we.isDisplayed());
			fe.setLocation(we.getLocation());

			// Get the selected value
			String val = getSelectedValue(we, fe.getType());
			fe.setSelectedValue(val);
		}

		// set text
		fe.setText(we.getText().trim().replaceAll("\\s+", " "));

		// Get the values
		List<String> vals = new ArrayList<String>();
		vals = getElementValues(we, fe.getType());
		fe.setValues(vals);
	}

	public static WebElement getFormElementByNameOrId(Browser browser,
			String name, String id) {
		RemoteWebDriver driver = browser.getWebDriver();
		WebElement we = null;

		if (name != null)
			we = driver.findElement(By.name(name));
		if (we == null && id != null)
			we = driver.findElement(By.id(id));
		return we;
	}

	/**
	 * Extracts forms in the browser
	 * 
	 * @param browser
	 * @param onlyMainForm
	 *            - If true, it only extracts the largest form in the page.
	 *            Otherwise selects all forms in the page.
	 * @return
	 */
	public static List<Form> getForms(Browser browser, boolean onlyMainForm) {
		RemoteWebDriver driver = browser.getWebDriver();
		List<Form> forms = null;

		// Get the forms
		List<WebElement> formElements = driver.findElements(By.tagName("form"));

		if (formElements != null && formElements.size() > 0) {
			forms = new ArrayList<Form>();
			WebElement f;
			List<WebElement> selectedForms = new ArrayList<WebElement>();
			if (onlyMainForm) {
				f = getMainForm(formElements);
				selectedForms.add(f);
			} else {
				selectedForms = formElements;
			}

			// For each selected forms, extract information
			for (int i = 0; i < selectedForms.size(); i++) {
				Form form = new Form();
				;
				f = selectedForms.get(i);
				form.setFormId(i);
				form.setFormElement(f);
				form.setCSSId(Utils.getAttribute(f, "id"));
				form.setName(Utils.getAttribute(f, "name"));
				form.setFormElement(f);
				forms.add(form);
			}
		}
		return forms;
	}

	public static WebElement getSubmitButton(Form form) {
		List<WebElement> inputElements = FormExtractor.extractInputFields(form
				.getFormElement());

		for (WebElement we : inputElements) {
			if (Utils.getAttribute(we, "type").equalsIgnoreCase("submit")) {
				return we;
			}
		}
		return null;
	}

	/**
	 * Returns the elements given in the fields from the element
	 * 
	 * @param element
	 * @param fields
	 * @return
	 */
	public static List<WebElement> extractFields(WebElement element,
			String... fields) {
		List<WebElement> inputElements = new ArrayList<WebElement>();
		for (String field : fields) {
			List<WebElement> results = element.findElements(By.tagName(field));
			if (results != null) {
				inputElements.addAll(results);
			}
		}
		return inputElements;
	}

	/**
	 * Returns input elements embedded by the given element
	 * 
	 * @param element
	 * @return
	 */
	public static List<WebElement> extractInputFields(WebElement element) {
		// W3c: The <form> element can contain one or more of the following form
		// elements:
		/*
		 * <input> <textarea> <button> <select> <option> <optgroup> <fieldset>
		 * <label>
		 */
		List<WebElement> inputElements = extractFields(element, INPUT,
				TEXTAREA, BUTTON, SELECT, A);
		return inputElements;
	}

	public static List<String> getElementValues(WebElement we, String type) {
		List<String> vals = new ArrayList<String>();
		String val = "";
		if (type.equalsIgnoreCase(TEXT) || type.equalsIgnoreCase(TEXTAREA)) {
			val = Utils.getAttribute(we, "value");
			if (val != null) {
				vals.add(val);
			}
		} else if (type.equalsIgnoreCase(A)) {
			val = we.getText();
			vals.add(val);
		}

		else if (type.equalsIgnoreCase(CHECKBOX)
				|| type.equalsIgnoreCase(RADIO)) {
			val = "";
			String selected = Utils.getAttribute(we, "checked");
			if (selected != null && selected.equalsIgnoreCase("true")) {
				val = PrefixDefaultVals + val;
			}
		} else if (type.equalsIgnoreCase(SELECT)) {
			List<WebElement> options = we.findElements(By.tagName("option"));
			for (WebElement opt : options) {
				val = Utils.getAttribute(opt, "value");
				val = val + "-" + opt.getText();
				String selected = Utils.getAttribute(opt, "selected");
				if (selected != null && selected.equalsIgnoreCase("true")) {
					val = PrefixDefaultVals + val;
				}
				vals.add(val);
			}

		}

		return vals;
	}

	public static String getSelectedValue(WebElement we, String type) {
		String val = "";
		if (type.equalsIgnoreCase(TEXT) || type.equalsIgnoreCase(TEXTAREA)) {
			val = Utils.getAttribute(we, "value");
		} else if (type.equalsIgnoreCase(A)) {
			val = we.getText();
		} else if (type.equalsIgnoreCase(CHECKBOX)
				|| type.equalsIgnoreCase(RADIO)) {
			String selected = Utils.getAttribute(we, "checked");
			if (selected != null && selected.equalsIgnoreCase("true")) {
				val = PrefixDefaultVals + val;
			}
		} else if (type.equalsIgnoreCase(SELECT)) {
			List<WebElement> options = we.findElements(By.tagName("option"));
			for (WebElement opt : options) {
				String sval = Utils.getAttribute(opt, "value");
				sval = sval + "-" + opt.getText();
				String selected = Utils.getAttribute(opt, "selected");
				if (selected != null && selected.equalsIgnoreCase("true")) {
					val = sval;
				}
			}
		}
		return val;
	}

	/**
	 * Returns a form which collects all elements which are outside the form
	 * elements i.e. leftovers returns null if there are no leftovers
	 * 
	 * @param browser
	 * @param forms
	 * @return
	 */
	public static Form getLeftOverElements(Browser browser, List<Form> forms) {
		boolean found = false;
		Form f = new Form();
		f.setFormId(-1);
		Map<String, FormElement> elementsInsideForms = new HashMap<String, FormElement>();
		for (Form fi : forms) {
			for (FormElement fe : fi.getElements()) {
				elementsInsideForms.put(fe.getName(), fe);
			}
		}
		WebElement body = browser.getWebDriver().findElementByTagName("body");
		f.setFormElement(body);
		if (body != null) {
			List<WebElement> inputElements = extractFields(body, INPUT,
					TEXTAREA, BUTTON, SELECT);
			for (WebElement we : inputElements) {
				String name = Utils.getAttribute(we, "name");
				if (name != null && !name.isEmpty()
						&& !elementsInsideForms.containsKey(name)) {
					found = true;
					FormElement fe = new FormElement(we);
					fe.setForm(f);
					try {
						extractFormElementInformation(fe);
					} catch (Exception e) {
						e.printStackTrace();
					}
					f.addElement(fe);
				}
			}
		}
		if (!found) {
			f = null;
		}

		return f;
	}

}
