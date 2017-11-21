package org.pattonvillerobotics.commoncode.robotclasses.opencv;

import android.graphics.Bitmap;
import android.os.Environment;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.pattonvillerobotics.commoncode.enums.ColorSensorColor;
import org.pattonvillerobotics.commoncode.robotclasses.opencv.util.Contour;
import org.pattonvillerobotics.commoncode.robotclasses.opencv.util.PhoneOrientation;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JewelColorDetector {

    public static final String TAG = JewelColorDetector.class.getSimpleName();

    private ColorBlobDetector redDetector, blueDetector;
    private PhoneOrientation phoneOrientation;

    private MatOfPoint jewelHolderTape;
    private Vector3D redJewel, blueJewel;

    private Mat grayScaleMat = new Mat();
    private Mat thresholdMat = new Mat();
    private Mat hierarchyMat = new Mat();
    private boolean debug;

    public JewelColorDetector(PhoneOrientation phoneOrientation) {
        if (!ImageProcessor.isInitialized())
            throw new IllegalStateException("OpenCV not initialized!");

        this.phoneOrientation = phoneOrientation;
        redDetector = new ColorBlobDetector(ColorSensorColor.RED);
        blueDetector = new ColorBlobDetector(ColorSensorColor.BLUE);
    }

    public JewelColorDetector(PhoneOrientation phoneOrientation, boolean debug) {
        if (!ImageProcessor.isInitialized())
            throw new IllegalStateException("OpenCV not initialized!");

        this.debug = debug;
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
     * Thresholds the image to find the white tape. Filters the threshold by area, distance from jewels,
     * and shape to find the correct contour.
     *
     * @param rgbaMat a mat of the rgba image that is being processed for color detection
     */
    private void findTapeContour(Mat rgbaMat) {
        Imgproc.cvtColor(rgbaMat, grayScaleMat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.threshold(grayScaleMat, thresholdMat, 240, 255, Imgproc.THRESH_BINARY);

        List<MatOfPoint> possibleTapeContours = new ArrayList<>();

        Imgproc.findContours(thresholdMat, possibleTapeContours, hierarchyMat, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        List<MatOfPoint> filteredArea = new ArrayList<>();

        for (MatOfPoint contour : possibleTapeContours) {
            if (Imgproc.contourArea(contour) > 1000 && (inRange(contour, blueJewel) || inRange(contour, redJewel))) {
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
     * a contour representing the tape between the jewels. If debug is enabled, then it draws on the
     * image and saves it.
     *
     * @param rgbaMat a mat of the rgba image that is wanting to be processed for detection
     */
    public void process(Mat rgbaMat) {
        jewelHolderTape = null;
        redJewel = null;
        blueJewel = null;

        redDetector.process(rgbaMat);
        blueDetector.process(rgbaMat);

        findJewelContours();
        findTapeContour(rgbaMat);


        if (debug && jewelHolderTape != null) {
            ArrayList<MatOfPoint> temp = new ArrayList<>();
            temp.add(jewelHolderTape);
            Imgproc.drawContours(rgbaMat, temp, -1, new Scalar(0, 0, 255), 3);
            Imgproc.circle(rgbaMat, new Point(blueJewel.getX(), blueJewel.getY()), (int) blueJewel.getZ(), new Scalar(0, 255, 0), 3);
            Imgproc.circle(rgbaMat, new Point(redJewel.getX(), redJewel.getY()), (int) redJewel.getZ(), new Scalar(0, 255, 0), 3);

            Imgproc.putText(rgbaMat, "BLUE", new Point(blueJewel.getX(), blueJewel.getY()),
                    Core.FONT_HERSHEY_PLAIN, 3, new Scalar(0, 255, 0), 3);

            Imgproc.putText(rgbaMat, "RED", new Point(redJewel.getX(), redJewel.getY()),
                    Core.FONT_HERSHEY_PLAIN, 3, new Scalar(0, 255, 0), 3);

            Imgproc.putText(rgbaMat, "TAPE", Contour.centroid(jewelHolderTape),
                    Core.FONT_HERSHEY_PLAIN, 3, new Scalar(0, 255, 0), 3);

            Imgproc.putText(rgbaMat, phoneOrientation.toString(), new Point(10, 100),
                    Core.FONT_HERSHEY_PLAIN, 3, new Scalar(0, 255, 0), 3);

            FileOutputStream out;
            Bitmap bmp = Bitmap.createBitmap(rgbaMat.width(), rgbaMat.height(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(rgbaMat, bmp);
            Date date = new Date();
            String fileName = DateFormat.getDateTimeInstance().format(date) + ".png";

            try {
                out = new FileOutputStream(new File(Environment.getExternalStorageDirectory(), fileName));
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                // PNG is a lossless format, the compression factor (100) is ignored
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Finds if a point is within twice the radius of a jewel.
     * @param tape the contour of the tape
     * @param jewel the circle representing a jewel on the image
     * @return true if the distance is less than range
     */
    private boolean inRange(MatOfPoint tape, Vector3D jewel) {
        if (jewel == null) return false;
        Point tapeCenter = Contour.centroid(tape);
        return Math.hypot(tapeCenter.x - jewel.getX(), tapeCenter.y - jewel.getY()) < jewel.getZ() * 2;
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

        if (jewelHolderTape == null) return new JewelColorDetector.Analysis();

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
