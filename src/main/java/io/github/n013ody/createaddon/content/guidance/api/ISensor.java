package io.github.n013ody.createaddon.content.guidance.api;

public interface ISensor<T> extends IValueProvider<T> {
    String getSensorId();

    SensorReading<T> readSensor();

    boolean isReadingValid();
}

