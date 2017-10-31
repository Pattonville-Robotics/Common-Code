package org.pattonvillerobotics.commoncode.robotclasses.opencv;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.pattonvillerobotics.commoncode.enums.ColorSensorColor;
import org.pattonvillerobotics.commoncode.robotclasses.opencv.util.Contour;
import org.pattonvillerobotics.commoncode.robotclasses.opencv.util.PhoneOrientation;

import java.util.ArrayList;
import java.util.List;

public class JewelColorDetector {

    public static final String TAG = JewelColorDetector.class.getSimpleName();
    private static final int TAPE_JEWEL_RANGE = 300;

    private ColorBlobDetector redDetector, blueDetector;
    private PhoneOrientation phoneOrientation;

    private MatOfPoint redJewel, blueJewel, jewelHolderTape;

    private Mat grayScaleMat = new Mat();
    private Mat thresholdMat = new Mat();
    private Mat hierarchyMat = new Mat();

    public JewelColorDetector(PhoneOrientation phoneOrientation) {
        this.phoneOrientation = phoneOrientation;
        redDetector = new ColorBlobDetector(ColorSensorColor.RED);
        blueDetector = new ColorBlobDetector(ColorSensorColor.BLUE);
    }

    public void process(Bitmap bitmap) {
        Log.i(TAG, "Processing bitmap");

        Mat rgbaMat = ImageProcessor.processBitmap(bitmap, phoneOrientation);
        process(rgbaMat);
    }

    private void findJewelContours() {
        Mat redCircles = new Mat();
        Mat blueCircles = new Mat();

        Imgproc.HoughCircles(redDetector.getThresholdMat(), redCircles, Imgproc.HOUGH_GRADIENT, 3.5, 1000);
        Imgproc.HoughCircles(blueDetector.getThresholdMat(), blueCircles, Imgproc.HOUGH_GRADIENT, 3.5, 1000);

        for (int i = 0; i < redCircles.cols(); i++) {
            double[] circle = redCircles.get(0, i);

            for (MatOfPoint contour : redDetector.getContours()) {
                if (Imgproc.pointPolygonTest(new MatOfPoint2f(contour.toArray()), new Point(circle[0], circle[1]), false) == 1) {
                    redJewel = contour;
                    break;
                }
            }
        }

        for (int i = 0; i < blueCircles.cols(); i++) {
            double[] circle = blueCircles.get(0, i);

            for (MatOfPoint contour : blueDetector.getContours()) {
                if (Imgproc.pointPolygonTest(new MatOfPoint2f(contour.toArray()), new Point(circle[0], circle[1]), false) == 1) {
                    blueJewel = contour;
                    break;
                }
            }
        }
    }

    private void findTapeContour(MatOfPoint jewel1, MatOfPoint jewel2, Mat rgbaMat) {
        Imgproc.cvtColor(rgbaMat.clone(), grayScaleMat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(grayScaleMat, thresholdMat, 230, 255, Imgproc.THRESH_BINARY);

        List<MatOfPoint> possibleTapeContours = new ArrayList<>();

        Imgproc.findContours(thresholdMat, possibleTapeContours, hierarchyMat, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        for (MatOfPoint contour : possibleTapeContours) {
            if (Imgproc.contourArea(contour) > 500 && (inRange(contour, jewel1) || inRange(contour, jewel2))) {
                jewelHolderTape = contour;
            }
        }
    }

    public void process(Mat rgbaMat) {
        redDetector.process(rgbaMat);
        blueDetector.process(rgbaMat);

        findJewelContours();
        findTapeContour(blueJewel, redJewel, rgbaMat);
    }

    private boolean inRange(MatOfPoint tape, MatOfPoint jewel) {
        if (jewel == null) return false;

        Point tapeCenter = Contour.centroid(tape);
        Point jewelCenter = Contour.centroid(jewel);

        return Math.hypot(tapeCenter.x - jewelCenter.x, tapeCenter.y - jewelCenter.y) < TAPE_JEWEL_RANGE;
    }

    public JewelColorDetector.Analysis getAnalysis() {
        ColorSensorColor leftJewelColor = null;
        ColorSensorColor rightJewelColor = null;

        if (jewelHolderTape == null) return new JewelColorDetector.Analysis();

        Point tapeCenter = Contour.centroid(jewelHolderTape);

        if (redJewel != null) {
            Point redCenter = Contour.centroid(redJewel);
            if (redCenter.x < tapeCenter.x) {
                leftJewelColor = ColorSensorColor.RED;
            } else if (redCenter.x > tapeCenter.x) {
                rightJewelColor = ColorSensorColor.RED;
            }
        }

        if (blueJewel != null) {
            Point blueCenter = Contour.centroid(blueJewel);
            if (blueCenter.x < tapeCenter.x) {
                leftJewelColor = ColorSensorColor.BLUE;
            } else if (blueCenter.x > tapeCenter.x) {
                rightJewelColor = ColorSensorColor.BLUE;
            }
        }

        return new JewelColorDetector.Analysis(leftJewelColor, rightJewelColor);
    }

    public static class Analysis {
        public final ColorSensorColor leftJewelColor;
        public final ColorSensorColor rightJewelColor;

        Analysis() {
            this.leftJewelColor = null;
            this.rightJewelColor = null;
        }

        Analysis(ColorSensorColor leftJewelColor, ColorSensorColor rightJewelColor) {
            this.leftJewelColor = leftJewelColor;
            this.rightJewelColor = rightJewelColor;
        }
    }
}
