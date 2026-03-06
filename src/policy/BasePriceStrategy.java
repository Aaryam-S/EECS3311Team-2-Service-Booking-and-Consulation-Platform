package policy;

import model.Service;

public class BasePriceStrategy implements PricingStrategy {

    //Constructor
    public BasePriceStrategy() {

    }

    //Methods
    @Override
    public double finalPrice(Service service) {
        // TODO: return base service price
        return 0.0;
    }
	
}
