package org.pattonvillerobotics.commoncode.robotclasses.opencv.roverruckus.minerals;

import android.graphics.Bitmap;
import android.os.Environment;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.pattonvillerobotics.commoncode.enums.ColorSensorColor;
import org.pattonvillerobotics.commoncode.robotclasses.opencv.ColorBlobDetector;
import org.pattonvillerobotics.commoncode.robotclasses.opencv.ImageProcessor;
import org.pattonvillerobotics.commoncode.robotclasses.opencv.util.Contour;
import org.pattonvillerobotics.commoncode.robotclasses.opencv.util.PhoneOrientation;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MineralDetector {

    private ColorBlobDetector goldDetector, silverDetector;
    private PhoneOrientation phoneOrientation;

    private boolean debug;

    public MineralDetector(PhoneOrientation phoneOrientation) {
        if (!ImageProcessor.isInitialized())
            throw new IllegalStateException("OpenCV not initialized!");

        this.phoneOrientation = phoneOrientation;
        goldDetector = new ColorBlobDetector(ColorSensorColor.YELLOW);
        silverDetector = new ColorBlobDetector(ColorSensorColor.WHITE);

        debug = false;
    }

    public MineralDetector(PhoneOrientation phoneOrientation, boolean debug) {
        if (!ImageProcessor.isInitialized())
            throw new IllegalStateException("OpenCV not initialized!");

        this.phoneOrientation = phoneOrientation;
        goldDetector = new ColorBlobDetector(ColorSensorColor.YELLOW);
        silverDetector = new ColorBlobDetector(ColorSensorColor.WHITE);

        this.debug = debug;
    }

    public void process(Bitmap bitmap) {
        Mat rgbaMat = ImageProcessor.processBitmap(bitmap, phoneOrientation);

        Rect rectCrop = new Rect(rgbaMat.width()/4, rgbaMat.height()*4/5, rgbaMat.width()*3/4, rgbaMat.height()/5);

        rgbaMat = new Mat(rgbaMat, rectCrop);

        process(rgbaMat);
    }

    public void process(Mat rgbaMat) {
        goldDetector.process(rgbaMat);
        silverDetector.process(rgbaMat);

        if(debug) {
            ArrayList<MatOfPoint> largestGoldContour = new ArrayList<>();
            ArrayList<MatOfPoint> largestSilverContour = new ArrayList<>();

            if(Contour.findLargestContour(goldDetector.getContours()) != null) {

                // adding the largest contours to the holder arrays
                largestGoldContour.add(Contour.findLargestContour(goldDetector.getContours()));

                // drawing contours on the original image in corresponding colors
                Imgproc.drawContours(rgbaMat, largestGoldContour, -1, new Scalar(230, 180, 30), 3);

                // finding the center of the contours
                Point goldCenter = new Point();
                float[] goldRadius = new float[1];
                Imgproc.minEnclosingCircle(new MatOfPoint2f(largestGoldContour.get(0).toArray()), goldCenter, goldRadius);

                // putting the contour area at the center of the contour
                Imgproc.putText(rgbaMat, "Contour Area: "+Imgproc.contourArea(largestGoldContour.get(0)),
                        new Point(goldCenter.x, goldCenter.y),
                        Core.FONT_HERSHEY_PLAIN, 3, new Scalar(230, 180, 30), 3);
            }
            if(Contour.findLargestContour(silverDetector.getContours()) != null) {
                largestSilverContour.add(Contour.findLargestContour(silverDetector.getContours()));

                Imgproc.drawContours(rgbaMat, largestSilverContour, -1, new Scalar(255, 255, 255), 3);

                Point silverCenter = new Point();
                float[] silverRadius = new float[1];
                Imgproc.minEnclosingCircle(new MatOfPoint2f(largestSilverContour.get(0).toArray()), silverCenter, silverRadius);

                Imgproc.putText(rgbaMat, "Contour Area: "+Imgproc.contourArea(largestSilverContour.get(0)),
                        new Point(silverCenter.x, silverCenter.y),
                        Core.FONT_HERSHEY_PLAIN, 3, new Scalar(255, 255, 255), 3);
            }

            // creating the image file
            FileOutputStream out;
            Bitmap bmp = Bitmap.createBitmap(rgbaMat.width(), rgbaMat.height(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(rgbaMat, bmp);
            Date date = new Date();
            String fileName = DateFormat.getDateTimeInstance().format(date) + ".png";

            // saving the image file
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

    public ColorSensorColor getAnalysis() {
        if(Contour.findLargestContour(goldDetector.getContours()) != null &&
                Contour.findLargestContour(silverDetector.getContours()) != null) {
            if(Imgproc.contourArea(Contour.findLargestContour(goldDetector.getContours())) >
                    8000) {
                return ColorSensorColor.YELLOW;
            } else {
                return ColorSensorColor.WHITE;
            }
        } else if(Contour.findLargestContour(goldDetector.getContours()) != null) {
            return ColorSensorColor.YELLOW;
        } else {
            return ColorSensorColor.WHITE;
        }
    }
}