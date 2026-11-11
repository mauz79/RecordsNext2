package it.alterlega.recordsnext.gui;

import it.alterlega.recordsnext.app.topflop.TopFlopIdentityEvidence;
import it.alterlega.recordsnext.app.topflop.TopFlopPlayerIdentities;
import it.alterlega.recordsnext.app.topflop.TopFlopPlayerIdentities.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Approval screen for player identities: no automatic changes, ever.
 * LCS CSVs only help explain choices and are never a source of approved IDs.
 */
public final class TopFlopIdentityReviewDialog extends JDialog {
    private record Row(Proposal proposal, Collision collision, TopFlopIdentityEvidence.Assessment assessment) {
        String kind() {
            if (collision != null) return assessment.category();
            return proposal.safe() ? "CORRISPONDENZA ESATTA" : "PROPOSTA DA VERIFICARE";
        }
        String detail() { return collision!=null ? collision.description() : proposal.description(); }
        String evidence() {
            return collision!=null ? assessment.lcsEvidence() : proposal.reason();
        }
    }
    private final Registry registry;
    private final Audit audit;
    private final Path file;
    private final TopFlopIdentityEvidence evidence;
    private final List<Row> rows = new ArrayList<>();
    private final JLabel status = new JLabel();
    private final JTable table;
    private final TableRowSorter<AbstractTableModel> sorter;
    private final JTextArea detail = new JTextArea(7,100);
    private final JTextField search = new JTextField(24);
    private final JComboBox<String> filter = new JComboBox<>(new String[]{
        "Anomalie codici", "Proposte da verificare", "Proposte esatte", "Tutte", "Date giorno/mese invertiti"
    });

