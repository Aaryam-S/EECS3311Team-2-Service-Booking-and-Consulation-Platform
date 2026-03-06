package policy;

import model.Booking;

public class FlexibleCancellation implements CancellationPolicy {

	//Constructor
	public FlexibleCancellation() {
		
	}
	
	//Methods
	@Override
	public boolean canCancel(Booking booking) {
		// TODO: flexible cancellation rules
		return true;
	}
	
	@Override
	public double cancellationFee(Booking booking) {
		// TODO
		return 0.0;
	}
	
}
