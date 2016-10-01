package org.pattonvillerobotics.commoncode.robotclasses.drive;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;

/**
 * Created by Mitchell on 10/1/2016.
 */

public class VuforiaDrive extends EncoderDrive {

    private static final double MILLIMETERS_PER_INCH = 25.4;

    private final VuforiaLocalizer vuforia;
    private final VuforiaTrackable[] allTrackables;
    private final OpenGLMatrix[] lastRobotPositions;

    public VuforiaDrive(HardwareMap hardwareMap, LinearOpMode linearOpMode, RobotParameters robotParameters) {
        super(hardwareMap, linearOpMode, robotParameters);
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters(com.qualcomm.ftcrobotcontroller.R.id.cameraMonitorViewId);
        parameters.vuforiaLicenseKey = "AT54AxL/////AAAAGTiul5Yjd0WprHC+42IL79BVCudCN3SSdKaMqI48s3HmVcB7xcljYLBODi/StZzfKTIKJYcyD1O/zNY44QWmqQpp9mOws95Vwe+oen+NuleQRFTlmnXbG2iD7yiHyhULiTZndjFZOGAOoA2xkiOhDxTnRxwbXT7ifrBwap+qVQaSL1QUr040LWUYhVO3VsbyI8Q2EKrinVtThHHtvli43qWTeAaCoGZ1mzVrepHMwEEE/ZZNlxUx/btepZObZRkw07JeI6LwkZMe/Prk+sBqWgz54vAWZ/g4lT/JBxaOhH3MnPkeAb/0jXgncXXGPJjDRLCxVo/5bW9nv3pHEcGm7JqQ/8cqoM+ZHN3l+UUIBznD";
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;
        vuforia = ClassFactory.createVuforiaLocalizer(parameters);

        VuforiaTrackables stonesAndChips = this.vuforia.loadTrackablesFromAsset("StonesAndChips");

        VuforiaTrackable redTarget = stonesAndChips.get(0);
        redTarget.setName("RedTarget");  // Stones
        VuforiaTrackable blueTarget = stonesAndChips.get(1);
        blueTarget.setName("BlueTarget");  // Chips

        allTrackables = new VuforiaTrackable[]{redTarget, blueTarget};
        lastRobotPositions = new OpenGLMatrix[2];

        OpenGLMatrix redTargetLocationOnField = OpenGLMatrix
                /* Then we translate the target off to the RED WALL. Our translation here
                is a negative translation in X.*/
                ////                .translation(-mmFTCFieldWidth/2, 0, 0)
                //// put target at 0,0,0
                .translation(0, 0, 0)
                .multiplied(Orientation.getRotationMatrix(
                        /* First, in the fixed (field) coordinate system, we rotate 90deg in X, then 90 in Z */
                        AxesReference.EXTRINSIC, AxesOrder.XZX,
                        AngleUnit.DEGREES, 90, 90, 0));
        redTarget.setLocation(redTargetLocationOnField);
        OpenGLMatrix blueTargetLocationOnField = OpenGLMatrix
                /* Then we translate the target off to the Blue Audience wall.
                Our translation here is a positive translation in Y.*/
                ////                .translation(0, mmFTCFieldWidth/2, 0)
                //// put target at 0,0,0
                .translation(0, 0, 0)
                .multiplied(Orientation.getRotationMatrix(
                        /* First, in the fixed (field) coordinate system, we rotate 90deg in X */
                        ////AxesReference.EXTRINSIC, AxesOrder.XZX,
                        ////AngleUnit.DEGREES, 90, 0, 0));
                        //// put at same location as red target
                        /* First, in the fixed (field) coordinate system, we rotate 90deg in X, then 90 in Z */
                        AxesReference.EXTRINSIC, AxesOrder.XZX,
                        AngleUnit.DEGREES, 90, 90, 0));
        blueTarget.setLocation(blueTargetLocationOnField);

        OpenGLMatrix phoneLocationOnRobot = OpenGLMatrix
                ////.translation(mmBotWidth/2,0,0)
                //// keep phone in center of robot
                .translation(0, 0, 0)
                .multiplied(Orientation.getRotationMatrix(
                        AxesReference.EXTRINSIC, AxesOrder.YZY,
                        AngleUnit.DEGREES, -90, 0, 0));

        ((VuforiaTrackableDefaultListener) redTarget.getListener()).setPhoneInformation(phoneLocationOnRobot, parameters.cameraDirection);
        ((VuforiaTrackableDefaultListener) blueTarget.getListener()).setPhoneInformation(phoneLocationOnRobot, parameters.cameraDirection);

        stonesAndChips.activate();
    }
}
