package it.alterlega.recordsnext.gui;

import it.alterlega.recordsnext.ConfigurationSchema;
import it.alterlega.recordsnext.RawSqliteImporter;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

final class RecordsNextConfigurationDialog extends JDialog {
    private static final Pattern SEASON = Pattern.compile("\\d{4}_\\d{4}");
    private final Path projectRoot, configPath, databasePath;
    private final Properties properties = new Properties();
    private final JPanel seasonsPanel = new JPanel();
    private final List<SeasonEditor> editors = new ArrayList<>();
    private final SeasonConfigurationRepository repository;
    private boolean saved;

    RecordsNextConfigurationDialog(Window owner, Path projectRoot, Path configPath) {
        super(owner,"RecordsNext - Configurazione stagioni",ModalityType.APPLICATION_MODAL);
        this.projectRoot=projectRoot; this.configPath=configPath;
        loadProperties();
        this.databasePath=projectRoot.resolve(properties.getProperty("database","data/database/recordsnext.db")).normalize();
        this.repository=new SeasonConfigurationRepository(databasePath);
        build(); loadSeasons();
    }
    boolean open(){ setVisible(true); return saved; }

    private void build(){
        JPanel root=new JPanel(new BorderLayout(10,10)); root.setBorder(new EmptyBorder(12,14,12,14));
        JPanel top=new JPanel(new BorderLayout());
        JLabel info=new JLabel("Configurare le stagioni gestite o manuali e, successivamente, i relativi siti.");
        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton mappings = new JButton("Configura associazioni storiche...");
        mappings.addActionListener(e -> openMappings());
        JButton add=new JButton("Aggiungi stagione"); add.addActionListener(e->addSeason());
        topButtons.add(mappings); topButtons.add(add);
        top.add(info,BorderLayout.WEST); top.add(topButtons,BorderLayout.EAST); root.add(top,BorderLayout.NORTH);
        seasonsPanel.setLayout(new BoxLayout(seasonsPanel,BoxLayout.Y_AXIS)); seasonsPanel.setBorder(new EmptyBorder(4,4,4,4));
        JScrollPane scroll=new JScrollPane(seasonsPanel); scroll.getVerticalScrollBar().setUnitIncrement(20); root.add(scroll,BorderLayout.CENTER);
        JPanel buttons=new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel=new JButton("Annulla"), save=new JButton("Salva");
        cancel.addActionListener(e->dispose()); save.addActionListener(e->saveConfiguration());
        buttons.add(cancel); buttons.add(save); root.add(buttons,BorderLayout.SOUTH);
        setContentPane(root); setDefaultCloseOperation(DISPOSE_ON_CLOSE); setSize(980,720); setMinimumSize(new Dimension(860,600)); setLocationRelativeTo(getOwner());
    }

    private void loadProperties(){
        if(Files.isRegularFile(configPath)) try(InputStream in=Files.newInputStream(configPath)){properties.load(in);} catch(IOException ex){error("Lettura configurazione",ex);}
    }
    private void loadSeasons(){
        try {
            if (Files.isRegularFile(databasePath)) {
                new HistoricalMappingRepository(databasePath).prepare();
            }
        } catch (Exception ex) {
            error("Pulizia configurazione storica", ex);
        }
        editors.clear(); seasonsPanel.removeAll();
        Set<String> selected=Arrays.stream(properties.getProperty("seasons","").split(",")).map(String::trim).filter(s->!s.isEmpty()).collect(Collectors.toSet());
        try{
            for(var loaded:repository.load()) {
                var row = refreshManagedMetadata(loaded);
                addEditor(new SeasonEditor(row,selected.contains(row.seasonId())));
            }
        }catch(Exception ex){error("Lettura stagioni",ex);}
        refresh();
    }


