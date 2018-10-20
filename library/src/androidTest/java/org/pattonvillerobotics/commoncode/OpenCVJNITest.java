package org.pattonvillerobotics.commoncode;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencv.engine.OpenCVEngineInterface;
import org.pattonvillerobotics.commoncode.robotclasses.opencv.ImageProcessor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 * Created by Mitchell on 10/31/2017.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class OpenCVJNITest {

    @Test
    public void testInitJNI() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final AtomicInteger status = new AtomicInteger(-1);
        Class<OpenCVEngineInterface> clazz = OpenCVEngineInterface.class;
        Log.i("Debug", clazz.getName());

        LinearOpMode linearOpMode = new LinearOpMode() {
            @Override
            public void runOpMode() throws InterruptedException {
            }
        };
        linearOpMode.start();

        assertThat(ImageProcessor.initOpenCV(InstrumentationRegistry.getContext(), linearOpMode), is(true));
/*
        assertThat(
                OpenCVLoader.initAsync(OPENCV_VERSION, InstrumentationRegistry.getContext(),
                        new BaseLoaderCallback(InstrumentationRegistry.getContext()) {
                            @Override
                            public void onManagerConnected(int s) {
                                status.set(s);
                                countDownLatch.countDown();
                                super.onManagerConnected(s);
                            }

                            @Override
                            public void onPackageInstall(int operation, InstallCallbackInterface callback) {
                                super.onPackageInstall(operation, callback);
                            }
                        }),
                is(true));

        countDownLatch.await(10, SECONDS);

        assertThat(status.get(), is(SUCCESS));*/
    }
}
