package model;

public class Service {
    private int id;
    private String name;
    private double rate;
    private int duration; // in minutes
    private String consultantName;

    public Service(String name, double rate, int duration, String consultantName) {
        this.id = generateId();
        this.name = name;
        this.rate = rate;
        this.duration = duration;
        this.consultantName = consultantName;
    }

    private static int nextId = 1;
    private static synchronized int generateId() {
        return nextId++;
    }

    public int getId() {
        return id;
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

    public String getConsultantName() {
        return consultantName;
    }

    public void setConsultantName(String consultantName) {
        this.consultantName = consultantName;
    }

    public double getBasePrice() {
        return rate;
    }

    @Override
    public String toString() {
        return String.format("%s ($%.2f) - %d mins", name, rate, duration);
    }
}