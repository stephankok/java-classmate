package com.fasterxml.classmate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;

import com.fasterxml.classmate.members.HierarchicType;
import com.fasterxml.classmate.members.RawConstructor;
import com.fasterxml.classmate.members.ResolvedConstructor;
import com.fasterxml.classmate.util.MethodKey;

public class ConstructResolver {
	private final static ResolvedType[] NO_RESOLVED_TYPES = new ResolvedType[0];    
    private final static ResolvedConstructor[] NO_RESOLVED_CONSTRUCTORS = new ResolvedConstructor[0];
	
	private HierarchicResolver _hierarchicResolver;
	private AnnotationHandler _annotationHandler;     	        
    
    /**
     * Need to be able to resolve member types still
     */
    protected final TypeResolver _typeResolver;
    
    /**
     * Filter to use for selecting constructors to include
     */
    protected Filter<RawConstructor> _constructorFilter;
 
    
    /*
    /**********************************************************************
    /* Lazily constructed members
    /**********************************************************************
     */

    protected ResolvedConstructor[] _constructors = null;
    
    public ConstructResolver(HierarchicResolver hierarchicResolver, AnnotationHandler annotationHandler, TypeResolver typeResolver, Filter<RawConstructor> constructorFilter) {
    	_hierarchicResolver = hierarchicResolver;        	
    	_annotationHandler = annotationHandler;
    	_typeResolver = typeResolver;
    	_constructorFilter = constructorFilter;
	}
    
    public ResolvedConstructor[] getConstructors()
    {
        if (_constructors == null) {
            _constructors = resolveConstructors();
        }
        return _constructors;
    }
    
    /**
     * Method that will actually resolve full information (types, annotations)
     * for constructors of the main type.
     */
    protected ResolvedConstructor[] resolveConstructors()
    {
        // First get static methods for main type, filter
        LinkedHashMap<MethodKey, ResolvedConstructor> constructors = new LinkedHashMap<MethodKey, ResolvedConstructor>();
        for (RawConstructor constructor : _hierarchicResolver.get_mainType().getType().getConstructors()) {
            // no filter for constructors (yet?)
            if (_constructorFilter == null || _constructorFilter.include(constructor)) {
                constructors.put(constructor.createKey(), resolveConstructor(constructor));
            }
        }
        // then apply overrides (mix-ins):
        for (HierarchicType type : _hierarchicResolver.overridesOnly()) {
            for (RawConstructor raw : type.getType().getConstructors()) {
                ResolvedConstructor constructor = constructors.get(raw.createKey()); 
                // must override something, otherwise to ignore
                if (constructor != null) {
                    for (Annotation ann : raw.getAnnotations()) {
                        if (_annotationHandler.includeMethodAnnotation(ann)) {
                            constructor.applyOverride(ann);
                        }
                    }

                    // and parameter annotations
                    Annotation[][] params = raw.getRawMember().getParameterAnnotations();
                    for (int i = 0; i < params.length; i++) {
                        for (Annotation annotation : params[i]) {
                            if (_annotationHandler.includeParameterAnnotation(annotation)) {
                                constructor.applyParamOverride(i, annotation);
                            }
                        }
                    }
                }
            }
        }
        if (constructors.size() == 0) {
            return NO_RESOLVED_CONSTRUCTORS;
        }
        return constructors.values().toArray(new ResolvedConstructor[constructors.size()]);
    }
    
    /**
     * Method for resolving individual constructor completely
     */
    protected ResolvedConstructor resolveConstructor(RawConstructor raw)
    {
        final ResolvedType context = raw.getDeclaringType();
        final TypeBindings bindings = context.getTypeBindings();
        Constructor<?> ctor = raw.getRawMember();
        Type[] rawTypes = ctor.getGenericParameterTypes();
        ResolvedType[] argTypes;
        if (rawTypes == null || rawTypes.length == 0) {
            argTypes = NO_RESOLVED_TYPES;
        } else {
            argTypes = new ResolvedType[rawTypes.length];
            for (int i = 0, len = rawTypes.length; i < len; ++i) {
                argTypes[i] = _typeResolver.resolve(bindings, rawTypes[i]);
            }
        }
        // And then annotations
        Annotations anns = new Annotations();
        for (Annotation ann : ctor.getAnnotations()) {
            if (_annotationHandler.includeConstructorAnnotation(ann)) {
                anns.add(ann);
            }
        }

        ResolvedConstructor constructor = new ResolvedConstructor(context, anns, ctor, argTypes);

        // and parameter annotations
        Annotation[][] annotations = ctor.getParameterAnnotations();
        for (int i = 0; i < argTypes.length; i++) {
            for (Annotation ann : annotations[i]) {
                constructor.applyParamOverride(i, ann);
            }
        }

        return constructor;
    }
    
}