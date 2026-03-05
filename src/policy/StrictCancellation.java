package policy;

import model.Booking;

public class StrictCancellation implements CancellationPolicy {

    //Constructor
    public StrictCancellation() {

    }
    
	//Methods
	@Override
	public boolean canCancel(Booking booking) {
		// TODO: strict cancellation rules
		return true;
	}
	
	@Override
	public double cancellationFee(Booking booking) {
		// TODO
		return 0.0;
	}
    
}
