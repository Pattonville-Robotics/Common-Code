package org.pattonvillerobotics.commoncode.robotclasses.opencv;

import android.graphics.Bitmap;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.opencv.core.CvType;
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
    private static final int TAPE_JEWEL_RANGE = 200;

    private ColorBlobDetector redDetector, blueDetector;
    private PhoneOrientation phoneOrientation;

    private MatOfPoint jewelHolderTape;
    private Vector3D redJewel, blueJewel;

    private Mat grayScaleMat = new Mat();
    private Mat thresholdMat = new Mat();
    private Mat hierarchyMat = new Mat();

    public JewelColorDetector(PhoneOrientation phoneOrientation) {
        this.phoneOrientation = phoneOrientation;
        redDetector = new ColorBlobDetector(ColorSensorColor.RED);
        blueDetector = new ColorBlobDetector(ColorSensorColor.BLUE);
    }

    /**
     * Converts bitmap to mat and processes it.
     */
    public void process(Bitmap bitmap) {
        Mat rgbaMat = ImageProcessor.processBitmap(bitmap, phoneOrientation);
        process(rgbaMat);
    }

    /**
     * Uses Hough Circles to find which color blobs are jewels. If no Hough Circle found, uses the
     * largest color blob.
     */
    private void findJewelContours() {
        Mat redCircles = new Mat();
        Mat blueCircles = new Mat();

        Imgproc.HoughCircles(redDetector.getThresholdMat(), redCircles, Imgproc.HOUGH_GRADIENT, 3.5, 10000);
        Imgproc.HoughCircles(blueDetector.getThresholdMat(), blueCircles, Imgproc.HOUGH_GRADIENT, 3.5, 10000);

        redCircles:
        for (int i = 0; i < redCircles.cols(); i++) {
            double[] circle = redCircles.get(0, i);

            for (MatOfPoint contour : redDetector.getContours()) {
                if (Imgproc.pointPolygonTest(new MatOfPoint2f(contour.toArray()), new Point(circle[0], circle[1]), false) == 1) {
                    redJewel = new Vector3D(circle);
                    break redCircles;
                }
            }
        }
        if (redJewel == null && Contour.findLargestContour(redDetector.getContours()) != null) {
            Point center = Contour.centroid(Contour.findLargestContour(redDetector.getContours()));
            redJewel = new Vector3D(center.x, center.y, 20);
        }

        blueCircles:
        for (int i = 0; i < blueCircles.cols(); i++) {
            double[] circle = blueCircles.get(0, i);

            for (MatOfPoint contour : blueDetector.getContours()) {
                if (Imgproc.pointPolygonTest(new MatOfPoint2f(contour.toArray()), new Point(circle[0], circle[1]), false) == 1) {
                    blueJewel = new Vector3D(circle);
                    break blueCircles;
                }
            }
        }
        if (blueJewel == null && Contour.findLargestContour(blueDetector.getContours()) != null) {
            Point center = Contour.centroid(Contour.findLargestContour(blueDetector.getContours()));
            blueJewel = new Vector3D(center.x, center.y, 20);
        }
    }

    /**
     * Thresholds the image to find the white tape. Filters the threshold by area and by shape to
     * find the correct contour.
     *
     * @param rgbaMat a mat of the rgba image that is being processed for color detection
     */
    private void findTapeContour(Mat rgbaMat) {
        Imgproc.cvtColor(rgbaMat, grayScaleMat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.threshold(grayScaleMat, thresholdMat, 230, 255, Imgproc.THRESH_BINARY);

        List<MatOfPoint> possibleTapeContours = new ArrayList<>();

        Imgproc.findContours(thresholdMat, possibleTapeContours, hierarchyMat, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        List<MatOfPoint> filteredArea = new ArrayList<>();

        for (MatOfPoint contour : possibleTapeContours) {
            if (Imgproc.contourArea(contour) > 500 && Imgproc.contourArea(contour) < 10000) {
                jewelHolderTape = contour;
                filteredArea.add(contour);
            }
        }

        List<MatOfPoint> rectangles = new ArrayList<>();

        for (MatOfPoint contour : filteredArea) {
            double peri = Imgproc.arcLength(new MatOfPoint2f(contour.toArray()), true);
            MatOfPoint2f approx2f = new MatOfPoint2f();
            MatOfPoint approx = new MatOfPoint();

            Imgproc.approxPolyDP(new MatOfPoint2f(contour.toArray()), approx2f, 0.04 * peri, true);
            approx2f.convertTo(approx, CvType.CV_32S);

            if (approx.size().height == 4) {
                rectangles.add(contour);
            }
        }

        if (rectangles.size() == 0) rectangles = filteredArea;

        Point lowestPoint = new Point(0, 0);
        for (MatOfPoint contour : rectangles) {
            Point contourCenter = Contour.centroid(contour);
            if (contourCenter.y > lowestPoint.y) {
                lowestPoint = contourCenter;
                jewelHolderTape = contour;
            }
        }
    }

    /**
     * Processes the image for color blobs. Filters the color blobs to find the jewels. Then finds
     * a contour representing the tape between the jewels.
     *
     * @param rgbaMat a mat of the rgba image that is wanting to be processed for detection
     */
    public void process(Mat rgbaMat) {
        redDetector.process(rgbaMat);
        blueDetector.process(rgbaMat);

        findJewelContours();
        findTapeContour(rgbaMat);
    }

    /**
     * Finds if a point is within a certain distance of a contour.
     * @param tape the contour of the tape
     * @param jewel the circle representing a jewel on the image
     * @return true if the distance is less than range
     */
    private boolean inRange(MatOfPoint tape, Vector3D jewel) {
        if (jewel == null) return false;
        Point tapeCenter = Contour.centroid(tape);
        return Math.hypot(tapeCenter.x - jewel.getX(), tapeCenter.y - jewel.getY()) < TAPE_JEWEL_RANGE;
    }

    /**
     * Compares the center of the jewels to the center of the found tape and decides
     * what side each jewel is on depending on the x value of the points. Attempts to compare jewels
     * to each other if no tape is found.
     *
     * @return the results of the analysis, if nothing found then both colors are null
     */
    public JewelColorDetector.Analysis getAnalysis() {
        ColorSensorColor leftJewelColor = null;
        ColorSensorColor rightJewelColor = null;

        if (jewelHolderTape == null && redJewel != null && blueJewel != null) {
            if (redJewel.getX() < blueJewel.getX()) {
                return new JewelColorDetector.Analysis(ColorSensorColor.RED, ColorSensorColor.BLUE);
            } else {
                return new JewelColorDetector.Analysis(ColorSensorColor.BLUE, ColorSensorColor.RED);
            }
        } else if (jewelHolderTape == null) {
            return new JewelColorDetector.Analysis();
        }

        Point tapeCenter = Contour.centroid(jewelHolderTape);
        if (redJewel != null) {
            if (redJewel.getX() < tapeCenter.x) {
                leftJewelColor = ColorSensorColor.RED;
            } else if (redJewel.getX() > tapeCenter.x) {
                rightJewelColor = ColorSensorColor.RED;
            }
        }
        if (blueJewel != null) {
            if (blueJewel.getX() < tapeCenter.x) {
                leftJewelColor = ColorSensorColor.BLUE;
            } else if (blueJewel.getX() > tapeCenter.x) {
                rightJewelColor = ColorSensorColor.BLUE;
            }
        }

        return new JewelColorDetector.Analysis(leftJewelColor, rightJewelColor);
    }

    /**
     * Contains the results of an analysis of the jewel holder.
     */
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
