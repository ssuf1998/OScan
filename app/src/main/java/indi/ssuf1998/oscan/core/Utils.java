package indi.ssuf1998.oscan.core;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class Utils {

    public static Mat bmp2Mat(Bitmap bmp) {
        Mat ret = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8UC3);
        org.opencv.android.Utils.bitmapToMat(bmp, ret);
        Imgproc.cvtColor(ret, ret, Imgproc.COLOR_RGB2BGR);
        return ret;
    }

    public static Mat drawable2Mat(Drawable drawable) {
        Bitmap bmp = ((BitmapDrawable) drawable).getBitmap();
        return bmp2Mat(bmp);
    }

    public static Bitmap mat2Bmp(Mat mat) {
        Bitmap bmp = Bitmap.createBitmap(
                mat.cols(),
                mat.rows(),
                Bitmap.Config.ARGB_8888
        );
        org.opencv.android.Utils.matToBitmap(mat, bmp);

        return bmp;
    }

    public static Drawable mat2Drawable(Resources res, Mat mat) {
        return new BitmapDrawable(res, mat2Bmp(mat));
    }


}
