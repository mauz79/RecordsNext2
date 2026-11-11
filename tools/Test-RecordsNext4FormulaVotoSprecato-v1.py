#!/usr/bin/env python3
"""Tests ONLY the approved arithmetic; it does not classify FCM/FCA events."""
from __future__ import annotations
from decimal import Decimal
import unittest


def media_voto_sprecato(esclusioni: int, voti_validi: list[Decimal]):
    if not isinstance(esclusioni, int) or esclusioni < 0:
        raise ValueError('esclusioni non valido')
    if len(voti_validi) > esclusioni:
        raise ValueError('piu voti che esclusioni')
    if esclusioni == 0:
        return None
    return sum(voti_validi, Decimal('0')) / Decimal(esclusioni)


class ContrattoMetrica(unittest.TestCase):
    def test_esempio_utente(self):
        self.assertEqual(media_voto_sprecato(23, [Decimal('6')]*3), Decimal(18)/Decimal(23))

    def test_zero_voto_ma_esclusioni(self):
        self.assertEqual(media_voto_sprecato(23, []), Decimal('0'))

    def test_nessuna_esclusione(self):
        self.assertIsNone(media_voto_sprecato(0, []))

    def test_non_diluire_solo_sulle_gare_con_voto(self):
        self.assertNotEqual(media_voto_sprecato(23, [Decimal('6')]*3), Decimal('6'))

    def test_validazioni(self):
        with self.assertRaises(ValueError):
            media_voto_sprecato(2, [Decimal('6')]*3)
        with self.assertRaises(ValueError):
            media_voto_sprecato(-1, [])


if __name__ == '__main__':
    unittest.main(verbosity=2)
