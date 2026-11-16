#!/usr/bin/env python3
"""Generate v42 Bible by appending verified post-v55 evidence to v41.

Preserves the source v41 and captures sha256 for each embedded file.
Designed to be called after the audit and versioned documentation are in place.
"""
from pathlib import Path
import argparse
import hashlib
import os


def sha(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def main():
    p = argparse.ArgumentParser()
    p.add_argument('--root', required=True)
    p.add_argument('--report-dir', required=True)
    a = p.parse_args()
    root = Path(a.root).resolve()
    reports = Path(a.report_dir).resolve()
    old = root / 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v41.md'
    new = root / 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v42.md'
    if new.exists():
        raise SystemExit('Bibbia v42 gia esistente: non sovrascrivo.')
    if not old.is_file():
        raise SystemExit('Bibbia v41 mancante.')
    required = [
        'docs/VERIFICA_FCA_2012_2013_PRIMA_GIORNATA_v56.md',
        'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v42.md',
        'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v35.md',
        'tools/Create-RecordsNext4WorkingCodeMd-v42.py',
        'CHANGELOG.md',
    ]
    optional = [
        'docs/CRITERI_CORRETTI_CALCOLI_v55.md',
        'docs/INDICE_COMPLETO_RECORD_E_RENEWO_v55.md',
        'tools/Audit-Fonti-FCA-v55.py',
        'tools/Audit-Fantamedia-FCA-v55.py',
        'tools/Audit-JS-Careers-v55.py',
    ]
    report_files = [
        'audit-fonti-fca-v55.json',
        'audit-fantamedia-fca-v55.json',
        'audit-js-careers-v55.json',
    ]
    files = [(relative, root / relative) for relative in required]
    files += [(relative, root / relative) for relative in optional if (root / relative).is_file()]
    files += [(str(reports.relative_to(root)).replace('\\', '/') + '/' + name, reports / name) for name in report_files]
    missing = [label for label, path in files if not path.is_file()]
    if missing:
        raise SystemExit('File mancanti: ' + ', '.join(missing))

    # Do not replace the predecessor; append an evidence-based and versioned section.
    parts = [old.read_text(encoding='utf-8').rstrip(),
             '\n\n# AGGIORNAMENTO POST-v55 / BIBBIA v42 — FCA 2012/2013\n\n',
             'Nessuna modifica Java, FCM, FCA sorgente o sito operativo in questa documentazione. '
             'I report sono post-reimport; le evidenze v55 pre-correzione rimangono storiche.\n\n',
             'Bibbia v41 SHA256: `' + sha(old.read_bytes()) + '`\n\n']
    for label, path in files:
        raw = path.read_bytes()
        try:
            content = raw.decode('utf-8-sig').rstrip()
        except UnicodeDecodeError as e:
            raise SystemExit('File non UTF-8: ' + str(path)) from e
        lang = 'python' if path.suffix == '.py' else 'json' if path.suffix == '.json' else 'markdown'
        parts.append(f'\n## `{label}`\n\nSHA256: `{sha(raw)}`\n\n~~~~~{lang}\n{content}\n~~~~~\n')
    text = ''.join(parts)
    tmp = new.with_name(new.name + '.part')
    try:
        with tmp.open('x', encoding='utf-8', newline='\n') as out:
            out.write(text)
        os.replace(tmp, new)
    except BaseException:
        tmp.unlink(missing_ok=True)
        raise
    print('BIBBIA v42:', new)
    print('BIBBIA SHA256:', sha(new.read_bytes()))
    print('BIBBIA bytes:', new.stat().st_size)


if __name__ == '__main__':
    main()
