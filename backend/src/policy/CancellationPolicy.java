package policy;

import model.Booking;

public interface CancellationPolicy {
 
	//Methods
	boolean canCancel(Booking booking);
	
	double cancellationFee(Booking booking);
}
