package org.pattonvillerobotics.commoncode.robotclasses.opencv;

import android.graphics.Bitmap;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
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

    public void process(Bitmap bitmap) {
        Mat rgbaMat = ImageProcessor.processBitmap(bitmap, phoneOrientation);
        process(rgbaMat);
    }

    private void findJewelContours() {
        Mat redCircles = new Mat();
        Mat blueCircles = new Mat();

        Imgproc.HoughCircles(redDetector.getThresholdMat(), redCircles, Imgproc.HOUGH_GRADIENT, 3.5, 10000);
        Imgproc.HoughCircles(blueDetector.getThresholdMat(), blueCircles, Imgproc.HOUGH_GRADIENT, 3.5, 10000);

        for (int i = 0; i < redCircles.cols(); i++) {
            double[] circle = redCircles.get(0, i);

            for (MatOfPoint contour : redDetector.getContours()) {
                if (Imgproc.pointPolygonTest(new MatOfPoint2f(contour.toArray()), new Point(circle[0], circle[1]), false) == 1) {
                    //Imgproc.circle(rgbaMat, new Point(circle[0], circle[1]), (int)circle[2], new Scalar(0, 255, 0), 3);
                    redJewel = new Vector3D(circle);
                    break;
                }
            }
        }
        if (redJewel == null) {
            Point center = Contour.centroid(Contour.findLargestContour(redDetector.getContours()));
            redJewel = new Vector3D(center.x, center.y, 20);
        }

        for (int i = 0; i < blueCircles.cols(); i++) {
            double[] circle = blueCircles.get(0, i);

            for (MatOfPoint contour : blueDetector.getContours()) {
                if (Imgproc.pointPolygonTest(new MatOfPoint2f(contour.toArray()), new Point(circle[0], circle[1]), false) == 1) {
                    blueJewel = new Vector3D(circle);
                    //Imgproc.circle(rgbaMat, new Point(circle[0], circle[1]), (int)circle[2], new Scalar(0, 255, 0), 3);
                    break;
                }
            }
        }
        if (blueJewel == null) {
            Point center = Contour.centroid(Contour.findLargestContour(redDetector.getContours()));
            blueJewel = new Vector3D(center.x, center.y, 20);
        }
    }

    private void findTapeContour(Vector3D jewel1, Vector3D jewel2, Mat rgbaMat) {
        Imgproc.cvtColor(rgbaMat, grayScaleMat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.threshold(grayScaleMat, thresholdMat, 230, 255, Imgproc.THRESH_BINARY);

        List<MatOfPoint> possibleTapeContours = new ArrayList<>();

        Imgproc.findContours(thresholdMat, possibleTapeContours, hierarchyMat, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        List<MatOfPoint> filteredArea = new ArrayList<>();

        for (MatOfPoint contour : possibleTapeContours) {
            if (Imgproc.contourArea(contour) > 1000 && (inRange(contour, jewel1) || inRange(contour, jewel2))) {
                jewelHolderTape = contour;
                filteredArea.add(contour);
            }
        }

        Point lowestPoint = new Point(0, 0);
        for (MatOfPoint contour : filteredArea) {
            Point contourCenter = Contour.centroid(contour);
            if (contourCenter.y > lowestPoint.y) {
                lowestPoint = contourCenter;
                jewelHolderTape = contour;
            }
        }
        //Imgproc.circle(rgbaMat, lowestPoint, 3, new Scalar(255, 0, 0), 3);
    }

    public void process(Mat rgbaMat) {
        redDetector.process(rgbaMat);
        blueDetector.process(rgbaMat);

        findJewelContours();
        findTapeContour(blueJewel, redJewel, rgbaMat);

        /*Bitmap bmp = Bitmap.createBitmap(rgbaMat.width(), rgbaMat.height(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(rgbaMat, bmp);
        try {
            FileOutputStream fos = hardwareMap.appContext.openFileOutput("testPic.png", Context.MODE_PRIVATE);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            Log.i("OpenCVTest", "Image saved.");
            Log.i("OpenCVTest", hardwareMap.appContext.getFilesDir().getAbsolutePath());
        } catch (Exception e) {
            Log.e("OpenCVTest", e.getMessage());
        }*/
    }

    private boolean inRange(MatOfPoint tape, Vector3D jewel) {
        if (jewel == null) return false;
        Point tapeCenter = Contour.centroid(tape);
        return Math.hypot(tapeCenter.x - jewel.getX(), tapeCenter.y - jewel.getY()) < TAPE_JEWEL_RANGE;
    }

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
