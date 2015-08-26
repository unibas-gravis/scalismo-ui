package scalismo.ui.swing.util;

import javax.swing.*;

/* This class is a workaround. ListModel is untyped in JDK 6, and typed in JDK7.
 * Therefore, we need a wrapper class which is untyped, but compiles both in JDK6 and 7.
 * This class only wraps the methods which would otherwise provoke scala type errors.
 */

@SuppressWarnings("unchecked")
public class UntypedListModel {
    public final DefaultListModel peer;

    public UntypedListModel() { peer = new DefaultListModel(); }

    public void addElement(Object e) {
        peer.addElement(e);
    }

    public void add(int index, Object e) {
        peer.add(index, e);
    }
}