package indi.ssuf1998.oscan;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageProxy;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import es.dmoral.toasty.Toasty;

public class Utils {
    public static int getStatusBarHeight(@NonNull Activity activity) {
        int result = 0;
        //获取状态栏高度的资源id
        final int resourceId = activity.getResources().getIdentifier(
                "status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = activity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static void setFullScreen(Window win, boolean full) {
        if (full) {
            win.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

            );
        } else {
            win.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE | View.SYSTEM_UI_LAYOUT_FLAGS);
        }
    }

    public static void setFullScreen(Window win) {
        setFullScreen(win, true);
    }

    public static void setActionBar(AppCompatActivity activity, boolean hide) {
        activity.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        final ActionBar actionBar = activity.getSupportActionBar();
        assert actionBar != null;
        if (hide) {
            actionBar.hide();
        } else {
            actionBar.show();
        }
    }

    public static void setActionBar(AppCompatActivity activity) {
        setActionBar(activity, true);
    }


    public static Bitmap imgProxy2Bitmap(ImageProxy img) {
        final ByteBuffer buffer = img.getPlanes()[0].getBuffer();
        final byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
    }

    public static byte[] imgProxy2Bytes(ImageProxy img) {
        final ByteBuffer buffer = img.getPlanes()[0].getBuffer();
        final byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        return bytes;
    }

    public static byte[] bmp2Bytes(Bitmap bmp) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static void setText4Toasty(Toast toast, String newStr) {
        final TextView toastTextView = (TextView) toast.getView().findViewById(R.id.toast_text);
        assert toastTextView != null;
        toastTextView.setText(newStr);
    }

    public static void setText4Toasty(Toast toast, int resId) {
        final TextView toastTextView = (TextView) toast.getView().findViewById(R.id.toast_text);
        final Context c = toast.getView().getContext();
        assert toastTextView != null;
        toastTextView.setText(c.getText(resId));
    }
}
