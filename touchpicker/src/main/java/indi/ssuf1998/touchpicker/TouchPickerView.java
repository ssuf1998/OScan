package indi.ssuf1998.touchpicker;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import indi.ssuf1998.touchpicker.databinding.TouchPickerViewBinding;

public class TouchPickerView extends RelativeLayout {
    private final TouchPickerViewBinding binding;
    private int currentIdx = 0;
    private int liveIdx = 0;
    private boolean isOffsetYEnough = false;
    private boolean isOffsetXEnough = false;

    private int itemHeight = 0;
    private int rangeMax = 0;
    private float downX = 0, downY = 0;
    private float lastOffsetY;
    private float initOffsetY;
    private boolean haveChosen = false;

    private int menuWidth;
    private float menuWidthPercent;
    private int menuItemTextAppearance;
    private int menuBgColor;

    private final List<TouchPickerItem> items = new ArrayList<>();
    private int currentItemNewValue;
    private ItemAttrChangedListener mItemAttrChangedListener;
    private AttrValueCommitListener mAttrValueCommitListener;

    public TouchPickerView(Context context) {
        this(context, null, 0, 0);
    }

    public TouchPickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public TouchPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TouchPickerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        binding = TouchPickerViewBinding.inflate(LayoutInflater.from(context), this);
        initAttrs(context, attrs);

        binding.getRoot().post(() -> {
            initUI();
        });
    }

    private void initUI() {
        setBackgroundColor(Color.TRANSPARENT);

        final TouchPickerAdapter<Integer> adapter = new TouchPickerAdapter<>(items);
        adapter.setTextAppearance(menuItemTextAppearance);
        final LinearLayoutManager mgr = new LinearLayoutManager(getContext());
        mgr.setOrientation(LinearLayoutManager.VERTICAL);
        binding.attrMenuList.setAdapter(adapter);
        binding.attrMenuList.setLayoutManager(mgr);

        final RelativeLayout.LayoutParams attrMenuLP =
                (RelativeLayout.LayoutParams) binding.attrMenu.getLayoutParams();
        attrMenuLP.width = (menuWidth == 0 ? (int) (getWidth() * menuWidthPercent) : menuWidth);

        final RelativeLayout.LayoutParams pickedOneLP =
                (RelativeLayout.LayoutParams) binding.pickedOne.getLayoutParams();
        pickedOneLP.width = (menuWidth == 0 ? (int) (getWidth() * menuWidthPercent) : menuWidth) -
                (binding.attrMenu.getPaddingEnd() + binding.attrMenu.getPaddingStart());

        binding.attrMenu.setLayoutParams(attrMenuLP);
        binding.pickedOne.setLayoutParams(pickedOneLP);

        itemHeight = binding.pickedOne.getHeight();

        final int headerHeight = binding.arrowUp.getHeight() + binding.attrMenu.getPaddingTop();

        initOffsetY = lastOffsetY = getHeight() / 2f - headerHeight - itemHeight / 2f;
        binding.attrMenu.setTranslationY(initOffsetY);

        updateUI();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TouchPickerView);
        binding.arrowUp.setImageDrawable(ContextCompat.getDrawable(context,
                ta.getResourceId(R.styleable.TouchPickerView_arrowDrawable, R.drawable.arrow)));
        binding.arrowDown.setImageDrawable(ContextCompat.getDrawable(context,
                ta.getResourceId(R.styleable.TouchPickerView_arrowDrawable, R.drawable.arrow)));
        binding.arrowUp.setImageTintList(
                ColorStateList.valueOf(ta.getColor(R.styleable.TouchPickerView_arrowColor, Color.WHITE))
        );
        binding.arrowDown.setImageTintList(
                ColorStateList.valueOf(ta.getColor(R.styleable.TouchPickerView_arrowColor, Color.WHITE))
        );

        binding.arrowUp.setRotation(ta.getFloat(R.styleable.TouchPickerView_arrowUpRotation, 90f));
        binding.arrowDown.setRotation(ta.getFloat(R.styleable.TouchPickerView_arrowDownRotation, -90f));

        menuWidth = ta.getDimensionPixelSize(R.styleable.TouchPickerView_menuWidth, 0);
        menuWidthPercent = ta.getFloat(R.styleable.TouchPickerView_menuWidthPercent, 0.5f);

