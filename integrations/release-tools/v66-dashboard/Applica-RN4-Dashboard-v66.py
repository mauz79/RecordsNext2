#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Modifica SOLO dashboard Java nella RC v64, senza leggere/scrivere SQLite."""
from __future__ import annotations
import argparse
import hashlib
import json
import shutil
import sys
from pathlib import Path

REL = Path('src/main/java/it/alterlega/recordsnext/gui/RecordsNext2Dashboard.java')
DIALOG = Path('src/main/java/it/alterlega/recordsnext/gui/RecordsNextConfigurationDialog.java')
EXPECTED_FCA = 'e3c34b941aedb35951ba030701fe47072afa00941f7fa7884d0658442cfcf766'
FCA = Path(r'E:\FCM\data\ArchivioA2012SerieAmauz_G1_RECUPERATA_v1.fca')

SUBSTITUTIONS = [
    ('Titolo barra Windows',
     'new JFrame("RecordsNext by mauz79 · 4.0 (sviluppo)")',
     'new JFrame("RecordsNext by mauz79 · 4.0")'),
    ('Versione menu',
     'new JLabel("by mauz79 · 4.0 dev")',
     'new JLabel("by mauz79 · 4.0")'),
    ('Titolo dashboard',
     'new JLabel("RecordsNext 4.0 (sviluppo)", SwingConstants.CENTER)',
     'new JLabel("RecordsNext 4.0", SwingConstants.CENTER)'),
    ('Messaggio errore avvio',
     '"RecordsNext 4.0 (sviluppo)"',
     '"RecordsNext 4.0"', 2),
    ('Dimensioni cornice',
     '''        frame.setMinimumSize(new Dimension(900, 620));
        frame.setSize(1040, 700);''',
     '''        frame.setMinimumSize(new Dimension(940, 640));
        frame.setSize(1060, 720);'''),
    ('Dimensione menu',
     'side.setPreferredSize(new Dimension(180, 700));',
     'side.setPreferredSize(new Dimension(200, 700));'),
    ('Menu uniforme',
     '''        addNav(side, group, "dashboard", "Dashboard");
        addNav(side, group, "seasons", "Configurazione stagioni");
        addNav(side, group, "topflop", "Top & Flop");
        addNav(side, group, "project", "Progetto e motore");''',
     '''        addNav(side, group, "dashboard", "Dashboard");
        // Tutte le sezioni delle card hanno la stessa voce nel menu laterale.
        addNav(side, group, "seasons", "Configurazione stagioni");
        addNav(side, group, "families", "Famiglie record");
        addNav(side, group, "topflop", "Top & Flop");
        addNav(side, group, "culometro", "Soglie e Culometro");
        addNav(side, group, "project", "Progetto e motore");'''),
    ('Dimensioni card griglia',
     '''        cards.setPreferredSize(new Dimension(0, 352));
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 352));''',
     '''        // Altezza flessibile: evita il taglio di descrizioni e azioni.
        // Le card occupano l'area disponibile anche con font/DPI maggiorati.'''),
    ('Testo famiglie',
     'activeFamiliesSummary() + "\\n\\nSelezione granulare dei sottorecord",',
     'activeFamiliesSummary() + "\\nSelezione granulare dei sottorecord",'),
    ('Layout card espandibili',
     '''        JPanel cardsTop = new JPanel(new BorderLayout());
        cardsTop.setOpaque(false);
        cardsTop.add(cards, BorderLayout.NORTH);
        page.add(cardsTop, BorderLayout.CENTER);''',
     '''        // GridLayout distribuisce l'altezza disponibile equamente tra le quattro card.
        page.add(cards, BorderLayout.CENTER);'''),
    ('Dimensioni fisse card',
     '''        card.setPreferredSize(new Dimension(230, 185));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 185));''',
     '''        card.setMinimumSize(new Dimension(235, 190));'''),
]

def sha(path: Path) -> str:
    h=hashlib.sha256()
    with path.open('rb') as f:
        for b in iter(lambda:f.read(1048576),b''):
            h.update(b)
    return h.hexdigest()

def check(ok: bool, msg: str):
    if not ok: raise RuntimeError(msg)

