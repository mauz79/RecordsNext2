package it.alterlega.recordsnext.gui;

import it.alterlega.recordsnext.app.culometro.CulometroConfig;
import it.alterlega.recordsnext.app.culometro.CulometroConfigLoader;
import it.alterlega.recordsnext.app.culometro.CulometroConfigWriter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class CulometroSettingsDialog extends JDialog {
    private final Path configFile;
    private CulometroConfig config;

    private final JCheckBox enabled = new JCheckBox("Genera il Culometro");
    private final JSpinner minimumMatches = new JSpinner(new SpinnerNumberModel(20, 10, 40, 1));
    private final JSpinner kScale = new JSpinner(new SpinnerNumberModel(4.15, 3.00, 6.00, 0.05));
    private final JSpinner secondaryWeight = new JSpinner(new SpinnerNumberModel(0.20, 0.10, 0.35, 0.01));
    private final JSpinner rarityMultiplier = new JSpinner(new SpinnerNumberModel(5.25, 3.00, 6.50, 0.05));
    private final JSpinner minimumOccurrences = new JSpinner(new SpinnerNumberModel(3, 1, 20, 1));

    private final JSlider simpleSensitivity = new JSlider(300, 600, 415);
    private final JSlider simpleReliability = new JSlider(10, 40, 20);
    private final JComboBox<String> simpleRarity = new JComboBox<>(new String[]{"Bassa", "Normale", "Alta"});
    private final JComboBox<String> simpleProfile = new JComboBox<>(new String[]{"Equilibrato", "Prudente", "Reattivo", "Personalizzato"});
    private final JLabel sensitivityValue = new JLabel();
    private final JLabel reliabilityValue = new JLabel();

    private final JComboBox<String> preset = new JComboBox<>(new String[]{"GOLIARDICO", "NEUTRAL", "CUSTOM"});
    private final ComponentTableModel componentModel = new ComponentTableModel();
    private final LabelTableModel labelModel = new LabelTableModel();
    private boolean synchronizing;
    private boolean saved;

    CulometroSettingsDialog(Window owner, Path configFile) throws Exception {
        super(owner, "Culometro · configurazione", ModalityType.APPLICATION_MODAL);
        this.configFile = configFile;
        this.config = CulometroConfigLoader.load(configFile);
        build();
        load();
    }

    boolean open() {
        setVisible(true);
        return saved;
    }

    private void build() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(880, 650));
        setSize(980, 720);
        setLocationRelativeTo(getOwner());

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(16, 16, 12, 16));
        root.setBackground(new Color(244, 247, 252));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("Culometro configurabile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(26, 52, 105));
        top.add(title, BorderLayout.WEST);
        enabled.setFont(new Font("Segoe UI", Font.BOLD, 13));
        top.add(enabled, BorderLayout.EAST);
        root.add(top, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Semplice", buildSimplePanel());
        tabs.addTab("Avanzata", buildAdvancedPanel());
        tabs.addTab("Pesi fattori", new JScrollPane(buildComponentTable()));
        tabs.addTab("Etichette", buildLabelsPanel());
        root.add(tabs, BorderLayout.CENTER);

        JButton save = new JButton("Salva configurazione");
        JButton cancel = new JButton("Annulla");
        save.addActionListener(e -> save());
        cancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(cancel);
        buttons.add(save);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel buildSimplePanel() {
        JPanel page = new JPanel(new BorderLayout(10, 10));
        page.setBorder(new EmptyBorder(14, 14, 14, 14));
        page.setBackground(Color.WHITE);

        JPanel controls = new JPanel(new GridBagLayout());
        controls.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.insets = new Insets(7, 7, 7, 7);

        addSimpleRow(controls, g, "Profilo di calcolo", simpleProfile,
                "Applica valori coordinati, modificabili successivamente.");
        configureSlider(simpleSensitivity, 50, 5);
        addSliderRow(controls, g, "Sensibilità dell'indice", simpleSensitivity, sensitivityValue,
                "Più bassa = valori più estremi; più alta = classifica più compressa.");
        configureSlider(simpleReliability, 5, 1);
        addSliderRow(controls, g, "Partite per piena affidabilità", simpleReliability, reliabilityValue,
                "Riduce gli estremi quando il campione è piccolo.");
        addSimpleRow(controls, g, "Influenza della rarità", simpleRarity,
                "Limita quanto un episodio raro può incidere sul risultato.");

        JPanel presets = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        presets.setOpaque(false);
        JButton balanced = new JButton("Equilibrato");
        JButton prudent = new JButton("Prudente");
        JButton reactive = new JButton("Reattivo");
        balanced.addActionListener(e -> applySimpleProfile("Equilibrato"));
        prudent.addActionListener(e -> applySimpleProfile("Prudente"));
        reactive.addActionListener(e -> applySimpleProfile("Reattivo"));
        presets.add(new JLabel("Impostazioni rapide:"));
        presets.add(balanced);
        presets.add(prudent);
        presets.add(reactive);
        g.gridy++;
        controls.add(presets, g);

        ChangeListener listener = e -> syncSimpleToAdvanced();
        simpleSensitivity.addChangeListener(listener);
        simpleReliability.addChangeListener(listener);
        simpleRarity.addActionListener(e -> syncSimpleToAdvanced());
        simpleProfile.addActionListener(e -> {
            if (!synchronizing) applySimpleProfile(String.valueOf(simpleProfile.getSelectedItem()));
        });

        page.add(controls, BorderLayout.NORTH);
        JLabel note = new JLabel("<html>Per regolare i singoli fattori usa le schede Avanzata e Pesi fattori. "
                + "I valori restano sempre entro i range ammessi.</html>");
        note.setForeground(new Color(88, 96, 112));
        page.add(note, BorderLayout.SOUTH);
        return page;
    }

    private JPanel buildAdvancedPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));
        panel.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(7, 7, 7, 16);
        addRow(panel, g, "Partite minime affidabili", minimumMatches);
        addRow(panel, g, "Sensibilità della scala", kScale);
        addRow(panel, g, "Peso evento secondario", secondaryWeight);
        addRow(panel, g, "Moltiplicatore massimo rarità", rarityMultiplier);
        addRow(panel, g, "Occorrenze storiche minime", minimumOccurrences);
        g.gridy++;
        g.weighty = 1;
        panel.add(Box.createVerticalGlue(), g);

        ChangeListener sync = e -> syncAdvancedToSimple();
        minimumMatches.addChangeListener(sync);
        kScale.addChangeListener(sync);
        rarityMultiplier.addChangeListener(sync);
        return panel;
    }

    private JTable buildComponentTable() {
        JTable table = new JTable(componentModel);
        table.setRowHeight(25);
        table.setAutoCreateRowSorter(true);
        return table;
    }

    private JPanel buildLabelsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setBackground(Color.WHITE);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.setOpaque(false);
        controls.add(new JLabel("Preset attivo:"));
        controls.add(preset);
        JButton goliardico = new JButton("Ripristina Goliardico");
        JButton neutro = new JButton("Ripristina Neutro");
        goliardico.addActionListener(e -> resetLabels("GOLIARDICO_DEFAULT", "GOLIARDICO"));
        neutro.addActionListener(e -> resetLabels("NEUTRAL_DEFAULT", "NEUTRAL"));
        controls.add(goliardico);
        controls.add(neutro);
        panel.add(controls, BorderLayout.NORTH);
        JTable table = new JTable(labelModel);
        table.setRowHeight(26);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JLabel hint = new JLabel("Le etichette sono sempre modificabili. Il reset ripristina la base originale del preset scelto.");
        hint.setForeground(new Color(88, 96, 112));
        panel.add(hint, BorderLayout.SOUTH);
        return panel;
    }

    private static void configureSlider(JSlider slider, int major, int minor) {
        slider.setMajorTickSpacing(major);
        slider.setMinorTickSpacing(minor);
        slider.setPaintTicks(true);
    }

    private static void addSimpleRow(JPanel panel, GridBagConstraints g, String label,
                                     JComponent component, String hint) {
        JPanel row = new JPanel(new BorderLayout(12, 2));
        row.setOpaque(false);
        JLabel title = new JLabel(label);
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        row.add(title, BorderLayout.WEST);
        row.add(component, BorderLayout.CENTER);
        JLabel help = new JLabel(hint);
        help.setForeground(new Color(96, 104, 120));
        row.add(help, BorderLayout.SOUTH);
        panel.add(row, g);
        g.gridy++;
    }

    private static void addSliderRow(JPanel panel, GridBagConstraints g, String label,
                                     JSlider slider, JLabel value, String hint) {
        JPanel control = new JPanel(new BorderLayout(8, 0));
        control.setOpaque(false);
        control.add(slider, BorderLayout.CENTER);
        value.setPreferredSize(new Dimension(55, 24));
        value.setHorizontalAlignment(SwingConstants.RIGHT);
        value.setFont(new Font("Segoe UI", Font.BOLD, 12));
        control.add(value, BorderLayout.EAST);
        addSimpleRow(panel, g, label, control, hint);
    }

    private static void addRow(JPanel panel, GridBagConstraints g, String label, JComponent component) {
        g.gridx = 0;
        g.weightx = 0;
        g.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(label), g);
        g.gridx = 1;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, g);
        g.gridy++;
    }

    private void load() {
        synchronizing = true;
        enabled.setSelected(config.enabled());
        minimumMatches.setValue(config.minimumMatches());
        kScale.setValue(config.kScale().doubleValue());
        secondaryWeight.setValue(config.secondaryWeight().doubleValue());
        rarityMultiplier.setValue(config.maximumRarityMultiplier().doubleValue());
        minimumOccurrences.setValue(config.minimumHistoricalOccurrences());
        preset.setSelectedItem(config.labelConfiguration().preset());
        componentModel.set(config.components());
        labelModel.set(config.labels());
        syncAdvancedToSimple();
        simpleProfile.setSelectedItem("Personalizzato");
        synchronizing = false;
    }

    private void applySimpleProfile(String profile) {
        if ("Personalizzato".equals(profile) || synchronizing) return;
        synchronizing = true;
        switch (profile) {
            case "Prudente" -> {
                simpleSensitivity.setValue(500);
                simpleReliability.setValue(30);
                simpleRarity.setSelectedItem("Bassa");
            }
            case "Reattivo" -> {
                simpleSensitivity.setValue(350);
                simpleReliability.setValue(14);
                simpleRarity.setSelectedItem("Alta");
            }
            default -> {
                simpleSensitivity.setValue(415);
                simpleReliability.setValue(20);
                simpleRarity.setSelectedItem("Normale");
            }
        }
        synchronizing = false;
        syncSimpleToAdvanced();
    }

    private void syncSimpleToAdvanced() {
        if (synchronizing) return;
        synchronizing = true;
        kScale.setValue(simpleSensitivity.getValue() / 100.0);
        minimumMatches.setValue(simpleReliability.getValue());
        rarityMultiplier.setValue(switch (String.valueOf(simpleRarity.getSelectedItem())) {
            case "Bassa" -> 3.50;
            case "Alta" -> 6.50;
            default -> 5.25;
        });
        sensitivityValue.setText(String.format(java.util.Locale.ROOT, "%.2f", simpleSensitivity.getValue() / 100.0));
        reliabilityValue.setText(String.valueOf(simpleReliability.getValue()));
        if (!"Personalizzato".equals(simpleProfile.getSelectedItem())) {
            simpleProfile.setSelectedItem("Personalizzato");
        }
        synchronizing = false;
    }

    private void syncAdvancedToSimple() {
        if (synchronizing) return;
        synchronizing = true;
        simpleSensitivity.setValue((int) Math.round(((Number) kScale.getValue()).doubleValue() * 100));
        simpleReliability.setValue(((Number) minimumMatches.getValue()).intValue());
        double rarity = ((Number) rarityMultiplier.getValue()).doubleValue();
        simpleRarity.setSelectedItem(rarity <= 4.0 ? "Bassa" : rarity >= 6.0 ? "Alta" : "Normale");
        sensitivityValue.setText(String.format(java.util.Locale.ROOT, "%.2f", simpleSensitivity.getValue() / 100.0));
        reliabilityValue.setText(String.valueOf(simpleReliability.getValue()));
        simpleProfile.setSelectedItem("Personalizzato");
        synchronizing = false;
    }

    private void resetLabels(String source, String activePreset) {
        List<CulometroConfig.LabelBand> bands = config.labelConfiguration().presetDefaults().get(source);
        if (bands == null) return;
        labelModel.set(bands);
        preset.setSelectedItem(activePreset);
    }

    private void save() {
        try {
            Map<String, CulometroConfig.Component> components = componentModel.toMap();
            String selectedPreset = String.valueOf(preset.getSelectedItem());
            String resetSource = switch (selectedPreset) {
                case "NEUTRAL" -> "NEUTRAL_DEFAULT";
                case "GOLIARDICO" -> "GOLIARDICO_DEFAULT";
                default -> config.labelConfiguration().resetSource();
            };
            List<CulometroConfig.LabelBand> activeBands = labelModel.toList();
            List<CulometroConfig.LabelBand> resetBands = config.labelConfiguration().presetDefaults().get(resetSource);
            boolean customized = resetBands == null || !activeBands.equals(resetBands);
            CulometroConfig.LabelConfiguration labels = new CulometroConfig.LabelConfiguration(
                    selectedPreset, customized, resetSource, activeBands,
                    config.labelConfiguration().presetDefaults());
            CulometroConfig updated = new CulometroConfig(
                    enabled.isSelected(), ((Number) minimumMatches.getValue()).intValue(),
                    decimal(kScale), decimal(secondaryWeight), decimal(rarityMultiplier),
                    ((Number) minimumOccurrences.getValue()).intValue(), components, labels);
            CulometroConfigWriter.save(configFile, updated);
            config = updated;
            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Configurazione non valida", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static BigDecimal decimal(JSpinner spinner) {
        return BigDecimal.valueOf(((Number) spinner.getValue()).doubleValue());
    }

    private static final class ComponentTableModel extends AbstractTableModel {
        private final List<Row> rows = new ArrayList<>();
        void set(Map<String, CulometroConfig.Component> source) {
            rows.clear();
            source.forEach((id, c) -> rows.add(new Row(id, c.enabled(), c.weight(), c.min(), c.max())));
            fireTableDataChanged();
        }
        Map<String, CulometroConfig.Component> toMap() {
            Map<String, CulometroConfig.Component> result = new LinkedHashMap<>();
            for (Row r : rows) result.put(r.id, new CulometroConfig.Component(r.enabled, r.weight, r.min, r.max));
            return result;
        }
        public int getRowCount() { return rows.size(); }
        public int getColumnCount() { return 5; }
        public String getColumnName(int c) { return new String[]{"Fattore", "Attivo", "Peso", "Min", "Max"}[c]; }
        public Class<?> getColumnClass(int c) { return c == 1 ? Boolean.class : c >= 2 ? BigDecimal.class : String.class; }
        public boolean isCellEditable(int r, int c) { return c == 1 || c == 2; }
        public Object getValueAt(int r, int c) {
            Row x = rows.get(r);
            return switch (c) { case 0 -> x.id; case 1 -> x.enabled; case 2 -> x.weight; case 3 -> x.min; default -> x.max; };
        }
        public void setValueAt(Object value, int r, int c) {
            Row x = rows.get(r);
            if (c == 1) x.enabled = Boolean.TRUE.equals(value);
            if (c == 2) x.weight = new BigDecimal(String.valueOf(value));
            fireTableRowsUpdated(r, r);
        }
        private static final class Row {
            final String id;
            boolean enabled;
            BigDecimal weight;
            final BigDecimal min;
            final BigDecimal max;
            Row(String id, boolean enabled, BigDecimal weight, BigDecimal min, BigDecimal max) {
                this.id = id;
                this.enabled = enabled;
                this.weight = weight;
                this.min = min;
                this.max = max;
            }
        }
    }

    private static final class LabelTableModel extends AbstractTableModel {
        private final List<CulometroConfig.LabelBand> rows = new ArrayList<>();
        void set(List<CulometroConfig.LabelBand> source) {
            rows.clear();
            rows.addAll(source);
            fireTableDataChanged();
        }
        List<CulometroConfig.LabelBand> toList() { return List.copyOf(rows); }
        public int getRowCount() { return rows.size(); }
        public int getColumnCount() { return 2; }
        public String getColumnName(int c) { return c == 0 ? "Da indice" : "Etichetta"; }
        public Class<?> getColumnClass(int c) { return c == 0 ? BigDecimal.class : String.class; }
        public boolean isCellEditable(int r, int c) { return true; }
        public Object getValueAt(int r, int c) {
            var x = rows.get(r);
            return c == 0 ? x.min() : x.label();
        }
        public void setValueAt(Object value, int r, int c) {
            var x = rows.get(r);
            rows.set(r, c == 0
                    ? new CulometroConfig.LabelBand(new BigDecimal(String.valueOf(value)), x.label())
                    : new CulometroConfig.LabelBand(x.min(), String.valueOf(value)));
            fireTableRowsUpdated(r, r);
        }
    }
}
