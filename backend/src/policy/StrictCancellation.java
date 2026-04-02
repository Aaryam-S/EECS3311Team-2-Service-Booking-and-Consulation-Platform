package policy;

import model.Booking;
import model.SystemPolicy;

public class StrictCancellation implements CancellationPolicy {

    //Constructor
    public StrictCancellation() {

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

		 return SystemPolicy.getInstance().getCancellationFee();
	}
    
}
