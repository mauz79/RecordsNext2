#!/usr/bin/env python3
"""Tests for read-only observational flags audit."""
from __future__ import annotations
import argparse
from pathlib import Path
import sqlite3
import sys
import unittest
import importlib.util

SCRIPT=Path(__file__).with_name('Audit-RecordsNext4Utilizzo-v1.py')
SPEC=importlib.util.spec_from_file_location('rn4_audit_utilizzo_v1',SCRIPT)
MOD=importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MOD)
DB=None

class TestFormat(unittest.TestCase):
    def test_numeric_decimals(self):
        self.assertEqual(MOD.score_type('7,5'), 'NUMERICO')
        self.assertEqual(MOD.score_type('-1.5'), 'NUMERICO')
    def test_non_numeric(self):
        self.assertEqual(MOD.score_type('sv/ng'), 'NON_NUMERICO')
        self.assertEqual(MOD.score_type('NaN'), 'NON_NUMERICO')
        self.assertEqual(MOD.score_type(''), 'VUOTO')
    def test_tab_alignment_and_special(self):
        s, data=MOD.index_tabellino({'LISTA':'0%42%-1','RUOLO':'0%1%1',
                                    'VOTO':'0%6%0','MODIF':'0%0%0','TOT':'0%6%0'})
        self.assertEqual(s,'ALLINEATO')
        self.assertEqual(data[42],['6'])
        self.assertEqual(data[0],['0'])
        self.assertEqual(data[-1],['0'])
    def test_non_aligned(self):
        s,_=MOD.index_tabellino({'LISTA':'1%2','RUOLO':'0','VOTO':'6%7','MODIF':'0%0','TOT':'6%7'})
        self.assertEqual(s,'LUNGHEZZE_DIVERSE')
    def test_position(self):
        self.assertEqual(MOD.pos_group(0),'POS_0_TITOLARE')
        self.assertEqual(MOD.pos_group(-1),'POS_NEG_FUORI')
        self.assertEqual(MOD.pos_group(9),'POS_POS_PANCHINA')

class TestDatabase(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.conn=sqlite3.connect(DB.as_uri()+'?mode=ro',uri=True)
        cls.conn.row_factory=sqlite3.Row
        cls.conn.execute('PRAGMA query_only=ON')
    @classmethod
    def tearDownClass(cls): cls.conn.close()
    def test_all_season_count(self):
        self.assertEqual(len(MOD.seasons(self.conn)),21)
    def test_2006(self):
        r=MOD.audit_season(self.conn,'2006_2007')
        self.assertEqual(r['formazioni'],6050)
        self.assertEqual(sum(r['combinazioni'].values()),6050)
        self.assertEqual(r['marcatori']['PANCHINA_NON_ENTRATA_TOT_NUMERICO'],837)
    def test_2025(self):
        r=MOD.audit_season(self.conn,'2025_2026')
        self.assertEqual(r['formazioni'],39850)
        self.assertEqual(sum(r['combinazioni'].values()),39850)
        self.assertEqual(r['marcatori']['PANCHINA_NON_ENTRATA_TOT_NUMERICO'],7970)
        self.assertEqual(r['marcatori']['PANCHINA_ENTRATA_SENZA_FLAG_RISERVA'],1547)
    def test_2026_empty(self):
        r=MOD.audit_season(self.conn,'2026_2027')
        self.assertEqual(r['formazioni'],0)

def main():
    global DB
    parser=argparse.ArgumentParser()
    parser.add_argument('--database',required=True,type=Path)
    args=parser.parse_args()
    DB=args.database.resolve(strict=True)
    suite=unittest.defaultTestLoader.loadTestsFromModule(sys.modules[__name__])
    outcome=unittest.TextTestRunner(verbosity=2).run(suite)
    return 0 if outcome.wasSuccessful() else 1

if __name__=='__main__': sys.exit(main())
