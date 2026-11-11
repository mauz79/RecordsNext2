#!/usr/bin/env python3
"""RecordsNext 4.0 - audit di lettura dei codici giocatore negli FCA/FCM raw.

Non crea/modifica tabelle, non aggiorna configurazioni e non genera JS.
Usa esclusivamente Python 3 standard library.
"""

import argparse
import csv
import hashlib
import json
import re
import sqlite3
from datetime import datetime
from pathlib import Path


def quote_table(table):
    if not re.fullmatch(r"[A-Za-z0-9_]+", table):
        raise ValueError("Nome tabella non sicuro")
    return '"' + table + '"'


def scalar(conn, statement, values=()):
    return conn.execute(statement, values).fetchone()[0]


def file_sha256(path):
    hasher = hashlib.sha256()
    with path.open("rb") as handle:
        while True:
            chunk = handle.read(4 * 1024 * 1024)
            if not chunk:
                break
            hasher.update(chunk)
    return hasher.hexdigest()


def inspect_player(conn, known, season, code):
    prefix = "raw_" + season + "_"
    base = prefix + "fca_giocatorea"
    if base not in known:
        return [], False

    player_table = quote_table(base)
    player_rows = conn.execute(
        f"SELECT ID, NOME, CODICEGAZZA, RUOLO, DATADINASCITA, FOTO "
        f"FROM {player_table} WHERE CAST(CODICEGAZZA AS TEXT) = ? ORDER BY ID", (code,)
    ).fetchall()

    output = []
    for local_id, name, source_code, role, dob, photo in player_rows:
        row = {
            "stagione": season,
            "codice_storico_sorgente": str(source_code),
            "id_fca_stagionale": local_id,
            "nome_fca": name,
            "ruolo_fca": role,
            "data_nascita_fca": dob,
            "foto_fca": photo,
            "giornate_giocain": None,
            "giornate_con_id_punteggio": None,
            "righe_formazione_fcm": None,
            "righe_formazione_entrato": None,
            "stato": "OK",
        }
        gi = prefix + "fca_giocain"
        if gi in known:
            t = quote_table(gi)
            a, b = conn.execute(
                f"SELECT COUNT(*), SUM(CASE WHEN IDPUNTEGGIO IS NOT NULL "
                f"AND IDPUNTEGGIO <> 0 THEN 1 ELSE 0 END) "
                f"FROM {t} WHERE IDGIOCATORE = ?", (local_id,)
            ).fetchone()
            row["giornate_giocain"] = a
            row["giornate_con_id_punteggio"] = b or 0
        else:
            row["stato"] = "GIOCAIN_ASSENTE"

        f = prefix + "fcm_formazione"
        if f in known:
            t = quote_table(f)
            a, b = conn.execute(
                f"SELECT COUNT(*), SUM(CASE WHEN ENTRATO IS NOT NULL "
                f"AND ENTRATO <> 0 THEN 1 ELSE 0 END) "
                f"FROM {t} WHERE IDGIOC = ?", (local_id,)
            ).fetchone()
            row["righe_formazione_fcm"] = a
            row["righe_formazione_entrato"] = b or 0
        else:
            row["stato"] += ";FORMAZIONE_ASSENTE"
        output.append(row)

    if len(player_rows) > 1:
        for row in output:
            row["stato"] += ";CODICE_NON_UNIVOCO_NELLA_STAGIONE"
    return output, True


def main():
    ap = argparse.ArgumentParser(description="Audit giocatore per codice storico FCA (sola lettura)")
    ap.add_argument("--database", required=True, type=Path, help="Copia del recordsnext.db")
    ap.add_argument("--code", required=True, help="Codice storico (es. 100843)")
    ap.add_argument("--output-dir", required=True, type=Path, help="Cartella per i due report")
    args = ap.parse_args()

    if not re.fullmatch(r"[0-9]+", args.code):
        ap.error("--code deve essere numerico")
    db = args.database.resolve()
    if not db.is_file():
        ap.error("Database non trovato: " + str(db))
    if args.code == "0":
        ap.error("Il codice 0 non identifica univocamente un calciatore")

    con = sqlite3.connect(db.as_uri() + "?mode=ro", uri=True)
    try:
        con.execute("PRAGMA query_only=ON")
        con.execute("PRAGMA busy_timeout=10000")
        known = {name for (name,) in con.execute(
            "SELECT name FROM sqlite_master WHERE type='table'")}
        seasons = sorted({m.group(1) for name in known
                          if (m := re.fullmatch(r"raw_(\d{4}_\d{4})_fca_giocatorea", name))})
        rows = []
        for season in seasons:
            batch, _ = inspect_player(con, known, season, args.code)
            rows.extend(batch)
    finally:
        con.close()

    # L'identita all-time non viene approvata automaticamente: questo audit
    # mostra solo le anagrafiche legate al codice cercato.
    names = sorted({str(r["nome_fca"]).strip() for r in rows})
    birthdates = sorted({str(r["data_nascita_fca"]).strip() for r in rows
                         if r["data_nascita_fca"] is not None})
    status = "NON_TROVATO" if not rows else (
        "REVISIONE_MANUALE" if len(names) > 1 or len(birthdates) > 1
        or any("NON_UNIVOCO" in r["stato"] for r in rows)
        else "SENZA_ANOMALIE_RILEVATE")

    now = datetime.now().strftime("%Y%m%d_%H%M%S")
    args.output_dir.mkdir(parents=True, exist_ok=True)
    basename = "RecordsNext4_AuditGiocatore_v1_" + args.code + "_" + now
    path_csv = args.output_dir / (basename + ".csv")
    path_json = args.output_dir / (basename + ".json")
    fields = ["stagione", "codice_storico_sorgente", "id_fca_stagionale",
              "nome_fca", "ruolo_fca", "data_nascita_fca", "foto_fca",
              "giornate_giocain", "giornate_con_id_punteggio",
              "righe_formazione_fcm", "righe_formazione_entrato", "stato"]
    with path_csv.open("w", encoding="utf-8-sig", newline="") as out:
        writer = csv.DictWriter(out, fieldnames=fields, delimiter=";")
        writer.writeheader()
        writer.writerows(rows)
    result = {
        "audit_version": "1",
        "project_target": "RecordsNext 4.0.0",
        "data_only_no_modifications": True,
        "database": str(db),
        "database_size": db.stat().st_size,
        "database_sha256": file_sha256(db),
        "code": args.code,
        "status": status,
        "seasons_with_fca_player_table": len(seasons),
        "seasons_with_matching_player": len({r["stagione"] for r in rows}),
        "matching_rows": len(rows),
        "names_found": names,
        "birthdates_found": birthdates,
        "note": "ID FCA locale; le righe GiocaIn non equivalgono necessariamente a presenze reali. "
                "Le righe Formazione non equivalgono necessariamente a utilizzi effettivi. "
                "Nessun record all-time e generato da questo audit.",
        "rows": rows,
    }
    path_json.write_text(json.dumps(result, ensure_ascii=False, indent=2, default=str), encoding="utf-8")
    print("Audit:", status)
    print("Stagioni FCA:", len(seasons), "| stagioni con codice:", result["seasons_with_matching_player"])
    print("Righe corrispondenti:", len(rows))
    print("CSV:", path_csv)
    print("JSON:", path_json)


if __name__ == "__main__":
    main()
