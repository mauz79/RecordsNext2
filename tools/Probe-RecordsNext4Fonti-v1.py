#!/usr/bin/env python3
"""RecordsNext4 fase 0: inventario/raw e integrita referenziale. SOLO LETTURA.

Non interpreta voti, non deduce identita all time, non modifica database e non
pubblica output; richiede Python 3 standard library.
"""
import argparse
import csv
import json
import re
import sqlite3
from datetime import datetime
from pathlib import Path

FAMILIES = (
    "fca_giocatorea", "fca_giocain", "fca_punteggio", "fca_statistiche",
    "fca_storico", "fca_squadradia", "fca_incontroina",
    "fcm_formazione", "fcm_tesserato", "fcm_tabellino",
    "fcm_incontro", "fcm_fantasquadra",
)
LINKS = (
    ("fcm_formazione", "IDGIOC", "fca_giocatorea", "ID", "formazione_giocatore"),
    ("fcm_tesserato", "IDGIOCATORE", "fca_giocatorea", "ID", "tesserato_giocatore"),
    ("fca_giocain", "IDGIOCATORE", "fca_giocatorea", "ID", "giocain_giocatore"),
    ("fcm_formazione", "IDINCONTRO", "fcm_incontro", "ID", "formazione_incontro"),
    ("fcm_tabellino", "IDINCONTRO", "fcm_incontro", "ID", "tabellino_incontro"),
)


def quoted(name):
    if not re.fullmatch(r"[A-Za-z0-9_]+", name):
        raise ValueError("Nome tabella imprevisto: " + name)
    return '"' + name + '"'


def norm(value):
    if value is None:
        return ""
    s = str(value).strip()
    if re.fullmatch(r"[+-]?\d+\.0+", s):
        s = s.split(".")[0]
    return s


def unique_values(conn, table, field):
    return {norm(v) for (v,) in conn.execute(
        "SELECT " + quoted(field) + " FROM " + quoted(table)
    ) if v is not None}


def write_csv(path, records, columns):
    with path.open("w", encoding="utf-8-sig", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=columns, delimiter=";")
        writer.writeheader()
        writer.writerows(records)


