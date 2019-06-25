package com.fasterxml.classmate;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;

/**
 * Helper class we use to reduce number of calls to {@link AnnotationConfiguration};
 * mostly because determination may be expensive.
 */
public class AnnotationHandler {
    private AnnotationHandlerProduct methodInclusions = new AnnotationHandlerProduct();
    private AnnotationHandlerProduct fieldInclusion = new AnnotationHandlerProduct();
    private AnnotationHandlerProduct constructorInclusion = new AnnotationHandlerProduct();
    private AnnotationHandlerProduct parameterInclusion = new AnnotationHandlerProduct();

	private final AnnotationConfiguration _annotationConfig;

    public AnnotationHandler(AnnotationConfiguration annotationConfig) {
        _annotationConfig = annotationConfig;
    }

    public boolean includeConstructorAnnotation(Annotation ann)
    {
    	return constructorInclusion.includeProductAnnotation(ann, this._annotationConfig);
    }
    
    public boolean includeFieldAnnotation(Annotation ann)
    {
    	return fieldInclusion.includeProductAnnotation(ann, this._annotationConfig);
    }

    public boolean includeMethodAnnotation(Annotation ann)
    {
        return methodInclusions.includeProductAnnotation(ann, this._annotationConfig);
    }

    public boolean includeParameterAnnotation(Annotation ann)
    {
    	return parameterInclusion.includeProductAnnotation(ann, this._annotationConfig);
    }
    
    public AnnotationInclusion methodInclusion(Annotation ann)
    {
        return methodInclusions.productInclusion(ann, this._annotationConfig);
    }

    public AnnotationInclusion parameterInclusion(Annotation ann)
    {
    	return parameterInclusion.productInclusion(ann, this._annotationConfig);
    }
    
    protected boolean methodCanInherit(Annotation annotation) {
        AnnotationInclusion annotationInclusion = this.methodInclusion(annotation);
        if (annotationInclusion == AnnotationInclusion.INCLUDE_AND_INHERIT_IF_INHERITED) {
            return annotation.annotationType().isAnnotationPresent(Inherited.class);
        }
        return (annotationInclusion == AnnotationInclusion.INCLUDE_AND_INHERIT);
    }
    
    protected boolean parameterCanInherit(Annotation annotation) {
        AnnotationInclusion annotationInclusion = this.parameterInclusion(annotation);
        if (annotationInclusion == AnnotationInclusion.INCLUDE_AND_INHERIT_IF_INHERITED) {
            return annotation.annotationType().isAnnotationPresent(Inherited.class);
        }
        return (annotationInclusion == AnnotationInclusion.INCLUDE_AND_INHERIT);
    }
}
