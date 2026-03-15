package com.github.alantr7.codebots.utils;

import java.util.LinkedList;

public class Timing {

    private final int samples;

    private final LinkedList<Integer> measurements = new LinkedList<>();

    public Timing(int samples) {
        this.samples = samples;
    }

    public void add(int measurement) {
        measurements.add(measurement);
        if (measurements.size() > samples)
            measurements.removeFirst();
    }

    private float avg() {
        float sum = 0;
        for (int i : measurements)
            sum += i;

        return sum / measurements.size();
    }

    public float getAverage() {
        return measurements.isEmpty() ? 0 : avg();
    }

}
