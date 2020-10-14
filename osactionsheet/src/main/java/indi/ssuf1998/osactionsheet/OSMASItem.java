package indi.ssuf1998.osactionsheet;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

public class OSMASItem {
    private String itemText;
    private Drawable itemIcon;
    private int itemTextColor;
    private int typefaceStyle;

    public OSMASItem(String itemText, Drawable itemIcon, int itemTextColor, int typefaceStyle) {
        this.itemText = itemText;
        this.itemIcon = itemIcon;
        this.itemTextColor = itemTextColor;
        this.typefaceStyle = typefaceStyle;
    }

    public OSMASItem(String itemText) {
        this(itemText, null, 0, Typeface.NORMAL);
    }

    public OSMASItem(String itemText, Drawable itemIcon) {
        this(itemText, itemIcon, 0, Typeface.NORMAL);
    }

    public OSMASItem() {
        this("", null, 0, Typeface.NORMAL);
    }

    public String getItemText() {
        return itemText;
    }

    public OSMASItem setItemText(String itemText) {
        this.itemText = itemText;
        return this;
    }

    public Drawable getItemIcon() {
        return itemIcon;
    }

    public OSMASItem setItemIcon(Drawable itemIcon) {
        this.itemIcon = itemIcon;
        return this;
    }

    public int getItemTextColor() {
        return itemTextColor;
    }

    public OSMASItem setItemTextColor(int itemTextColor) {
        this.itemTextColor = itemTextColor;
        return this;
    }

    public int getTypefaceStyle() {
        return typefaceStyle;
    }

    public OSMASItem setTypefaceStyle(int typefaceStyle) {
        this.typefaceStyle = typefaceStyle;
        return this;
    }
}
