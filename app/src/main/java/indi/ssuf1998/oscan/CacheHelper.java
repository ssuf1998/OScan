package indi.ssuf1998.oscan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

public class CacheHelper {
    private static CacheHelper instance;

    private final HashMap<String, Object> dataMap;

    private CacheHelper() {
//        final int maxMemory = (int) (Runtime.getRuntime().totalMemory() / 1024);
//        final int cacheSize = maxMemory / 4;
        dataMap = new HashMap<>();
    }

    public static CacheHelper getInstance() {
        if (instance == null) {
            synchronized (CacheHelper.class) {
                if (instance == null) {
                    instance = new CacheHelper();
                }
            }
        }
        return instance;
    }

    public Object getDataThenSweep(String key, Object defaultValue) {
        final Object v = getData(key, defaultValue);
        removeData(key);
        return v;
    }

    public Object getDataThenSweep(String key) {
        return getDataThenSweep(key, null);
    }

    public Object getData(String key, Object defaultValue) {
        return Optional
                .ofNullable(dataMap.get(key))
                .orElse(defaultValue);
    }

    public Object getData(String key) {
        return getData(key, null);
    }

    public void putData(String key, Object value) {
        dataMap.put(key, value);
    }

    public void removeData(String key) {
        dataMap.remove(key);
    }

    public Bitmap getBmpFromCache(String key, Context context) {
        if (dataMap.get(key) == null) {
            return null;
        }

        final String fn = (String) dataMap.get(key);
        final File bmpFile = new File(context.getCacheDir().getPath(), fn);
        try {
            final FileInputStream is = new FileInputStream(bmpFile);
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;

            final Bitmap bmp = BitmapFactory.decodeStream(is, null, options);
            is.close();
            return bmp;
        } catch (IOException e) {
            return null;
        }
    }

    public void putBmpIntoCache(String key, Bitmap bmp, Context context) {
        final String fn = String.format("%s", key);
        final File bmpFile = new File(context.getCacheDir().getPath(), fn);

        if (bmpFile.exists()) {
            bmpFile.delete();
        }

        try {
            final FileOutputStream os = new FileOutputStream(bmpFile);
            final Bitmap bmpNoA = bmp.copy(Bitmap.Config.RGB_565, false);
            bmpNoA.compress(Bitmap.CompressFormat.JPEG, 80, os);
            os.flush();

            os.close();
        } catch (IOException ignore) {
        }

        dataMap.put(key, fn);
    }

    public void removeBmpFromCache(String key, Context context) {
        if (dataMap.get(key) != null) {
            final String fn = (String) dataMap.get(key);
            final File bmpFile = new File(context.getCacheDir().getPath(), fn);

            if (bmpFile.exists()) {
                bmpFile.delete();
                dataMap.remove(key);
            }
        }
    }

    public void cleanBmpCache(Context context) {
        final Set<String> keys = dataMap.keySet();
        if (keys != null) {
            for (String key : keys) {
                Object value = dataMap.get(key);
                if (value instanceof String) {
                    removeBmpFromCache(key, context);
                }
            }
        }
    }

}