def rewrite(s: str) -> str:
    for entry in SUBSTITUTIONS:
        name, old, new, *cnt = entry
        expected = cnt[0] if cnt else 1
        count=s.count(old)
        check(count==expected, f'STOP: pattern {name!r} atteso {expected}, trovato {count}; nessuna modifica applicata')
        s=s.replace(old,new)
    # Non perdere la correzione v65 per i clic, e non modificare il dialog.
    check('for (Component child : card.getComponents())' in s, 'STOP: manca correzione clic v65')
    check('addNav(side, group, "families", "Famiglie record")' in s, 'STOP: menu famiglie mancante')
    check('addNav(side, group, "culometro", "Soglie e Culometro")' in s, 'STOP: menu Culometro mancante')
    check('page.add(cards, BorderLayout.CENTER)' in s, 'STOP: layout card non flessibile')
    check('(sviluppo)' not in s and '4.0 dev' not in s, 'STOP: diciture di sviluppo residue')
    return s

def main():
    p=argparse.ArgumentParser()
    p.add_argument('--root',required=True)
    p.add_argument('--backup',required=True)
    p.add_argument('--mode',choices=['apply','verify'],required=True)
    p.add_argument('--check-fca',action='store_true')
    args=p.parse_args()
    root=Path(args.root)
    backup=Path(args.backup)
    check(root.name=='RecordsNext4.0_RC_v64', 'STOP: destinazione consentita solo RC v64')
    check(root.is_dir() and (root/'pom.xml').is_file(), 'STOP: directory RC non valida')
    check((root/'data/database/recordsnext.db').is_file(), 'STOP: SQLite RC mancante')
    source=root/REL
    dialog=root/DIALOG
    check(source.is_file() and dialog.is_file(), 'STOP: sorgenti GUI assenti')
    check('var row = loaded;' in dialog.read_text(encoding='utf-8-sig'), 'STOP: correzione v65 al caricamento stagioni non installata')
    if args.check_fca:
        check(FCA.is_file(), 'STOP: FCA corretto 2012/13 assente')
        check(sha(FCA)==EXPECTED_FCA, 'STOP: FCA corretto 2012/13 SHA256 non corrispondente')
    text=source.read_text(encoding='utf-8-sig')
    if args.mode=='apply':
        check(not backup.exists(), 'STOP: backup presente, non sovrascrivere')
        patched=rewrite(text) # preflight completo PRIMA del backup e di qualsiasi modifica
        backup_source=backup/REL
        backup_source.parent.mkdir(parents=True,exist_ok=False)
        shutil.copy2(source,backup_source)
        jar=root/'target/RecordsNext.jar'
        if jar.is_file():
            dest=backup/'target/RecordsNext.jar'
            dest.parent.mkdir(parents=True,exist_ok=True)
            shutil.copy2(jar,dest)
        (backup/'manifest-v66.json').write_text(json.dumps({
            'patch':'RN4_GUI_DASHBOARD_v66',
            'source_sha_before':sha(source),
            'fca2012_verified':args.check_fca,
            'files_modified':[str(REL)],
            'database_modified':False,
            'production_published':False
        },indent=2),encoding='utf-8')
        source.write_text(patched,encoding='utf-8',newline='')
        print('PATCH DASHBOARD v66: applicata a un solo sorgente Java')
        print('BACKUP:',backup)
        print('SHA256 aggiornato:',sha(source))
    else:
        # Riconosci esattamente i nuovi blocchi, non tentare una seconda applicazione.
        for name,old,new,*cnt in SUBSTITUTIONS:
            check(new in text, 'STOP: patch v66 assente in '+name)
            check(old not in text, 'STOP: residuo precedente in '+name)
        check('for (Component child : card.getComponents())' in text, 'STOP: clic v65 assente')
        print('VERIFICA DASHBOARD v66 OK: card flessibili, 4 sezioni nel menu, nessun titolo sviluppo')
        print('SHA256:',sha(source))

if __name__=='__main__':
    try:main()
    except Exception as exc:
        print('ERRORE v66:',str(exc),file=sys.stderr)
        sys.exit(2)
