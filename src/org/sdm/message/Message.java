package org.sdm.message;

import java.io.Serializable;

/**
 * @author Mathew : 19/11/2017.
 */
public class Message implements Serializable {

	private String type;
	private Object object;

	public Message(String type, Object object) {
		this.type = type;
		this.object = object;
	}

	public String getType() {
		return type;
	}

	public Object getObject() {
		return object;
	}
}
