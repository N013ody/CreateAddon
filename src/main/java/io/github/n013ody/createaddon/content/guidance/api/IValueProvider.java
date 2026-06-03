package io.github.n013ody.createaddon.content.guidance.api;

public interface IValueProvider<T> {
    T getValue();

    Class<?> getValueType();
}

