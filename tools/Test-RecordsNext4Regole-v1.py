#!/usr/bin/env python3
"""Synthetic read-only and real-database regression tests for Phase 3B2."""
from __future__ import annotations
import argparse
import importlib.util
from pathlib import Path
import sqlite3
import tempfile
import unittest

MODULE_PATH=Path(__file__).with_name('Audit-RecordsNext4Regole-v1.py')
spec=importlib.util.spec_from_file_location('rn4_regole',MODULE_PATH)
assert spec and spec.loader
mod=importlib.util.module_from_spec(spec)
spec.loader.exec_module(mod)
DB=None
SCRATCH=None


def inspect(path:Path):
    c=sqlite3.connect(path.resolve().as_uri()+'?mode=ro',uri=True)
    c.row_factory=sqlite3.Row
    c.execute('PRAGMA query_only=ON')
    try:return mod.analyze(c,max_samples=10)
    finally:c.close()


class SyntheticTests(unittest.TestCase):
    def test_minimal_fixture_and_no_mutation(self):
        with tempfile.TemporaryDirectory(prefix='rn4-regole-',dir=SCRATCH) as d:
            db=Path(d)/'test.db'
            c=sqlite3.connect(db)
            cols=', '.join('"'+k+'" INTEGER' for k in mod.SELECTED)
            c.execute('CREATE TABLE raw_2006_2007_fcm_competizione (ID INTEGER,NOME TEXT,'+cols+')')
            keys=['ID','NOME',*mod.SELECTED]
            values=[4,'Campionato',*[0]*len(mod.SELECTED)]
            values[keys.index('NUMEROSOSTITUZIONI')]=3
            values[keys.index('NUMERORISUFFICIO')]=1
            c.execute('INSERT INTO raw_2006_2007_fcm_competizione VALUES('+','.join('?'*len(keys))+')',values)
            c.execute('CREATE TABLE raw_2006_2007_fcm_girone (ID INTEGER,IDCOMPETIZIONE INTEGER)')
            c.execute('INSERT INTO raw_2006_2007_fcm_girone VALUES (9,4)')
            c.execute('CREATE TABLE raw_2006_2007_fcm_incontro (ID INTEGER,IDGIRONE INTEGER,IDTIPO INTEGER,IDCASA INTEGER,IDFUORI INTEGER,GIOCATO INTEGER)')
            c.execute('INSERT INTO raw_2006_2007_fcm_incontro VALUES (100,9,2,1,0,1)')
            c.execute('CREATE TABLE raw_2006_2007_fcm_tabellino (IDINCONTRO INTEGER,IDSQUADRA INTEGER,TOTALESQUADRAESISTE INTEGER,PARZIALESQUADRAESISTE INTEGER,LISTA TEXT,RUOLO TEXT,TOT TEXT)')
            c.execute("INSERT INTO raw_2006_2007_fcm_tabellino VALUES (100,1,1,1,'1','1','6')")
            c.execute("INSERT INTO raw_2006_2007_fcm_tabellino VALUES (999,3,0,0,'1','1','sv/ng')")
            c.execute('CREATE TABLE raw_2006_2007_fcm_modulo (ID INTEGER,NUMDIFE INTEGER,NUMCENT INTEGER,NUMATTA INTEGER)')
            c.execute('INSERT INTO raw_2006_2007_fcm_modulo VALUES (5,3,4,3)')
            c.execute('CREATE TABLE raw_2006_2007_fcm_moduloammesso (IDCOMPETIZIONE INTEGER,IDMODULO INTEGER,MODIF REAL,MODIFAVV REAL)')
            c.execute('INSERT INTO raw_2006_2007_fcm_moduloammesso VALUES (4,5,0,0)')
            c.commit();c.close()
            old=mod.sha_path(db)
            r=inspect(db)
            self.assertEqual(r['totali']['tabellini'],2)
            self.assertEqual(r['totali']['tabellini_incontro_assente'],1)
            self.assertEqual(r['totali']['tabellini_incontro_squadra_zero'],1)
            self.assertEqual(r['competizioni'][0]['NUMEROSOSTITUZIONI'],3)
            self.assertEqual(r['competizioni'][0]['moduliAmmessi'],1)
            self.assertEqual(old,mod.sha_path(db))

    def test_missing_reference_not_accepted(self):
        with sqlite3.connect(':memory:') as conn:
            self.assertRaises(ValueError,mod.analyze,conn)

    def test_selected_fields_no_duplicates(self):
        self.assertEqual(len(mod.SELECTED),len(set(mod.SELECTED)))
        self.assertIn('ASOSTITUISCEC',mod.SELECTED)


class RealDatabaseTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.r=inspect(DB)

    def test_global_counts(self):
        t=self.r['totali']
        self.assertEqual(t['stagioni'],21)
        self.assertEqual(t['competizioni'],180)
        self.assertEqual(t['tabellini'],22265)
        self.assertEqual(t['tabellini_incontro_assente'],168)
        self.assertEqual(t['tabellini_incontro_squadra_zero'],23)
        self.assertEqual(t['tabellini_con_incontro']+t['tabellini_incontro_assente'],t['tabellini'])

    def test_2007_rules_preserved_without_interpretation(self):
        matches=[r for r in self.r['competizioni'] if r['stagione']=='2007_2008']
        self.assertEqual(sorted(x['NUMEROSOSTITUZIONI'] for x in matches),[3,8,11])
        self.assertEqual(sorted(x['NUMERORISUFFICIO'] for x in matches),[0,99,99])

    def test_2019_five_substitutions(self):
        matches=[r for r in self.r['competizioni'] if r['stagione']=='2019_2020']
        self.assertEqual(len(matches),12)
        self.assertTrue(all(x['NUMEROSOSTITUZIONI']==5 and x['NUMERORISUFFICIO']==3 for x in matches))

    def test_2025_competition_specific(self):
        matches=[r for r in self.r['competizioni'] if r['stagione']=='2025_2026']
        self.assertEqual(len(matches),12)
        self.assertEqual([x['NUMERORISUFFICIO'] for x in matches if x['competizione']=='Play Off - Play Out'],[0])
        self.assertTrue(all(x['NUMEROSOSTITUZIONI']==3 for x in matches))

    def test_2026_no_tabellini(self):
        row=next(x for x in self.r['stagioni'] if x['stagione']=='2026_2027')
        self.assertEqual(row['tabellini'],0)


if __name__=='__main__':
    p=argparse.ArgumentParser()
    p.add_argument('--database',type=Path,required=True)
    p.add_argument('--scratch-dir',type=Path,required=True)
    args,remaining=p.parse_known_args()
    if not args.database.is_file():p.error('Database non trovato: '+str(args.database))
    if not args.scratch_dir.is_dir():p.error('Cartella test non trovata: '+str(args.scratch_dir))
    DB=args.database
    SCRATCH=args.scratch_dir
    unittest.main(argv=[__file__,*remaining],verbosity=2)
