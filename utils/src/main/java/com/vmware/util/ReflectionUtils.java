package com.vmware.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.vmware.util.exception.RuntimeReflectiveOperationException;

/**
 * Util methods for invoking reflection methods and wrapping the exceptions with runtime exceptions.
 */
public class ReflectionUtils {

    public static Class forName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeReflectiveOperationException(e);
        }
    }

    public static Object newInstance(Class clazz, Object... constructorParameters) {
        try {
            List<Class> parameterClasses =
                    Arrays.stream(constructorParameters).map(Object::getClass).collect(Collectors.toList());
            Class[] parameterClassesArray = parameterClasses.toArray(new Class[parameterClasses.size()]);
            return clazz.getConstructor(parameterClassesArray).newInstance(constructorParameters);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeReflectiveOperationException(e);
        }
    }

    public static void invokeMethod(Method method, Object instance) {
        try {
            method.invoke(instance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeReflectiveOperationException(e);
        }
    }

    public static Field getField(Class clazz, String fieldName) {
        try {
            return clazz.getField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new RuntimeReflectiveOperationException(e);
        }
    }

    public static Object getValue(Field field, Object instance) {
        try {
            Object instanceToUse = determineInstanceForField(field, instance);
            return field.get(instanceToUse);
        } catch (IllegalAccessException e) {
            throw new RuntimeReflectiveOperationException(e);
        }
    }

    public static void setValue(Field field, Object instance, Object value) {
        try {
            Object instanceToUse = determineInstanceForField(field, instance);
            field.set(instanceToUse, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeReflectiveOperationException(e);
        }
    }

    private static Object determineInstanceForField(Field field, Object instance) throws IllegalAccessException {
        if (field.getDeclaringClass().isAssignableFrom(instance.getClass())) {
            return instance;
        }
        Optional<Field> matchingAdditionalConfigField = Arrays.stream(instance.getClass().getFields())
                .filter(configField -> configField.getType() == field.getDeclaringClass()).findFirst();
        if (!matchingAdditionalConfigField.isPresent()) {
            throw new RuntimeException("Failed to find field for class " + field.getDeclaringClass().getName() + " in " + instance.getClass().getName());
        }
        return matchingAdditionalConfigField.get().get(instance);
    }
}
