# RecordsNext 4.0 - CODICE FUNZIONANTE v29

Appendice cumulativa verificabile alla v28, preservata byte per byte.

Bibbia precedente: `docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v28.md` SHA256 `952f0a7434c226a081aae10fcd198afa46e6aa5632171e97a303f84aebb7e194`

## `src/main/java/it/alterlega/recordsnext/gui/RecordsNext2Dashboard.java`

SHA256: `365c241c6bba087016c96bf88fc0bd9b0f5e9b7767dc148afc0b8e3b78a07a4c`

~~~~java
package it.alterlega.recordsnext.gui;

import it.alterlega.recordsnext.ConfigurationSchema;
import it.alterlega.recordsnext.app.PipelineConfig;
import it.alterlega.recordsnext.app.ProcessingMode;
import it.alterlega.recordsnext.app.ProcessingOptions;
import it.alterlega.recordsnext.app.RecordsNextPipeline;
import it.alterlega.recordsnext.app.config.ProcessingConfigLoader;
import it.alterlega.recordsnext.app.config.ProcessingConfigWriter;
import it.alterlega.recordsnext.app.topflop.TopFlopSettings;

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
    private final Path topFlopSettingsFile = root.resolve("config/topflop.json");
    private final Path propertiesFile = root.resolve("config/recordsnext-gui.properties");
    private final Path consolidationStateFile = root.resolve("data/consolidation/recordsnext-consolidation.properties");

    private final JFrame frame = new JFrame("RecordsNext by mauz79 · 4.0 (sviluppo)");
    private final CardLayout pages = new CardLayout();
    private final JPanel pageHost = new JPanel(pages);
    private final Map<String, JToggleButton> navButtons = new LinkedHashMap<>();
    private final Map<String, JCheckBox> familyChecks = new LinkedHashMap<>();
    private final Map<String, JCheckBox> childChecks = new LinkedHashMap<>();
    private final Map<String, JLabel> familySummaries = new LinkedHashMap<>();
    private final Map<String, JTextField> modifierNameFields = new LinkedHashMap<>();
    private final Map<String, JSpinner> topFlopThresholdFields = new LinkedHashMap<>();

    private final JCheckBox culometro = new JCheckBox("Genera Culometro");
    private final JCheckBox publish = new JCheckBox("Pubblica nel sito della stagione corrente al termine");
    private final JRadioButton full = new JRadioButton("Completa");
    private final JRadioButton consolidated = new JRadioButton("Consolidata");
    private final JTextArea log = new JTextArea();
    private final JProgressBar progress = new JProgressBar(0, 100);
    private final JLabel status = new JLabel("Pronto");
    private final JLabel phase = new JLabel("Nessuna elaborazione in corso");
    private final JButton run = new JButton("Elabora");
    private final JButton publishAllSites = new JButton("Pubblica i siti delle stagioni selezionate");
    private final JTextField exampleSiteDirectory = new JTextField();
    private final JLabel exampleRootTarget = new JLabel("Non selezionata");
    private final JLabel exampleViewsTarget = new JLabel("Non selezionata");
    private final JLabel exampleJsTarget = new JLabel("Non selezionata");
    private final JButton installExamples = new JButton("Installa esempi");
    private final JComboBox<String> htmlProfile = new JComboBox<>(new String[] {"mauzstrom", "maelstrom", "fantablue2", "neutral"});
    private ProcessingConfigWriter.State state;
    private JPanel dashboardPage;

    private static final Map<String, String> TOP_FLOP_LABELS = Map.ofEntries(
        Map.entry("topFlop.tf01", "TF01 - Miglior voto reale"),
        Map.entry("topFlop.tf02", "TF02 - Peggior voto reale"),
        Map.entry("topFlop.tf03", "TF03 - Miglior fantavoto apportato"),
        Map.entry("topFlop.tf04", "TF04 - Peggior fantavoto apportato"),
        Map.entry("topFlop.tf05", "TF05 - Medie voto (soglia configurabile)"),
        Map.entry("topFlop.tf06", "TF06 - Fantamedie (soglia configurabile)"),
        Map.entry("topFlop.tf07", "TF07 - Gol reali e apportati"),
        Map.entry("topFlop.tf08", "TF08 - Assist reali e apportati"),
        Map.entry("topFlop.tf09", "TF09 - Malus"),
        Map.entry("topFlop.tf10", "TF10 - Presenze reali e fantasy"),
        Map.entry("topFlop.tf11", "TF11 - Voti sprecati (da verificare)"),
        Map.entry("topFlop.tf12", "TF12 - Errori pesati (da verificare)"),
        Map.entry("topFlop.tf13", "TF13 - Fantavoti sprecati (da verificare)"),
        Map.entry("topFlop.tf14", "TF14 - Stagione schillaciana (da definire)"),
        Map.entry("topFlop.tf15", "TF15 - Stagione sbagliata (da definire)"),
        Map.entry("topFlop.tf16", "TF16 - Rendimento per fantasquadra"),
        Map.entry("topFlop.tf17", "TF17 - Rigori parati"),
        Map.entry("topFlop.tf18", "TF18 - Gol subiti"),
        Map.entry("topFlop.tf19", "TF19 - Giornate con zero gol subiti"),
        Map.entry("topFlop.tf20", "TF20 - Portieri goleador e assist")
    );

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new RecordsNext2Dashboard().show();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, ex.toString(), "RecordsNext 4.0 (sviluppo)", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private RecordsNext2Dashboard() throws Exception {
        bootstrap();
        state = ProcessingConfigWriter.load(processingFile);
        applyLookAndFeel();
        build();
        loadState();
        refreshProcessingModeAvailability();
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
        JLabel version = new JLabel("by mauz79 · 4.0 dev");
        version.setAlignmentX(Component.LEFT_ALIGNMENT);
        version.setForeground(new Color(174, 192, 224));
        version.setFont(new Font("Segoe UI", Font.BOLD, 11));
        side.add(version);
        side.add(Box.createVerticalStrut(28));

        ButtonGroup group = new ButtonGroup();
        addNav(side, group, "dashboard", "Dashboard");
        addNav(side, group, "seasons", "Configurazione stagioni");
        addNav(side, group, "topflop", "Top & Flop");
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
        pageHost.add(buildTopFlopPage(), "topflop");
        pageHost.add(buildProjectPage(), "project");
        pageHost.add(buildPublishPage(), "tools");
        pageHost.add(buildLogPage(), "log");
        main.add(pageHost, BorderLayout.CENTER);
        main.add(buildCompactRunBar(), BorderLayout.SOUTH);
        navButtons.get("dashboard").setSelected(true);
        return main;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new GridBagLayout());
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 42));

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;

        JPanel leftSpacer = new JPanel();
        leftSpacer.setOpaque(false);
        leftSpacer.setPreferredSize(new Dimension(180, 1));
        c.gridx = 0;
        c.weightx = 0.0;
        header.add(leftSpacer, c);

        JLabel title = new JLabel("RecordsNext 4.0 (sviluppo)", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI Black", Font.BOLD, 25));
        title.setForeground(RED);
        c.gridx = 1;
        c.weightx = 1.0;
        header.add(title, c);

        status.setFont(new Font("Segoe UI", Font.BOLD, 12));
        status.setForeground(new Color(35, 105, 62));
        status.setHorizontalAlignment(SwingConstants.RIGHT);
        status.setPreferredSize(new Dimension(180, 24));
        c.gridx = 2;
        c.weightx = 0.0;
        header.add(status, c);

        return header;
    }

    private JPanel buildDashboardPage() {
        JPanel page = new JPanel(new BorderLayout(0, 10));
        page.setOpaque(false);
        page.add(centeredSectionTitle(
                "Dashboard",
                "Configura RecordsNext e avvia l’elaborazione dalla barra inferiore"
        ), BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(2, 2, 12, 12));
        cards.setOpaque(false);
        cards.setPreferredSize(new Dimension(0, 352));
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 352));
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
                "Top & Flop",
                "Record e portieri\nMedie e soglie configurabili\nCarriere e fantasquadre",
                state.topFlop() ? "Apri Top & Flop" : "Configura Top & Flop",
                () -> showPage("topflop")
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
        if (state.topFlop()) names.add("Top & Flop");
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
        if (state.topFlop()) count++;
        return count;
    }

    private JPanel buildSeasonsPage() {
        JPanel page = verticalPage();
        page.add(pageHeader("Configurazione stagioni",
                "Stagioni, sorgenti, siti e collegamenti storici", true));
        JPanel card = cardPanel(new BorderLayout(10, 10));
        JLabel text = new JLabel("<html><b>Gestita:</b> FCM e FCA obbligatori; sito locale e online opzionali.<br>"
                + "<b>Calendario:</b> gestito automaticamente dall'archivio interno RecordsNext.<br>"
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

    private JPanel buildTopFlopPage() {
        JPanel page = new JPanel(new BorderLayout(0, 8));
        page.setOpaque(false);
        page.add(pageHeader("Records Top & Flop",
                "Record, soglie e modalita' di elaborazione della famiglia", true),BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        JPanel records = familySelectionPanel("topFlop", "Top & Flop",
                ProcessingConfigWriter.TOP_FLOP, TOP_FLOP_LABELS);
        JLabel info = new JLabel("<html>Le classifiche senza prova del possesso o identita' storica "
                + "restano disabilitate. TF05 e TF06 usano le soglie della seconda scheda.</html>");
        info.setForeground(MUTED);
        info.setBorder(new EmptyBorder(6, 12, 6, 12));
        records.add(info, BorderLayout.NORTH);
        for (String id : ProcessingConfigWriter.TOP_FLOP) {
            if (!ProcessingConfigWriter.topFlopImplemented(id)) {
                JCheckBox check = childChecks.get(id);
                check.setSelected(false);
                check.setEnabled(false);
                check.setToolTipText("Non ancora attivo: occorrono verifiche di possesso e/o baseline storica");
            }
        }
        tabs.addTab("Selezione record", records);
        tabs.addTab("Soglie e criteri", buildTopFlopSettingsPage());
        JPanel buttons=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton identities=new JButton("Riconcilia identita' storiche dei calciatori");
        identities.addActionListener(e->openPlayerIdentityConfiguration());
        buttons.add(identities);
        buttons.add(new JLabel("Proposte da approvare nella GUI; la 3.2 resta invariata."));
        page.add(buttons,BorderLayout.SOUTH);
        page.add(tabs,BorderLayout.CENTER);
        return page;
    }

    private JComponent buildTopFlopSettingsPage() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(14,18,14,18));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor=GridBagConstraints.WEST;
        c.fill=GridBagConstraints.HORIZONTAL;
        c.insets=new Insets(6,5,6,12);
        c.gridy=0;
        addTopFlopSettingsHeading(content,c,"Medie abilitate: TF05 e TF06");
        addTopFlopIntSetting(content,c,"realSeasonVotes","Voti reali minimi per stagione",5,1,1000);
        addTopFlopIntSetting(content,c,"fantasyLeagueMatches","Gare conteggiate minime - campionato",5,1,1000);
        addTopFlopIntSetting(content,c,"fantasyCupMatches","Gare conteggiate minime - coppe",3,1,1000);
        addTopFlopSettingsHeading(content,c,"Soglie conservate per funzioni non ancora abilitate");
        addTopFlopIntSetting(content,c,"realCareerVotes","Voti minimi per carriera verificata",20,1,10000);
        addTopFlopIntSetting(content,c,"fantasyCareerMatches","Gare minime per carriera fantasy",20,1,10000);
        addTopFlopIntSetting(content,c,"verifiedExclusions","Esclusioni certe minime (TF11-13)",3,1,1000);
        addTopFlopIntSetting(content,c,"exclusionsWithVote","Esclusioni con voto minime (TF11-13)",2,1,1000);
        addTopFlopIntSetting(content,c,"anomalySeasonVotes","Voti minimi stagione anomala (TF14-15)",8,1,1000);
        addTopFlopIntSetting(content,c,"anomalyBaselineVotes","Voti minimi altre stagioni (TF14-15)",20,1,10000);
        addTopFlopIntSetting(content,c,"anomalyBaselineSeasons","Altre stagioni minime (TF14-15)",2,1,100);
        addTopFlopDoubleSetting(content,c,"anomalyDelta","Scostamento minimo media voto (TF14-15)",0.75);
        c.gridx=0;c.gridwidth=2;c.weightx=1.0;
        JLabel note = new JLabel("<html>Le soglie sono salvate in <b>config/topflop.json</b>. "
          + "TF05/TF06 le applicano nei JS; TF11-15 e la carriera trasversale "
          + "restano inattive finche' non saranno validate le rispettive fonti.</html>");
        note.setForeground(MUTED);
        content.add(note,c);
        c.gridy++;
        JButton save=new JButton("Salva parametri Top & Flop");
        save.addActionListener(e->saveState());
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT));actions.setOpaque(false);
        actions.add(save);
        content.add(actions,c);
        JScrollPane scroll=new JScrollPane(content);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private void addTopFlopSettingsHeading(JPanel panel, GridBagConstraints c,String text) {
        c.gridx=0;c.gridwidth=2;c.weightx=1.0;
        JLabel title = new JLabel(text);
        title.setFont(new Font("Segoe UI",Font.BOLD,14));
        title.setForeground(NAVY);
        panel.add(title,c);c.gridy++;
        c.gridwidth=1;
    }
    private void addTopFlopIntSetting(JPanel panel,GridBagConstraints c,String key,
                                     String label,int value,int min,int max) {
        c.gridx=0;c.weightx=1.0;
        panel.add(new JLabel(label),c);
        JSpinner spinner=new JSpinner(new SpinnerNumberModel(value,min,max,1));
        spinner.setPreferredSize(new Dimension(110,27));
        topFlopThresholdFields.put(key,spinner);
        c.gridx=1;c.weightx=0;
        panel.add(spinner,c);c.gridy++;
    }
    private void addTopFlopDoubleSetting(JPanel panel,GridBagConstraints c,String key,
                                        String label,double value) {
        c.gridx=0;c.weightx=1.0;
        panel.add(new JLabel(label),c);
        JSpinner spinner=new JSpinner(new SpinnerNumberModel(value,0.05,10.0,0.05));
        topFlopThresholdFields.put(key,spinner);
        c.gridx=1;c.weightx=0;
        panel.add(spinner,c);c.gridy++;
    }

    private void openPlayerIdentityConfiguration() {
        try {
            new TopFlopIdentityReviewDialog(frame,root).setVisible(true);
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(frame,ex.getMessage(),
                "Configurazione identita' calciatori",JOptionPane.ERROR_MESSAGE);
        }
    }

    private TopFlopSettings readTopFlopSettings() {
        return new TopFlopSettings(
            topFlopInt("realSeasonVotes"),topFlopInt("realCareerVotes"),
            topFlopInt("fantasyLeagueMatches"),topFlopInt("fantasyCupMatches"),
            topFlopInt("fantasyCareerMatches"),topFlopInt("verifiedExclusions"),
            topFlopInt("exclusionsWithVote"),topFlopInt("anomalySeasonVotes"),
            topFlopInt("anomalyBaselineVotes"),topFlopInt("anomalyBaselineSeasons"),
            java.math.BigDecimal.valueOf(((Number)topFlopThresholdFields.get("anomalyDelta").getValue()).doubleValue()));
    }
    private int topFlopInt(String name) {
        return ((Number)topFlopThresholdFields.get(name).getValue()).intValue();
    }
    private void populateTopFlopSettings(TopFlopSettings x) {
        Map<String,Object> values=Map.ofEntries(
            Map.entry("realSeasonVotes",x.realSeasonVotes()),
            Map.entry("realCareerVotes",x.realCareerVotes()),
            Map.entry("fantasyLeagueMatches",x.fantasyLeagueMatches()),
            Map.entry("fantasyCupMatches",x.fantasyCupMatches()),
            Map.entry("fantasyCareerMatches",x.fantasyCareerMatches()),
            Map.entry("verifiedExclusions",x.verifiedExclusions()),
            Map.entry("exclusionsWithVote",x.exclusionsWithVote()),
            Map.entry("anomalySeasonVotes",x.anomalySeasonVotes()),
            Map.entry("anomalyBaselineVotes",x.anomalyBaselineVotes()),
            Map.entry("anomalyBaselineSeasons",x.anomalyBaselineSeasons()),
            Map.entry("anomalyDelta",x.anomalyDelta().doubleValue()));
        values.forEach((key,val)->topFlopThresholdFields.get(key).setValue(val));
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
            for (String id : ids) childChecks.get(id).setEnabled(enabled &&
                    (!familyId.equals("topFlop") || ProcessingConfigWriter.topFlopImplemented(id)));
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
        card.add(infoLine("Configurazione Top & Flop", topFlopSettingsFile.toString()));
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
        JLabel text = new JLabel("<html>L'elaborazione normale pubblica, se richiesto, solo nel sito "
                + "della stagione corrente. La pubblicazione di tutti i siti storici è un'azione separata: "
                + "ogni sito riceve esclusivamente lo storico disponibile fino alla propria stagione.</html>");
        publishCard.add(text, BorderLayout.CENTER);
        JPanel options = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        options.setOpaque(false);
        options.add(publish);
        publishAllSites.setToolTipText(
                "Rigenera e pubblica RecordsNext nei siti configurati delle stagioni selezionate"
        );
        publishAllSites.addActionListener(e -> runPipeline(
                RecordsNextPipeline.PublicationMode.ALL_CONFIGURED_SITES,
                true
        ));
        options.add(publishAllSites);
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
        JLabel subtitle = new JLabel("Seleziona la cartella della skin FCM e il profilo grafico da installare.");
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
        c.gridwidth = 1;
        c.weightx = 0;
        body.add(new JLabel("Profilo HTML:"), c);

        htmlProfile.setSelectedItem("mauzstrom");
        htmlProfile.setPreferredSize(new Dimension(180, 26));
        c.gridx = 1;
        c.gridwidth = 2;
        c.weightx = 1;
        body.add(htmlProfile, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        c.weightx = 0;
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
        installExamples.setToolTipText("Installa gli HTML RecordsNext e il profilo grafico selezionato");
        installExamples.addActionListener(e -> installExampleViewers());
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
        installExamples.setEnabled(true);
        status.setText("Destinazioni skin rilevate");
        status.setForeground(new Color(35, 105, 62));
    }

    private Path resolveViewerPackageDirectory() {
        Path releasePackage = root.resolve("visualizzatori");
        if (Files.isDirectory(releasePackage)) return releasePackage;

        Path developmentPackage = root.resolve("release/visualizzatori");
        if (Files.isDirectory(developmentPackage)) return developmentPackage;

        throw new IllegalStateException("Pacchetto visualizzatori non trovato.");
    }

    private void installExampleViewers() {
        String selectedSite = exampleSiteDirectory.getText().trim();
        if (selectedSite.isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                    "Selezionare prima la cartella della skin FCM.",
                    "Installazione visualizzatori",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Path siteRoot = Path.of(selectedSite).toAbsolutePath().normalize();
            if (!Files.isDirectory(siteRoot)) {
                throw new IllegalStateException("Cartella skin non trovata: " + siteRoot);
            }

            Path packageDir = resolveViewerPackageDirectory();
            String profile = String.valueOf(htmlProfile.getSelectedItem());
            Path profileCss = packageDir.resolve("profiles").resolve(profile).resolve("recordsnext.css");

            if (!Files.isRegularFile(profileCss)) {
                throw new IllegalStateException("Profilo CSS non trovato: " + profileCss);
            }

            Path siteJs = siteRoot.resolve("js");
            Path siteViews = siteRoot.resolve("RecordsNext");
            Files.createDirectories(siteJs);
            Files.createDirectories(siteViews);

            Files.copy(packageDir.resolve("recordsnext.html"),
                    siteRoot.resolve("recordsnext.html"),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            try (java.util.stream.Stream<Path> stream = Files.list(packageDir.resolve("RecordsNext"))) {
                for (Path source : stream.filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().toLowerCase(java.util.Locale.ROOT).endsWith(".html"))
                        .toList()) {
                    Files.copy(source,
                            siteViews.resolve(source.getFileName()),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            }

            Files.copy(packageDir.resolve("js/fcmRecordsNextFunzioni_common.js"),
                    siteJs.resolve("fcmRecordsNextFunzioni_common.js"),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            Files.copy(packageDir.resolve("js/fcmRecordsNextFunzioni_viewer.js"),
                    siteJs.resolve("fcmRecordsNextFunzioni_viewer.js"),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            Files.copy(profileCss,
                    siteViews.resolve("recordsnext.css"),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            status.setText("Visualizzatori installati · profilo " + profile);
            status.setForeground(new Color(35, 105, 62));

            JOptionPane.showMessageDialog(frame,
                    "Visualizzatori RecordsNext installati.\n\n"
                            + "Profilo: " + profile + "\n"
                            + "Indice: " + siteRoot.resolve("recordsnext.html") + "\n"
                            + "Viste: " + siteViews + "\n"
                            + "JavaScript: " + siteJs,
                    "Installazione visualizzatori",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            status.setText("Errore installazione visualizzatori");
            status.setForeground(RED);
            JOptionPane.showMessageDialog(frame,
                    ex.getMessage(),
                    "Installazione visualizzatori",
                    JOptionPane.ERROR_MESSAGE);
        }
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
        run.addActionListener(e -> runPipeline(RecordsNextPipeline.PublicationMode.CURRENT_SITE, false));
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
        wrapper.setPreferredSize(new Dimension(0, 72));

        JPanel leftSpacer = new JPanel();
        leftSpacer.setOpaque(false);
        leftSpacer.setPreferredSize(new Dimension(150, 36));
        wrapper.add(leftSpacer, BorderLayout.WEST);

        JPanel labels = sectionTitle(title, subtitle, SwingConstants.CENTER);
        wrapper.add(labels, BorderLayout.CENTER);

        JPanel holder = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        holder.setOpaque(false);
        holder.setPreferredSize(new Dimension(150, 36));

        if (back) {
            JButton button = new JButton("← Dashboard");
            button.addActionListener(e -> showPage("dashboard"));
            holder.add(button);
        }

        wrapper.add(holder, BorderLayout.EAST);
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
        for (String id : ids) childChecks.get(id).setSelected(selected &&
                (!familyId.equals("topFlop") || ProcessingConfigWriter.topFlopImplemented(id)));
        updateFamilySummary(familyId, ids);
    }

    private void updateFamilySummary(String familyId, String[] ids) {
        long selected = java.util.Arrays.stream(ids).filter(id -> childChecks.get(id).isSelected()).count();
        familySummaries.get(familyId).setText(selected + " / " + ids.length + " selezionati");
    }

    private boolean playerIdentityAlertChecked = false;

    private void showPage(String id) {
        pages.show(pageHost, id);
        JToggleButton button = navButtons.get(id);
        if (button != null) button.setSelected(true);
        if ("topflop".equals(id) && !playerIdentityAlertChecked) {
            playerIdentityAlertChecked=true;
            new SwingWorker<Boolean,Void>() {
                @Override protected Boolean doInBackground() throws Exception {
                    Path file=root.resolve("config/player-identities.json");
                    Path db=root.resolve("data/database/recordsnext.db");
                    if(!Files.isRegularFile(file))return true; // First use: require explicit approval.
                    if(!Files.isRegularFile(db))return false;
                    try(java.sql.Connection connection=java.sql.DriverManager.getConnection(
                        "jdbc:sqlite:"+db.toUri()+"?mode=ro")) {
                        var audit=it.alterlega.recordsnext.app.topflop.TopFlopPlayerIdentities.scanAvailable(connection);
                        return it.alterlega.recordsnext.app.topflop.TopFlopPlayerIdentities
                            .load(file).requiresReview(audit);
                    }
                }
                @Override protected void done() {
                    try {
                        if(Boolean.TRUE.equals(get())) {
                            int choice=JOptionPane.showConfirmDialog(frame,
                                "Ci sono identita' storiche da verificare. Le proposte non sono mai applicate automaticamente. " +
                                "Aprire la configurazione? (Richiede le annate FCA gia' importate)",
                                "Top & Flop - identita' calciatori",JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE);
                            if(choice==JOptionPane.YES_OPTION)openPlayerIdentityConfiguration();
                        }
                    } catch(Exception ex) {
                        JOptionPane.showMessageDialog(frame,ex.getMessage(),
                            "Avviso identita' storiche",JOptionPane.WARNING_MESSAGE);
                    }
                }
            }.execute();
        }
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
        familyChecks.get("topFlop").setSelected(state.topFlop());
        loadChildren(ProcessingConfigWriter.CLASSICS, "classics");
        loadChildren(ProcessingConfigWriter.SERIES, "series");
        loadChildren(ProcessingConfigWriter.RU, "ru");
        loadChildren(ProcessingConfigWriter.MODIFIERS, "modifiers");
        loadChildren(ProcessingConfigWriter.THRESHOLDS, "thresholdsLuck");
        loadChildren(ProcessingConfigWriter.TOP_FLOP, "topFlop");
        culometro.setSelected(state.culometro());
        publish.setSelected(state.publishToSite());
        try {populateTopFlopSettings(TopFlopSettings.load(topFlopSettingsFile));}
        catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,"Parametri Top & Flop: " + ex.getMessage(),
                    "Configurazione",JOptionPane.ERROR_MESSAGE);
        }
        // La disponibilita della modalita Consolidata viene aggiornata
        // da refreshProcessingModeAvailability().
        modifierNameFields.forEach((sourceField, field) ->
                field.setText(state.modifierName(sourceField)));
    }

    private void loadChildren(String[] ids, String familyId) {
        boolean familyEnabled = familyChecks.get(familyId).isSelected();
        for (String id : ids) {
            JCheckBox child = childChecks.get(id);
            child.setSelected(state.childEnabled(id));
            child.setEnabled(familyEnabled &&
                    (!familyId.equals("topFlop") || ProcessingConfigWriter.topFlopImplemented(id)));
        }
        updateFamilySummary(familyId, ids);
    }


    private void refreshProcessingModeAvailability() {
        boolean available = Files.isRegularFile(consolidationStateFile);
        consolidated.setEnabled(available);
        consolidated.setToolTipText(available
                ? "Usa il consolidamento esistente e aggiorna la stagione corrente."
                : "Disponibile dopo una prima elaborazione Completa riuscita.");
        if (!available) {
            consolidated.setSelected(false);
            full.setSelected(true);
        } else if (!consolidated.isSelected() && !full.isSelected()) {
            consolidated.setSelected(true);
        }
    }

    private boolean saveState() {
        try {
            for (JSpinner spinner:topFlopThresholdFields.values()) spinner.commitEdit();
            Map<String, Boolean> children = new LinkedHashMap<>();
            childChecks.forEach((id, check) -> children.put(id, check.isSelected()));
            state = new ProcessingConfigWriter.State(
                    familyChecks.get("classics").isSelected(),
                    familyChecks.get("series").isSelected(),
                    familyChecks.get("ru").isSelected(),
                    familyChecks.get("modifiers").isSelected(),
                    familyChecks.get("thresholdsLuck").isSelected(),
                    familyChecks.get("topFlop").isSelected(),
                    culometro.isSelected(),
                    publish.isSelected(),
                    children,
                    readModifierNamesFromFields()
            );
            TopFlopSettings configured = readTopFlopSettings();
            configured.save(topFlopSettingsFile);
            ProcessingConfigWriter.save(processingFile, state);
            refreshDashboard();
            status.setText("Configurazione salvata");
            status.setForeground(new Color(35, 105, 62));
            return true;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Salvataggio", JOptionPane.ERROR_MESSAGE);
            return false;
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

    private void runPipeline(
            RecordsNextPipeline.PublicationMode publicationMode,
            boolean forcePublish) {
        if (forcePublish && !publish.isSelected()) {
            publish.setSelected(true);
        }
        if (!saveState()) return;
        run.setEnabled(false);
        publishAllSites.setEnabled(false);
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
                return new RecordsNextPipeline().run(
                        cfg,
                        options,
                        mode,
                        new RecordsNextPipeline.Listener() {
                    @Override public void phase(String text, int percent) {
                        publish(text);
                        SwingUtilities.invokeLater(() -> {
                            phase.setText(text);
                            if (percent >= 0) progress.setValue(percent);
                        });
                    }
                    @Override public void timing(String text) { publish("TEMPO  " + text); }
                },
                        publicationMode
                );
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
                    refreshProcessingModeAvailability();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    log.append("ERRORE: " + cause + System.lineSeparator());
                    status.setText("Errore");
                    status.setForeground(RED);
                    JOptionPane.showMessageDialog(frame, String.valueOf(cause), "RecordsNext 4.0 (sviluppo)", JOptionPane.ERROR_MESSAGE);
                } finally {
                    run.setEnabled(true);
                                publishAllSites.setEnabled(true);
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
~~~~

## `src/main/java/it/alterlega/recordsnext/gui/TopFlopIdentityReviewDialog.java`

SHA256: `46e08f05ebc79f9cc72a6fe7390646831976f7b077538ff4a9ed330dc76cbfa5`

~~~~java
package it.alterlega.recordsnext.gui;

import it.alterlega.recordsnext.app.topflop.TopFlopPlayerIdentities;
import it.alterlega.recordsnext.app.topflop.TopFlopPlayerIdentities.*;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Human-approved, persistent identity decisions. No scanning result is auto-saved. */
public final class TopFlopIdentityReviewDialog extends JDialog {
    private record Row(Proposal proposal,Collision collision) {
        String kind(){return collision!=null?"CODICE AMBIGUO":(proposal.safe()?"PROPOSTA ESATTA":"DA VERIFICARE");}
        String detail(){return collision!=null?collision.description():proposal.description();}
    }
    private final Registry registry;
    private final Audit audit;
    private final Path file;
    private final List<Row> rows=new ArrayList<>();
    private final JLabel status=new JLabel();
    private final JTable table;
    public TopFlopIdentityReviewDialog(Frame owner,Path projectRoot) throws Exception {
        super(owner,"Identita' storiche calciatori - approvazione",true);
        this.file=projectRoot.resolve("config").resolve(TopFlopPlayerIdentities.FILE_NAME);
        this.registry=TopFlopPlayerIdentities.load(file);
        Path db=projectRoot.resolve("data/database/recordsnext.db");
        if(!Files.isRegularFile(db))throw new IllegalStateException("SQLite non presente: " + db);
        Class.forName("org.sqlite.JDBC");
        try(Connection c=DriverManager.getConnection("jdbc:sqlite:"+db.toUri()+"?mode=ro")) {
            audit=TopFlopPlayerIdentities.scanAvailable(c);
            if(audit.sources()==0)throw new IllegalStateException(
                "Archivio FCA non ancora importato. Eseguire prima l'elaborazione completa, poi riconciliare.");
        }
        audit.collisions().forEach(c->rows.add(new Row(null,c)));
        audit.proposals().forEach(p->rows.add(new Row(p,null)));
        var model=new AbstractTableModel() {
            public int getRowCount(){return rows.size();}
            public int getColumnCount(){return 4;}
            public String getColumnName(int col){return switch(col){case 0->"Tipo";case 1->"Stato";case 2->"Occorrenze";default->"Stagioni / nomi / codici";};}
            public Object getValueAt(int r,int c){Row row=rows.get(r);return switch(c){
                case 0->row.kind();
                case 1->row.collision()!=null?(registry.isDistinct(row.collision())?"DISTINTE APPROVATE":
                        registry.isMerged(row.collision())?"UNIONE APPROVATA":"DA DECIDERE"):
                    registry.complete(row.proposal())?"APPROVATO":registry.isIgnored(row.proposal())?"RINVIATO":"DA DECIDERE";
                case 2->row.collision()!=null?row.collision().members().size():row.proposal().members().size();
                default->row.detail();
            };}
        };
        setLayout(new BorderLayout(8,8));
        JPanel top=new JPanel(new BorderLayout(4,4));
        top.setBorder(BorderFactory.createEmptyBorder(12,12,8,12));
        top.add(new JLabel("<html><b>Identita' storiche: nessuna fusione automatica.</b> " +
            "Le proposte vengono applicate solo con un clic esplicito e ricordate alle prossime elaborazioni.</html>"),BorderLayout.NORTH);
        top.add(status,BorderLayout.SOUTH);
        add(top,BorderLayout.NORTH);
        table=new JTable(model);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(26);
        table.getColumnModel().getColumn(0).setPreferredWidth(140);
        table.getColumnModel().getColumn(1).setPreferredWidth(145);
        table.getColumnModel().getColumn(2).setPreferredWidth(85);
        table.getColumnModel().getColumn(3).setPreferredWidth(700);
        add(new JScrollPane(table),BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton safe=new JButton("Approva tutte le proposte esatte");
        safe.addActionListener(e->act(()->{
            for(Proposal p:audit.proposals())if(p.safe()&&!registry.complete(p))registry.approve(p);
        }));
        JButton approve=new JButton("Approva proposte selezionate");
        approve.addActionListener(e->act(()->{
            for(Row r:selected())if(r.proposal()!=null){
                for(Collision c:audit.collisions()) {
                    if(!registry.isDistinct(c) && r.proposal().members().stream().anyMatch(x->x.code()!=null&&x.code()==c.code()))
                        throw new IllegalArgumentException("Confermare prima la separazione del codice " + c.code());
                }
                registry.approve(r.proposal());
            }
        }));
        JButton distinct=new JButton("Conferma persone diverse");
        distinct.addActionListener(e->act(()->{
            for(Row r:selected())if(r.collision()!=null)registry.distinguish(r.collision());
        }));
        JButton same=new JButton("Conferma stessa persona");
        same.addActionListener(e->{
            int ok=JOptionPane.showConfirmDialog(this,
                "Unire esplicitamente i record dei codici selezionati? Verifica tutti i nomi e le stagioni.",
                "Conferma manuale identita'",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
            if(ok==JOptionPane.YES_OPTION)act(()->{
                for(Row r:selected())if(r.collision()!=null)registry.approveSame(r.collision());
            });
        });
        JButton undo=new JButton("Revoca decisioni selezionate");
        undo.addActionListener(e->act(()->{
            for(Row r:selected()) {
                if(r.collision()!=null)registry.undo(r.collision());
                else registry.undo(r.proposal());
            }
        }));
        JButton ignore=new JButton("Rimanda proposte selezionate");
        ignore.addActionListener(e->act(()->{
            for(Row r:selected())if(r.proposal()!=null)registry.ignore(r.proposal());
        }));
        JButton close=new JButton("Chiudi");close.addActionListener(e->dispose());
        for(JButton b:new JButton[]{safe,approve,distinct,same,undo,ignore,close})actions.add(b);
        add(actions,BorderLayout.SOUTH);
        setSize(1130,690);setLocationRelativeTo(owner);updateStatus();
    }
    private List<Row> selected(){
        List<Row> out=new ArrayList<>();
        for(int idx:table.getSelectedRows())out.add(rows.get(table.convertRowIndexToModel(idx)));
        if(out.isEmpty())throw new IllegalArgumentException("Selezionare almeno una riga");
        return out;
    }
    private void act(Runnable operation){
        try {
            operation.run();
            TopFlopPlayerIdentities.save(file,registry);
            ((AbstractTableModel)table.getModel()).fireTableDataChanged();
            updateStatus();
        } catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Verifica identita'",JOptionPane.ERROR_MESSAGE);}
    }
    private void updateStatus(){
        long pending=audit.proposals().stream().filter(p->!registry.complete(p)&&!registry.isIgnored(p)).count();
        long collisions=audit.collisions().stream().filter(c->!registry.isDistinct(c)&&!registry.isMerged(c)).count();
        status.setText("Anagrafiche: "+audit.sources()+" · proposte: "+audit.proposals().size()+
            " · identita' approvate: "+registry.approvedIdentities()+
            " · proposte in sospeso: "+pending+" · collisioni da risolvere: "+collisions);
    }
}
~~~~

## `src/main/java/it/alterlega/recordsnext/app/RecordsNextPipeline.java`

SHA256: `76da3e2fbce4b6925d13607d1054a7bb1858f3fec57bfe1b8c22d9e2b6803edf`

~~~~java
package it.alterlega.recordsnext.app;

import it.alterlega.recordsnext.Records2026SitePublisher;
import it.alterlega.recordsnext.RiserveUfficioArchiveBuilder;
import it.alterlega.recordsnext.SeasonRecordsArchiveBuilder;
import it.alterlega.recordsnext.app.manifest.ManifestMetadata;
import it.alterlega.recordsnext.app.core.LeagueMetadata;
import it.alterlega.recordsnext.app.core.LeagueMetadataLoader;
import it.alterlega.recordsnext.app.model.RecordFamily;
import it.alterlega.recordsnext.app.topflop.TopFlopViewsPublication;
import it.alterlega.recordsnext.app.manifest.ManifestPublishingSupport;
import it.alterlega.recordsnext.app.core.CoreJsExporter;
import it.alterlega.recordsnext.app.output.SeasonPublicationTargetRepository;
import it.alterlega.recordsnext.app.output.SeasonFamilyShardPublisher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Comparator;
import java.util.Set;

public final class RecordsNextPipeline {
    private static final Set<RecordFamily> IMPLEMENTED_FAMILIES = Set.copyOf(
            EnumSet.of(
                    RecordFamily.CLASSICS,
                    RecordFamily.SERIES,
                    RecordFamily.RU,
                    RecordFamily.MODIFIERS,
                    RecordFamily.THRESHOLDS_LUCK,
                    RecordFamily.TOP_FLOP
            )
    );

    public interface Listener {
        void phase(String text, int percent);

        default void timing(String text) {
            phase("TEMPO " + text, -1);
        }
    }

    public record Result(
            int classicEntries,
            int ruSeasons,
            int files,
            int published
    ) {
    }

    public enum PublicationMode {
        CURRENT_SITE,
        ALL_CONFIGURED_SITES
    }

    public Result run(
            PipelineConfig c,
            ProcessingOptions o,
            ProcessingMode mode,
            Listener l
    ) throws Exception {
        return run(c, o, mode, l, PublicationMode.CURRENT_SITE);
    }

    public Result run(
            PipelineConfig c,
            ProcessingOptions o,
            ProcessingMode mode,
            Listener l,
            PublicationMode publicationMode
    ) throws Exception {
        PipelinePreflight.Result preflight = preflight(o);

        l.phase(preflight.summary(), 2);

        for (String message : preflight.messages()) {
            l.phase("PREFLIGHT " + message, -1);
        }

        validateImplementedFamilies(o);

        long totalStarted = System.nanoTime();

        Path database = c.projectRoot()
                .resolve("data/database/recordsnext.db")
                .normalize();

        RecordsNextPreparationService preparation =
                new RecordsNextPreparationService(
                        c.projectRoot(),
                        database
                );

        long preparationStarted = System.nanoTime();

        List<String> changedSeasons =
                preparation.prepare(
                        mode,
                        c.seasons(),
                        l
                );

        l.timing(
                "preparazione complessiva: "
                        + elapsed(preparationStarted)
        );

        /*
         * Una stagione puo' risultare modificata/elaborata senza produrre
         * alcun season_normalized_*.json.
         *
         * Caso reale:
         * 2026_2027 e' correttamente la stagione corrente, ma prima della
         * prima partita non esiste ancora alcuna competizione testa-a-testa
         * elaborabile.
         *
         * In modalita' CONSOLIDATED questo NON deve essere considerato
         * un errore e gli archivi storici gia' consolidati non devono
         * essere ricostruiti.
         */
        List<String> reportSeasons = new ArrayList<>();

        for (String season : changedSeasons) {
            Path seasonReports = c.reports()
                    .resolve(season)
                    .normalize();

            if (!Files.isDirectory(seasonReports)) {
                continue;
            }

            try (var files = Files.list(seasonReports)) {
                boolean hasNormalized = files.anyMatch(path -> {
                    if (!Files.isRegularFile(path)) {
                        return false;
                    }

                    String name = path.getFileName().toString();

                    return name.startsWith("season_normalized_")
                            && name.endsWith(".json");
                });

                if (hasNormalized) {
                    reportSeasons.add(season);
                }
            }
        }

        if (mode == ProcessingMode.FULL) {
            l.phase(
                    "Pulizia archivi derivati delle stagioni gestite",
                    52
            );

            for (String season : changedSeasons) {
                deleteTree(
                        c.classicArchive().resolve(season)
                );

                deleteTree(
                        c.ruArchive().resolve(season)
                );
            }
        }

        if (o.familyEnabled(RecordFamily.CLASSICS)) {
            if (reportSeasons.isEmpty()) {
                l.phase(
                        "Record classici invariati: nessun nuovo report normalizzato",
                        55
                );
            } else {
                l.phase(
                        "Generazione record classici",
                        55
                );

                long started = System.nanoTime();

                SeasonRecordsArchiveBuilder.build(
                        c.reports(),
                        c.classicArchive(),
                        reportSeasons
                );

                l.timing(
                        "record classici: "
                                + elapsed(started)
                );
            }
        }

        if (o.familyEnabled(RecordFamily.RU)) {
            if (reportSeasons.isEmpty()) {
                l.phase(
                        "Riserve d'ufficio invariate: nessun nuovo report normalizzato",
                        68
                );
            } else {
                l.phase(
                        "Generazione riserve d'ufficio",
                        68
                );

                long started = System.nanoTime();

                RiserveUfficioArchiveBuilder.build(
                        c.reports(),
                        c.ruArchive(),
                        reportSeasons
                );

                l.timing(
                        "riserve d'ufficio: "
                                + elapsed(started)
                );
            }
        }

        Result result;
        Path topFlopScratch = null;
        TopFlopViewsPublication.Prepared topFlopPrepared = null;
        if (o.generateJs() && o.familyEnabled(RecordFamily.TOP_FLOP)) {
            Files.createDirectories(c.staging());
            topFlopScratch = Files.createTempDirectory(c.staging(), "recordsnext4-topflop-");
            try {
                l.phase("Elaborazione delle viste Top & Flop selezionate (SQLite locale)", 79);
                topFlopPrepared = TopFlopViewsPublication.prepare(
                        database, c.reports(), c.seasons(),
                        TopFlopViewsPublication.selectedIds(o.selection()), topFlopScratch,
                        it.alterlega.recordsnext.app.topflop.TopFlopSettings.load(
                                c.projectRoot().resolve("config/topflop.json")),
                        c.projectRoot().resolve("config/player-identities.json")
                );
                l.phase("Top & Flop: " + topFlopPrepared.files()
                        + " JS verificati, una sola elaborazione per tutte le destinazioni", -1);
            } catch (Exception ex) {
                deleteTree(topFlopScratch);
                throw ex;
            }
        }

        try {
        if (!o.generateJs()) {
            l.phase(
                    "Archivi elaborati; generazione JavaScript non richiesta",
                    96
            );

            result = new Result(
                    0,
                    0,
                    0,
                    0
            );
        } else {
            l.phase(
                    o.publish()
                            ? "Generazione e pubblicazione JavaScript"
                            : "Generazione JavaScript",
                    82
            );

            long started = System.nanoTime();

            LeagueMetadata leagueMetadata =
                    LeagueMetadataLoader.load(
                            c.projectRoot()
                                    .resolve("config/league.json")
                    );

            if (!o.publish()) {
                ManifestMetadata manifestMetadata =
                        new ManifestMetadata(
                                "RecordsNext by mauz79",
                                "3.2.0",
                                "2.0",
                                OffsetDateTime.now(),
                                leagueMetadata.leagueId(),
                                leagueMetadata.currentSeasonId(),
                                c.seasons(),
                                List.of()
                        );

                var r = Records2026SitePublisher.run(
                        c.classicArchive(),
                        c.ruArchive(),
                        c.staging(),
                        c.siteJs(),
                        true,
                        o.familyEnabled(RecordFamily.CLASSICS),
                        o.familyEnabled(RecordFamily.RU),
                        o,
                        preflight,
                        manifestMetadata,
                        database,
                        leagueMetadata,
                        c.reports()
                );

                int addedTopFlop = 0;
                if (topFlopPrepared != null) {
                    addedTopFlop = TopFlopViewsPublication.attachScoped(
                            topFlopPrepared, c.seasons(), r.stagingDirectory().resolve("js"));
                    ManifestPublishingSupport.write(r.stagingDirectory().resolve("js"),
                            o, preflight, manifestMetadata);
                    TopFlopViewsPublication.validateStaging(r.stagingDirectory().resolve("js"));
                    l.phase("Top & Flop integrato nello staging 3.2: " + addedTopFlop + " JS", -1);
                }
                result = new Result(
                        r.classicEntries(),
                        r.ruSeasons(),
                        r.validatedFiles() + addedTopFlop,
                        0
                );
            } else {
                SeasonPublicationTargetRepository targetRepository =
                        new SeasonPublicationTargetRepository(database);

                List<SeasonPublicationTargetRepository.Target> targets =
                        targetRepository.load(c.seasons());

                List<SeasonPublicationTargetRepository.Target> selectedTargets =
                        selectPublicationTargets(
                                targets,
                                leagueMetadata.currentSeasonId(),
                                publicationMode
                        );

                List<SeasonPublicationTargetRepository.Target> availableTargets =
                        selectedTargets.stream()
                                .filter(SeasonPublicationTargetRepository.Target::available)
                                .toList();

                for (SeasonPublicationTargetRepository.Target target : selectedTargets) {
                    if (!target.available()) {
                        l.phase(
                                "PUBBLICAZIONE saltata per "
                                        + target.seasonId()
                                        + ": sito locale non disponibile: "
                                        + target.siteRoot(),
                                -1
                        );
                    }
                }

                if (availableTargets.isEmpty()) {
                    if (publicationMode == PublicationMode.CURRENT_SITE) {
                        throw new IllegalStateException(
                                "Nessun sito locale configurato e disponibile per la stagione corrente "
                                        + leagueMetadata.currentSeasonId()
                                        + ". Configurare il sito della stagione oppure disattivare la pubblicazione."
                        );
                    }
                    throw new IllegalStateException(
                            "Nessun sito locale configurato e disponibile per la pubblicazione multisito."
                    );
                }

                int totalClassicEntries = 0;
                int totalRuSeasons = 0;
                int totalFiles = 0;
                int totalPublished = 0;

                Path scopesRoot = c.staging()
                        .resolve("multisite-scopes")
                        .normalize();

                deleteTree(scopesRoot);
                Files.createDirectories(scopesRoot);

                Path culometroConfig = c.projectRoot()
                        .resolve("config/culometro.json")
                        .normalize();

                if (o.culometroEnabled()) {
                    if (!Files.isRegularFile(culometroConfig)) {
                        throw new IllegalStateException(
                                "Configurazione Culometro non trovata: "
                                        + culometroConfig
                        );
                    }

                    Path scopedConfigDir = scopesRoot.resolve("config");
                    Files.createDirectories(scopedConfigDir);
                    Files.copy(
                            culometroConfig,
                            scopedConfigDir.resolve("culometro.json"),
                            StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.COPY_ATTRIBUTES
                    );
                }

                try {
                    for (SeasonPublicationTargetRepository.Target target : availableTargets) {
                        List<String> targetSeasons =
                                targetRepository.scope(
                                        c.seasons(),
                                        target
                                );

                        if (targetSeasons.isEmpty()) {
                            l.phase(
                                    "PUBBLICAZIONE saltata per "
                                            + target.seasonId()
                                            + ": nessuna stagione selezionata nello scope.",
                                    -1
                            );
                            continue;
                        }

                        l.phase(
                                "Pubblicazione sito "
                                        + target.seasonId()
                                        + " -> "
                                        + target.siteJs(),
                                84
                        );

                        Path targetScope = scopesRoot.resolve(target.seasonId());
                        Path scopedClassic = targetScope.resolve("classic");
                        Path scopedRu = targetScope.resolve("ru");
                        Path scopedReports = targetScope.resolve("reports");

                        createSeasonScopedView(
                                c.classicArchive(),
                                scopedClassic,
                                targetSeasons
                        );
                        createSeasonScopedView(
                                c.ruArchive(),
                                scopedRu,
                                targetSeasons
                        );
                        createSeasonScopedView(
                                c.reports(),
                                scopedReports,
                                targetSeasons
                        );

                        ManifestMetadata targetManifest =
                                new ManifestMetadata(
                                        "RecordsNext by mauz79",
                                        "3.2.0",
                                        "2.0",
                                        OffsetDateTime.now(),
                                        leagueMetadata.leagueId(),
                                        target.seasonId(),
                                        targetSeasons,
                                        List.of()
                                );

                        var generated = Records2026SitePublisher.run(
                                scopedClassic,
                                scopedRu,
                                c.staging(),
                                target.siteJs(),
                                true,
                                o.familyEnabled(RecordFamily.CLASSICS),
                                o.familyEnabled(RecordFamily.RU),
                                o,
                                preflight,
                                targetManifest,
                                null,
                                null,
                                scopedReports
                        );

                        Path generatedDir =
                                generated.stagingDirectory().resolve("js");

                        CoreJsExporter.export(
                                database,
                                generatedDir.resolve("fcmRecordsNext_Core.js"),
                                leagueMetadata.leagueId(),
                                leagueMetadata.leagueName(),
                                target.seasonId()
                        );

                        if (topFlopPrepared != null) {
                            int appended = TopFlopViewsPublication.attachScoped(
                                    topFlopPrepared, targetSeasons, generatedDir);
                            l.phase("Top & Flop " + target.seasonId()
                                    + ": " + appended + " JS nel cutoff storico", -1);
                        }

                        var flatPlan = SeasonFamilyShardPublisher.prepareFlat(generatedDir);
                        if (topFlopPrepared != null) {
                            // Il manifest deve elencare anche le viste e gli shard flat reali.
                            ManifestPublishingSupport.write(generatedDir, o, preflight, targetManifest);
                            TopFlopViewsPublication.validateStaging(generatedDir);
                        }
                        l.phase(
                                "Output flat " + target.seasonId()
                                        + ": shard=" + flatPlan.shards().size()
                                        + ", max=" + String.format(Locale.ROOT, "%.2f MiB",
                                                flatPlan.maxShardBytes() / (1024.0 * 1024.0)),
                                -1
                        );

                        int published = publishGeneratedDirectory(
                                generatedDir,
                                target.siteJs()
                        );

                        totalClassicEntries += generated.classicEntries();
                        totalRuSeasons += generated.ruSeasons();
                        totalFiles += published;
                        totalPublished += published;
                    }
                } finally {
                    deleteTree(scopesRoot);
                }

                result = new Result(
                        totalClassicEntries,
                        totalRuSeasons,
                        totalFiles,
                        totalPublished
                );
            }

            l.timing(
                    (
                            o.publish()
                                    ? "generazione e pubblicazione JavaScript multisito: "
                                    : "generazione JavaScript: "
                    )
                            + elapsed(started)
            );
        }

        } finally {
            if (topFlopScratch != null) deleteTree(topFlopScratch);
        }

        preparation.saveConsolidation(
                c.seasons()
        );

        l.timing(
                "totale elaborazione: "
                        + elapsed(totalStarted)
        );

        l.phase(
                "Elaborazione completata e consolidamento aggiornato",
                100
        );

        return result;
    }

    public PipelinePreflight.Result preflight(
            ProcessingOptions options
    ) {
        return PipelinePreflight.evaluate(options);
    }

    public boolean hasConsolidation(
            PipelineConfig c
    ) {
        Path database = c.projectRoot()
                .resolve("data/database/recordsnext.db")
                .normalize();

        return new RecordsNextPreparationService(
                c.projectRoot(),
                database
        ).hasConsolidation();
    }

    static void validateImplementedFamilies(
            ProcessingOptions options
    ) {
        Set<RecordFamily> unsupported =
                EnumSet.copyOf(
                        options.selection().enabledFamilies()
                );

        unsupported.removeAll(
                IMPLEMENTED_FAMILIES
        );

        if (!unsupported.isEmpty()) {
            throw new IllegalArgumentException(
                    "Famiglie selezionate ma non ancora collegate alla pipeline 2.0: "
                            + unsupported
            );
        }
    }

    static List<SeasonPublicationTargetRepository.Target> selectPublicationTargets(
            List<SeasonPublicationTargetRepository.Target> targets,
            String currentSeasonId,
            PublicationMode mode) {

        if (mode == PublicationMode.ALL_CONFIGURED_SITES) {
            return List.copyOf(targets);
        }

        return targets.stream()
                .filter(target -> target.seasonId().equals(currentSeasonId))
                .toList();
    }

    private static void createSeasonScopedView(
            Path sourceRoot,
            Path targetRoot,
            List<String> seasons) throws Exception {

        Files.createDirectories(targetRoot);

        for (String season : seasons) {
            Path source = sourceRoot.resolve(season).normalize();
            if (!Files.isDirectory(source)) {
                continue;
            }

            copyTree(source, targetRoot.resolve(season));
        }
    }

    private static void copyTree(
            Path source,
            Path target) throws Exception {

        try (var paths = Files.walk(source)) {
            for (Path path : paths.toList()) {
                Path relative = source.relativize(path);
                Path destination = target.resolve(relative);

                if (Files.isDirectory(path)) {
                    Files.createDirectories(destination);
                } else {
                    Files.createDirectories(destination.getParent());
                    Files.copy(
                            path,
                            destination,
                            StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.COPY_ATTRIBUTES
                    );
                }
            }
        }
    }

    static int publishGeneratedDirectory(
            Path generatedDir,
            Path siteJsDir) throws Exception {

        Files.createDirectories(siteJsDir);

        List<Path> files;
        try (var stream = Files.list(generatedDir)) {
            files = stream
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(
                            path -> path.getFileName().toString()
                    ))
                    .toList();
        }

        Set<String> generatedNames = files.stream()
                .map(path -> path.getFileName().toString())
                .collect(java.util.stream.Collectors.toSet());

        List<Path> staleFlatShards;
        try (var stream = Files.list(siteJsDir)) {
            staleFlatShards = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> SeasonFamilyShardPublisher.isFlatShardFileName(
                            path.getFileName().toString())
                            || path.getFileName().toString().equals(
                                    "fcmRecordsNext_TopFlopViews.js")
                            || path.getFileName().toString().matches(
                                    "fcmRecordsNext_TopFlopViews\\.(?:Careers|Teams|Records|Highlights)\\."
                                    + "[0-9]{4}_[0-9]{4}\\.[0-9]{3}\\.js"))
                    .filter(path -> !generatedNames.contains(path.getFileName().toString()))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
        }

        Path backupRoot = generatedDir.getParent()
                .resolve("multisite-publish-backup");
        deleteTree(backupRoot);
        Files.createDirectories(backupRoot);

        List<Path> replaced = new ArrayList<>();
        List<Path> created = new ArrayList<>();
        List<Path> removedStale = new ArrayList<>();

        try {
            for (Path stale : staleFlatShards) {
                Path backup = backupRoot.resolve(stale.getFileName());
                Files.copy(
                        stale,
                        backup,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.COPY_ATTRIBUTES
                );
                Files.delete(stale);
                removedStale.add(stale);
            }

            for (Path source : files) {
                Path target = siteJsDir.resolve(source.getFileName());

                if (Files.exists(target)) {
                    Files.copy(
                            target,
                            backupRoot.resolve(source.getFileName()),
                            StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.COPY_ATTRIBUTES
                    );
                    replaced.add(target);
                } else {
                    created.add(target);
                }

                Path temp = siteJsDir.resolve(
                        "." + source.getFileName()
                                + ".recordsnext-multisite.tmp"
                );

                Files.copy(
                        source,
                        temp,
                        StandardCopyOption.REPLACE_EXISTING
                );

                try {
                    Files.move(
                            temp,
                            target,
                            StandardCopyOption.ATOMIC_MOVE,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                } catch (java.nio.file.AtomicMoveNotSupportedException ex) {
                    Files.move(
                            temp,
                            target,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }
            }
        } catch (Exception ex) {
            for (Path target : created) {
                Files.deleteIfExists(target);
            }

            for (Path target : replaced) {
                Path backup = backupRoot.resolve(target.getFileName());
                if (Files.isRegularFile(backup)) {
                    Files.copy(
                            backup,
                            target,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }
            }

            for (Path target : removedStale) {
                Path backup = backupRoot.resolve(target.getFileName());
                if (Files.isRegularFile(backup)) {
                    Files.copy(
                            backup,
                            target,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }
            }

            throw ex;
        }

        return files.size();
    }

    private static void deleteTree(
            Path root
    ) throws Exception {
        if (!Files.exists(root)) {
            return;
        }

        try (var paths = Files.walk(root)) {
            for (Path path :
                    paths.sorted(
                            java.util.Comparator.reverseOrder()
                    ).toList()) {

                Files.deleteIfExists(path);
            }
        }
    }

    private static String elapsed(
            long started
    ) {
        double seconds =
                (System.nanoTime() - started)
                        / 1_000_000_000.0;

        return String.format(
                Locale.ROOT,
                "%.3f s",
                seconds
        );
    }
}
~~~~

## `src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopPublicViewsStageExporter.java`

SHA256: `6a1dd4b7afb3a3b50e405715e61e6c03814d6c9312a08bf50d4ea74b1caaf298`

~~~~java
package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.topflop.TopFlopFacts.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * Approved public VIEWS, not the full FCA/FCM facts dump. Stage-only.
 * All historical facts remain in local SQLite. No scorecard URL is synthesized.
 * No publisher, manifest, CSS or site is modified by this class.
 *
 * Averages (TF05/TF06), wasted votes (TF11-13), and baseline-derived
 * rankings (TF14-15) are NOT promoted to definitive rankings while their
 * policy/evidence is incomplete. Their available raw aggregates are exposed.
 */
public final class TopFlopPublicViewsStageExporter {
    private TopFlopPublicViewsStageExporter() {}
    public static final long LIMIT_BYTES = 1_500_000;
    public static final long TARGET_BYTES = 950_000;
    public static final String FACADE = "fcmRecordsNext_TopFlopViews.js";
    private static final MathContext PRECISION = MathContext.DECIMAL64;
    private static final List<String> EVENTS = List.of(
        "GOLFATTI1", "GOLFATTISURIGORE1", "ASSIST", "GOLSUBITI",
        "GOLSUBITISURIGORE", "RIGPAR", "RIGSBA", "AUTOGOL1", "AMM", "ESP");

    public record FileInfo(String name, String season, String kind, int rows, long bytes, String sha256) {}
    public record Result(List<FileInfo> files, int seasons, int players,
                         long sourceRealDays, long sourceFantasyContexts, Path output) {}

    static final class Totals {
        int validVotes, counted, countedRealValid, observed, benchUnused, outsideList, unknown;
        int keeperValidConcededDays, keeperZeroConcededDays;
        BigDecimal votes = BigDecimal.ZERO, fantasy = BigDecimal.ZERO;
        BigDecimal countedRealVotes = BigDecimal.ZERO;
        int unusedRealVotesValid, unusedFantasyTotalsValid;
        BigDecimal unusedVotes=BigDecimal.ZERO, unusedFantasy=BigDecimal.ZERO;
        final Map<String,BigDecimal> unusedEvents=new LinkedHashMap<>();
        BigDecimal keeperConcededOnValidDays = BigDecimal.ZERO;
        final Map<String,BigDecimal> events = new LinkedHashMap<>();
        void event(Map<String,BigDecimal> raw) {
            for (String key : EVENTS) {
                BigDecimal amount = raw.get(key);
                if (amount != null && amount.signum() != 0) {
                    events.merge(key, amount, BigDecimal::add);
                }
            }
        }
        Map<String,Object> realView() {
            return obj("votiValidi",validVotes,"sommaVoti",votes,
                "mediaVoto",mean(votes,validVotes),"eventi",events,
                "giornateGolSubitiConVoto",keeperValidConcededDays,
                "giornateZeroGolSubitiRegistrati",keeperZeroConcededDays,
                "mediaGolSubitiSuGiornateVerificate",
                    mean(keeperConcededOnValidDays,keeperValidConcededDays));
        }
        Map<String,Object> fantasyView() {
            return obj("contestiOsservati",observed,"conteggiati",counted,
                "conVotoRealeValido",countedRealValid,"sommaVotiRealiApportati",countedRealVotes,
                "mediaVotoRealeApportato",mean(countedRealVotes,countedRealValid),
                "sommaFantavotiApportati",fantasy,
                "fantamediaApportata",mean(fantasy,counted),
                "panchinaNonConteggiata",benchUnused,"fuoriLista",outsideList,
                "utilizzoDaVerificare",unknown,"eventiApportati",events,
                "nonUtilizzatoOsservato",obj(
                    "gareConVotoReale",unusedRealVotesValid,
                    "sommaVotiReali",unusedVotes,
                    "mediaVotoReale",mean(unusedVotes,unusedRealVotesValid),
                    "gareConFantavotoNumerico",unusedFantasyTotalsValid,
                    "sommaFantavotiTabellino",unusedFantasy,
                    "fantamediaTabellino",mean(unusedFantasy,unusedFantasyTotalsValid),
                    "eventiRealiNonApportati",unusedEvents,
                    "origine","FCM_FORMAZIONE_NON_CONTEGGIATA_E_VOTO_FCA"));
        }
    }
    static final class Costs {
        int n;
        BigDecimal sum=BigDecimal.ZERO,min,max;
        void add(String raw) {
            if(raw==null || !raw.matches("[0-9]+(?:[,.][0-9]+)?"))return;
            BigDecimal value=new BigDecimal(raw.replace(',','.'));
            n++;sum=sum.add(value);
            if(min==null || value.compareTo(min)<0)min=value;
            if(max==null || value.compareTo(max)>0)max=value;
        }
        Map<String,Object> view() {
            return obj("registrazioniConCosto",n,"minimo",min,"massimo",max,
                "medio",n==0?null:mean(sum,n),"origine","FCM.Tesserato.COSTOACQ");
        }
    }
    static final class Player {
        final TopFlopFcaJdbcReader.PlayerProfile profile;
        final Totals real = new Totals(), fantasy = new Totals();
        final Costs costs = new Costs();
        Player(TopFlopFcaJdbcReader.PlayerProfile profile) {this.profile=profile;}
        String id() {return profile.player().seasonId()+":"+profile.player().fcaPlayerId();}
        int role() {
            try {return Integer.parseInt(profile.originalFields().get("GiocatoreA.RUOLO"));}
            catch (RuntimeException ex) {return -1;}
        }
        Map<String,Object> label() {
            return obj("playerKey",id(),"nome",profile.name(),
                "codiceStorico",profile.player().gazzaCode(),"ruoloFca",role());
        }
    }
    record TeamKey(int playerId,int teamId,int competitionId) {}
    static final class Team {
        final Player player;
        final int teamId, competitionId;
        final String teamName, competitionName, family;
        final Totals totals = new Totals();
        final Costs costs = new Costs();
        Team(Player player,FantasyFact fact) {
            this.player=player;
            teamId=fact.key().fantasyTeamId();competitionId=fact.key().sourceCompetitionId();
            teamName=fact.sourceFields().getOrDefault("FCM.Fantasquadra.Nome", "");
            competitionName=fact.competitionName();family=fact.family().name();
        }
        Map<String,Object> view() {
            Map<String,Object> x=new LinkedHashMap<>(player.label());
            x.putAll(obj("fantasquadra",teamName,"idSquadraInterno",teamId,
                "competizione",competitionName,"idCompetizioneInterno",competitionId,
                "famiglia",family,"apporto",totals.fantasyView(),
                "costoAcquisto",costs.view()));
            return x;
        }
    }
    static final class Winner {
        final String id, group, metric, direction;
        BigDecimal value;
        final List<Map<String,Object>> entries = new ArrayList<>();
        Winner(String id,String group,String metric,String direction) {
            this.id=id;this.group=group;this.metric=metric;this.direction=direction;
        }
        void offer(BigDecimal candidate,Map<String,Object> entry) {
            if (candidate == null) return;
            int compare=value==null ? 1 : candidate.compareTo(value);
            boolean better=value==null || (direction.equals("MAX") && compare>0) ||
                (direction.equals("MIN") && compare<0);
            if (better) {value=candidate;entries.clear();}
            if (better || candidate.compareTo(value)==0) entries.add(entry);
        }
        Map<String,Object> view() {
            return obj("recordId",id,"ambito",group,"metrica",metric,
                "ordine",direction,"valore",value,"pariMerito",entries.size(),"risultati",entries);
        }
    }
    static final class SeasonView {
        final List<Map<String,Object>> careers = new ArrayList<>(), teams = new ArrayList<>(),
            records = new ArrayList<>(), highlights = new ArrayList<>();
        final Map<String,Object> counters = new LinkedHashMap<>();
    }

    /** Exposed for synthetic, source-independent regression tests. */
    static SeasonView build(TopFlopJdbcArchiveAssembler.LoadedSeason loaded,
                     Map<TopFlopCanonicalScorecards.MatchTeam,String> links) {
        return build(loaded,links,TopFlopSettings.defaults());
    }
    static SeasonView build(TopFlopJdbcArchiveAssembler.LoadedSeason loaded,
                     Map<TopFlopCanonicalScorecards.MatchTeam,String> links,TopFlopSettings settings) {
        Objects.requireNonNull(loaded);Objects.requireNonNull(links);
        TopFlopArchive archive=loaded.archive();
        Map<Integer,Player> players=new TreeMap<>();
        for (var profile:loaded.profiles().values()) {
            Player old=players.putIfAbsent(profile.player().fcaPlayerId(),new Player(profile));
            if(old!=null)throw new IllegalStateException("Duplicate player profile");
        }
        Map<String,Winner> winners=new LinkedHashMap<>();
        Map<String,Map<String,Object>> highlights=new LinkedHashMap<>();
        int valid=0,counted=0;
        for (RealDay day:archive.allRealDays()) {
            Player p=need(players,day.key().player().fcaPlayerId());
            boolean validVote=day.voteState()==VoteState.VALID;
            if(validVote) {
                valid++;
                p.real.validVotes++;
                p.real.votes=p.real.votes.add(day.vote());
                String hk=realHighlight(day,p);
                offer(winners,"TF01","realeSingolaGiornata","votoReale","MAX",day.vote(),
                    obj("calciatore",p.label(),"giornataSerieA",day.key().serieARound(),"highlightKey",hk));
                offer(winners,"TF02","realeSingolaGiornata","votoReale","MIN",day.vote(),
                    obj("calciatore",p.label(),"giornataSerieA",day.key().serieARound(),"highlightKey",hk));
            }
            p.real.event(day.events());
            if(p.role()==1 && validVote && day.events().containsKey("GOLSUBITI")) {
                p.real.keeperValidConcededDays++;
                BigDecimal conceded=day.events().get("GOLSUBITI");
                p.real.keeperConcededOnValidDays=p.real.keeperConcededOnValidDays.add(conceded);
                if(conceded.signum()==0)p.real.keeperZeroConcededDays++;
                if(conceded.signum()>0)offer(winners,"TF18","portieriSingolaGiornata","golSubiti","MAX",conceded,
                    obj("calciatore",p.label(),"giornataSerieA",day.key().serieARound(),
                        "highlightKey",hkReal(day,p,highlights)));
            }
            if(p.role()==1) {
                BigDecimal saved=event(day.events(),"RIGPAR");
                if(saved.signum()>0) offer(winners,"TF17","portieriSingolaGiornata","rigoriParati","MAX",saved,
                    obj("calciatore",p.label(),"giornataSerieA",day.key().serieARound(),
                        "highlightKey",hkReal(day,p,highlights)));
                BigDecimal goals=event(day.events(),"GOLFATTI1");
                if(goals.signum()>0) {
                    // Only an actual source goal, NOT goals + penalty goals.
                    hkReal(day,p,highlights);
                }
            }
        }
        Map<TeamKey,Team> teams=new LinkedHashMap<>();
        for (FantasyFact fact:archive.allFantasyFacts()) {
            Player p=need(players,fact.key().player().fcaPlayerId());
            TeamKey key=new TeamKey(fact.key().player().fcaPlayerId(),
                fact.key().fantasyTeamId(),fact.key().sourceCompetitionId());
            Team t=teams.computeIfAbsent(key, ignored->new Team(p,fact));
            RealDay realForFact=archive.realDay(fact.realDayKey());
            addUsage(p.fantasy,fact,realForFact);addUsage(t.totals,fact,realForFact);
            if(fact.usage()!=Usage.COUNTED)continue;
            if(realForFact.voteState()==VoteState.VALID) {
                p.fantasy.countedRealValid++;
                p.fantasy.countedRealVotes=p.fantasy.countedRealVotes.add(realForFact.vote());
                t.totals.countedRealValid++;
                t.totals.countedRealVotes=t.totals.countedRealVotes.add(realForFact.vote());
            }
            counted++;
            String hk=highlightKey(fact);
            Map<String,Object> entry=obj("calciatore",p.label(),
                "fantasquadra",t.teamName,"competizione",t.competitionName,
                "famiglia",t.family,"giornataCompetizione",fact.fantasyRound(),
                "giornataSerieA",fact.realDayKey().serieARound(),"highlightKey",hk);
            offer(winners,"TF03","fantasySingoloIncontro","fantavotoApportato","MAX",fact.fantasyVote(),entry);
            offer(winners,"TF04","fantasySingoloIncontro","fantavotoApportato","MIN",fact.fantasyVote(),entry);
        }
        // Acquisition prices are actual FCM roster-event values, never inferred
        // from votes or price trends; no assumption about ownership dates.
        Map<String,Costs> costsByTeam=new LinkedHashMap<>();
        for(var roster:loaded.rosterEvents()) {
            Player p=players.get(roster.player().fcaPlayerId());
            if(p==null)continue;
            String cost=roster.sourceFields().get("boughtFor");
            p.costs.add(cost);
            costsByTeam.computeIfAbsent(roster.player().fcaPlayerId()+":"+roster.fantasyTeamId(),
                ignored->new Costs()).add(cost);
        }
        for(Team team:teams.values()) {
            Costs registered=costsByTeam.get(team.player.profile.player().fcaPlayerId()+":"+team.teamId);
            if(registered!=null) {
                team.costs.n=registered.n;team.costs.sum=registered.sum;
                team.costs.min=registered.min;team.costs.max=registered.max;
            }
        }
        // A highlight is published ONLY if an approved record points to it;
        // notable goalkeeper goals are an explicitly approved TF20 exception.
        Set<String> selected=new LinkedHashSet<>();
        for(Winner w:winners.values())for(var e:w.entries){
            Object h=e.get("highlightKey");if(h!=null)selected.add(h.toString());
        }
        for(RealDay day:archive.allRealDays()) {
            Player p=need(players,day.key().player().fcaPlayerId());
            if(p.role()==1 && (event(day.events(),"GOLFATTI1").signum()>0 ||
                event(day.events(),"ASSIST").signum()>0))
                selected.add(realHighlight(day,p));
        }
        // If a REAL record day also appears in a fantasy fixture, expose its
        // actual FCM context and canonical tabellino (never a fabricated link).
        Map<String,List<Map<String,Object>>> realLinks=new LinkedHashMap<>();
        for(FantasyFact fact:archive.allFantasyFacts()) {
            RealDay realDay=archive.realDay(fact.realDayKey());
            Player p=need(players,fact.key().player().fcaPlayerId());
            String realKey=realHighlight(realDay,p);
            if(!selected.contains(realKey))continue;
            String url=links.get(new TopFlopCanonicalScorecards.MatchTeam(
                fact.key().matchId(),fact.key().fantasyTeamId()));
            realLinks.computeIfAbsent(realKey,ignored->new ArrayList<>()).add(obj(
                "fantasquadra",fact.sourceFields().get("FCM.Fantasquadra.Nome"),
                "competizione",fact.competitionName(),"famiglia",fact.family().name(),
                "giornataCompetizione",fact.fantasyRound(),
                "descrizioneGiornata",clean(fact.sourceFields().get("FCM.Giornata.Desc")),
                "giornataSerieA",fact.realDayKey().serieARound(),
                "schieramento",lineup(fact),"conteggiato",fact.usage()==Usage.COUNTED,
                "fantavoto",fact.usage()==Usage.COUNTED?fact.fantasyVote():null,
                "urlTabellino",url));
        }
        // Resolve only the selected fantasy encounters, never copy all canonical links.
        for(FantasyFact fact:archive.allFantasyFacts()) {
            String key=highlightKey(fact);
            if(!selected.contains(key))continue;
            Player p=need(players,fact.key().player().fcaPlayerId());
            String url=links.get(new TopFlopCanonicalScorecards.MatchTeam(fact.key().matchId(),
                fact.key().fantasyTeamId()));
            RealDay real=archive.realDay(fact.realDayKey());
            highlights.putIfAbsent(key,obj("key",key,"tipo","fantasyConteggiato",
                "calciatore",p.label(),"votoReale",real.vote(),"fantavoto",fact.fantasyVote(),
                "votoTabellino",numericToken(fact.sourceFields().get("FCM.Tabellino.Voto")),
                "eventi",publicEvents(fact.fantasyEvents()),
                "fantasquadra",fact.sourceFields().get("FCM.Fantasquadra.Nome"),
                "competizione",fact.competitionName(),"famiglia",fact.family().name(),
                "giornataCompetizione",fact.fantasyRound(),
                "descrizioneGiornata",clean(fact.sourceFields().get("FCM.Giornata.Desc")),
                "giornataSerieA",fact.realDayKey().serieARound(),
                "schieramento",lineup(fact),"conteggiato",true,
                "urlTabellino",url));
        }
        for(RealDay day:archive.allRealDays()) {
            Player p=need(players,day.key().player().fcaPlayerId());
            String key=realHighlight(day,p);
            if(selected.contains(key)) highlights.putIfAbsent(key,obj(
                "key",key,"tipo","prestazioneReale","calciatore",p.label(),
                "giornataSerieA",day.key().serieARound(),"votoReale",day.vote(),
                "eventi",publicEvents(day.events()),"urlTabellino",null,
                "contestiFantasy",realLinks.getOrDefault(key,List.of())));
        }
        SeasonView out=new SeasonView();
        for(Player p:players.values()) {
            Map<String,Object> career=new LinkedHashMap<>(p.label());
            career.put("stagione",p.profile.player().seasonId());
            career.put("reale",p.real.realView());career.put("fantasy",p.fantasy.fantasyView());
            career.put("costoAcquisto",p.costs.view());
            career.put("identitaStoricaVerificata",false); // no auto-merge of CodiceGazza
            out.careers.add(career);
            Map<String,Object> base=obj("calciatore",p.label());
            if(p.real.validVotes>=settings.realSeasonVotes()) {
                offer(winners,"TF05","mediaVotoRealeStagione","mediaVoto","MAX",
                    mean(p.real.votes,p.real.validVotes),
                    obj("calciatore",p.label(),"votiValidi",p.real.validVotes));
                offer(winners,"TF05","mediaVotoRealeStagione","mediaVoto","MIN",
                    mean(p.real.votes,p.real.validVotes),
                    obj("calciatore",p.label(),"votiValidi",p.real.validVotes));
            }
            offerPositive(winners,"TF07","golRealiStagione","gol",event(p.real.events,"GOLFATTI1"),base);
            offerPositive(winners,"TF07","golApportatiContestiFantasy","gol",event(p.fantasy.events,"GOLFATTI1"),base);
            offerPositive(winners,"TF08","assistRealiStagione","assist",event(p.real.events,"ASSIST"),base);
            offerPositive(winners,"TF08","assistApportatiContestiFantasy","assist",event(p.fantasy.events,"ASSIST"),base);
            for(String negative:List.of("AMM","ESP","AUTOGOL1","RIGSBA")) {
                offerPositive(winners,"TF09","eventiNegativiRealiStagione",negative,event(p.real.events,negative),base);
                offerPositive(winners,"TF09","eventiNegativiApportati",negative,event(p.fantasy.events,negative),base);
            }
            offerPositive(winners,"TF10","giornateRealiConVoto","n",BigDecimal.valueOf(p.real.validVotes),base);
            offerPositive(winners,"TF10","incontriFantasyConteggiati","n",BigDecimal.valueOf(p.fantasy.counted),base);
            if(p.role()==1) {
                offerPositive(winners,"TF17","portieriRigoriParatiStagione","RIGPAR",event(p.real.events,"RIGPAR"),base);
                offerPositive(winners,"TF17","portieriRigoriParatiApportati","RIGPAR",event(p.fantasy.events,"RIGPAR"),base);
                offerPositive(winners,"TF18","portieriGolSubitiStagione","GOLSUBITI",event(p.real.events,"GOLSUBITI"),base);
                offerPositive(winners,"TF18","portieriGolSubitiApportati","GOLSUBITI",event(p.fantasy.events,"GOLSUBITI"),base);
                offerPositive(winners,"TF19","portieriGiornateZeroGolSubitiRegistrati","n",
                    BigDecimal.valueOf(p.real.keeperZeroConcededDays),base);
                offerPositive(winners,"TF20","portieriGolStagione","GOLFATTI1",event(p.real.events,"GOLFATTI1"),base);
                offerPositive(winners,"TF20","portieriGolSuRigoreStagione","GOLFATTISURIGORE1",event(p.real.events,"GOLFATTISURIGORE1"),base);
                offerPositive(winners,"TF20","portieriAssistStagione","ASSIST",event(p.real.events,"ASSIST"),base);
                offerPositive(winners,"TF20","portieriGolApportati","GOLFATTI1",event(p.fantasy.events,"GOLFATTI1"),base);
                offerPositive(winners,"TF20","portieriGolSuRigoreApportati","GOLFATTISURIGORE1",event(p.fantasy.events,"GOLFATTISURIGORE1"),base);
                offerPositive(winners,"TF20","portieriAssistApportati","ASSIST",event(p.fantasy.events,"ASSIST"),base);
            }
        }
        for(Team t:teams.values()) {
            int minimum="CAMPIONATO".equals(t.family)
                ? settings.fantasyLeagueMatches():settings.fantasyCupMatches();
            if(t.totals.counted>=minimum) {
                BigDecimal avg=mean(t.totals.fantasy,t.totals.counted);
                Map<String,Object> entry=obj("calciatore",t.player.label(),
                    "fantasquadra",t.teamName,"competizione",t.competitionName,
                    "famiglia",t.family,"incontriConteggiati",t.totals.counted);
                offer(winners,"TF06","fantamediaApportataPerSquadraECompetizione",
                    "fantamedia","MAX",avg,entry);
                offer(winners,"TF06","fantamediaApportataPerSquadraECompetizione",
                    "fantamedia","MIN",avg,entry);
            }
        }
        teams.values().stream().sorted(Comparator.comparing((Team t)->t.player.id())
            .thenComparingInt(t->t.teamId).thenComparingInt(t->t.competitionId))
            .map(Team::view).forEach(out.teams::add);
        for(Winner w:winners.values()) if(w.value!=null)out.records.add(w.view());
        out.highlights.addAll(highlights.values());
        out.counters.putAll(obj("players",players.size(),"realDays",archive.realDayCount(),
            "validRealVotes",valid,"fantasyContexts",archive.fantasyFactCount(),
            "fantasyCounted",counted,"teamCompetitionAggregates",teams.size(),
            "selectedHighlights",out.highlights.size(),"youthExcluded",loaded.fcm().youthExcluded()));
        return out;
    }

    private static void addUsage(Totals a,FantasyFact f,RealDay real) {
        a.observed++;
        if(f.usage()!=Usage.COUNTED) {
            switch(f.usage()){
                case BENCH_UNUSED -> a.benchUnused++;
                case OUTSIDE_LIST -> a.outsideList++;
                default -> a.unknown++;
            }
            // A substitute not counted, or a documented outside-list slot, can
            // have an actual FCA vote and an actual numeric FCM tabellino total.
            // This is NOT a simulated lineup, nor proof of continuous ownership.
            if(f.usage()==Usage.BENCH_UNUSED || f.usage()==Usage.OUTSIDE_LIST) {
                if(real.voteState()==VoteState.VALID) {
                    a.unusedRealVotesValid++;
                    a.unusedVotes=a.unusedVotes.add(real.vote());
                    for(String event:EVENTS) {
                        BigDecimal v=real.events().get(event);
                        if(v!=null && v.signum()!=0)
                            a.unusedEvents.merge(event,v,BigDecimal::add);
                    }
                }
                if(f.fantasyVote()!=null && real.voteState()==VoteState.VALID) {
                    a.unusedFantasyTotalsValid++;
                    a.unusedFantasy=a.unusedFantasy.add(f.fantasyVote());
                }
            }
            return;
        }
        if(f.fantasyVote()==null) throw new IllegalStateException("COUNTED without FCM total");
        a.counted++;a.fantasy=a.fantasy.add(f.fantasyVote());
        // The assembler credits source FCA events ONLY on COUNTED contexts.
        a.event(f.fantasyEvents());
    }
    private static String lineup(FantasyFact fact) {
        if(fact.usage()==Usage.BENCH_UNUSED)return "RISERVA_NON_ENTRATA";
        if(fact.usage()==Usage.OUTSIDE_LIST)return "FUORI_LISTA";
        if(fact.usage()!=Usage.COUNTED)return "SCHIERAMENTO_DA_VERIFICARE";
        if(fact.initialStarter())return "TITOLARE";
        if("1".equals(fact.sourceFields().get("FCM.Formazione.EntratoRiserva")))return "RISERVA_ENTRATA";
        return "CONTEGGIATO_SCHIERAMENTO_DA_VERIFICARE";
    }
    private static BigDecimal numericToken(String source) {
        if(source==null || !source.matches("[+-]?[0-9]+(?:[,.][0-9]+)?"))return null;
        return new BigDecimal(source.replace(',','.'));
    }
    private static String clean(String raw) {
        if(raw==null || raw.equals("__SQL_NULL__"))return null;
        return raw;
    }
    private static void offerPositive(Map<String,Winner> w,String id,String group,String metric,
                                     BigDecimal amount,Map<String,Object> entry) {
        if(amount.signum()>0) offer(w,id,group,metric,"MAX",amount,entry);
    }
    private static void offer(Map<String,Winner> w,String id,String group,String metric,
                              String order,BigDecimal value,Map<String,Object> entry) {
        String key=id+":"+group+":"+metric+":"+order;
        w.computeIfAbsent(key,ignored->new Winner(id,group,metric,order)).offer(value,entry);
    }
    static BigDecimal event(Map<String,BigDecimal> events,String name) {
        return events.getOrDefault(name,BigDecimal.ZERO);
    }
    static Map<String,BigDecimal> publicEvents(Map<String,BigDecimal> input) {
        Map<String,BigDecimal> m=new LinkedHashMap<>();
        for(String key:EVENTS) {
            BigDecimal v=input.get(key);
            if(v!=null && v.signum()!=0) m.put(key,v);
        }
        return m;
    }
    private static String realHighlight(RealDay day,Player p) {
        return "R:"+p.id()+":"+day.key().serieARound();
    }
    private static String hkReal(RealDay day,Player p,Map<String,Map<String,Object>> highlights) {
        return realHighlight(day,p);
    }
    private static String highlightKey(FantasyFact fact) {
        return "F:"+fact.key().player().seasonId()+":"+fact.key().player().fcaPlayerId()+
            ":"+fact.key().matchId()+":"+fact.key().fantasyTeamId();
    }
    private static Player need(Map<Integer,Player> players,int id) {
        Player p=players.get(id);if(p==null)throw new IllegalStateException("Missing profile: "+id);return p;
    }
    private static BigDecimal mean(BigDecimal value,int count) {
        return count==0?null:value.divide(BigDecimal.valueOf(count),PRECISION);
    }
    private static Map<String,Object> obj(Object... args) {
        if(args.length%2!=0)throw new IllegalArgumentException("odd map args");
        Map<String,Object> m=new LinkedHashMap<>();
        for(int i=0;i<args.length;i+=2)m.put((String)args[i],args[i+1]);
        return m;
    }
    private static String json(Object value) {return TopFlopCompactStageJsExporter.Json.write(value);}

    /** Reversible positional encoding: only public VIEWS, never source match histories. */
    private static List<Object> packEvents(Object raw) {
        @SuppressWarnings("unchecked") Map<String,BigDecimal> events=(Map<String,BigDecimal>)raw;
        List<Object> compact=new ArrayList<>();
        if(events==null)return compact;
        for(int i=0;i<EVENTS.size();i++) {
            BigDecimal v=events.get(EVENTS.get(i));
            if(v!=null && v.signum()!=0) {compact.add(i);compact.add(v);}
        }
        return compact;
    }
    private static List<Object> packCost(Object raw) {
        @SuppressWarnings("unchecked") Map<String,Object> c=(Map<String,Object>)raw;
        return List.of(c.get("registrazioniConCosto"),
            c.get("minimo")==null?"":c.get("minimo"),
            c.get("massimo")==null?"":c.get("massimo"),
            c.get("medio")==null?"":c.get("medio"));
    }
    private static List<Object> packUnused(Object raw) {
        @SuppressWarnings("unchecked") Map<String,Object> u=(Map<String,Object>)raw;
        return List.of(u.get("gareConVotoReale"),u.get("sommaVotiReali"),
            u.get("gareConFantavotoNumerico"),u.get("sommaFantavotiTabellino"),
            packEvents(u.get("eventiRealiNonApportati")));
    }
    private static List<Object> packFantasy(Map<String,Object> f) {
        return List.of(f.get("contestiOsservati"),f.get("conteggiati"),f.get("conVotoRealeValido"),
            f.get("sommaVotiRealiApportati"),f.get("sommaFantavotiApportati"),
            f.get("panchinaNonConteggiata"),f.get("fuoriLista"),f.get("utilizzoDaVerificare"),
            packEvents(f.get("eventiApportati")),packUnused(f.get("nonUtilizzatoOsservato")));
    }
    @SuppressWarnings("unchecked")
    private static List<Object> packCareer(Map<String,Object> row) {
        Map<String,Object> r=(Map<String,Object>)row.get("reale");
        Map<String,Object> f=(Map<String,Object>)row.get("fantasy");
        return List.of(row.get("playerKey"), row.get("nome"),row.get("ruoloFca"),
            row.get("codiceStorico")==null?0:row.get("codiceStorico"),
            List.of(r.get("votiValidi"),r.get("sommaVoti"),packEvents(r.get("eventi")),
                r.get("giornateGolSubitiConVoto"),r.get("giornateZeroGolSubitiRegistrati"),
                r.get("mediaGolSubitiSuGiornateVerificate")==null?"":r.get("mediaGolSubitiSuGiornateVerificate")),
            packFantasy(f),packCost(row.get("costoAcquisto")));
    }
    @SuppressWarnings("unchecked")
    private static List<Object> packTeam(Map<String,Object> row) {
        Map<String,Object> f=(Map<String,Object>)row.get("apporto");
        return List.of(row.get("playerKey"),row.get("idSquadraInterno"),
            row.get("idCompetizioneInterno"),packFantasy(f),packCost(row.get("costoAcquisto")));
    }
    private static List<Object> packs(String kind,List<Map<String,Object>> rows) {
        if(kind.equals("Careers"))return rows.stream().map(x->(Object)packCareer(x)).toList();
        if(kind.equals("Teams"))return rows.stream().map(x->(Object)packTeam(x)).toList();
        return new ArrayList<>(rows);
    }
    private static Map<String,Object> dictionaries(SeasonView view) throws IOException {
        Map<Integer,String> teams=new TreeMap<>();
        Map<Integer,List<String>> competitions=new TreeMap<>();
        for(var row:view.teams) {
            int t=((Number)row.get("idSquadraInterno")).intValue();
            int comp=((Number)row.get("idCompetizioneInterno")).intValue();
            String tn=(String)row.get("fantasquadra");
            List<String> cn=List.of((String)row.get("competizione"),(String)row.get("famiglia"));
            String old=teams.putIfAbsent(t,tn);
            if(old!=null && !old.equals(tn))throw new IOException("Conflicting team ID "+t);
            List<String> oc=competitions.putIfAbsent(comp,cn);
            if(oc!=null && !oc.equals(cn))throw new IOException("Conflicting competition ID "+comp);
        }
        return obj("squadre",teams.entrySet().stream().map(e->List.of(e.getKey(),e.getValue())).toList(),
            "competizioni",competitions.entrySet().stream().map(e->List.of(e.getKey(),e.getValue().get(0),
                e.getValue().get(1))).toList());
    }

    public static Result export(Connection c,List<String> seasonIds,Path output,Path reports)
            throws SQLException,IOException {
        return export(c, seasonIds, output, reports, Set.of());
    }

    /**
     * selectedIds: catalogo TF01..TF20. In questo overload un insieme vuoto
     * conserva il comportamento v1 per la diagnostica; la pipeline risolve e
     * valida sempre una selezione esplicita non vuota.
     */
    public static Result export(Connection c,List<String> seasonIds,Path output,Path reports,
                                Set<String> selectedIds) throws SQLException,IOException {
        return export(c,seasonIds,output,reports,selectedIds,TopFlopSettings.defaults());
    }

    public static Result export(Connection c,List<String> seasonIds,Path output,Path reports,
                                Set<String> selectedIds,TopFlopSettings settings) throws SQLException,IOException {
        Objects.requireNonNull(settings);
        Objects.requireNonNull(c);Objects.requireNonNull(seasonIds);Objects.requireNonNull(output);
        Objects.requireNonNull(selectedIds);
        for (String id : selectedIds) if (!id.matches("TF(?:0[1-9]|1[0-9]|20)"))
            throw new IllegalArgumentException("Identificativo record sconosciuto: " + id);
        if(seasonIds.isEmpty() || seasonIds.stream().distinct().count()!=seasonIds.size())
            throw new IllegalArgumentException("Empty or duplicate season list");
        for(String s:seasonIds)if(s==null || !s.matches("[0-9]{4}_[0-9]{4}"))
            throw new IllegalArgumentException("Invalid season: "+s);
        if(Files.exists(output))throw new IOException("Staging must be a NEW directory: "+output);
        Files.createDirectories(output);
        List<FileInfo> files=new ArrayList<>();
        List<Map<String,Object>> metadata=new ArrayList<>();
        int players=0;long real=0,fantasy=0;
        for(String season:seasonIds) {
            var loaded=TopFlopJdbcArchiveAssembler.loadSeason(c,season);
            var canonical=reports==null ? Map.<TopFlopCanonicalScorecards.MatchTeam,String>of()
                :TopFlopCanonicalScorecards.load(reports,season);
            SeasonView view=build(loaded,canonical,settings);
            if (!selectedIds.isEmpty()) filterSelected(view, selectedIds);
            players+=view.careers.size();real+=loaded.archive().realDayCount();
            fantasy+=loaded.archive().fantasyFactCount();
            List<FileInfo> seasonFiles=new ArrayList<>();
            seasonFiles.addAll(shard(output,season,"Careers",packs("Careers",view.careers)));
            seasonFiles.addAll(shard(output,season,"Teams",packs("Teams",view.teams)));
            seasonFiles.addAll(shard(output,season,"Records",packs("Records",view.records)));
            seasonFiles.addAll(shard(output,season,"Highlights",packs("Highlights",view.highlights)));
            files.addAll(seasonFiles);
            metadata.add(obj("stagione",season,"contatori",view.counters,"dizionari",dictionaries(view),
                "files",seasonFiles.stream().map(f->obj("nome",f.name(),"tipo",f.kind(),
                    "righe",f.rows(),"byte",f.bytes(),"sha256",f.sha256())).toList()));
            System.out.println("VIEWS_SEASON_OK "+season+" players="+view.careers.size()+
                " fantasy="+loaded.archive().fantasyFactCount()+" files="+seasonFiles.size()+
                " bytes="+seasonFiles.stream().mapToLong(FileInfo::bytes).sum());
        }
        Map<String,Object> status=new LinkedHashMap<>();
        for(int i=1;i<=20;i++) status.put(String.format("TF%02d",i),"ATTIVA");
        status.put("TF05","ATTIVA_SOGLIA_MEDIA_VOTO_STAGIONALE");
        status.put("TF06","ATTIVA_SOGLIA_FANTAMEDIA_PER_SQUADRA_E_COMPETIZIONE");
        for(String id:List.of("TF11","TF12","TF13"))status.put(id,"NON_ATTIVA_ESCLUSIONI_NON_VERIFICATE");
        for(String id:List.of("TF14","TF15"))status.put(id,"NON_ATTIVA_BASELINE_E_SOGLIE_DA_VALIDARE");
        status.put("TF16","CONFRONTO_PER_SQUADRA_SENZA_VINCITORE_ARBITRARIO");
        if (!selectedIds.isEmpty())
            for (String id : new ArrayList<>(status.keySet()))
                if (!selectedIds.contains(id)) status.put(id, "NON_SELEZIONATO");
        Map<String,Object> facade=obj("schemaVersion","4.0-public-views-v1",
            "catalogo","TF01-TF20","status","STAGING_ONLY_NOT_PUBLISHED",
            "limitBytesPerJs",LIMIT_BYTES,"identity","SEASON_LOCAL_NO_AUTO_MERGE",
            "viste",List.of("Careers","Teams","Records","Highlights"),"eventi",EVENTS,
            "soglie",obj("votiMinimiStagione",settings.realSeasonVotes(),
                "gareMinimeCampionato",settings.fantasyLeagueMatches(),
                "gareMinimeCoppe",settings.fantasyCupMatches()),
            "attivazioneRecord",status,"stagioni",metadata);
        byte[] b=("window.fcmRecordsNextTopFlopViews = "+json(facade)+";\n")
            .getBytes(StandardCharsets.UTF_8);
        files.add(write(output,FACADE,"","Facade",1,b));
        return new Result(List.copyOf(files),seasonIds.size(),players,real,fantasy,output);
    }
    @SuppressWarnings("unchecked")
    static void filterSelected(SeasonView view, Set<String> ids) {
        view.records.removeIf(record -> !ids.contains((String)record.get("recordId")));
        Set<String> highlightKeys = new LinkedHashSet<>();
        for (Map<String,Object> record : view.records) {
            for (Map<String,Object> result : (List<Map<String,Object>>)record.get("risultati")) {
                Object key = result.get("highlightKey");
                if (key != null) highlightKeys.add(key.toString());
            }
        }
        // Il portiere goleador merita un highlight anche se non ha un voto estremo.
        if (ids.contains("TF20")) {
            for (Map<String,Object> highlight : view.highlights) {
                Map<String,Object> player=(Map<String,Object>)highlight.get("calciatore");
                if(player == null || !"prestazioneReale".equals(highlight.get("tipo")))continue;
                if(!Integer.valueOf(1).equals(player.get("ruoloFca")))continue;
                Map<String,Object> events=(Map<String,Object>)highlight.get("eventi");
                if(events!=null && (events.containsKey("GOLFATTI1") || events.containsKey("ASSIST")))
                    highlightKeys.add(String.valueOf(highlight.get("key")));
            }
        }
        view.highlights.removeIf(h -> !highlightKeys.contains(h.get("key")));
        view.counters.put("selectedHighlights", view.highlights.size());
        view.counters.put("recordGroups", view.records.size());
    }

    static List<FileInfo> shard(Path dir,String season,String kind,
                                        List<Object> entries) throws IOException {
        String prefix="(function(){var w=window.fcmRecordsNextTopFlopViewsData||(window.fcmRecordsNextTopFlopViewsData={});"+
            "var g=w."+kind+"||(w."+kind+"={});var a=g[\""+season+"\"]||(g[\""+season+"\"]=[]);a.push(...[";
        String suffix="]);})();\n";
        byte[] overhead=(prefix+suffix).getBytes(StandardCharsets.UTF_8);
        List<FileInfo> result=new ArrayList<>();
        StringBuilder current=new StringBuilder(prefix);int used=overhead.length,count=0,part=1;
        for(var entry:entries) {
            String json=json(entry);
            int add=json.getBytes(StandardCharsets.UTF_8).length+(count==0?0:1);
            if(count>0 && used+add>TARGET_BYTES) {
                result.add(write(dir,name(season,kind,part++),season,kind,count,
                    (current.toString()+suffix).getBytes(StandardCharsets.UTF_8)));
                current=new StringBuilder(prefix);used=overhead.length;count=0;add=json.getBytes(StandardCharsets.UTF_8).length;
            }
            if(used+add>LIMIT_BYTES)throw new IOException("Oversize single view row "+season+" "+kind);
            if(count>0)current.append(',');current.append(json);used+=add;count++;
        }
        if(count>0)result.add(write(dir,name(season,kind,part),season,kind,count,
            (current.toString()+suffix).getBytes(StandardCharsets.UTF_8)));
        return result;
    }
    private static String name(String season,String kind,int n) {
        return "fcmRecordsNext_TopFlopViews."+kind+"."+season+"."+String.format("%03d",n)+".js";
    }
    private static FileInfo write(Path dir,String name,String season,String kind,int rows,byte[] bytes)
        throws IOException {
        if(bytes.length>LIMIT_BYTES)throw new IOException("JS size limit exceeded "+name+" "+bytes.length);
        Path target=dir.resolve(name);Files.write(target,bytes,
            java.nio.file.StandardOpenOption.CREATE_NEW);
        if(Files.size(target)!=bytes.length)throw new IOException("JS size changed: "+name);
        String hash;
        try {
            byte[] digest=MessageDigest.getInstance("SHA-256").digest(bytes);
            StringBuilder s=new StringBuilder();for(byte x:digest)s.append(String.format("%02x",x&255));hash=s.toString();
        }catch(Exception ex){throw new IllegalStateException(ex);}
        return new FileInfo(name,season,kind,rows,bytes.length,hash);
    }

    /** CLI, read-only database: db stage reports-or-dash season1 season2 ... */
    public static void main(String[] args) throws Exception {
        if(args.length<4)throw new IllegalArgumentException("<database> <new-stage> <reports-or-dash> <season...>");
        Path db=Path.of(args[0]).toAbsolutePath().normalize();
        if(!Files.isRegularFile(db))throw new IOException("Missing SQLite: "+db);
        Path out=Path.of(args[1]).toAbsolutePath().normalize();
        Path reports=args[2].equals("-")?null:Path.of(args[2]).toAbsolutePath().normalize();
        if(reports!=null && !Files.isDirectory(reports))throw new IOException("Missing reports: "+reports);
        Class.forName("org.sqlite.JDBC");
        try(Connection c=DriverManager.getConnection("jdbc:sqlite:"+db.toUri()+"?mode=ro")) {
            Result r=export(c,List.of(args).subList(3,args.length),out,reports);
            long total=r.files().stream().mapToLong(FileInfo::bytes).sum();
            long maximum=r.files().stream().mapToLong(FileInfo::bytes).max().orElse(0);
            System.out.println("TOP_FLOP_VIEWS_STAGE_OK seasons="+r.seasons()+" players="+r.players()+
                " realDays="+r.sourceRealDays()+" fantasyContexts="+r.sourceFantasyContexts()+
                " files="+r.files().size()+" bytes="+total+" maxBytes="+maximum);
        }
    }
}
~~~~

## `src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopViewsPublication.java`

SHA256: `929dd8c5fba20b15bbbf8eecab22e0a40502649ad585bd6ff3b91dd7136fafcc`

~~~~java
package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.config.MiniJson;
import it.alterlega.recordsnext.app.config.ProcessingConfigWriter;
import it.alterlega.recordsnext.app.model.ProcessingSelection;
import it.alterlega.recordsnext.app.model.RecordFamily;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Adattatore dell'exporter di VISTE approvate alla pipeline 3.2.
 * Nessuna pubblicazione diretta: aggiunge allo staging JS gia' validato.
 * La pipeline 3.2 mantiene la responsabilita' esclusiva di rollback e publish.
 */
public final class TopFlopViewsPublication {
    private TopFlopViewsPublication() {}

    private static final String PREFIX = "window.fcmRecordsNextTopFlopViews = ";
    private static final String BASENAME = "fcmRecordsNext_TopFlopViews.";

    public static Set<String> selectedIds(ProcessingSelection selection) {
        Objects.requireNonNull(selection);
        if (!selection.isFamilyEnabled(RecordFamily.TOP_FLOP)) return Set.of();
        Set<String> chosen = new LinkedHashSet<>();
        for (String child : ProcessingConfigWriter.TOP_FLOP) {
            boolean supported = ProcessingConfigWriter.topFlopImplemented(child);
            if (selection.enabledChildren().contains(child) && !supported) {
                throw new IllegalArgumentException("Top & Flop non ancora verificato: " + child);
            }
            if (supported && (selection.enabledChildren().isEmpty()
                    || selection.enabledChildren().contains(child))) {
                chosen.add(child.substring("topFlop.".length()).toUpperCase(java.util.Locale.ROOT));
            }
        }
        if (chosen.isEmpty()) {
            throw new IllegalArgumentException("Top & Flop attivo senza record disponibili selezionati");
        }
        return Set.copyOf(chosen);
    }

    /** Esegue il calcolo una volta sola, indipendentemente dal numero di siti. */
    public static Prepared prepare(Path sqlite, Path reports, List<String> seasonIds,
                                   Set<String> selectedIds, Path scratch) throws Exception {
        return prepare(sqlite,reports,seasonIds,selectedIds,scratch,TopFlopSettings.defaults());
    }

    public static Prepared prepare(Path sqlite, Path reports, List<String> seasonIds,
                                   Set<String> selectedIds, Path scratch,TopFlopSettings settings) throws Exception {
        return prepare(sqlite,reports,seasonIds,selectedIds,scratch,settings,null);
    }

    public static Prepared prepare(Path sqlite, Path reports, List<String> seasonIds,
                                   Set<String> selectedIds, Path scratch,TopFlopSettings settings,
                                   Path identitiesFile) throws Exception {
        if (selectedIds.isEmpty()) throw new IllegalArgumentException("Selezionare i record TF");
        if (seasonIds.isEmpty()) throw new IllegalArgumentException("Nessuna stagione per Top & Flop");
        if (!Files.isRegularFile(sqlite)) throw new IOException("SQLite mancante: " + sqlite);
        if (reports != null && !Files.isDirectory(reports)) {
            throw new IOException("Report canonici mancanti: " + reports);
        }
        Files.createDirectories(scratch);
        Path output = scratch.resolve("js");
        Class.forName("org.sqlite.JDBC");
        TopFlopPublicViewsStageExporter.Result result;
        try (Connection db = DriverManager.getConnection("jdbc:sqlite:" + sqlite.toUri() + "?mode=ro")) {
            result = TopFlopPublicViewsStageExporter.export(db, seasonIds, output, reports, selectedIds, settings);
        }
        for (var file : result.files()) {
            Path path = output.resolve(file.name());
            validate(path, file.bytes(), file.sha256());
        }
        Map<String,String> identities=identitiesFile==null?Map.of():
            TopFlopPlayerIdentities.load(identitiesFile).approved();
        return new Prepared(output, Set.copyOf(seasonIds), result.files().size(),identities);
    }

    /** Preserva esattamente le stagioni ammesse dalla 3.2 per il singolo sito. */
    public static int attachScoped(Prepared prepared, List<String> scope, Path generatedJs)
            throws Exception {
        Objects.requireNonNull(prepared);
        if (!Files.isDirectory(generatedJs)) throw new IOException("Staging 3.2 mancante: " + generatedJs);
        if (scope.isEmpty()) throw new IllegalArgumentException("Scope Top & Flop vuoto");
        if (!prepared.seasons().containsAll(scope) || scope.stream().distinct().count()!=scope.size()) {
            throw new IOException("Cutoff storico Top & Flop incoerente: " + scope);
        }
        Map<String,Object> facade = loadFacade(prepared.jsDirectory().resolve(
                TopFlopPublicViewsStageExporter.FACADE));
        @SuppressWarnings("unchecked") List<Map<String,Object>> metadata =
                (List<Map<String,Object>>) facade.get("stagioni");
        if(metadata==null)throw new IOException("Metadati Top & Flop mancanti");
        Map<String,Map<String,Object>> bySeason = new LinkedHashMap<>();
        for (Map<String,Object> m : metadata) {
            String season=String.valueOf(m.get("stagione"));
            if(bySeason.putIfAbsent(season,m)!=null)throw new IOException("Doppia stagione: "+season);
        }
        List<Map<String,Object>> scopedMetadata = new ArrayList<>();
        List<Path> sources = new ArrayList<>();
        for(String season:scope) {
            Map<String,Object> row=bySeason.get(season);
            if(row==null)throw new IOException("Stagione non presente nell'indice TF: "+season);
            scopedMetadata.add(row);
            @SuppressWarnings("unchecked") List<Map<String,Object>> files=
                    (List<Map<String,Object>>)row.get("files");
            if(files==null)throw new IOException("Elenco file mancante per "+season);
            for(var file:files) {
                String name=String.valueOf(file.get("nome"));
                if(!name.matches("fcmRecordsNext_TopFlopViews\\.(?:Careers|Teams|Records|Highlights)\\."
                    +season+"\\.[0-9]{3}\\.js")) {
                    throw new IOException("Nome shard TF non valido: "+name);
                }
                Path src=prepared.jsDirectory().resolve(name);
                validate(src,((Number)file.get("byte")).longValue(),String.valueOf(file.get("sha256")));
                sources.add(src);
            }
        }
        if(sources.stream().map(p->p.getFileName().toString()).distinct().count()!=sources.size())
            throw new IOException("Shard Top & Flop duplicati");
        Map<String,Object> scoped = new LinkedHashMap<>(facade);
        scoped.put("stagioni",scopedMetadata);
        scoped.put("status","GENERATED_FOR_SITE_SCOPE");
        scoped.put("identity","SEASON_LOCAL_WITH_EXPLICITLY_APPROVED_HISTORIC_LINKS");
        // Historic career totals are computed in Java for THIS site's cutoff,
        // using only identities explicitly approved in the GUI. Never expose
        // future seasons to an older site, and never merge by CodiceGazza.
        List<Map<String,Object>> history = TopFlopHistoricalViews.aggregate(
            TopFlopHistoricalViews.readCareers(prepared.jsDirectory(),sources),
            prepared.approvedIdentities());
        List<TopFlopPublicViewsStageExporter.FileInfo> historyFiles=history.isEmpty()
            ?List.of():TopFlopPublicViewsStageExporter.shard(generatedJs,"Storico","History",
                new ArrayList<Object>(history));
        if(!historyFiles.isEmpty()) {
            scoped.put("viste",List.of("Careers","Teams","Records","Highlights","History"));
        }
        scoped.put("storico",Map.of("status",history.isEmpty()?"NESSUNA_IDENTITA_APPROVATA_NEL_CUTOFF":"IDENTITA_APPROVATE",
            "identita",history.size(),"file",historyFiles.stream().map(f->Map.of(
                "nome",f.name(),"tipo",f.kind(),"righe",f.rows(),"byte",f.bytes(),"sha256",f.sha256())).toList(),
            "fonte","config/player-identities.json"));
        byte[] facadeJs=(PREFIX+TopFlopCompactStageJsExporter.Json.write(scoped)+";\n")
                .getBytes(StandardCharsets.UTF_8);
        if(facadeJs.length>TopFlopPublicViewsStageExporter.LIMIT_BYTES)
            throw new IOException("Facade Top & Flop troppo grande: "+facadeJs.length);
        Path targetFacade = generatedJs.resolve(TopFlopPublicViewsStageExporter.FACADE);
        if(Files.exists(targetFacade))throw new IOException("Facade TF gia' presente: "+targetFacade);
        for (Path source:sources) {
            Path target=generatedJs.resolve(source.getFileName());
            if(Files.exists(target))throw new IOException("Collisione JS TF: "+target);
            Files.copy(source,target);
        }
        Files.write(targetFacade,facadeJs);
        return sources.size()+historyFiles.size()+1;
    }

    /** Controlla tutti i JS dello staging prima che parta il publish 3.2. */
    public static int validateStaging(Path staging) throws IOException {
        int count=0;
        try (var files=Files.list(staging)) {
            for(Path file:files.filter(Files::isRegularFile).toList()) {
                if(!file.getFileName().toString().endsWith(".js"))continue;
                long bytes=Files.size(file);
                if(bytes>TopFlopPublicViewsStageExporter.LIMIT_BYTES)
                    throw new IOException("JS oltre 1.500.000 byte: "+file.getFileName()+"="+bytes);
                if(bytes==0)throw new IOException("JS vuoto: "+file.getFileName());
                count++;
            }
        }
        return count;
    }

    @SuppressWarnings("unchecked")
    private static Map<String,Object> loadFacade(Path file) throws IOException {
        String content=Files.readString(file, StandardCharsets.UTF_8).strip();
        if(!content.startsWith(PREFIX) || !content.endsWith(";"))
            throw new IOException("Facade Top & Flop non canonica");
        Object parsed=MiniJson.parse(content.substring(PREFIX.length(),content.length()-1));
        if(!(parsed instanceof Map<?,?>))throw new IOException("Facade TF invalida");
        return (Map<String,Object>)parsed;
    }

    private static void validate(Path file,long size,String expectedHash) throws Exception {
        if(!Files.isRegularFile(file) || Files.size(file)!=size || size<=0
                || size>TopFlopPublicViewsStageExporter.LIMIT_BYTES)
            throw new IOException("Dimensione JS incoerente: "+file);
        byte[] hash=MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file));
        StringBuilder hex=new StringBuilder();
        for(byte b:hash)hex.append(String.format("%02x",b&255));
        if(!hex.toString().equalsIgnoreCase(expectedHash))
            throw new IOException("Hash JS differente: "+file);
    }

    public record Prepared(Path jsDirectory,Set<String> seasons,int files,
                           Map<String,String> approvedIdentities) {
        public Prepared {
            approvedIdentities=Map.copyOf(approvedIdentities);
        }
    }
}
~~~~

## `src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopPlayerIdentities.java`

SHA256: `93ad57678cbab66389e2504f344691a907b4346e74b9d98adf697ef7db962a68`

~~~~java
package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.config.MiniJson;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Explicitly approved player identities. Only player IDs and decisions are stored here;
 * the original FCA/FCM IDs, names and rows are never rewritten. In particular, the
 * Gazzetta code is a hint, not a unique person identifier.
 */
public final class TopFlopPlayerIdentities {
    private TopFlopPlayerIdentities() {}
    public static final String FILE_NAME = "player-identities.json";

    public record Source(String season, int id, Integer code, String name, String birth) {
        public String key() { return season + ":" + id; }
    }
    public record Proposal(String fingerprint, List<Source> members, boolean safe, String reason) {
        public String description() {
            return members.stream().map(s -> s.season() + " " + s.name() + " (#" + s.code() + ")")
                    .collect(Collectors.joining(" | "));
        }
    }
    public record Collision(String fingerprint, int code, List<Source> members) {
        public String description() {
            return "Codice " + code + ": " + members.stream()
                    .map(s -> s.season() + " " + s.name())
                    .collect(Collectors.joining(" | "));
        }
    }
    public record Audit(List<Proposal> proposals, List<Collision> collisions, int sources) {}

    public static final class Registry {
        private final Map<String,String> approved = new LinkedHashMap<>();
        private final Set<String> ignored = new LinkedHashSet<>();
        private final Set<String> distinct = new LinkedHashSet<>();
        public Map<String,String> approved() {return Map.copyOf(approved);}
        public boolean isIgnored(Proposal p) {return ignored.contains(p.fingerprint());}
        public boolean isDistinct(Collision c) {return distinct.contains(c.fingerprint());}
        public boolean isMerged(Collision c) {
            String id=null;
            for(Source source:c.members()) {
                String assigned=approved.get(source.key());
                if(assigned==null)return false;
                if(id!=null&&!id.equals(assigned))return false;
                id=assigned;
            }
            return true;
        }
        public void approveSame(Collision collision) {
            if(isDistinct(collision))distinct.remove(collision.fingerprint());
            approve(new Proposal(collision.fingerprint(),collision.members(),false,
                "Stesso calciatore, approvato manualmente"));
        }
        public void undo(Proposal proposal) {
            proposal.members().forEach(s->approved.remove(s.key()));
            ignored.remove(proposal.fingerprint());
        }
        public void undo(Collision collision) {
            distinct.remove(collision.fingerprint());
            collision.members().forEach(s->approved.remove(s.key()));
        }
        public int approvedPlayers() {return approved.size();}
        public int approvedIdentities() {return new HashSet<>(approved.values()).size();}
        public void ignore(Proposal p) {ignored.add(p.fingerprint());}
        public void distinguish(Collision c) {distinct.add(c.fingerprint());}
        public void approve(Proposal p) {
            if(p.members().size()<2)throw new IllegalArgumentException("Gruppo non storico");
            if(p.members().stream().map(Source::season).distinct().count()!=p.members().size())
                throw new IllegalArgumentException("Identita' diverse nella stessa stagione: " + p.description());
            String chosen=null;
            for(Source s:p.members()) {
                String old=approved.get(s.key());
                if(old!=null) {
                    if(chosen!=null && !chosen.equals(old))
                        throw new IllegalArgumentException("Associazioni approvate in conflitto: " + p.description());
                    chosen=old;
                }
            }
            if(chosen==null) chosen="rn4p-"+p.members().stream().map(Source::key).sorted().findFirst().orElseThrow().replace(':','-');
            for(Source s:p.members())approved.put(s.key(),chosen);
            ignored.remove(p.fingerprint());
        }
        public boolean complete(Proposal p) {
            String id=null;
            for(Source s:p.members()) {
                String assigned=approved.get(s.key());
                if(assigned==null)return false;
                if(id!=null && !id.equals(assigned))return false;
                id=assigned;
            }
            return true;
        }
        /** Expired decisions are not auto-reapproved; new evidence is a new fingerprint. */
        public boolean requiresReview(Audit audit) {
            return audit.proposals().stream().anyMatch(p->!complete(p)&&!isIgnored(p))
                 || audit.collisions().stream().anyMatch(c->!isDistinct(c)&&!isMerged(c));
        }
    }

    public static Registry load(Path file) throws IOException {
        Registry r=new Registry();
        if(!Files.isRegularFile(file))return r;
        Object parsed;
        try {parsed=MiniJson.parse(Files.readString(file,StandardCharsets.UTF_8));}
        catch(RuntimeException ex){throw new IOException("Identita' storiche: JSON non valido",ex);}
        if(!(parsed instanceof Map<?,?> map) || !"rn4-player-identities-v1".equals(map.get("schemaVersion")))
            throw new IOException("Schema non riconosciuto per " + file);
        Object entries=map.get("approved");
        if(!(entries instanceof Map<?,?> m))throw new IOException("Mappa approved mancante");
        for(var entry:m.entrySet()) {
            String key=String.valueOf(entry.getKey()), id=String.valueOf(entry.getValue());
            if(!key.matches("[0-9]{4}_[0-9]{4}:[0-9]+") || !id.matches("rn4p-[0-9]{4}_[0-9]{4}-[0-9]+"))
                throw new IOException("Identita' non valida: " + key + "/" + id);
            r.approved.put(key,id);
        }
        readFingerprints(map.get("ignored"),r.ignored);
        readFingerprints(map.get("distinct"),r.distinct);
        return r;
    }
    private static void readFingerprints(Object raw,Set<String> out) throws IOException {
        if(raw==null)return;
        if(!(raw instanceof List<?> list))throw new IOException("Elenco fingerprint invalido");
        for(Object x:list){String s=String.valueOf(x);if(!s.matches("[0-9a-f]{64}"))throw new IOException("Fingerprint invalido");out.add(s);}
    }
    public static void save(Path file,Registry r) throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        Map<String,Object> data=new LinkedHashMap<>();
        data.put("schemaVersion","rn4-player-identities-v1");
        data.put("approved",new TreeMap<>(r.approved));
        data.put("ignored",r.ignored.stream().sorted().toList());
        data.put("distinct",r.distinct.stream().sorted().toList());
        String json=TopFlopCompactStageJsExporter.Json.write(data)+"\n";
        Path tmp=Files.createTempFile(file.getParent(),".player-identities-",".tmp");
        try{
            Files.writeString(tmp,json,StandardCharsets.UTF_8);
            try{Files.move(tmp,file,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}
            catch(java.nio.file.AtomicMoveNotSupportedException ex){Files.move(tmp,file,StandardCopyOption.REPLACE_EXISTING);}
        }finally{Files.deleteIfExists(tmp);}
    }

    /** All imported FCA seasons in the current canonical database. */
    public static Audit scanAvailable(Connection c) throws SQLException {
        List<String> seasons=new ArrayList<>();
        try(Statement st=c.createStatement();ResultSet rows=st.executeQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name LIKE 'raw_%_fca_giocatorea'")) {
            while(rows.next()) {
                String name=rows.getString(1);
                if(name.matches("raw_[0-9]{4}_[0-9]{4}_fca_giocatorea"))
                    seasons.add(name.substring(4,13));
            }
        }
        seasons.sort(String::compareTo);
        return scan(c,seasons);
    }

    public static Audit scan(Connection c,List<String> seasons) throws SQLException {
        List<Source> all=new ArrayList<>();
        for(String season:seasons) {
            if(!season.matches("[0-9]{4}_[0-9]{4}"))throw new IllegalArgumentException("Stagione invalida");
            String table="raw_"+season+"_fca_giocatorea";
            String sql="SELECT ID,CODICEGAZZA,NOME,DATADINASCITA FROM \""+table+"\" ORDER BY ID";
            try(Statement st=c.createStatement();ResultSet rs=st.executeQuery(sql)) {
                while(rs.next()) {
                    int code=rs.getInt(2);
                    all.add(new Source(season,rs.getInt(1),rs.wasNull()||code<=0?null:code,
                        rs.getString(3),rs.getString(4)));
                }
            }
        }
        // Candidate links are deliberately conservative. Same Gazzetta code with
        // different names and birth dates is NOT enough to join two people.
        Map<String,List<Source>> byCodeName=new LinkedHashMap<>();
        Map<String,List<Source>> byBirthName=new LinkedHashMap<>();
        Map<Integer,List<Source>> byCode=new TreeMap<>();
        for(Source s:all) {
            String n=normalize(s.name());
            if(s.code()!=null) {
                byCode.computeIfAbsent(s.code(),x->new ArrayList<>()).add(s);
                byCodeName.computeIfAbsent(s.code()+"/"+n,x->new ArrayList<>()).add(s);
            }
            String birth=dob(s.birth());
            if(birth!=null && !n.isEmpty()) {
                // Keep surname + first given name; do not merge common surnames
                // on a birthday alone, nor invent reversed-name permutations.
                String[] tokens=n.split(" ");
                if(tokens.length>=2) byBirthName.computeIfAbsent(birth+"/"+tokens[0]+"/"+tokens[1],x->new ArrayList<>()).add(s);
            }
        }
        Map<String,String> parents=new HashMap<>();
        Map<String,Set<String>> rootSeasons=new HashMap<>();
        for(Source s:all){parents.put(s.key(),s.key());rootSeasons.put(s.key(),new HashSet<>(Set.of(s.season())));}
        for(List<Source> group:byCodeName.values())unionGroup(group,parents,rootSeasons);
        for(List<Source> group:byBirthName.values())unionGroup(group,parents,rootSeasons);
        Map<String,List<Source>> grouped=new TreeMap<>();
        for(Source s:all)grouped.computeIfAbsent(find(parents,s.key()),x->new ArrayList<>()).add(s);
        Set<Integer> disputedCodes=new HashSet<>();
        List<Collision> collisions=new ArrayList<>();
        for(var e:byCode.entrySet()) {
            List<Source> members=e.getValue();
            if(members.stream().map(s->find(parents,s.key())).distinct().count()>1 &&
                    members.stream().map(s->normalize(s.name())).distinct().count()>1) {
                disputedCodes.add(e.getKey());
                collisions.add(new Collision(fingerprint(members),e.getKey(),List.copyOf(members)));
            }
        }
        List<Proposal> proposals=new ArrayList<>();
        for(List<Source> group:grouped.values()) {
            if(group.size()<2)continue;
            group.sort(Comparator.comparing(Source::key));
            long seasonsCount=group.stream().map(Source::season).distinct().count();
            boolean birthConflict=group.stream().map(s->dob(s.birth())).filter(x->x!=null).distinct().count()>1;
            boolean codeCollision=group.stream().anyMatch(s->s.code()!=null&&disputedCodes.contains(s.code()));
            boolean namesEqual=group.stream().map(s->normalize(s.name())).distinct().count()==1;
            boolean safe=seasonsCount==group.size()&&!birthConflict&&!codeCollision&&namesEqual;
            String reason=birthConflict?"Date di nascita discordanti":
                (seasonsCount<group.size()?"Piu' persone nella stessa stagione":
                (codeCollision?"Codice storico riutilizzato":"Nomi/codici da verificare"));
            proposals.add(new Proposal(fingerprint(group),List.copyOf(group),safe,safe?"Corrispondenza esatta":reason));
        }
        proposals.sort(Comparator.comparing((Proposal p)->p.safe()?1:0)
            .thenComparing(p->p.members().get(0).name(),String.CASE_INSENSITIVE_ORDER));
        return new Audit(List.copyOf(proposals),List.copyOf(collisions),all.size());
    }
    private static void unionGroup(List<Source> group,Map<String,String> p,Map<String,Set<String>> roots) {
        if(group.size()<2)return;
        String first=group.get(0).key();
        for(int i=1;i<group.size();i++) {
            String other=group.get(i).key();
            // No two different player records in the same season may be joined.
            if(group.get(i).season().equals(group.get(0).season()))continue;
            String a=find(p,first),b=find(p,other);
            if(a.equals(b))continue;
            Set<String> firstSeasons=roots.get(a), secondSeasons=roots.get(b);
            boolean conflict=secondSeasons.stream().anyMatch(firstSeasons::contains);
            if(!conflict){p.put(b,a);firstSeasons.addAll(secondSeasons);roots.remove(b);}
        }
    }
    private static String find(Map<String,String> p,String key) {
        String parent=p.get(key);if(parent.equals(key))return key;
        String root=find(p,parent);p.put(key,root);return root;
    }
    private static String normalize(String raw) {
        if(raw==null)return "";
        return Normalizer.normalize(raw,Normalizer.Form.NFD).replaceAll("\\p{M}","")
            .toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+"," ").trim().replaceAll(" +"," ");
    }
    private static String dob(String raw) {
        if(raw==null||raw.length()<10||!raw.substring(0,10).matches("[0-9]{4}-[0-9]{2}-[0-9]{2}"))return null;
        return raw.substring(0,10);
    }
    private static String fingerprint(Collection<Source> src) {
        try {
            String evidence=src.stream().sorted(Comparator.comparing(Source::key))
                .map(s->s.key()+"/"+s.code()+"/"+s.name()+"/"+s.birth()).collect(Collectors.joining("|"));
            byte[] hash=MessageDigest.getInstance("SHA-256").digest(evidence.getBytes(StandardCharsets.UTF_8));
            StringBuilder h=new StringBuilder();for(byte b:hash)h.append(String.format("%02x",b&255));return h.toString();
        }catch(Exception e){throw new IllegalStateException(e);}
    }
}
~~~~

## `src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopHistoricalViews.java`

SHA256: `c56a8389f911afb43e1b1a30725096f9ea4503e03aa7789b7a3416ceb09c324c`

~~~~java
package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.config.MiniJson;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Historical real-performance summaries over explicitly approved identities.
 *  Build once for each site's 3.2 cutoff; never merge by a raw code or name.
 *  Fantasy per-team/per-competition details remain in the season Teams shards.
 */
public final class TopFlopHistoricalViews {
    private TopFlopHistoricalViews() {}
    private static final MathContext MC=MathContext.DECIMAL64;
    private static final String SHARD_PREFIX="a.push(...[";
    private static final List<String> EVENTS=List.of("GOLFATTI1","GOLFATTISURIGORE1","ASSIST",
        "GOLSUBITI","GOLSUBITISURIGORE","RIGPAR","RIGSBA","AUTOGOL1","AMM","ESP");

    static final class Career {
        final String identityId;
        final List<Map<String,Object>> seasons=new ArrayList<>();
        BigDecimal votes=BigDecimal.ZERO;
        int valid=0;
        final Map<String,BigDecimal> events=new LinkedHashMap<>();
        Career(String id){identityId=id;}
        void add(Map<String,Object> row) {
            Map<String,Object> real=asObject(row.get("reale"));
            int validSeason=asInt(real.get("votiValidi"));
            BigDecimal sum=asDecimal(real.get("sommaVoti"));
            if(validSeason<0 || sum.signum()<0)throw new IllegalArgumentException("Voti reali invalidi");
            valid+=validSeason;votes=votes.add(sum);
            Map<String,Object> srcEvents=asObject(real.get("eventi"));
            for(var e:srcEvents.entrySet())events.merge(e.getKey(),asDecimal(e.getValue()),BigDecimal::add);
            String key=String.valueOf(row.get("playerKey"));
            String season=key.length()>=9?key.substring(0,9):"";
            if(!season.matches("[0-9]{4}_[0-9]{4}") ||
                (row.get("stagione")!=null && !season.equals(String.valueOf(row.get("stagione")))))
                throw new IllegalArgumentException("Stagione incoerente: " + key);
            seasons.add(Map.of("stagione",season,
                "playerKey",key,
                "nome",String.valueOf(row.get("nome")),
                "votiValidi",validSeason,"sommaVoti",sum,
                "mediaVoto",validSeason==0?BigDecimal.ZERO:sum.divide(BigDecimal.valueOf(validSeason),MC),
                "eventi",srcEvents));
        }
        Map<String,Object> view() {
            Map<String,Object> result=new LinkedHashMap<>();
            result.put("playerIdentityId",identityId);
            result.put("identitaStoricaVerificata",true);
            result.put("stagioni",seasons);
            result.put("numeroStagioni",seasons.size());
            result.put("votiRealiValidi",valid);
            result.put("mediaVotoReale",valid==0?null:votes.divide(BigDecimal.valueOf(valid),MC));
            result.put("eventiReali",events);
            result.put("gol",events.getOrDefault("GOLFATTI1",BigDecimal.ZERO));
            result.put("assist",events.getOrDefault("ASSIST",BigDecimal.ZERO));
            result.put("ammonizioni",events.getOrDefault("AMM",BigDecimal.ZERO));
            result.put("espulsioni",events.getOrDefault("ESP",BigDecimal.ZERO));
            result.put("autogol",events.getOrDefault("AUTOGOL1",BigDecimal.ZERO));
            result.put("rigoriSbagliati",events.getOrDefault("RIGSBA",BigDecimal.ZERO));
            result.put("golSubiti",events.getOrDefault("GOLSUBITI",BigDecimal.ZERO));
            result.put("rigoriParati",events.getOrDefault("RIGPAR",BigDecimal.ZERO));
            result.put("fantamediaReale",null); // FCA does not supply a standalone FCM scoring total.
            return result;
        }
    }
    public static List<Map<String,Object>> aggregate(List<Map<String,Object>> seasonCareers,
                                                       Map<String,String> approved) {
        Objects.requireNonNull(seasonCareers);Objects.requireNonNull(approved);
        Map<String,Career> byIdentity=new LinkedHashMap<>();
        Map<String,String> seenKeys=new LinkedHashMap<>();
        for(Map<String,Object> row:seasonCareers) {
            String key=String.valueOf(row.get("playerKey"));
            String identity=approved.get(key);
            if(identity==null)continue; // Never guess identity by raw code or name.
            if(seenKeys.putIfAbsent(key,identity)!=null)
                throw new IllegalArgumentException("Doppia anagrafica nello stesso cutoff: " + key);
            byIdentity.computeIfAbsent(identity,Career::new).add(row);
        }
        List<Map<String,Object>> out=new ArrayList<>();
        for(Career c:byIdentity.values()) {
            c.seasons.sort(Comparator.comparing(x->String.valueOf(x.get("stagione"))));
            if(c.seasons.stream().map(s->s.get("stagione")).distinct().count()!=c.seasons.size())
                throw new IllegalArgumentException("Identita' approvata compare due volte nella stessa stagione: " + c.identityId);
            // One-season identities remain in Careers; historical view requires >=2 seasons.
            if(c.seasons.size()>1)out.add(c.view());
        }
        out.sort(Comparator.comparing(x->String.valueOf(x.get("playerIdentityId"))));
        return List.copyOf(out);
    }

    /** Read canonical shards rather than independently querying/guessing from FCA. */
    public static List<Map<String,Object>> readCareers(Path directory,List<Path> files) throws IOException {
        List<Map<String,Object>> out=new ArrayList<>();
        for(Path source:files) {
            if(!source.getFileName().toString().contains(".Careers."))continue;
            if(!source.normalize().getParent().equals(directory.normalize()))throw new IOException("Shard fuori dalla directory");
            String js=Files.readString(source,StandardCharsets.UTF_8);
            int begin=js.indexOf(SHARD_PREFIX),end=js.lastIndexOf("]);})();");
            if(begin<0 || end<begin)throw new IOException("Formato shard non riconosciuto: " + source);
            String body=js.substring(begin+SHARD_PREFIX.length(),end);
            Object parsed=MiniJson.parse("["+body+"]");
            if(!(parsed instanceof List<?> rows))throw new IOException("Shard non-array: " + source);
            for(Object raw:rows)out.add(decodeCareer(raw));
        }
        return out;
    }
    private static Map<String,Object> decodeCareer(Object raw) {
        if(raw instanceof Map<?,?>)return asObject(raw); // useful for controlled test fixtures.
        if(!(raw instanceof List<?> r) || (r.size()!=6 && r.size()!=7))
            throw new IllegalArgumentException("Career compatta non valida: "+raw);
        if(!(r.get(4) instanceof List<?> real) || real.size()!=6)
            throw new IllegalArgumentException("Aggregato reale non valido");
        Map<String,Object> events=new LinkedHashMap<>();
        if(!(real.get(2) instanceof List<?> pairs) || pairs.size()%2!=0)
            throw new IllegalArgumentException("Eventi reali non validi");
        for(int i=0;i<pairs.size();i+=2) {
            int index=((Number)pairs.get(i)).intValue();
            if(index<0||index>=EVENTS.size()||events.putIfAbsent(EVENTS.get(index),pairs.get(i+1))!=null)
                throw new IllegalArgumentException("Codice evento duplicato o fuori range");
        }
        Map<String,Object> row=new LinkedHashMap<>();
        row.put("playerKey",r.get(0));row.put("nome",r.get(1));
        row.put("reale",Map.of("votiValidi",real.get(0),"sommaVoti",real.get(1),"eventi",events));
        return row;
    }
    @SuppressWarnings("unchecked")
    private static Map<String,Object> asObject(Object raw) {
        if(!(raw instanceof Map<?,?> m))throw new IllegalArgumentException("Formato non-oggetto: " + raw);
        return (Map<String,Object>)m;
    }
    private static int asInt(Object raw){if(!(raw instanceof Number n))throw new IllegalArgumentException("Contatore mancante");return n.intValue();}
    private static BigDecimal asDecimal(Object raw){if(raw==null)return BigDecimal.ZERO;return new BigDecimal(raw.toString());}
}
~~~~

## `visualizzatori/js/fcmRecordsNextTopFlopViewsDecoder-v1.js`

SHA256: `b47ac66fbcffbce37d7021a0dcc984570365a2c8f5199ae8f4559d33e047037f`

~~~~javascript
/* RecordsNext 4.0 - public Top & Flop VIEWS v1.
 * Only selected records and aggregated careers/teams; no complete FCM/FCA dump.
 * Lazy loading: do NOT preload all years or sections. Caller applies historical cutoff.
 */
(function (root) {
  'use strict';
  var pending = Object.create(null);
  function facade() {
    var f = root.fcmRecordsNextTopFlopViews;
    if (!f || f.schemaVersion !== '4.0-public-views-v1') {
      throw new Error('RecordsNext TopFlop: incompatible views facade');
    }
    return f;
  }
  function meta(season) {
    var found = facade().stagioni.find(function (s) { return s.stagione === season; });
    if (!found) throw new Error('Unknown season (or past cutoff): ' + season);
    return found;
  }
  function pairs(vector) {
    var keys = facade().eventi, out = {};
    if (!Array.isArray(vector) || vector.length % 2 !== 0) throw new Error('Invalid event vector');
    for (var i = 0; i < vector.length; i += 2) {
      if (!Number.isInteger(vector[i]) || !keys[vector[i]] || Object.hasOwn(out,keys[vector[i]]))
        throw new Error('Invalid event index');
      out[keys[vector[i]]] = vector[i + 1];
    }
    return out;
  }
  function average(sum,n) { return n > 0 ? sum / n : null; }
  function cost(row) {
    if (!row) return null;
    if (!Array.isArray(row) || row.length !== 4) throw new Error('Invalid cost vector');
    return {registrazioniConCosto:row[0],minimo:row[1] === ''?null:row[1],
      massimo:row[2] === ''?null:row[2],medio:row[3] === ''?null:row[3],
      origine:'FCM.Tesserato.COSTOACQ'};
  }
  function unused(row) {
    if (!row) return null;
    if (!Array.isArray(row) || row.length !== 5) throw new Error('Invalid unused vector');
    return {gareConVotoReale:row[0],sommaVotiReali:row[1],
      mediaVotoReale:average(row[1],row[0]),gareConFantavotoNumerico:row[2],
      sommaFantavotiTabellino:row[3],fantamediaTabellino:average(row[3],row[2]),
      eventiRealiNonApportati:pairs(row[4]),
      origine:'FCM_FORMAZIONE_NON_CONTEGGIATA_E_VOTO_FCA'};
  }
  function decodeCareer(row) {
    if (!Array.isArray(row) || (row.length !== 6 && row.length !== 7)) throw new Error('Invalid career row');
    var r = row[4], f = row[5];
    if (r.length !== 6 || (f.length !== 9 && f.length !== 10)) throw new Error('Invalid aggregates');
    return {
      playerKey:row[0],nome:row[1],ruoloFca:row[2],
      codiceStorico:row[3] === 0 ? null : row[3],identitaStoricaVerificata:false,
      reale:{votiValidi:r[0],sommaVoti:r[1],mediaVoto:average(r[1],r[0]),
        eventi:pairs(r[2]),giornateGolSubitiConVoto:r[3],
        giornateZeroGolSubitiRegistrati:r[4],
        mediaGolSubitiSuGiornateVerificate:r[5] === '' ? null : r[5]},
      fantasy:{contestiOsservati:f[0],conteggiati:f[1],conVotoRealeValido:f[2],
        sommaVotiRealiApportati:f[3],mediaVotoRealeApportato:average(f[3],f[2]),
        sommaFantavotiApportati:f[4],fantamediaApportata:average(f[4],f[1]),
        panchinaNonConteggiata:f[5],fuoriLista:f[6],utilizzoDaVerificare:f[7],
        eventiApportati:pairs(f[8]),nonUtilizzatoOsservato:unused(f[9])},
      costoAcquisto:cost(row[6])
    };
  }
  function dict(rows) {
    var obj=Object.create(null);
    (rows || []).forEach(function (r) {
      if (obj[r[0]] !== undefined) throw new Error('Duplicate dictionary key');
      obj[r[0]] = r;
    });
    return obj;
  }
  function decodeTeam(row, season) {
    if (!Array.isArray(row) || (row.length !== 4 && row.length !== 5) ||
      (row[3].length !== 9 && row[3].length !== 10))
      throw new Error('Invalid team row');
    var md=meta(season), ts=dict(md.dizionari.squadre), cs=dict(md.dizionari.competizioni),f=row[3];
    if (!ts[row[1]] || !cs[row[2]]) throw new Error('Missing team/competition dictionary');
    return {playerKey:row[0],idSquadraInterno:row[1],fantasquadra:ts[row[1]][1],
      idCompetizioneInterno:row[2],competizione:cs[row[2]][1],famiglia:cs[row[2]][2],
      apporto:{contestiOsservati:f[0],conteggiati:f[1],conVotoRealeValido:f[2],
        sommaVotiRealiApportati:f[3],mediaVotoRealeApportato:average(f[3],f[2]),
        sommaFantavotiApportati:f[4],fantamediaApportata:average(f[4],f[1]),
        panchinaNonConteggiata:f[5],fuoriLista:f[6],utilizzoDaVerificare:f[7],
        eventiApportati:pairs(f[8]),nonUtilizzatoOsservato:unused(f[9])},
      costoAcquisto:cost(row[4])};
  }
  function store() {
    return root.fcmRecordsNextTopFlopViewsData || (root.fcmRecordsNextTopFlopViewsData={});
  }
  function loadScript(url) {
    if (pending[url]) return pending[url];
    pending[url] = new Promise(function (resolve,reject) {
      var script=document.createElement('script');script.async=true;script.src=url;
      script.onload=resolve;
      script.onerror=function () {delete pending[url];reject(new Error('Failed to load '+url));};
      document.head.appendChild(script);
    });
    return pending[url];
  }
  function baseUrl(base,name) {return String(base || '.').replace(/\/+$/,'')+'/'+name;}
  function loadFacade(base) {
    if (root.fcmRecordsNextTopFlopViews) return Promise.resolve(facade());
    return loadScript(baseUrl(base,'fcmRecordsNext_TopFlopViews.js')).then(facade);
  }
  function loadView(base,season,kind) {
    if (!['Careers','Teams','Records','Highlights'].includes(kind))
      return Promise.reject(new Error('Unknown view section'));
    return loadFacade(base).then(function () {
      var files=meta(season).files.filter(function (f) {return f.tipo === kind;});
      return Promise.all(files.map(function (f) {return loadScript(baseUrl(base,f.nome));})).then(function () {
        var data=(store()[kind] || {})[season] || [];
        if (data.length !== files.reduce(function (n,f) {return n+f.righe;},0))
          throw new Error('Incomplete '+kind+' shard coverage for '+season);
        if (kind === 'Careers') return data.map(decodeCareer);
        if (kind === 'Teams') return data.map(function (r) {return decodeTeam(r,season);});
        return data;
      });
    });
  }
  function loadHistory(base) {
    return loadFacade(base).then(function(f) {
      var meta=f.storico;
      if(!meta || !meta.file || !meta.file.length) return [];
      return Promise.all(meta.file.map(function(x){return loadScript(baseUrl(base,x.nome));}))
        .then(function(){
          var data=(store().History || {}).Storico || [];
          if(data.length!==meta.file.reduce(function(n,x){return n+x.righe;},0))
            throw new Error('Incomplete historical identity shards');
          return data;
        });
    });
  }
  root.RecordsNextTopFlopViewsV1 = Object.freeze({
    facade:facade,meta:meta,pairs:pairs,
    decodeCareer:decodeCareer,decodeTeam:decodeTeam,
    loadFacade:loadFacade,loadView:loadView,loadHistory:loadHistory
  });
})(typeof window !== 'undefined' ? window : globalThis);
~~~~

## `src/test/java/it/alterlega/recordsnext/app/TopFlopGuiPanelSmokeTest.java`

SHA256: `c741d83ab491c051dda7bc4b0850274f538b04638fe3d32216acd3a80970b956`

~~~~java
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
~~~~

## `src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopHistoricIdentitySmokeTest.java`

SHA256: `2ce5285d30e2bb77b477f2c9e1b76b8c49727e108309ce9aef102a40fa168740`

~~~~java
package it.alterlega.recordsnext.app.topflop;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

/** No JDBC dependency: approved identities, collisions, cutoff, weighted averages. */
public final class TopFlopHistoricIdentitySmokeTest {
    static void ok(boolean yes,String why){if(!yes)throw new AssertionError(why);}
    static Map<String,Object> row(String key,int n,int votes,int gol) {
        return Map.of("playerKey",key,"nome","Test player", "reale",
            Map.of("votiValidi",n,"sommaVoti",votes,"eventi",Map.of("GOLFATTI1",gol)));
    }
    public static void main(String[] args)throws Exception {
        var src=List.of(row("2014_2015:4",10,60,2),row("2015_2016:9",20,140,5),
                        row("2016_2017:2",5,10,1),row("2015_2016:15",10,60,0));
        var missing=TopFlopHistoricalViews.aggregate(src,Map.of());
        ok(missing.isEmpty(),"missing approval must NOT merge");
        var approval=Map.of("2014_2015:4","rn4p-2014_2015-4",
            "2015_2016:9","rn4p-2014_2015-4", "2016_2017:2","rn4p-2016_2017-2");
        var grouped=TopFlopHistoricalViews.aggregate(src,approval);
        ok(grouped.size()==1,"only approved 2-season identity");
        var one=grouped.get(0);
        ok(one.get("votiRealiValidi").equals(30),"real votes must count only once");
        ok(((BigDecimal)one.get("mediaVotoReale")).compareTo(new BigDecimal("6.666666666666667"))==0,
            "weighted real average, not average of seasonal averages");
        ok(one.get("gol").equals(BigDecimal.valueOf(7)),"sum real goals, no fantasy duplication");
        ok(((List<?>)one.get("stagioni")).size()==2,"no future season in limited cutoff");
        boolean rejected=false;
        try{TopFlopHistoricalViews.aggregate(src,Map.of(
           "2015_2016:9","rn4p-2014_2015-4", "2015_2016:15","rn4p-2014_2015-4"));}
        catch(IllegalArgumentException expected){rejected=true;}
        ok(rejected,"two distinct source players same season must NOT be merged");

        var r=new TopFlopPlayerIdentities.Registry();
        var p=new TopFlopPlayerIdentities.Proposal("a".repeat(64),List.of(
            new TopFlopPlayerIdentities.Source("2006_2007",224,12302,"ASCOLI Nicola","1979-11-09"),
            new TopFlopPlayerIdentities.Source("2007_2008",364,12302,"ASCOLI Nicola","1979-09-11")),
            false,"Date discordanti");
        ok(r.approved().isEmpty(),"no silent approval");
        r.approve(p);
        var file=Files.createTempFile("rn4_identity",".json");
        try {
            TopFlopPlayerIdentities.save(file,r);
            var restored=TopFlopPlayerIdentities.load(file);
            ok(restored.approvedPlayers()==2 && restored.approvedIdentities()==1,
                "persist and restore explicit approval");
            ok(restored.approved().get("2015_2016:175")==null,
                "Crivello 12302 never inherits Ascoli approval");
        } finally {Files.deleteIfExists(file);}
        System.out.println("RN4_HISTORY_IDENTITY_OK weighted-votes cutoff collisions persistence");
    }
}
~~~~

## `src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopHistoricalStagingSmokeTest.java`

SHA256: `b23085e24690e0fbe4e79cdb6ca60dfd40d149221e7122712d9bdefcce354e92`

~~~~java
package it.alterlega.recordsnext.app.topflop;
import java.nio.file.*;
import java.util.*;
public final class TopFlopHistoricalStagingSmokeTest {
    public static void main(String[] args)throws Exception {
        Path js=Path.of(args[0]);
        List<Path> files;
        try(var s=Files.list(js)){
            files=s.filter(p->p.getFileName().toString().matches(
                "fcmRecordsNext_TopFlopViews\\.Careers\\.[0-9]{4}_[0-9]{4}\\.[0-9]{3}\\.js"))
                .sorted().toList();
        }
        var careers=TopFlopHistoricalViews.readCareers(js,files);
        if(careers.size()!=16262)throw new AssertionError("Actual career rows="+careers.size());
        var higuain=careers.stream().filter(x->String.valueOf(x.get("nome")).startsWith("HIGUAIN Gonzalo"))
                .toList();
        if(higuain.size()<4)throw new AssertionError("Higuain seasons missing "+higuain.size());
        Map<String,String> approved=new HashMap<>();
        for(Map<String,Object> x:higuain)approved.put(String.valueOf(x.get("playerKey")),"rn4p-2013_2014-304");
        var history=TopFlopHistoricalViews.aggregate(careers,approved);
        if(history.size()!=1)throw new AssertionError("Historic groups "+history.size());
        var row=history.get(0);
        if((int)row.get("numeroStagioni")!=higuain.size())throw new AssertionError("Missing higuain years");
        Path folder=Files.createTempDirectory("rn4-historic-shards-");
        try {
            var out=TopFlopPublicViewsStageExporter.shard(folder,"Storico","History",new ArrayList<Object>(history));
            if(out.size()!=1||out.get(0).bytes()>1_500_000)throw new AssertionError("Historic shard size");
        } finally {
            try(var s=Files.list(folder)){for(Path f:s.toList())Files.deleteIfExists(f);}
            Files.deleteIfExists(folder);
        }
        System.out.println("RN4_REAL_STAGING_HISTORY_OK seasons="+files.size()+
            " careers="+careers.size()+" higuainSeasons="+higuain.size()+
            " higuainRealVotes="+row.get("votiRealiValidi"));
    }
}
~~~~

## `src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopScopedPublicationSmokeTest.java`

SHA256: `e4a476c6246a7ab96bf790f3a2d6101b0d1f0d9ff12a45d9535e01963803f54b`

~~~~java
package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.config.MiniJson;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Staging from real 21-season export; strict site cutoff and approved history only. */
public final class TopFlopScopedPublicationSmokeTest {
    @SuppressWarnings("unchecked")
    public static void main(String[] args)throws Exception {
        Path from=Path.of(args[0]);
        var prepared=new TopFlopViewsPublication.Prepared(from,
            Set.of("2013_2014","2014_2015","2015_2016","2016_2017","2017_2018","2018_2019","2019_2020","2020_2021"), 85,
            Map.of("2015_2016:315","rn4p-2013_2014-304", "2016_2017:302","rn4p-2013_2014-304",
                "2017_2018:265","rn4p-2013_2014-304"));
        Path dest=Files.createTempDirectory("rn4-publish-scoped-");
        try {
            int count=TopFlopViewsPublication.attachScoped(prepared,
                List.of("2015_2016","2016_2017"),dest);
            if(count<2)throw new AssertionError("No JS attached");
            String facade=Files.readString(dest.resolve(TopFlopPublicViewsStageExporter.FACADE));
            int begin=facade.indexOf('{');int end=facade.lastIndexOf("};");
            if(begin<0||end<begin)throw new AssertionError("No facade");
            Map<String,Object> meta=(Map<String,Object>)MiniJson.parse(facade.substring(begin,end+1));
            if(((List<?>)meta.get("stagioni")).size()!=2)throw new AssertionError("Historical cutoff leaked");
            Map<String,Object> historical=(Map<String,Object>)meta.get("storico");
            if(((Number)historical.get("identita")).intValue()!=1)throw new AssertionError("Approved identity not present: "+historical);
            List<Map<String,Object>> historyFiles=(List<Map<String,Object>>)historical.get("file");
            if(historyFiles.isEmpty())throw new AssertionError("History file missing");
            Path history=dest.resolve((String)historyFiles.get(0).get("nome"));
            String js=Files.readString(history,StandardCharsets.UTF_8);
            if(!js.contains("2015_2016:315")||!js.contains("2016_2017:302")||js.contains("2017_2018:265"))
                throw new AssertionError("Approved historical cutoff incorrect");
            if(TopFlopViewsPublication.validateStaging(dest)!=count)
                throw new AssertionError("JS count mismatch");
            System.out.println("RN4_SCOPED_PUBLISH_OK files="+count+" careers=1 seasonScope=2");
        } finally {
            try(var stream=Files.list(dest)){for(Path file:stream.toList())Files.deleteIfExists(file);}
            Files.deleteIfExists(dest);
        }
    }
}
~~~~

## `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v29.md`

SHA256: `9bcdc6f615125ba48431ddbcda01e3464d85975d37357ee7106616cd3099a66c`

~~~~markdown
# RecordsNext 4.0 - diario implementazione v29 integrata (10 novembre 2026)

Base: sorgenti della GUI v28 estratti dal progetto originale del 6 novembre 2026.
Nessun codice delle famiglie 3.2 (Classics, Series, RU, Modifiers, ThresholdsLuck)
e nessun mapping storico di squadre/competizioni sono stati modificati.

## Componenti Java realmente modificati

- RecordsNext2Dashboard: rimossi seconda modalita' Completa/Consolidata e pulsante Top & Flop duplicati; aggiunto comando di riconciliazione delle identita'; alert non bloccante al primo ingresso in Top & Flop.
- TopFlopPlayerIdentities (nuovo): lettura delle anagrafiche FCA, proposte di associazione, identificazione dei codici storici ambigui, scelte esplicite salvate in config/player-identities.json. Le proposte non sono applicate automaticamente. Distinguere, unire manualmente, rinviare e revocare sono disponibili.
- TopFlopIdentityReviewDialog (nuovo): configurazione tramite GUI dopo importazione FCA; approvazione massiva SOLO dei casi esatti, verifica manuale degli altri.
- TopFlopPublicViewsStageExporter: usa il tabellino FCM per le prestazioni non conteggiate ma osservate (panchina/fuori lista); calcola media voto reale e fantamedia tabellino, eventi bonus/malus; espone i costi Tesserato.COSTOACQ (min, max, media, campione) per calciatore e fantasquadra.
- TopFlopHistoricalViews (nuovo): carriere storiche dai soli playerKey con identityId approvato; medie ponderate su voti e contatori, non sulle medie; nessuna duplicazione dei voti per fantasquadre.
- TopFlopViewsPublication: nel normale attachScoped 3.2 genera nuovi shard History aggregati solo sulle annate disponibili per ciascun sito; size limit 1.500.000 byte reale; hash e metadata nella facade.
- RecordsNextPipeline: carica le decisioni dal project root ed esegue il nuovo calcolo nella pipeline gia' consolidata.
- fcmRecordsNextTopFlopViewsDecoder-v1.js: supporta senza regressioni sia i vecchi array compatti (6/4) che le nuove estensioni (7/5); espone loadHistory().

## Contratto JS aggiuntivo

- `Career` conserva tutti i primi 6 elementi; 7o elemento `[numeroRegistrazioniCosto,min,max,media]`.
- `Team` conserva tutti i primi 4 elementi; 5o elemento con lo stesso vettore costi.
- Vettore `fantasy` conserva i 9 elementi originali; 10o elemento `[giornateFuoriConVoto,sommaVotiFuori,gareConFantavotoTabellino,sommaFantavotiFuori,eventiNonApportati]`.
- `History` contiene record storici `[identita, stagioni, voti, media reale, eventi]`, formato oggetto, in `fcmRecordsNext_TopFlopViews.History.Storico.XXX.js`.
- La facade, senza cambiare schemaVersion, espone `storico.file` e `storico.identita` del SOLO cutoff destinazione.

## Verifiche eseguite nell'ambiente di sviluppo

- Java 21: compilazione completa di tutti i sorgenti main (nessun errore).
- TopFlopPublicViewsStageSmokeTest: superato, inclusi portieri, rigori, contesti distinti e no >1.5MB.
- TopFlopHistoricIdentitySmokeTest: superato; nessun join senza approvazione; media storica ponderata; revoca e persistenza.
- TopFlopHistoricalStagingSmokeTest: letti 21 anni di 85 JS reali, 16.262 carriere, 8 stagioni di Higuaín; aggregazione reale 217 voti validi (sorgente), non somma contesti fantasy.
- TopFlopScopedPublicationSmokeTest: superato; sito con cutoff 2015/16-2016/17 non riceve 2017/18; History generato nella stessa staging 3.2.
- Test JS Node: decoder vecchio/nuovo Career e Team, costi e dentro/fuori.

## Resta da collaudare / implementare

- Build Maven completa e GUI Windows sul progetto effettivo. Ambiente Linux privo di dipendenze sqlite-jdbc e Maven scaricabili: non dichiarare eseguito un test SQLite Java completo.
- Calcolo della fantamedia reale indipendente dai contesti fantasy: il solo FCA non contiene un punteggio finale pronto; non inventare coefficienti, completare con le regole di scoring verificate.
- Fuori formazione non documentati in FCM.Formazione: non dedurre la rosa giorno per giorno dai soli tesseramenti a snapshot. I dati 'non utilizzati' coprono soltanto i casi osservati (panchina e fuori lista).
- Valore tecnico economico IVS da calibrare e configurare. Il costo di acquisto e' stato aggiunto; nessuna stima di valore economico artificiale.
- Fotografie storiche: percorsi fisici non ancora verificati, da completare.
- Classifiche storiche per rendimento delle fantasquadre canoniche: conservare i mapping 3.2, da sviluppare separatamente.

Non sono state modificate le basi FCA/FCM originali, il database SQLite e i siti pubblicati.
~~~~

## `docs/REGISTRO_DECISIONI_RECORDSNEXT4_v29.md`

SHA256: `601eda91c09dc8d976bce1d2dc5ee0bc4f3670c48c33ae67468934b0149a0c23`

~~~~markdown
# RecordsNext 4.0 - decisioni consolidate v29 integrata

1. La GUI RecordsNext 4.0 e' l'unico punto di avvio dell'elaborazione, completa o consolidata. Nessun secondo comando di elaborazione dentro Top & Flop.
2. Preservare output 3.2 e motore canonico associazioni storico/attuali fantasquadre e competizioni. Gli output Top & Flop si aggiungono nel normale staging e nella pubblicazione esistente.
3. La carriera storica del calciatore richiede `playerIdentityId` approvato in configurazione iniziale. Il codice Gazzetta non e' identita' universalmente unica. Nessuna riconciliazione silenziosa, neanche dei nomi esatti.
4. Persistenza in `config/player-identities.json`. Le proposte esatte possono essere approvate in blocco; nomi diversi, collisioni di codice e omonimie devono poter essere accettati o distinti manualmente e revocati.
5. Le prestazioni reali delle singole giornate FCA si contano una volta sola; i contesti fantasquadra/competizione FCM restano distinti. Non sommare gli utilizzi fantasy come fossero partite reali del calciatore.
6. Statistiche 'dentro/fuori': media voto, fantamedia tabellino quando disponibile, bonus/malus osservati, senza simulare formazioni, moduli, sostituzioni e punti recuperabili.
7. Costi: min, max, media dei costi Tesserato.COSTOACQ documentati, con numerosita' campione. Valore tecnico IVS solo dopo calibrazione della formula da GUI.
8. Le soglie di ranking e i filtri saranno preferibilmente nel nuovo sito Renewo; non pre-tagliare l'insieme delle carriere e dei Teams pubblicati.
9. Il contratto principale sono i JS prodotti dal plugin, non la pagina HTML di anteprima. Gli shard devono essere <=1.500.000 byte reali.
10. Le nuove viste History devono rispettare rigorosamente il cutoff temporale della 3.2 per ciascun sito destinatario; non inviare dati futuri ai siti storici.
11. Nessuna modifica ai siti ne' ai database in sede di patch: backup, compilazione e test locali prima dell'elaborazione pubblicante.
~~~~

## `docs/PIANO_RECORDSNEXT4_GIOCATORI_v29.md`

SHA256: `7706ea18402b756a56f11205b5f41430ca80c66d9669b77aaa0af2b0cc9e447c`

~~~~markdown
# RecordsNext 4.0 - piano lavoro dopo integrazione v29

Realizzato e in collaudo: gestione delle identita' (GUI e config persistente), nuove carriere storiche per identita' approvata, statistiche fuori/dentro osservate, costi di acquisto, pipeline storica con cutoff, decoder compatibile.

Prossimi gate di completamento nell'installazione Windows:

1. Compilazione Maven/Java 21 con le dipendenze SQLite reali; avvio GUI e salvataggio/revoca associazioni; conferma separazione ASCOLI/CRIVELLO 12302.
2. Elaborazione Completa/Consolidata con Top & Flop su 21 annate; test bilancio realDays, contesti fantasy, prezzi, non conteggiati e controllo sha/byte di tutti i JS.
3. Validazione dei totali reali e delle medie sulle prestazioni originali; una sola giornata reale per calciatore e un contesto indipendente per ogni fantasquadra.
4. Fonte di fantavoto reale unica: verificare scoring Gazzetta/FCM senza dedurlo dal contributo di una fantasquadra.
5. Ricostruzione del possesso giornaliero solo se documentabile, altrimenti limitare gli indicatori di fuori formazione a quelli realmente osservati.
6. Costruire le graduatorie storiche e annuali per real/fantasy, con soglie lato Renewo; sfruttare le associazioni squadre della 3.2; definire IVS con parametri configurabili.
7. Verificare percorsi immagini storiche e completare l'indice foto con fallback.
8. Visualizzatore di prova che legge unicamente JS appena pubblicati dal plugin; regressione senza perdita degli output classici della 3.2.
9. Pubblicazione protetta e release finale con GUI verificata su Windows.

Non scambiare una milestone compilata con una release completa: i gate sopra devono essere superati.
~~~~
