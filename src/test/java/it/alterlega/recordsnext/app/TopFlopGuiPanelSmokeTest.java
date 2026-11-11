package it.alterlega.recordsnext.app;

import it.alterlega.recordsnext.gui.RecordsNext2Dashboard;
import javax.swing.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;

/** Verifica GUI senza sovrascrivere la configurazione della lega reale. */
public final class TopFlopGuiPanelSmokeTest {
    private TopFlopGuiPanelSmokeTest() {}
    private static void ok(boolean value,String reason){if(!value)throw new AssertionError(reason);}
    @SuppressWarnings("unchecked")
    private static boolean containsLabel(java.awt.Container node,String text) {
        for(java.awt.Component component:node.getComponents()) {
            if(component instanceof JLabel label && text.equals(label.getText()))return true;
            if(component instanceof java.awt.Container child && containsLabel(child,text))return true;
        }
        return false;
    }
    public static void main(String[] args)throws Exception {
        SwingUtilities.invokeAndWait(()->{
            try{
                Constructor<RecordsNext2Dashboard> ctor=RecordsNext2Dashboard.class.getDeclaredConstructor();
                ctor.setAccessible(true);
                Object gui=ctor.newInstance();
                Field nav=RecordsNext2Dashboard.class.getDeclaredField("navButtons");nav.setAccessible(true);
                Map<String,JToggleButton> buttons=(Map<String,JToggleButton>)nav.get(gui);
                ok(buttons.containsKey("topflop"),"Top & Flop navigable dedicated page");
                Field family=RecordsNext2Dashboard.class.getDeclaredField("familyChecks");family.setAccessible(true);
                Map<String,JCheckBox> families=(Map<String,JCheckBox>)family.get(gui);
                Field child=RecordsNext2Dashboard.class.getDeclaredField("childChecks");child.setAccessible(true);
                Map<String,JCheckBox> children=(Map<String,JCheckBox>)child.get(gui);
                ok(families.containsKey("topFlop"),"Top & Flop master exists");
                ok(children.keySet().stream().filter(x->x.startsWith("topFlop.")).count()==20,
                   "20 separate Top & Flop choices");
                families.get("topFlop").doClick();
                ok(children.get("topFlop.tf20").isEnabled(),"keeper goal records available");
                ok(children.get("topFlop.tf17").isEnabled(),"keeper penalty-save records available");
                ok(!children.get("topFlop.tf11").isEnabled(),"unverified incompresi disabled");
                ok(children.get("topFlop.tf05").isEnabled(),"configurable TF05 available");
                ok(children.get("topFlop.tf06").isEnabled(),"configurable TF06 available");
                Field settings=RecordsNext2Dashboard.class.getDeclaredField("topFlopThresholdFields");settings.setAccessible(true);
                Map<String,JSpinner> thresholdFields=(Map<String,JSpinner>)settings.get(gui);
                ok(thresholdFields.size()==11,"eleven persisted settings controls");
                Field dashboardField=RecordsNext2Dashboard.class.getDeclaredField("dashboardPage");
                dashboardField.setAccessible(true);
                JPanel dashboard=(JPanel)dashboardField.get(gui);
                ok(containsLabel(dashboard,"Top & Flop"),"dashboard Top & Flop card visible");
                boolean extraModePresent;
                try{RecordsNext2Dashboard.class.getDeclaredField("topFlopFromScratch");extraModePresent=true;}
                catch(NoSuchFieldException correct){extraModePresent=false;}
                ok(!extraModePresent,"Top & Flop must use global processing mode only");
                Field frame=RecordsNext2Dashboard.class.getDeclaredField("frame");frame.setAccessible(true);
                JFrame window=(JFrame)frame.get(gui);
                window.dispose();
                System.out.println("TOP_FLOP_NATIVE_GUI_PANEL_OK checks=6");
            }catch(Exception ex){throw new RuntimeException(ex);}
        });
    }
}
