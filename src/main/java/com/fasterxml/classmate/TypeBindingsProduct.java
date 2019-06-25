package com.fasterxml.classmate;

public class TypeBindingsProduct {
	
private final String[] _names;
	
	private final static String[] NO_STRINGS = new String[0];    

	public TypeBindingsProduct(String[] names) {
		_names = (names == null) ? NO_STRINGS : names;
	}

	public String[] get_names() {
		return _names;
	}

	public String getBoundName(int index) {
		if (index < 0 || index >= _names.length) {
			return null;
		}
		return _names[index];
	}
	
	public int length() {
		return _names.length;
	}

	/**
	* Find type bound to specified name, if there is one; returns bound type if so, null if not.
	*/
	public ResolvedType findBoundType(String name, ResolvedType[] this_types) {
		for (int i = 0, len = length(); i < len; ++i) {
			if (name.equals(_names[i])) {
				return this_types[i];
			}
		}
		return null;
	}
}