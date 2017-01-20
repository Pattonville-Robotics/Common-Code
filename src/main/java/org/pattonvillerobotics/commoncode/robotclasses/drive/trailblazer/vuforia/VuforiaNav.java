package org.pattonvillerobotics.commoncode.robotclasses.drive.trailblazer.vuforia;

import android.graphics.Bitmap;
import android.util.Log;

import com.vuforia.HINT;
import com.vuforia.Image;
import com.vuforia.Vuforia;

import org.apache.commons.math3.util.FastMath;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;

import java.util.List;

/**
 * Created by bahrg on 10/29/16.
 */

public class VuforiaNav {

    public static final float MM_PER_INCH = 25.4f;
    private static final String TAG = "VuforiaNav";
    private VuforiaTrackables beacons;
    private boolean isActivated;
    private VuforiaLocalizerImplSubclass vuforia;
    private OpenGLMatrix lastLocation;
    private VuforiaLocalizer.Parameters parameters;

    public VuforiaNav(VuforiaParameters parameters) {
        this.parameters = new VuforiaLocalizer.Parameters(parameters.getCameraMonitorViewId());
        this.parameters.cameraDirection = parameters.getCameraDirection();
        this.parameters.vuforiaLicenseKey = parameters.getLicenseKey();
        this.parameters.cameraMonitorFeedback = VuforiaLocalizer.Parameters.CameraMonitorFeedback.AXES;
        this.parameters.useExtendedTracking = true;
        vuforia = new VuforiaLocalizerImplSubclass(this.parameters);
        beacons = vuforia.loadTrackablesFromAsset("FTC_2016-17");

        beacons.get(0).setName("Wheels");
        beacons.get(1).setName("Tools");
        beacons.get(2).setName("Lego");
        beacons.get(3).setName("Gears");

        setPhoneInformation(parameters.getPhoneLocation());
        setBeaconLocations(parameters.getBeaconLocations());

        isActivated = false;
    }

    public VuforiaTrackables getBeacons() {
        return beacons;
    }

    /**
     * @param locations - a list of the places on the field where the beacons are located
     */
    private void setBeaconLocations(List<OpenGLMatrix> locations) {
        for (int i = 0; i < beacons.size(); i++) {
            beacons.get(i).setLocation(locations.get(i));
        }
    }

    /**
     * @param location - location of the phone on the robot
     */
    private void setPhoneInformation(OpenGLMatrix location) {
        for (VuforiaTrackable beacon : beacons) {
            ((VuforiaTrackableDefaultListener) beacon.getListener()).setPhoneInformation(location, parameters.cameraDirection);
        }
    }

    /**
     * called to begin tracking
     */
    public void activate() {
        beacons.activate();
        isActivated = true;

        Vuforia.setHint(HINT.HINT_MAX_SIMULTANEOUS_IMAGE_TARGETS, 4);
    }

    public void deactivate() {
        beacons.deactivate();
        isActivated = false;
    }

    /**
     * @return {@link OpenGLMatrix} location of the current beacon being tracked
     */
    public OpenGLMatrix getNearestBeaconLocation() {
        if (!isActivated) {
            throw new IllegalStateException("Vuforia must be activated to track beacons.");
        }
        for (VuforiaTrackable beacon : beacons) {
            OpenGLMatrix robotLocationTransform = ((VuforiaTrackableDefaultListener) beacon.getListener()).getUpdatedRobotLocation();
            if (robotLocationTransform != null) {
                lastLocation = robotLocationTransform;
                return robotLocationTransform;
            }
        }
        return null;
    }

    /**
     * @return the most recent tracked distance perpendicular to the picture
     */
    public double getDistance() {
        VectorF translation = lastLocation.getTranslation();
        return translation.getData()[0] / MM_PER_INCH;
    }

    /**
     * @return the most recent position data of the robot
     */
    public float[] getLocation() {
        if (lastLocation != null) {
            VectorF translation = lastLocation.getTranslation();
            if (translation != null)
                return translation.getData();
        }
        Log.w(TAG, "No target found.");
        return null;
    }

    public OpenGLMatrix getLastLocation() {
        return lastLocation;
    }

    /**
     * @return the most recently tracked distance horizontally off the picture
     */
    public double getxPos() {
        VectorF translation = lastLocation.getTranslation();
        return translation.getData()[1] / MM_PER_INCH;
    }


    public double getAngle() {
        return FastMath.toDegrees(FastMath.atan(getDistance() / getxPos()));
    }

    /**
     * @return most recently tracked angle from the phone to the picture
     */
    public double getHeading() {
        return Orientation.getOrientation(getLastLocation(), AxesReference.EXTRINSIC, AxesOrder.XYZ, AngleUnit.DEGREES).thirdAngle;
    }


    /**
     * @return {@link Bitmap} of the most recent frame from vuforia
     */
    public Bitmap getImage() {
        Image img = vuforia.getImage();

        if (img != null) {
            Bitmap bm = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.RGB_565);
            bm.copyPixelsFromBuffer(img.getPixels());
            return bm;
        } else {
            return null;
        }
    }

    public boolean isActivated() {
        return isActivated;
    }
}
