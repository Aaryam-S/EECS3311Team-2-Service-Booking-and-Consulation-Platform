package policy;

import model.Service;

public class BasePriceStrategy implements PricingStrategy {

    //Constructor
    public BasePriceStrategy() {

    }

    //Methods
    @Override
    public double finalPrice(Service service) {
    	if (service == null) {
            throw new IllegalArgumentException("Service cannot be null.");
        }

        return service.getRate();
    }
	
}