    public TopFlopIdentityReviewDialog(Frame owner, Path projectRoot) throws Exception {
        super(owner,"Identita' storiche calciatori - approvazione",true);
        file=projectRoot.resolve("config").resolve(TopFlopPlayerIdentities.FILE_NAME);
        registry=TopFlopPlayerIdentities.load(file);
        evidence=TopFlopIdentityEvidence.load(projectRoot.resolve("config").resolve("identity-evidence"));
        Path db=projectRoot.resolve("data/database/recordsnext.db");
        if (!Files.isRegularFile(db)) throw new IllegalStateException("SQLite non presente: " + db);
        Class.forName("org.sqlite.JDBC");
        try(Connection c=DriverManager.getConnection("jdbc:sqlite:"+db.toUri()+"?mode=ro")) {
            audit=TopFlopPlayerIdentities.scanAvailable(c);
            if(audit.sources()==0)throw new IllegalStateException(
                "Archivio FCA non ancora importato. Eseguire prima l'elaborazione completa, poi riconciliare.");
        }
        // Restore the effective constraints of decisions already confirmed in v39.
        // This does not merge or approve anything and does not write the registry.
        registry.bindAudit(audit);
        audit.collisions().forEach(c->rows.add(new Row(null,c,evidence.classify(c))));
        audit.proposals().forEach(p->rows.add(new Row(p,null,null)));

        var model=new AbstractTableModel() {
            public int getRowCount(){return rows.size();}
            public int getColumnCount(){return 5;}
            public String getColumnName(int col){return switch(col){
                case 0->"Categoria";case 1->"Stato";case 2->"Occ.";
                case 3->"Evidenza";default->"Stagioni / nomi / codici";};}
            public Object getValueAt(int r,int c){
                Row row=rows.get(r);
                return switch(c){
                    case 0->row.kind();
                    case 1->row.collision()!=null?(registry.isDistinct(row.collision())?"PERSONE DISTINTE":
                            registry.isMerged(row.collision())?"UNIONE APPROVATA":"DA DECIDERE"):
                        registry.complete(row.proposal())?"APPROVATO":
                        registry.isIgnored(row.proposal())?"RINVIATO":"DA DECIDERE";
                    case 2->row.collision()!=null?row.collision().members().size():row.proposal().members().size();
                    case 3->row.evidence();
                    default->row.detail();
                };
            }
        };
        setLayout(new BorderLayout(8,8));
        JPanel top=new JPanel(new BorderLayout(4,6));
        top.setBorder(BorderFactory.createEmptyBorder(10,12,6,12));
        top.add(new JLabel("<html><b>Nessuna fusione automatica.</b> Le fonti LCS sono indizi, non approvazioni. " +
            "Le versioni 2015, 2019 e 2020 restano distinte. Ogni decisione richiede il tuo clic.</html>"),BorderLayout.NORTH);
        top.add(status,BorderLayout.CENTER);
        JPanel selectors=new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectors.add(new JLabel("Visualizza:"));selectors.add(filter);
        selectors.add(new JLabel("Cerca nome / codice:"));selectors.add(search);
        selectors.add(new JLabel("Seleziona una riga per leggere tutti i dettagli."));
        top.add(selectors,BorderLayout.SOUTH);
        add(top,BorderLayout.NORTH);

        table=new JTable(model);
        sorter=new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setRowHeight(26);
        table.getColumnModel().getColumn(0).setPreferredWidth(190);
        table.getColumnModel().getColumn(1).setPreferredWidth(125);
        table.getColumnModel().getColumn(2).setPreferredWidth(45);
        table.getColumnModel().getColumn(3).setPreferredWidth(250);
        table.getColumnModel().getColumn(4).setPreferredWidth(600);
        table.getSelectionModel().addListSelectionListener(e-> {if(!e.getValueIsAdjusting()) updateDetails();});
        filter.addActionListener(e->updateFilter());
        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e){updateFilter();}
            public void removeUpdate(DocumentEvent e){updateFilter();}
            public void changedUpdate(DocumentEvent e){updateFilter();}
        });
        detail.setEditable(false);
        detail.setLineWrap(true);detail.setWrapStyleWord(true);
        detail.setText("Seleziona una riga per vedere nomi, stagioni e date di nascita complete.");
        JScrollPane detailScroll=new JScrollPane(detail);
        detailScroll.setBorder(BorderFactory.createTitledBorder("Dettaglio delle anagrafiche e riscontri LCS"));
        JSplitPane body=new JSplitPane(JSplitPane.VERTICAL_SPLIT,new JScrollPane(table),detailScroll);
        body.setResizeWeight(0.75);
        add(body,BorderLayout.CENTER);

        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,6,5));
        JButton exact=new JButton("Approva le proposte esatte");
        exact.addActionListener(e->{
            long pending=audit.proposals().stream().filter(p->p.safe()&&!registry.complete(p)).count();
            if(pending==0){JOptionPane.showMessageDialog(this,"Nessuna proposta esatta ancora da approvare.");return;}
            int reply=JOptionPane.showConfirmDialog(this,
                "Approvare esplicitamente "+pending+" corrispondenze esatte?\n"+
                "I codici ambigui non saranno inclusi.","Approvazione collettiva",JOptionPane.YES_NO_OPTION);
            if(reply==JOptionPane.YES_OPTION)act(()->{
                for(Proposal p:audit.proposals())if(p.safe()&&!registry.complete(p))registry.approve(p);
            });
        });
        JButton approve=new JButton("Approva proposte selezionate");
        approve.addActionListener(e->{
            List<Row> chosen=selected().stream().filter(r->r.proposal()!=null&&!registry.complete(r.proposal())).toList();
            if(chosen.isEmpty()){JOptionPane.showMessageDialog(this,"Nessuna proposta non approvata selezionata.");return;}
            // Always show the operator the evidence before changing any history.
            StringBuilder preview=new StringBuilder();
            int linkingPreviouslyApproved=0;
            for(Row r:chosen){
                int existing=registry.existingCareerGroups(r.proposal());
                if(existing>1)linkingPreviouslyApproved++;
                preview.append(r.proposal().reason()).append(" | carriere gia' approvate: ").append(existing).append("\n");
                for(Source source:r.proposal().members())preview.append("  ")
                    .append(source.season()).append(" | ").append(source.name())
                    .append(" | codice ").append(source.code())
                    .append(" | nascita ").append(source.birth()).append("\n");
                preview.append("\n");
            }
            String title="Approvare ESPRESSAMENTE "+chosen.size()+" proposte?";
            String note=linkingPreviouslyApproved>0
                ?"ATTENZIONE: "+linkingPreviouslyApproved+" proposte collegano carriere gia' approvate."
                :"Le proposte selezionate saranno associate solo dopo la conferma.";
            if(confirmDetails(preview.toString(),title,note)!=JOptionPane.YES_OPTION)return;
            act(()->{
                for(Row r:chosen){
                    TopFlopPlayerIdentities.requireResolvedCollisions(registry,audit,r.proposal());
                    registry.approve(r.proposal());
                }
            });
        });
        JButton distinct=new JButton("Conferma persone diverse");
        distinct.addActionListener(e->{
            List<Row> chosen=selected();
            if(chosen.stream().noneMatch(r->r.collision()!=null)){
                JOptionPane.showMessageDialog(this,"Seleziona almeno un codice ambiguo.");return;
            }
            int ok=JOptionPane.showConfirmDialog(this,
                "Confermi che le identita' collegate ai codici selezionati NON devono essere unite?\n"+
                "Le proposte interne resteranno esaminabili separatamente.",
                "Separazione identita'",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
            if(ok==JOptionPane.YES_OPTION)act(()->{
                for(Row r:chosen)if(r.collision()!=null)registry.distinguish(r.collision());
            });
        });
        JButton same=new JButton("Conferma stessa persona");
        same.addActionListener(e->{
            // A mixed multi-selection is valid: review every critical case separately,
            // while low-risk variants can be explicitly accepted as one batch.
            // A cancellation causes NO changes; a declined case remains pending.
            List<Row> chosen=selected().stream().filter(r->r.collision()!=null).toList();
            if(chosen.isEmpty()){
                JOptionPane.showMessageDialog(this,"Seleziona almeno un codice da riconciliare.");return;
            }
            for(Row r:chosen)if(registry.isDistinct(r.collision())){
                JOptionPane.showMessageDialog(this,
                    "Il codice "+r.collision().code()+" e' gia' PERSONE DISTINTE.\n"+
                    "Per cambiare questa scelta, revocala prima esplicitamente.");
                return;
            }
            List<Row> pending=chosen.stream().filter(r->!registry.isMerged(r.collision())).toList();
            if(pending.isEmpty()){
                JOptionPane.showMessageDialog(this,"Le identita' selezionate sono gia' approvate.");return;
            }
            List<Row> straightforward=pending.stream()
                .filter(r->!r.assessment().requiresIndividualReview()).toList();
            List<Row> critical=pending.stream()
                .filter(r->r.assessment().requiresIndividualReview()).toList();
            List<Row> accepted=new ArrayList<>();
            if(!straightforward.isEmpty()){
                StringBuilder names=new StringBuilder();
                for(Row r:straightforward)names.append("Codice ").append(r.collision().code())
                    .append(" - ").append(r.assessment().category()).append("\n")
                    .append(r.collision().description()).append("\n\n");
                int choice=confirmDetails(names.toString(),
                    "Approvare "+straightforward.size()+" varianti non critiche?",
                    "Ogni codice selezionato sara' unito soltanto dopo il tuo SI'.");
                if(choice==JOptionPane.CANCEL_OPTION||choice==JOptionPane.CLOSED_OPTION)return;
                if(choice==JOptionPane.YES_OPTION)accepted.addAll(straightforward);
            }
            for(Row r:critical){
                // Exactly one explicit Yes/No for each potentially ambiguous code,
                // without asking the operator to retype an arbitrary confirmation word.
                StringBuilder facts=new StringBuilder();
                for(Source s:r.collision().members())facts.append(s.season()).append(" | ")
                    .append(s.name()).append(" | nato ").append(s.birth())
                    .append(" | codice ").append(s.code()).append("\n");
                facts.append("\n").append(r.assessment().explanation()).append("\n")
                    .append(evidence.evidenceDetails(r.collision()));
                int choice=confirmDetails(facts.toString(),
                    "Verifica individuale codice "+r.collision().code(),
                    "Unire SOLO se tutte le anagrafiche sono della stessa persona.");
                if(choice==JOptionPane.CANCEL_OPTION||choice==JOptionPane.CLOSED_OPTION)return;
                if(choice==JOptionPane.YES_OPTION)accepted.add(r);
            }
            if(!accepted.isEmpty())act(()->{
                for(Row r:accepted)registry.approveSame(r.collision());
            });
        });
        JButton internal=new JButton("Approva gruppi interni");
        internal.addActionListener(e->{
            List<Row> chosen=selected().stream().filter(r->r.collision()!=null).toList();
            if(chosen.isEmpty()){
                JOptionPane.showMessageDialog(this,"Seleziona un codice gia' segnato PERSONE DISTINTE.");return;
            }
            List<Proposal> groups=new ArrayList<>();
            for(Row r:chosen) {
                if(!registry.isDistinct(r.collision())){
                    JOptionPane.showMessageDialog(this,"Prima conferma PERSONE DISTINTE per il codice "
                        +r.collision().code());return;
                }
                for(Proposal p:TopFlopPlayerIdentities.internalGroups(r.collision()))
                    if(!registry.complete(p))groups.add(p);
            }
            if(groups.isEmpty()){
                JOptionPane.showMessageDialog(this,"Non ci sono gruppi interni verificabili da approvare.");return;
            }
            StringBuilder summary=new StringBuilder();
            for(Proposal p:groups)summary.append(p.reason()).append("\n")
                .append(p.description()).append("\n\n");
            JTextArea preview=new JTextArea(summary.toString(),Math.min(groups.size()*3+3,19),85);
            preview.setEditable(false);preview.setLineWrap(true);preview.setWrapStyleWord(true);
            int ok=JOptionPane.showConfirmDialog(this,new JScrollPane(preview),
                "Approva ESPLICITAMENTE "+groups.size()+" gruppi interni (nessuna fusione automatica)",
                JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
            if(ok==JOptionPane.YES_OPTION)act(()->{
                for(Proposal p:groups)registry.approve(p);
            });
        });
        JButton undo=new JButton("Revoca decisioni selezionate");
        undo.addActionListener(e->{
            if(selected().isEmpty()){JOptionPane.showMessageDialog(this,"Seleziona una decisione.");return;}
            act(()->{
            for(Row r:selected()){
                if(r.collision()!=null)registry.undo(r.collision());
                else registry.undo(r.proposal());
            }
        });});
        JButton ignore=new JButton("Rimanda proposte selezionate");
        ignore.addActionListener(e->{
            if(selected().stream().noneMatch(r->r.proposal()!=null)){
                JOptionPane.showMessageDialog(this,"Seleziona una proposta da rimandare.");return;
            }
            act(()->{
                for(Row r:selected())if(r.proposal()!=null)registry.ignore(r.proposal());
            });
        });
        JButton close=new JButton("Chiudi");close.addActionListener(e->dispose());
        for(JButton b:new JButton[]{exact,approve,distinct,internal,same,undo,ignore,close})actions.add(b);
        add(actions,BorderLayout.SOUTH);
        setSize(1460,790);setLocationRelativeTo(owner);
        updateFilter();updateStatus();
    }
    /** Read-only evidence preview; the action is authorized only by an explicit Yes. */
    private int confirmDetails(String facts,String title,String instruction) {
        JTextArea preview=new JTextArea(facts,Math.min(15,Math.max(5,facts.split("\n").length)),90);
        preview.setEditable(false);
        preview.setLineWrap(true);
        preview.setWrapStyleWord(true);
        JScrollPane scroll=new JScrollPane(preview);
        scroll.setPreferredSize(new Dimension(810,Math.min(300,Math.max(140,preview.getRows()*19))));
        JPanel message=new JPanel(new BorderLayout(0,8));
        message.add(new JLabel(instruction),BorderLayout.NORTH);
        message.add(scroll,BorderLayout.CENTER);
        return JOptionPane.showConfirmDialog(this,message,title,
            JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE);
    }
    private void updateFilter(){
        String needle=search.getText().strip().toUpperCase(Locale.ROOT);
        int mode=filter.getSelectedIndex();
        sorter.setRowFilter(new RowFilter<>(){
            public boolean include(Entry<? extends AbstractTableModel,? extends Integer> e){
                Row r=rows.get(e.getIdentifier());
                if(mode==0 && r.collision()==null)return false;
                if(mode==1 && (r.proposal()==null || r.proposal().safe()))return false;
                if(mode==2 && (r.proposal()==null || !r.proposal().safe()))return false;
                if(mode==4 && (r.proposal()==null || !r.proposal().reason().equals("Giorno/mese di nascita invertiti")))return false;
                String text=(r.kind()+" "+r.detail()+" "+r.evidence()).toUpperCase(Locale.ROOT);
                return needle.isEmpty()||text.contains(needle);
            }
        });
        updateDetails();
    }
    private void updateDetails(){
        int selected=table.getSelectedRow();
        if(selected<0){detail.setText("Seleziona un codice per esaminare tutte le stagioni, i nomi e le date.");return;}
        Row r=rows.get(table.convertRowIndexToModel(selected));
        var b=new StringBuilder(r.kind()+" - "+r.evidence()+"\n");
        if(r.collision()!=null){
            b.append(r.assessment().explanation()).append("\n\n");
            for(Source s:r.collision().members())b.append(s.season()).append(" | ")
                .append(s.name()).append(" | codice ").append(s.code())
                .append(" | nascita ").append(s.birth()).append("\n");
            b.append("\n").append(evidence.evidenceDetails(r.collision()));
            if(registry.isDistinct(r.collision())) {
                var groups=TopFlopPlayerIdentities.internalGroups(r.collision());
                if(groups.isEmpty())b.append("\nNessun sottogruppo verificabile; mantenere le carriere separate.");
                for(Proposal p:groups)b.append("\nGRUPPO INTERNO ")
                    .append(registry.complete(p)?"GIA' APPROVATO: ":"DA APPROVARE: ")
                    .append(p.reason()).append("\n").append(p.description()).append("\n");
            }
        } else {
            b.append(r.proposal().reason()).append("\n\n");
            for(Source s:r.proposal().members())b.append(s.season()).append(" | ")
                .append(s.name()).append(" | codice ").append(s.code())
                .append(" | nascita ").append(s.birth()).append("\n");
        }
        detail.setText(b.toString());detail.setCaretPosition(0);
    }
    private List<Row> selected(){
        List<Row> out=new ArrayList<>();
        for(int idx:table.getSelectedRows())out.add(rows.get(table.convertRowIndexToModel(idx)));
        return out;
    }
    private void act(Runnable operation){
        Registry before=registry.snapshot();
        try {
            operation.run();
            TopFlopPlayerIdentities.save(file,registry);
            ((AbstractTableModel)table.getModel()).fireTableDataChanged();
            updateStatus();updateFilter();
        } catch(Exception ex){
            registry.restore(before);
            ((AbstractTableModel)table.getModel()).fireTableDataChanged();
            JOptionPane.showMessageDialog(this,ex.getMessage(),"Verifica identita'",JOptionPane.ERROR_MESSAGE);
        }
    }
    private void updateStatus(){
        long pending=audit.proposals().stream().filter(p->!registry.complete(p)&&!registry.isIgnored(p)).count();
        long collisions=audit.collisions().stream().filter(c->!registry.isDistinct(c)&&!registry.isMerged(c)).count();
        status.setText("Anagrafiche: "+audit.sources()+"  |  proposte: "+audit.proposals().size()+
            "  |  approvazioni: "+registry.approvedIdentities()+
            "  |  in sospeso: "+pending+"  |  codici da decidere: "+collisions+
            "  |  LCS: "+evidence.aliasRows()+" correnti, "+evidence.historicalRows()+" storici"
            +" ("+evidence.historicalConflicts()+" chiavi discordanti), "+evidence.checkedRows()+" controlli");
    }
}
