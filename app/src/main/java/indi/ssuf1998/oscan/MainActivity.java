package indi.ssuf1998.oscan;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import indi.ssuf1998.osactionsheet.OSASItem;
import indi.ssuf1998.osactionsheet.OSActionSheet;
import indi.ssuf1998.oscan.adapter.MainViewPager2Adapter;
import indi.ssuf1998.oscan.databinding.MainActivityLayoutBinding;

public class MainActivity extends AppCompatActivity {
    private MainActivityLayoutBinding binding;
    private OSActionSheet scanActionSheet;
    private ArrayList<Fragment> fragments;

    private static final SparseIntArray BTM_NAV_ITEM_IDX_MAPPING = new SparseIntArray() {{
        append(R.id.bottomNavHome, 0);
        append(R.id.bottomNavMore, 1);
    }};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        new OpenCVNativeLoader().init();
        binding = MainActivityLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.getRoot().post(() -> {
            initUI();
            bindListeners();
        });

    }

    private void initUI() {
        fragments = new ArrayList<Fragment>() {{
            add(new HomeFragment());
            add(new HomeFragment());
        }};

        MainViewPager2Adapter MVP2Adapter = new MainViewPager2Adapter(this, fragments);
        binding.mViewPager2.setAdapter(MVP2Adapter);
        binding.mViewPager2.setUserInputEnabled(false);

        ArrayList<OSASItem> items = new ArrayList<OSASItem>() {{
            add(new OSASItem(getString(R.string.scan_action_sheet_doc)));
            add(new OSASItem(getString(R.string.scan_action_sheet_card)));
            add(new OSASItem(getString(R.string.scan_action_sheet_code)));
        }};

        scanActionSheet = new OSActionSheet(getString(R.string.scan_action_sheet_title), items);
        scanActionSheet.setOnItemClickListener(idx -> {
            switch (idx) {
                case 0: {
                    Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                    MainActivity.this.startActivity(intent);
                    break;
                }
                default: {

                }
            }

        });

    }

    private void bindListeners() {
        binding.bottomNav.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottomNavScan) {
                scanActionSheet.show(getSupportFragmentManager(), "scanActionSheet");
                return false;
            }

            binding.mViewPager2.setCurrentItem(BTM_NAV_ITEM_IDX_MAPPING.get(item.getItemId()), false);
            binding.mAppBarLayout.setExpanded(true);
            binding.mToolBarTitle.setText(item.getTitle());
            return true;
        });

        binding.bottomNav.setOnNavigationItemReselectedListener(item -> {
            binding.mAppBarLayout.setExpanded(true);
            switch (item.getItemId()) {
                case R.id.bottomNavHome: {
                    ((HomeFragment) fragments.get(0)).scrollToTop();
                    break;
                }
                case R.id.bottomNavMore: {
                    ((HomeFragment) fragments.get(1)).scrollToTop();
                    break;
                }
            }
        });
    }



}