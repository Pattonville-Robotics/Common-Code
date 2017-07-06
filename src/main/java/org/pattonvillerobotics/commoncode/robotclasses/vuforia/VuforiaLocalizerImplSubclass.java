package org.pattonvillerobotics.commoncode.robotclasses.vuforia;

import com.qualcomm.robotcore.util.RobotLog;
import com.vuforia.Frame;
import com.vuforia.Image;
import com.vuforia.PIXEL_FORMAT;
import com.vuforia.State;
import com.vuforia.Vuforia;

import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.internal.VuforiaLocalizerImpl;

/**
 * Created by bahrg on 10/20/16.
 */

// A hack that works around lack of access in v2.2 to camera image data when Vuforia is running
// Note: this may or may not be supported in future releases.
class VuforiaLocalizerImplSubclass extends VuforiaLocalizerImpl {

    private Image rgba;

    VuforiaLocalizerImplSubclass(VuforiaLocalizer.Parameters parameters) {
        super(parameters);
        stopAR();
        clearGlSurface();

        this.vuforiaCallback = new VuforiaCallbackSubclass();
        startAR();

        // Optional: set the pixel format(s) that you want to have in the callback
        Vuforia.setFrameFormat(PIXEL_FORMAT.RGB565, true);
    }

    public Image getImage() {
        return rgba;
    }

    void clearGlSurface() {
        if (this.glSurfaceParent != null) {
            appUtil.synchronousRunOnUiThread(new Runnable() {
                @Override
                public void run() {
                    glSurfaceParent.removeAllViews();
                    glSurfaceParent.getOverlay().clear();
                    glSurface = null;
                }
            });
        }
    }

    static class CloseableFrame extends Frame {
        public CloseableFrame(Frame other) { // clone the frame so we can be useful beyond callback
            super(other);
        }

        public void close() {
            super.delete();
        }
    }

    class VuforiaCallbackSubclass extends VuforiaLocalizerImpl.VuforiaCallback {

        @Override
        public synchronized void Vuforia_onUpdate(State state) {
            super.Vuforia_onUpdate(state);
            // We wish to accomplish two things: (a) get a clone of the Frame so we can use
            // it beyond the callback, and (b) get a variant that will allow us to proactively
            // reduce memory pressure rather than relying on the garbage collector (which here
            // has been observed to interact poorly with the image data which is allocated on a
            // non-garbage-collected heap). Note that both of this concerns are independent of
            // how the Frame is obtained in the first place.
            CloseableFrame frame = new CloseableFrame(state.getFrame());
            RobotLog.vv(TAG, "received Vuforia frame#=%d", frame.getIndex());

            long imgnum = frame.getNumImages();

            for (int i = 0; i < imgnum; i++) {
                if (frame.getImage(i).getFormat() == PIXEL_FORMAT.RGB565) {
                    rgba = frame.getImage(i);
                }
            }
            frame.close();
        }
    }

}
