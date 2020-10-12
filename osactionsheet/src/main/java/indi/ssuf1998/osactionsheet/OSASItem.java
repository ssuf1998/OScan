package indi.ssuf1998.osactionsheet;

public class OSASItem {
    private String itemText = "OSASItemText";

    public OSASItem(String itemText) {
        this.itemText = itemText;
    }

    public OSASItem() {
    }

    public String getItemText() {
        return itemText;
    }

    public void setItemText(String itemText) {
        this.itemText = itemText;
    }
}
