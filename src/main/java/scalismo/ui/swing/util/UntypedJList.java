package scalismo.ui.swing.util;

import javax.swing.*;

/* This class is a workaround. JList is untyped in JDK 6, and typed in JDK7.
 * Therefore, we need a wrapper class which is untyped, but compiles both in JDK6 and 7.
 */

@SuppressWarnings("ALL")
public class UntypedJList {
    @SuppressWarnings("rawtypes")
    public final JList peer;

    @SuppressWarnings("unchecked")
    public UntypedJList(final Object[] contents) {
        peer = new JList(contents);
    }
}
