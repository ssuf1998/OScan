package indi.ssuf1998.oscan.core;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class Utils {

    public static Mat bmp2Mat(Bitmap bmp) {
        final Mat ret = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8UC3);
        org.opencv.android.Utils.bitmapToMat(bmp, ret);
        return ret;
    }

    public static Mat drawable2Mat(Drawable drawable) {
        final Bitmap bmp = ((BitmapDrawable) drawable).getBitmap();
        return bmp2Mat(bmp);
    }

    public static Bitmap mat2Bmp(Mat mat) {
        final Bitmap bmp = Bitmap.createBitmap(
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

    public static MappedByteBuffer getModelBuffer(Context context, String modelFile) throws IOException {
        AssetFileDescriptor descriptor = context.getAssets().openFd(modelFile);
        FileInputStream is = new FileInputStream(descriptor.getFileDescriptor());
        FileChannel fileChannel = is.getChannel();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY,
                descriptor.getStartOffset(),
                descriptor.getDeclaredLength());
    }

    @NonNull
    public static ByteBuffer bmp2ByteBuffer(@NonNull Bitmap bitmap) {
        final int w = bitmap.getWidth();
        final int h = bitmap.getHeight();

        final ByteBuffer buffer = ByteBuffer.allocateDirect(
                w * h * 3 * Float.SIZE / Byte.SIZE);
        buffer.order(ByteOrder.nativeOrder());
        buffer.rewind();

        final int[] ints = new int[w * h];

        bitmap.getPixels(ints, 0, w, 0, 0, w, h);

        int pixelIdx = 0;
        for (int i = 0; i < w; ++i) {
            for (int j = 0; j < h; ++j) {
                final int pixel = ints[pixelIdx++];
                buffer.putFloat(((pixel >> 16) & 0xFF));
                buffer.putFloat(((pixel >> 8) & 0xFF));
                buffer.putFloat((pixel & 0xFF));
            }
        }
        return buffer;
    }

    @NonNull
    public static Bitmap byteBuffer2Bmp(@NonNull ByteBuffer buffer, int width, int height) {
        buffer.rewind();

        final Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final int[] pixels = new int[width * height];
        for (int i = 0; i < width * height; i++) {
            float val = buffer.getFloat();
            if (val > 0.2) {
                pixels[i] = 0xFFFFFFFF;
            } else {
                pixels[i] = 0xFF000000;
            }
        }
        bmp.setPixels(pixels, 0, width, 0, 0, width, height);

        return bmp;
    }

}
