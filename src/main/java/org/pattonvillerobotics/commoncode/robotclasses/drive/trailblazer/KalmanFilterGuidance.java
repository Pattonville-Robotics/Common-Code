package org.pattonvillerobotics.commoncode.robotclasses.drive.trailblazer;

import android.util.Log;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.apache.commons.math3.filter.DefaultMeasurementModel;
import org.apache.commons.math3.filter.MeasurementModel;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
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
                    {0, 0, 0, 1, 0, 0, 0, 0}
            }),
            new Array2DRowRealMatrix(new double[][]{
                    {2, 0},
                    {0, 2}
            })
    );
    private static final MeasurementModel GYRO_MEASUREMENT_MODEL = new DefaultMeasurementModel(
            new Array2DRowRealMatrix(new double[][]{
                    {0, 0, 0, 0, 0, 0, 0, 1}
            }),
            new Array2DRowRealMatrix(new double[][]{
                    {100}
            })
    );
    private static final double S_TO_NS = TimeUnit.SECONDS.toNanos(1);
    public final KalmanFilter kalmanFilter;
    private final Thread encoderThread;
    private final Thread predictThread;
    private final Thread gyroThread;

    public KalmanFilterGuidance(final LinearOpMode linearOpMode, final AbstractComplexDrive complexDrive, final ModernRoboticsI2cGyro gyro) {

        predictThread = new Thread(new Runnable() {
            long lastTimeNS = System.nanoTime();

            @Override
            public void run() {
                while (!linearOpMode.isStopRequested()) {
                    synchronized (kalmanFilter) {
                        long nowTimeNS = System.nanoTime();
                        double elapsedTimeS = (nowTimeNS - lastTimeNS) / S_TO_NS;
                        lastTimeNS = nowTimeNS;

                        Log.e("Predict", "Elapsed time: " + elapsedTimeS);

                        kalmanFilter.predictNextState(elapsedTimeS);
                    }
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
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
                while (!linearOpMode.isStopRequested()) {
                    synchronized (kalmanFilter) {
                        long nowTimeNS = System.nanoTime();
                        double elapsedTimeS = (nowTimeNS - lastTimeNS) / S_TO_NS;
                        lastTimeNS = nowTimeNS;

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
                        //double approximateAngularVelocity = complexDrive.degreesToInchesInverse((deltaRightInches - deltaLeftInches) / 2) / elapsedTimeS;

                        RealVector measurement = new ArrayRealVector(new double[]{vx, vy});//, approximateAngularVelocity});

                        Log.e("Encoder", "Updating measurement of " + measurement + " in time " + elapsedTimeS);

                        kalmanFilter.measureAndGetState(ENCODER_MEASUREMENT_MODEL, measurement);
                    }
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        gyroThread = new Thread(new Runnable() {
            long lastTimeNS = System.nanoTime();

            @Override
            public void run() {
                while (!linearOpMode.isStopRequested()) {
                    synchronized (kalmanFilter) {
                        long nowTimeNS = System.nanoTime();
                        double elapsedTimeS = (nowTimeNS - lastTimeNS) / S_TO_NS;
                        lastTimeNS = nowTimeNS;
                        double angularVelocity = gyro.rawZ() / 52.416666667; //TODO find conversion from raw to true values
                        RealVector measurement = new ArrayRealVector(new double[]{angularVelocity});

                        Log.e("Gyro", "Updating measurement of " + measurement + " in time " + elapsedTimeS);

                        kalmanFilter.measureAndGetState(GYRO_MEASUREMENT_MODEL, measurement);
                    }
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        /*
        SensorManager mSensorManager = (SensorManager) linearOpMode.hardwareMap.appContext.getSystemService(Context.SENSOR_SERVICE);
        Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
        mSensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        }, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
        */
        /*                                                   x  y  vx vy ax ay θ  ω*/
        RealVector x = new ArrayRealVector(new double[]{0, 0, 0, 0, 0, 0, 0, 0});
        RealMatrix processNoise = MatrixUtils.createRealIdentityMatrix(x.getDimension()).scalarMultiply(0.1);
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

    public void stopThreads() {
        predictThread.interrupt();
        encoderThread.interrupt();
        gyroThread.interrupt();
    }
}
