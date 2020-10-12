package indi.ssuf1998.oscan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;

import indi.ssuf1998.oscan.adapter.Doc;
import indi.ssuf1998.oscan.adapter.DocAdapter;
import indi.ssuf1998.oscan.databinding.HomeFragmentLayoutBinding;

public class HomeFragment extends Fragment {
    private HomeFragmentLayoutBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = HomeFragmentLayoutBinding.inflate(inflater);
        initUI();

        return binding.getRoot();
    }

    private void initUI() {
        ArrayList<Doc> docs = new ArrayList<Doc>() {{
            add(new Doc("test1"));
        }};

        DocAdapter adapter = new DocAdapter(docs);
        LinearLayoutManager layoutMgr = new LinearLayoutManager(getContext());
        binding.docRecyclerView.setLayoutManager(layoutMgr);
        binding.docRecyclerView.setAdapter(adapter);
    }

    public void scrollToTop(){
        binding.docRecyclerView.smoothScrollToPosition(0);
    }
}
