/*
 *    Copyright (c) Open Connectivity Foundation (OCF), AllJoyn Open Source
 *    Project (AJOSP) Contributors and others.
 *
 *    SPDX-License-Identifier: Apache-2.0
 *
 *    All rights reserved. This program and the accompanying materials are
 *    made available under the terms of the Apache License, Version 2.0
 *    which accompanies this distribution, and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Copyright (c) Open Connectivity Foundation and Contributors to AllSeen
 *    Alliance. All rights reserved.
 *
 *    Permission to use, copy, modify, and/or distribute this software for
 *    any purpose with or without fee is hereby granted, provided that the
 *    above copyright notice and this permission notice appear in all
 *    copies.
 *
 *    THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 *    WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 *    WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE
 *    AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 *    DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR
 *    PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER
 *    TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *    PERFORMANCE OF THIS SOFTWARE.
*/

package org.alljoyn.bus;

import org.alljoyn.bus.annotation.Position;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Signature provides static methods for converting between Java and DBus type signatures.
 * This class is used internally.
 */
public final class Signature {

    private Signature() {}

    public static Object[] structArgs(Object struct) throws IllegalAccessException,
                                                            BusException {
        Class<?> type = struct.getClass();
        Field[] fields = getInstanceFields(type);

        /*
         * If the given struct is an instance of Object[], there is no implementation class from
         * which Position annotations can be obtained. Therefore, assume field positions are the
         * same as element-positions in the array, and return a shallow copy of the struct array.
         */
        if (type.isInstance(new Object[]{})) {
            Object[] objArray = (Object[])struct;
            return Arrays.copyOf(objArray, objArray.length);
        }

        Object[] args = new Object[fields.length];
        for (Field field : fields) {
            Position position = field.getAnnotation(Position.class);
            if (position == null) {
                throw new BusException("field " + field + " of " + type
                                       + " does not annotate position");
            }
            args[position.value()] = field.get(struct);
        }
        return args;
    }

    public static Field[] structFields(Class<?> cls) throws BusException {
        Field[] fields = getInstanceFields(cls);
        Field[] orderedFields = new Field[fields.length];
        for (Field field : fields) {
            Position position = field.getAnnotation(Position.class);
            if (position == null) {
                throw new BusException("field " + field + " of " + cls
                                       + " does not annotate position");
            }
            orderedFields[position.value()] = field;
        }
        return orderedFields;
    }

    public static Type[] structTypes(Class<?> cls) throws AnnotationBusException {
        Field[] fields = getInstanceFields(cls);
        Type[] types = new Type[fields.length];
        for (Field field : fields) {
            Position position = field.getAnnotation(Position.class);
            if (position == null) {
                throw new AnnotationBusException("field " + field + " of " + cls
                                                 + " does not annotate position");
            }
            types[position.value()] = field.getGenericType();
        }
        return types;
    }

