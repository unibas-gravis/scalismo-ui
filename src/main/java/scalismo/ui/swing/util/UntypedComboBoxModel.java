package scalismo.ui.swing.util;

import javax.swing.*;

/* This class is a workaround. DefaultComboBoxModel is untyped in JDK 6, and typed in JDK7.
 * Therefore, we need a wrapper class which is untyped, but compiles both in JDK6 and 7.
 */

@SuppressWarnings("ALL")
public class UntypedComboBoxModel {
    @SuppressWarnings("rawtypes")
    public final DefaultComboBoxModel model = new DefaultComboBoxModel();

    @SuppressWarnings("unchecked")
    public void addElement(Object element) {
        model.addElement(element);
    }
}
