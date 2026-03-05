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
	
}
