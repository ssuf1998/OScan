package indi.ssuf1998.oscan.core;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class OSCore {
    private Mat resMat;
    private Mat procMat;
    private MatOfPoint biggestRectPts;
    private Point[] cornerPts = new Point[4];
    private Point centerPts = new Point(0, 0);

    public OSCore() {
    }

    public OSCore(@NonNull Mat resMat) {
        this.resMat = resMat;
        this.procMat = resMat.clone();
        Imgproc.cvtColor(resMat, resMat, Imgproc.COLOR_BGR2RGB);
    }

    public Mat getResMat() {
        return resMat;
    }

    public OSCore setResMat(@NonNull Mat resMat) {
        this.resMat = resMat;
        this.procMat = resMat.clone();
        Imgproc.cvtColor(resMat, resMat, Imgproc.COLOR_BGR2RGB);
        return this;
    }

    public OSCore setResBmp(Bitmap bmp) {
        return setResMat(Utils.bmp2Mat(bmp));
    }

    public OSCore setResDrawable(Drawable drawable) {
        return setResMat(Utils.drawable2Mat(drawable));
    }

    public Mat getProcMat() {
        return procMat;
    }

    public Bitmap getProcBmp() {
        return Utils.mat2Bmp(procMat);
    }

    public OSCore grey() {
        Imgproc.cvtColor(procMat, procMat, Imgproc.COLOR_BGR2GRAY);
        return this;
    }

    public OSCore reverseColor() {
        Core.bitwise_not(procMat, procMat);
        return this;
    }

    public OSCore adaptiveLightThreshold(int winSize, boolean highScan) {
        int halfWin = winSize / 2;
        int processors = Runtime.getRuntime().availableProcessors();
        int threadCount = (int) (processors * 0.75);

        int r = procMat.rows();
        int c = procMat.cols();

        int smlR;
        int smlC;
        Size smlSize = computeAspectSize(procMat.size(), highScan ? 512 : 256);
        smlR = (int) smlSize.height;
        smlC = (int) smlSize.width;

        Mat smlResMat = procMat.clone();
        Imgproc.resize(smlResMat, smlResMat, new Size(smlC, smlR));
        Imgproc.GaussianBlur(smlResMat, smlResMat,
                new Size(9, 9), 0);

        Mat[] tmpMats = new Mat[threadCount];
        Mat[] tmpMatChunks = new Mat[threadCount];
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            tmpMatChunks[i] = new Mat(smlResMat,
                    new Rect(0, (smlR / threadCount) * i, smlC, smlR / threadCount));
            final int finalI = i;
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    tmpMats[finalI] = ALTThread(tmpMatChunks[finalI], halfWin);
                }
            });
        }

        for (int i = 0; i < threadCount; i++) {
            threads[i].start();
            try {
                threads[i].join();
            } catch (Exception ignored) {

            }
        }

        Mat tmpMat = new Mat(new Size(smlC, smlR), CvType.CV_8UC1);

        Core.vconcat(Arrays.asList(tmpMats), tmpMat);
        Imgproc.resize(tmpMat, tmpMat, new Size(c, r));
