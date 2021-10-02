package com.slr.app.models;

import java.util.HashMap;
import java.util.Map;

public class ApiManager {
	
	private Map<String,Object> elements;

	public ApiManager(Map<String, Object > elements) {
		this.elements = elements;
	}
	
	public ApiManager() {
		this.elements = new HashMap<String, Object>();
	}

	public Map<String, Object> getElements() {
		return elements;
	}

	public void setElements(Map<String, Object> elements) {
		this.elements = elements;
	}
	
	public void addElement(String key,Object obj) {
		this.elements.put(key, obj);
	}
	
	public void mergeResults() {
		for (String key : this.elements.keySet()) {
			Object obj = this.elements.get(key);
			
			
		}
	}
	
}
