package it.alterlega.recordsnext.gui;

import it.alterlega.recordsnext.ConfigurationSchema;
import it.alterlega.recordsnext.app.PipelineConfig;
import it.alterlega.recordsnext.app.ProcessingOptions;
import it.alterlega.recordsnext.app.ProcessingMode;
import it.alterlega.recordsnext.app.RecordsNextPipeline;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.image.BufferedImage;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class RecordsNextApp {
    private static final String KEY_CLASSIC = "processing.classic";
    private static final String KEY_RU = "processing.ru";
    private static final String KEY_GENERATE_JS = "processing.generateJs";
    private static final String KEY_PUBLISH = "processing.publish";
    private static final String KEY_MODE = "processing.mode";
    private static final String KEY_PUBLISH_MODE = "publish.destinationMode";
    private static final String KEY_PUBLISH_CUSTOM = "publish.customDirectory";

    private final JFrame frame = new JFrame("FCM RecordsNext 1.0");
    private final JCheckBox classic = new JCheckBox("Record classici");
    private final JCheckBox ru = new JCheckBox("Riserve d'ufficio");
    private final JCheckBox generateJs = new JCheckBox("Genera file JavaScript");
    private final JCheckBox publish = new JCheckBox("Pubblica i file nel sito");
    private final JRadioButton publishCurrent = new JRadioButton("Cartella js della stagione attuale");
    private final JRadioButton publishCustom = new JRadioButton("Cartella personalizzata");
    private final JTextField publishDirectory = new JTextField();
    private final JButton publishBrowse = new JButton("...");
    private final JLabel publishResolved = new JLabel(" ");
    private final JRadioButton fullMode = new JRadioButton("Elaborazione completa");
    private final JRadioButton consolidatedMode = new JRadioButton("Aggiornamento da consolidamento");
    private final JTextArea log = new JTextArea(10, 48);
    private final JProgressBar phaseProgress = new JProgressBar();
    private final JProgressBar progress = new JProgressBar(0, 100);
    private final JLabel phaseLabel = new JLabel("Nessuna operazione in corso");
    private final JButton start = new JButton("Avvia");
    private final JLabel status = new JLabel("Pronto", SwingConstants.CENTER);
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final Path configPath = root.resolve("config/recordsnext-gui.properties");
    private final Properties properties = new Properties();
    private boolean loadingSelections;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RecordsNextApp().show());
    }

    private RecordsNextApp() {
        bootstrapRuntimeDirectories();
        loadProperties();
        build();
        loadSelections();
    }

    private void bootstrapRuntimeDirectories() {
        String[] directories = {
                "config",
                "data/database"
        };
        try {
            for (String directory : directories) {
                Files.createDirectories(root.resolve(directory));
            }
            ConfigurationSchema.initializeEmpty(root.resolve("data/database/recordsnext.db"));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Impossibile creare le cartelle di lavoro di RecordsNext:\n" + ex.getMessage(),
                    "FCM RecordsNext 1.0", JOptionPane.ERROR_MESSAGE);
            throw new IllegalStateException("Bootstrap delle cartelle fallito", ex);
        }
    }

    private void build() {
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 13));
        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("CheckBox.font", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("RadioButton.font", new Font("Segoe UI", Font.PLAIN, 13));

        Color background = new Color(244, 247, 252);
        Color panelBorder = new Color(196, 205, 222);
        Color blue = new Color(34, 72, 150);
        Color red = new Color(201, 34, 45);

        JPanel rootPanel = new JPanel(new BorderLayout(12, 12));
        rootPanel.setBorder(new EmptyBorder(16, 20, 12, 20));
        rootPanel.setBackground(background);

        JPanel header = new JPanel(new GridBagLayout());
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(590, 112));
        header.setMinimumSize(new Dimension(590, 112));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 112));
        GridBagConstraints hg = new GridBagConstraints();
        hg.gridx = 0;
        hg.weightx = 1;
        hg.fill = GridBagConstraints.HORIZONTAL;
        hg.anchor = GridBagConstraints.CENTER;

        JLabel title = new JLabel("FCM RecordsNext 1.0", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI Black", Font.BOLD, 35));
        title.setForeground(red);
        hg.gridy = 0;
        header.add(title, hg);

        JLabel sub = new JLabel("Records storici e tanto altro", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
        sub.setForeground(new Color(62, 72, 92));
        hg.gridy = 1;
        hg.insets = new Insets(4, 0, 0, 0);
        header.add(sub, hg);

        status.setFont(new Font("Segoe UI", Font.BOLD, 13));
        status.setForeground(new Color(35, 105, 62));
        status.setPreferredSize(new Dimension(540, 26));
        status.setMinimumSize(new Dimension(540, 26));
        hg.gridy = 2;
        hg.insets = new Insets(11, 0, 0, 0);
        header.add(status, hg);
        rootPanel.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        JPanel options = new JPanel(new GridBagLayout());
        options.setBackground(Color.WHITE);
        options.setBorder(new CompoundBorder(
                new LineBorder(panelBorder),
                new EmptyBorder(12, 15, 12, 15)));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.insets = new Insets(3, 4, 7, 4);

        JLabel modeTitle = new JLabel("Modalità");
        modeTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        modeTitle.setForeground(blue);
        options.add(modeTitle, g);
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(fullMode);
        modeGroup.add(consolidatedMode);
        g.gridy++;
        options.add(fullMode, g);
        g.gridy++;
        options.add(consolidatedMode, g);
        g.gridy++;
        g.insets = new Insets(10, 4, 7, 4);

        JLabel sectionTitle = new JLabel("Elaborazioni");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sectionTitle.setForeground(blue);
        options.add(sectionTitle, g);

        g.insets = new Insets(2, 4, 2, 4);
        g.gridy++;
        options.add(classic, g);
        g.gridy++;
        options.add(ru, g);
        g.gridy++;
        options.add(generateJs, g);
        g.gridy++;
        options.add(publish, g);

        ButtonGroup publishGroup = new ButtonGroup();
        publishGroup.add(publishCurrent);
        publishGroup.add(publishCustom);
        JPanel publishDestination = new JPanel(new GridBagLayout());
        publishDestination.setOpaque(false);
        publishDestination.setBorder(new EmptyBorder(3, 24, 2, 0));
        GridBagConstraints dg = new GridBagConstraints();
        dg.gridx = 0;
        dg.gridy = 0;
        dg.gridwidth = 3;
        dg.anchor = GridBagConstraints.WEST;
        dg.fill = GridBagConstraints.HORIZONTAL;
        dg.weightx = 1;
        publishDestination.add(publishCurrent, dg);
        dg.gridy = 1;
        publishDestination.add(publishCustom, dg);
        dg.gridy = 2;
        dg.gridwidth = 1;
        dg.weightx = 1;
        publishDestination.add(publishDirectory, dg);
        dg.gridx = 1;
        dg.weightx = 0;
        dg.fill = GridBagConstraints.NONE;
        publishDestination.add(publishBrowse, dg);
        dg.gridx = 0;
        dg.gridy = 3;
        dg.gridwidth = 3;
        dg.fill = GridBagConstraints.HORIZONTAL;
        publishResolved.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        publishResolved.setForeground(new Color(90, 98, 112));
        publishDestination.add(publishResolved, dg);
        g.gridy++;
        g.insets = new Insets(0, 4, 2, 4);
        options.add(publishDestination, g);

        JLabel savedHint = new JLabel("Le scelte vengono memorizzate automaticamente.");
        savedHint.setForeground(new Color(90, 98, 112));
        savedHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        g.gridy++;
        g.insets = new Insets(8, 7, 1, 4);
        options.add(savedHint, g);
        options.setAlignmentX(Component.LEFT_ALIGNMENT);
        options.setMaximumSize(new Dimension(Integer.MAX_VALUE, options.getPreferredSize().height));
        center.add(options);
        center.add(Box.createVerticalStrut(10));

        JPanel progressPanel = new JPanel(new GridBagLayout());
        progressPanel.setBackground(Color.WHITE);
        progressPanel.setBorder(new CompoundBorder(
                new LineBorder(panelBorder),
                new EmptyBorder(10, 12, 10, 12)));
        GridBagConstraints pg = new GridBagConstraints();
        pg.gridx = 0;
        pg.weightx = 1;
        pg.fill = GridBagConstraints.HORIZONTAL;
        pg.anchor = GridBagConstraints.WEST;
        pg.insets = new Insets(2, 2, 3, 2);

        JLabel phaseTitle = new JLabel("Operazione corrente");
        phaseTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        phaseTitle.setForeground(blue);
        pg.gridy = 0;
        progressPanel.add(phaseTitle, pg);
        phaseLabel.setPreferredSize(new Dimension(540, 22));
        phaseLabel.setMinimumSize(new Dimension(540, 22));
        pg.gridy = 1;
        progressPanel.add(phaseLabel, pg);
        phaseProgress.setIndeterminate(false);
        phaseProgress.setStringPainted(false);
        phaseProgress.setPreferredSize(new Dimension(540, 16));
        pg.gridy = 2;
        progressPanel.add(phaseProgress, pg);

        JLabel overallTitle = new JLabel("Avanzamento generale");
        overallTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        overallTitle.setForeground(blue);
        pg.gridy = 3;
        pg.insets = new Insets(9, 2, 3, 2);
        progressPanel.add(overallTitle, pg);
        progress.setStringPainted(true);
        progress.setValue(0);
        progress.setString("0%");
        progress.setPreferredSize(new Dimension(540, 20));
        pg.gridy = 4;
        pg.insets = new Insets(2, 2, 2, 2);
        progressPanel.add(progress, pg);
        progressPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressPanel.setPreferredSize(new Dimension(560, 126));
        progressPanel.setMinimumSize(new Dimension(560, 126));
        progressPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 126));
        center.add(progressPanel);
        center.add(Box.createVerticalStrut(10));

        log.setEditable(false);
        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        DefaultCaret logCaret = (DefaultCaret) log.getCaret();
        logCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane logScroll = new JScrollPane(
                log,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        logScroll.setBorder(new LineBorder(panelBorder));
        logScroll.setPreferredSize(new Dimension(560, 190));
        logScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        logScroll.setMinimumSize(new Dimension(560, 120));
        logScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        logScroll.getVerticalScrollBar().setUnitIncrement(18);
        logScroll.getHorizontalScrollBar().setUnitIncrement(18);
        center.add(logScroll);
        rootPanel.add(center, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(0, 8));
        south.setOpaque(false);
        JPanel credits = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        credits.setOpaque(false);
        JLabel credit = new JLabel("powered by mauz79 © 2026");
        credit.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        credit.setForeground(new Color(82, 89, 105));
        credits.add(credit);
        south.add(credits, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.setOpaque(false);
        JButton config = new JButton("Configurazione");
        JButton exit = new JButton("Esci");
        buttons.add(start);
        buttons.add(config);
        buttons.add(exit);
        south.add(buttons, BorderLayout.SOUTH);
        rootPanel.add(south, BorderLayout.SOUTH);

        fullMode.addActionListener(e -> saveSelections());
        consolidatedMode.addActionListener(e -> saveSelections());
        classic.addActionListener(e -> saveSelections());
        ru.addActionListener(e -> saveSelections());
        generateJs.addActionListener(e -> {
            if (!generateJs.isSelected()) {
                publish.setSelected(false);
            }
            publish.setEnabled(generateJs.isSelected());
            updatePublishControls();
            saveSelections();
        });
        publish.addActionListener(e -> {
            updatePublishControls();
            saveSelections();
        });
        publishCurrent.addActionListener(e -> {
            updatePublishControls();
            saveSelections();
        });
        publishCustom.addActionListener(e -> {
            updatePublishControls();
            saveSelections();
        });
        publishBrowse.addActionListener(e -> choosePublishDirectory());
        start.addActionListener(e -> runPipeline());
        exit.addActionListener(e -> closeApplication());
        config.addActionListener(e -> openConfiguration());

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveSelections();
            }
        });

        frame.setIconImage(createAppIcon());
        frame.setContentPane(rootPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.setMinimumSize(new Dimension(660, 760));
        frame.setPreferredSize(new Dimension(680, 920));
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    private static Image createAppIcon() {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(new Color(201, 34, 45));
            graphics.fillRoundRect(3, 3, 58, 58, 14, 14);
            graphics.setColor(Color.WHITE);
            graphics.setFont(new Font("Segoe UI", Font.BOLD, 25));
            FontMetrics metrics = graphics.getFontMetrics();
            String text = "RN";
            graphics.drawString(text, (64 - metrics.stringWidth(text)) / 2, 42);
        } finally {
            graphics.dispose();
        }
        return image;
    }

    private void show() {
        frame.setVisible(true);
    }

    private void loadProperties() {
        if (!Files.isRegularFile(configPath)) {
            return;
        }
        try (InputStream input = Files.newInputStream(configPath)) {
            properties.load(input);
        } catch (IOException ex) {
            throw new IllegalStateException("Impossibile leggere " + configPath, ex);
        }
    }

    private void loadSelections() {
        loadingSelections = true;
        try {
            classic.setSelected(readBoolean(KEY_CLASSIC, true));
            ru.setSelected(readBoolean(KEY_RU, false));
            generateJs.setSelected(readBoolean(KEY_GENERATE_JS, true));
            publish.setSelected(readBoolean(KEY_PUBLISH, true) && generateJs.isSelected());
            publish.setEnabled(generateJs.isSelected());
            boolean customDestination = "custom".equalsIgnoreCase(
                properties.getProperty(KEY_PUBLISH_MODE, "currentSeason"));
            publishCustom.setSelected(customDestination);
            publishCurrent.setSelected(!customDestination);
            publishDirectory.setText(properties.getProperty(KEY_PUBLISH_CUSTOM, ""));
            updatePublishControls();
            boolean consolidated = "CONSOLIDATED".equalsIgnoreCase(properties.getProperty(KEY_MODE, "FULL"));
            consolidatedMode.setSelected(consolidated);
            fullMode.setSelected(!consolidated);
            try {
                var cfg = PipelineConfig.load(root, configPath);
                boolean available = new RecordsNextPipeline().hasConsolidation(cfg);
                consolidatedMode.setEnabled(available);
                if (!available) fullMode.setSelected(true);
            } catch (Exception ignored) {
                consolidatedMode.setEnabled(false);
                fullMode.setSelected(true);
            }
        } finally {
            loadingSelections = false;
        }
    }

    private boolean readBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        return value == null ? defaultValue : Boolean.parseBoolean(value.trim());
    }

    private void saveSelections() {
        if (loadingSelections) {
            return;
        }
        properties.setProperty(KEY_CLASSIC, Boolean.toString(classic.isSelected()));
        properties.setProperty(KEY_RU, Boolean.toString(ru.isSelected()));
        properties.setProperty(KEY_GENERATE_JS, Boolean.toString(generateJs.isSelected()));
        properties.setProperty(KEY_PUBLISH, Boolean.toString(publish.isSelected()));
        properties.setProperty(KEY_MODE, consolidatedMode.isSelected() ? "CONSOLIDATED" : "FULL");
        properties.setProperty(KEY_PUBLISH_MODE,
            publishCustom.isSelected() ? "custom" : "currentSeason");
        properties.setProperty(KEY_PUBLISH_CUSTOM, publishDirectory.getText().trim());
        try {
            Files.createDirectories(configPath.getParent());
            try (OutputStream output = Files.newOutputStream(configPath)) {
                properties.store(output, "RecordsNext configuration");
            }
        } catch (IOException ex) {
            status.setText("Impossibile salvare la configurazione");
            log.append("AVVISO: impossibile salvare " + configPath + ": " + ex.getMessage()
                    + System.lineSeparator());
        }
    }

    private void openConfiguration() {
        saveSelections();
        RecordsNextConfigurationDialog dialog =
                new RecordsNextConfigurationDialog(frame, root, configPath);
        if (dialog.open()) {
            properties.clear();
            loadProperties();
            loadSelections();
            status.setText("Configurazione salvata");
            log.append("Configurazione aggiornata." + System.lineSeparator());
        }
    }

    private void updatePublishControls() {
        boolean enabled = generateJs.isSelected() && publish.isSelected();
        publishCurrent.setEnabled(enabled);
        publishCustom.setEnabled(enabled);
        boolean custom = enabled && publishCustom.isSelected();
        publishDirectory.setEnabled(custom);
        publishBrowse.setEnabled(custom);
        try {
            Path resolved = PipelineConfig.resolvePublishDirectory(root, propertiesForCurrentUi());
            publishResolved.setText("Destinazione: " + resolved);
        } catch (Exception ex) {
            publishResolved.setText("Destinazione non disponibile");
        }
    }

    private Properties propertiesForCurrentUi() {
        Properties copy = new Properties();
        copy.putAll(properties);
        copy.setProperty(KEY_PUBLISH_MODE,
            publishCustom.isSelected() ? "custom" : "currentSeason");
        copy.setProperty(KEY_PUBLISH_CUSTOM, publishDirectory.getText().trim());
        return copy;
    }

    private void choosePublishDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleziona la cartella di pubblicazione");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        String current = publishDirectory.getText().trim();
        if (!current.isEmpty() && Files.isDirectory(Path.of(current))) {
            chooser.setCurrentDirectory(Path.of(current).toFile());
        } else {
            String remembered = properties.getProperty("chooser.lastPublishDirectory", "").trim();
            if (!remembered.isEmpty() && Files.isDirectory(Path.of(remembered))) {
                chooser.setCurrentDirectory(Path.of(remembered).toFile());
            }
        }
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            Path selected = chooser.getSelectedFile().toPath().toAbsolutePath().normalize();
            publishDirectory.setText(selected.toString());
            properties.setProperty("chooser.lastPublishDirectory", selected.toString());
            updatePublishControls();
            saveSelections();
        }
    }

    private void closeApplication() {
        saveSelections();
        frame.dispose();
    }

    private void runPipeline() {
        saveSelections();
        if (publish.isSelected() && publishCustom.isSelected()
                && publishDirectory.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                "Selezionare la cartella personalizzata di pubblicazione.",
                "RecordsNext", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            var cfg = PipelineConfig.load(root, configPath);
            if (publish.isSelected() && !Files.isDirectory(cfg.siteJs())) {
                JOptionPane.showMessageDialog(frame,
                    "La cartella di pubblicazione non esiste:\n" + cfg.siteJs(),
                    "RecordsNext", JOptionPane.WARNING_MESSAGE);
                return;
            }
            HistoricalMappingRepository repository = new HistoricalMappingRepository(
                root.resolve("data/database/recordsnext.db"));
            repository.prepare();
            String incompleteSeason = null;
            for (String season : repository.seasonsNewestFirst()) {
                if (cfg.seasons().contains(season) && repository.pending(season) > 0) {
                    incompleteSeason = season;
                    break;
                }
            }
            if (incompleteSeason != null) {
                int pending = repository.pending(incompleteSeason);
                status.setText("Configurazione incompleta");
                JOptionPane.showMessageDialog(frame,
                    incompleteSeason + ": restano " + pending + " associazioni da configurare.",
                    "RecordsNext", JOptionPane.WARNING_MESSAGE);
                HistoricalMappingDialog dialog = new HistoricalMappingDialog(frame, repository, incompleteSeason);
                dialog.open();
                return;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "RecordsNext", JOptionPane.ERROR_MESSAGE);
            return;
        }
        final ProcessingOptions options;
        try {
            options = new ProcessingOptions(
                    classic.isSelected(), ru.isSelected(),
                    generateJs.isSelected(), publish.isSelected());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    frame, ex.getMessage(), "RecordsNext", JOptionPane.WARNING_MESSAGE);
            return;
        }

        start.setEnabled(false);
        log.setText("");
        progress.setValue(0);
        progress.setString("0%");
        phaseLabel.setText("Preparazione elaborazione");
        phaseProgress.setIndeterminate(true);
        status.setText("Elaborazione in corso");
        status.setForeground(new Color(35, 82, 150));

        new SwingWorker<RecordsNextPipeline.Result, String>() {
            @Override
            protected RecordsNextPipeline.Result doInBackground() throws Exception {
                var cfg = PipelineConfig.load(root, configPath);
                ProcessingMode mode = consolidatedMode.isSelected()
                    ? ProcessingMode.CONSOLIDATED : ProcessingMode.FULL;
                return new RecordsNextPipeline().run(cfg, options, mode,
                    new RecordsNextPipeline.Listener() {
                        @Override
                        public void phase(String text, int percent) {
                            publish(text);
                            SwingUtilities.invokeLater(() -> {
                                phaseLabel.setText(text);
                                if (percent >= 0) {
                                    progress.setValue(percent);
                                    progress.setString(percent + "%");
                                }
                            });
                        }

                        @Override
                        public void timing(String text) {
                            publish("TEMPO  " + text);
                        }
                    });
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                chunks.forEach(value -> log.append(value + System.lineSeparator()));
            }

            @Override
            protected void done() {
                try {
                    var result = get();
                    log.append("File validi: " + result.files()
                            + "; pubblicati: " + result.published()
                            + System.lineSeparator());
                    status.setText("Elaborazione completata");
                    status.setForeground(new Color(35, 105, 62));
                    phaseLabel.setText("Elaborazione completata");
                    phaseProgress.setIndeterminate(false);
                    phaseProgress.setValue(100);
                    consolidatedMode.setEnabled(true);
                } catch (Exception ex) {
                    status.setText("Errore");
                    status.setForeground(new Color(178, 38, 45));
                    phaseLabel.setText("Elaborazione interrotta");
                    phaseProgress.setIndeterminate(false);
                    phaseProgress.setValue(0);
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    log.append("ERRORE: " + cause + System.lineSeparator());
                    JOptionPane.showMessageDialog(
                            frame, String.valueOf(cause),
                            "Errore RecordsNext", JOptionPane.ERROR_MESSAGE);
                } finally {
                    start.setEnabled(true);
                }
            }
        }.execute();
    }
}
