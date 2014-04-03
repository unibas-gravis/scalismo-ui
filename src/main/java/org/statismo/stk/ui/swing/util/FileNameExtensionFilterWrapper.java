package org.statismo.stk.ui.swing.util;

import javax.swing.filechooser.FileNameExtensionFilter;

@SuppressWarnings("ALL")
public class FileNameExtensionFilterWrapper {
	public FileNameExtensionFilter create(String description, String[] extensions) {
		return new FileNameExtensionFilter(description, extensions);
	}
}