//        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9, 9));
//        Imgproc.morphologyEx(tmpMat, tmpMat, Imgproc.MORPH_CLOSE, kernel);
        Imgproc.threshold(tmpMat, procMat, 230, 255,
                Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);

        return this;
    }

    public OSCore adaptiveLightThreshold(int winSize) {
        return adaptiveLightThreshold(winSize, true);
    }

    private Mat ALTThread(Mat matChunk, int halfWin) {
        int r = matChunk.rows();
        int c = matChunk.cols();

        Mat retMatChunk = new Mat(r, c, CvType.CV_8UC1);

        for (int y = 0; y < r; y++) {
            for (int x = 0; x < c; x++) {

                int left = Math.max(x - halfWin, 0);
                int top = Math.max(y - halfWin, 0);

                int right = Math.min(x + halfWin, c) - left;
                int bottom = Math.min(y + halfWin, r) - top;

                Mat subMat = new Mat(matChunk, new Rect(left, top, right, bottom)).clone();
                subMat = subMat.reshape(0, 1);
                Core.sort(subMat, subMat, Core.SORT_DESCENDING);

                int maxValuesSum = 0;
                int bgValue;
                for (int s = 1; s < 6; s++) {
                    maxValuesSum += (int) subMat.get(0, s)[0];
                }
                bgValue = maxValuesSum / 5;

                int resValue = (int) matChunk.get(y, x)[0];
                int smallResValue;
                if (bgValue > resValue) {
                    smallResValue = (int) (255 - kFuc(bgValue) * (bgValue - resValue));
                    if (smallResValue < 230) {
                        smallResValue = 230;
                    }
                } else {
                    smallResValue = 255;
                }
                retMatChunk.put(y, x, smallResValue);
            }
        }
        return retMatChunk;
    }

    public static float kFuc(int b) {
        float B1 = 2.5f;
        float B2 = 1f;

        if (b < 20) {
            return B1;
        } else if (b <= 100) {
            return 1 + (B1 - 1) * ((100 - b) / 80f);
        } else if (b < 200) {
            return 1;
        } else {
            return 1 + B2 * ((b - 220) / 35f);
        }
    }

    private static Size computeAspectSize(Size size, int prefLongest) {
        double w = size.width;
        double h = size.height;

        double ratio = w / h;

        if (w >= h) {
            w = prefLongest;
            h = w / ratio;
        } else {
            h = prefLongest;
            w = h * ratio;
        }

        return new Size(w, h);
    }


    public OSCore computeBiggestRectPts() {
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(procMat, contours, hierarchy,
                Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        ArrayList<MatOfPoint> hullList = new ArrayList<>();
        for (int i = 0; i < contours.size(); i++) {

            MatOfInt hullPointsIdx = new MatOfInt();
            Imgproc.convexHull(contours.get(i), hullPointsIdx);

            Point[] hullPoints = new Point[hullPointsIdx.rows()];
            for (int j = 0; j < hullPointsIdx.col(0).total(); j++) {
                int idx = (int) hullPointsIdx.get(j, 0)[0];
                hullPoints[j] = new Point(contours.get(i).get(idx, 0));
            }

            hullList.add(new MatOfPoint(hullPoints));
        }

        hullList.sort(Comparator.comparingDouble(Imgproc::contourArea));

        biggestRectPts = hullList.get(hullList.size() - 1);

        return this;
    }

    public OSCore computeCornerPts() {
        List<Point> biggestHullPts = biggestRectPts.toList();
        int len = biggestHullPts.size();

        for (Point p : biggestHullPts) {
            centerPts.x += p.x;
            centerPts.y += p.y;
        }
        centerPts.x /= len;
        centerPts.y /= len;

        double[] distances = {0, 0, 0, 0};
        for (Point p : biggestHullPts) {
            double distance = eulerDistance(p, centerPts);
            int quadrant = getQuadrant(p, centerPts);

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
    public OSCore clipThenTransform(Point[] clipPts) {
        Point tl = clipPts[0];
        Point tr = clipPts[1];
        Point br = clipPts[2];
        Point bl = clipPts[3];

        double widthA = Math.sqrt(Math.pow(br.x - bl.x, 2) + Math.pow(br.y - bl.y, 2));
        double widthB = Math.sqrt(Math.pow(tr.x - tl.x, 2) + Math.pow(tr.y - tl.y, 2));
        double maxWidth = Math.max((int) widthA, (int) widthB);

        double heightA = Math.sqrt(Math.pow(tr.x - br.x, 2) + Math.pow(tr.y - br.y, 2));
        double heightB = Math.sqrt(Math.pow(tl.x - bl.x, 2) + Math.pow(tl.y - bl.y, 2));
        double maxHeight = Math.max((int) heightA, (int) heightB);

        MatOfPoint2f dst = new MatOfPoint2f(
                new Point(0, 0),
                new Point(maxWidth - 1, 0),
                new Point(maxWidth - 1, maxHeight - 1),
                new Point(0, maxHeight - 1)
        );


        MatOfPoint2f cornerPtsMat = new MatOfPoint2f(cornerPts);
        Mat getMat = Imgproc.getPerspectiveTransform(cornerPtsMat, dst);

        Imgproc.warpPerspective(resMat, procMat, getMat, new Size(maxWidth, maxHeight));

        return this;
    }

    public OSCore clipThenTransform() {
        return clipThenTransform(cornerPts);
    }


    public OSCore drawHull() {
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

    public OSCore drawMarks() {
        procMat = resMat.clone();
        Imgproc.drawMarker(procMat, centerPts, new Scalar(0, 0, 255),
                Imgproc.MARKER_CROSS, 32, 3);

        for (Point p : cornerPts) {
            if (p != null) {
                Imgproc.drawMarker(procMat, p, new Scalar(255, 0, 0),
                        Imgproc.MARKER_CROSS, 32, 3);
            }
        }

        return this;
    }

    public Point[] getCornerPts() {
        return cornerPts;
    }

}
