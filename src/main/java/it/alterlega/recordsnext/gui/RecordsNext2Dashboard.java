package it.alterlega.recordsnext.gui;

import it.alterlega.recordsnext.ConfigurationSchema;
import it.alterlega.recordsnext.app.PipelineConfig;
import it.alterlega.recordsnext.app.ProcessingMode;
import it.alterlega.recordsnext.app.ProcessingOptions;
import it.alterlega.recordsnext.app.RecordsNextPipeline;
import it.alterlega.recordsnext.app.config.ProcessingConfigLoader;
import it.alterlega.recordsnext.app.config.ProcessingConfigWriter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public final class RecordsNext2Dashboard {
    private static final Color NAVY = new Color(22, 42, 79);
    private static final Color BLUE = new Color(38, 86, 171);
    private static final Color RED = new Color(198, 38, 48);
    private static final Color BG = new Color(241, 245, 251);
    private static final Color BORDER = new Color(211, 219, 232);
    private static final Color MUTED = new Color(82, 92, 110);

    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final Path processingFile = root.resolve("config/processing.json");
    private final Path culometroFile = root.resolve("config/culometro.json");
    private final Path propertiesFile = root.resolve("config/recordsnext-gui.properties");

    private final JFrame frame = new JFrame("RecordsNext by mauz79 · 2.0");
    private final CardLayout pages = new CardLayout();
    private final JPanel pageHost = new JPanel(pages);
    private final Map<String, JToggleButton> navButtons = new LinkedHashMap<>();
    private final Map<String, JCheckBox> familyChecks = new LinkedHashMap<>();
    private final Map<String, JCheckBox> childChecks = new LinkedHashMap<>();
    private final Map<String, JLabel> familySummaries = new LinkedHashMap<>();
    private final Map<String, JTextField> modifierNameFields = new LinkedHashMap<>();

    private final JCheckBox culometro = new JCheckBox("Genera Culometro");
    private final JCheckBox publish = new JCheckBox("Pubblica nel sito al termine");
    private final JRadioButton full = new JRadioButton("Completa");
    private final JRadioButton consolidated = new JRadioButton("Consolidata");
    private final JTextArea log = new JTextArea();
    private final JProgressBar progress = new JProgressBar(0, 100);
    private final JLabel status = new JLabel("Pronto");
    private final JLabel phase = new JLabel("Nessuna elaborazione in corso");
    private final JButton run = new JButton("Elabora");
    private final JTextField exampleSiteDirectory = new JTextField();
    private final JLabel exampleRootTarget = new JLabel("Non selezionata");
    private final JLabel exampleViewsTarget = new JLabel("Non selezionata");
    private final JLabel exampleJsTarget = new JLabel("Non selezionata");
    private final JButton installExamples = new JButton("Installa esempi");
    private ProcessingConfigWriter.State state;
    private JPanel dashboardPage;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new RecordsNext2Dashboard().show();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, ex.toString(), "RecordsNext 2.0", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private RecordsNext2Dashboard() throws Exception {
        bootstrap();
        state = ProcessingConfigWriter.load(processingFile);
        applyLookAndFeel();
        build();
        loadState();
    }

    private void bootstrap() throws Exception {
        Files.createDirectories(root.resolve("config"));
        Files.createDirectories(root.resolve("data/database"));
        ConfigurationSchema.initializeEmpty(root.resolve("data/database/recordsnext.db"));
        if (!Files.isRegularFile(processingFile)) {
            ProcessingConfigWriter.save(processingFile,
                    new ProcessingConfigWriter.State(true, true, true, true, true, false, false));
        }
    }

    private static void applyLookAndFeel() {
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 12));
        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 12));
        UIManager.put("CheckBox.font", new Font("Segoe UI", Font.PLAIN, 12));
        UIManager.put("RadioButton.font", new Font("Segoe UI", Font.PLAIN, 12));
        UIManager.put("TabbedPane.font", new Font("Segoe UI", Font.BOLD, 12));
    }

    private void build() {
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { saveQuietly(); frame.dispose(); }
        });
        frame.setMinimumSize(new Dimension(900, 620));
        frame.setSize(1040, 700);
        frame.setLocationRelativeTo(null);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(BG);
        rootPanel.add(buildSidebar(), BorderLayout.WEST);
        rootPanel.add(buildMain(), BorderLayout.CENTER);
        frame.setContentPane(rootPanel);
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setPreferredSize(new Dimension(180, 700));
        side.setBackground(NAVY);
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(new EmptyBorder(18, 12, 16, 12));

        JLabel brand = new JLabel("RecordsNext");
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.setFont(new Font("Segoe UI Black", Font.BOLD, 22));
        brand.setForeground(Color.WHITE);
        side.add(brand);
        JLabel version = new JLabel("by mauz79 · 2.0");
        version.setAlignmentX(Component.LEFT_ALIGNMENT);
        version.setForeground(new Color(174, 192, 224));
        version.setFont(new Font("Segoe UI", Font.BOLD, 11));
        side.add(version);
        side.add(Box.createVerticalStrut(28));

        ButtonGroup group = new ButtonGroup();
        addNav(side, group, "dashboard", "Dashboard");
        addNav(side, group, "seasons", "Configurazione stagioni");
        addNav(side, group, "project", "Progetto e motore");
        addNav(side, group, "tools", "Debug e strumenti");
        addNav(side, group, "log", "Log e diagnostica");
        side.add(Box.createVerticalGlue());
        JLabel footer = new JLabel("Backend modulare attivo");
        footer.setForeground(new Color(130, 219, 164));
        footer.setFont(new Font("Segoe UI", Font.BOLD, 11));
        side.add(footer);
        return side;
    }

    private void addNav(JPanel side, ButtonGroup group, String id, String text) {
        JToggleButton button = new JToggleButton(text);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setForeground(Color.WHITE);
        button.setBackground(NAVY);
        button.setBorder(new EmptyBorder(0, 12, 0, 8));
        button.setFocusPainted(false);
        button.addActionListener(e -> showPage(id));
        group.add(button);
        side.add(button);
        side.add(Box.createVerticalStrut(6));
        navButtons.put(id, button);
    }

    private JPanel buildMain() {
        JPanel main = new JPanel(new BorderLayout(14, 12));
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(12, 14, 10, 14));
        main.add(buildHeader(), BorderLayout.NORTH);

        pageHost.setOpaque(false);
        dashboardPage = buildDashboardPage();
        pageHost.add(dashboardPage, "dashboard");
        pageHost.add(buildSeasonsPage(), "seasons");
        pageHost.add(buildFamiliesPage(), "families");
        pageHost.add(buildCulometroPage(), "culometro");
        pageHost.add(buildProjectPage(), "project");
        pageHost.add(buildPublishPage(), "tools");
        pageHost.add(buildLogPage(), "log");
        main.add(pageHost, BorderLayout.CENTER);
        main.add(buildCompactRunBar(), BorderLayout.SOUTH);
        navButtons.get("dashboard").setSelected(true);
        return main;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("RecordsNext 2.0");
        title.setFont(new Font("Segoe UI Black", Font.BOLD, 25));
        title.setForeground(RED);
        header.add(title, BorderLayout.WEST);
        status.setFont(new Font("Segoe UI", Font.BOLD, 12));
        status.setForeground(new Color(35, 105, 62));
        header.add(status, BorderLayout.EAST);
        return header;
    }

    private JPanel buildDashboardPage() {
        JPanel page = new JPanel(new BorderLayout(0, 10));
        page.setOpaque(false);
        page.add(centeredSectionTitle(
                "Dashboard",
                "Configura RecordsNext e avvia l’elaborazione dalla barra inferiore"
        ), BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 3, 12, 0));
        cards.setOpaque(false);
        cards.setPreferredSize(new Dimension(0, 185));
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 185));
        cards.add(actionCard(
                "Configurazione stagioni",
                seasonSummary() + "\nGestite e manuali\nSorgenti, siti e tabellini",
                "Apri configurazione",
                this::openSeasonConfiguration
        ));
        cards.add(actionCard(
                "Famiglie record",
                activeFamiliesSummary() + "\n\nSelezione granulare dei sottorecord",
                activeFamilyCountFromState() + " famiglie selezionate",
                () -> showPage("families")
        ));
        cards.add(actionCard(
                "Soglie, Fortuna e Culometro",
                thresholdsCulometroSummary(),
                state.culometro() ? "Culometro attivo" : "Culometro disattivo",
                () -> showPage("culometro")
        ));

        JPanel cardsTop = new JPanel(new BorderLayout());
        cardsTop.setOpaque(false);
        cardsTop.add(cards, BorderLayout.NORTH);
        page.add(cardsTop, BorderLayout.CENTER);
        return page;
    }

    private String seasonSummary() {
        try {
            Properties properties = new Properties();
            if (Files.isRegularFile(propertiesFile)) {
                try (InputStream input = Files.newInputStream(propertiesFile)) {
                    properties.load(input);
                }
            }
            Path database = root.resolve(
                    properties.getProperty("database", "data/database/recordsnext.db")
            ).normalize();
            if (Files.isRegularFile(database)) {
                int count = new SeasonConfigurationRepository(database).load().size();
                if (count > 0) return count == 1 ? "1 stagione" : count + " stagioni";
            }
        } catch (Exception ignored) {
            // Ripiego sul JSON pubblico, se il database non è ancora disponibile.
        }

        Path seasons = root.resolve("config/seasons.json");
        if (!Files.isRegularFile(seasons)) return "Nessuna stagione configurata";
        try {
            String json = Files.readString(seasons);
            int count = json.split("\"seasonNumber\"\s*:", -1).length - 1;
            if (count == 0) count = json.split("\"seasonId\"\s*:", -1).length - 1;
            return count == 1 ? "1 stagione" : count + " stagioni";
        } catch (Exception ex) {
            return "Stagioni configurate";
        }
    }

    private String activeFamiliesSummary() {
        java.util.List<String> names = new java.util.ArrayList<>();
        if (state.classics()) names.add("Classici");
        if (state.series()) names.add("Serie");
        if (state.ru()) names.add("Riserve d'Ufficio");
        if (state.modifiers()) names.add("Modificatori");
        if (names.isEmpty()) return "Nessuna famiglia selezionata";
        return String.join("\n", names);
    }

    private String thresholdsCulometroSummary() {
        String thresholds = state.thresholdsLuck()
                ? "Soglie e indicatori di fortuna attivi"
                : "Soglie e indicatori di fortuna disattivi";
        String ru = state.ru()
                ? "Componenti RU disponibili"
                : "Componenti RU non elaborate";
        String generation = state.culometro()
                ? "Generazione Culometro selezionata"
                : "Generazione Culometro non selezionata";
        return thresholds + "\n" + ru + "\n" + generation;
    }

    private int activeFamilyCountFromState() {
        int count = 0;
        if (state.classics()) count++;
        if (state.series()) count++;
        if (state.ru()) count++;
        if (state.modifiers()) count++;
        return count;
    }

    private JPanel buildSeasonsPage() {
        JPanel page = verticalPage();
        page.add(pageHeader("Configurazione stagioni",
                "Stagioni, sorgenti, siti e collegamenti storici", true));
        JPanel card = cardPanel(new BorderLayout(10, 10));
        JLabel text = new JLabel("<html><b>Gestita:</b> FCM/FCA, sito locale e online, DataA.js e tabellini.<br>"
                + "<b>Manuale:</b> solo anni nel formato YYYY/YYYY e numero stagione.<br>"
                + "Tabellini compatibili: <b>ris*.htm</b>, <b>ris*.html</b> e <b>ris*.php</b>.</html>");
        card.add(text, BorderLayout.CENTER);
        JButton open = new JButton("Apri configurazione stagioni");
        open.addActionListener(e -> openSeasonConfiguration());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actions.setOpaque(false);
        actions.add(open);
        card.add(actions, BorderLayout.SOUTH);
        page.add(card);
        return page;
    }

    private JPanel buildFamiliesPage() {
        JPanel page = new JPanel(new BorderLayout(0, 8));
        page.setOpaque(false);
        page.add(pageHeader("Famiglie record",
                "Attiva la famiglia e scegli i singoli sottorecord da elaborare", true), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Classici", familySelectionPanel("classics", "Classici",
                ProcessingConfigWriter.CLASSICS, CLASSIC_LABELS));
        tabs.addTab("Serie", seriesSelectionPanel());
        tabs.addTab("RU", familySelectionPanel("ru", "Riserve d'Ufficio",
                ProcessingConfigWriter.RU, RU_LABELS));
        tabs.addTab("Modificatori", modifiersSelectionPanel());
        page.add(tabs, BorderLayout.CENTER);

        JButton save = new JButton("Salva selezione granulare");
        save.addActionListener(e -> saveState());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setOpaque(false);
        bottom.add(save);
        page.add(bottom, BorderLayout.SOUTH);
        return page;
    }

    private JPanel seriesSelectionPanel() {
        JPanel panel = familySelectionPanel("series", "Serie",
                ProcessingConfigWriter.SERIES, SERIES_LABELS);
        JLabel note = new JLabel("<html><b>Serie dei modificatori:</b> si selezionano nella card Modificatori, accanto a Massimo, Totale, Media e Utilizzi.</html>");
        note.setForeground(MUTED);
        note.setBorder(new EmptyBorder(4, 12, 4, 12));
        panel.add(note, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel modifiersSelectionPanel() {
        JPanel rootPanel = new JPanel(new BorderLayout(0, 8));
        rootPanel.setOpaque(false);

        JPanel names = cardPanel(new GridBagLayout());
        names.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER), new EmptyBorder(8, 10, 8, 10)));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 3, 3, 8);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        JLabel title = new JLabel("Nomi modificatori personalizzati");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2; c.weightx = 1.0;
        names.add(title, c);
        addModifierNameRow(names, c, 1, "MODM1PERS", "Personalizzato 1");
        addModifierNameRow(names, c, 2, "MODM2PERS", "Personalizzato 2");
        addModifierNameRow(names, c, 3, "MODM3PERS", "Personalizzato 3");
        rootPanel.add(names, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setBackground(Color.WHITE);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(8, 10, 8, 10));
        JCheckBox master = new JCheckBox("Elabora famiglia Modificatori");
        master.setFont(new Font("Segoe UI", Font.BOLD, 15));
        familyChecks.put("modifiers", master);
        JLabel summary = new JLabel();
        summary.setForeground(MUTED);
        familySummaries.put("modifiers", summary);
        JPanel head = new JPanel(new BorderLayout()); head.setOpaque(false);
        head.add(master, BorderLayout.WEST); head.add(summary, BorderLayout.EAST);
        body.add(head);

        body.add(modifierGroupPanel("Modificatori personalizzati", new String[] {"MODM1PERS", "MODM2PERS", "MODM3PERS"}));
        body.add(modifierGroupPanel("Modificatori standard FCM", new String[] {"MODPORTIERE", "MODDIFESA", "MODCENTROCAMPO", "MODATTACCO", "MODMODULO"}));
        body.add(modifierSimpleCheck("modifiers.home-field-deciding", "Fattore Campo decisivo"));
        body.add(modifierSimpleCheck("modifiers.home-field-points-gained", "Punti guadagnati col Fattore Campo"));
        body.add(modifierSimpleCheck("modifiers.home-field-points-lost", "Punti persi fuori casa"));
        body.add(modifierSimpleCheck("modifiers.home-field-balance", "Saldo Fattore Campo"));

        master.addActionListener(e -> {
            boolean enabled = master.isSelected();
            for (String id : ProcessingConfigWriter.MODIFIERS) childChecks.get(id).setEnabled(enabled);
            updateFamilySummary("modifiers", ProcessingConfigWriter.MODIFIERS);
        });
        rootPanel.add(new JScrollPane(body), BorderLayout.CENTER);
        return rootPanel;
    }

    private JPanel modifierGroupPanel(String title, String[] sourceFields) {
        JPanel group = new JPanel();
        group.setOpaque(false);
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setBorder(BorderFactory.createTitledBorder(title));
        for (String sourceField : sourceFields) {
            String fixedName = switch (sourceField) {
                case "MODPORTIERE" -> "Modificatore Portiere FCM";
                case "MODDIFESA" -> "Modificatore Difesa FCM";
                case "MODCENTROCAMPO" -> "Modificatore Centrocampo FCM";
                case "MODATTACCO" -> "Modificatore Attacco FCM";
                case "MODMODULO" -> "Modificatore Modulo FCM";
                default -> sourceField;
            };
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
            row.setOpaque(false);
            JLabel label = new JLabel(fixedName + " (" + sourceField + ")");
            label.setPreferredSize(new Dimension(245, 24));
            row.add(label);
            for (String stat : new String[] {"max", "total", "average", "uses", "series"}) {
                String id = "modifiers." + sourceField.toLowerCase() + "." + stat;
                String text = switch (stat) {
                    case "max" -> "Massimo";
                    case "total" -> "Totale";
                    case "average" -> "Media";
                    case "uses" -> "Utilizzi";
                    default -> "Serie";
                };
                JCheckBox check = new JCheckBox(text);
                check.setToolTipText(id);
                check.addActionListener(e -> updateFamilySummary("modifiers", ProcessingConfigWriter.MODIFIERS));
                childChecks.put(id, check);
                row.add(check);
            }
            group.add(row);
        }
        return group;
    }

    private JCheckBox modifierSimpleCheck(String id, String text) {
        JCheckBox check = new JCheckBox(text);
        check.setToolTipText(id);
        check.addActionListener(e -> updateFamilySummary("modifiers", ProcessingConfigWriter.MODIFIERS));
        childChecks.put(id, check);
        return check;
    }

    private void addModifierNameRow(JPanel panel, GridBagConstraints c, int row,
                                    String sourceField, String label) {
        c.gridwidth = 1; c.gridy = row; c.gridx = 0; c.weightx = 0.0;
        JLabel fieldLabel = new JLabel(label + " (" + sourceField + ")");
        panel.add(fieldLabel, c);

        JTextField field = new JTextField(28);
        field.setToolTipText("Nome mostrato nei dati e nei visualizzatori");
        modifierNameFields.put(sourceField, field);
        c.gridx = 1; c.weightx = 1.0;
        panel.add(field, c);
    }

    private JPanel familySelectionPanel(String familyId, String title, String[] ids, Map<String, String> labels) {
        JPanel body = new JPanel(new BorderLayout(8, 8));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(10, 12, 10, 12));

        JCheckBox master = new JCheckBox("Elabora famiglia " + title);
        master.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JLabel summary = new JLabel();
        summary.setForeground(MUTED);
        familySummaries.put(familyId, summary);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(master, BorderLayout.WEST);
        top.add(summary, BorderLayout.EAST);
        body.add(top, BorderLayout.NORTH);
        familyChecks.put(familyId, master);

        JPanel grid = new JPanel(new GridLayout(0, 2, 8, 8));
        grid.setOpaque(false);
        for (String id : ids) {
            JCheckBox child = new JCheckBox(labels.getOrDefault(id, id));
            child.setToolTipText(id);
            child.addActionListener(e -> updateFamilySummary(familyId, ids));
            childChecks.put(id, child);
            grid.add(child);
        }
        JPanel gridTop = new JPanel(new BorderLayout());
        gridTop.setOpaque(false);
        gridTop.add(grid, BorderLayout.NORTH);
        master.addActionListener(e -> {
            boolean enabled = master.isSelected();
            for (String id : ids) childChecks.get(id).setEnabled(enabled);
            updateFamilySummary(familyId, ids);
        });
        JScrollPane scroll = new JScrollPane(gridTop);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        body.add(scroll, BorderLayout.CENTER);

        JPanel commands = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        commands.setOpaque(false);
        JButton all = new JButton("Seleziona tutti");
        JButton none = new JButton("Deseleziona tutti");
        all.addActionListener(e -> setChildren(ids, true, familyId));
        none.addActionListener(e -> setChildren(ids, false, familyId));
        commands.add(all);
        commands.add(none);
        body.add(commands, BorderLayout.SOUTH);
        return body;
    }

    private JPanel buildCulometroPage() {
        JPanel page = new JPanel(new BorderLayout(0, 8));
        page.setOpaque(false);
        page.add(pageHeader("Soglie, Fortuna e Culometro",
                "Configura gli indicatori di base e scegli separatamente se generare il Culometro", true),
                BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Soglie e Fortuna",
                familySelectionPanel("thresholdsLuck", "Soglie e Fortuna",
                        ProcessingConfigWriter.THRESHOLDS, THRESHOLD_LABELS));

        JPanel culometroPanel = new JPanel(new BorderLayout(10, 10));
        culometroPanel.setBackground(Color.WHITE);
        culometroPanel.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel explanation = new JLabel("<html>"
                + "<b>Generazione opzionale:</b> il Culometro viene prodotto solo quando la casella è selezionata.<br><br>"
                + "Usa gli indicatori di Soglie/Fortuna e, quando disponibili, soltanto le RU "
                + "di cui il dataset dimostra l'effetto decisivo.<br><br>"
                + "<b>Modalità semplice:</b> preset e slider principali. "
                + "<b>Modalità avanzata:</b> pesi, rarità, affidabilità ed etichette."
                + "</html>");
        culometroPanel.add(explanation, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        culometro.setText("Genera Culometro");
        actions.add(culometro);
        JButton configure = new JButton("Configura preset, slider e pesi");
        configure.addActionListener(e -> openCulometroSettings());
        actions.add(configure);
        culometroPanel.add(actions, BorderLayout.SOUTH);
        tabs.addTab("Generazione Culometro", culometroPanel);

        JPanel publication = new JPanel(new BorderLayout(8, 8));
        publication.setBackground(Color.WHITE);
        publication.setBorder(new EmptyBorder(12, 14, 12, 14));
        publication.add(new JLabel("<html><b>HTML Culometro:</b><br>"
                + "• modello senza dati: copiabile nella cartella della skin FCM;<br>"
                + "• pagina con dati incorporati: pubblicabile direttamente nel sito generato;<br>"
                + "• dati JavaScript: destinazione <code>js</code> del sito.<br><br>"
                + "Questi comandi saranno abilitati quando verrà definito il pacchetto HTML definitivo."
                + "</html>"), BorderLayout.CENTER);
        tabs.addTab("HTML e pubblicazione", publication);

        page.add(tabs, BorderLayout.CENTER);

        JButton save = new JButton("Salva Soglie e Culometro");
        save.addActionListener(e -> saveState());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setOpaque(false);
        bottom.add(save);
        page.add(bottom, BorderLayout.SOUTH);
        return page;
    }

    private JPanel buildProjectPage() {
        JPanel page = verticalPage();
        page.add(pageHeader("Progetto e motore",
                "Percorsi operativi, configurazioni e stato della pipeline", true));
        JPanel card = cardPanel(new GridLayout(0, 1, 6, 6));
        card.add(infoLine("Cartella progetto", root.toString()));
        card.add(infoLine("Configurazione elaborazione", processingFile.toString()));
        card.add(infoLine("Configurazione Culometro", culometroFile.toString()));
        card.add(infoLine("Database predefinito", root.resolve("data/database/recordsnext.db").toString()));
        card.add(infoLine("Configurazione GUI", Files.isRegularFile(propertiesFile)
                ? propertiesFile.toString() : "Default PipelineConfig"));
        card.add(infoLine("Backend", "Modulare 2.0 attivo"));
        card.add(infoLine("Modalità selezionata", consolidated.isSelected() ? "Consolidata" : "Completa"));
        card.add(infoLine("Famiglie attive", String.valueOf(activeFamilyCount())));
        card.add(infoLine("Sottorecord attivi", String.valueOf(activeChildCount())));
        card.add(infoLine("Culometro", culometro.isSelected() ? "Attivo" : "Disattivo"));
        card.add(infoLine("Fase corrente", phase.getText()));
        page.add(card);
        return page;
    }

    private JPanel buildPublishPage() {
        JPanel page = verticalPage();
        page.add(pageHeader("Debug e strumenti",
                "Diagnostica della pubblicazione e utilità per la skin FCM", true));

        JPanel publishCard = cardPanel(new BorderLayout(8, 8));
        JLabel text = new JLabel("<html>Gli output vengono prima validati nello staging. La pubblicazione nel sito "
                + "avviene solo quando l'opzione è attiva e usa il rollback del publisher.</html>");
        publishCard.add(text, BorderLayout.CENTER);
        JPanel options = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        options.setOpaque(false);
        options.add(publish);
        publishCard.add(options, BorderLayout.SOUTH);
        page.add(publishCard);
        page.add(Box.createVerticalStrut(10));

        JPanel examples = cardPanel(new BorderLayout(10, 10));
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Installa esempi nella skin");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(NAVY);
        JLabel subtitle = new JLabel("Seleziona la cartella della skin FCM. Nessun file viene copiato in questa versione.");
        subtitle.setForeground(MUTED);
        heading.add(title);
        heading.add(Box.createVerticalStrut(2));
        heading.add(subtitle);
        examples.add(heading, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 0, 3, 8);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;
        c.gridx = 0;
        c.gridy = 0;
        body.add(new JLabel("Cartella skin:"), c);

        exampleSiteDirectory.setEditable(false);
        c.gridx = 1;
        c.weightx = 1;
        body.add(exampleSiteDirectory, c);

        JButton browse = new JButton("Sfoglia...");
        browse.addActionListener(e -> chooseExampleSiteDirectory());
        c.gridx = 2;
        c.weightx = 0;
        body.add(browse, c);

        c.gridx = 0;
        c.gridy++;
        body.add(new JLabel("HTML indice:"), c);
        c.gridx = 1;
        c.gridwidth = 2;
        body.add(exampleRootTarget, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        body.add(new JLabel("Viste e asset:"), c);
        c.gridx = 1;
        c.gridwidth = 2;
        body.add(exampleViewsTarget, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        body.add(new JLabel("JavaScript:"), c);
        c.gridx = 1;
        c.gridwidth = 2;
        body.add(exampleJsTarget, c);

        examples.add(body, BorderLayout.CENTER);

        JButton preview = new JButton("Anteprima destinazioni");
        preview.setEnabled(false);
        installExamples.setEnabled(false);
        installExamples.setToolTipText("Disponibile quando sarà definito il pacchetto esempi definitivo");
        JPanel exampleActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        exampleActions.setOpaque(false);
        exampleActions.add(preview);
        exampleActions.add(installExamples);
        examples.add(exampleActions, BorderLayout.SOUTH);

        page.add(examples);
        return page;
    }

    private void chooseExampleSiteDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleziona la cartella della skin FCM");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        String current = exampleSiteDirectory.getText().trim();
        if (!current.isEmpty()) {
            Path currentPath = Path.of(current);
            if (Files.isDirectory(currentPath)) chooser.setCurrentDirectory(currentPath.toFile());
        }

        if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return;

        Path siteRoot = chooser.getSelectedFile().toPath().toAbsolutePath().normalize();
        exampleSiteDirectory.setText(siteRoot.toString());
        exampleRootTarget.setText(siteRoot.toString());
        exampleViewsTarget.setText(siteRoot.resolve("RecordsNext").toString());
        exampleJsTarget.setText(siteRoot.resolve("js").toString());
        status.setText("Destinazioni skin rilevate; installazione esempi non ancora attiva");
        status.setForeground(new Color(145, 91, 18));
    }

    private JPanel buildLogPage() {
        JPanel page = new JPanel(new BorderLayout(10, 10));
        page.setOpaque(false);
        page.add(pageHeader("Log e diagnostica", "Messaggi della pipeline e tempi di elaborazione", true), BorderLayout.NORTH);
        log.setEditable(false);
        log.setFont(new Font("Consolas", Font.PLAIN, 12));
        log.setLineWrap(false);
        ((DefaultCaret) log.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        page.add(new JScrollPane(log), BorderLayout.CENTER);
        JButton clear = new JButton("Pulisci log");
        clear.addActionListener(e -> log.setText(""));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(clear);
        page.add(bottom, BorderLayout.SOUTH);
        return page;
    }

    private JPanel buildCompactRunBar() {
        JPanel bar = cardPanel(new BorderLayout(10, 0));
        bar.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER), new EmptyBorder(7, 9, 7, 9)));
        JPanel options = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        options.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        group.add(full);
        group.add(consolidated);
        options.add(new JLabel("Modalità:"));
        options.add(consolidated);
        options.add(full);
        bar.add(options, BorderLayout.CENTER);

        JButton save = new JButton("Salva");
        save.addActionListener(e -> saveState());
        JButton terminate = new JButton("Termina");
        terminate.addActionListener(e -> {
            saveQuietly();
            frame.dispose();
            System.exit(0);
        });
        run.setBackground(BLUE);
        run.setForeground(Color.WHITE);
        run.addActionListener(e -> runPipeline());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(save);
        actions.add(run);
        actions.add(terminate);
        bar.add(actions, BorderLayout.EAST);
        return bar;
    }

    private JPanel pageHeader(String title, String subtitle, boolean back) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel labels = sectionTitle(title, subtitle, SwingConstants.LEFT);
        wrapper.add(labels, BorderLayout.CENTER);
        if (back) {
            JButton button = new JButton("← Dashboard");
            button.addActionListener(e -> showPage("dashboard"));
            JPanel holder = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            holder.setOpaque(false);
            holder.add(button);
            wrapper.add(holder, BorderLayout.EAST);
        }
        return wrapper;
    }

    private JPanel centeredSectionTitle(String title, String subtitle) {
        return sectionTitle(title, subtitle, SwingConstants.CENTER);
    }

    private static JPanel sectionTitle(String titleText, String subtitleText, int alignment) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel title = new JLabel(titleText, alignment);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(NAVY);
        title.setAlignmentX(alignment == SwingConstants.CENTER ? Component.CENTER_ALIGNMENT : Component.LEFT_ALIGNMENT);
        JLabel subtitle = new JLabel(subtitleText, alignment);
        subtitle.setForeground(MUTED);
        subtitle.setAlignmentX(alignment == SwingConstants.CENTER ? Component.CENTER_ALIGNMENT : Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(2));
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(10));
        return panel;
    }

    private JPanel actionCard(String title, String text, String value, Runnable action) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER),
                new EmptyBorder(12, 10, 10, 10)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setPreferredSize(new Dimension(230, 185));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 185));

        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(new Font("Segoe UI", Font.BOLD, 14));
        t.setForeground(NAVY);
        t.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel body = new JLabel(
                "<html><div style='text-align:center;'>" + text.replace("\n", "<br>") + "</div></html>",
                SwingConstants.CENTER
        );
        body.setForeground(MUTED);
        body.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel v = new JLabel(value + "  ›", SwingConstants.CENTER);
        v.setFont(new Font("Segoe UI", Font.BOLD, 12));
        v.setForeground(BLUE);
        v.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(t);
        card.add(Box.createVerticalStrut(14));
        card.add(body);
        card.add(Box.createVerticalGlue());
        card.add(v);

        MouseAdapter click = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { action.run(); }
        };
        card.addMouseListener(click);
        t.addMouseListener(click);
        body.addMouseListener(click);
        v.addMouseListener(click);
        return card;
    }

    private static JPanel verticalPage() {
        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        return page;
    }

    private static JPanel cardPanel(LayoutManager layout) {
        JPanel card = new JPanel(layout);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER), new EmptyBorder(10, 11, 10, 11)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    private static JPanel infoLine(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        JLabel left = new JLabel(label);
        left.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel right = new JLabel(value);
        right.setForeground(MUTED);
        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.CENTER);
        return row;
    }

    private void setChildren(String[] ids, boolean selected, String familyId) {
        for (String id : ids) childChecks.get(id).setSelected(selected);
        updateFamilySummary(familyId, ids);
    }

    private void updateFamilySummary(String familyId, String[] ids) {
        long selected = java.util.Arrays.stream(ids).filter(id -> childChecks.get(id).isSelected()).count();
        familySummaries.get(familyId).setText(selected + " / " + ids.length + " selezionati");
    }

    private void showPage(String id) {
        pages.show(pageHost, id);
        JToggleButton button = navButtons.get(id);
        if (button != null) button.setSelected(true);
    }


    private void refreshDashboard() {
        if (dashboardPage != null) pageHost.remove(dashboardPage);
        dashboardPage = buildDashboardPage();
        pageHost.add(dashboardPage, "dashboard");
        pageHost.revalidate();
        pageHost.repaint();
    }

    private void openSeasonConfiguration() {
        try {
            RecordsNextConfigurationDialog dialog = new RecordsNextConfigurationDialog(frame, root, propertiesFile);
            if (dialog.open()) {
                status.setText("Configurazione stagioni salvata");
                refreshDashboard();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Configurazione stagioni", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openCulometroSettings() {
        try {
            CulometroSettingsDialog dialog = new CulometroSettingsDialog(frame, culometroFile);
            if (dialog.open()) {
                culometro.setSelected(true);
                saveState();
                status.setText("Configurazione Culometro salvata");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Culometro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadState() {
        familyChecks.get("classics").setSelected(state.classics());
        familyChecks.get("series").setSelected(state.series());
        familyChecks.get("ru").setSelected(state.ru());
        familyChecks.get("modifiers").setSelected(state.modifiers());
        familyChecks.get("thresholdsLuck").setSelected(state.thresholdsLuck());
        loadChildren(ProcessingConfigWriter.CLASSICS, "classics");
        loadChildren(ProcessingConfigWriter.SERIES, "series");
        loadChildren(ProcessingConfigWriter.RU, "ru");
        loadChildren(ProcessingConfigWriter.MODIFIERS, "modifiers");
        loadChildren(ProcessingConfigWriter.THRESHOLDS, "thresholdsLuck");
        culometro.setSelected(state.culometro());
        publish.setSelected(state.publishToSite());
        consolidated.setSelected(true);
        full.setSelected(false);
        modifierNameFields.forEach((sourceField, field) ->
                field.setText(state.modifierName(sourceField)));
    }

    private void loadChildren(String[] ids, String familyId) {
        boolean familyEnabled = familyChecks.get(familyId).isSelected();
        for (String id : ids) {
            JCheckBox child = childChecks.get(id);
            child.setSelected(state.childEnabled(id));
            child.setEnabled(familyEnabled);
        }
        updateFamilySummary(familyId, ids);
    }

    private void saveState() {
        try {
            Map<String, Boolean> children = new LinkedHashMap<>();
            childChecks.forEach((id, check) -> children.put(id, check.isSelected()));
            state = new ProcessingConfigWriter.State(
                    familyChecks.get("classics").isSelected(),
                    familyChecks.get("series").isSelected(),
                    familyChecks.get("ru").isSelected(),
                    familyChecks.get("modifiers").isSelected(),
                    familyChecks.get("thresholdsLuck").isSelected(),
                    culometro.isSelected(),
                    publish.isSelected(),
                    children,
                    readModifierNamesFromFields()
            );
            ProcessingConfigWriter.save(processingFile, state);
            status.setText("Configurazione salvata");
            status.setForeground(new Color(35, 105, 62));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Salvataggio", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Map<String, String> readModifierNamesFromFields() {
        Map<String, String> names = new LinkedHashMap<>();
        for (String sourceField : new String[] {"MODM1PERS", "MODM2PERS", "MODM3PERS"}) {
            JTextField field = modifierNameFields.get(sourceField);
            names.put(sourceField, field == null ? "" : field.getText().trim());
        }
        return names;
    }

    private void saveQuietly() {
        try { saveState(); } catch (Exception ignored) { }
    }

    private int activeFamilyCount() {
        return (int) familyChecks.values().stream().filter(AbstractButton::isSelected).count();
    }

    private int activeChildCount() {
        return (int) childChecks.values().stream().filter(AbstractButton::isSelected).count();
    }

    private void runPipeline() {
        saveState();
        run.setEnabled(false);
        log.setText("");
        progress.setValue(0);
        phase.setText("Preparazione");
        status.setText("Elaborazione in corso");
        status.setForeground(BLUE);
        showPage("log");

        new SwingWorker<RecordsNextPipeline.Result, String>() {
            @Override protected RecordsNextPipeline.Result doInBackground() throws Exception {
                PipelineConfig cfg = Files.isRegularFile(propertiesFile)
                        ? PipelineConfig.load(root, propertiesFile)
                        : PipelineConfig.defaults(root);
                ProcessingOptions options = ProcessingConfigLoader.load(processingFile);
                ProcessingMode mode = consolidated.isSelected() ? ProcessingMode.CONSOLIDATED : ProcessingMode.FULL;
                return new RecordsNextPipeline().run(cfg, options, mode, new RecordsNextPipeline.Listener() {
                    @Override public void phase(String text, int percent) {
                        publish(text);
                        SwingUtilities.invokeLater(() -> {
                            phase.setText(text);
                            if (percent >= 0) progress.setValue(percent);
                        });
                    }
                    @Override public void timing(String text) { publish("TEMPO  " + text); }
                });
            }

            @Override protected void process(java.util.List<String> chunks) {
                chunks.forEach(v -> log.append(v + System.lineSeparator()));
            }

            @Override protected void done() {
                try {
                    RecordsNextPipeline.Result result = get();
                    log.append("\nCompletato · file validi=" + result.files()
                            + " · pubblicati=" + result.published() + System.lineSeparator());
                    status.setText("Elaborazione completata");
                    status.setForeground(new Color(35, 105, 62));
                    progress.setValue(100);
                    phase.setText("Elaborazione completata");
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    log.append("ERRORE: " + cause + System.lineSeparator());
                    status.setText("Errore");
                    status.setForeground(RED);
                    JOptionPane.showMessageDialog(frame, String.valueOf(cause), "RecordsNext 2.0", JOptionPane.ERROR_MESSAGE);
                } finally {
                    run.setEnabled(true);
                }
            }
        }.execute();
    }

    private void show() { frame.setVisible(true); }

    private static Map<String, String> labels(Object... values) {
        Map<String, String> result = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) result.put(String.valueOf(values[i]), String.valueOf(values[i + 1]));
        return Map.copyOf(result);
    }

    private static final Map<String, String> CLASSIC_LABELS = labels(
            "classics.highest-match-score", "Maggior punteggio in una partita",
            "classics.lowest-match-score", "Minor punteggio in una partita",
            "classics.most-regulation-goals", "Partita con più gol regolamentari",
            "classics.largest-regulation-margin", "Maggior scarto regolamentare",
            "classics.average-points", "Media punteggio",
            "classics.total-points", "Somma totale punti",
            "classics.standings-points", "Punti classifica",
            "classics.wins", "Vittorie",
            "classics.draws", "Pareggi",
            "classics.losses", "Sconfitte",
            "classics.goals-for", "Gol fatti",
            "classics.goals-against", "Gol subiti",
            "classics.yellow-cards-team", "Ammonizioni per squadra",
            "classics.red-cards-team", "Espulsioni per squadra",
            "classics.red-cards-player", "Espulsioni per giocatore",
            "classics.assists-team", "Assist per squadra",
            "classics.own-goals-team", "Autogol per squadra",
            "classics.penalties-scored", "Rigori segnati",
            "classics.penalties-missed", "Rigori sbagliati",
            "classics.penalties-saved", "Rigori parati",
            "classics.clean-sheets", "Clean sheet"
    );

    private static final Map<String, String> SERIES_LABELS = labels(
            "series.unbeaten", "Serie senza sconfitte",
            "series.winless", "Serie senza vittorie",
            "series.wins", "Vittorie consecutive",
            "series.draws", "Pareggi consecutivi",
            "series.losses", "Sconfitte consecutive",
            "series.clean-sheets", "Clean sheet consecutivi"
    );

    private static final Map<String, String> RU_LABELS = labels(
            "ru.max-in-match", "Maggior numero di RU in una partita",
            "ru.matches-with", "Partite con RU",
            "ru.matches-against", "Partite contro squadre con RU",
            "ru.deciding", "RU decisive",
            "ru.deciding-against", "RU decisive subite",
            "ru.balance", "Bilancio con RU",
            "ru.balance-against", "Bilancio contro RU",
            "ru.average-points", "Media punti con RU",
            "ru.average-points-against", "Media punti contro RU",
            "ru.role-distribution", "Distribuzione PU, DU, CU e AU"
    );

    private static final Map<String, String> MODIFIER_LABELS = labels(
            "modifiers.defence-best-match", "Miglior modificatore difesa in una gara",
            "modifiers.defence-total", "Totale modificatore difesa",
            "modifiers.captain-uses", "Utilizzi Capitano",
            "modifiers.captain-total", "Totale modificatore Capitano",
            "modifiers.home-field-deciding", "Fattore Campo decisivo",
            "modifiers.home-field-points-gained", "Punti guadagnati col Fattore Campo",
            "modifiers.home-field-points-lost", "Punti persi fuori casa",
            "modifiers.home-field-balance", "Saldo Fattore Campo"
    );

    private static final Map<String, String> THRESHOLD_LABELS = labels(
            "thresholds.surgical-win", "Vittoria chirurgica",
            "thresholds.mocking-loss", "Sconfitta beffa",
            "thresholds.miraculous-draw", "Pareggio miracolato",
            "thresholds.narrow-draw", "Pareggio stretto",
            "thresholds.missed-win-half-point", "Vittoria mancata per mezzo punto",
            "thresholds.loss-by-a-whisker", "Sconfitta per un pelo",
            "thresholds.exact-threshold", "Soglia precisa",
            "thresholds.just-enough", "Giusto giusto",
            "thresholds.wasted-points", "Spreco punti",
            "luck.favourable-events", "Eventi favorevoli",
            "luck.unfavourable-events", "Eventi sfavorevoli",
            "luck.balance", "Saldo fortuna-sfortuna"
    );
}
