package com.fasterxml.classmate;

import java.util.*;

import com.fasterxml.classmate.members.*;

/**
 * Class that contains information about fully resolved members of a
 * type; resolution meaning that masking is handled for methods, and
 * all inheritable annotations are flattened using optional overrides
 * as well ("mix-in annotations").
 * Instances are created by {@link com.fasterxml.classmate.MemberResolver}.
 *<p>
 * Note that instances are not thread-safe, as the expectation is that instances
 * will not be shared (unlike raw members or resolved types)
 */
public class ResolvedTypeWithMembers
{
    private HierarchicResolver _hierarchicResolver;
    private MethodResolver _methodResolver;
    private FieldResolver _fieldResolver;
    private ConstructResolver _constructResolver;

    /**
     * Default annotation configuration is to ignore all annotations types.
     */
    protected final static AnnotationConfiguration DEFAULT_ANNOTATION_CONFIG
        = new AnnotationConfiguration.StdConfiguration(AnnotationInclusion.DONT_INCLUDE);   

    /*
    /**********************************************************************
    /* Life cycle at this point
    /**********************************************************************
     */

    public ResolvedTypeWithMembers(TypeResolver typeResolver, AnnotationConfiguration annotationConfig,
            HierarchicType mainType, HierarchicType[] types,
            Filter<RawConstructor> constructorFilter, Filter<RawField> fieldFilter, Filter<RawMethod> methodFilter)
    {
    	if (annotationConfig == null) {
    		annotationConfig = DEFAULT_ANNOTATION_CONFIG;
    	}    	 
    	AnnotationHandler annotationHandler = new AnnotationHandler(annotationConfig);
        _hierarchicResolver = new HierarchicResolver(mainType, types);
        _methodResolver = new MethodResolver(methodFilter, _hierarchicResolver, annotationHandler, typeResolver);
        _fieldResolver = new FieldResolver(_hierarchicResolver, annotationHandler, typeResolver, fieldFilter);
        _constructResolver = new ConstructResolver(_hierarchicResolver, annotationHandler, typeResolver, constructorFilter);
    }
    
    /*
    /**********************************************************************
    /* Public API, access to component types
    /**********************************************************************
     */
    
    public int size() { return _hierarchicResolver.size(); }

    /**
     * Accessor for getting full type hierarchy as priority-ordered list, from
     * the lowest precedence to highest precedence (main type, its mix-in overrides)
     */
    public List<HierarchicType> allTypesAndOverrides() {
        return _hierarchicResolver.allTypesAndOverrides();
    }

    /**
     * Accessor for getting subset of type hierarchy which only contains main type
     * and possible overrides (mix-ins) it has, but not supertypes or their overrides.
     */
    public List<HierarchicType> mainTypeAndOverrides()
    {
        return _hierarchicResolver.mainTypeAndOverrides();
    }

    /**
     * Accessor for finding just overrides for the main type (if any).
     */
    public List<HierarchicType> overridesOnly()
    {
        return _hierarchicResolver.overridesOnly();
    }
    
    /*
    /**********************************************************************
    /* Public API, actual resolution of members
    /**********************************************************************
     */

    /**
     * Method for finding all static fields of the main type (except for ones
     * possibly filtered out by filter) and applying annotation overrides, if any,
     * to annotations.
     * 
     * @since 1.2.0
     */
    public ResolvedField[] getStaticFields()
    {
        return _fieldResolver.getStaticFields();
    }
    
    /**
     * Method for finding all static methods of the main type (except for ones
     * possibly filtered out by filter) and applying annotation overrides, if any,
     * to annotations.
     */
    public ResolvedMethod[] getStaticMethods()
    {
        return _methodResolver.getStaticMethods();
    }

    public ResolvedField[] getMemberFields()
    {
        return _fieldResolver.getMemberFields();
    }
    
    public ResolvedMethod[] getMemberMethods()
    {
        return _methodResolver.getMemberMethods();
    }

    public ResolvedConstructor[] getConstructors()
    {
        return _constructResolver.getConstructors();
    }
    
    /*
    /**********************************************************************
    /* Internal methods: actual resolution
    /**********************************************************************
     */
    
    /**
     * Method that will actually resolve full information (types, annotations)
     * for constructors of the main type.
     */
    protected ResolvedConstructor[] resolveConstructors()
    {
        return _constructResolver.resolveConstructors();
    }
    
    /**
     * Method for resolving individual method completely
     */
    protected ResolvedMethod resolveMethod(RawMethod raw)
    {
    	return _methodResolver.resolveMethod(raw);
    }
    
    protected ResolvedField resolveField(RawField raw)
    {
    	return _fieldResolver.resolveField(raw);
    }

    /**
     * Method for fully resolving field definitions and associated annotations.
     * Neither field definitions nor associated annotations inherit, but we may
     * still need to add annotation overrides, as well as filter out filters
     * and annotations that caller is not interested in.
     */
    protected ResolvedField[] resolveMemberFields()
    {
        return _fieldResolver.resolveMemberFields();
    }

    protected ResolvedMethod[] resolveMemberMethods()
    {
        return _methodResolver.resolveMemberMethods();
    }
    
    /**
     * Method for fully resolving static field definitions and associated annotations.
     * Neither field definitions nor associated annotations inherit, but we may
     * still need to add annotation overrides, as well as filter out filters
     * and annotations that caller is not interested in.
     * 
     * @since 1.2.0
     */
    protected ResolvedField[] resolveStaticFields()
    {
        return _fieldResolver.resolveStaticFields();
    }

    /**
     * Method that will actually resolve full information (types, annotations)
     * for static methods, using configured filter.
     */
    protected ResolvedMethod[] resolveStaticMethods()
    {
        return _methodResolver.resolveStaticMethods();
    }

    /*
    /**********************************************************************
    /* Helper methods
    /**********************************************************************
     */

    /**
     * Method for resolving individual constructor completely
     */
    protected ResolvedConstructor resolveConstructor(RawConstructor raw)
    {
        return _constructResolver.resolveConstructor(raw);
    }
}
