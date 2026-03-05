package policy;

import model.Service;

public class TaxedPriceStrategy implements PricingStrategy {

    //Constructor
    public TaxedPriceStrategy() {

    }

    //Methods
    @Override
    public double finalPrice(Service service) {
        // TODO: return service price + tax
        return 0.0;
    }	
    
}
