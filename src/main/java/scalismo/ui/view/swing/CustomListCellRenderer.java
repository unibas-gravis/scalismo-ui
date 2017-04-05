/*
 * Copyright (C) 2016  University of Basel, Graphics and Vision Research Group 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package scalismo.ui.view.swing;

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
