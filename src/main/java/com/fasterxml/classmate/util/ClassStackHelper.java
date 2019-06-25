package com.fasterxml.classmate.util;


import java.util.ArrayList;
import com.fasterxml.classmate.types.ResolvedRecursiveType;
import com.fasterxml.classmate.ResolvedType;

public class ClassStackHelper {
	private ArrayList<ResolvedRecursiveType> _selfRefs;

	/**
	* Method called to indicate that there is a self-reference from deeper down in stack pointing into type this stack frame represents.
	*/
	public void addSelfReference(ResolvedRecursiveType ref) {
		if (_selfRefs == null) {
			_selfRefs = new ArrayList<ResolvedRecursiveType>();
		}
		_selfRefs.add(ref);
	}

	/**
	* Method called when type that this stack frame represents is fully resolved, allowing self-references to be completed (if there are any)
	*/
	public void resolveSelfReferences(ResolvedType resolved) {
		if (_selfRefs != null) {
			for (ResolvedRecursiveType ref : _selfRefs) {
				ref.setReference(resolved);
			}
		}
	}
}