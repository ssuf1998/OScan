package indi.ssuf1998.itempicker;

import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ItemPickerHelper {
    public static List<TextView> viewsFromStrings(Context context, String[] strings) {
        List<TextView> texts = new ArrayList<>();
        for (String s : strings) {
            final TextView t = new TextView(context);
            t.setText(s);
            t.setGravity(Gravity.CENTER);
            texts.add(t);
        }
        return texts;
    }

}
