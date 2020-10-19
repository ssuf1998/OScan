package indi.ssuf1998.oscan;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import indi.ssuf1998.osactionsheet.OSMASItem;
import indi.ssuf1998.osactionsheet.OSMenuActionSheet;
import indi.ssuf1998.oscan.adapter.MainViewPager2Adapter;
import indi.ssuf1998.oscan.databinding.MainActivityLayoutBinding;

public class MainActivity extends AppCompatActivity {
    private MainActivityLayoutBinding binding;
    private OSMenuActionSheet scanMenuAS;
    private ArrayList<Fragment> fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = MainActivityLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.getRoot().post(() -> {
            initUI();
            bindListeners();
        });
    }

    private void initUI() {
        fragments = new ArrayList<>();
        fragments.add(new HomeFragment());
        fragments.add(new HomeFragment());

        final MainViewPager2Adapter MVP2Adapter = new MainViewPager2Adapter(this, fragments);
        binding.mViewPager2.setAdapter(MVP2Adapter);
        binding.mViewPager2.setUserInputEnabled(false);

        final ArrayList<OSMASItem> items = new ArrayList<>();

        items.add(new OSMASItem().setItemText(getString(R.string.scan_action_sheet_doc))
                .setItemIcon(ContextCompat.getDrawable(
                        MainActivity.this,
                        R.drawable.ic_fluent_document_24_filled))
        );
        items.add(new OSMASItem().setItemText(getString(R.string.scan_action_sheet_card))
                .setItemIcon(ContextCompat.getDrawable(
                        MainActivity.this,
                        R.drawable.ic_fluent_contact_card_24_filled))
        );
        items.add(new OSMASItem().setItemText(getString(R.string.scan_action_sheet_code))
                .setItemIcon(ContextCompat.getDrawable(
                        MainActivity.this,
                        R.drawable.ic_fluent_qr_code_24_filled))
        );

        scanMenuAS = new OSMenuActionSheet(getString(R.string.scan_action_sheet_title), items);
        scanMenuAS.setDecoration(new RecycleViewDivider(this));

        scanMenuAS.setOnItemClickListener(idx -> {
            if (idx == 0) {
                final Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                MainActivity.this.startActivity(intent);
            }
            scanMenuAS.dismiss();
        });
    }

    private void bindListeners() {
        binding.bottomNav.setOnNavigationItemSelectedListener(item -> {
            final int itemId = item.getItemId();
            if (itemId == R.id.bottomNavScan) {
                scanMenuAS.show(getSupportFragmentManager(), "scanMenuAS");
                return false;
            } else if (itemId == R.id.bottomNavHome) {
                binding.mViewPager2.setCurrentItem(0, false);
            } else if (itemId == R.id.bottomNavMore) {
                binding.mViewPager2.setCurrentItem(1, false);
            }

            binding.mAppBarLayout.setExpanded(true);
            binding.mToolBarTitle.setText(item.getTitle());
            return true;
        });

        binding.bottomNav.setOnNavigationItemReselectedListener(item -> {
            binding.mAppBarLayout.setExpanded(true);
            final int itemId = item.getItemId();

            if (itemId == R.id.bottomNavHome) {
                ((HomeFragment) fragments.get(0)).scrollToTop();
            } else if (itemId == R.id.bottomNavMore) {
                ((HomeFragment) fragments.get(1)).scrollToTop();
            }
        });
    }


}