    private SeasonConfigurationRepository.SeasonRow refreshManagedMetadata(
        SeasonConfigurationRepository.SeasonRow row
    ) {
        if (!"GESTITA".equals(row.managementType()) || row.fcmPath().isBlank()) {
            return row;
        }
        try {
            Path fcm = Path.of(row.fcmPath());
            if (!Files.isRegularFile(fcm)) return row;
            var detection = new FcmSeasonDetector().detect(fcm);
            if (!row.seasonId().equals(detection.seasonId())) {
                return row;
            }
            return new SeasonConfigurationRepository.SeasonRow(
                row.seasonId(),
                detection.seasonNumber(),
                row.anchor(),
                row.managementType(),
                row.status(),
                row.fcmPath(),
                row.fcaPath(),
                row.localSitePath(),
                row.onlineSiteUrl()
            );
        } catch (Exception ignored) {
            return row;
        }
    }

    private void addSeason(){
        try {
            List<SeasonConfigurationRepository.SeasonRow> current=editors.stream().map(SeasonEditor::value).toList();
            AddSeasonWizard wizard=new AddSeasonWizard(this,repository,current,properties,configPath);
            SeasonConfigurationRepository.SeasonRow row=wizard.open();
            if(row==null)return;
            if(editors.stream().anyMatch(e->e.row.seasonId().equals(row.seasonId()))){warn("La stagione è già presente.");return;}
            List<SeasonConfigurationRepository.SeasonRow> rows = new ArrayList<>();
            for (SeasonEditor editor : editors) rows.add(editor.value());
            rows.add(row);
            repository.save(rows);
            selectSeasonByDefault(row.seasonId());

            if ("GESTITA".equals(row.managementType())) {
                importForConfiguration(row, rows);
            }

            loadSeasons();
            SeasonConfigurationRepository.SeasonRow loaded = repository.load().stream()
                .filter(r -> r.seasonId().equals(row.seasonId()))
                .findFirst().orElse(row);
            if ("GESTITA".equals(loaded.managementType()) && !loaded.anchor()) {
                openMappings();
                loadSeasons();
            }
        } catch(Exception ex) { error("Aggiunta stagione",ex); }
    }

    private void selectSeasonByDefault(String seasonId) throws IOException {
        LinkedHashSet<String> selected = Arrays.stream(properties.getProperty("seasons", "").split(","))
            .map(String::trim)
            .filter(value -> !value.isEmpty())
            .collect(Collectors.toCollection(LinkedHashSet::new));
        selected.add(seasonId);
        properties.setProperty("seasons", String.join(",", selected));
        Files.createDirectories(configPath.getParent());
        try (OutputStream out = Files.newOutputStream(configPath)) {
            properties.store(out, "RecordsNext configuration");
        }
    }

    private void importForConfiguration(
        SeasonConfigurationRepository.SeasonRow row,
        List<SeasonConfigurationRepository.SeasonRow> allRows
    ) throws Exception {
        RawSqliteImporter.main(new String[]{row.fcmPath(), "FCM", row.seasonId(), databasePath.toString()});
        RawSqliteImporter.main(new String[]{row.fcaPath(), "FCA", row.seasonId(), databasePath.toString()});
        String anchor = allRows.stream()
            .filter(r -> "GESTITA".equals(r.managementType()))
            .max(Comparator.comparingInt(r -> Integer.parseInt(r.seasonId().substring(0, 4))))
            .orElseThrow(() -> new IllegalStateException("Nessuna stagione gestita"))
            .seasonId();
        ConfigurationSchema.main(new String[]{databasePath.toString(), anchor});
    }

    private void openMappings() {
        try {
            HistoricalMappingRepository mappingRepository = new HistoricalMappingRepository(databasePath);
            HistoricalMappingDialog dialog = new HistoricalMappingDialog(this, mappingRepository);
            dialog.open();
            loadSeasons();
        } catch (Exception ex) {
            error("Associazioni storiche", ex);
        }
    }
    private void addEditor(SeasonEditor e){editors.add(e); seasonsPanel.add(e.panel); seasonsPanel.add(Box.createVerticalStrut(8));}
    private void refresh(){seasonsPanel.revalidate();seasonsPanel.repaint();}

