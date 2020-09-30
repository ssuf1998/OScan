package indi.ssuf1998.oscan;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import org.opencv.osgi.OpenCVNativeLoader;

import indi.ssuf1998.oscan.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new OpenCVNativeLoader().init();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bindListeners();

    }

    private void bindListeners() {
        binding.doScanBtn.setOnClickListener(view -> {
            Intent scanIntent = new Intent(MainActivity.this, ScanActivity.class);
            startActivity(scanIntent);
        });
    }
}