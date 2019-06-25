package com.fasterxml.classmate.util;

import com.fasterxml.classmate.types.ResolvedRecursiveListType;

/**
 * Simple helper class used to keep track of 'call stack' for classes being referenced
 * (as well as unbound variables)
 */
public final class ClassStack extends ResolvedRecursiveListType
{
	protected final ClassStack _parent;
    protected final Class<?> _current;

    public ClassStack(Class<?> rootType) {
        this(null, rootType);
    }

    private ClassStack(ClassStack parent, Class<?> curr) {
        _parent = parent;
        _current = curr;
    }

    /**
     * @return New stack frame, if addition is ok; null if not
     */
    public ClassStack child(Class<?> cls)
    {
        return new ClassStack(this, cls);
    }

    public ClassStack find(Class<?> cls)
    {
        if (_current == cls) return this;
        for (ClassStack curr = _parent; curr != null; curr = curr._parent) {
            if (curr._current == cls) {
                return curr;
            }
        }
        return null;
    }
}
