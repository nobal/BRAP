package brap.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebElement;

public class Form {
	/**
	 * A page can have more forms
	 */
	int formId;
	List<FormElement> elements;
	List<FormElement> visibleElements;
	List<FormElement> invisibleElements;
	
	String cssId;
	String name;
	
	
	/**
	 * If exists it stores the original form element in page
	 */
	WebElement formElement = null;

	Form() {
		elements = new ArrayList<FormElement>();
		invisibleElements = new ArrayList<FormElement>();
		visibleElements = new ArrayList<FormElement>();
		}

	/**
	 * Provide the form element from you extracted the elements
	 * 
	 * @param e
	 */
	Form(WebElement e) {
		formElement = e;
		elements = new ArrayList<FormElement>();
	}

	public void addElement(FormElement fe) {
		elements.add(fe);
		if (fe.isDisplayed()) {
			visibleElements.add(fe);
		} else {
			invisibleElements.add(fe);
		}
	}

	public int getFormId() {
		return formId;
	}

	public void setFormId(int formId) {
		this.formId = formId;
	}

	public String getCSSId() {
		return cssId;
	}
	/**
	 * Set the CSS id of the form
	 * @param id
	 */
	public void setCSSId(String cssId) {
		this.cssId = cssId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public WebElement getFormElement() {
		return formElement;
	}

	public void setFormElement(WebElement formElement) {
		this.formElement = formElement;
	}

	public void clearElements() {
		elements.clear();
	}
	
	public List<FormElement> getElements() {
		return elements;
	}

	public void setElements(List<FormElement> elements) {
		this.elements = elements;
	}

	public List<FormElement> getVisibleFormElements() {
		return this.visibleElements;
	}

	public List<FormElement> getInvisibleFormElements() {
		return this.invisibleElements;
	}


	/**
	 * Returns the filtered elements.
	 * 
	 * @param type
	 *            the type of elements e.g. INPUT, case insensitive
	 * @param onlyDisplayed
	 *            If true, returns only the filtered elements that are displayed
	 * @return
	 */
	public List<FormElement> getFilteredElementsByType(String type,
			boolean onlyDisplayed) {
		List<FormElement> filteredElements = new ArrayList<FormElement>();
		for (FormElement e : elements) {
			if (e.getType().equalsIgnoreCase(type)) {
				if (onlyDisplayed) {
					if (e.isDisplayed())
						filteredElements.add(e);
				} else {
					filteredElements.add(e);
				}
			}
		}
		return filteredElements;
	}
	/**
	 * Returns the form elements for the given tag name
	 * @param tag
	 * @param onlyDisplayed true means returns only displayed elements 
	 * @return
	 */
	public List<FormElement> getFilteredElementsByTagName(String tag,
			boolean onlyDisplayed) {
		List<FormElement> filteredElements = new ArrayList<FormElement>();
		for (FormElement e : elements) {
			if (e.getTagName().equalsIgnoreCase(tag)) {
				if (onlyDisplayed) {
					if (e.isDisplayed())
						filteredElements.add(e);
				} else {
					filteredElements.add(e);
				}
			}
		}
		return filteredElements;
	}

	public void printFormElements() {
		System.out.println("---- Form Elements (Visual + Hidden) ----");
		int i = 1;
		for (FormElement fe : elements) {
			System.out.println(i + ". " + fe.getCSDescription());
			i++;
		}
	}

	public void printVisibleFormElements() {
		System.out.println("---- Visible Form Elements ----");
		int i = 1;
		for (FormElement fe : elements) {
			if (fe.isDisplayed()) {
				System.out.println(i + ". " + fe.getCSDescription());
				i++;
			}
		}
	}
	
	/**
	 * Returns the position, if any, in the list of Form elements of its type. -1 otherwise
	 * @param me
	 * @return
	 */
	public int getElementPosition(FormElement me){
		List<FormElement> allElementsOfMytype= me.getForm().getFilteredElementsByType(me.getType(), false);
		for(int i=0; i< allElementsOfMytype.size();i++){
			FormElement fe = allElementsOfMytype.get(i);
			if(fe.equals(me)){
				return i;
			}
		}	
		return -1;
	}

	
}
