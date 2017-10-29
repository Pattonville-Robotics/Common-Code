package org.pattonvillerobotics.commoncode.robotclasses.opencv;

import android.graphics.Bitmap;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.pattonvillerobotics.commoncode.enums.ColorSensorColor;
import org.pattonvillerobotics.commoncode.robotclasses.opencv.util.Contour;
import org.pattonvillerobotics.commoncode.robotclasses.opencv.util.PhoneOrientation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gregbahr on 9/26/17.
 */

public class JewelColorDetector {

    public static final String TAG = JewelColorDetector.class.getSimpleName();
    private static final int TAPE_JEWEL_RANGE = 200;

    private ColorBlobDetector redDetector, blueDetector;
    private PhoneOrientation phoneOrientation;

    private MatOfPoint largestRed, largestBlue, jewelHolderTape;

    private Mat grayScaleMat = new Mat();
    private Mat thresholdMat = new Mat();
    private Mat hierarchyMat = new Mat();
    private List<MatOfPoint> jewelHolderContours = new ArrayList<>();

    public JewelColorDetector(PhoneOrientation phoneOrientation) {
        this.phoneOrientation = phoneOrientation;
        redDetector = new ColorBlobDetector(ColorSensorColor.RED);
        blueDetector = new ColorBlobDetector(ColorSensorColor.BLUE);
    }

    public void process(Bitmap bitmap) {
        Mat rgbaMat = ImageProcessor.processBitmap(bitmap, phoneOrientation);
        process(rgbaMat);
    }

    public void process(Mat rgbaMat) {
        redDetector.process(rgbaMat);
        blueDetector.process(rgbaMat);

        Imgproc.cvtColor(rgbaMat, grayScaleMat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(grayScaleMat, thresholdMat, 225, 255, Imgproc.THRESH_BINARY);

        List<MatOfPoint> tmp = new ArrayList<>();

        Imgproc.findContours(thresholdMat, tmp, hierarchyMat, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        jewelHolderContours.clear();
        for (MatOfPoint contour : tmp) {
            if (Imgproc.contourArea(contour) > 500) {
                jewelHolderContours.add(contour);
            }
        }

        largestBlue = blueDetector.getLargestContour();
        largestRed = redDetector.getLargestContour();
        jewelHolderTape = Contour.findLargestContour(jewelHolderContours);
    }

    private boolean inRange(MatOfPoint tape, MatOfPoint jewel) {
        Point tapeCenter = Contour.centroid(tape);
        Point jewelCenter = Contour.centroid(jewel);

        return Math.hypot(tapeCenter.x - jewelCenter.x, tapeCenter.y - jewelCenter.y) < TAPE_JEWEL_RANGE;
    }

    public JewelColorDetector.Analysis getAnalysis() {
        ColorSensorColor leftJewelColor = null;
        ColorSensorColor rightJewelColor = null;
        Point jewelCenter = Contour.centroid(jewelHolderTape);

        if (largestRed != null && inRange(jewelHolderTape, largestRed)) {
            Point redCenter = Contour.centroid(largestRed);
            if (redCenter.x < jewelCenter.x) {
                leftJewelColor = ColorSensorColor.RED;
            } else if (redCenter.x > jewelCenter.x) {
                rightJewelColor = ColorSensorColor.RED;
            }
        }

        if (largestBlue != null && inRange(jewelHolderTape, largestBlue)) {
            Point blueCenter = Contour.centroid(largestBlue);
            if (blueCenter.x < jewelCenter.x) {
                leftJewelColor = ColorSensorColor.BLUE;
            } else if (blueCenter.x > jewelCenter.x) {
                rightJewelColor = ColorSensorColor.BLUE;
            }
        }

        return new JewelColorDetector.Analysis(leftJewelColor, rightJewelColor);
    }

    public static class Analysis {
        public final ColorSensorColor leftJewelColor;
        public final ColorSensorColor rightJewelColor;

        Analysis(ColorSensorColor leftJewelColor, ColorSensorColor rightJewelColor) {
            this.leftJewelColor = leftJewelColor;
            this.rightJewelColor = rightJewelColor;
        }
    }
}
