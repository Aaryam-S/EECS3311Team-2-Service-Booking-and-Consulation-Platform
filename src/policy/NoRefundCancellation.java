package policy;

import model.Booking;

public class NoRefundCancellation implements CancellationPolicy {

    //Constructor
    public NoRefundCancellation() {

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
        
        return booking.getService().getRate();
    
    }
	
}
