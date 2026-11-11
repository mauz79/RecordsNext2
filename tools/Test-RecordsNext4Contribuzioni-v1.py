#!/usr/bin/env python3
"""Unit and actual-archive regression tests, read-only."""
from __future__ import annotations
import argparse
import importlib.util
from pathlib import Path
import sqlite3
import sys
import unittest

HERE=Path(__file__).resolve().parent
spec=importlib.util.spec_from_file_location('rn4_audit',HERE/'Audit-RecordsNext4Contribuzioni-v1.py')
audit=importlib.util.module_from_spec(spec)
spec.loader.exec_module(audit)


def setup(c,year='2025_2026'):
    for table,ddl in {
      'fcm_formazione':'IDINCONTRO INT, IDSQUADRA INT, IDGIOC INT, POS INT, ENTRATO INT, ENTRATORISERVA INT, RIG INT',
      'fcm_tabellino':'IDINCONTRO INT, IDSQUADRA INT, LISTA TEXT, RUOLO TEXT, TOT TEXT, TOTALESQUADRAESISTE INT',
      'fcm_incontro':'ID INT, GIOCATO INT, IDTIPO INT, IDCASA INT, IDFUORI INT',
      'fca_giocatorea':'ID INT, NOME TEXT, CODICEGAZZA INT',
    }.items():
        c.execute('CREATE TABLE raw_'+year+'_'+table+' ('+ddl+')')
    c.row_factory=sqlite3.Row


class UnitTests(unittest.TestCase):
    def test_season_validation(self):
        self.assertEqual(audit.tbl('2025_2026','fcm_formazione'),'"raw_2025_2026_fcm_formazione"')
        with self.assertRaises(ValueError):audit.tbl('2025_2026";drop','fcm_formazione')
        with self.assertRaises(ValueError):audit.tbl('2025_2026','fcm_password')

    def test_special_role_and_position(self):
        self.assertEqual(audit.position(0),'TITOLARE_INIZIALE')
        self.assertEqual(audit.position(3),'PANCHINA')
        self.assertEqual(audit.position(-1),'FUORI_FORMAZIONE')
        self.assertEqual(audit.role_group('-5'),'NEGATIVO')
        self.assertEqual(audit.split('1%2%3'),['1','2','3'])

    def test_first11_and_flag_without_inference(self):
        c=sqlite3.connect(':memory:'); setup(c)
        c.execute('INSERT INTO raw_2025_2026_fcm_incontro VALUES (1,1,1,10,20)')
        c.executemany('INSERT INTO raw_2025_2026_fca_giocatorea VALUES (?,?,?)',
                      [(1,'Uno',100843),(2,'Due',223),(3,'Tre',334)])
        c.executemany('INSERT INTO raw_2025_2026_fcm_formazione VALUES (?,?,?,?,?,?,?)',
                      [(1,10,1,0,0,0,0),(1,10,2,1,1,0,0),(1,10,3,2,0,1,0)])
        c.execute('INSERT INTO raw_2025_2026_fcm_tabellino VALUES (?,?,?,?,?,?)',
                  (1,10,'%'.join(['1','2']+['0']*9+['3']),'%'.join(['2','2']+['5']*9+['-6']),'%'.join(['7','8']+['3']*9+['6']),1))
        s=audit.analyze(c,'2025_2026')
        n=s['contatori']
        self.assertEqual(n['RIGHE_FORMAZIONE'],3)
        self.assertEqual(n['FORMAZIONI_CON_SLOT'],3)
        self.assertEqual(n['PRIMI11_ENTRATO_ZERO'],1)
        self.assertEqual(n['PANCHINARI_NEI_PRIMI11'],1)
        self.assertEqual(n['SLOT_SPECIALE_PRIMI11'],9)
        self.assertEqual(n['FLAG_RISERVA_FUORI_PRIMI11'],1)
        self.assertEqual(n.get('DOPO_PRIMI11_ENTRATO_UNO',0),0)
        self.assertEqual(n.get('FORMAZIONE_SENZA_SLOT',0),0)
        c.close()

    def test_tabellino_without_incontro_kept_separate(self):
        c=sqlite3.connect(':memory:');setup(c)
        c.execute('INSERT INTO raw_2025_2026_fcm_tabellino VALUES (?,?,?,?,?,?)',(9,10,'0','1','3',1))
        counts=audit.analyze(c,'2025_2026')['contatori']
        self.assertEqual(counts['TABELLO_ID_INCONTRO_ASSENTE'],1)
        self.assertEqual(counts['TABELLO_SENZA_FORMAZIONE'],1)
        self.assertEqual(counts.get('TABELLO_INCONTRO_SQUADRA_ZERO',0),0)
        c.close()


class ActualArchive(unittest.TestCase):
    db=None
    @classmethod
    def setUpClass(cls):
        if cls.db is None:raise unittest.SkipTest('No database provided')
        cls.c=sqlite3.connect(cls.db.as_uri()+'?mode=ro',uri=True)
        cls.c.row_factory=sqlite3.Row
        cls.c.execute('PRAGMA query_only=ON')
    @classmethod
    def tearDownClass(cls):
        if hasattr(cls,'c'):cls.c.close()

    def test_2025(self):
        n=audit.analyze(self.c,'2025_2026')['contatori']
        self.assertEqual(n['RIGHE_FORMAZIONE'],39850)
        self.assertEqual(n['FORMAZIONI_CON_SLOT'],39850)
        self.assertEqual(n['PRIMI11_ENTRATO_ZERO'],11)

    def test_2012_all_initial_flags_zero_in_a_scored_match(self):
        n=audit.analyze(self.c,'2012_2013')['contatori']
        self.assertEqual(n['PRIMI11_ENTRATO_ZERO'],11)
        r=self.c.execute('SELECT GIOCATO FROM raw_2012_2013_fcm_incontro WHERE ID=3256').fetchone()
        t=self.c.execute('SELECT TOTALESQUADRAESISTE FROM raw_2012_2013_fcm_tabellino WHERE IDINCONTRO=3256 AND IDSQUADRA=3').fetchone()
        self.assertEqual(r['GIOCATO'],1)
        self.assertEqual(t['TOTALESQUADRAESISTE'],1)

    def test_all_history(self):
        years=audit.seasons(self.c)
        self.assertEqual(len(years),21)
        total=0
        tab=0
        orphan=0
        formation_orphan=0
        reserve_unused=0
        for year in years:
            n=audit.analyze(self.c,year)['contatori']
            total+=n['RIGHE_FORMAZIONE']
            tab+=n.get('TABELLO_RIGHE',0)
            orphan+=n.get('TABELLO_ID_INCONTRO_ASSENTE',0)
            formation_orphan+=n.get('TABELLO_SENZA_FORMAZIONE',0)
            reserve_unused+=n.get('FLAG_RISERVA_FUORI_PRIMI11',0)
        self.assertEqual(total,552025)
        self.assertEqual(tab,22265)
        self.assertEqual(orphan,168)
        self.assertEqual(formation_orphan,178)
        self.assertEqual(reserve_unused,43)


def main()->int:
    ap=argparse.ArgumentParser()
    ap.add_argument('--database',type=Path)
    args=ap.parse_args()
    if args.database:ActualArchive.db=args.database.resolve(strict=True)
    suite=unittest.defaultTestLoader.loadTestsFromModule(sys.modules[__name__])
    result=unittest.TextTestRunner(verbosity=2).run(suite)
    return 0 if result.wasSuccessful() else 1


if __name__=='__main__':sys.exit(main())
