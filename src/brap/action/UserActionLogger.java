package brap.action;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import brap.form.Form;
import brap.form.FormElement;

public class UserActionLogger implements UserAction {
	public final static String[] HEADERS=new String[]{"URL","TYPE","ID","NAME","EVENT","VALUE","FORMID","FEGID","FEID","ATTID","OPTIONS","TOP","LEFT","WIDTH","HEIGHT","TAG"};
	String logFileName;
	boolean firstTime = true; 
	String url;
	List<Form> forms;
	Map<String, FormElement> elementMap;

	UserActionLogger(String logFileName, List<Form> forms) {
		this.logFileName = logFileName;
		this.forms = forms;
		this.elementMap = new HashMap<String, FormElement>();
		if (forms != null) {
			for (Form f : forms) {
				for (FormElement fe : f.getElements()) {
					if (fe.getName() != null) {
						elementMap.put(fe.getName().toLowerCase(), fe);
					}else if(fe.getId()!=null){
						elementMap.put(fe.getId().toLowerCase(), fe);
					}
				}
			}

		}
	}

	@Override
	public void logAction(String action) {
		try {
			if (logFileName != null) {
				boolean append = true;
				if (firstTime) {
					append = false;
					firstTime = false;
				} else {
					append = true;
				}

				Map<String, String> parsed = InteractionFileParser
						.parseLine(action);
				if (parsed.containsKey("name")  ) {
					String fieldName = parsed.get("name").toLowerCase();
					if (elementMap.containsKey(fieldName)) {
						FormElement me = this.elementMap.get(fieldName);
						action += "\t" + "formid:" + me.getForm().getFormId()
								+ "\t" + "fegid:0" + "\t" + "feid:"
								+ me.getForm().getElementPosition(me) + "\t";
					}
				}else if (parsed.containsKey("id")  ) {
					String fieldId = parsed.get("id").toLowerCase();
					if (elementMap.containsKey(fieldId)) {
						FormElement me = this.elementMap.get(fieldId);
						action += "\t" + "formid:" + me.getForm().getFormId()
								+ "\t" + "fegid:0" + "\t" + "feid:"
								+ me.getForm().getElementPosition(me) + "\t";
					}
				}
				
				System.out.println("Parsing action:"+action);
				parsed = InteractionFileParser
						.parseLine(action);
				
				String newFormat="",header="";
				if(!append){
					for(String h:HEADERS){
						header+=h+"\t";
					}
					header=header.trim()+"\n";
				}

				for(String h:HEADERS){
					 if(parsed.containsKey(h.toLowerCase())){
						 newFormat=newFormat+"\t"+parsed.get(h.toLowerCase()).trim();
					 }else{
						 newFormat=newFormat+"\t"+" ";
					 }
				}
				if(!header.isEmpty()){
					newFormat=header+newFormat.trim();
				}else{
					newFormat=newFormat.trim();
				}
				logAction(newFormat, append);
			} else {
				System.out.println(action);
			}
		} catch (Exception e) {
			System.out.println("Action:" + action);
			System.out.println(e);
		}

	}

	public void logAction(String action, boolean append) {
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter(logFileName, append)));
			System.out.println(action);
			out.println(action);
			out.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

}
