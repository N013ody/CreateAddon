package com.example.createaddon.content.guidance.api;

public interface IValueProvider<T> {
    T getValue();

    Class<?> getValueType();
}
