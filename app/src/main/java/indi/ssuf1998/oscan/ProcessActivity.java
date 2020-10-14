package indi.ssuf1998.oscan;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import indi.ssuf1998.oscan.databinding.ProcessActivityLayoutBinding;


public class ProcessActivity extends AppCompatActivity {

    private ProcessActivityLayoutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ProcessActivityLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }


}