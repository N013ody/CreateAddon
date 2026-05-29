package com.example.createaddon.content.guidance.api;

public interface ISensor<T> extends IValueProvider<T> {
    String getSensorId();

    SensorReading<T> readSensor();

    boolean isReadingValid();
}
