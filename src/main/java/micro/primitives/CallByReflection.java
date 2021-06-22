package micro.primitives;

import micro.Check;
import micro.Value;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class CallByReflection implements Primitive {

    private final List<String> formalParameters;
    private final String className;
    private final String methodName;

    public CallByReflection(String className, String methodName, List<String> formalParameters) {
        this.formalParameters = formalParameters;
        this.className = className;
        this.methodName = methodName;
    }

    @Override
    public Object execute(Map<String, Value> parameters) {
        Object[] actualParams = getByPosition(parameters);
        try {
            return pickMethod(actualParams).invoke(null, actualParams);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static Class<?> getThatClass(String className) {
        Class<?> c;
        try {
            c = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return c;
    }

    private Method pickMethod(Object[] actualParams) {
        Class<?> c = getThatClass(className);
        List<Method> methods = Arrays.stream(c.getDeclaredMethods())
                .filter(i -> i.getName().equals(methodName))
                .collect(Collectors.toList());

        Check.preCondition(methods.size() > 0);
        if (methods.size() == 1)
            return methods.get(0);
        else {
            Class<?>[] actualTypes = Arrays.stream(actualParams).map(Object::getClass).toArray(Class<?>[]::new);
            return methods.stream()
                    .filter(m -> Arrays.equals(m.getParameterTypes(), actualTypes))
                    .findAny()
                    .orElseThrow(() -> new IllegalStateException("No overloaded method found for types " + Arrays.toString(actualTypes)));
        }
    }

    private Object[] getByPosition(Map<String, Value> parameters) {
        Check.preCondition(parameters.size() == formalParameters.size());
        Object[] result = new Object[parameters.size()];
        for (int i = 0; i < parameters.size(); i++) {
            Object val = parameters.get(formalParameters.get(i)).get();
            if (val == null)
                throw new IllegalStateException("Expected parameter not found: " + formalParameters.get(i));
            result[i] = val;
        }

        return result;
    }

    public static CallByReflection callByReflection(String className, String methodName, List<String> formalParameters){
        return new CallByReflection(className, methodName, formalParameters);
    }
}
