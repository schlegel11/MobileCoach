package org.isgf.mhc.ui;

import java.io.Serializable;

import lombok.Data;

@Data
public class UISession implements Serializable {
	private static final long	serialVersionUID	= 1L;

	boolean						isLoggedIn			= false;
}
