package model;

import policy.CancellationPolicy;
import policy.PricingStrategy;

public class Admin {

	//Constructor
	public Admin() {
		
	}
	
	//Methods
    public void approveConsultant(Consultant consultant) {
        if (consultant == null) {
            throw new IllegalArgumentException("Consultant cannot be null.");
        }

        consultant.setApproved(true);
    }

    public void setCancellationPolicy(CancellationPolicy policy) {
    	SystemPolicy.getInstance().setCancellationPolicy(policy);
    }

    public void setPricingStrategy(PricingStrategy strategy) {
    	 SystemPolicy.getInstance().setPricingStrategy(strategy);
    }

    public void setNotificationEnabled(boolean enabled) {
    	SystemPolicy.getInstance().setNotificationsEnabled(enabled);
    }

    public void setRefundEnabled(boolean enabled) {
    	SystemPolicy.getInstance().setRefundsEnabled(enabled);
    }

}
