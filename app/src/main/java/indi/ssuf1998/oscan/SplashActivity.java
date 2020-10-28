package indi.ssuf1998.oscan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.opencv.osgi.OpenCVNativeLoader;

import java.io.IOException;

import indi.ssuf1998.oscan.core.OSCoreHED;
import indi.ssuf1998.oscan.databinding.SplashActivityLayoutBinding;

public class SplashActivity extends AppCompatActivity {

    private SplashActivityLayoutBinding binding;
    private final CacheHelper cache = CacheHelper.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final boolean isLoaded = (Boolean) cache.getData(Const.APP_LOAD_FINISHED, false);

        if (!isLoaded) {
            binding = SplashActivityLayoutBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            hideSystemUI();

            new InitProcess().execute(SplashActivity.this, cache);
        } else {
            final Intent intent = new Intent(this,
                    MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }


    private static class InitProcess extends AsyncTask<Object, Void, Object[]> {

        @Override
        protected Object[] doInBackground(Object... objects) {
            final long start = System.currentTimeMillis();
            final Context c = (Context) objects[0];
            final CacheHelper mBlock = (CacheHelper) objects[1];

            try {
                mBlock.putData("hed", new OSCoreHED(c));

                new OpenCVNativeLoader().init();

                final long during = System.currentTimeMillis() - start;
                final long waitTime = 1000 - during;

                if (waitTime > 0) {
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ignore) {
                    }
                }

            } catch (IOException e) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(c);
                builder.setTitle(c.getString(R.string.err_dialog_title));
                builder.setMessage(String.format("%s\n%s", c.getString(R.string.err_dialog_msg_pre),
                        e.toString()));
                builder.setCancelable(false);
                builder.setPositiveButton(c.getString(R.string.dialog_confirm),
                        (dialogInterface, i) -> Runtime.getRuntime().exit(1)
                );

                Looper.prepare();
                builder.show();
                Looper.loop();
            }

            return objects;
        }

        @Override
        protected void onPostExecute(Object[] objects) {
            final Activity a = (Activity) objects[0];
            final CacheHelper mBlock = (CacheHelper) objects[1];

            final Intent intent = new Intent(a,
                    MainActivity.class);
            a.startActivity(intent);

            mBlock.putData(Const.APP_LOAD_FINISHED, true);
            a.finish();
        }
    }
}