package indi.ssuf1998.oscan;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.SparseIntArray;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;

import indi.ssuf1998.osactionsheet.OSMASItem;
import indi.ssuf1998.osactionsheet.OSMenuActionSheet;
import indi.ssuf1998.oscan.adapter.MainViewPager2Adapter;
import indi.ssuf1998.oscan.databinding.MainActivityLayoutBinding;

public class MainActivity extends AppCompatActivity {
    private MainActivityLayoutBinding binding;
    private OSMenuActionSheet scanMenuAS;
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

        initUI();
        bindListeners();

    }

    private void initUI() {
        fragments = new ArrayList<Fragment>() {{
            add(new HomeFragment());
            add(new HomeFragment());
        }};

        MainViewPager2Adapter MVP2Adapter = new MainViewPager2Adapter(this, fragments);
        binding.mViewPager2.setAdapter(MVP2Adapter);
        binding.mViewPager2.setUserInputEnabled(false);

        ArrayList<OSMASItem> items = new ArrayList<OSMASItem>() {{
            add(new OSMASItem().setItemText(getString(R.string.scan_action_sheet_doc))
                    .setItemIcon(ContextCompat.getDrawable(
                            MainActivity.this,
                            R.drawable.ic_fluent_document_24_filled))
            );
            add(new OSMASItem().setItemText(getString(R.string.scan_action_sheet_card))
                    .setItemIcon(ContextCompat.getDrawable(
                            MainActivity.this,
                            R.drawable.ic_fluent_contact_card_24_filled))
            );
            add(new OSMASItem().setItemText(getString(R.string.scan_action_sheet_code))
                    .setItemIcon(ContextCompat.getDrawable(
                            MainActivity.this,
                            R.drawable.ic_fluent_qr_code_24_filled))
            );
        }};

        scanMenuAS = new OSMenuActionSheet(getString(R.string.scan_action_sheet_title), items);
        scanMenuAS.setDecoration(new RecycleViewDivider(this));
        scanMenuAS.setOnItemClickListener(idx -> {
            if (idx==0){
                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                MainActivity.this.startActivity(intent);
            }
            scanMenuAS.dismiss();
        });

    }

    private void bindListeners() {
        binding.bottomNav.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottomNavScan) {
                scanMenuAS.show(getSupportFragmentManager(), "scanMenuAS");
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