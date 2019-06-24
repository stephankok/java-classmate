package com.fasterxml.classmate;


import com.fasterxml.classmate.members.HierarchicType;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;

public class HierarchicResolver {
	private final HierarchicType _mainType;
	private final HierarchicType[] _types;

	public HierarchicResolver(HierarchicType mainType, HierarchicType[] types) {
		_mainType = mainType;
		_types = types;
	}

	public HierarchicType get_mainType() {
		return _mainType;
	}

	public HierarchicType[] get_types() {
		return _types;
	}

	/**
	* Accessor for getting subset of type hierarchy which only contains main type and possible overrides (mix-ins) it has, but not supertypes or their overrides.
	*/
	public List<HierarchicType> mainTypeAndOverrides() {
		List<HierarchicType> l = Arrays.asList(_types);
		int end = _mainType.getPriority() + 1;
		if (end < l.size()) {
			l = l.subList(0, end);
		}
		return l;
	}

	/**
	* Accessor for finding just overrides for the main type (if any).
	*/
	public List<HierarchicType> overridesOnly() {
		int index = _mainType.getPriority();
		if (index == 0) {
			return Collections.emptyList();
		}
		List<HierarchicType> l = Arrays.asList(_types);
		return l.subList(0, index);
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