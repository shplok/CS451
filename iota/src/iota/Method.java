package iota;

/**
 * This class provides a wrapper for a method.
 */
class Method {
    // Internal representation of this method.
    private final java.lang.reflect.Method method;

    /**
     * Constructs a method given its internal representation.
     *
     * @param method the internal representation.
     */
    public Method(java.lang.reflect.Method method) {
        this.method = method;
    }

    /**
     * Returns this method's return type.
     *
     * @return this method's return type.
     */
    public Type returnType() {
        return Type.typeFor(method.getReturnType());
    }

    /**
     * Returns this method's (simple) name.
     *
     * @return this method's (simple) name.
     */
    public String name() {
        return member().getName();
    }

    /**
     * Returns the JVM descriptor for this method.
     *
     * @return the JVM descriptor for this method.
     */
    public String toDescriptor() {
        String descriptor = "(";
        for (Class paramType : method.getParameterTypes()) {
            descriptor += Type.typeFor(paramType).toDescriptor();
        }
        descriptor += ")" + Type.typeFor(method.getReturnType()).toDescriptor();
        return descriptor;
    }

    /**
     * Returns this method's internal representation.
     *
     * @return this method's internal representation.
     */
    protected java.lang.reflect.Member member() {
        return method;
    }
}
