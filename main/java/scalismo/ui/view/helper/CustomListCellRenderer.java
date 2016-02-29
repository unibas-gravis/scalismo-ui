package scalismo.ui.view.helper;

import javax.swing.*;
import java.awt.*;

/**
 * This is a helper class to allow overriding a DefaultListCellRenderer implementation.
 * This cannot be directly done in Scala, because it produces some weird type errors, so we
 * resort to an abstract method with a well-defined signature that can be overridden in Scala.
 *
 * @see scalismo.ui.view.StatusBar.StatusMessageCellRenderer for an example.
 */
public abstract class CustomListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        return updateListCellRendererComponent(component, list, value, index, isSelected, cellHasFocus);
    }

    public abstract Component updateListCellRendererComponent(Component component, Object list, Object value, int index, boolean isSelected, boolean cellHasFocus);
}
