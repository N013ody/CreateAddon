package com.example.createaddon.content.guidance.api;

public record SensorReading<T>(String sensorId, T value, long gameTime, boolean valid) {
}
