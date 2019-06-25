package com.fasterxml.classmate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;

import com.fasterxml.classmate.members.HierarchicType;
import com.fasterxml.classmate.members.RawField;
import com.fasterxml.classmate.members.ResolvedField;

public class FieldResolver {
	
    private final static ResolvedField[] NO_RESOLVED_FIELDS = new ResolvedField[0];
	
	private HierarchicResolver _hierarchicResolver;
	private AnnotationHandler _annotationHandler;
	
    /**
     * Need to be able to resolve member types still
     */
    protected final TypeResolver _typeResolver;
    
	
	/**
     * Filter to use for selecting fields to include
     */
    protected Filter<RawField> _fieldFilter;
    
    /*
    /**********************************************************************
    /* Lazily constructed members
    /**********************************************************************
     */

    protected ResolvedField[] _staticFields = null;

    protected ResolvedField[] _memberFields = null;
 
    
    public FieldResolver(HierarchicResolver hierarchicResolver, AnnotationHandler annotationHandler, TypeResolver typeResolver, Filter<RawField> fieldFilter) {
    	_hierarchicResolver = hierarchicResolver;
    	_annotationHandler = annotationHandler;
    	_fieldFilter = fieldFilter;
    	_typeResolver = typeResolver;
	}

	public ResolvedField[] getStaticFields()
    {
        if (_staticFields == null) {
            _staticFields = resolveStaticFields();
        }
        return _staticFields;
    }
    
    public ResolvedField[] getMemberFields()
    {
        if (_memberFields == null) {
            _memberFields = resolveMemberFields();
        }
        return _memberFields;
    }
    
    /**
     * Method for fully resolving field definitions and associated annotations.
     * Neither field definitions nor associated annotations inherit, but we may
     * still need to add annotation overrides, as well as filter out filters
     * and annotations that caller is not interested in.
     */
    protected ResolvedField[] resolveMemberFields()
    {
        LinkedHashMap<String, ResolvedField> fields = new LinkedHashMap<String, ResolvedField>();

        /* Fields need different handling: must start from bottom; and annotations only get added
         * as overrides, never as defaults. And sub-classes fully mask fields. This makes
         * handling bit simpler than that of member methods.
         */
        for (int typeIndex = _hierarchicResolver.get_types().length; --typeIndex >= 0; ) {
            HierarchicType thisType = _hierarchicResolver.get_types()[typeIndex];
            // If it's just a mix-in, add annotations as overrides
            if (thisType.isMixin()) {
                for (RawField raw : thisType.getType().getMemberFields()) {
                    if ((_fieldFilter != null) && !_fieldFilter.include(raw)) {
                        continue;
                    }
                    ResolvedField field = fields.get(raw.getName());
                    if (field != null) {
                        for (Annotation ann : raw.getAnnotations()) {
                            if (_annotationHandler.includeMethodAnnotation(ann)) {
                                field.applyOverride(ann);
                            }
                        }
                    }
                }
            } else { // If actual type, add fields, masking whatever might have existed before:
                for (RawField field : thisType.getType().getMemberFields()) {
                    if ((_fieldFilter != null) && !_fieldFilter.include(field)) {
                        continue;
                    }
                    fields.put(field.getName(), resolveField(field));
                }
            }
        }
        // and that's it?
        if (fields.size() == 0) {
            return NO_RESOLVED_FIELDS;
        }
        return fields.values().toArray(new ResolvedField[fields.size()]);
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
        // First get static methods for main type, filter
        LinkedHashMap<String, ResolvedField> fields = new LinkedHashMap<String, ResolvedField>();
        for (RawField field : _hierarchicResolver.get_mainType().getType().getStaticFields()) {
            if (_fieldFilter == null || _fieldFilter.include(field)) {
                fields.put(field.getName(), resolveField(field));
            }
        }
        // then apply overrides (mix-ins):
        for (HierarchicType type : _hierarchicResolver.overridesOnly()) {
            for (RawField raw : type.getType().getStaticFields()) {
                ResolvedField field = fields.get(raw.getName()); 
                // must override something, otherwise to ignore
                if (field != null) {
                    for (Annotation ann : raw.getAnnotations()) {
                        if (_annotationHandler.includeFieldAnnotation(ann)) {
                            field.applyOverride(ann);
                        }
                    }
                }
            }
        }
        // and that's it?
        if (fields.isEmpty()) {
            return NO_RESOLVED_FIELDS;
        }
        return fields.values().toArray(new ResolvedField[ fields.size()]);
    }
    
    /**
	 * Method for resolving individual field completely
	 */
	protected ResolvedField resolveField(RawField raw)
	{
	    final ResolvedType context = raw.getDeclaringType();
	    Field field = raw.getRawMember();
	    ResolvedType type = _typeResolver.resolve(context.getTypeBindings(), field.getGenericType());
	    // And then annotations
	    Annotations anns = new Annotations();
	    for (Annotation ann : field.getAnnotations()) {
	        if (_annotationHandler.includeFieldAnnotation(ann)) {
	            anns.add(ann);
	        }
	    }
	    return new ResolvedField(context, anns, field, type);
	}
}