package com.notonthehighstreet.aequum;

import java.util.function.Function;

/**
 * A function which allows introspection of the passed in lambda.
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 */
public interface SerializableFunction<T, R> extends MethodFinder, Function<T, R> {
}
