package scalismo.ui.swing.util;

import javax.swing.*;
import java.awt.*;

abstract public class UntypedListCellRenderer extends DefaultListCellRenderer {
    @Override
    public final Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        return getListCellRendererComponentUntyped(list, value, index, isSelected, cellHasFocus);
    }

    public Component getListCellRendererComponentUntyped(Object listAsObject, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        return super.getListCellRendererComponent((JList) listAsObject, value, index, isSelected, cellHasFocus);
    }
}