//        binding.attrMenu.setBackgroundColor(
//                ta.getColor(R.styleable.TouchPickerView_menuBgColor, 0x80000000)
//        );
        menuBgColor = ta.getColor(R.styleable.TouchPickerView_menuBgColor, 0x80000000);

        menuItemTextAppearance =
                ta.getResourceId(R.styleable.TouchPickerView_menuItemTextAppearance,
                        R.style.TPVDefaultItemTextAppearance);

        binding.pickedAttrText.setTextAppearance(
                ta.getResourceId(R.styleable.TouchPickerView_pickedOneTextAppearance,
                        R.style.TPVDefaultPickedOneTextAppearance)
        );
        binding.pickedAttrValue.setTextAppearance(
                ta.getResourceId(R.styleable.TouchPickerView_pickedOneTextAppearance,
                        R.style.TPVDefaultPickedOneTextAppearance)
        );

        binding.pickedOne.setBackgroundColor(
                ta.getColor(R.styleable.TouchPickerView_pickedOneBgColor, 0xffffffff)
        );

        ta.recycle();
    }

    private void updateUI() {

        binding.attrMenuList.post(() -> {
            assert binding.attrMenuList.getLayoutManager() != null;
            final RelativeLayout currentItemView =
                    (RelativeLayout) binding.attrMenuList.getLayoutManager().findViewByPosition(currentIdx);
            if (currentItemView != null) {
                currentItemView.setVisibility(INVISIBLE);
            }

            drawMenuBg();
        });

        post(() -> {
            binding.pickedAttrText.setText(
                    items.isEmpty() ? "" : items.get(currentIdx).getAttrName()
            );
            binding.pickedAttrValue.setText(
                    String.valueOf(
                            items.isEmpty() ? 0 : items.get(currentIdx).getAttrValue()
                    )
            );
        });

        rangeMax = itemHeight *
                (Optional.ofNullable(binding.attrMenuList.getAdapter())
                        .map(a -> a.getItemCount() - 1)
                        .orElse(0));

    }

    private void drawMenuBg() {
        final int realHeight = itemHeight * items.size() +
                binding.arrowUp.getHeight() + binding.arrowDown.getHeight() +
                binding.attrMenu.getPaddingTop() + binding.attrMenu.getPaddingBottom();

        final Bitmap bmp = Bitmap.createBitmap(
                binding.attrMenu.getWidth(),
                realHeight,
                Bitmap.Config.ARGB_8888
        );

        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(menuBgColor);
        paint.setShadowLayer(
                18, 0, 0, 0x54000000
        );

        final Canvas canvas = new Canvas(bmp);
        canvas.drawRoundRect(
                binding.attrMenu.getPaddingStart(),
                binding.attrMenu.getPaddingTop(),
                binding.attrMenu.getWidth() - binding.attrMenu.getPaddingEnd(),
                realHeight - binding.attrMenu.getPaddingBottom(),
                24, 24, paint
        );

        binding.attrMenu.setBackground(new BitmapDrawable(getContext().getResources(), bmp));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();

        if (items.isEmpty())
            return true;

        if (action == MotionEvent.ACTION_DOWN) {
            downX = event.getX();
            downY = event.getY();

        } else if (action == MotionEvent.ACTION_MOVE) {

            final float offsetX = event.getX() - downX;
            final float offsetY = event.getY() - downY;

            final float k = (event.getY() - downY) / (event.getX() - downX);

            if (Math.abs(k) >= Math.tan(Math.toRadians(30))) {
                if (!isOffsetYEnough && !isOffsetXEnough &&
                        Math.abs(offsetY) >= getHeight() * Const.OFFSET_Y_ENOUGH_THRESHOLD)
                    isOffsetYEnough = true;
            } else {
                if (!isOffsetXEnough && haveChosen && !isOffsetYEnough &&
                        Math.abs(offsetX) >= getWidth() * Const.OFFSET_X_ENOUGH_THRESHOLD)
                    isOffsetXEnough = true;
            }

            if (isOffsetYEnough) {
                binding.attrMenu.setVisibility(VISIBLE);
                binding.pickedOne.setVisibility(VISIBLE);

                final int positionY = (int) (currentIdx * itemHeight - offsetY);

                if (positionY <= rangeMax && positionY >= 0) {
                    binding.attrMenu.setTranslationY(
                            lastOffsetY + offsetY
                    );
                } else if (positionY > rangeMax) {
                    binding.attrMenu.setTranslationY(
                            lastOffsetY + (currentIdx * itemHeight - rangeMax)
                    );
                } else {
                    binding.attrMenu.setTranslationY(
                            lastOffsetY + currentIdx * itemHeight
                    );
                }

                final int adjustOffsetY = Math.min(Math.max(positionY, 0), rangeMax) +
                        (int) (itemHeight / 2f);

                if (liveIdx != adjustOffsetY / itemHeight) {

                    final LinearLayoutManager mgr =
                            (LinearLayoutManager) binding.attrMenuList.getLayoutManager();
                    assert mgr != null;
                    final RelativeLayout nextItemView =
                            (RelativeLayout) mgr.findViewByPosition(adjustOffsetY / itemHeight);
                    final RelativeLayout nowItemView =
                            (RelativeLayout) mgr.findViewByPosition(liveIdx);

                    if (nextItemView != null && nowItemView != null) {
                        nowItemView.setVisibility(VISIBLE);

                        final TextView nextAttrText = nextItemView.findViewById(R.id.attrText);
                        final TextView nextAttrValueText = nextItemView.findViewById(R.id.attrValueText);

                        binding.pickedAttrText.setText(nextAttrText.getText());
                        binding.pickedAttrValue.setText(nextAttrValueText.getText());

                        nextItemView.setVisibility(INVISIBLE);
                    }

                    liveIdx = adjustOffsetY / itemHeight;
                }
            }

            if (isOffsetXEnough) {
                final TouchPickerItem item = items.get(currentIdx);
                final float percent = item.getPercent() +
                        Math.max(Math.min((offsetX / getWidth() / Const.OFFSET_X_PROGRESS_SENSITIVITY),
                                1 - item.getPercent()),
                                -item.getPercent());
                final int newValue = Math.round(percent * (item.getAttrValueMax() - item.getAttrValueMin()))
                        + item.getAttrValueMin();
                currentItemNewValue = newValue;
                if (mItemAttrChangedListener != null && !items.isEmpty())
                    mItemAttrChangedListener.changed(newValue, percent, currentIdx);
            }

        } else if (action == MotionEvent.ACTION_UP) {

            if (isOffsetYEnough) {
                binding.attrMenu.setVisibility(INVISIBLE);
                binding.pickedOne.setVisibility(INVISIBLE);

                setCurrentIdx(liveIdx);

                isOffsetYEnough = false;
            }

            if (isOffsetXEnough) {
                items.get(currentIdx).setAttrValue(currentItemNewValue);
                assert binding.attrMenuList.getAdapter() != null;
                binding.attrMenuList.getAdapter().notifyItemChanged(currentIdx);
                binding.pickedAttrValue.setText(String.valueOf(currentItemNewValue));

                isOffsetXEnough = false;

                if (mAttrValueCommitListener != null)
                    mAttrValueCommitListener.commit();
            }

            performClick();
        }
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public void setItems(List<TouchPickerItem> items) {
        if (binding.attrMenuList.getAdapter() != null) {
            final int previousSize = this.items.size();
            this.items.clear();
            binding.attrMenuList.getAdapter().notifyItemRangeRemoved(0, previousSize);
            this.items.addAll(items);
            binding.attrMenuList.getAdapter().notifyItemRangeInserted(0, this.items.size());
        }

        updateUI();
        setCurrentIdx(-1);

        if (mAttrValueCommitListener != null) {
            mAttrValueCommitListener.commit();
        }
    }

    public void setCurrentIdx(int currentIdx) {
        this.currentIdx = Math.max(0, currentIdx);
        haveChosen = (currentIdx != -1);
        binding.attrMenu.setTranslationY(
                initOffsetY - this.currentIdx * itemHeight
        );
        lastOffsetY = binding.attrMenu.getTranslationY();

        if (binding.attrMenuList.getLayoutManager() != null) {
            for (int i = 0; i < items.size(); i++) {
                final View view = binding.attrMenuList.getLayoutManager().findViewByPosition(i);
                if (view == null) {
                    continue;
                }
                view.setVisibility(i == currentIdx ? INVISIBLE : VISIBLE);
            }
        }
    }

    public interface ItemAttrChangedListener {
        void changed(int newValue, float percent, int idx);
    }

    public void setOnItemAttrChanged(ItemAttrChangedListener mItemAttrChangedListener) {
        this.mItemAttrChangedListener = mItemAttrChangedListener;
    }

    public interface AttrValueCommitListener {
        void commit();
    }

    public void setOnAttrValueCommit(AttrValueCommitListener mAttrValueCommitListener) {
        this.mAttrValueCommitListener = mAttrValueCommitListener;
    }

    public boolean isHaveChosen() {
        return haveChosen;
    }
}
