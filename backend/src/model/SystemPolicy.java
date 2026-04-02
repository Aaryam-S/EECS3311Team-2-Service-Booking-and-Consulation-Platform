package model;

import policy.CancellationPolicy;
import policy.PricingStrategy;

public class SystemPolicy {

	//Singleton Instance
    private static SystemPolicy instance;
    
    //Attributes
    private double cancellationFee;
    private double taxRate;
    private boolean notificationsEnabled;
    private boolean refundsEnabled;

    private CancellationPolicy cancellationPolicy;
    private PricingStrategy pricingStrategy;
    
    //Constructor
    private SystemPolicy() {
    	// defaults
    	this.cancellationFee = 0.0;
    	this.taxRate = 0.0;
    	this.notificationsEnabled = true;
    	this.refundsEnabled = true;
    }
    
    //Methods
    public static SystemPolicy getInstance() {
        if (instance == null) {
            instance = new SystemPolicy();
        }
        return instance;
    }

    public void setCancellationPolicy(CancellationPolicy policy) {
        this.cancellationPolicy = policy;
    }

    public void setPricingStrategy(PricingStrategy strategy) {
        this.pricingStrategy = strategy;
    }
    
    //Getters and Setters
    public CancellationPolicy getCancellationPolicy() {
        return cancellationPolicy;
    }

    public PricingStrategy getPricingStrategy() {
        return pricingStrategy;
    }

    public double getCancellationFee() {
        return cancellationFee;
    }

    public void setCancellationFee(double cancellationFee) {
        this.cancellationFee = cancellationFee;
    }

    public double getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(double taxRate) {
        this.taxRate = taxRate;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public boolean isRefundsEnabled() {
        return refundsEnabled;
    }

    public void setRefundsEnabled(boolean refundsEnabled) {
        this.refundsEnabled = refundsEnabled;
    }

}