    private void saveConfiguration(){
        if(editors.isEmpty()){warn("Aggiungere almeno una stagione.");return;}
        List<SeasonConfigurationRepository.SeasonRow> rows=new ArrayList<>();
        for(SeasonEditor e:editors){String problem=e.validateFields(); if(problem!=null){warn(problem);return;} rows.add(e.value());}
        List<String> selected=editors.stream().filter(e->e.include.isSelected()).map(e->e.row.seasonId()).toList();
        if(selected.isEmpty()){warn("Selezionare almeno una stagione da elaborare.");return;}
        try {
            HistoricalMappingRepository mappingRepository = new HistoricalMappingRepository(databasePath);
            for (SeasonConfigurationRepository.SeasonRow row : rows) {
                if (selected.contains(row.seasonId()) && "GESTITA".equals(row.managementType()) && !row.anchor()) {
                    int pending = mappingRepository.pending(row.seasonId());
                    if (pending > 0) {
                        warn(row.seasonId() + ": restano " + pending + " associazioni da configurare.");
                        return;
                    }
                }
            }
        } catch (Exception ex) {
            error("Verifica associazioni", ex);
            return;
        }
        properties.setProperty("seasons",String.join(",",selected));
        rows.stream().filter(r->"GESTITA".equals(r.managementType())).max(Comparator.comparing(r->r.seasonId())).ifPresent(current->
            properties.setProperty("siteJs",Path.of(current.localSitePath()).resolve("js").toString()));
        try{
            repository.save(rows);
            Files.createDirectories(configPath.getParent());
            try(OutputStream out=Files.newOutputStream(configPath)){properties.store(out,"RecordsNext configuration");}
            saved=true;dispose();
        }catch(Exception ex){error("Salvataggio configurazione",ex);}
    }

    private void remove(SeasonEditor e){
        int x=JOptionPane.showConfirmDialog(this,"Rimuovere "+e.row.seasonId()+" dalla configurazione?\nI dati già importati non saranno cancellati.","RecordsNext",JOptionPane.YES_NO_OPTION);
        if(x!=JOptionPane.YES_OPTION)return;
        try{repository.removeConfiguration(e.row.seasonId());}catch(Exception ex){error("Rimozione stagione",ex);return;}
        int i=editors.indexOf(e); editors.remove(e); seasonsPanel.remove(e.panel); if(i<seasonsPanel.getComponentCount()) seasonsPanel.remove(i); refresh();
    }

    private void choose(JTextField field, int mode, String extension) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(mode);
        configureExtensionFilter(chooser, extension);

