package it.alterlega.recordsnext.app;

import it.alterlega.recordsnext.gui.RecordsNext2Dashboard;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

/** Exercises the actual v49 GUI layout without launching JFrame/SQLite. */
public final class TopFlopSettingsGuiV49SmokeTest {
    public static void main(String[] args) throws Exception {
        Class<?> unsafeClass=Class.forName("sun.misc.Unsafe");
        Field singleton=unsafeClass.getDeclaredField("theUnsafe");
        singleton.setAccessible(true);
        Object unsafe=singleton.get(null);
        Object dashboard=unsafeClass.getMethod("allocateInstance",Class.class).invoke(unsafe,RecordsNext2Dashboard.class);
        Field field=RecordsNext2Dashboard.class.getDeclaredField("topFlopThresholdFields");
        field.setAccessible(true);
        Map<String,JSpinner> inputs=new LinkedHashMap<>();
        field.set(dashboard,inputs);
        Method build=RecordsNext2Dashboard.class.getDeclaredMethod("buildTopFlopSettingsPage");
        build.setAccessible(true);
        JScrollPane pane=(JScrollPane)build.invoke(dashboard);
        if(inputs.size()!=11)throw new AssertionError("missing thresholds: "+inputs.keySet());
        if(pane.getHorizontalScrollBarPolicy()!=ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)
            throw new AssertionError("horizontal scrolling still required");
        for(int width:new int[]{730,810}) {
            pane.setSize(new Dimension(width,520));
            pane.doLayout();
            JComponent view=(JComponent)pane.getViewport().getView();
            view.setSize(pane.getViewport().getExtentSize());
            view.doLayout();
            for(var entry:inputs.entrySet()) {
                JSpinner spinner=entry.getValue();
                Rectangle r=SwingUtilities.convertRectangle(spinner.getParent(),spinner.getBounds(),view);
                if(r.x<0 || r.width<65 || r.x+r.width>view.getWidth())
                    throw new AssertionError("spinner outside viewport at "+width+": "+entry.getKey()+"="+r+" viewport="+view.getWidth());
            }
        }
        System.out.println("RN4_V49_GUI_THRESHOLDS_OK 11 visible editors, no horizontal scroll");
    }
}
