package indi.ssuf1998.oscan.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;
import org.tensorflow.lite.Interpreter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OSCoreHED {
    private Mat resMat;
    private Mat procMat;

    private MatOfPoint biggestRectPts;
    private final Point[] cornerPts = new Point[4];
    private final Point centerPts = new Point(0, 0);

    // 感谢提供学习代码以及模型
    // 没有时间去找数据，写模型和训练模型了，也没有哪个本事哈哈哈
    // 本应该用vgg16风格来实现HED的，这里的模型用的是mobilenet v2跑的
    // http://fengjian0106.github.io/2017/05/08/Document-Scanning-With-TensorFlow-And-OpenCV/
    // https://github.com/fengjian0106/hed-tutorial-for-document-scanning
    // https://pqpo.me/2019/08/02/machine-learning-hed-smartcropper/
    // https://github.com/pqpo/SmartCropper
    private final static String HED_MODEL_FILE_NAME = "hed_lite_model_quantize.tflite";
    private final static int MODEL_PROC_IMG_A = 256;
    private Interpreter tfInterpreter;

    public OSCoreHED(Context context) throws IOException {
        initTF(context);
    }

    public OSCoreHED(@NonNull Context context,
                     @NonNull Mat resMat) throws IOException {
        this(context);
        setRes(resMat);
    }

    public OSCoreHED setRes(@NonNull Mat resMat) {
        this.resMat = resMat;
        this.procMat = resMat.clone();
        Imgproc.cvtColor(resMat, resMat, Imgproc.COLOR_BGR2RGB);
        return this;
    }

    public Mat getResMat() {
        return resMat;
    }

    public OSCoreHED setRes(Bitmap bmp) {
        return setRes(Utils.bmp2Mat(bmp));
    }

    public OSCoreHED setRes(Drawable drawable) {
        return setRes(Utils.drawable2Mat(drawable));
    }

    public Mat getProcMat() {
        return procMat;
    }

    public Bitmap getProcBmp() {
        return Utils.mat2Bmp(getProcMat());
    }

    public OSCoreHED grey() {
        Imgproc.cvtColor(procMat, procMat, Imgproc.COLOR_BGR2GRAY);
        return this;
    }

    public synchronized OSCoreHED runHED() {

        final Bitmap resBmp = Bitmap.createScaledBitmap(
                Utils.mat2Bmp(procMat), MODEL_PROC_IMG_A, MODEL_PROC_IMG_A, false);

        final ByteBuffer inBuffer = Utils.bmp2ByteBuffer(resBmp);
        final ByteBuffer outBuffer = ByteBuffer.allocateDirect(
                MODEL_PROC_IMG_A * MODEL_PROC_IMG_A * Float.SIZE / Byte.SIZE);
        outBuffer.order(ByteOrder.nativeOrder());
        outBuffer.rewind();

        tfInterpreter.run(inBuffer, outBuffer);

        final double[] aspectSize = Utils.getAspectSize(resMat.size(), MODEL_PROC_IMG_A);
        final Bitmap aspectBmp = Bitmap.createScaledBitmap(
                Utils.byteBuffer2Bmp(outBuffer, MODEL_PROC_IMG_A, MODEL_PROC_IMG_A),
                (int) aspectSize[0], (int) aspectSize[1], false);

        procMat = Utils.bmp2Mat(aspectBmp);
        Imgproc.cvtColor(procMat, procMat, Imgproc.COLOR_BGR2GRAY);

        inBuffer.clear();
        outBuffer.clear();

        return this;
    }

    private void initTF(Context context) throws IOException {
        MappedByteBuffer buffer = Utils.getModelBuffer(context, HED_MODEL_FILE_NAME);
        Interpreter.Options options = new Interpreter.Options();
        tfInterpreter = new Interpreter(buffer, options);
    }

    @Deprecated
    public OSCoreHED thinning() {
        if (procMat.channels() != 1) {
            Imgproc.cvtColor(procMat, procMat, Imgproc.COLOR_BGR2GRAY);
        }

        Core.divide(procMat, new Scalar(255), procMat);
        procMat = thinningCore(procMat);
        Core.multiply(procMat, new Scalar(255), procMat);

        return this;
    }

    private Mat thinningCore(Mat input) {
        final Mat mat = input.clone();
        int times = 0;

        for (int r = 1; r < mat.rows() - 1; r++) {
            for (int c = 1; c < mat.cols() - 1; c++) {
                final int p1 = (int) mat.get(r, c)[0];

                if (p1 == 0)
                    continue;

                final int[] neighbors = new int[]{
                        (int) mat.get(r - 1, c)[0],
                        (int) mat.get(r - 1, c + 1)[0],
                        (int) mat.get(r, c + 1)[0],
                        (int) mat.get(r + 1, c + 1)[0],
                        (int) mat.get(r + 1, c)[0],
                        (int) mat.get(r + 1, c - 1)[0],
                        (int) mat.get(r, c - 1)[0],
                        (int) mat.get(r - 1, c - 1)[0],
                };

                final int N = neighborOnes(neighbors);

                if (!(N >= 2 && N <= 6))
                    continue;

                final int S = zero2OneTimes(neighbors);

                if (S != 1)
                    continue;

                if ((neighbors[0] & neighbors[2] & neighbors[4]) == 0 &&
                        (neighbors[2] & neighbors[4] & neighbors[6]) == 0
                ) {
                    mat.put(r, c, 0);
                    times++;
                } else {
                    if ((neighbors[0] & neighbors[2] & neighbors[6]) == 0 &&
                            (neighbors[0] & neighbors[4] & neighbors[6]) == 0
                    ) {
                        mat.put(r, c, 0);
                        times++;
                    }
                }
            }
        }

        if (times != 0) {
            return thinningCore(mat);
        } else {
            return mat;
        }
    }

    private int neighborOnes(int[] neighbors) {
        int ret = 0;
        for (int n : neighbors) {
            if (n == 1)
                ret++;
        }
        return ret;
    }

    private int zero2OneTimes(int[] neighbors) {
        int ret = 0;
        for (int i = 0; i < neighbors.length; i++) {
            final int cur = neighbors[i];
            final int next = neighbors[i + 1 >= neighbors.length ? 0 : i + 1];

            if (cur == 0 && next == 1)
                ret++;
        }
        return ret;
    }

    public OSCoreHED computeBiggestRectPts() {
        final ArrayList<MatOfPoint> contours = new ArrayList<>();
        final Mat hierarchy = new Mat();

        Imgproc.findContours(procMat, contours, hierarchy,
                Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        final ArrayList<MatOfPoint> hullList = new ArrayList<>();
        for (int i = 0; i < contours.size(); i++) {

            final MatOfInt hullPointsIdx = new MatOfInt();
            Imgproc.convexHull(contours.get(i), hullPointsIdx);

            final Point[] hullPoints = new Point[hullPointsIdx.rows()];
            for (int j = 0; j < hullPointsIdx.col(0).total(); j++) {
                final int idx = (int) hullPointsIdx.get(j, 0)[0];
                hullPoints[j] = new Point(contours.get(i).get(idx, 0));
            }

            hullList.add(new MatOfPoint(hullPoints));
        }

        hullList.sort(Comparator.comparingDouble(Imgproc::contourArea));

        biggestRectPts = hullList.get(hullList.size() - 1);

        if (Imgproc.contourArea(biggestRectPts) <= procMat.total() * 0.33) {
            biggestRectPts = null;
            return this;
        }

        final float scaleRatio = ((float) MODEL_PROC_IMG_A) / Math.max(resMat.cols(), resMat.rows());

        for (int r = 0; r < biggestRectPts.rows(); r++) {
            final double[] p = biggestRectPts.get(r, 0);
            biggestRectPts.put(r, 0, p[0] / scaleRatio, p[1] / scaleRatio);
        }

        return this;
    }

    public OSCoreHED drawHull() {
        procMat = resMat.clone();
        Imgproc.drawContours(procMat,
                new ArrayList<MatOfPoint>() {{
                    add(biggestRectPts);
                }},
                0,
                new Scalar(255, 0, 0),
                3);
        return this;
    }

    public OSCoreHED computeCornerPts() {
        if (biggestRectPts == null)
            return this;

        final List<Point> biggestHullPts = biggestRectPts.toList();
        final int len = biggestHullPts.size();

        for (Point p : biggestHullPts) {
            centerPts.x += p.x;
            centerPts.y += p.y;
        }
        centerPts.x /= len;
        centerPts.y /= len;

        final double[] distances = {0, 0, 0, 0};
        for (Point p : biggestHullPts) {
            final double distance = eulerDistance(p, centerPts);
            final int quadrant = getQuadrant(p, centerPts);

            if (quadrant != -1 && distance > distances[quadrant]) {
                distances[quadrant] = distance;
                cornerPts[quadrant] = p;
            }
        }

        return this;
    }

    private static double eulerDistance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    private static int getQuadrant(Point p, Point origin) {
        if (p.x == origin.x || p.y == origin.y) {
            return -1;
        }

        if (p.x < origin.x && p.y < origin.y)
            return 0;
        else if (p.x > origin.x && p.y < origin.y)
            return 1;
        else if (p.x > origin.x && p.y > origin.y)
            return 2;
        else
            return 3;
    }

    // https://zhuanlan.zhihu.com/p/64025334
    // https://stackoverflow.com/questions/38285229/calculating-aspect-ratio-of-perspective-transform-destination-image
    // http://research.microsoft.com/en-us/um/people/zhang/papers/tr03-39.pdf
    public OSCoreHED clipThenTransform(Point[] clipPts) {
        final int u0 = resMat.cols() / 2,
                v0 = resMat.rows() / 2;

        // zigzag order, clipPts is clockwise
        final float m1x = (float) (clipPts[0].x - u0);
        final float m1y = (float) (clipPts[0].y - v0);
        final float m2x = (float) (clipPts[1].x - u0);
        final float m2y = (float) (clipPts[1].y - v0);
        final float m3x = (float) (clipPts[3].x - u0);
        final float m3y = (float) (clipPts[3].y - v0);
        final float m4x = (float) (clipPts[2].x - u0);
        final float m4y = (float) (clipPts[2].y - v0);

        final float k2 = ((m1y - m4y) * m3x - (m1x - m4x) * m3y + m1x * m4y - m1y * m4x) /
                ((m2y - m4y) * m3x - (m2x - m4x) * m3y + m2x * m4y - m2y * m4x);

        final float k3 = ((m1y - m4y) * m2x - (m1x - m4x) * m2y + m1x * m4y - m1y * m4x) /
                ((m3y - m4y) * m2x - (m3x - m4x) * m2y + m3x * m4y - m3y * m4x);

        final float fSquare =
                -((k3 * m3y - m1y) * (k2 * m2y - m1y) + (k3 * m3x - m1x) * (k2 * m2x - m1x)) /
                        ((k3 - 1) * (k2 - 1));

        final float realRatio;

        if (k2 == 1 && k3 == 1) {
            realRatio = (float) Math.sqrt(
                    (Math.pow((k2 - 1), 2) + Math.pow((k2 * m2y - m1y), 2) / fSquare + Math.pow((k2 * m2x - m1x), 2) / fSquare) /
                            (Math.pow((k3 - 1), 2) + Math.pow((k3 * m3y - m1y), 2) / fSquare + Math.pow((k3 * m3x - m1x), 2) / fSquare));
        } else {
            realRatio = (float) Math.sqrt(
                    (Math.pow((m2y - m1y), 2) + Math.pow((m2x - m1x), 2)) /
                            (Math.pow((m3y - m1y), 2) + Math.pow((m3x - m1x), 2)));
        }

        final double w1 = Math.sqrt(Math.pow(clipPts[0].x - clipPts[1].x, 2) + Math.pow(clipPts[0].y - clipPts[1].y, 2));
        final double w2 = Math.sqrt(Math.pow(clipPts[3].x - clipPts[2].x, 2) + Math.pow(clipPts[3].y - clipPts[2].y, 2));
        final double w = Math.max(w1, w2);

        final double h1 = Math.sqrt(Math.pow(clipPts[0].x - clipPts[3].x, 2) + Math.pow(clipPts[0].y - clipPts[3].y, 2));
        final double h2 = Math.sqrt(Math.pow(clipPts[1].x - clipPts[2].x, 2) + Math.pow(clipPts[1].y - clipPts[2].y, 2));
        final double h = Math.max(h1, h2);

        final float visibleRatio = (float) w / (float) h;

        final int realW, realH;
        if (realRatio < visibleRatio) {
            realW = (int) w;
            realH = (int) (w / realRatio);
        } else {
            realW = (int) (h * realRatio);
            realH = (int) h;
        }

        final MatOfPoint2f dst = new MatOfPoint2f(
                new Point(0, 0),
                new Point(realW, 0),
                new Point(realW, realH),
                new Point(0, realH)
        );

        final Mat transMat = Imgproc.getPerspectiveTransform(new MatOfPoint2f(clipPts), dst);

        Imgproc.warpPerspective(resMat, procMat,
                transMat, new Size(realW, realH));

        return this;
    }

    public OSCoreHED drawMarks() {
        procMat = resMat.clone();
        Imgproc.drawMarker(procMat, centerPts, new Scalar(255, 0, 0),
                Imgproc.MARKER_CROSS, 64, 6);

        for (Point p : cornerPts) {
            if (p != null) {
                Imgproc.drawMarker(procMat, p, new Scalar(0, 0, 255),
                        Imgproc.MARKER_CROSS, 64, 6);
            }
        }

        return this;
    }

    public OSCoreHED removeBg(int intensity) {
        // 1024的用25就差不多
        // 最好用一个1024的做预览图
        // 最后一步保存中用25乘上一个放大系数
        // 这个放大系数确实不好确定，暂时就凭感觉吧，
        // 直接乘上一个缩放比例也不合适，高斯滤镜应该不是线性的
        final int validIntensity = intensity % 2 == 0 ? intensity + 1 : intensity;
        final Mat tmp = procMat.clone();
        Imgproc.GaussianBlur(tmp, tmp, new Size(validIntensity, validIntensity), 0);
        Core.divide(procMat, tmp, procMat, 255);

        return this;
    }

    public OSCoreHED binaryThruHist(float hold) {
        // hold尽量小于等于0.1，保留太多黑色像素会导致整个图像显得很脏
        // 为啥hold需要这么小？在一张文档图像中，黑色像素的占比往往很小
        if (procMat.channels() != 1)
            Imgproc.cvtColor(procMat, procMat, Imgproc.COLOR_BGR2GRAY);

        final Mat hist = new Mat();
        Imgproc.calcHist(Collections.singletonList(procMat),
                new MatOfInt(0), new Mat(),
                hist, new MatOfInt(256), new MatOfFloat(0, 256));
        Core.divide(hist, new Scalar(procMat.total()), hist);

        float accumulate = 0;
        int threshold = 255;
        for (int i = 0; i < hist.rows(); i++) {
            accumulate += hist.get(i, 0)[0];
            if (accumulate > hold) {
                threshold = i;
                break;
            }
        }

        Imgproc.threshold(procMat, procMat, threshold, 255, Imgproc.THRESH_BINARY);

        return this;
    }

    @Deprecated
    public OSCoreHED greyWorld() {
        final List<Mat> bgrMat = new ArrayList<>();

        Core.split(procMat, bgrMat);

        final double b, g, r;
        b = Core.mean(bgrMat.get(0)).val[0];
        g = Core.mean(bgrMat.get(1)).val[0];
        r = Core.mean(bgrMat.get(2)).val[0];

        final double kB, kG, kR;
        kB = (b + g + r) / (3 * b);
        kG = (b + g + r) / (3 * g);
        kR = (b + g + r) / (3 * r);

        Core.multiply(bgrMat.get(0), new Scalar(kB), bgrMat.get(0));
        Core.multiply(bgrMat.get(1), new Scalar(kG), bgrMat.get(1));
        Core.multiply(bgrMat.get(2), new Scalar(kR), bgrMat.get(2));

        Core.merge(bgrMat, procMat);

        return this;
    }

    public OSCoreHED adjustContrastNBright(float c, float beta) {
        // c is in [-1,1] helping calculate the contrast factor
        // beta is in [-1,1] standing for brightness
        final float k = (float) Math.tan((45 + 44 * c) / 180 * Math.PI);
        final Mat lookUpTable = new Mat(1, 256, CvType.CV_8U);
        final byte[] lookUpTableData = new byte[(int) (lookUpTable.total() * lookUpTable.channels())];
        for (int i = 0; i < lookUpTable.cols(); i++) {
            lookUpTableData[i] = saturate(((float) i - 127.5 * (1 - beta)) * k + 127.5 * (1 + beta));
        }
        lookUpTable.put(0, 0, lookUpTableData);
        Core.LUT(procMat, lookUpTable, procMat);

        return this;
    }

    public OSCoreHED adjustSaturate(float mul) {
        // 0.5-1.5

        final List<Mat> hsvMat = new ArrayList<>();
        final Mat hsvImg = new Mat();
        Imgproc.cvtColor(procMat, hsvImg, Imgproc.COLOR_BGR2HSV);

        Core.split(hsvImg, hsvMat);
        Core.multiply(hsvMat.get(1), new Scalar(mul), hsvMat.get(1));
        Core.merge(hsvMat, hsvImg);

        Imgproc.cvtColor(hsvImg, procMat, Imgproc.COLOR_HSV2BGR);

        return this;
    }

    private byte saturate(double val) {
        int iVal = (int) Math.round(val);
        iVal = Math.min(Math.max(iVal, 0), 255);
        return (byte) iVal;
    }

    public OSCoreHED colorCorrect() {
        Imgproc.cvtColor(procMat, procMat, Imgproc.COLOR_RGB2BGR);
        return this;
    }

    public Point[] getCornerPts() {
        return cornerPts;
    }

    public void sweep() {
        resMat = null;
        procMat = null;
        biggestRectPts = null;

        for (int i = 0; i < cornerPts.length; i++) {
            cornerPts[i] = new Point();
        }

        centerPts.x = 0;
        centerPts.y = 0;
    }

    public OSCoreHED detect() {
        if (procMat == null)
            return null;

        return runHED()
                .computeBiggestRectPts()
                .computeCornerPts();
    }
}