        String text = field.getText().trim();
        Path directory = null;
        if (!text.isEmpty()) {
            Path path = Path.of(text);
            directory = Files.isDirectory(path) ? path : path.getParent();
        }
        String chooserKey = chooserKey(extension, mode);
        if ((directory == null || !Files.exists(directory)) && chooserKey != null) {
            String remembered = properties.getProperty(chooserKey, "").trim();
            if (!remembered.isEmpty() && Files.isDirectory(Path.of(remembered))) directory = Path.of(remembered);
        }
        if (directory != null && Files.exists(directory)) chooser.setCurrentDirectory(directory.toFile());

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            Path selected = chooser.getSelectedFile().toPath().toAbsolutePath().normalize();
            if (!hasExtension(selected, extension)) {
                warn("Selezionare un file " + extension);
                return;
            }
            field.setText(selected.toString());
            Path rememberedDirectory = mode == JFileChooser.DIRECTORIES_ONLY ? selected : selected.getParent();
            rememberChooserDirectory(extension, mode, rememberedDirectory);
            if (".fcm".equalsIgnoreCase(extension)) {
                rememberChooserDirectory(".fca", JFileChooser.FILES_ONLY, selected.getParent());
            }
        }
    }

    private static String chooserKey(String extension, int mode) {
        if (mode == JFileChooser.DIRECTORIES_ONLY) return "chooser.lastSiteDirectory";
        if (".fcm".equalsIgnoreCase(extension)) return "chooser.lastFcmDirectory";
        if (".fca".equalsIgnoreCase(extension)) return "chooser.lastFcaDirectory";
        return null;
    }

    private void rememberChooserDirectory(String extension, int mode, Path directory) {
        String key = chooserKey(extension, mode);
        if (key == null || directory == null) return;
        properties.setProperty(key, directory.toString());
        try {
            Files.createDirectories(configPath.getParent());
            try (OutputStream out = Files.newOutputStream(configPath)) { properties.store(out, "RecordsNext configuration"); }
        } catch (IOException ignored) { }
    }

    private static void configureExtensionFilter(JFileChooser chooser, String extension) {
        if (extension == null || extension.isBlank()) {
            return;
        }
        String normalized = extension.startsWith(".") ? extension.substring(1) : extension;
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter(
            "File " + normalized.toUpperCase(Locale.ROOT) + " (*." + normalized + ")",
            normalized
        ));
    }

    private static boolean hasExtension(Path path, String extension) {
        if (extension == null || extension.isBlank()) {
            return true;
        }
        return path.getFileName().toString().toLowerCase(Locale.ROOT)
            .endsWith(extension.toLowerCase(Locale.ROOT));
    }
    private void warn(String m){JOptionPane.showMessageDialog(this,m,"RecordsNext",JOptionPane.WARNING_MESSAGE);} private void error(String m,Exception e){JOptionPane.showMessageDialog(this,m+":\n"+e.getMessage(),"Errore RecordsNext",JOptionPane.ERROR_MESSAGE);}

    private final class SeasonEditor{
        final SeasonConfigurationRepository.SeasonRow row; final JPanel panel=new JPanel(new GridBagLayout());
        final JCheckBox include=new JCheckBox("Elabora");
        final JTextField fcm=new JTextField(),fca=new JTextField(),site=new JTextField(),online=new JTextField(); final JLabel js=new JLabel(),dataa=new JLabel();
        SeasonEditor(SeasonConfigurationRepository.SeasonRow row,boolean selected){this.row=row;include.setSelected(selected);fcm.setText(row.fcmPath());fca.setText(row.fcaPath());site.setText(row.localSitePath());online.setText(row.onlineSiteUrl());build();updateDerived();}
        void build(){
            panel.setAlignmentX(Component.LEFT_ALIGNMENT); panel.setMaximumSize(new Dimension(Integer.MAX_VALUE,315)); panel.setBackground(Color.WHITE); panel.setBorder(new CompoundBorder(new LineBorder(new Color(190,199,214)),new EmptyBorder(9,10,9,10)));
            GridBagConstraints g=new GridBagConstraints();g.gridy=0;g.gridx=0;g.gridwidth=2;g.anchor=GridBagConstraints.WEST;
            String current=row.anchor()?"  -  ATTUALE":"";
            JLabel title=new JLabel("Stagione "+row.seasonId()+"  (#"+row.seasonNumber()+")  -  "+row.managementType()+current);title.setFont(title.getFont().deriveFont(Font.BOLD,14f));title.setForeground(new Color(25,67,160));panel.add(title,g);
            JPanel flags=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));flags.setOpaque(false);flags.add(include);JButton remove=new JButton("Rimuovi");remove.addActionListener(e->remove(this));flags.add(remove);g.gridx=2;g.gridwidth=2;g.weightx=1;g.anchor=GridBagConstraints.EAST;panel.add(flags,g);
            if("GESTITA".equals(row.managementType())) {addPath("File FCM",fcm,1,JFileChooser.FILES_ONLY,".fcm"); addPath("File FCA",fca,2,JFileChooser.FILES_ONLY,".fca");}
            else {addReadOnly("File FCM","Stagione manuale: non previsto",1); addPath("File FCA (facoltativo)",fca,2,JFileChooser.FILES_ONLY,".fca");}
            addPath("Cartella sito locale",site,3,JFileChooser.DIRECTORIES_ONLY,null); addText("Sito online",online,4);
            site.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){public void insertUpdate(javax.swing.event.DocumentEvent e){updateDerived();}public void removeUpdate(javax.swing.event.DocumentEvent e){updateDerived();}public void changedUpdate(javax.swing.event.DocumentEvent e){updateDerived();}});
            addLabel("Cartella JS",js,5);addLabel("DataA.js",dataa,6);
            if ("GESTITA".equals(row.managementType()) && !row.anchor()) {
                JLabel mappingStatus = new JLabel();
                mappingStatus.setName("mappingStatus");
                addLabel("Associazioni", mappingStatus, 7);
                updateMappingStatus();
            } else if (row.anchor()) {
                JLabel currentStatus = new JLabel("Identità della stagione attuale");
                currentStatus.setForeground(new Color(20,120,55));
                addLabel("Associazioni", currentStatus, 7);
            }
        }

        void updateMappingStatus() {
            for (Component component : panel.getComponents()) {
                if (component instanceof JLabel label && "mappingStatus".equals(label.getName())) {
                    try {
                        int pending = new HistoricalMappingRepository(databasePath).pending(row.seasonId());
                        label.setText(pending == 0 ? "Complete" : pending + " da configurare");
                        label.setForeground(pending == 0 ? new Color(20,120,55) : new Color(170,55,35));
                    } catch (Exception ex) {
                        label.setText("Stato non disponibile");
                        label.setForeground(new Color(170,55,35));
                    }
                }
            }
        }
        void addPath(String label,JTextField field,int y,int mode,String ext){GridBagConstraints g=base(label,y);g.gridx=1;g.gridwidth=2;g.weightx=1;g.fill=GridBagConstraints.HORIZONTAL;panel.add(field,g);JButton b=new JButton("...");b.addActionListener(e->choose(field,mode,ext));g.gridx=3;g.gridwidth=1;g.weightx=0;g.fill=GridBagConstraints.NONE;panel.add(b,g);}
        void addText(String label,JTextField field,int y){GridBagConstraints g=base(label,y);g.gridx=1;g.gridwidth=3;g.weightx=1;g.fill=GridBagConstraints.HORIZONTAL;panel.add(field,g);}
        void addReadOnly(String label,String text,int y){JLabel value=new JLabel(text);value.setForeground(Color.GRAY);addLabel(label,value,y);}
        void addLabel(String label,JLabel value,int y){GridBagConstraints g=base(label,y);g.gridx=1;g.gridwidth=3;g.weightx=1;g.fill=GridBagConstraints.HORIZONTAL;panel.add(value,g);}
        GridBagConstraints base(String label,int y){GridBagConstraints g=new GridBagConstraints();g.gridy=y;g.gridx=0;g.anchor=GridBagConstraints.WEST;g.insets=new Insets(3,2,3,8);panel.add(new JLabel(label+":"),g);return g;}
        void updateDerived(){String s=site.getText().trim();if(s.isEmpty()){js.setText("-");dataa.setText("-");return;}Path j=Path.of(s).resolve("js");Path d=j.resolve("DataA.js");js.setText(j.toString());dataa.setText((Files.isRegularFile(d)?"Trovato: ":"Non trovato: ")+d);dataa.setForeground(Files.isRegularFile(d)?new Color(20,120,55):new Color(170,55,35));}
        String validateFields(){if("GESTITA".equals(row.managementType())){if(fcm.getText().trim().isEmpty()||!Files.isRegularFile(Path.of(fcm.getText().trim())))return row.seasonId()+": selezionare un file FCM esistente.";if(fca.getText().trim().isEmpty()||!Files.isRegularFile(Path.of(fca.getText().trim())))return row.seasonId()+": selezionare un file FCA esistente.";}else if(!fca.getText().trim().isEmpty()&&!Files.isRegularFile(Path.of(fca.getText().trim())))return row.seasonId()+": il file FCA indicato non esiste.";if(site.getText().trim().isEmpty()||!Files.isDirectory(Path.of(site.getText().trim())))return row.seasonId()+": selezionare una cartella sito esistente.";return null;}
        SeasonConfigurationRepository.SeasonRow value(){return new SeasonConfigurationRepository.SeasonRow(row.seasonId(),row.seasonNumber(),row.anchor(),row.managementType(),row.status(),"GESTITA".equals(row.managementType())?fcm.getText().trim():"",fca.getText().trim(),site.getText().trim(),online.getText().trim());}
    }

    private static final class AddSeasonWizard extends JDialog {
        private final JRadioButton managed = new JRadioButton("Gestita", true);
        private final JRadioButton manual = new JRadioButton("Manuale");
        private final JTextField fcm = new JTextField();
        private final JTextField fca = new JTextField();
        private final JTextField manualSeason = new JTextField();
        private final JTextField manualNumber = new JTextField();
        private final JLabel detected = new JLabel(" ");
        private final SeasonConfigurationRepository repo;
        private final List<SeasonConfigurationRepository.SeasonRow> current;
        private final Properties properties;
        private final Path configPath;
        private SeasonConfigurationRepository.SeasonRow result;

        AddSeasonWizard(
            Window owner,
            SeasonConfigurationRepository repo,
            List<SeasonConfigurationRepository.SeasonRow> current,
            Properties properties,
            Path configPath
        ) {
            super(owner, "RecordsNext - Aggiungi stagione", ModalityType.APPLICATION_MODAL);
            this.repo = repo;
            this.current = current;
            this.properties = properties;
            this.configPath = configPath;
            build();
        }

        SeasonConfigurationRepository.SeasonRow open() {
            setVisible(true);
            return result;
        }

        private void build() {
            setLayout(new BorderLayout(10, 10));
            ((JComponent) getContentPane()).setBorder(new EmptyBorder(14, 16, 12, 16));

            ButtonGroup group = new ButtonGroup();
            group.add(managed);
            group.add(manual);

            JPanel form = new JPanel(new GridBagLayout());
            form.setBorder(new TitledBorder("Tipo e sorgenti della stagione"));

            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(5, 5, 5, 5);
            g.anchor = GridBagConstraints.WEST;
            g.gridx = 0;
            g.gridy = 0;
            form.add(managed, g);
            g.gridx = 1;
            form.add(manual, g);

            addChooser(form, "File FCM", fcm, 1, ".fcm");
            addChooser(form, "File FCA", fca, 2, ".fca");
            addField(form, "Stagione manuale (AAAA_AAAA)", manualSeason, 3);
            addField(form, "Numero stagione", manualNumber, 4);
            addValue(form, "Dati rilevati", detected, 5);

            managed.addActionListener(e -> updateMode());
            manual.addActionListener(e -> updateMode());
            updateMode();

            add(form, BorderLayout.CENTER);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton add = new JButton("Aggiungi stagione");
            JButton cancel = new JButton("Annulla");
            add.addActionListener(e -> finish());
            cancel.addActionListener(e -> dispose());
            buttons.add(add);
            buttons.add(cancel);
            add(buttons, BorderLayout.SOUTH);

            setSize(720, 390);
            setLocationRelativeTo(getOwner());
        }

        private void updateMode() {
            boolean isManaged = managed.isSelected();
            fcm.setEnabled(isManaged);
            manualSeason.setEnabled(!isManaged);
            manualNumber.setEnabled(!isManaged);
            detected.setText(isManaged
                ? "Stagione e numero saranno letti dal file FCM."
                : "Inserire stagione e numero manualmente.");
        }

        private void finish() {
            try {
                String seasonId;
                int seasonNumber;
                String type;
                String fcmPath = "";

                if (managed.isSelected()) {
                    if (!file(fcm, ".fcm") || !file(fca, ".fca")) return;
                    var detection = new FcmSeasonDetector().detect(Path.of(fcm.getText().trim()));
                    seasonId = detection.seasonId();
                    seasonNumber = detection.seasonNumber();
                    type = "GESTITA";
                    fcmPath = fcm.getText().trim();
                    detected.setText(
                        seasonId + " (#" + seasonNumber + ") - " + detection.evidence()
                    );
                } else {
                    seasonId = manualSeason.getText().trim();
                    if (!SEASON.matcher(seasonId).matches()) {
                        warn("Formato stagione non valido.");
                        return;
                    }
                    try {
                        seasonNumber = Integer.parseInt(manualNumber.getText().trim());
                    } catch (NumberFormatException ex) {
                        warn("Indicare un numero stagione valido.");
                        return;
                    }
                    if (seasonNumber < 1) {
                        warn("Il numero stagione deve essere positivo.");
                        return;
                    }
                    if (!fca.getText().trim().isEmpty()
                        && !Files.isRegularFile(Path.of(fca.getText().trim()))) {
                        warn("Il file FCA indicato non esiste.");
                        return;
                    }
                    type = "MANUALE";
                }

                if (current.stream().anyMatch(r -> r.seasonId().equals(seasonId))) {
                    warn("La stagione " + seasonId + " è già presente.");
                    return;
                }

                result = new SeasonConfigurationRepository.SeasonRow(
                    seasonId,
                    seasonNumber,
                    false,
                    type,
                    "DA_CONFIGURARE",
                    fcmPath,
                    fca.getText().trim(),
                    "",
                    ""
                );
                dispose();
            } catch (Exception ex) {
                warn(ex.getMessage());
            }
        }

        private boolean file(JTextField field, String extension) {
            String value = field.getText().trim();
            if (value.isEmpty() || !Files.isRegularFile(Path.of(value))) {
                warn("Selezionare un file " + extension + " esistente.");
                return false;
            }
            return true;
        }

        private void addChooser(
            JPanel panel,
            String label,
            JTextField field,
            int row,
            String extension
        ) {
            addField(panel, label, field, row);
            JButton button = new JButton("...");
            button.addActionListener(e -> {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                configureExtensionFilter(chooser, extension);
                String current = field.getText().trim();
                Path directory = null;
                if (!current.isEmpty()) {
                    Path path = Path.of(current);
                    directory = Files.isDirectory(path) ? path : path.getParent();
                }
                String key = chooserKey(extension, JFileChooser.FILES_ONLY);
                if ((directory == null || !Files.exists(directory)) && key != null) {
                    String remembered = properties.getProperty(key, "").trim();
                    if (!remembered.isEmpty() && Files.isDirectory(Path.of(remembered))) directory = Path.of(remembered);
                }
                if (directory != null && Files.exists(directory)) chooser.setCurrentDirectory(directory.toFile());
                if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    Path selected = chooser.getSelectedFile().toPath().toAbsolutePath().normalize();
                    if (!hasExtension(selected, extension)) {
                        warn("Selezionare un file " + extension);
                        return;
                    }
                    field.setText(selected.toString());
                    if (key != null && selected.getParent() != null) {
                        properties.setProperty(key, selected.getParent().toString());
                        if (".fcm".equalsIgnoreCase(extension)) {
                            properties.setProperty("chooser.lastFcaDirectory", selected.getParent().toString());
                        }
                        try {
                            Files.createDirectories(configPath.getParent());
                            try (OutputStream out = Files.newOutputStream(configPath)) {
                                properties.store(out, "RecordsNext configuration");
                            }
                        } catch (IOException ignored) { }
                    }
                }
            });
            GridBagConstraints g = new GridBagConstraints();
            g.gridx = 2;
            g.gridy = row;
            g.insets = new Insets(5, 5, 5, 5);
            panel.add(button, g);
        }

        private void addField(JPanel panel, String label, JTextField field, int row) {
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(5, 5, 5, 5);
            g.anchor = GridBagConstraints.WEST;
            g.gridx = 0;
            g.gridy = row;
            panel.add(new JLabel(label + ":"), g);
            g.gridx = 1;
            g.weightx = 1;
            g.fill = GridBagConstraints.HORIZONTAL;
            panel.add(field, g);
        }

        private void addValue(JPanel panel, String label, JLabel value, int row) {
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(5, 5, 5, 5);
            g.anchor = GridBagConstraints.WEST;
            g.gridx = 0;
            g.gridy = row;
            panel.add(new JLabel(label + ":"), g);
            g.gridx = 1;
            g.gridwidth = 2;
            g.weightx = 1;
            g.fill = GridBagConstraints.HORIZONTAL;
            panel.add(value, g);
        }

        private void warn(String message) {
            JOptionPane.showMessageDialog(
                this,
                message,
                "RecordsNext",
                JOptionPane.WARNING_MESSAGE
            );
        }
    }

}