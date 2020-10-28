package indi.ssuf1998.touchpicker;

public class TouchPickerItem {
    private String attrName;
    private int attrValue;
    private int attrValueMax;
    private int attrValueMin;

    public TouchPickerItem(String attrName, int attrValue, int attrValueMax, int attrValueMin) {
        this.attrName = attrName;
        this.attrValue = Math.max(Math.min(attrValue, attrValueMax), attrValueMin);
        this.attrValueMax = attrValueMax;
        this.attrValueMin = attrValueMin;
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public int getAttrValue() {
        return attrValue;
    }

    public void setAttrValue(int attrValue) {
        this.attrValue = attrValue;
    }

    public int getAttrValueMax() {
        return attrValueMax;
    }

    public void setAttrValueMax(int attrValueMax) {
        this.attrValueMax = attrValueMax;
    }

    public int getAttrValueMin() {
        return attrValueMin;
    }

    public void setAttrValueMin(int attrValueMin) {
        this.attrValueMin = attrValueMin;
    }

    public float getPercent() {
        return (float) (attrValue - attrValueMin) /
                (float) (attrValueMax - attrValueMin);
    }
}
