package org.pattonvillerobotics.commoncode.robotclasses.opencv.roverruckus.minerals;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.pattonvillerobotics.commoncode.robotclasses.opencv.util.Contour;

import java.util.ArrayList;
import java.util.List;

public class MineralAnalyzer {

    public static Vector3D analyzeFast(List<MatOfPoint> contours) {
        MatOfPoint2f approx = new MatOfPoint2f();

        ArrayList<MatOfPoint> filtered = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            double peri = Imgproc.arcLength(new MatOfPoint2f(contour.toArray()), true);
            Imgproc.approxPolyDP(new MatOfPoint2f(contour.toArray()), approx, 0.01 * peri, true);
            filtered.add(contour);
        }
        MatOfPoint largest = Contour.findLargestContour(filtered);
        if (largest != null) {
            Point center = new Point();
            float[] radius = new float[1];
            Imgproc.minEnclosingCircle(new MatOfPoint2f(largest.toArray()), center, radius);
            return new Vector3D(center.x, center.y, radius[0]);
        }

        return null;
    }

    public static Vector3D analyzeFast(MatOfPoint largestContour) {
        Point center = new Point();
        float[] radius = new float[1];
        Imgproc.minEnclosingCircle(new MatOfPoint2f(largestContour.toArray()), center, radius);
        return new Vector3D(center.x, center.y, radius[0]);
    }

}
