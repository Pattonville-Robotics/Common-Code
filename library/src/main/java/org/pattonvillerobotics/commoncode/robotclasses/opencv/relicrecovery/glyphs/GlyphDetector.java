package org.pattonvillerobotics.commoncode.robotclasses.opencv.relicrecovery.glyphs;

import android.graphics.Bitmap;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.pattonvillerobotics.commoncode.robotclasses.opencv.ImageProcessor;
import org.pattonvillerobotics.commoncode.robotclasses.opencv.util.Contour;
import org.pattonvillerobotics.commoncode.robotclasses.opencv.util.PhoneOrientation;

import java.util.ArrayList;
import java.util.List;

public class GlyphDetector {

    public static final String TAG = GlyphDetector.class.getSimpleName();
    private final Mat hierarchyMat = new Mat();
    private PhoneOrientation phoneOrientation;
    private boolean debug;
    private List<MatOfPoint> glyphs;
    private double CANNY_THRESHHOLD = 15;
    private Mat grayScaleMat = new Mat();
    private Mat canny = new Mat();
    private Mat structuringElement = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(5, 5));
    private Mat processed = new Mat();

    public GlyphDetector(PhoneOrientation phoneOrientation) {
        if (!ImageProcessor.isInitialized())
            throw new IllegalStateException("OpenCV not initialized!");

        this.phoneOrientation = phoneOrientation;
    }

    public GlyphDetector(PhoneOrientation phoneOrientation, boolean debug) {
        if (!ImageProcessor.isInitialized())
            throw new IllegalStateException("OpenCV not initialized!");

        this.debug = debug;
        this.phoneOrientation = phoneOrientation;
    }

    public void process(Bitmap bitMap) {
        Mat rgbaMat = ImageProcessor.processBitmap(bitMap, phoneOrientation);
        process(rgbaMat);
    }

    public void process(Mat mat) {
        Imgproc.cvtColor(mat, grayScaleMat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.pyrDown(grayScaleMat, grayScaleMat);
        Imgproc.pyrDown(grayScaleMat, grayScaleMat);

        Imgproc.Canny(grayScaleMat, canny, CANNY_THRESHHOLD, CANNY_THRESHHOLD * 3);
        Imgproc.morphologyEx(canny, processed, Imgproc.MORPH_CLOSE, structuringElement);

        findContours(processed);
    }

    private void findContours(Mat mat) {
        List<MatOfPoint> tmp = new ArrayList<>();

        Imgproc.findContours(mat, glyphs, hierarchyMat, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        MatOfPoint largest = Contour.findLargestContour(tmp);
        double maxArea = 0;
        if (largest != null) {
            maxArea = Imgproc.contourArea(largest);
        }

        // filters out super small contours
        glyphs.clear();
        for (MatOfPoint contour : tmp) {
            if (Imgproc.contourArea(contour) > maxArea * .1) {
                Core.multiply(contour, new Scalar(4, 4), contour);
                glyphs.add(contour);
            }
        }
    }

    public List<MatOfPoint> getGlyphContours() {
        return glyphs;
    }
}
