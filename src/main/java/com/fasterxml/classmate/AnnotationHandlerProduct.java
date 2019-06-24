package com.fasterxml.classmate;


import java.util.HashMap;
import java.lang.annotation.Annotation;

public class AnnotationHandlerProduct {
	private HashMap<Class<? extends Annotation>, AnnotationInclusion> _methodInclusions;

	public AnnotationInclusion productInclusion(Annotation ann, AnnotationConfiguration this_annotationConfig) {
		Class<? extends Annotation> annType = ann.annotationType();
		if (_methodInclusions == null) {
			_methodInclusions = new HashMap<Class<? extends Annotation>, AnnotationInclusion>();
		} else {
			AnnotationInclusion incl = _methodInclusions.get(annType);
			if (incl != null) {
				return incl;
			}
		}
		AnnotationInclusion incl = this_annotationConfig.getInclusionForMethod(annType);
		_methodInclusions.put(annType, incl);
		return incl;
	}

	public boolean includeProductAnnotation(Annotation ann, AnnotationConfiguration this_annotationConfig) {
		return productInclusion(ann, this_annotationConfig) != AnnotationInclusion.DONT_INCLUDE;
	}
}