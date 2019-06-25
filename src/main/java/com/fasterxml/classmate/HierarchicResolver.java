package com.fasterxml.classmate;


import com.fasterxml.classmate.members.HierarchicType;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;

public class HierarchicResolver {
	private HierarchicResolverProduct _hierarchicResolverProduct;
	private final HierarchicType _mainType;
	public HierarchicResolver(HierarchicType mainType, HierarchicType[] types) {
		_hierarchicResolverProduct = new HierarchicResolverProduct(types);
		_mainType = mainType;
	}

	public HierarchicType get_mainType() {
		return _mainType;
	}

	public HierarchicType[] get_types() {
		return _hierarchicResolverProduct.get_types();
	}

	/**
	* Accessor for getting subset of type hierarchy which only contains main type and possible overrides (mix-ins) it has, but not supertypes or their overrides.
	*/
	public List<HierarchicType> mainTypeAndOverrides() {
		List<HierarchicType> l = _hierarchicResolverProduct.allTypesAndOverrides();
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
		List<HierarchicType> l = _hierarchicResolverProduct.allTypesAndOverrides();
		return l.subList(0, index);
	}

	public int size() {
		return _hierarchicResolverProduct.size();
	}

	/**
	* Accessor for getting full type hierarchy as priority-ordered list, from the lowest precedence to highest precedence (main type, its mix-in overrides)
	*/
	public List<HierarchicType> allTypesAndOverrides() {
		return _hierarchicResolverProduct.allTypesAndOverrides();
	}
}