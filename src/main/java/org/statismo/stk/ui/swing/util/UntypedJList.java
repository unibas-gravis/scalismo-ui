package org.statismo.stk.ui.swing.util;

import javax.swing.*;

/* This class is a workaround. JList is untyped in JDK 6, and typed in JDK7.
 * Therefore, we need a wrapper class which is untyped, but compiles both in JDK6 and 7.
 */

public class UntypedJList {
    public final JList peer;

    public UntypedJList(final Object[] contents) {
        peer = new JList(contents);
    }
}
