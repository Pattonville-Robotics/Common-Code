package org.pattonvillerobotics.commoncode.robotclasses.drive.trailblazer;

import org.apache.commons.math3.filter.DefaultMeasurementModel;
import org.apache.commons.math3.filter.DefaultProcessModel;
import org.apache.commons.math3.filter.MeasurementModel;
import org.apache.commons.math3.filter.ProcessModel;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.FastMath;

/**
 * Created by skaggsm on 10/20/16.
 */

public class KalmanFilterGuidance {

    private final KalmanFilter kalmanFilter;

    public KalmanFilterGuidance() {

        /*                                                   x  y  vx vy ax ay θ  vθ*/
        RealMatrix x = new Array2DRowRealMatrix(new double[]{0, 0, 0, 0, 0, 0, 0, 0});

        double dt = .1; // To be made dynamic later
        double dt2 = FastMath.pow(dt, 2) / 2;

        RealMatrix stateTransition = new Array2DRowRealMatrix(new double[][]{
                {1, 0, dt, 0, dt2, 0, 0, 0},
                {0, 1, 0, dt, 0, dt2, 0, 0},
                {0, 0, 1, 0, dt, 0, 0, 0},
                {0, 0, 0, 1, 0, dt, 0, 0},
                {0, 0, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 0, 0},
                {0, 0, 0, 0, 0, 0, 1, dt},
                {0, 0, 0, 0, 0, 0, 0, 1}
        });
        RealMatrix control = new Array2DRowRealMatrix(new double[][]{{0}});
        RealMatrix processNoise = new Array2DRowRealMatrix(new double[][]{});
        RealVector initialStateEstimate = new ArrayRealVector(new double[]{});
        RealMatrix initialErrorCovariance = new Array2DRowRealMatrix(new double[][]{});
        ProcessModel processModel = new DefaultProcessModel(stateTransition, control, processNoise, initialStateEstimate, initialErrorCovariance);

        RealMatrix measMatrix = new Array2DRowRealMatrix(new double[][]{});
        RealMatrix measNoise = new Array2DRowRealMatrix(new double[][]{});
        MeasurementModel measurementModel = new DefaultMeasurementModel(measMatrix, measNoise);

        kalmanFilter = new KalmanFilter(processModel);
    }

    public void measure(MeasurementModel measurementModel, RealVector measuredState) {
        kalmanFilter.measureAndGetState(measurementModel, measuredState);
    }

    public RealVector getCurrentState() {
        return kalmanFilter.getCurrentState();
    }
}
