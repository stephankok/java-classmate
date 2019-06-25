package com.fasterxml.classmate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;

import com.fasterxml.classmate.members.HierarchicType;
import com.fasterxml.classmate.members.RawMethod;
import com.fasterxml.classmate.members.ResolvedMethod;
import com.fasterxml.classmate.util.MethodKey;

public class MethodResolver {
	
	private final static ResolvedType[] NO_RESOLVED_TYPES = new ResolvedType[0];        
	private final static ResolvedMethod[] NO_RESOLVED_METHODS = new ResolvedMethod[0];
	
	private HierarchicResolver _hierarchicResolver;
	private AnnotationHandler _annotationHandler; 
	
    /**
     * Filter to use for selecting methods to include
     */
    protected Filter<RawMethod> _methodFilter;
    
    /**
     * Need to be able to resolve member types still
     */
    protected final TypeResolver _typeResolver;
    
    /*
    /**********************************************************************
    /* Lazily constructed members
    /**********************************************************************
     */

    protected ResolvedMethod[] _staticMethods = null;

    protected ResolvedMethod[] _memberMethods = null;
 
    
    public MethodResolver(Filter<RawMethod> methodFilter, HierarchicResolver hierarchicResolver, AnnotationHandler annotationHandler, TypeResolver typeResolver) {
    	_hierarchicResolver = hierarchicResolver;
    	_methodFilter = methodFilter;
    	_annotationHandler = annotationHandler;
    	_typeResolver = typeResolver;
	}

    /**
     * Method for finding all static methods of the main type (except for ones
     * possibly filtered out by filter) and applying annotation overrides, if any,
     * to annotations.
     */
    public ResolvedMethod[] getStaticMethods()
    {
        if (_staticMethods == null) {
            _staticMethods = resolveStaticMethods();
        }
        return _staticMethods;
    }
    
    public ResolvedMethod[] getMemberMethods()
    {        	
        if (_memberMethods == null) {            	      
            _memberMethods = resolveMemberMethods();
        }
        return _memberMethods;
    }
    
    /*
    /**********************************************************************
    /* Internal methods: actual resolution
    /**********************************************************************
     */
    
