#!/usr/bin/env python3
"""Test sintetici delle quattro metriche; non interpreta dati FCM/FCA."""
from __future__ import annotations
from dataclasses import dataclass
from decimal import Decimal
import unittest


@dataclass(frozen=True)
class Metriche:
    esclusioni: int
    con_voto: int
    somma_voti: Decimal
    somma_fantavoti: Decimal
    media_voto: Decimal | None
    errore_pesato_voto: Decimal | None
    fantamedia: Decimal | None
    errore_pesato_fantavoto: Decimal | None
    quota_con_voto: Decimal | None


def calcola(esclusioni: int, voti: list[Decimal], fantavoti: list[Decimal]) -> Metriche:
    if isinstance(esclusioni, bool) or not isinstance(esclusioni, int) or esclusioni < 0:
        raise ValueError('esclusioni invalide')
    if len(voti) != len(fantavoti):
        raise ValueError('voti e fantavoti devono avere gli stessi eventi validi')
    p = len(voti)
    if p > esclusioni:
        raise ValueError('prestazioni valide superiori alle esclusioni')
    if any(not isinstance(v, Decimal) or not v.is_finite() for v in voti + fantavoti):
        raise ValueError('richiesti Decimal finiti (niente float/NaN/Infinity)')
    sv = sum(voti, Decimal(0))
    sf = sum(fantavoti, Decimal(0))
    e = Decimal(esclusioni)
    pd = Decimal(p)
    return Metriche(esclusioni, p, sv, sf,
                   sv / pd if p else None,
                   sv / e if esclusioni else None,
                   sf / pd if p else None,
                   sf / e if esclusioni else None,
                   pd / e if esclusioni else None)


class ContrattoMetriche(unittest.TestCase):
    def test_tre_esclusioni_tre_voti(self):
        x = calcola(3, [Decimal('5.5'), Decimal('7'), Decimal('8')],
                    [Decimal('5.5'), Decimal('10'), Decimal('7.5')])
        self.assertEqual(x.media_voto, Decimal('20.5') / 3)
        self.assertEqual(x.errore_pesato_voto, Decimal('20.5') / 3)
        self.assertEqual(x.fantamedia, Decimal('23') / 3)
        self.assertEqual(x.errore_pesato_fantavoto, Decimal('23') / 3)
        self.assertEqual(x.quota_con_voto, Decimal('1'))

    def test_cinque_esclusioni_tre_voti(self):
        x = calcola(5, [Decimal('5.5'), Decimal('7'), Decimal('8')],
                    [Decimal('5.5'), Decimal('10'), Decimal('7.5')])
        self.assertEqual(x.media_voto, Decimal('20.5') / 3)
        self.assertEqual(x.errore_pesato_voto, Decimal('4.1'))
        self.assertEqual(x.fantamedia, Decimal('23') / 3)
        self.assertEqual(x.errore_pesato_fantavoto, Decimal('4.6'))
        self.assertEqual(x.quota_con_voto, Decimal('0.6'))

    def test_ventitre_esclusioni_tre_6(self):
        x = calcola(23, [Decimal('6')] * 3, [Decimal('6')] * 3)
        self.assertEqual(x.media_voto, Decimal('6'))
        self.assertEqual(x.errore_pesato_voto, Decimal('18') / 23)

    def test_sette_integrale_non_sopra_sei(self):
        x = calcola(1, [Decimal('7')], [Decimal('7')])
        self.assertEqual(x.media_voto, Decimal('7'))
        self.assertEqual(x.errore_pesato_voto, Decimal('7'))

    def test_solo_esclusioni_senza_voto(self):
        x = calcola(4, [], [])
        self.assertIsNone(x.media_voto)
        self.assertIsNone(x.fantamedia)
        self.assertEqual(x.errore_pesato_voto, Decimal('0'))
        self.assertEqual(x.errore_pesato_fantavoto, Decimal('0'))
        self.assertEqual(x.quota_con_voto, Decimal('0'))

    def test_nessuna_esclusione(self):
        x = calcola(0, [], [])
        self.assertIsNone(x.media_voto)
        self.assertIsNone(x.errore_pesato_voto)
        self.assertIsNone(x.fantamedia)
        self.assertIsNone(x.errore_pesato_fantavoto)
        self.assertIsNone(x.quota_con_voto)

    def test_malus_puo_ridurre_fantavoto(self):
        x = calcola(1, [Decimal('7')], [Decimal('6.5')])
        self.assertEqual(x.fantamedia, Decimal('6.5'))

    def test_restrizioni_input(self):
        for args in [(-1, [], []), (True, [], []), (1, [Decimal('7')], []),
                     (1, [Decimal('7')]*2, [Decimal('7')]*2),
                     (1, [Decimal('NaN')], [Decimal('7')]),
                     (1, [7.0], [Decimal('7')])]:
            with self.subTest(args=args), self.assertRaises(ValueError):
                calcola(*args)


if __name__ == '__main__':
    unittest.main(verbosity=2)
