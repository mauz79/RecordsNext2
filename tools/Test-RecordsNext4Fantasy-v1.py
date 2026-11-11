#!/usr/bin/env python3
"""Test di regressione Fase 2 sul DB 3.2: soli SELECT e test semantici."""
from __future__ import annotations
import argparse
import importlib.util
from pathlib import Path
import sqlite3
import sys
import unittest

HERE = Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location('fantasy_v1', HERE/'Estrai-RecordsNext4Fantasy-v1.py')
fantasy = importlib.util.module_from_spec(spec)
spec.loader.exec_module(fantasy)
DB = None


class TestCampi(unittest.TestCase):
    def test_senza_voto_e_decimali_restano_stringhe(self):
        tab = {'LISTA':'1%-1%0', 'RUOLO':'4%4%4', 'VOTO':'6,5%sv/ng%0',
               'MODIF':'0%-1%0', 'TOT':'9,5%sv/ng%0'}
        idx = fantasy.index_tab(tab)
        self.assertEqual(idx['stato'], 'LISTE_ALLINEATE')
        self.assertEqual(idx['slots'][1][0]['votoRaw'], '6,5')
        self.assertEqual(idx['slots'][1][0]['totaleRaw'], '9,5')
        self.assertEqual(idx['slots'][-1][0]['votoRaw'], 'sv/ng')
        self.assertEqual(idx['slots'][0][0]['listaRaw'], '0')

    def test_duplicazioni_non_arbitrariamente_risolte(self):
        t={'LISTA':'1%1','RUOLO':'2%2','VOTO':'6%6','MODIF':'0%0','TOT':'6%6'}
        idx=fantasy.index_tab(t)
        self.assertEqual(len(idx['slots'][1]),2)

    def test_liste_disallineate(self):
        t={'LISTA':'1%2','RUOLO':'1','VOTO':'6%7','MODIF':'0%0','TOT':'6%7'}
        self.assertEqual(fantasy.index_tab(t)['stato'],'LUNGHEZZE_DIVERSE')


class TestDatabase(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.conn=sqlite3.connect(DB.resolve().as_uri()+'?mode=ro',uri=True)
        cls.conn.row_factory=sqlite3.Row
        cls.conn.execute('PRAGMA query_only=ON')

    @classmethod
    def tearDownClass(cls):
        cls.conn.close()

    def test_21_stagioni(self):
        self.assertEqual(len(fantasy.get_seasons(self.conn)),21)

    def test_lautaro_2025_partite_162(self):
        arr=[]
        d=fantasy.run_export(self.conn,'2025_2026',100843,arr.append)
        self.assertEqual(d['formazioni'],162)
        self.assertEqual(len(arr),162)
        self.assertEqual(d['statoTabellino'],{'SLOT_UNICO':162})
        self.assertEqual(d['senzaGiocaIn'],0)
        self.assertTrue(all(x['codiceStoricoSorgente']==100843 for x in arr))
        self.assertTrue(all(x['incontroFcm'] is not None for x in arr))

    def test_2025_26_totali_e_integrita(self):
        d=fantasy.run_export(self.conn,'2025_2026',None,lambda _: None)
        self.assertEqual(d['formazioni'],39850)
        self.assertEqual(d['statoTabellino'],{'SLOT_UNICO':39850})
        self.assertEqual(d['senzaIncontro'],0)
        self.assertEqual(d['senzaGiocaIn'],0)
        self.assertEqual(d['senzaGiocatoreFca'],0)
        self.assertEqual(d['senzaCompetizione'],0)

    def test_2006_07_totali_e_integrita(self):
        d=fantasy.run_export(self.conn,'2006_2007',None,lambda _: None)
        self.assertEqual(d['formazioni'],6050)
        self.assertEqual(d['statoTabellino'],{'SLOT_UNICO':6050})

    def test_2026_27_vuota(self):
        d=fantasy.run_export(self.conn,'2026_2027',None,lambda _: None)
        self.assertEqual(d['formazioni'],0)


if __name__=='__main__':
    p=argparse.ArgumentParser()
    p.add_argument('--database',type=Path,required=True)
    args=p.parse_args()
    DB=args.database
    unittest.main(argv=[sys.argv[0]],verbosity=2)
