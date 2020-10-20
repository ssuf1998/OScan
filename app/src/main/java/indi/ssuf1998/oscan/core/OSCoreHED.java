package indi.ssuf1998.oscan.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.Interpreter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
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
        final Mat tmp = new Mat();
        Imgproc.cvtColor(procMat, tmp, Imgproc.COLOR_BGR2RGB);

        return tmp;
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

        final double[] aspectSize = getAspectSize(resMat.size(), MODEL_PROC_IMG_A);
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

    private double[] getAspectSize(Size size, int longest) {
        double w = size.width;
        double h = size.height;

        final double ratio = w / h;

        if (w >= h) {
            w = longest;
            h = w / ratio;
        } else {
            h = longest;
            w = h * ratio;
        }

        return new double[]{w, h};
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

        final double scaleRatio = ((double) MODEL_PROC_IMG_A) / Math.max(resMat.cols(), resMat.rows());

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
    public OSCoreHED clipThenTransform(Point[] clipPts) {
        final Point tl = clipPts[0];
        final Point tr = clipPts[1];
        final Point br = clipPts[2];
        final Point bl = clipPts[3];

        final double widthA = Math.sqrt(Math.pow(br.x - bl.x, 2) + Math.pow(br.y - bl.y, 2));
        final double widthB = Math.sqrt(Math.pow(tr.x - tl.x, 2) + Math.pow(tr.y - tl.y, 2));
        final double maxWidth = Math.max((int) widthA, (int) widthB);

        final double heightA = Math.sqrt(Math.pow(tr.x - br.x, 2) + Math.pow(tr.y - br.y, 2));
        final double heightB = Math.sqrt(Math.pow(tl.x - bl.x, 2) + Math.pow(tl.y - bl.y, 2));
        final double maxHeight = Math.max((int) heightA, (int) heightB);

        final MatOfPoint2f dst = new MatOfPoint2f(
                new Point(0, 0),
                new Point(maxWidth - 1, 0),
                new Point(maxWidth - 1, maxHeight - 1),
                new Point(0, maxHeight - 1)
        );


        MatOfPoint2f clipPtsMat = new MatOfPoint2f(clipPts);
        final Mat getMat = Imgproc.getPerspectiveTransform(clipPtsMat, dst);

        Imgproc.warpPerspective(resMat, procMat, getMat, new Size(maxWidth, maxHeight));

        return this;
    }

    public OSCoreHED clipThenTransform() {
        return clipThenTransform(cornerPts);
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
