package it.alterlega.recordsnext.gui;

import it.alterlega.recordsnext.app.topflop.TopFlopPlayerIdentities;
import it.alterlega.recordsnext.app.topflop.TopFlopViewsPublication;
import it.alterlega.recordsnext.app.topflop.TopFlopPlayerIdentities.Audit;
import it.alterlega.recordsnext.app.topflop.TopFlopPlayerIdentities.Proposal;
import it.alterlega.recordsnext.app.topflop.TopFlopPlayerIdentities.Registry;
import it.alterlega.recordsnext.app.topflop.TopFlopPlayerIdentities.Source;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Read-only evidence and explicitly authorized, atomic approval of historical careers.
 * Follows the same grouping used by the published-carrier audit v108:
 *   exact date of birth + normalized surname + normalized first given name.
 * This is a REVIEW CANDIDATE, never independent proof of identity.
 * No database, FCA/FCM or public JS files are ever changed here.
 */
public final class TopFlopHistoricalCareerDialogV109 extends JDialog {
    private record Group(String displayName, String birth, List<Source> members,
                         String status, int oldIdentities, boolean sameSeason,
                         boolean sameFullName, String evidence) {
        String shortEvidence() { return members.stream()
                .map(s -> s.season()+": "+s.name()+" (#"+s.code()+")")
                .collect(Collectors.joining(" | ")); }
        boolean bulkEligible() {
            // Never batch-approve conflicts between previously approved career groups,
            // same-season collisions or cases already marked as separate by source records.
            return !sameSeason && oldIdentities <= 1 && !status.equals("UNIFICATO")
                && !status.startsWith("BLOCCATA");
        }
    }
    private final Registry registry;
    private final Audit audit;
    private final Path registryPath;
    private final Path dbPath;
    private final List<Group> groups = new ArrayList<>();
    private final JTable table;
    private final JLabel statusLabel = new JLabel();
    private final JTextArea detail = new JTextArea(7, 100);
    private final JCheckBox permission = new JCheckBox("Ho esaminato i gruppi selezionati e autorizzo i collegamenti");
    private final TableRowSorter<AbstractTableModel> sorter;

    public TopFlopHistoricalCareerDialogV109(Dialog owner, Registry registry, Audit audit,
                                             Path registryPath, Path projectRoot) throws Exception {
        super(owner, "Carriere storiche separate — audit v108", true);
        this.registry = registry;
        this.audit = audit;
        this.registryPath = registryPath;
        this.dbPath = projectRoot.resolve("data").resolve("database").resolve("recordsnext.db");
        refreshGroups();
        var model = new AbstractTableModel() {
            public int getRowCount() { return groups.size(); }
            public int getColumnCount() { return 6; }
            public String getColumnName(int column) { return switch(column) {
                case 0 -> "Giocatore"; case 1 -> "Nascita"; case 2 -> "Annate";
                case 3 -> "Identita'"; case 4 -> "Stato"; default -> "Evidenza";
            }; }
            public Object getValueAt(int row, int col) {
                Group g = groups.get(row);
                return switch(col) {
                    case 0 -> g.displayName(); case 1 -> g.birth();
                    case 2 -> g.members().stream().map(Source::season).distinct().count();
                    case 3 -> g.members().size(); case 4 -> g.status();
                    default -> g.shortEvidence();
                };
            }
        };
        setLayout(new BorderLayout(8, 8));
        JPanel top = new JPanel(new BorderLayout(5, 5));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        top.add(new JLabel("<html><b>Candidature basate su nome, primo nome e nascita.</b> " +
            "Non sono identificazioni certe. Nessuna fusione senza selezione e conferma esplicita. " +
            "Gruppi con stagioni sovrapposte o identita' approvate differenti sono esclusi dalla selezione rapida.</html>"),
            BorderLayout.NORTH);
        top.add(statusLabel, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);
        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setRowHeight(27);
        table.getColumnModel().getColumn(0).setPreferredWidth(185);
        table.getColumnModel().getColumn(1).setPreferredWidth(95);
        table.getColumnModel().getColumn(2).setPreferredWidth(65);
        table.getColumnModel().getColumn(3).setPreferredWidth(60);
        table.getColumnModel().getColumn(4).setPreferredWidth(175);
        table.getColumnModel().getColumn(5).setPreferredWidth(640);
        table.getSelectionModel().addListSelectionListener(e -> {
            if(!e.getValueIsAdjusting()) renderDetail();
        });
        detail.setEditable(false);
        detail.setLineWrap(true);
        detail.setWrapStyleWord(true);
        detail.setText("Selezionare una carriera per visualizzare tutti i record FCA stagionali.");
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            new JScrollPane(table), new JScrollPane(detail));
        split.setResizeWeight(0.75);
        add(split, BorderLayout.CENTER);

