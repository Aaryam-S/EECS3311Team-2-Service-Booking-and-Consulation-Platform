package model;

import java.util.concurrent.atomic.AtomicInteger;

public class Service {
    private static final AtomicInteger idCounter = new AtomicInteger(1);

    private final int id;
    private final String name;
    private final double rate;
    private final int duration;
    private final String consultantName;

    public Service(String name, double rate, int duration, String consultantName) {
        this.id = idCounter.getAndIncrement();
        this.name = name;
        this.rate = rate;
        this.duration = duration;
        this.consultantName = consultantName;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getRate() { return rate; }
    public double getBasePrice() { return rate; }
    public int getDuration() { return duration; }
    public String getConsultantName() { return consultantName; }

    @Override
    public String toString() {
        return String.format("%s ($%.2f) - %d mins", name, rate, duration);
    }
}
