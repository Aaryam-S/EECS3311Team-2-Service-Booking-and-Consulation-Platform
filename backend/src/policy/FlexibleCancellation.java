package policy;

import model.Booking;


public class FlexibleCancellation implements CancellationPolicy {

	//Constructor
	public FlexibleCancellation() {
		
	}
	
	//Methods
	@Override
	public boolean canCancel(Booking booking) {
		if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null.");
        }

        return true;
	}
	
	@Override
	public double cancellationFee(Booking booking) {
		if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null.");
        }

        return 0.0;
	}
	
}
