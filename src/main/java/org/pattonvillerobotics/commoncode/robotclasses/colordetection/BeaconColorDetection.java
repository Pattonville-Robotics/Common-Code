package org.pattonvillerobotics.commoncode.robotclasses.colordetection;

import android.graphics.Bitmap;
import android.util.Log;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
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
    private boolean usingSimpleDetection;

    public BeaconColorDetection(HardwareMap hardwareMap) {
        initOpenCv(hardwareMap);
        beacon = new Beacon();
        setAnalysisMethod(Beacon.AnalysisMethod.FAST);
    }

    public BeaconColorDetection(HardwareMap hardwareMap, boolean usingSimpleDetection) {
        this.usingSimpleDetection = usingSimpleDetection;
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
    public Beacon.BeaconAnalysis analyzeFrame(Bitmap frame, ScreenOrientation screenOrientation) {
        Mat rgba = new Mat();
        Mat gray = new Mat();

        Utils.bitmapToMat(frame, rgba);
        Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGBA2GRAY);
        if(usingSimpleDetection) {

            int sensorSize = 100;
            double width = rgba.width();
            double height = rgba.height();

            Rect sensorAreaLeft = new Rect(new Point(((width - sensorSize) / 2) - 100, ((height - sensorSize) / 2) - 100),
                    new Point(((width + sensorSize) / 2) - 100, ((height + sensorSize) / 2) - 100));

            Rect sensorAreaRight = new Rect(new Point(((width - sensorSize) / 2) + 100, ((height - sensorSize) / 2) + 100),
                    new Point(((width + sensorSize) / 2) + 100, ((height + sensorSize) / 2) + 100));


            this.analysis = new Beacon.BeaconAnalysis(analyzeRect(rgba, sensorAreaLeft), analyzeRect(rgba, sensorAreaRight));

        } else {
            this.analysis = beacon.analyzeFrame(rgba, gray, screenOrientation);
        }
        return this.analysis;
    }

    public Beacon.BeaconColor analyzeRect(Mat rgba, Rect sensorArea) {
        Mat sensorImage = new Mat(rgba, sensorArea);
        Mat hsv = new Mat();
        Imgproc.cvtColor(sensorImage, hsv, Imgproc.COLOR_RGB2HSV);

        Scalar averageColor = Core.mean(hsv);

        int hue = (int)averageColor.val[0];
        int saturation = (int)averageColor.val[1];
        int brightness = (int)averageColor.val[2];

        if((hue > 140 || hue < 40) && (saturation < 200 && saturation > 100) &&
                (brightness < 255 && brightness > 50)) {
            return Beacon.BeaconColor.RED;
        } else if((hue < 120 && hue > 80) && (saturation < 200 && saturation > 30) &&
                (brightness < 255 && brightness > 50)) {
            return Beacon.BeaconColor.BLUE;
        } else {
            return Beacon.BeaconColor.UNKNOWN;
        }

    }


    /**
     * Finds the color of the left side of the beacon from the most recent analysis
     *
     * @return the {@link ColorSensorColor} of the left side of the beacon
     */
    public ColorSensorColor getLeftColor() {
        return toColorSensorColor(getAnalysis().getStateLeft());
    }


    /**
     * Finds the color of the right side of the beacon from the most recent analysis
     *
     * @return the {@link ColorSensorColor} of the left side of the beacon
     */
    public ColorSensorColor getRightColor() {
        return toColorSensorColor(getAnalysis().getStateRight());
    }

    public ColorSensorColor toColorSensorColor(Beacon.BeaconColor beaconColor) {
        switch (beaconColor) {
            case RED_BRIGHT:
            case RED:
                return ColorSensorColor.RED;
            case BLUE_BRIGHT:
            case BLUE:
                return ColorSensorColor.BLUE;
            default:
                return ColorSensorColor.GREEN;
        }
    }
}
