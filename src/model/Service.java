package model;

public class Service {
    private String name;
    private double rate;
    private int duration; // in minutes

    public Service(String name, double rate, int duration) {
        this.name = name;
        this.rate = rate;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public double getRate() {
        return rate;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return String.format("%s ($%.2f) - %d mins", name, rate, duration);
    }
}