#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Patch estremamente mirata alla GUI RC v64. Nessun database aperto o modificato."""
from __future__ import annotations
import argparse
import hashlib
import json
import shutil
import sys
from pathlib import Path

DASH = Path('src/main/java/it/alterlega/recordsnext/gui/RecordsNext2Dashboard.java')
DIALOG = Path('src/main/java/it/alterlega/recordsnext/gui/RecordsNextConfigurationDialog.java')
FCA_EXPECTED = 'e3c34b941aedb35951ba030701fe47072afa00941f7fa7884d0658442cfcf766'
FCA_PATH = Path(r'E:\FCM\data\ArchivioA2012SerieAmauz_G1_RECUPERATA_v1.fca')

OLD_CARD = '''        card.addMouseListener(click);
        t.addMouseListener(click);
        body.addMouseListener(click);
        v.addMouseListener(click);'''
NEW_CARD = '''        // L'intera card deve rispondere al primo clic, anche su spazi e distanziatori.
        card.addMouseListener(click);
        for (Component child : card.getComponents()) {
            child.addMouseListener(click);
        }'''
OLD_FCM = '''            for(var loaded:repository.load()) {
                var row = refreshManagedMetadata(loaded);
                boolean includeByDefault = !selectionInitialized || selected.contains(row.seasonId());'''
NEW_FCM = '''            for(var loaded:repository.load()) {
                // Usa i metadati gia' salvati: riaprire ogni FCM tramite UCanAccess
                // blocca la GUI e genera centinaia di warning all'apertura.
                var row = loaded;
                boolean includeByDefault = !selectionInitialized || selected.contains(row.seasonId());'''

def sha(path: Path) -> str:
    h = hashlib.sha256()
    with path.open('rb') as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b''):
            h.update(block)
    return h.hexdigest()

def ensure(condition: bool, msg: str):
    if not condition:
        raise RuntimeError(msg)

def patch_once(text: str, needle: str, replacement: str, name: str) -> str:
    ensure(text.count(needle) == 1, f'STOP: sorgente inatteso ({name}), pattern trovato {text.count(needle)} volte; nessuna modifica')
    return text.replace(needle, replacement, 1)

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--root', required=True)
    parser.add_argument('--backup', required=True)
    parser.add_argument('--mode', choices=('apply','verify'), required=True)
    parser.add_argument('--check-fca', action='store_true')
    args = parser.parse_args()
    root = Path(args.root).resolve()
    backup = Path(args.backup).resolve()
    ensure(root.name == 'RecordsNext4.0_RC_v64', 'STOP: consentita solo la RC v64, non TEST/3.2/sito')
    ensure(root.is_dir() and (root/'pom.xml').is_file(), 'STOP: RC non valida')
    ensure((root/'data/database/recordsnext.db').is_file(), 'STOP: SQLite RC assente')
    paths = [root / DASH, root / DIALOG]
    for p in paths:
        ensure(p.is_file(), 'STOP: sorgente GUI mancante: ' + str(p))
    if args.check_fca:
        ensure(FCA_PATH.is_file(), 'STOP: FCA corretto non trovato: ' + str(FCA_PATH))
        ensure(sha(FCA_PATH) == FCA_EXPECTED, 'STOP: SHA256 FCA 2012/13 differente; non proseguire')
    if args.mode == 'apply':
        ensure(not backup.exists(), 'STOP: backup gia esistente: ' + str(backup))
        orig = [p.read_text(encoding='utf-8-sig') for p in paths]
        updated = [patch_once(orig[0], OLD_CARD, NEW_CARD, 'actionCard'),
                   patch_once(orig[1], OLD_FCM, NEW_FCM, 'loadSeasons')]
        backup.mkdir(parents=True)
        for p in paths:
            dest = backup / p.relative_to(root)
            dest.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(p, dest)
        jar = root/'target/RecordsNext.jar'
        if jar.is_file():
            dest = backup/'target/RecordsNext.jar'
            dest.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(jar, dest)
        manifest = {
            'patch':'RN4_GUI_CONFIG_v65', 'root':str(root), 'mode':'apply',
            'fca_verified':args.check_fca, 'files':[]
        }
        for p, original, new in zip(paths, orig, updated):
            if original != new:
                # Riutilizza l'encoding UTF-8 originario; Java 21 accetta entrambi gli EOL.
                p.write_text(new, encoding='utf-8', newline='')
                manifest['files'].append({'path':str(p.relative_to(root)), 'sha256_after':sha(p)})
        (backup/'manifest-v65.json').write_text(json.dumps(manifest,indent=2,ensure_ascii=False),encoding='utf-8')
        print('PATCH GUI v65 APPLICATA: card cliccabile su tutta la superficie, metadati FCM da SQLite')
        print('BACKUP:', backup)
    else:
        d = paths[0].read_text(encoding='utf-8-sig')
        c = paths[1].read_text(encoding='utf-8-sig')
        ensure(NEW_CARD in d and OLD_CARD not in d, 'STOP: patch card non presente')
        ensure(NEW_FCM in c and OLD_FCM not in c, 'STOP: patch configurazione non presente')
        print('VERIFICA SORGENTI v65 OK')
        print('GUI CARD:',sha(paths[0]))
        print('GUI DIALOG:',sha(paths[1]))

if __name__ == '__main__':
    try:
        main()
    except Exception as ex:
        print('ERRORE PATCH GUI v65:', ex, file=sys.stderr)
        sys.exit(2)
