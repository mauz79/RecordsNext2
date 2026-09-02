package it.alterlega.recordsnext.gui;

import it.alterlega.recordsnext.ConfigurationSchema;
import it.alterlega.recordsnext.RawSqliteImporter;
import it.alterlega.recordsnext.app.core.LeagueMetadata;
import it.alterlega.recordsnext.app.core.LeagueMetadataLoader;

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
    private final JTextField leagueName = new JTextField(28);
    private String leagueIdValue = "";
    private final SeasonConfigurationRepository repository;
    private boolean saved;

    RecordsNextConfigurationDialog(Window owner, Path projectRoot, Path configPath) {
        super(owner,"RecordsNext - Configurazione stagioni",ModalityType.APPLICATION_MODAL);
        this.projectRoot=projectRoot; this.configPath=configPath;
        loadProperties();
        loadLeagueIdentity();
        this.databasePath=projectRoot.resolve(properties.getProperty("database","data/database/recordsnext.db")).normalize();
        this.repository=new SeasonConfigurationRepository(databasePath);
        build(); loadSeasons();
    }
    boolean open(){ setVisible(true); return saved; }

    private void build(){
        JPanel root=new JPanel(new BorderLayout(10,10)); root.setBorder(new EmptyBorder(12,14,12,14));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JPanel leaguePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leaguePanel.setBorder(new TitledBorder("Lega"));
        leaguePanel.add(new JLabel("Nome lega:"));
        leaguePanel.add(leagueName);
        JLabel leagueHint = new JLabel("Se vuoto, RecordsNext prova a ricavarlo dal primo FCM.");
        leagueHint.setForeground(Color.GRAY);
        leaguePanel.add(leagueHint);
        header.add(leaguePanel);
        header.add(Box.createVerticalStrut(8));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel infoLine1 = new JLabel("Configurare stagioni gestite e manuali.");
        JLabel infoLine2 = new JLabel("Il sito locale è opzionale e serve solo come destinazione di pubblicazione.");
        infoLine1.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoLine2.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(infoLine1);
        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(infoLine2);
        header.add(infoPanel);
        header.add(Box.createVerticalStrut(8));

        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topButtons.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton mappings = new JButton("Configura associazioni storiche...");
        mappings.addActionListener(e -> openMappings());
        JButton add=new JButton("Aggiungi stagione");
        add.addActionListener(e->addSeason());
        topButtons.add(mappings);
        topButtons.add(add);
        header.add(topButtons);
        root.add(header,BorderLayout.NORTH);
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

    private void loadLeagueIdentity() {
        Path leagueFile = projectRoot.resolve("config/league.json").normalize();
        if (!Files.isRegularFile(leagueFile)) return;
        try {
            LeagueMetadata metadata = LeagueMetadataLoader.load(leagueFile);
            leagueName.setText(metadata.leagueName());
            leagueIdValue = metadata.leagueId();
        } catch (Exception ex) {
            error("Lettura identità lega", ex);
        }
    }

    private void writeLeagueIdentity(String id, String name, String currentSeasonId) throws IOException {
        Path leagueFile = projectRoot.resolve("config/league.json").normalize();
        Files.createDirectories(leagueFile.getParent());
        String json = "{\n"
                + "  \"leagueId\": \"" + jsonEscape(id) + "\",\n"
                + "  \"leagueName\": \"" + jsonEscape(name) + "\",\n"
                + "  \"currentSeasonId\": \"" + jsonEscape(currentSeasonId) + "\"\n"
                + "}\n";
        Files.writeString(leagueFile, json, java.nio.charset.StandardCharsets.UTF_8);
    }

    private static String slug(String value) {
        String normalized = java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return normalized;
    }

    private static String deriveLeagueNameFromFcm(Path fcm) {
        if (fcm == null) return "";
        String name = fcm.getFileName().toString();
        name = name.replaceFirst("(?i)\\.fcm$", "");
        name = name.replaceFirst("\\s+\\d{4}_\\d{4}.*$", "");
        name = name.replaceFirst("\\s+\\d{4}/\\d{4}.*$", "");
        name = name.replaceFirst("[-_ ]+\\d{4}.*$", "");
        name = name.trim();
        return name;
    }

    private String effectiveLeagueName(List<SeasonConfigurationRepository.SeasonRow> rows) {
        String value = leagueName.getText().trim();
        if (!value.isEmpty()) return value;

        for (SeasonConfigurationRepository.SeasonRow row : rows) {
            if (!"GESTITA".equals(row.managementType()) || row.fcmPath().isBlank()) continue;
            String detected = deriveLeagueNameFromFcm(Path.of(row.fcmPath()));
            if (!detected.isEmpty()) {
                leagueName.setText(detected);
                return detected;
            }
        }

        leagueName.setText("RecordsNext League");
        return "RecordsNext League";
    }

    private static String normalizeOnlineUrl(String value) {
        if (value == null) return "";
        String normalized = value.trim().replace('\\', '/');
        if (normalized.isEmpty()) return "";

        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        if (!normalized.matches("(?i)^https?://.*")) {
            normalized = "http://" + normalized;
        }

        return normalized;
    }

    private static String jsonEscape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
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
        boolean selectionInitialized = Boolean.parseBoolean(
            properties.getProperty("seasonsSelectionInitialized", "false")
        );
        try{
            for(var loaded:repository.load()) {
                var row = refreshManagedMetadata(loaded);
                boolean includeByDefault = !selectionInitialized || selected.contains(row.seasonId());
                addEditor(new SeasonEditor(row, includeByDefault));
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
                if (leagueName.getText().trim().isEmpty()) {
                    String detectedLeagueName = deriveLeagueNameFromFcm(Path.of(row.fcmPath()));
                    if (!detectedLeagueName.isEmpty()) {
                        leagueName.setText(detectedLeagueName);
                    }
                }
                importForConfiguration(row, rows);
            }

            loadSeasons();
            SeasonConfigurationRepository.SeasonRow loaded = repository.load().stream()
                .filter(r -> r.seasonId().equals(row.seasonId()))
                .findFirst().orElse(row);
            if ("GESTITA".equals(loaded.managementType()) && !loaded.anchor()) {
                openMappings(loaded.seasonId());
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
        openMappings(null);
    }

    private void openMappings(String initialSeason) {
        try {
            HistoricalMappingRepository mappingRepository = new HistoricalMappingRepository(databasePath);
            HistoricalMappingDialog dialog = initialSeason == null
                ? new HistoricalMappingDialog(this, mappingRepository)
                : new HistoricalMappingDialog(this, mappingRepository, initialSeason);
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
        String leagueNameValue = effectiveLeagueName(rows);
        String effectiveLeagueId = leagueIdValue == null ? "" : leagueIdValue.trim();
        if (effectiveLeagueId.isEmpty()) effectiveLeagueId = slug(leagueNameValue);
        if (effectiveLeagueId.isEmpty()) effectiveLeagueId = "recordsnext";
        leagueIdValue = effectiveLeagueId;
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
        properties.setProperty("seasonsSelectionInitialized", "true");
        rows.stream()
            .filter(r->"GESTITA".equals(r.managementType()))
            .max(Comparator.comparing(r->r.seasonId()))
            .ifPresent(current->{
                String currentSite=current.localSitePath().trim();
                if(currentSite.isEmpty()){
                    properties.remove("siteJs");
                }else{
                    properties.setProperty("siteJs",Path.of(currentSite).resolve("js").toString());
                }
            });
        try{
            repository.save(rows);
            String currentSeasonId = rows.stream()
                    .filter(r -> "GESTITA".equals(r.managementType()))
                    .max(Comparator.comparing(r -> r.seasonId()))
                    .map(SeasonConfigurationRepository.SeasonRow::seasonId)
                    .orElseThrow(() -> new IllegalStateException("Configurare almeno una stagione gestita."));
            writeLeagueIdentity(effectiveLeagueId, leagueNameValue, currentSeasonId);
            Files.createDirectories(configPath.getParent());
            try(OutputStream out=Files.newOutputStream(configPath)){properties.store(out,"RecordsNext configuration");}
            saved=true;dispose();
        }catch(Exception ex){error("Salvataggio configurazione",ex);}
    }

    private void remove(SeasonEditor e){
        int x=JOptionPane.showConfirmDialog(this,"Rimuovere completamente "+e.row.seasonId()+" da RecordsNext?\nVerranno eliminati i dati interni della stagione, ma non i file FCM/FCA/DataA né il sito sul disco.","RecordsNext",JOptionPane.YES_NO_OPTION);
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
            panel.setAlignmentX(Component.LEFT_ALIGNMENT); panel.setMaximumSize(new Dimension(Integer.MAX_VALUE,"GESTITA".equals(row.managementType())?315:125)); panel.setBackground(Color.WHITE); panel.setBorder(new CompoundBorder(new LineBorder(new Color(190,199,214)),new EmptyBorder(9,10,9,10)));
            GridBagConstraints g=new GridBagConstraints();g.gridy=0;g.gridx=0;g.gridwidth=2;g.anchor=GridBagConstraints.WEST;
            String current=row.anchor()?"  -  ATTUALE":"";
            JLabel title=new JLabel("Stagione "+row.seasonId()+"  (#"+row.seasonNumber()+")  -  "+row.managementType()+current);title.setFont(title.getFont().deriveFont(Font.BOLD,14f));title.setForeground(new Color(25,67,160));panel.add(title,g);
            JPanel flags=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));flags.setOpaque(false);flags.add(include);JButton remove=new JButton("Rimuovi");remove.addActionListener(e->remove(this));flags.add(remove);g.gridx=2;g.gridwidth=2;g.weightx=1;g.anchor=GridBagConstraints.EAST;panel.add(flags,g);
            if("GESTITA".equals(row.managementType())) {
                addPath("File FCM",fcm,1,JFileChooser.FILES_ONLY,".fcm");
                addPath("File FCA",fca,2,JFileChooser.FILES_ONLY,".fca");
                addPath("Cartella sito locale (opzionale)",site,3,JFileChooser.DIRECTORIES_ONLY,null);
                addText("Sito online",online,4);
                site.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){public void insertUpdate(javax.swing.event.DocumentEvent e){updateDerived();}public void removeUpdate(javax.swing.event.DocumentEvent e){updateDerived();}public void changedUpdate(javax.swing.event.DocumentEvent e){updateDerived();}});
                addLabel("Cartella JS",js,5);
                addLabel("Calendario e tabellini",dataa,6);
            } else {
                addReadOnly("Configurazione", "Solo riferimento storico: " + row.seasonId().replace('_','/') + " · stagione n. " + row.seasonNumber(), 1);
                addReadOnly("Dati gara", "FCM, FCA, sito, calendario e tabellini non previsti", 2);
            }
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
        void updateDerived(){

            String startYear=row.seasonId().substring(0,4);

            Path archivedDataA=projectRoot.resolve("data/calendars/DataA-"+startYear+".js").normalize();



            String s=site.getText().trim();

            boolean recoveredFromSite=false;



            if(!Files.isRegularFile(archivedDataA) && !s.isEmpty()){

                Path siteDataA=Path.of(s).resolve("js").resolve("DataA.js").normalize();

                if(Files.isRegularFile(siteDataA)){

                    try{

                        Files.createDirectories(archivedDataA.getParent());

                        Files.copy(siteDataA,archivedDataA,StandardCopyOption.REPLACE_EXISTING);

                        recoveredFromSite=true;

                    }catch(IOException ignored){ }

                }

            }



            boolean dataAFound=Files.isRegularFile(archivedDataA);

            String calendarStatus=dataAFound

                ? (recoveredFromSite?"Calendario disponibile (recuperato dal sito)":"Calendario disponibile")

                : "Calendario non disponibile";



            if(s.isEmpty()){

                js.setText("-");

                dataa.setText(calendarStatus+" · sito non configurato");

                dataa.setForeground(dataAFound?new Color(20,120,55):new Color(170,55,35));

                return;

            }



            Path rootSite=Path.of(s);

            Path j=rootSite.resolve("js");

            js.setText(j.toString());



            String matchPage=detectMatchPage(rootSite);

            dataa.setText(calendarStatus+" · tabellini: "+matchPage);

            dataa.setForeground(dataAFound?new Color(20,120,55):new Color(170,55,35));

        }


        String detectMatchPage(Path siteRoot){
            if(!Files.isDirectory(siteRoot)) return "sito non disponibile";
            try(var files=Files.list(siteRoot)){
                return files.filter(Files::isRegularFile)
                    .map(p->p.getFileName().toString())
                    .filter(n->n.toLowerCase(Locale.ROOT).matches("ris.*\\.(htm|html|php)"))
                    .sorted().findFirst().orElse("nessun ris*.htm/html/php");
            }catch(IOException ex){return "non rilevabile";}
        }
        String validateFields(){
            if("MANUALE".equals(row.managementType())) return null;
            if(fcm.getText().trim().isEmpty()||!Files.isRegularFile(Path.of(fcm.getText().trim())))return row.seasonId()+": selezionare un file FCM esistente.";
            if(fca.getText().trim().isEmpty()||!Files.isRegularFile(Path.of(fca.getText().trim())))return row.seasonId()+": selezionare un file FCA esistente.";
            String sitePath=site.getText().trim();
            if(!sitePath.isEmpty()&&!Files.isDirectory(Path.of(sitePath)))return row.seasonId()+": la cartella sito indicata non esiste; correggerla oppure lasciare il campo vuoto.";
            return null;
        }
        SeasonConfigurationRepository.SeasonRow value(){
            boolean managed="GESTITA".equals(row.managementType());
            String normalizedOnline = managed ? normalizeOnlineUrl(online.getText()) : "";
            if (managed) online.setText(normalizedOnline);
            return new SeasonConfigurationRepository.SeasonRow(
                row.seasonId(),
                row.seasonNumber(),
                row.anchor(),
                row.managementType(),
                row.status(),
                managed?fcm.getText().trim():"",
                managed?fca.getText().trim():"",
                managed?site.getText().trim():"",
                normalizedOnline
            );
        }
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

        private final CardLayout modeCards = new CardLayout();

        private final JPanel modePanel = new JPanel(modeCards);



        private void build() {

            setLayout(new BorderLayout(10, 10));

            ((JComponent) getContentPane()).setBorder(new EmptyBorder(14, 16, 12, 16));



            ButtonGroup group = new ButtonGroup();

            group.add(managed);

            group.add(manual);



            JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

            typePanel.setBorder(new TitledBorder("Tipo di stagione"));

            typePanel.add(managed);

            typePanel.add(manual);

            add(typePanel, BorderLayout.NORTH);



            JPanel managedPanel = new JPanel(new GridBagLayout());

            managedPanel.setBorder(new TitledBorder("Sorgenti stagione gestita"));

            addChooser(managedPanel, "File FCM", fcm, 0, ".fcm");

            addChooser(managedPanel, "File FCA", fca, 1, ".fca");

            addValue(managedPanel, "Dati rilevati", detected, 2);



            JPanel manualPanel = new JPanel(new GridBagLayout());

            manualPanel.setBorder(new TitledBorder("Dati stagione manuale"));

            addField(manualPanel, "Anni stagione (AAAA/AAAA)", manualSeason, 0);

            addField(manualPanel, "Numero stagione", manualNumber, 1);

            JLabel manualHint = new JLabel("Le stagioni manuali non richiedono file FCM/FCA, sito o tabellini.");

            manualHint.setForeground(Color.GRAY);

            addValue(manualPanel, "Nota", manualHint, 2);



            modePanel.add(managedPanel, "GESTITA");

            modePanel.add(manualPanel, "MANUALE");

            add(modePanel, BorderLayout.CENTER);



            managed.addActionListener(e -> updateMode());

            manual.addActionListener(e -> updateMode());

            updateMode();



            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

            JButton add = new JButton("Aggiungi stagione");

            JButton cancel = new JButton("Annulla");

            add.addActionListener(e -> finish());

            cancel.addActionListener(e -> dispose());

            buttons.add(add);

            buttons.add(cancel);

            add(buttons, BorderLayout.SOUTH);



            setSize(720, 330);

            setLocationRelativeTo(getOwner());

        }



        private void updateMode() {

            boolean isManaged = managed.isSelected();

            modeCards.show(modePanel, isManaged ? "GESTITA" : "MANUALE");

            detected.setText(isManaged

                ? "Stagione e numero saranno letti dal file FCM."

                : " ");

            revalidate();

            repaint();

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
                    String years = manualSeason.getText().trim();
                    if (!years.matches("\\d{4}/\\d{4}")) {
                        warn("Usare il formato anni AAAA/AAAA, ad esempio 2005/2006.");
                        return;
                    }
                    int startYear = Integer.parseInt(years.substring(0,4));
                    int endYear = Integer.parseInt(years.substring(5,9));
                    if (endYear != startYear + 1) {
                        warn("Il secondo anno deve essere successivo al primo.");
                        return;
                    }
                    seasonId = years.replace('/', '_');
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
                    managed.isSelected() ? fca.getText().trim() : "",
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