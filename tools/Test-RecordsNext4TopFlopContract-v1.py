#!/usr/bin/env python3
"""Read-only regression anchors for the actual RecordsNext 3.2 Java integration."""
from pathlib import Path
import argparse
import unittest

ROOT = None


def read(rel):
    p = ROOT / rel
    if not p.is_file():
        raise AssertionError('Sorgente 3.2 mancante: '+rel)
    return p.read_text(encoding='utf-8-sig')


PREFIX = 'src/main/java/it/alterlega/recordsnext/'


class Contract(unittest.TestCase):
    def test_family_catalog(self):
        text = read(PREFIX+'app/model/RecordFamily.java')
        self.assertIn('CLASSICS(',text)
        self.assertIn('THRESHOLDS_LUCK(',text)

    def test_children_catalog(self):
        text=read(PREFIX+'app/model/CoreRecordCatalog.java')
        self.assertIn('ProcessingConfigWriter.CLASSICS',text)
        self.assertIn('addFamilyChildren(',text)

    def test_writer_state_and_config(self):
        text=read(PREFIX+'app/config/ProcessingConfigWriter.java')
        self.assertIn('public record State(',text)
        self.assertIn('public static void save(',text)
        self.assertIn('"schemaVersion": "2.0"',text)

    def test_config_loader(self):
        text=read(PREFIX+'app/config/ProcessingConfigLoader.java')
        self.assertIn('RecordFamily.values()',text)
        self.assertIn('CoreRecordCatalog',text)

    def test_dependencies_preflight(self):
        text=read(PREFIX+'app/PipelinePreflight.java')
        self.assertIn('DependencyInventory',text)
        self.assertIn('ExecutionPlanner.plan(',text)

    def test_pipeline_lifecycle(self):
        text=read(PREFIX+'app/RecordsNextPipeline.java')
        for token in ['IMPLEMENTED_FAMILIES', 'RecordsNextPreparationService', 'SeasonRecordsArchiveBuilder.build(', 'PublicationMode.ALL_CONFIGURED_SITES','SeasonFamilyShardPublisher.prepareFlat(', 'publishGeneratedDirectory(']:
            self.assertIn(token,text)

    def test_publisher_validation_and_rollback(self):
        text=read(PREFIX+'Records2026SitePublisher.java')
        for token in ['validateGenerated(', 'expectedTotal', 'publishWithRollback(', 'ManifestPublishingSupport.write(', 'SeasonFamilyShardPublisher.prepareFlat(']:
            self.assertIn(token,text)

    def test_shard_limit_and_family_specs(self):
        text=read(PREFIX+'app/output/SeasonFamilyShardPublisher.java')
        for token in ['DEFAULT_FLAT_MAX_SHARD_BYTES', '1536L * 1024L', 'private static final List<FamilySpec> FAMILIES', 'prepareFlat(']:
            self.assertIn(token,text)

    def test_match_public_contract(self):
        text=read(PREFIX+'app/matches/MatchesJsExporter.java')
        for token in ['"seasonId"', '"competitionId"', '"matchId"', '"scorecardUrl"']:
            self.assertIn(token,text)

    def test_normalized_links(self):
        text=read(PREFIX+'SeasonNormalizedExporter.java')
        for token in ['urlTabellino', 'urlTabellinoLocale', 'urlTabellinoOnline']:
            self.assertIn(token,text)

    def test_gui_family_checks(self):
        text=read(PREFIX+'gui/RecordsNext2Dashboard.java')
        for token in ['ProcessingConfigWriter', 'JTabbedPane', 'familySelectionPanel(']:
            self.assertIn(token,text)

    def test_existing_regression_test_sources(self):
        base=ROOT/'src/test/java/it/alterlega/recordsnext/'
        files=['app/model/CoreRecordCatalogCompletenessTest.java', 'app/RecordsNextPipelinePublicationModeTest.java', 'app/output/SeasonFamilyShardPublisherFlatTest.java']
        for rel in files:
            self.assertTrue((base/rel).is_file(), str(base/rel))


if __name__ == '__main__':
    p=argparse.ArgumentParser()
    p.add_argument('--project-dir',type=Path,required=True)
    args=p.parse_args()
    ROOT=args.project_dir.resolve(strict=True)
    suite=unittest.defaultTestLoader.loadTestsFromTestCase(Contract)
    result=unittest.TextTestRunner(verbosity=2).run(suite)
    raise SystemExit(0 if result.wasSuccessful() else 1)
