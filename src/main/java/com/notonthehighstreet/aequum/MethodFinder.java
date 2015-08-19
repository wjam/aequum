package com.notonthehighstreet.aequum;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;

/**
 * A helper interface which allows functional interfaces to introspect their details.
 * @see <a href="http://benjiweber.co.uk/blog/2015/08/17/lambda-parameter-names-with-reflection/">Blog post this was inspired from</a>
 */
interface MethodFinder extends Serializable {
    default SerializedLambda serialized() {
        try {
            final Method replaceMethod = getClass().getDeclaredMethod("writeReplace");
            replaceMethod.setAccessible(true);
            return (SerializedLambda) replaceMethod.invoke(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    default MethodDetails method() {
        final SerializedLambda lambda = serialized();

        return new MethodDetails(lambda.getImplClass(), lambda.getImplMethodName(), lambda.getImplMethodSignature());
    }

}
