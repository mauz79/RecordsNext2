package it.alterlega.recordsnext.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

final class HistoricalMappingDialog extends JDialog {
    private static final Object NEW_IDENTITY = "<Nuova identità storica>";
    private static final Object EXCLUDE = "<Non elaborare>";

    private final HistoricalMappingRepository repository;
    private final List<String> seasons;
    private int seasonIndex;
    private String seasonId;
    private final JLabel heading = new JLabel();
    private final JLabel missingCount = new JLabel();
    private final JButton nextMissing = new JButton("Vai alla prossima mancante");
    private final JTabbedPane tabs = new JTabbedPane();
    private final List<RowEditor> competitionEditors = new ArrayList<>();
    private final List<RowEditor> teamEditors = new ArrayList<>();
    private final JButton previous = new JButton("<< Indietro");
    private final JButton next = new JButton("Salva e avanti >>");
    private boolean saved;

    HistoricalMappingDialog(Window owner, HistoricalMappingRepository repository) throws Exception {
        this(owner, repository, null);
    }

    HistoricalMappingDialog(Window owner, HistoricalMappingRepository repository, String initialSeason) throws Exception {
        super(owner, "RecordsNext - Associazioni storiche", ModalityType.APPLICATION_MODAL);
        this.repository = repository;
        repository.prepare();
        this.seasons = repository.seasonsNewestFirst();
        build();
        if (!seasons.isEmpty()) {
            int index = initialSeason == null ? 0 : seasons.indexOf(initialSeason);
            loadSeason(index < 0 ? 0 : index);
        }
    }

    boolean open() {
        if (seasons.isEmpty()) {
            JOptionPane.showMessageDialog(getOwner(), "Non ci sono stagioni gestite da configurare.", "RecordsNext", JOptionPane.INFORMATION_MESSAGE);
            return true;
        }
        setVisible(true);
        return saved;
    }

