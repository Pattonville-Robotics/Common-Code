package org.pattonvillerobotics.commoncode.robotclasses.drive.trailblazer;

import org.apache.commons.math3.filter.MeasurementModel;
import org.apache.commons.math3.filter.ProcessModel;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.FastMath;

/**
 * Created by skaggsm on 10/25/16.
 */

public class KalmanFilter {

    private RealMatrix F;
    private RealMatrix Ft;
    private RealVector x;
    private RealMatrix P;
    private ProcessModel processModel;

    public KalmanFilter(ProcessModel processModel) {
        this.processModel = processModel;
        setStateTransitionMatrix(0);
        x = processModel.getInitialStateEstimate();
    }

    public synchronized RealVector predictNextState(final double dt) {
        setStateTransitionMatrix(dt);

        x = F.operate(x); // B_t * u_t is ignored since no point to control data
        P = F.multiply(P).multiply(Ft).add(processModel.getProcessNoise());

        return x;
    }

    private void setStateTransitionMatrix(final double dt) {
        final double dt2 = FastMath.pow(dt, 2);
        F = new Array2DRowRealMatrix(new double[][]{
                {1, 0, dt, 0, dt2, 0, 0, 0},
                {0, 1, 0, dt, 0, dt2, 0, 0},
                {0, 0, 1, 0, dt, 0, 0, 0},
                {0, 0, 0, 1, 0, dt, 0, 0},
                {0, 0, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 0, 0},
                {0, 0, 0, 0, 0, 0, 1, dt},
                {0, 0, 0, 0, 0, 0, 0, 1}
        });
        Ft = F.transpose();
    }

    public RealVector getCurrentState() {
        return x;
    }

    public synchronized RealVector measureAndGetState(MeasurementModel measurementModel, RealVector measuredState) {
        RealMatrix H = measurementModel.getMeasurementMatrix();
        RealMatrix Ht = H.transpose();
        RealMatrix PHt = P.multiply(Ht);

        RealVector y = H.operate(measuredState).subtract(H.operate(x));

        RealMatrix S = H.multiply(PHt).add(measurementModel.getMeasurementNoise());
        RealMatrix Si = new LUDecomposition(S).getSolver().getInverse();

        RealMatrix K = PHt.multiply(Si);

        x = x.add(K.operate(y));
        P = P.subtract(K.multiply(H).multiply(P));

        return x;
    }

}
