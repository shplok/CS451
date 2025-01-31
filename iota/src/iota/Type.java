package iota;

import java.util.Hashtable;

/**
 * A class for representing iota types. All types are represented underneath (in the classRep field) by Java objects
 * of type Class.
 */
class Type {
    // The Type's internal (Java) representation.
    private Class<?> classRep;

    // Maps type names to their Type representations.
    private final static Hashtable<String, Type> types = new Hashtable<>();

    /**
     * The int type.
     */
    public final static Type INT = typeFor(int.class);

    /**
     * The boolean type.
     */
    public final static Type BOOLEAN = typeFor(boolean.class);

    /**
     * The void type.
     */
    public final static Type VOID = typeFor(void.class);

    /**
     * The "any" type (denotes wild expressions).
     */
    public final static Type ANY = new Type(null);

    /**
     * This constructor is to keep the compiler happy.
     */
    protected Type() {
        super();
    }

    /**
     * Constructs and returns a representation for a type from its (Java) class representation, making sure there is
     * a unique representation for each unique type.
     *
     * @param classRep the Java class representation.
     * @return a type representation of classRep.
     */
    public static Type typeFor(Class<?> classRep) {
        if (types.get(descriptorFor(classRep)) == null) {
            types.put(descriptorFor(classRep), new Type(classRep));
        }
        return types.get(descriptorFor(classRep));
    }

    /**
     * Returns true if this type has the same descriptor as other, and false otherwise.
     *
     * @param other the other type.
     * @return true if this type has the same descriptor as other, and false otherwise.
     */
    public boolean equals(Type other) {
        return this.toDescriptor().equals(other.toDescriptor());
    }

    /**
     * Returns true if this is a primitive type, and false otherwise.
     *
     * @return true if this is a primitive type, and false otherwise.
     */
    public boolean isPrimitive() {
        return classRep.isPrimitive();
    }

    /**
     * An assertion that this type matches the specified type. If there is no match, an error is
     * reported.
     *
     * @param line         the line near which the mismatch occurs.
     * @param expectedType type with which to match.
     */
    public void mustMatchExpected(int line, Type expectedType) {
        if (!matchesExpected(expectedType)) {
            IAST.compilationUnit.reportSemanticError(line, "type %s doesn't match type %s", this, expectedType);
        }
    }

    /**
     * Returns true if this type matches expected, and false otherwise.
     *
     * @param expected the type that this might match.
     * @return true if this type matches expected, and false otherwise.
     */
    public boolean matchesExpected(Type expected) {
        return this == Type.ANY || expected == Type.ANY || this.equals(expected);
    }

    /**
     * Returns true if the argument types match, and false otherwise.
     *
     * @param argTypes1 arguments (classReps) of one method.
     * @param argTypes2 arguments (classReps) of another method.
     * @return true if the argument types match, and false otherwise.
     */
    public static boolean argTypesMatch(Class<?>[] argTypes1, Class<?>[] argTypes2) {
        if (argTypes1.length != argTypes2.length) {
            return false;
        }
        for (int i = 0; i < argTypes1.length; i++) {
            if (!Type.descriptorFor(argTypes1[i]).equals(Type.descriptorFor(argTypes2[i]))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the simple (unqualified) name of this type.
     *
     * @return the simple (unqualified) name of this type.
     */
    public String simpleName() {
        return classRep.getSimpleName();
    }

    /**
     * Returns a string representation of this type.
     *
     * @return a string representation of this type.
     */
    public String toString() {
        return toJava(this.classRep);
    }

    /**
     * Returns the JVM descriptor of this type.
     *
     * @return the JVM descriptor of this type.
     */
    public String toDescriptor() {
        return descriptorFor(classRep);
    }

    /**
     * Returns the JVM representation of this type's name.
     *
     * @return the JVM representation of this type's name.
     */
    public String jvmName() {
        return this.isPrimitive() ? this.toDescriptor() : classRep.getName().replace('.', '/');
    }

    /**
     * Finds and returns a method in this type having the given name and argument types, or null.
     *
     * @param name     the method name.
     * @param argTypes the argument types.
     * @return a method in this type having the given name and argument types, or null.
     */
    public Method methodFor(String name, Type[] argTypes) {
        Class[] classes = new Class[argTypes.length];
        for (int i = 0; i < argTypes.length; i++) {
            classes[i] = argTypes[i].classRep;
        }
        Class cls = classRep;

        // Search this class and all superclasses.
        while (cls != null) {
            java.lang.reflect.Method[] methods = cls.getDeclaredMethods();
            for (java.lang.reflect.Method method : methods) {
                if (method.getName().equals(name) && Type.argTypesMatch(classes, method.getParameterTypes())) {
                    return new Method(method);
                }
            }
            cls = cls.getSuperclass();
        }

        return null;
    }

    /**
     * Resolves this type in the given context and returns the resolved type.
     *
     * @param context context in which the names are resolved.
     * @return the resolved type.
     */
    public Type resolve(Context context) {
        return this;
    }

    /**
     * Returns a signature for reporting unfound methods.
     *
     * @param name     the message or type name.
     * @param argTypes the actual argument types.
     * @return a signature for reporting unfound methods.
     */
    public static String signatureFor(String name, Type[] argTypes) {
        String signature = name + "(";
        if (argTypes.length > 0) {
            signature += argTypes[0].toString();
            for (int i = 1; i < argTypes.length; i++) {
                signature += "," + argTypes[i].toString();
            }
        }
        signature += ")";
        return signature;
    }

    // Sets a representation for a type from its Java (Class) representation. Use typeFor() that maps types having
    // like classReps to like Types.
    private Type(Class<?> classRep) {
        this.classRep = classRep;
    }

    // Returns the JVM descriptor of a type's class representation.
    private static String descriptorFor(Class<?> classRep) {
        return classRep == null ? "V" : classRep == void.class ? "V"
                : classRep.isPrimitive() ? (classRep == int.class ? "I"
                : classRep == boolean.class ? "Z" : "?")
                : "L" + classRep.getName().replace('.', '/') + ";";
    }

    // Returns the Java (and so iota) denotation for the specified type.
    private static String toJava(Class classRep) {
        return classRep != null ? classRep.getName() : "";
    }
}
