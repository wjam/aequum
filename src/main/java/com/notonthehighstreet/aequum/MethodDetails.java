package com.notonthehighstreet.aequum;

class MethodDetails {
    private final String containingClass;
    private final String methodName;
    private final String methodSignature;

    MethodDetails(final String containingClass, final String methodName, final String methodSignature) {
        this.containingClass = containingClass;
        this.methodName = methodName;
        this.methodSignature = methodSignature;
    }

    String getContainingClass() {
        return containingClass;
    }

    String getMethodName() {
        return methodName;
    }

    String getMethodSignature() {
        return methodSignature;
    }
}
