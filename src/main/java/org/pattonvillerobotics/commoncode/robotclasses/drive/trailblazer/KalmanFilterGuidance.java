package org.pattonvillerobotics.commoncode.robotclasses.drive.trailblazer;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.apache.commons.math3.filter.DefaultMeasurementModel;
import org.apache.commons.math3.filter.MeasurementModel;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.FastMath;
import org.pattonvillerobotics.commoncode.robotclasses.drive.AbstractComplexDrive;

import java.util.concurrent.TimeUnit;

/**
 * Created by skaggsm on 10/20/16.
 */

public class KalmanFilterGuidance implements Runnable {

    private static final MeasurementModel ENCODER_MEASUREMENT_MODEL = new DefaultMeasurementModel(
            new Array2DRowRealMatrix(new double[][]{
                    {0, 0, 1, 0, 0, 0, 0, 0},
                    {0, 0, 0, 1, 0, 0, 0, 0},
                    {0, 0, 0, 0, 0, 0, 0, 1}
            }),
            new Array2DRowRealMatrix(new double[][]{
                    {2, 0, 0},
                    {0, 2, 0},
                    {0, 0, 5}
            })
    );
    private static final MeasurementModel GYRO_MEASUREMENT_MODEL = new DefaultMeasurementModel(
            new Array2DRowRealMatrix(new double[][]{
                    {0, 0, 0, 0, 0, 0, 0, 1}
            }),
            new Array2DRowRealMatrix(new double[][]{
                    {10}
            })
    );
    private static final double S_TO_NS = TimeUnit.SECONDS.toNanos(1);
    private final KalmanFilter kalmanFilter;
    private final Thread encoderThread;
    private final Thread predictThread;
    private final Thread gyroThread;

    public KalmanFilterGuidance(final LinearOpMode linearOpMode, final AbstractComplexDrive complexDrive, final ModernRoboticsI2cGyro gyro) {

        predictThread = new Thread(new Runnable() {
            long lastTimeNS = System.nanoTime();

            @Override
            public void run() {
                while (linearOpMode.opModeIsActive()) {
                    synchronized (kalmanFilter) {
                        long nowTimeNS = System.nanoTime();
                        double elapsedTimeS = (nowTimeNS - lastTimeNS) / S_TO_NS;
                        kalmanFilter.predictNextState(elapsedTimeS);
                    }
                }
            }
        });
        encoderThread = new Thread(new Runnable() {
            long lastTimeNS = System.nanoTime();
            int priorLeftEncoderReading, priorRightEncoderReading;

            /**
             * WARNING: This does NOT take into account anything except rotation on the spot and straight-line motion!
             */
            @Override
            public void run() {
                priorLeftEncoderReading = complexDrive.leftDriveMotor.getCurrentPosition();
                priorRightEncoderReading = complexDrive.rightDriveMotor.getCurrentPosition();
                while (linearOpMode.opModeIsActive()) {
                    synchronized (kalmanFilter) {
                        long nowTimeNS = System.nanoTime();
                        double elapsedTimeS = (nowTimeNS - lastTimeNS) / S_TO_NS;

                        double currentHeading = kalmanFilter.getCurrentState().getEntry(6);
                        //Get new values
                        int currentLeftEncoderReading = complexDrive.leftDriveMotor.getCurrentPosition();
                        int currentRightEncoderReading = complexDrive.rightDriveMotor.getCurrentPosition();
                        //Find difference
                        int deltaLeftEncoderReading = currentLeftEncoderReading - priorLeftEncoderReading;
                        int deltaRightEncoderReading = currentRightEncoderReading - priorRightEncoderReading;
                        //Replace old values
                        priorLeftEncoderReading = currentLeftEncoderReading;
                        priorRightEncoderReading = currentRightEncoderReading;
                        //Find real distance
                        double deltaLeftInches = complexDrive.inchesToTicksInverse(deltaLeftEncoderReading);
                        double deltaRightInches = complexDrive.inchesToTicksInverse(deltaRightEncoderReading);
                        double averageSpeed = ((deltaLeftInches + deltaRightInches) / 2) / elapsedTimeS;

                        double cos = FastMath.cos(FastMath.toRadians(currentHeading));
                        double sin = FastMath.sin(FastMath.toRadians(currentHeading));
                        double vx = cos * averageSpeed;
                        double vy = sin * averageSpeed;
                        double approximateAngularVelocity = complexDrive.degreesToInchesInverse((deltaRightInches - deltaLeftInches) / 2) / elapsedTimeS;

                        kalmanFilter.measureAndGetState(ENCODER_MEASUREMENT_MODEL, new ArrayRealVector(new double[]{vx, vy, approximateAngularVelocity}));
                    }
                }
            }
        });
        gyroThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (linearOpMode.opModeIsActive()) {
                    synchronized (kalmanFilter) {
                        double angularVelocity = gyro.rawZ() / 1000d; //TODO find conversion from raw to true values
                        kalmanFilter.measureAndGetState(GYRO_MEASUREMENT_MODEL, new ArrayRealVector(new double[]{angularVelocity}));
                    }
                }
            }
        });
        /*                                                   x  y  vx vy ax ay θ  ω*/
        RealVector x = new ArrayRealVector(new double[]{0, 0, 0, 0, 0, 0, 0, 0});
        RealMatrix processNoise = new Array2DRowRealMatrix(new double[][]{});
/*
        RealMatrix stateTransition = KalmanFilter.getStateTransitionMatrix(.1);

        RealMatrix control = new Array2DRowRealMatrix(new double[][]{{0}});
        RealVector initialStateEstimate = new ArrayRealVector(new double[]{});
        RealMatrix initialErrorCovariance = new Array2DRowRealMatrix(new double[][]{});
        ProcessModel processModel = new DefaultProcessModel(stateTransition, control, processNoise, initialStateEstimate, initialErrorCovariance);

        RealMatrix measMatrix = new Array2DRowRealMatrix(new double[][]{});
        RealMatrix measNoise = new Array2DRowRealMatrix(new double[][]{});
        MeasurementModel measurementModel = new DefaultMeasurementModel(measMatrix, measNoise);
*/
        kalmanFilter = new KalmanFilter(processNoise, x);
    }

    public void measure(MeasurementModel measurementModel, RealVector measuredState) {
        kalmanFilter.measureAndGetState(measurementModel, measuredState);
    }

    public RealVector getCurrentState() {
        return kalmanFilter.getCurrentState();
    }

    @Override
    public void run() {
        predictThread.start();
        encoderThread.start();
        gyroThread.start();
    }
}
