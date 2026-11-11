#!/usr/bin/env python3
"""Test standard library dell'estrattore FCA reale v1. Solo lettura.

Uso: py -3 tools/Test-RecordsNext4Reali-v1.py --database <DB>
"""
from __future__ import annotations
import argparse
import importlib.util
from pathlib import Path
import sqlite3
import sys
import unittest

MODULE = Path(__file__).with_name('Estrai-RecordsNext4Reali-v1.py')
spec = importlib.util.spec_from_file_location('recordsnext4_real_v1', MODULE)
extractor = importlib.util.module_from_spec(spec)
spec.loader.exec_module(extractor)


class TestRegolePure(unittest.TestCase):
    def test_stati_voto1_non_interpretati_come_presenze(self):
        self.assertEqual('VOTO1_ASSENTE', extractor.vote_state(None))
        self.assertEqual('VOTO1_ASSENTE', extractor.vote_state({'VOTO1': None}))
        self.assertEqual('VOTO1_ZERO', extractor.vote_state({'VOTO1': 0}))
        self.assertEqual('VOTO1_MENO_UNO', extractor.vote_state({'VOTO1': -1}))
        self.assertEqual('VOTO1_POSITIVO', extractor.vote_state({'VOTO1': 6.5}))

    def test_partita_reale_non_si_inventa(self):
        ref = {'teams': {1: 'Casa', 2: 'Fuori'},
               'encounters': {(1, 1): [{'GIORNATA': 1, 'IDCASA': 1,
                                         'IDFUORI': 2, 'GOLCASA': 2, 'GOLFUORI': 1}],
                              (2, 1): [{'GIORNATA': 2, 'IDCASA': 1, 'IDFUORI': 2},
                                       {'GIORNATA': 2, 'IDCASA': 1, 'IDFUORI': 3}]}}
        self.assertEqual('COLLEGATO', extractor.real_match(1, 1, ref)['stato'])
        self.assertEqual('Fuori', extractor.real_match(1, 1, ref)['dettaglio']['avversario'])
        self.assertEqual('NON_COLLEGATO', extractor.real_match(3, 1, ref)['stato'])
        self.assertEqual('AMBIGUO', extractor.real_match(2, 1, ref)['stato'])

    def test_anagrafiche_omonime_codice_non_vengono_fuse(self):
        recs = [
            {'stagione': '2006_2007', 'giocatoreAFca': {'NOME': 'A', 'DATADINASCITA': '1979-01-01'}},
            {'stagione': '2015_2016', 'giocatoreAFca': {'NOME': 'B', 'DATADINASCITA': '1991-01-01'}},
        ]
        self.assertEqual(1, len(extractor.identity_warning(recs)))
        recs[1]['giocatoreAFca'] = recs[0]['giocatoreAFca']
        self.assertEqual([], extractor.identity_warning(recs))


class TestDbOriginale(unittest.TestCase):
    DB = None

    @classmethod
    def setUpClass(cls):
        if cls.DB is None:
            raise unittest.SkipTest('DB non specificato')
        cls.con = sqlite3.connect(cls.DB.resolve().as_uri() + '?mode=ro', uri=True)
        cls.con.row_factory = sqlite3.Row
        cls.con.execute('PRAGMA query_only=ON')

    @classmethod
    def tearDownClass(cls):
        if hasattr(cls, 'con'):
            cls.con.close()

    def test_copertura_21_anni_e_8_famiglie(self):
        years = extractor.seasons(self.con)
        self.assertEqual(21, len(years))
        for year in years:
            extractor.validate_schema(self.con, year)

    def test_lautaro_code_100843_nove_stagioni(self):
        available = [year for year in extractor.seasons(self.con)
                     if extractor.select_players(self.con, year, 100843)]
        self.assertEqual(['2018_2019','2019_2020','2020_2021','2021_2022',
                          '2022_2023','2023_2024','2024_2025','2025_2026',
                          '2026_2027'], available)
        all_rows = []
        for year in available:
            pp = extractor.select_players(self.con, year, 100843)
            extracted, _ = extractor.build_season(self.con, year, pp,
                                                  extractor.load_references(self.con, year))
            self.assertEqual(1, len(extracted))
            self.assertEqual(38, len(extracted[0]['giornateReali']))
            all_rows.extend(extracted)
        self.assertEqual([], extractor.identity_warning(all_rows))
        yr25 = next(r for r in all_rows if r['stagione'] == '2025_2026')
        stat = yr25['statisticheFca']
        self.assertEqual(1, len(stat))
        self.assertEqual(17, stat[0]['GOLFATTI'])
        self.assertEqual(6, stat[0]['ASSIST'])
        day_sum_goals = sum((r['punteggioFca'] or {}).get('GOLFATTI1') or 0
                            for r in yr25['giornateReali'])
        day_sum_assists = sum((r['punteggioFca'] or {}).get('ASSIST') or 0
                             for r in yr25['giornateReali'])
        self.assertEqual(17, day_sum_goals)
        self.assertEqual(6, day_sum_assists)

    def test_12302_non_aggregabile_automaticamente(self):
        candidates = []
        for y in extractor.seasons(self.con):
            for p in extractor.select_players(self.con, y, 12302):
                candidates.append({'stagione': y, 'giocatoreAFca': p})
        self.assertEqual(3, len(candidates))
        self.assertTrue(extractor.identity_warning(candidates))

    def test_statistiche_e_storico_sono_fonti_diverse(self):
        yr = '2025_2026'
        p = extractor.select_players(self.con, yr, 100843)
        recs, _ = extractor.build_season(self.con, yr, p, extractor.load_references(self.con, yr))
        r = recs[0]
        self.assertEqual(1, len(r['statisticheFca']))
        self.assertGreater(len(r['storicoFcaSnapshot']), 1)
        self.assertEqual(38, len(r['giornateReali']))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--database', type=Path, required=True)
    args = parser.parse_args()
    if not args.database.is_file():
        parser.error('Database non trovato: ' + str(args.database))
    TestDbOriginale.DB = args.database
    suite = unittest.TestSuite()
    suite.addTests(unittest.defaultTestLoader.loadTestsFromTestCase(TestRegolePure))
    suite.addTests(unittest.defaultTestLoader.loadTestsFromTestCase(TestDbOriginale))
    result = unittest.TextTestRunner(verbosity=2, stream=sys.stdout).run(suite)
    return 0 if result.wasSuccessful() else 1


if __name__ == '__main__':
    sys.exit(main())