    private void build() {
        setLayout(new BorderLayout(8, 8));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(10, 12, 10, 12));
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 14f));
        missingCount.setFont(missingCount.getFont().deriveFont(Font.BOLD));
        missingCount.setForeground(new Color(185, 45, 35));
        nextMissing.addActionListener(e -> focusNextMissing());
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.add(heading, BorderLayout.WEST);
        JPanel missingPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        missingPanel.add(missingCount);
        missingPanel.add(nextMissing);
        header.add(missingPanel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton("Annulla");
        cancel.addActionListener(e -> dispose());
        previous.addActionListener(e -> goPrevious());
        next.addActionListener(e -> saveAndNext());
        buttons.add(cancel); buttons.add(previous); buttons.add(next);
        add(buttons, BorderLayout.SOUTH);
        setSize(900, 700); setMinimumSize(new Dimension(780, 540)); setLocationRelativeTo(getOwner());
    }

    private void loadSeason(int index) throws Exception {
        seasonIndex = index;
        seasonId = seasons.get(index);
        competitionEditors.clear();
        teamEditors.clear();
        tabs.removeAll();
        tabs.addTab("1. Competizioni", createPage(HistoricalMappingRepository.Kind.COMPETITION, competitionEditors));
        tabs.addTab("2. Squadre", createPage(HistoricalMappingRepository.Kind.TEAM, teamEditors));
        boolean anchor = repository.isAnchor(seasonId);
        heading.setText("Stagione " + seasonId + "  (" + (index + 1) + "/" + seasons.size() + ") — " +
            (anchor ? "definizione delle identità attuali" : "associazione alle identità già definite"));
        previous.setEnabled(index > 0);
        next.setText(index == seasons.size() - 1 ? "Salva e termina" : "Salva e avanti >>");
        tabs.setSelectedIndex(0);
        updateMissingState();
        SwingUtilities.invokeLater(this::focusNextMissing);
    }

    private JScrollPane createPage(HistoricalMappingRepository.Kind kind, List<RowEditor> editors) throws Exception {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        GridBagConstraints h = new GridBagConstraints();
        h.gridy = 0; h.insets = new Insets(3, 4, 8, 4); h.anchor = GridBagConstraints.WEST;
        h.gridx = 0; h.weightx = .42; h.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel(kind == HistoricalMappingRepository.Kind.COMPETITION ? "Competizione stagione" : "Squadra stagione"), h);
        h.gridx = 1; h.weightx = .58;
        panel.add(new JLabel(kind == HistoricalMappingRepository.Kind.COMPETITION ? "Identità storica/canonica" : "Identità storica/canonica"), h);

        int row = 1;
        for (var mapping : repository.load(seasonId, kind)) {
            RowEditor editor = new RowEditor(mapping, repository.isAnchor(seasonId));
            editors.add(editor);
            GridBagConstraints g = new GridBagConstraints();
            g.gridy = row++; g.insets = new Insets(3, 4, 3, 4); g.anchor = GridBagConstraints.WEST;
            g.gridx = 0; g.weightx = .42; g.fill = GridBagConstraints.HORIZONTAL;
            panel.add(editor.sourceLabel, g);
            g.gridx = 1; g.weightx = .58;
            panel.add(editor.combo, g);
        }
        GridBagConstraints filler = new GridBagConstraints();
        filler.gridy = row; filler.weighty = 1; filler.fill = GridBagConstraints.VERTICAL;
        panel.add(Box.createVerticalGlue(), filler);
        JScrollPane scroll = new JScrollPane(panel);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        return scroll;
    }

    private void updateMissingState() {
        long count = competitionEditors.stream().filter(RowEditor::isMissing).count()
            + teamEditors.stream().filter(RowEditor::isMissing).count();
        missingCount.setText(count == 0 ? "Associazioni complete" : count + " associazioni mancanti");
        missingCount.setForeground(count == 0 ? new Color(20, 120, 55) : new Color(185, 45, 35));
        nextMissing.setEnabled(count > 0);
    }

    private void focusNextMissing() {
        List<RowEditor> current = tabs.getSelectedIndex() == 0 ? competitionEditors : teamEditors;
        RowEditor missing = current.stream().filter(RowEditor::isMissing).findFirst().orElse(null);
        if (missing == null && tabs.getSelectedIndex() == 0) {
            tabs.setSelectedIndex(1);
            missing = teamEditors.stream().filter(RowEditor::isMissing).findFirst().orElse(null);
        }
        if (missing != null) {
            missing.combo.requestFocusInWindow();
            missing.combo.scrollRectToVisible(missing.combo.getBounds());
        }
    }

    private void saveAndNext() {
        try {
            saveCurrent();
            if (seasonIndex == seasons.size() - 1) {
                saved = true;
                dispose();
            } else {
                loadSeason(seasonIndex + 1);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "RecordsNext", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void goPrevious() {
        try {
            saveCurrent();
            loadSeason(seasonIndex - 1);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "RecordsNext", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveCurrent() throws Exception {
        repository.save(seasonId, HistoricalMappingRepository.Kind.COMPETITION,
            competitionEditors.stream().map(RowEditor::decision).toList());
        repository.save(seasonId, HistoricalMappingRepository.Kind.TEAM,
            teamEditors.stream().map(RowEditor::decision).toList());
    }

    private final class RowEditor {
        final HistoricalMappingRepository.MappingRow row;
        final JLabel sourceLabel;
        final JComboBox<Object> combo = new JComboBox<>();

        RowEditor(HistoricalMappingRepository.MappingRow row, boolean anchorSeason) {
            this.row = row;
            this.sourceLabel = new JLabel(row.sourceName());
            combo.addItem("<Selezionare>");
            combo.addItem(EXCLUDE);
            if (!anchorSeason) combo.addItem(NEW_IDENTITY);
            for (var identity : row.candidates()) combo.addItem(identity);

            Long preferredIdentityId = row.identityId() != null
                ? row.identityId()
                : row.inheritedIdentityId();
            if (preferredIdentityId != null) {
                for (int i = 0; i < combo.getItemCount(); i++) {
                    Object item = combo.getItemAt(i);
                    if (item instanceof HistoricalMappingRepository.Identity id && id.id() == preferredIdentityId) {
                        combo.setSelectedIndex(i);
                        break;
                    }
                }
            } else if ("ESCLUSA".equals(row.status())) {
                combo.setSelectedItem(EXCLUDE);
            } else {
                for (int i = 0; i < combo.getItemCount(); i++) {
                    Object item = combo.getItemAt(i);
                    if (item instanceof HistoricalMappingRepository.Identity id
                        && normalize(id.name()).equals(normalize(row.sourceName()))) {
                        combo.setSelectedIndex(i);
                        break;
                    }
                }
            }

            combo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                               boolean selected, boolean focus) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focus);
                    if ("<Selezionare>".equals(value)) {
                        label.setForeground(selected ? Color.WHITE : new Color(185, 45, 35));
                        label.setFont(label.getFont().deriveFont(Font.BOLD));
                    } else if (EXCLUDE.equals(value) || NEW_IDENTITY.equals(value)) {
                        label.setFont(label.getFont().deriveFont(Font.BOLD));
                    }
                    return label;
                }
            });
            combo.addActionListener(e -> { updateVisualState(); updateMissingState(); });
            updateVisualState();
        }

        boolean isMissing() {
            return "<Selezionare>".equals(combo.getSelectedItem());
        }

        void updateVisualState() {
            boolean missing = isMissing();
            sourceLabel.setForeground(missing ? new Color(185, 45, 35) : UIManager.getColor("Label.foreground"));
            sourceLabel.setFont(sourceLabel.getFont().deriveFont(missing ? Font.BOLD : Font.PLAIN));
            combo.setBackground(missing ? new Color(255, 225, 220) : Color.WHITE);
            combo.setBorder(missing ? BorderFactory.createLineBorder(new Color(210, 60, 45), 2)
                                    : UIManager.getBorder("ComboBox.border"));
        }

        HistoricalMappingRepository.Decision decision() {
            Object selected = combo.getSelectedItem();
            if (selected instanceof HistoricalMappingRepository.Identity id) {
                return new HistoricalMappingRepository.Decision(row.seasonEntityIds(), row.sourceName(), id.id(), false, false);
            }
            return new HistoricalMappingRepository.Decision(row.seasonEntityIds(), row.sourceName(), null,
                NEW_IDENTITY.equals(selected), EXCLUDE.equals(selected));
        }

        private String normalize(String value) {
            return value == null ? "" : value.toLowerCase().replaceAll("[^a-z0-9]", "");
        }
    }
}
