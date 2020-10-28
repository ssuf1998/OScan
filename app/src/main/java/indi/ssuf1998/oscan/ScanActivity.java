package indi.ssuf1998.oscan;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceOrientedMeteringPointFactory;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;
import indi.ssuf1998.osactionsheet.OSMASItem;
import indi.ssuf1998.osactionsheet.OSMenuActionSheet;
import indi.ssuf1998.oscan.databinding.ScanActivityLayoutBinding;

public class ScanActivity extends AppCompatActivity {

    private ScanActivityLayoutBinding binding;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private CameraSelector cameraSelector;
    private ImageCapture capture;
    private Preview preview;
    private Camera camera;
    private Size captureMaxSize;

    private final CacheHelper cache = CacheHelper.getInstance();
    private OSMenuActionSheet grantedMenuAS;
    private int tryTimes = 0;

    private final SparseIntArray flashModes = new SparseIntArray() {{
        append(ImageCapture.FLASH_MODE_AUTO,
                R.drawable.ic_fluent_flash_auto_24_selector);
        append(ImageCapture.FLASH_MODE_ON,
                R.drawable.ic_fluent_flash_on_24_selector);
        append(ImageCapture.FLASH_MODE_OFF,
                R.drawable.ic_fluent_flash_off_24_selector);
    }};
    private int flashNowMode = ImageCapture.FLASH_MODE_AUTO;

    private final ValueAnimator focusAnim = ValueAnimator.ofFloat(0, 1);

    private final Canvas pvaCanvas = new Canvas();
    private final Paint pvaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap pvaBmp;
    private int touchX;
    private int touchY;

//    float[] accValues; // 1*3
//    float[] magValues; // 1*3


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ScanActivityLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        usesPermissions();

        requireCaptureMaxSize();

        binding.getRoot().post(() -> {
            initUI();
            initAnim();
            bindListeners();
        });
    }

    private void initUI() {
        binding.takePicBtn.setTranslationY(-binding.takePicBtn.getHeight() * (1 / 3f));
        binding.pickPicBtn.setTranslationY(binding.takePicBtn.getHeight() / 1.75f +
                binding.takePicBtn.getTranslationY());
        binding.switchFlashBtn.setTranslationY(binding.takePicBtn.getHeight() / 1.75f +
                binding.takePicBtn.getTranslationY());

        final ArrayList<OSMASItem> items = new ArrayList<>();
        items.add(new OSMASItem()
                .setItemText(getString(R.string.granted_action_sheet_auth))
                .setItemTextColor(ScanActivity.this.getColor(R.color.colorPrimary))
                .setTypefaceStyle(Typeface.BOLD)
        );
        items.add(new OSMASItem(getString(R.string.pick_pic_btn_desc)));
        items.add(new OSMASItem(getString(R.string.dialog_cancel)));

        grantedMenuAS = new OSMenuActionSheet(getString(R.string.granted_action_sheet_title), items);
        grantedMenuAS.setCancelable(false);
        grantedMenuAS.setDecoration(new RecycleViewDivider(this));

        final Toast planeToast = Toasty.info(this, getString(R.string.plane_tip), Toast.LENGTH_SHORT);
        planeToast.setGravity(Gravity.CENTER, 0, 0);
        planeToast.show();
    }

    private void initAnim() {
        pvaBmp = Bitmap.createBitmap(
                binding.previewView.getWidth(),
                binding.previewView.getHeight(),
                Bitmap.Config.ARGB_8888);
        pvaCanvas.setBitmap(pvaBmp);
        pvaPaint.setColor(Color.WHITE);

        focusAnim.setDuration(500);
        focusAnim.addUpdateListener(valueAnimator -> {

            final float scale = 1.2f - (float) valueAnimator.getAnimatedValue() * 0.2f;
            final int alpha = (int) (192 * (float) valueAnimator.getAnimatedValue());

            pvaCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            pvaCanvas.drawCircle(touchX, touchY,
                    128 * scale, pvaPaint);
            pvaPaint.setAlpha(alpha);

            binding.pvAnimView.setImageBitmap(pvaBmp);
        });

        focusAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                new Handler().postDelayed(() -> binding.pvAnimView.setImageBitmap(null),
                        1000);
            }
        });

    }

    private void bindListeners() {
        grantedMenuAS.setOnItemClickListener(idx -> {
            if (idx == 0) {
                usesPermissions();
            } else if (idx == 1) {
                pickPic();
            } else if (idx == 2) {
                finish();
            }
        });
    }