    protected ResolvedMethod[] resolveMemberMethods()
    {
        LinkedHashMap<MethodKey, ResolvedMethod> methods = new LinkedHashMap<MethodKey, ResolvedMethod>();
        LinkedHashMap<MethodKey, Annotations> overrides = new LinkedHashMap<MethodKey, Annotations>();
        LinkedHashMap<MethodKey, Annotations[]> paramOverrides = new LinkedHashMap<MethodKey, Annotations[]>();

        /* Member methods are handled from top to bottom; and annotations are tracked
         * alongside (for overrides), as well as "merged down" for inheritable
         * annotations.
         */
        for (HierarchicType type : _hierarchicResolver.allTypesAndOverrides()) {
            for (RawMethod method : type.getType().getMemberMethods()) {
                // First: ignore methods caller is not interested
                if (_methodFilter != null && !_methodFilter.include(method)) {
                    continue;
                }

                MethodKey key = method.createKey();
                ResolvedMethod old = methods.get(key);
                
                // Ok, now, mix-ins only contribute annotations; whereas 'real' types methods
                if (type.isMixin()) { // mix-in: only get annotations
                    for (Annotation ann : method.getAnnotations()) {
                        // If already have a method, must be inheritable to include
                        if (old != null) {
                            if (!_annotationHandler.methodCanInherit(ann)) {
                                continue;
                            }
                            // and if so, apply as default (i.e. do not override)
                            old.applyDefault(ann);
                        } else { // If no method, need to add to annotation override map
                            Annotations oldAnn = overrides.get(key);
                            if (oldAnn == null) {
                                oldAnn = new Annotations();
                                oldAnn.add(ann);
                                overrides.put(key, oldAnn);
                            } else {
                                oldAnn.addAsDefault(ann);
                            }
                        }
                    }

                    // override argument annotations
                    final Annotation[][] argAnnotations = method.getRawMember().getParameterAnnotations();
                    if (old == null) { // no method (yet), add argument annotations to override map
                        Annotations[] oldParamAnns = paramOverrides.get(key);
                        if (oldParamAnns == null) { // no existing argument annotations for method
                            oldParamAnns = new Annotations[argAnnotations.length];
                            for (int i = 0; i < argAnnotations.length; i++) {
                                oldParamAnns[i] = new Annotations();
                                for (final Annotation annotation : argAnnotations[i]) {
                                    if (_annotationHandler.parameterCanInherit(annotation)) {
                                        oldParamAnns[i].add(annotation);
                                    }
                                }
                            }
                            paramOverrides.put(key, oldParamAnns);
                        } else {
                            for (int i = 0; i < argAnnotations.length; i++) {
                                for (final Annotation annotation : argAnnotations[i]) {
                                    if (_annotationHandler.parameterCanInherit(annotation)) {
                                        oldParamAnns[i].addAsDefault(annotation);
                                    }
                                }
                            }
                        }
                    } else { // already have a method, apply argument annotations as defaults
                        for (int i = 0; i < argAnnotations.length; i++) {
                            for (final Annotation annotation : argAnnotations[i]) {
                                if (_annotationHandler.parameterCanInherit(annotation)) {
                                    old.applyParamDefault(i, annotation);
                                }
                            }
                        }
                    }
                } else { // "real" methods; add if not present, possibly add defaults as well
                    if (old == null) { // new one to add
                        ResolvedMethod newMethod = resolveMethod(method);
                        methods.put(key, newMethod);
                        // But we may also have annotation overrides, so:
                        Annotations overrideAnn = overrides.get(key);
                        if (overrideAnn != null) {
                            newMethod.applyOverrides(overrideAnn);
                        }
                        // and apply parameter annotation overrides
                        Annotations[] annotations = paramOverrides.get(key);
                        if (annotations != null) {
                            for (int i = 0; i < annotations.length; i++) {
                                newMethod.applyParamOverrides(i, annotations[i]);
                            }
                        }
                    } else { // method masked by something else? can only contribute annotations
                        for (Annotation ann : method.getAnnotations()) {
                            if (_annotationHandler.methodCanInherit(ann)) {
                                old.applyDefault(ann);
                            }
                        }
                        // and parameter annotations
                        final Annotation[][] parameterAnnotations = method.getRawMember().getParameterAnnotations();
                        for (int i = 0; i < parameterAnnotations.length; i++) {
                            for (final Annotation annotation : parameterAnnotations[i]) {
                                if (_annotationHandler.parameterCanInherit(annotation)) {
                                    old.applyParamDefault(i, annotation);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (methods.size() == 0) {
            return NO_RESOLVED_METHODS;
        }
        return methods.values().toArray(new ResolvedMethod[methods.size()]);
    }
    
    /**
     * Method that will actually resolve full information (types, annotations)
     * for static methods, using configured filter.
     */
    protected ResolvedMethod[] resolveStaticMethods()
    {
        // First get static methods for main type, filter
        LinkedHashMap<MethodKey, ResolvedMethod> methods = new LinkedHashMap<MethodKey, ResolvedMethod>();
        for (RawMethod method : _hierarchicResolver.get_mainType().getType().getStaticMethods()) {
            if (_methodFilter == null || _methodFilter.include(method)) {
                methods.put(method.createKey(), resolveMethod(method));
            }
        }
        // then apply overrides (mix-ins):
        for (HierarchicType type : _hierarchicResolver.overridesOnly()) {
            for (RawMethod raw : type.getType().getStaticMethods()) {
                ResolvedMethod method = methods.get(raw.createKey()); 
                // must override something, otherwise to ignore
                if (method != null) {
                    for (Annotation ann : raw.getAnnotations()) {
                        if (_annotationHandler.includeMethodAnnotation(ann)) {
                            method.applyOverride(ann);
                        }
                    }
                }
            }
        }
        if (methods.size() == 0) {
            return NO_RESOLVED_METHODS;
        }
        return methods.values().toArray(new ResolvedMethod[methods.size()]);
    }
    
    /**
     * Method for resolving individual method completely
     */
    protected ResolvedMethod resolveMethod(RawMethod raw)
    {
        final ResolvedType context = raw.getDeclaringType();
        final TypeBindings bindings = context.getTypeBindings();
        Method m = raw.getRawMember();
        Type rawType = m.getGenericReturnType();
        ResolvedType rt = (rawType == Void.TYPE) ? null : _typeResolver.resolve(bindings, rawType);
        Type[] rawTypes = m.getGenericParameterTypes();
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
        for (Annotation ann : m.getAnnotations()) {
            if (_annotationHandler.includeMethodAnnotation(ann)) {
                anns.add(ann);
            }
        }

        ResolvedMethod method = new ResolvedMethod(context, anns, m, rt, argTypes);

        // and argument annotations
        Annotation[][] annotations = m.getParameterAnnotations();
        for (int i = 0; i < argTypes.length; i++) {
            for (Annotation ann : annotations[i]) {
                method.applyParamOverride(i, ann);
            }
        }
        return method;
    }               
}