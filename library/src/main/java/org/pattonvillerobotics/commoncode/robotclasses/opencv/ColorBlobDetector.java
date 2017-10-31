package org.pattonvillerobotics.commoncode.robotclasses.opencv;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.pattonvillerobotics.commoncode.enums.ColorSensorColor;

import java.util.ArrayList;
import java.util.List;

public class ColorBlobDetector {

    private final List<MatOfPoint> contours = new ArrayList<>();
    private final Mat blurMat = new Mat();
    private final Mat thresholdMat = new Mat();
    private final Mat hierarchyMat = new Mat();
    private final Mat hsvMat = new Mat();
    private Scalar lowerBoundHSV;
    private Scalar upperBoundHSV;

    public ColorBlobDetector(Scalar lowerBoundHSV, Scalar upperBoundHSV) {
        setHSVBounds(lowerBoundHSV, upperBoundHSV);
    }

    public ColorBlobDetector(ColorSensorColor color) {
        setHSVBounds(color);
    }

    public void setHSVBounds(Scalar lower, Scalar upper) {
        lowerBoundHSV = lower;
        upperBoundHSV = upper;
    }

    public void setHSVBounds(ColorSensorColor color) {
        switch (color) {
            case RED:
                setHSVBounds(new Scalar(160, 40, 0), new Scalar(180, 255, 255));
                break;
            case BLUE:
                setHSVBounds(new Scalar(90, 40, 0), new Scalar(110, 255, 255));
                break;
            case GREEN:
                setHSVBounds(new Scalar(45, 40, 0), new Scalar(75, 255, 255));
                break;
            default:
                throw new IllegalArgumentException("Must provide RED, BLUE, or GREEN!");
        }
    }

    public void process(Mat rgbaMat) {
        Imgproc.blur(rgbaMat, blurMat, new Size(71, 71));

        Imgproc.cvtColor(blurMat, hsvMat, Imgproc.COLOR_BGR2HSV);
        Core.inRange(hsvMat, lowerBoundHSV, upperBoundHSV, thresholdMat);

        List<MatOfPoint> tmp = new ArrayList<>();

        Imgproc.findContours(thresholdMat, tmp, hierarchyMat, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // filters out super small contours
        contours.clear();
        for (MatOfPoint contour : tmp) {
            if (Imgproc.contourArea(contour) > 500) {
                contours.add(contour);
            }
        }
    }

    public Mat getThresholdMat() {
        return thresholdMat;
    }

    public List<MatOfPoint> getContours() {
        return contours;
    }
}
