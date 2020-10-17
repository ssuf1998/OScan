package indi.ssuf1998.oscan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.FileNotFoundException;
import java.util.ArrayList;

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

    private final SharedBlock block = SharedBlock.getInstance();
    private Bitmap acquiredBmp;
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
    int flashNowMode = ImageCapture.FLASH_MODE_AUTO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ScanActivityLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        usesPermissions();

        binding.getRoot().post(() -> {
            initUI();
            bindListeners();
        });
    }

    private void initUI() {
        binding.takePicBtn.setTranslationY(-binding.takePicBtn.getHeight() * (1 / 3f));
        binding.pickPicBtn.setTranslationY(binding.takePicBtn.getHeight() / 2f +
                binding.takePicBtn.getTranslationY());
        binding.switchFlashBtn.setTranslationY(binding.takePicBtn.getHeight() / 2f +
                binding.takePicBtn.getTranslationY());

        final ArrayList<OSMASItem> items = new ArrayList<>();
        items.add(new OSMASItem()
                .setItemText(getString(R.string.granted_action_sheet_auth))
                .setItemTextColor(ScanActivity.this.getColor(R.color.colorPrimary))
                .setTypefaceStyle(Typeface.BOLD)
        );
        items.add(new OSMASItem(getString(R.string.pick_pic_btn_desc)));
        items.add(new OSMASItem(getString(R.string.action_sheet_cancel)));

        grantedMenuAS = new OSMenuActionSheet(getString(R.string.granted_action_sheet_title), items);
        grantedMenuAS.setCancelable(false);
        grantedMenuAS.setDecoration(new RecycleViewDivider(this));
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

    private void giveBmp2Process(Bitmap bmp) {
        final Intent intent = new Intent(this, ProcessActivity.class);
        block.putData("bmp", bmp);
        this.startActivity(intent);
    }

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
            capture.takePicture(
                    ContextCompat.getMainExecutor(ScanActivity.this),
                    new ImageCapture.OnImageCapturedCallback() {
                        @Override
                        public void onCaptureSuccess(@NonNull ImageProxy image) {
                            giveBmp2Process(Utils.imgProxy2Bitmap(image));
                            image.close();
                        }
                    }
            );
        });
    }

    private void bindToCamera(@NonNull ProcessCameraProvider cameraProvider) {
        preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build();

        capture = new ImageCapture.Builder()
                .build();

        final OrientationEventListener orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                final int rotation;

                if (orientation >= 45 && orientation < 135) {
                    rotation = Surface.ROTATION_270;
                } else if (orientation >= 135 && orientation < 225) {
                    rotation = Surface.ROTATION_180;
                } else if (orientation >= 225 && orientation < 315) {
                    rotation = Surface.ROTATION_90;
                } else {
                    rotation = Surface.ROTATION_0;
                }

                capture.setTargetRotation(rotation);
            }
        };

        orientationEventListener.enable();

        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());
        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, capture);
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
                            this.getString(R.string.granted_fail_toast),
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
                    final Bitmap bmp = BitmapFactory.decodeStream(
                            this.getContentResolver().openInputStream(uri));
                    giveBmp2Process(bmp);
                } catch (FileNotFoundException e) {
                    Toasty.error(ScanActivity.this,
                            getString(R.string.err_file_not_find),
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}