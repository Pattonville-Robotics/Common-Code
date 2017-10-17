package org.pattonvillerobotics.commoncode.robotclasses.opencv;

import android.util.Log;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;


/**
 * Created by greg on 10/4/2017.
 */

public abstract class ImageProcessor {

    private static final String TAG = ImageProcessor.class.getSimpleName();

    private boolean initialized;
    private HardwareMap hardwareMap;

    public ImageProcessor(HardwareMap hardwareMap) {
        this.hardwareMap = hardwareMap;

        initOpenCV();
    }

    private void initOpenCV() {
        BaseLoaderCallback baseLoaderCallback = null;

        try {
            baseLoaderCallback = new BaseLoaderCallback(hardwareMap.appContext) {
                @Override
                public void onManagerConnected(int status) {
                    switch (status) {
                        case LoaderCallbackInterface.SUCCESS:
                            Log.i(TAG, "OpenCV Loaded Successfully!");
                            initialized = true;
                            break;
                        default:
                            super.onManagerConnected(status);
                    }
                }
            };
        } catch (NullPointerException e) {
            Log.e(TAG, "Failed to load OpenCV app, it can be found on the playstore.");
        }

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            boolean success = OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, hardwareMap.appContext, baseLoaderCallback);
            if (!success) {
                Log.e(TAG, "Asynchronous initialization failed!");
                Log.e(TAG, "Could not initialize OpenCV!\r\n" +
                        "Did you install the OpenCV Manager from the Play Store?");
            } else {
                Log.d(TAG, "Asynchronous initialization succeeded!");
            }
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            if (baseLoaderCallback != null)
                baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            else {
                Log.e(TAG, "Failed to load OpenCV from package!");
                return;
            }
        }

        while (!initialized) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void waitForOpenCVInit() {
        while (!initialized) {
            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
