package policy;

import model.Booking;

public class NoRefundCancellation implements CancellationPolicy {

    //Constructor
    public NoRefundCancellation() {

    }

    //Methods
    @Override
    public boolean canCancel(Booking booking) {
        // TODO: no-refund cancellation rules
        return true;
    }

    @Override
    public double cancellationFee(Booking booking) {
        // TODO
        return 0.0;
    }
	
}
