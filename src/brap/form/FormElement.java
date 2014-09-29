package brap.form;

import static brap.form.FormConstants.*;


import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

import brap.tool.Utils;



public class FormElement {
	private WebElement parent = null;
	private WebElement element = null;
	private Form form;

	// Four information about a form field
	private String type;
	private String name;
	private String id;
	private String tagName;
	private boolean isDisplayed;
	private Point location;
	private String position;
	private String text;	
	private String label = "";
	private String help = "";
	private List<String> values;
	private String selectedValue = "";	
	
	public FormElement() {
		initialize(null);
	}

	public FormElement(WebElement e) {
		initialize(e);
	}
	
	private void initialize(WebElement we) {
		element = we;
		values = new ArrayList<String>();
		label = "";
		help = "";
		selectedValue = "";
	}
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public Point getLocation() {
		return location;
	}

	public void setLocation(Point location) {
		this.location = location;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setDisplayed(boolean isDisplayed) {
		this.isDisplayed = isDisplayed;
	}

	public String getHelp() {
		return help;
	}

	public void setHelp(String help) {
		this.help = help;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String predictedLabel) {
		this.label = predictedLabel;
	}

	public void setElement(WebElement element) {
		this.element = element;
		this.type= computeType(element);
	}

	public void setSelectedValue(String val) {
		selectedValue = val;
		selectedValue = selectedValue.replaceAll("-.*$", "");
	}
	
	public String getSelectedValue() {
		return selectedValue;
	}
	
	public void addValue(String val) {
		this.values.add(val);
	}

	public String getValAt(int index) {
		return this.values.get(index);
	}

	public Form getForm() {
		return form;
	}

	public void setForm(Form form) {
		this.form = form;
	}

	public WebElement getParent() {
		return parent;
	}

	public void setParent(WebElement parent) {
		this.parent = parent;
	}


	public WebElement getElement() {
		return element;
	}

	public String getType() {
		return type;
	}

	/**
	 * Computes the type of the given element 
	 * @param elmt
	 * @return
	 */
	public static String computeType(WebElement elmt){
		String type="";
		type = "UNKNOWN";
		String tagName = "";
		try {
			type = elmt.getAttribute("type");
			tagName = elmt.getTagName();
		} catch (Exception e) {
		}	
		return getElementType(tagName,type);
	}
	
	public void computeType(){
		this.type=getElementType(this.tagName,this.type);
	}
	
	public static String getElementType(String tagName,String type){
		String elType = "UNKNOWN";
		if (type.equalsIgnoreCase(TEXT)) {
			elType = TEXT;
		} else if (type.equalsIgnoreCase(TEXTAREA)) {
			elType = TEXTAREA;
		} else if (type.equalsIgnoreCase(RADIO)) {
			elType = RADIO;
		} else if (type.equalsIgnoreCase(CHECKBOX)) {
			elType = CHECKBOX;
		} else if (tagName.equalsIgnoreCase(A)) {
			elType = A;
		} else if (tagName.equalsIgnoreCase(SELECT) || tagName.equalsIgnoreCase(SELECT_ONE)) {
			elType = SELECT;
		} else if (type.equalsIgnoreCase(HIDDEN)) {
			elType = HIDDEN;
		}			
		return elType;
	}
	
	/**
	 * Element is visible if it is displayed or has x or y coordinate greater
	 * than 0.
	 * 
	 * @return
	 */
	public boolean isDisplayed() {
		boolean result = this.isDisplayed;
		if (this.getTagName().equalsIgnoreCase("select")) {
			result = this.isDisplayed || this.getLocation().x > 0
					|| this.getLocation().y > 0;
		}
		return result;
	}

	public String getCSDescription() {
		String elementText = this.getText().trim().replaceAll("\\s+", " ");
		if (elementText != null && elementText.length() > 0) {
			int len = elementText.length() > 15 ? 15 : elementText.length();
			elementText = elementText.substring(0, len) + "...";
		}
		StringBuffer valS = new StringBuffer();
		for(String val: values) {
			valS.append("; " + val);
		}
		String vals = new String(valS);
		vals = vals.replaceAll("^\\s*;\\s*", "");
		vals = vals.replaceAll("\\s+", " ");		
		StringBuffer sb = new StringBuffer();
		sb.append(this.getTagName());
		sb.append("\t" + this.getType());
		sb.append("\t" + this.getName());
		sb.append("\t" + this.getId());
		sb.append("\t" + this.isDisplayed() );
		sb.append("\t" + this.getLocation().toString());
		sb.append("\t" + elementText);
		sb.append("\t" + vals);
		sb.append("\t" + selectedValue.replaceAll("\\s+", " "));	
		return sb.toString();
	}
		
	
	public String getMySignature(){
		StringBuffer bw = new StringBuffer();
		
		bw.append(getElement().getTagName()+"_");
		bw.append(Utils.getAttribute(getElement(), "type")+"_");
		bw.append(Utils.getAttribute(getElement(), "id")+"_");

		String elementText = element.getText().trim().replaceAll("\\s+", " ");
		if (elementText != null && elementText.length() > 0) {
			int len = elementText.length() > 15 ? 15 : elementText.length();
			elementText = elementText.substring(0, len) + "...";
			bw.append(Utils.getAttribute(getElement(), elementText));
		}
		return bw.toString();

	}
	


}
