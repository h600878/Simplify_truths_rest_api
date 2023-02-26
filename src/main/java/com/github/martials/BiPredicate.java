package com.github.martials;

@FunctionalInterface
public interface BiPredicate<T> {
    boolean test(T o1, T o2);
}
