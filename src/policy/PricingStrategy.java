package policy;

import model.Service;

public interface PricingStrategy {

	//Methods
	double finalPrice(Service service);
	
}
