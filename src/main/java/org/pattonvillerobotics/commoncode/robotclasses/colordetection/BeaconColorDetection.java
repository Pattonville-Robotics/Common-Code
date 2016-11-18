package org.pattonvillerobotics.commoncode.robotclasses.colordetection;

import android.graphics.Bitmap;
import android.util.Log;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.pattonvillerobotics.commoncode.enums.AllianceColor;
import org.pattonvillerobotics.commoncode.enums.ColorSensorColor;
import org.pattonvillerobotics.commoncode.vision.ftc.resq.Beacon;
import org.pattonvillerobotics.commoncode.vision.util.ScreenOrientation;

import static com.qualcomm.ftccommon.DbgLog.error;


/**
 * Created by greg on 10/16/2016.
 */

public class BeaconColorDetection {

    private Beacon beacon;
    private Beacon.BeaconAnalysis analysis = new Beacon.BeaconAnalysis();
    private boolean openCvInitialized = false;

    public BeaconColorDetection(HardwareMap hardwareMap) {
        initOpenCv(hardwareMap);
        beacon = new Beacon();
        setAnalysisMethod(Beacon.AnalysisMethod.FAST);
    }


    /**
     * Must be called in order to use opencv methods.
     *
     * @param hardwareMap must be provided from the opmode
     */
    private void initOpenCv(HardwareMap hardwareMap) {

        BaseLoaderCallback openCVLoaderCallback = null;
        try {
            openCVLoaderCallback = new BaseLoaderCallback(hardwareMap.appContext) {
                @Override
                public void onManagerConnected(int status) {
                    switch (status) {
                        case LoaderCallbackInterface.SUCCESS: {
                            Log.d("OpenCV", "OpenCV Manager connected!");
                            openCvInitialized = true;
                        }
                        break;
                        default: {
                            super.onManagerConnected(status);
                        }
                        break;
                    }
                }
            };
        } catch (NullPointerException e) {
            error("Could not find OpenCV Manager!\r\n" +
                    "Please install the app from the Google Play Store.");
        }

        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            boolean success = OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, hardwareMap.appContext, openCVLoaderCallback);
            if (!success) {
                Log.e("OpenCV", "Asynchronous initialization failed!");
                error("Could not initialize OpenCV!\r\n" +
                        "Did you install the OpenCV Manager from the Play Store?");
            } else {
                Log.d("OpenCV", "Asynchronous initialization succeeded!");
            }
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            if (openCVLoaderCallback != null)
                openCVLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            else {
                Log.e("OpenCV", "Failed to load OpenCV from package!");
                return;
            }
        }

        while (!openCvInitialized) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public Beacon.BeaconAnalysis getAnalysis() {
        return analysis;
    }

    public Beacon.AnalysisMethod getAnalysisMethod() {
        return beacon.getAnalysisMethod();
    }

    public void setAnalysisMethod(Beacon.AnalysisMethod method) {
        beacon.setAnalysisMethod(method);
    }

    public void setColorToleranceRed(double tolerance) {
        beacon.setColorToleranceRed(tolerance);
    }

    public void setColorToleranceBlue(double tolerance) {
        beacon.setColorToleranceBlue(tolerance);
    }


    /**
     * Analyzes a BitMap for a beacon
     *
     * @param frame the bitmap that is analyzed
     * @return {@link Beacon.BeaconAnalysis} of the frame.
     */
    public Beacon.BeaconAnalysis analyzeFrame(Bitmap frame) {
        Mat rgba = new Mat();
        Mat gray = new Mat();

        Utils.bitmapToMat(frame, rgba);
        Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGBA2GRAY);

        this.analysis = beacon.analyzeFrame(rgba, gray, ScreenOrientation.LANDSCAPE_REVERSE);
        return this.analysis;
    }


    /**
     * Finds the color of the left side of the beacon from the most recent analysis
     *
     * @return the {@link ColorSensorColor} of the left side of the beacon
     */
    public AllianceColor getLeftColor() {
        return getAnalysis().isLeftBlue() ? AllianceColor.BLUE : AllianceColor.RED;
    }


    /**
     * Finds the color of the right side of the beacon from the most recent analysis
     *
     * @return the {@link ColorSensorColor} of the left side of the beacon
     */
    public AllianceColor getRightColor() {
        return getAnalysis().isRightBlue() ? AllianceColor.BLUE : AllianceColor.RED;
    }
}
