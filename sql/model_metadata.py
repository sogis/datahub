#!/usr/bin/env python3
"""Build a reviewable metadata draft from ili2pg SQL, using our retained physical names."""
from pathlib import Path
import re

TABLES = {'deliveries_validationattempt': 'validation_attempt',
          'deliveries_datasetpublication': 'dataset_publication',
          'deliveries_notification': 'notification'}
COLUMNS = {
    'core_apikey': {'issuancestate': 'issuance_state'},
    'core_theme': {'expectedmodels': 'expected_models'},
    'deliveries_delivery': {name.replace('_', ''): name for name in (
        'operat_id', 'key_id', 'attempt_id', 'validator_config', 'validator_meta_config',
        'expected_models', 'execution_status', 'validation_status', 'publication_status',
        'notification_status', 'publication_tries', 'publication_next_at', 'updated_at',
        'finished_at', 'artifacts_expired')},
    'deliveries_validationattempt': {'astate': 'state', 'delivery_r': 'delivery_id', **{
        name.replace('_', ''): name for name in ('dispatch_state', 'created_at', 'started_at',
        'finished_at', 'heartbeat_at', 'validator_version', 'result_models', 'model_metadata',
        'configuration_snapshot', 'transfer_version')}},
    'deliveries_datasetpublication': {'operatid': 'operat_id', 'delivery_r': 'delivery_id'},
    'deliveries_notification': {'astate': 'state', 'asubject': 'subject',
        'nextat': 'next_at', 'leaseuntil': 'lease_until', 'delivery_r': 'delivery_id'},
}
META = ('CLASSNAME', 'ATTRNAME', 'COLUMN_PROP', 'TABLE_PROP', 'META_ATTRS', 'MODEL')


def statements(text):
    quoted = False
    start = 0
    i = 0
    while i < len(text):
        if text[i] == "'":
            if quoted and i + 1 < len(text) and text[i + 1] == "'":
                i += 2
                continue
            quoted = not quoted
        elif text[i] == ';' and not quoted:
            yield text[start:i].strip() + ';'
            start = i + 1
        i += 1


out = ['-- Generated metadata draft; review before including in a new migration.']
for kind in ('config', 'log'):
    schema = 'agi_datahub_' + kind + '_v1'
    placeholder = '${' + kind + 'Schema}'
    out += [f'DELETE FROM {placeholder}.T_ILI2DB_{table};' for table in META]
    inserts = []
    for statement in statements(Path('build/model-ddl/' + schema + '.sql').read_text()):
        if not re.match(r'INSERT INTO .*\.T_ILI2DB_(' + '|'.join(META) + r')\b', statement):
            continue
        if '.T_ILI2DB_MODEL ' not in statement:
            for owner, columns in COLUMNS.items():
                if "'" + owner + "'" in statement:
                    for source, target in columns.items():
                        statement = statement.replace("'" + source + "'", "'" + target + "'")
            for source, target in TABLES.items():
                statement = statement.replace("'" + source + "'", "'" + target + "'")
        inserts.append(statement.replace(schema, placeholder))
    out += sorted(inserts)
Path('build/model-ddl/metadata.sql').write_text('\n'.join(out) + '\n')
print('build/model-ddl/metadata.sql')
