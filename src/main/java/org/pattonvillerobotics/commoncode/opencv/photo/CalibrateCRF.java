
//
// This file is auto-generated. Please don't modify it!
//
package org.pattonvillerobotics.commoncode.opencv.photo;

import org.pattonvillerobotics.commoncode.opencv.core.Algorithm;
import org.pattonvillerobotics.commoncode.opencv.core.Mat;
import org.pattonvillerobotics.commoncode.opencv.utils.Converters;

import java.util.List;

// C++: class CalibrateCRF
//javadoc: CalibrateCRF
public class CalibrateCRF extends Algorithm {

    protected CalibrateCRF(long addr) {
        super(addr);
    }


    //
    // C++:  void process(vector_Mat src, Mat& dst, Mat times)
    //

    // C++:  void process(vector_Mat src, Mat& dst, Mat times)
    private static native void process_0(long nativeObj, long src_mat_nativeObj, long dst_nativeObj, long times_nativeObj);

    // native support for java finalize()
    private static native void delete(long nativeObj);

    //javadoc: CalibrateCRF::process(src, dst, times)
    public void process(List<Mat> src, Mat dst, Mat times) {
        Mat src_mat = Converters.vector_Mat_to_Mat(src);
        process_0(nativeObj, src_mat.nativeObj, dst.nativeObj, times.nativeObj);

        return;
    }

    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }

}