    public static String structSig(Class<?> cls) throws AnnotationBusException {
        Field[] fields = getInstanceFields(cls);
        String[] sig = new String[fields.length];
        for (Field field : fields) {
            Position position = field.getAnnotation(Position.class);
            if (position == null) {
                throw new AnnotationBusException("field " + field + " of " + cls
                                                + " does not annotate position");
            }
            org.alljoyn.bus.annotation.Signature signature =
                field.getAnnotation(org.alljoyn.bus.annotation.Signature.class);
            if (signature == null || "r".equals(signature.value())) {
                sig[position.value()] = typeSig(field.getGenericType(), null);
            } else {
                sig[position.value()] = signature.value();
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String s : sig) {
            sb.append(s);
        }
        return sb.toString();
    }

    public static native String[] split(String signature);

    /**
     * Compute the DBus type signature of the type.
     *
     * @param type the Java type
     * @param signature the annotated signature for the type
     * @return the DBus type signature
     * @throws AnnotationBusException if the signature cannot be computed
     */
    public static String typeSig(Type type, String signature) throws AnnotationBusException {
        if (type instanceof ParameterizedType) {
            return parameterizedTypeSig((ParameterizedType) type, signature);
        } else if (type instanceof Class) {
            return classTypeSig((Class<?>) type, signature);
        } else if (type instanceof GenericArrayType) {
            return genericArrayTypeSig((GenericArrayType) type, signature);
        } else {
            throw new AnnotationBusException("cannot determine signature for " + type);
        }
    }

    private static String parameterizedTypeSig(ParameterizedType type, String signature)
            throws AnnotationBusException {
        Class<?> cls = (Class<?>) type.getRawType();
        if (Map.class.isAssignableFrom(cls)) {
            String sig = "";
            Type[] actuals = type.getActualTypeArguments();
            String[] signatures  = null;
            if (signature != null) {
                signatures = split(signature.substring(2, signature.length() - 1));
            }
            for (int i = 0; i < actuals.length; ++i) {
                sig += typeSig(actuals[i], (signatures == null) ? null : signatures[i]);
            }
            return "a{" + sig + "}";
        } else {
            return classTypeSig(cls, signature);
        }
    }

    private static String classTypeSig(Class<?> cls, String signature) throws AnnotationBusException {
        if (Void.class.isAssignableFrom(cls) || void.class.isAssignableFrom(cls)) {
            return "";
        } else if (Byte.class.isAssignableFrom(cls) || byte.class.isAssignableFrom(cls)) {
            return (signature == null) ? "y" : signature;
        } else if (Boolean.class.isAssignableFrom(cls) || boolean.class.isAssignableFrom(cls)) {
            return (signature == null) ? "b" : signature;
        } else if (Short.class.isAssignableFrom(cls) || short.class.isAssignableFrom(cls)) {
            return (signature == null) ? "n" : signature;
        } else if (Integer.class.isAssignableFrom(cls) || int.class.isAssignableFrom(cls)) {
            return (signature == null) ? "i" : signature;
        } else if (Long.class.isAssignableFrom(cls) || long.class.isAssignableFrom(cls)) {
            return (signature == null) ? "x" : signature;
        } else if (Double.class.isAssignableFrom(cls) || double.class.isAssignableFrom(cls)) {
            return (signature == null) ? "d" : signature;
        } else if (String.class.isAssignableFrom(cls)) {
            return (signature == null) ? "s" : signature;
        } else if (Variant.class.isAssignableFrom(cls)) {
            return (signature == null) ? "v" : signature;
        } else if (cls.isArray()) {
            String sig = (signature == null) ? "a" : signature.substring(0, 1);
            return sig + typeSig(cls.getComponentType(),
                                 (signature == null) ? null : signature.substring(1));
        } else if (cls.isEnum() && signature == null) {
            throw new AnnotationBusException("enum type " + cls + " is missing annotation");
        } else if (signature == null || "r".equals(signature)) {
            String sig = typeSig(structTypes(cls), structSig(cls));
            if (sig.length() == 0) {
                throw new AnnotationBusException("cannot determine signature for " + cls);
            }
            return "(" + sig + ")";
        } else {
            /* Annotated application class - check that annotation is correct first */
            return signature;
        }
    }

    private static String genericArrayTypeSig(GenericArrayType type, String signature)
            throws AnnotationBusException {
        return "a" + typeSig(type.getGenericComponentType(),
                             (signature == null) ? null : signature.substring(1));
    }

    /**
     * Compute the DBus type signature of the types.
     * @param types The Java types.
     * @param signature The annotated signature for the types.
     */
    public static String typeSig(Type[] types, String signature) throws AnnotationBusException {
        String sig = "";
        String[] signatures = split(signature);
        for (int i = 0; i < types.length; ++i) {
            sig += typeSig(types[i], (signatures == null) ? null : signatures[i]);
        }
        return sig;
    }

    /**
     * Returns an array containing {@code Field} objects reflecting all the accessible
     * public instance fields of the class or interface represented by the given
     * {@code Class} object. Synthetic fields (added by the compiler) are also omitted.
     * @param cls
     */
    private static Field[] getInstanceFields(Class<?> cls) {
        Field[] fields = cls.getFields();
        List<Field> instanceFields = new ArrayList<Field>();
        for (Field field : fields) {
            if (!field.isSynthetic() && !Modifier.isStatic(field.getModifiers())) {
                instanceFields.add(field);
            }
        }
        return instanceFields.toArray(new Field[]{});
    }
}
