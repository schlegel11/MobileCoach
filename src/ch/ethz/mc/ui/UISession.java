package ch.ethz.mc.ui;

import java.io.Serializable;

import lombok.Data;

import org.bson.types.ObjectId;

@Data
public class UISession implements Serializable {
	private static final long	serialVersionUID	= 1L;

	boolean						isLoggedIn			= false;

	boolean						isAdmin				= false;

	ObjectId					currentAuthorId				= null;

	String						currentAuthorUsername			= null;
}