def main():
    ap = argparse.ArgumentParser(description="Inventario raw e relazioni FCM/FCA, sola lettura")
    ap.add_argument("--database", type=Path, required=True)
    ap.add_argument("--output-dir", type=Path, required=True)
    args = ap.parse_args()
    db = args.database.resolve()
    if not db.is_file():
        ap.error("Database non trovato: " + str(db))
    conn = sqlite3.connect(db.as_uri() + "?mode=ro", uri=True)
    conn.execute("PRAGMA query_only=ON")
    try:
        names = {name for (name,) in conn.execute(
            "SELECT name FROM sqlite_master WHERE type='table'"
        )}
        seasons = sorted({m.group(1) for name in names
                          if (m := re.fullmatch(r"raw_(\d{4}_\d{4})_fca_giocatorea", name))})
        # Non assumere che ogni stagione contenga necessariamente FCA.
        seasons = sorted(set(seasons) | {m.group(1) for name in names
                         if (m := re.fullmatch(r"raw_(\d{4}_\d{4})_fcm_formazione", name))})
        coverage, columns, rel, anomalous_codes = [], [], [], []
        for season in seasons:
            info = {}
            for family in FAMILIES:
                table = "raw_" + season + "_" + family
                if table not in names:
                    coverage.append({"stagione": season, "famiglia": family,
                                     "tabella": table, "presente": 0, "righe": ""})
                    continue
                count = conn.execute("SELECT COUNT(*) FROM " + quoted(table)).fetchone()[0]
                coverage.append({"stagione": season, "famiglia": family,
                                 "tabella": table, "presente": 1, "righe": count})
                cols = [row[1] for row in conn.execute("PRAGMA table_info(" + quoted(table) + ")")]
                info[family] = (table, set(cols), count)
                for col in cols:
                    columns.append({"stagione": season, "famiglia": family, "campo": col})
            for src, srcfield, dst, dstfield, label in LINKS:
                if src not in info or dst not in info:
                    rel.append({"stagione": season, "relazione": label, "stato": "TABELLA_ASSENTE",
                                "righe_origine": "", "riferimenti_mancanti": "", "distinti_mancanti": ""})
                    continue
                source, sourcecols, cnt = info[src]
                target, targetcols, _ = info[dst]
                if srcfield not in sourcecols or dstfield not in targetcols:
                    rel.append({"stagione": season, "relazione": label, "stato": "COLONNA_ASSENTE",
                                "righe_origine": cnt, "riferimenti_mancanti": "", "distinti_mancanti": ""})
                    continue
                # Conteggi esatti, lettura dei soli identificativi, senza creare indici o tabelle.
                valid = unique_values(conn, target, dstfield)
                missing = 0
                distinct = set()
                for (raw,) in conn.execute("SELECT " + quoted(srcfield) + " FROM " + quoted(source)):
                    key = norm(raw)
                    if key not in valid:
                        missing += 1
                        if len(distinct) < 10000:
                            distinct.add(key)
                rel.append({"stagione": season, "relazione": label, "stato": "OK" if missing == 0 else "DA_VERIFICARE",
                            "righe_origine": cnt, "riferimenti_mancanti": missing,
                            "distinti_mancanti": len(distinct) if len(distinct) < 10000 else ">=10000"})
            if "fca_giocatorea" in info:
                t, c, _ = info["fca_giocatorea"]
                if {"CODICEGAZZA", "NOME", "ID"}.issubset(c):
                    for code, n in conn.execute(
                        "SELECT CODICEGAZZA, COUNT(*) FROM " + quoted(t) +
                        " WHERE CODICEGAZZA IS NOT NULL AND CAST(CODICEGAZZA AS TEXT) <> '0' "
                        " GROUP BY CODICEGAZZA HAVING COUNT(*) > 1"
                    ):
                        anomalous_codes.append({"stagione": season, "codice": norm(code),
                                                "righe_stessa_stagione": n})
    finally:
        conn.close()
    folder = args.output_dir
    folder.mkdir(parents=True, exist_ok=True)
    stamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    prefix = "RecordsNext4_Fonti_v1_" + stamp
    write_csv(folder / (prefix + "_copertura.csv"), coverage,
              ("stagione", "famiglia", "tabella", "presente", "righe"))
    write_csv(folder / (prefix + "_colonne.csv"), columns,
              ("stagione", "famiglia", "campo"))
    write_csv(folder / (prefix + "_relazioni.csv"), rel,
              ("stagione", "relazione", "stato", "righe_origine", "riferimenti_mancanti", "distinti_mancanti"))
    write_csv(folder / (prefix + "_codici_duplicati_nella_stagione.csv"), anomalous_codes,
              ("stagione", "codice", "righe_stessa_stagione"))
    summary = {
        "script": "Probe-RecordsNext4Fonti-v1.py",
        "database": str(db),
        "read_only": True,
        "stagioni_raw": len(seasons),
        "famiglie_censite": len(FAMILIES),
        "relazioni_controllate": len(rel),
        "relazioni_da_verificare": [r for r in rel if r["stato"] not in ("OK",)],
        "duplicazioni_codici_nella_stessa_stagione": len(anomalous_codes),
        "limitazioni": [
            "Riferimenti mancanti non sono automaticamente errori: analizzare casi speciali.",
            "Non testa la semantica di Entrato/EntratoRiserva, SV, voti e tabellini.",
            "Non classifica identita storiche, non aggrega gol o assist.",
            "Non rileva codice storico nei campi FCM: verifica nelle strutture sorgente separata.",
        ],
    }
    (folder / (prefix + "_sintesi.json")).write_text(
        json.dumps(summary, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    print("Stagioni:", len(seasons))
    print("Famiglie:", len(FAMILIES))
    print("Relazioni da verificare:", len(summary["relazioni_da_verificare"]))
    print("Codici duplicati nella stessa stagione:", len(anomalous_codes))
    print("File generati nella cartella:", folder)


if __name__ == "__main__":
    main()