//    突然发现实现这个不能说是强制要求用户水平放置手机，需要的是手机和文档平行，这是无法做到的……
//    private void bindSensor() {
//        SensorManager sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
//        final Sensor magSensor = sensorMgr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
//        final Sensor accSensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//
//        final SensorEventCallback callback = new SensorEventCallback() {
//            @Override
//            public void onSensorChanged(SensorEvent event) {
//                if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
//                    magValues = event.values;
//                else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
//                    accValues = event.values;
//
//                final float[] values = new float[3];
//                final float[] R = new float[9];
//                SensorManager.getRotationMatrix(R, null, accValues, magValues);
//                SensorManager.getOrientation(R, values);
//
//                final float pitch = (float) Math.toDegrees(values[1]); // 俯仰角
//                final float roll = (float) Math.toDegrees(values[2]); // 翻滚角
//
//            }
//        };
//
//        sensorMgr.registerListener(callback, magSensor, SensorManager.SENSOR_DELAY_NORMAL);
//        sensorMgr.registerListener(callback, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
//    }

    private void usesPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                Const.CAMERA_PERMISSION_REQUEST);
        if (tryTimes <= 3) {
            tryTimes++;
        }
    }

    private void pickPic() {
        final Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.pick_pic_intent_title)),
                Const.PICK_IMG_REQUEST);
    }

    private void giveBmp2Crop(Bitmap bmp) {
        cache.putData("scan_bmp", bmp);
        this.startActivity(new Intent(this, CropActivity.class));
        binding.takePicBtn.setClickable(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void bindCameraListeners() {
        // 摄像头绑定监听
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                bindToCamera(cameraProviderFuture.get());
            } catch (Exception e) {
                Toasty.error(ScanActivity.this,
                        e.toString(),
                        Toast.LENGTH_LONG).show();
            }
        }, ContextCompat.getMainExecutor(this));

        binding.switchFlashBtn.setOnClickListener(view -> {
            try {
                flashNowMode = flashNowMode + 1 >= flashModes.size() ? 0 : flashNowMode + 1;
                capture.setFlashMode(flashModes.keyAt(flashNowMode));
                binding.switchFlashBtn.setImageDrawable(ContextCompat.getDrawable(
                        ScanActivity.this,
                        flashModes.get(flashNowMode)));
            } catch (Exception e) {
                Toasty.error(ScanActivity.this,
                        e.toString(),
                        Toast.LENGTH_LONG).show();
            }
        });

        binding.pickPicBtn.setOnClickListener(view -> pickPic());

        binding.takePicBtn.setOnClickListener(view -> {
            binding.takePicBtn.setClickable(false);
            capture.takePicture(
                    ContextCompat.getMainExecutor(ScanActivity.this),
                    new ImageCapture.OnImageCapturedCallback() {
                        @Override
                        public void onCaptureSuccess(@NonNull ImageProxy image) {
                            giveBmp2Crop(Utils.imgProxy2Bitmap(image));
                            image.close();
                        }
                    }
            );
        });

        // 安卓的设计者真是有病
        binding.previewView.setOnTouchListener((view, motionEvent) -> {

            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                setFocus(motionEvent.getX(), motionEvent.getY());
                touchX = (int) motionEvent.getX();
                touchY = (int) motionEvent.getY();

                focusAnim.start();
            }

            // 分发事件
            return true;
        });
    }

    private void bindToCamera(@NonNull ProcessCameraProvider cameraProvider) {
        preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build();

        capture = new ImageCapture.Builder()
                .setTargetResolution(new Size(captureMaxSize.getHeight(), captureMaxSize.getWidth()))
                .build();

        final OrientationEventListener orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                final int cameraRotation;
                if (orientation >= 45 && orientation < 135) {
                    cameraRotation = Surface.ROTATION_270;
                } else if (orientation >= 135 && orientation < 225) {
                    cameraRotation = Surface.ROTATION_180;
                } else if (orientation >= 225 && orientation < 315) {
                    cameraRotation = Surface.ROTATION_90;
                } else {
                    cameraRotation = Surface.ROTATION_0;
                }
                capture.setTargetRotation(cameraRotation);
            }
        };

        orientationEventListener.enable();

        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, capture);

    }

    private void setFocus(float x, float y) {
        final MeteringPointFactory factory = new SurfaceOrientedMeteringPointFactory(
                binding.previewView.getWidth(), binding.previewView.getHeight());
        final MeteringPoint point = factory.createPoint(x, y);

        camera.getCameraControl().startFocusAndMetering(
                new FocusMeteringAction
                        .Builder(point, FocusMeteringAction.FLAG_AF)
                        .setAutoCancelDuration(3, TimeUnit.SECONDS)
                        .build()
        );
    }

    private void requireCaptureMaxSize() {
        final SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        final String sizeStr = preferences.getString(Const.CAPTURE_MAX_SIZE, null);

        if (sizeStr == null) {
            final SharedPreferences.Editor editor = preferences.edit();
            try {
                final CameraManager mgr = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                final CameraCharacteristics c = mgr.getCameraCharacteristics(String.valueOf(CameraSelector.LENS_FACING_BACK));
                final StreamConfigurationMap map = c.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                final Size[] sizes = map.getOutputSizes(ImageFormat.JPEG);

                Arrays.sort(sizes, (s1, s2) -> s2.getWidth() * s2.getHeight() - s1.getWidth() * s1.getHeight());

                for (Size s : sizes) {
                    if ((double) s.getHeight() / s.getWidth() == 0.75) {
                        captureMaxSize = s;
                        editor.putString(Const.CAPTURE_MAX_SIZE, String.format("%s,%s", s.getWidth(), s.getHeight()));
                        editor.apply();
                        break;
                    }
                }

            } catch (CameraAccessException ignore) {
            }
        } else {
            String[] tmp = sizeStr.split(",");
            captureMaxSize = new Size(Integer.parseInt(tmp[0]),
                    Integer.parseInt(tmp[1]));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Const.CAMERA_PERMISSION_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (grantedMenuAS.isShowing()) {
                    grantedMenuAS.dismiss();
                }
                bindCameraListeners();
            } else {
                if (tryTimes > 1) {
                    final Toast grantToast = Toasty.error(
                            this,
                            getString(R.string.granted_fail_toast),
                            Toast.LENGTH_SHORT
                    );

                    if (tryTimes > 3) {
                        grantToast.setDuration(Toast.LENGTH_LONG);
                        Utils.setText4Toasty(grantToast, this.getString(R.string.granted_multi_fail_toast));
                    }
                    grantToast.setGravity(Gravity.CENTER, 0, 0);
                    grantToast.show();
                }

                if (!grantedMenuAS.isShowing()) {
                    grantedMenuAS.show(getSupportFragmentManager(), "grantedMenuAS");
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == Const.PICK_IMG_REQUEST) {
                final Uri uri = data.getData();
                try {
                    assert uri != null;
                    final BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = false;
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    final Bitmap bmp = BitmapFactory.decodeStream(
                            this.getContentResolver().openInputStream(uri),
                            null,
                            options
                    );
                    giveBmp2Crop(bmp);
                } catch (FileNotFoundException e) {
                    Toasty.error(ScanActivity.this,
                            getString(R.string.err_file_not_find),
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}