package indi.ssuf1998.oscan;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageProxy;

import java.nio.ByteBuffer;

public class Utils {
    public static int getStatusBarHeight(@NonNull Activity activity) {
        int result = 0;
        //获取状态栏高度的资源id
        int resourceId = activity.getResources().getIdentifier(
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
        ActionBar actionBar = activity.getSupportActionBar();
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
        ByteBuffer buffer = img.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
    }

}
