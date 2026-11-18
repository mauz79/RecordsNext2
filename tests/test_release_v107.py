#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Regressioni strutturali, senza modificare Git, Windows o siti."""
import importlib.util
from pathlib import Path
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]
SPEC = importlib.util.spec_from_file_location('rn4_v107_builder', ROOT/'tools/RN4_Crea-Installer-v107.py')
B = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(B)


class ReleaseTests(unittest.TestCase):
    def test_iss_explicitly_excludes_user_data(self):
        text=(ROOT/'installer/RecordsNext4_v107.iss').read_text('utf-8')
        files=text.split('[Files]',1)[1].split('[Icons]',1)[0]
        self.assertNotIn('payload\\config', files)
        self.assertNotIn('payload\\data', files)
        self.assertNotIn('player-identities.json', files)
        self.assertIn('payload\\runtime\\ucanaccess',files)
        self.assertIn('payload\\assets',files)
        self.assertIn('PrivilegesRequired=lowest',text)
        self.assertIn('DefaultDirName={localappdata}',text)
        self.assertIn('not FileExists(InstallDir',text)
        self.assertNotIn('[UninstallDelete]',text)

    def test_user_data_preserved_in_simulated_update(self):
        with tempfile.TemporaryDirectory() as t:
            p=Path(t)/'payload'
            p.mkdir()
            (p/'RecordsNext.jar').write_bytes(b'fakejar')
            (p/'RecordsNext.bat').write_text('start javaw',encoding='utf-8')
            (p/'runtime/ucanaccess').mkdir(parents=True)
            (p/'runtime/ucanaccess/ucanaccess-2.0.9.5.jar').write_bytes(b'fakejar')
            self.assertEqual(B.privacy_upgrade_simulation(p),'PASS')

    def test_no_private_records_in_payload(self):
        with tempfile.TemporaryDirectory() as t:
            p=Path(t)/'payload'; p.mkdir()
            (p/'RecordsNext.jar').write_bytes(b'fakejar')
            (p/'RecordsNext.bat').write_text('ok',encoding='utf-8')
            self.assertEqual(len(B.private_in_payload(p)),2)
            (p/'config').mkdir()
            (p/'config/player-identities.json').write_text('{}',encoding='utf-8')
            with self.assertRaises(RuntimeError): B.private_in_payload(p)

    def test_absolute_user_paths_blocked(self):
        with tempfile.TemporaryDirectory() as t:
            p=Path(t)/'payload';p.mkdir()
            (p/'RecordsNext.jar').write_bytes(b'fakejar')
            (p/'asset.js').write_text('const x="E:\\\\FCM\\\\data"',encoding='utf-8')
            with self.assertRaises(RuntimeError): B.private_in_payload(p)

    def test_generator_has_full_bible_guard(self):
        s=(ROOT/'tools/Genera-Bibbia-RecordsNext-v107.py').read_text('utf-8')
        self.assertIn('len(base) < 1_000_000',s)
        self.assertIn('CODICE_FUNZIONANTE_RECORDSNEXT4_v54.md',s)

    def test_bat_java_version_check(self):
        s=(ROOT/'installer/RecordsNext.bat').read_text('ascii')
        self.assertIn('LSS 21',s)
        self.assertIn('RecordsNext.jar',s)
        self.assertIn('ucanaccess',s)


    def test_staging_packs_only_static_and_jars(self):
        from unittest.mock import patch
        with tempfile.TemporaryDirectory() as d:
            tmp=Path(d)
            repo=tmp/'repo'; plugin=tmp/'plugin'
            (repo/'assets/js').mkdir(parents=True)
            (repo/'assets/js/fcmRecordsNextFunzioni_topflop_v92.js').write_text('window.rn4=true;',encoding='utf-8')
            (repo/'target').mkdir()
            import zipfile
            with zipfile.ZipFile(repo/'target/RecordsNext.jar','w') as z:
                z.writestr('it/alterlega/recordsnext/gui/RecordsNext2Dashboard.class',b'not-a-class')
            (plugin/'runtime/ucanaccess/lib').mkdir(parents=True)
            for name in ['ucanaccess-2.0.9.5.jar','lib/jackcess.jar','lib/hsqldb.jar',
                         'lib/commons-lang.jar','lib/commons-logging.jar']:
                (plugin/'runtime/ucanaccess'/name).write_bytes(b'jar-file')
            (plugin/'config').mkdir()
            (plugin/'config/player-identities.json').write_text('{"approved":{"x":"y"}}',encoding='utf-8')
            (plugin/'data/database').mkdir(parents=True)
            (plugin/'data/database/recordsnext.db').write_bytes(b'PRIVATE DATABASE')
            build=tmp/'build'
            with patch.object(B,'REPO',repo), patch.object(B,'PLUGIN',plugin), \
                 patch.object(B,'BUILD',build), patch.object(B,'STAGE',build/'payload'), \
                 patch.object(B,'compiler',return_value=None), \
                 patch.object(B,'listed_assets',return_value=['assets/js/fcmRecordsNextFunzioni_topflop_v92.js']):
                stats=B.stage_public_payload()
                self.assertGreaterEqual(stats['file_payload'],7)
                self.assertTrue((build/'payload/RecordsNext.jar').is_file())
                self.assertFalse((build/'payload/config').exists())
                self.assertFalse((build/'payload/data').exists())
                self.assertTrue((build/'RecordsNext4_v107.iss').is_file())


if __name__=='__main__':
    unittest.main(verbosity=2)