        JPanel controls = new JPanel(new BorderLayout(4, 4));
        controls.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        JButton selectSafe = new JButton("Seleziona candidati senza collisioni");
        selectSafe.addActionListener(e -> selectEligible());
        JButton approve = new JButton("Approva SOLO selezionati");
        approve.addActionListener(e -> approveSelection());
        JButton critical=new JButton("Esamina canoniche distinte (una per volta)");
        critical.addActionListener(e -> approveCriticalSingle());
        JButton collisionFix = new JButton("Risolvi Taider / Neuton (schede doppie)");
        collisionFix.addActionListener(e -> approveReviewedCollision());
        JButton reload = new JButton("Aggiorna elenco");
        reload.addActionListener(e -> {
            try { refreshGroups(); model.fireTableDataChanged(); updateStatus(); }
            catch (Exception ex) { showError(ex); }
        });
        JButton close = new JButton("Chiudi");
        close.addActionListener(e -> dispose());
        buttons.add(selectSafe); buttons.add(approve); buttons.add(critical);
        buttons.add(reload); buttons.add(close);
        JPanel specialActions = new JPanel(new FlowLayout(FlowLayout.RIGHT,6,2));
        specialActions.add(collisionFix);
        JPanel twoRows = new JPanel(new BorderLayout());
        twoRows.add(buttons,BorderLayout.NORTH);
        twoRows.add(specialActions,BorderLayout.SOUTH);
        controls.add(permission, BorderLayout.WEST);
        controls.add(twoRows, BorderLayout.EAST);
        add(controls, BorderLayout.SOUTH);
        updateStatus();
        setSize(1520, 790);
        setLocationRelativeTo(owner);
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value == null ? "" : value.toUpperCase(java.util.Locale.ROOT),
                Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .replaceAll("\\s+", " ").trim();
    }
    private static String birthday(String birth) {
        if(birth == null || birth.length()<10) return null;
        String d = birth.substring(0,10);
        return d.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}") ? d : null;
    }
    private static String groupKey(Source s) {
        String date = birthday(s.birth());
        if(date == null) return null;
        String[] words = normalize(s.name()).split(" ");
        if(words.length<2)return null;
        // EL AZZOUZI is a two-token surname: Oussama and Anouar are
        // DIFFERENT footballers born on the same date, not a broken career.
        if(words.length>=3 && words[0].equals("EL") && words[1].equals("AZZOUZI"))
            return date+"|EL AZZOUZI|"+words[2];
        return date+"|"+words[0]+"|"+words[1];
    }
    private void refreshGroups() throws Exception {
        if(!Files.isRegularFile(dbPath)) throw new IllegalStateException("SQLite assente: " + dbPath);
        Map<String, List<Source>> candidates = new LinkedHashMap<>();
        Class.forName("org.sqlite.JDBC");
        try(Connection c = DriverManager.getConnection("jdbc:sqlite:"+dbPath.toUri()+"?mode=ro")) {
            try(Statement ro=c.createStatement()) { ro.execute("PRAGMA query_only=ON"); }
            List<String> seasons = new ArrayList<>();
            try(Statement st=c.createStatement(); ResultSet rs=st.executeQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name LIKE 'raw_%_fca_giocatorea'")) {
                while(rs.next()) {
                    String name=rs.getString(1);
                    if(name.matches("raw_[0-9]{4}_[0-9]{4}_fca_giocatorea")) seasons.add(name.substring(4,13));
                }
            }
            if(seasons.size()!=21) throw new IllegalStateException(
                "Stagioni disponibili: "+seasons.size()+" (attese 21); nessuna approvazione possibile");
            seasons.sort(String::compareTo);
            for(String season : seasons) {
                String tableName="raw_"+season+"_fca_giocatorea";
                try(Statement st=c.createStatement(); ResultSet rs=st.executeQuery(
                    "SELECT ID,CODICEGAZZA,NOME,DATADINASCITA FROM \""+tableName+"\"")) {
                    while(rs.next()) {
                        String name=rs.getString(3), birth=rs.getString(4);
                        if(name == null || birth == null) continue;
                        int code=rs.getInt(2);
                        Integer realCode=rs.wasNull()||code<=0 ? null : code;
                        Source source=new Source(season,rs.getInt(1),realCode,name,birth);
                        String key=groupKey(source);
                        if(key != null) candidates.computeIfAbsent(key,x->new ArrayList<>()).add(source);
                    }
                }
            }
        }
        Map<String,String> approved=registry.approved();
        List<Group> next = new ArrayList<>();
        for(var e:candidates.entrySet()) {
            List<Source> members=e.getValue();
            if(members.stream().map(Source::season).distinct().count()<2) continue;
            members.sort(Comparator.comparing(Source::key));
            Set<String> identities=new HashSet<>();
            Set<String> assigned=new HashSet<>();
            int assignedCount=0;
            for(Source member:members) {
                String existing=approved.get(member.key());
                if(existing != null) { assignedCount++; assigned.add(existing); }
                identities.add(existing == null ? member.key() : existing);
            }
            if(identities.size()==1) continue;
            boolean collision=members.stream().map(Source::season).distinct().count()!=members.size();
            boolean sameName=members.stream().map(s->normalize(s.name())).distinct().count()==1;
            String state=collision?"COLLISIONE STAGIONE":
                assigned.size()>1?"CANONICHE DISTINTE":
                assignedCount==0?"NON COLLEGATA":
                assignedCount<members.size()?"COLLEGAMENTO PARZIALE":"DA ESAMINARE";
            String[] labels=e.getKey().split("\\|",3);
            next.add(new Group(labels[1]+" "+labels[2],labels[0],List.copyOf(members),
                state,assigned.size(),collision,sameName,""));
        }
        next.sort(Comparator.comparing((Group g)->!g.bulkEligible())
            .thenComparing((Group g)->-g.members().size())
            .thenComparing(Group::displayName));
        // Simulate all proposed approvals IN MEMORY, in the same order used at commit.
        // This removes groups which would fail because of an approved separation or
        // a transitive overlapping season, without writing the registry.
        Registry simulation=registry.snapshot();
        List<Group> screened=new ArrayList<>();
        for(Group g:next) {
            if(!g.bulkEligible()) { screened.add(g); continue; }
            try {
                Proposal p=new Proposal(fingerprint(g.members()),g.members(),false,
                    "Candidato storico sottoposto a verifica");
                TopFlopPlayerIdentities.requireResolvedCollisions(simulation,audit,p);
                simulation.approve(p);
                screened.add(g);
            } catch(Exception ex) {
                screened.add(new Group(g.displayName(),g.birth(),g.members(),
                    "BLOCCATA: conflitto di identita'",g.oldIdentities(),g.sameSeason(),
                    g.sameFullName(),ex.getMessage()));
            }
        }
        groups.clear(); groups.addAll(screened);
    }
    private void updateStatus() {
        long eligible=groups.stream().filter(Group::bulkEligible).count();
        long collisions=groups.stream().filter(Group::sameSeason).count();
        long distinct=groups.stream().filter(g->g.oldIdentities()>1).count();
        statusLabel.setText("Da riconciliare: "+groups.size()+"  |  senza collisioni: "+eligible+
                "  |  stagioni sovrapposte: "+collisions+
                "  |  canoniche gia' distinte: "+distinct+
                "  |  selezionati: "+table.getSelectedRowCount());
    }
    private void renderDetail() {
        int sel=table.getSelectedRow();
        if(sel<0) { detail.setText("Selezionare una o piu' righe per esaminarne gli elementi."); updateStatus(); return; }
        Group g=groups.get(table.convertRowIndexToModel(sel));
        StringBuilder b=new StringBuilder(g.displayName()+" — "+g.birth()+"\n"+
            "Stato: "+g.status()+"; nome completo uguale in tutte le stagioni: "+g.sameFullName()+"\n\n");
        Map<String,String> current=registry.approved();
        for(Source s:g.members()) b.append(s.season()).append(" | ").append(s.name())
            .append(" | codice ").append(s.code()).append(" | ").append(s.key())
            .append(" | canonica ").append(current.getOrDefault(s.key(),"NON ASSOCIATO")).append('\n');
        detail.setText(b.toString()); detail.setCaretPosition(0);
        updateStatus();
    }
    private void selectEligible() {
        table.getSelectionModel().setValueIsAdjusting(true);
        try {
            table.clearSelection();
            for(int row=0;row<table.getRowCount();row++) {
                Group g=groups.get(table.convertRowIndexToModel(row));
                if(g.bulkEligible()) table.addRowSelectionInterval(row,row);
            }
        } finally {
            table.getSelectionModel().setValueIsAdjusting(false);
        }
        updateStatus();
    }
    private static String fingerprint(List<Source> members) throws Exception {
        String evidence=members.stream().sorted(Comparator.comparing(Source::key))
            .map(s->s.key()+"/"+s.code()+"/"+s.name()+"/"+s.birth())
            .collect(Collectors.joining("|"));
        byte[] digest=MessageDigest.getInstance("SHA-256").digest(evidence.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex=new StringBuilder();
        for(byte b:digest) hex.append(String.format("%02x", b&255));
        return hex.toString();
    }
    private void approveSelection() {
        int[] selected=table.getSelectedRows();
        if(selected.length==0) { JOptionPane.showMessageDialog(this,"Seleziona almeno una carriera."); return; }
        if(!permission.isSelected()) {
            JOptionPane.showMessageDialog(this,"Per approvare, seleziona la casella di conferma dopo aver esaminato i gruppi.");
            return;
        }
        List<Group> chosen=new ArrayList<>();
        for(int idx:selected) {
            Group g=groups.get(table.convertRowIndexToModel(idx));
            if(!g.bulkEligible()) {
                JOptionPane.showMessageDialog(this,"La selezione contiene un gruppo con conflitto: "+g.displayName()+
                    ". Non verra' applicata alcuna modifica.");
                return;
            }
            chosen.add(g);
        }
        chosen.sort(Comparator.comparingInt(groups::indexOf));
        StringBuilder preview=new StringBuilder();
        for(Group g:chosen) {
            preview.append("=== ").append(g.displayName()).append(" ").append(g.birth())
                .append(" (n. ").append(g.members().size()).append(") ===\n")
                .append(g.shortEvidence()).append("\n\n");
        }
        JTextArea text=new JTextArea(preview.toString(),23,105);
        text.setEditable(false); text.setLineWrap(true); text.setWrapStyleWord(true);
        JScrollPane scroll=new JScrollPane(text);
        scroll.setPreferredSize(new Dimension(970,410));
        int reply=JOptionPane.showConfirmDialog(this,scroll,
            "CONFERMA ESPLICITA: unificare "+chosen.size()+" carriere?",JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        if(reply != JOptionPane.YES_OPTION) return;
        Registry before=registry.snapshot();
        Path backup=null;
        boolean saved=false;
        try {
            // Snapshot source registry once, then a single registry save.
            if(Files.isRegularFile(registryPath)) {
                String stamp=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                backup=registryPath.resolveSibling("player-identities-PRE_v109_"+stamp+".json");
                Files.copy(registryPath,backup,StandardCopyOption.COPY_ATTRIBUTES);
            }
            for(Group g:chosen) {
                Proposal p=new Proposal(fingerprint(g.members()),g.members(),false,
                    "Approvazione esplicita nella GUI v109: nome+nascita, "+g.displayName());
                TopFlopPlayerIdentities.requireResolvedCollisions(registry,audit,p);
                registry.approve(p);
            }
            TopFlopPlayerIdentities.save(registryPath,registry);
            saved=true;
            refreshGroups();
            ((AbstractTableModel)table.getModel()).fireTableDataChanged();
            permission.setSelected(false);
            updateStatus();
            JOptionPane.showMessageDialog(this,"Approvate "+chosen.size()+" carriere. Backup registro: "+backup+
                "\nLe modifiche restano locali: dopo la verifica ripubblicare i siti con Elabora.");
        } catch(Exception ex) {
            if(!saved) {
                registry.restore(before);
                showError(new IllegalStateException("Nessuna approvazione parziale salvata. "+ex.getMessage(),ex));
            } else {
                showError(new IllegalStateException("Registro salvato, ma aggiornamento dell'elenco non riuscito. " +
                    "Chiudere e riaprire la finestra. Backup: "+backup+". "+ex.getMessage(),ex));
            }
        }
    }
    /** Only an individual, double-confirmed decision may merge two already-approved careers. */
    private void approveCriticalSingle() {
        if(table.getSelectedRowCount()!=1) {
            JOptionPane.showMessageDialog(this,"Seleziona UNA SOLA carriera con canoniche distinte.");return;
        }
        Group g=groups.get(table.convertRowIndexToModel(table.getSelectedRow()));
        if(g.sameSeason() || g.oldIdentities()<2) {
            JOptionPane.showMessageDialog(this,"Operazione riservata a canoniche distinte senza stagioni sovrapposte.");
            return;
        }
        if(!permission.isSelected()) {
            JOptionPane.showMessageDialog(this,"Conferma prima di aver verificato le identita' nella casella.");
            return;
        }
        StringBuilder facts=new StringBuilder(g.displayName()+" ("+g.birth()+")\n"+
            "ATTENZIONE: unione di "+g.oldIdentities()+" carriere gia' approvate.\n\n");
        Map<String,String> assigned=registry.approved();
        for(Source s:g.members()) facts.append(s.season()).append(" | ").append(s.name())
            .append(" | codice ").append(s.code()).append(" | ")
            .append(assigned.getOrDefault(s.key(),"NON APPROVATO")).append('\n');
        JTextArea details=new JTextArea(facts.toString(),17,90);
        details.setEditable(false);
        JScrollPane scroll=new JScrollPane(details);
        scroll.setPreferredSize(new Dimension(870,310));
        if(JOptionPane.showConfirmDialog(this,scroll,"Verifica identita' approvate - "+g.displayName(),
              JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE)!=JOptionPane.YES_OPTION) return;
        if(JOptionPane.showConfirmDialog(this,
             "Confermi ESPRESSAMENTE che TUTTE queste anagrafiche sono lo stesso calciatore?",
             "Seconda conferma - "+g.displayName(),JOptionPane.YES_NO_OPTION,
             JOptionPane.WARNING_MESSAGE)!=JOptionPane.YES_OPTION) return;
        Registry before=registry.snapshot();
        Path backup=null;
        boolean saved=false;
        try {
            if(Files.isRegularFile(registryPath)) {
                String stamp=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                backup=registryPath.resolveSibling("player-identities-PRE_CRITICA_v109_"+stamp+".json");
                Files.copy(registryPath,backup,StandardCopyOption.COPY_ATTRIBUTES);
            }
            Proposal proposal=new Proposal(fingerprint(g.members()),g.members(),false,
                "Unione di canoniche preesistenti confermata due volte nella GUI v109");
            TopFlopPlayerIdentities.requireResolvedCollisions(registry,audit,proposal);
            registry.approve(proposal);
            TopFlopPlayerIdentities.save(registryPath,registry);
            saved=true;
            refreshGroups();
            ((AbstractTableModel)table.getModel()).fireTableDataChanged();
            permission.setSelected(false);
            updateStatus();
            JOptionPane.showMessageDialog(this,"Unione approvata. Backup registro: "+backup);
        }catch(Exception ex) {
            if(!saved)registry.restore(before);
            showError(new IllegalStateException(saved ?
                "Registro salvato, ma elenco non aggiornato: riaprire la finestra. " + ex.getMessage() :
                "Nessuna unione salvata: " + ex.getMessage(),ex));
        }
    }
    /** Strictly scoped to the two verified historical identities. No bulk
     * merge, no user-edited dates, no guessed homonyms or sibling names.
     */
    private void approveReviewedCollision() {
        if(table.getSelectedRowCount()!=1) {
            JOptionPane.showMessageDialog(this,"Seleziona soltanto Taider oppure Neuton.");return;
        }
        Group g=groups.get(table.convertRowIndexToModel(table.getSelectedRow()));
        String primary,secondary,expectedBirth;
        Map<String,Integer> codes=new LinkedHashMap<>();
        if(g.displayName().equals("TAIDER SAPHIR") && g.birth().equals("1992-02-29")) {
            primary="2014_2015:606";secondary="2014_2015:738";expectedBirth="1992-02-29";
            codes.put("2011_2012:679",123046);codes.put("2012_2013:586",123046);
            codes.put("2013_2014:612",123046);codes.put(primary,123046);
            codes.put(secondary,124530);codes.put("2015_2016:653",124530);
            codes.put("2016_2017:623",124530);codes.put("2017_2018:560",100558);
        } else if(g.displayName().equals("NEUTON SERGIO") && g.birth().equals("1990-03-14")) {
            primary="2013_2014:798";secondary="2013_2014:448";expectedBirth="1990-03-14";
            codes.put("2011_2012:405",123028);codes.put("2012_2013:425",123028);
            codes.put(primary,123028);codes.put(secondary,123130);
            codes.put("2014_2015:853",123028);codes.put("2015_2016:483",123028);
        } else {
            JOptionPane.showMessageDialog(this,"Questa funzione e' riservata a Taider e Neuton.");return;
        }
        if(!permission.isSelected()) {
            JOptionPane.showMessageDialog(this,"Seleziona la casella di verifica prima della correzione.");return;
        }
        try {
            Map<String,Source> byKey=g.members().stream().collect(Collectors.toMap(Source::key,s->s));
            if(byKey.size()!=codes.size() || !byKey.keySet().equals(codes.keySet()))
                throw new IllegalStateException("Righe FCA diverse dalla prova esaminata. NON applico la fusione.");
            for(var e:codes.entrySet()) {
                Source s=byKey.get(e.getKey());
                if(!expectedBirth.equals(birthday(s.birth())) || !e.getValue().equals(s.code()) ||
                   !normalize(s.name()).startsWith(g.displayName()))
                    throw new IllegalStateException("Anagrafica inattesa: "+s.key()+" / "+s.name());
            }
            String season=primary.substring(0,9);
            Map<Integer,Set<Integer>> days=new LinkedHashMap<>();
            try(Connection db=DriverManager.getConnection("jdbc:sqlite:"+dbPath.toUri()+"?mode=ro")) {
                try(Statement ro=db.createStatement()){ro.execute("PRAGMA query_only=ON");}
                for(String key:List.of(primary,secondary)) {
                    int id=Integer.parseInt(key.substring(10));
                    days.put(id,TopFlopViewsPublication.meaningfulFcaDays(db,season,id));
                }
            }
            Set<Integer> overlap=new LinkedHashSet<>(days.get(Integer.parseInt(primary.substring(10))));
            overlap.retainAll(days.get(Integer.parseInt(secondary.substring(10))));
            if(!overlap.isEmpty()) throw new IllegalStateException(
                "DUE prestazioni FCA effettive nella stessa giornata "+overlap+". " +
                "Nessuna modifica: confrontare voti/eventi prima di approvare.");
            StringBuilder proof=new StringBuilder("Carriera: "+g.displayName()+" ("+expectedBirth+")\n"+
                "Coppia FCA nella stessa stagione: "+primary+" + "+secondary+"\n"+
                "GIORNATE CON VOTI/EVENTI reali (righe vuote escluse): "+days+"\n"+
                "TUTTE le prestazioni sono conservate; nella pubblicazione vengono SOMMATE, " +
                "con tracciamento delle due sorgenti.\n\n");
            for(Source s:g.members())proof.append(s.key()).append(" | ").append(s.name())
                .append(" | codice ").append(s.code()).append("\n");
            JTextArea details=new JTextArea(proof.toString(),19,100);
            details.setEditable(false);
            if(JOptionPane.showConfirmDialog(this,new JScrollPane(details),
                "Unificare la carriera approvando il duplicato FCA?",JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE)!=JOptionPane.YES_OPTION)return;
            Registry before=registry.snapshot();
            boolean saved=false;
            try {
                String stamp=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                Path backup=registryPath.resolveSibling("player-identities-PRE_v112_"+stamp+".json");
                Files.copy(registryPath,backup,StandardCopyOption.COPY_ATTRIBUTES);
                Proposal p=new Proposal(fingerprint(g.members()),g.members(),false,
                    "v112: dossier Taider/Neuton; alias duplicato FCA approvato nella GUI");
                TopFlopPlayerIdentities.requireResolvedCollisions(registry,audit,p);
                registry.approveReviewedSameSeason(p,primary,secondary);
                TopFlopPlayerIdentities.save(registryPath,registry);
                saved=true;
                refreshGroups();
                ((AbstractTableModel)table.getModel()).fireTableDataChanged();
                permission.setSelected(false);updateStatus();
                JOptionPane.showMessageDialog(this,"Collegamento confermato. Backup: "+backup+
                    "\nLe pagine dei siti NON sono ancora state aggiornate.");
            }catch(Exception ex){if(!saved)registry.restore(before);throw ex;}
        } catch(Exception ex) {showError(ex);}
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this,ex.getMessage(),"Riconciliazione interrotta",JOptionPane.ERROR_MESSAGE);
    }
}
