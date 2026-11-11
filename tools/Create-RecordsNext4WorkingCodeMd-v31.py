#!/usr/bin/env python3
"""Generate/check immutable continuity bible v31; never touch existing v30."""
import argparse,hashlib
from pathlib import Path
PREV='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v29.md'
OUT='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v31.md'
FILES=[
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopPlayerIdentities.java',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopIdentityEvidence.java',
 'src/main/java/it/alterlega/recordsnext/gui/TopFlopIdentityReviewDialog.java',
 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopIdentityEvidenceSmokeTest.java',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v31.md',
]
EVIDENCE=[
 'config/identity-evidence/LCS_GiocatoriAccoppiamenti.csv',
 'config/identity-evidence/LCS_GiocatoriAccoppiamenti_2020.csv',
 'config/identity-evidence/LCS_GiocatoriAccoppiamenti_2019.csv',
 'config/identity-evidence/LCS_GiocatoriAccoppiamenti_2015_old.csv',
 'config/identity-evidence/LCS_ControlloGiocatori.csv',
]
def sha(path): return hashlib.sha256(path.read_bytes()).hexdigest()
def render(root):
    previous=root/PREV
    if not previous.is_file():raise RuntimeError('Bibbia v29 non trovata')
    lines=['# RecordsNext 4.0 - CODICE FUNZIONANTE v31','',
      'Appendice sorgenti alla v29: LCS storico per riconciliazione esplicita.','',
      'La v30 preesistente, se presente, NON viene sovrascritta.','',
      f'Bibbia precedente: `{PREV}` SHA256 `{sha(previous)}`','']
    for rel in FILES:
        p=root/rel
        if not p.is_file():raise RuntimeError('Manca '+rel)
        lang={'.java':'java','.md':'markdown'}.get(p.suffix,'text')
        lines.extend([f'## `{rel}`','',f'SHA256: `{sha(p)}`','','~~~~'+lang,
          p.read_text(encoding='utf-8-sig').rstrip(),'~~~~',''])
    lines.extend(['## Evidenze CSV esterne (mai approvazioni automatiche)',''])
    for rel in EVIDENCE:
        p=root/rel
        if not p.is_file():raise RuntimeError('Manca '+rel)
        lines.append(f'- `{rel}`: {p.stat().st_size} byte, SHA256 `{sha(p)}`')
    return '\n'.join(lines).rstrip()+'\n'
def main():
    ap=argparse.ArgumentParser();ap.add_argument('--project-dir',required=True)
    args=ap.parse_args();root=Path(args.project_dir).resolve()
    content=render(root);target=root/OUT
    if target.exists() and target.read_text(encoding='utf-8')!=content:
        raise RuntimeError('Bibbia v31 divergente: nessuna sovrascrittura')
    if not target.exists():target.write_text(content,encoding='utf-8')
    print(f'BIBBIA v31 VERIFICATA: {target} SHA256 {sha(target)}')
if __name__=='__main__':main()
