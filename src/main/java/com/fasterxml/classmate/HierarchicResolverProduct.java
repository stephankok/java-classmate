package com.fasterxml.classmate;


import com.fasterxml.classmate.members.HierarchicType;
import java.util.List;
import java.util.Arrays;

public class HierarchicResolverProduct {
	private final HierarchicType[] _types;

	public HierarchicResolverProduct(HierarchicType[] types) {
		_types = types;
	}

	public HierarchicType[] get_types() {
		return _types;
	}

	public int size() {
		return _types.length;
	}

	/**
	* Accessor for getting full type hierarchy as priority-ordered list, from the lowest precedence to highest precedence (main type, its mix-in overrides)
	*/
	public List<HierarchicType> allTypesAndOverrides() {
		return Arrays.asList(_types);
	}
}