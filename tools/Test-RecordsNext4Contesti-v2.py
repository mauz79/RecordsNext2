#!/usr/bin/env python3
"""Pure regression tests: no databases or worktree mutations."""
from __future__ import annotations
import importlib.util
from pathlib import Path
import unittest

s = Path(__file__).with_name('Audit-RecordsNext4Contesti-v2.py')
spec = importlib.util.spec_from_file_location('rn4_contesti_audit', s)
audit = importlib.util.module_from_spec(spec)
spec.loader.exec_module(audit)

class Contract(unittest.TestCase):
    def test_campionato(self):
        for name in ('Campionato', 'Serie A', 'Serie B', 'Serie C', 'Play Off - Play Out', 'Spareggio Serie B'):
            self.assertEqual(audit.family(name), 'CAMPIONATO')

    def test_coppe(self):
        for name in ('Coppa Serie A', 'Coppa di Lega', 'Supercoppa Serie B',
                     'Europa Pipps', 'Mitropa Cup', 'Coppa Reame Perduto'):
            self.assertEqual(audit.family(name), 'COPPE')

    def test_youth_league_esclusa(self):
        self.assertFalse(audit.included('Youth League'))
        self.assertIsNone(audit.family('Youth League'))

    def test_playoff_spareggio_ammessi(self):
        self.assertTrue(audit.included('Play Off - Play Out'))
        self.assertTrue(audit.included('Spareggio Serie B'))

    def test_coppe_ammessa(self):
        self.assertTrue(audit.included('Coppa Serie C'))

    def test_no_famiglie_inventate(self):
        self.assertIsNone(audit.family('Coppa nuova da verificare'))

    def test_titolare_iniziale_non_e_giocatore_conteggiato(self):
        self.assertEqual(audit.initial_status(0), 'TITOLARE_INIZIALE')

    def test_panchina_e_fuori_lista_distinti(self):
        self.assertEqual(audit.initial_status(3), 'PANCHINA')
        self.assertEqual(audit.initial_status(-1), 'FUORI_LISTA_FORMAZIONE')

    def test_pos_anomale_non_inventate(self):
        self.assertEqual(audit.initial_status(None), 'POS_DA_VERIFICARE')
        self.assertEqual(audit.initial_status(-7), 'POS_DA_VERIFICARE')

    def test_nomi_tabelle_whitelist(self):
        self.assertEqual(audit.table('2025_2026', 'fcm', 'formazione'),
                         '"raw_2025_2026_fcm_formazione"')
        with self.assertRaises(ValueError):
            audit.table('2025_2026; DROP TABLE x', 'fcm', 'formazione')

    def test_two_competitions_not_one_fact(self):
        # Same real day and team, different competition/match remains two facts.
        facts = [('2025_2026', 366, 8, 4, 32157, 4),
                 ('2025_2026', 366, 8, 5, 32649, 4)]
        self.assertEqual(len(set(facts)), 2)
        self.assertEqual(len({(x[0], x[1], x[5]) for x in facts}), 1)

    def test_id_incontro_non_e_giornata(self):
        refs = [('2025_2026', 'Serie B', 32157, 4),
                ('2025_2026', 'Coppa Serie B', 32649, 4)]
        self.assertNotEqual(refs[0][2], refs[1][2])
        self.assertEqual(refs[0][3], refs[1][3])

    def test_three_teams_are_distinct(self):
        facts = {('2025_2026', 366, x, 32157+x) for x in (8, 49, 77)}
        self.assertEqual(len(facts), 3)

if __name__ == '__main__':
    unittest.main(verbosity=2)
