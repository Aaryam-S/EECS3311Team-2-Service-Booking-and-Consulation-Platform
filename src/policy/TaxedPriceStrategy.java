package policy;

import model.Service;
import model.SystemPolicy;

public class TaxedPriceStrategy implements PricingStrategy {

    //Constructor
    public TaxedPriceStrategy() {

    }

    //Methods
    @Override
    public double finalPrice(Service service) {
    	if (service == null) {
            throw new IllegalArgumentException("Service cannot be null.");
        }

        double baseRate = service.getRate();
        double taxRate = SystemPolicy.getInstance().getTaxRate();

        return baseRate + (baseRate * taxRate);
    }	
    
}
