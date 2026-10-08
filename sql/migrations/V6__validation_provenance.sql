ALTER TABLE ${logSchema}.validation_attempt ADD COLUMN model_metadata text, ADD COLUMN configuration_snapshot text, ADD COLUMN transfer_version varchar(10);
-- Generated metadata draft; review before including in a new migration.
DELETE FROM ${configSchema}.T_ILI2DB_CLASSNAME;
DELETE FROM ${configSchema}.T_ILI2DB_ATTRNAME;
DELETE FROM ${configSchema}.T_ILI2DB_COLUMN_PROP;
DELETE FROM ${configSchema}.T_ILI2DB_TABLE_PROP;
DELETE FROM ${configSchema}.T_ILI2DB_META_ATTRS;
DELETE FROM ${configSchema}.T_ILI2DB_MODEL;
INSERT INTO ${configSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.apiKey','apikey','core_apikey',NULL);
INSERT INTO ${configSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.createdAt','createdat','core_apikey',NULL);
INSERT INTO ${configSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.dateOfExpiry','dateofexpiry','core_apikey',NULL);
INSERT INTO ${configSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.issuanceState','issuance_state','core_apikey',NULL);
INSERT INTO ${configSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.revokedAt','revokedat','core_apikey',NULL);
INSERT INTO ${configSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat.description','adescription','core_operat',NULL);
INSERT INTO ${configSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat.name','aname','core_operat',NULL);
INSERT INTO ${configSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Organisation_R','organisation_r','core_operat','core_organisation');
INSERT INTO ${configSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.email','email','core_organisation',NULL);
INSERT INTO ${configSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.name','aname','core_organisation',NULL);
INSERT INTO ${configSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.role','arole','core_organisation',NULL);
INSERT INTO ${configSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.uid','auid','core_organisation',NULL);
INSERT INTO ${configSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.Organisation_R','organisation_r','core_apikey','core_organisation');
INSERT INTO ${configSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.config','config','core_theme',NULL);
INSERT INTO ${configSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.description','adescription','core_theme',NULL);
INSERT INTO ${configSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.expectedModels','expected_models','core_theme',NULL);
INSERT INTO ${configSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.metaConfig','metaconfig','core_theme',NULL);
INSERT INTO ${configSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.name','aname','core_theme',NULL);
INSERT INTO ${configSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Theme_R','theme_r','core_operat','core_theme');
INSERT INTO ${configSchema}.T_ILI2DB_CLASSNAME (IliName,SqlName) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey','core_apikey');
INSERT INTO ${configSchema}.T_ILI2DB_CLASSNAME (IliName,SqlName) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat','core_operat');
INSERT INTO ${configSchema}.T_ILI2DB_CLASSNAME (IliName,SqlName) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation','core_operat_organisation');
INSERT INTO ${configSchema}.T_ILI2DB_CLASSNAME (IliName,SqlName) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation','core_organisation');
INSERT INTO ${configSchema}.T_ILI2DB_CLASSNAME (IliName,SqlName) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey','core_organisation_apikey');
INSERT INTO ${configSchema}.T_ILI2DB_CLASSNAME (IliName,SqlName) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme','core_theme');
INSERT INTO ${configSchema}.T_ILI2DB_CLASSNAME (IliName,SqlName) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme_Operat','core_theme_operat');
INSERT INTO ${configSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_apikey',NULL,'apikey','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${configSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_apikey',NULL,'createdat','ch.ehi.ili2db.typeKind','DATETIME');
INSERT INTO ${configSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_apikey',NULL,'dateofexpiry','ch.ehi.ili2db.typeKind','DATETIME');
INSERT INTO ${configSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_apikey',NULL,'issuance_state','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${configSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_apikey',NULL,'organisation_r','ch.ehi.ili2db.foreignKey','core_organisation');
INSERT INTO ${configSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_apikey',NULL,'revokedat','ch.ehi.ili2db.typeKind','DATETIME');
INSERT INTO ${configSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_operat',NULL,'adescription','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${configSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_operat',NULL,'aname','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${configSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_operat',NULL,'organisation_r','ch.ehi.ili2db.foreignKey','core_organisation');
INSERT INTO ${configSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_operat',NULL,'theme_r','ch.ehi.ili2db.foreignKey','core_theme');
INSERT INTO ${configSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_organisation',NULL,'aname','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${configSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_organisation',NULL,'arole','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${configSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_organisation',NULL,'auid','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${configSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_organisation',NULL,'email','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${configSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_theme',NULL,'adescription','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${configSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_theme',NULL,'aname','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${configSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_theme',NULL,'config','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${configSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_theme',NULL,'expected_models','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${configSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_theme',NULL,'metaconfig','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403','furtherInformation','http://geo.so.ch/models/AGI/SO_AGI_Datahub_20240301.uml');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403','technicalContact','mailto:agi@bd.so.ch');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core','ili2db.ili.topicClasses','core_apikey core_operat core_organisation core_theme');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.apiKey','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.apiKey','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.createdAt','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.createdAt','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.dateOfExpiry','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.dateOfExpiry','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.issuanceState','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.issuanceState','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.revokedAt','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.revokedAt','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat.description','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat.description','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat.name','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat.name','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Operat_R','ili2db.ili.assocCardinalityMax','*');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Operat_R','ili2db.ili.assocCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Operat_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Organisation_R','ili2db.ili.assocCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Organisation_R','ili2db.ili.assocCardinalityMin','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Organisation_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.email','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.email','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.name','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.name','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.role','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.role','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.uid','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.uid','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.ApiKey_R','ili2db.ili.assocCardinalityMax','*');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.ApiKey_R','ili2db.ili.assocCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.ApiKey_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.Organisation_R','ili2db.ili.assocCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.Organisation_R','ili2db.ili.assocCardinalityMin','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.Organisation_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.config','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.config','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.description','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.description','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.expectedModels','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.expectedModels','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.metaConfig','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.metaConfig','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.name','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.name','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Operat_R','ili2db.ili.assocCardinalityMax','*');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Operat_R','ili2db.ili.assocCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Operat_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Theme_R','ili2db.ili.assocCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Theme_R','ili2db.ili.assocCardinalityMin','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Theme_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403','furtherInformation','http://geo.so.ch/models/AGI/SO_AGI_Datahub_20240301.uml');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403','technicalContact','mailto:agi@bd.so.ch');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries','ili2db.ili.topicClasses','deliveries_datasetpublication deliveries_delivery deliveries_notification deliveries_validationattempt');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Attempt_Delivery.Attempt_R','ili2db.ili.assocCardinalityMax','*');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Attempt_Delivery.Attempt_R','ili2db.ili.assocCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Attempt_Delivery.Attempt_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Attempt_Delivery.Delivery_R','ili2db.ili.assocCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Attempt_Delivery.Delivery_R','ili2db.ili.assocCardinalityMin','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Attempt_Delivery.Delivery_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.DatasetPublication.operatId','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.DatasetPublication.operatId','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.artifactsExpired','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.artifactsExpired','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.attemptId','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.attemptId','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.deliveryDate','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.deliveryDate','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.email','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.email','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.executionStatus','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.executionStatus','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.expectedModels','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.expectedModels','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.finishedAt','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.finishedAt','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.isDelivered','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.isDelivered','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.isValid','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.isValid','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.jobId','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.jobId','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.keyId','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.keyId','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.notificationStatus','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.notificationStatus','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.operat','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.operat','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.operatId','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.operatId','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.organisation','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.organisation','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.publicationNextAt','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.publicationNextAt','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.publicationStatus','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.publicationStatus','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.publicationTries','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.publicationTries','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.theme','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.theme','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.updatedAt','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.updatedAt','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validationStatus','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validationStatus','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validatorConfig','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validatorConfig','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validatorMetaConfig','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validatorMetaConfig','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.body','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.body','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.id','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.id','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.leaseUntil','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.leaseUntil','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.nextAt','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.nextAt','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.recipient','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.recipient','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.state','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.state','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.subject','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.subject','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.tries','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.tries','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification_Delivery.Delivery_R','ili2db.ili.assocCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification_Delivery.Delivery_R','ili2db.ili.assocCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification_Delivery.Delivery_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification_Delivery.Notification_R','ili2db.ili.assocCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification_Delivery.Notification_R','ili2db.ili.assocCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification_Delivery.Notification_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Publication_Delivery.Delivery_R','ili2db.ili.assocCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Publication_Delivery.Delivery_R','ili2db.ili.assocCardinalityMin','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Publication_Delivery.Delivery_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Publication_Delivery.Publication_R','ili2db.ili.assocCardinalityMax','*');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Publication_Delivery.Publication_R','ili2db.ili.assocCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Publication_Delivery.Publication_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.configurationSnapshot','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.configurationSnapshot','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.createdAt','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.createdAt','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.dispatchState','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.dispatchState','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.finishedAt','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.finishedAt','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.heartbeatAt','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.heartbeatAt','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.id','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.id','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.modelMetadata','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.modelMetadata','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.resultModels','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.resultModels','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.startedAt','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.startedAt','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.state','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.state','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.transferVersion','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.transferVersion','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.validatorVersion','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.validatorVersion','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.version','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${configSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.version','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${configSchema}.T_ILI2DB_MODEL (filename,iliversion,modelName,content,importDate) VALUES ('SO_AGI_Datahub_20240403.ili','2.3','SO_AGI_Datahub_Config_20240403 SO_AGI_Datahub_Log_20240403','INTERLIS 2.3;

/** !!------------------------------------------------------------------------------
 *  !! Version    | wer | Änderung
 *  !!------------------------------------------------------------------------------
 *  !! 2024-02-12 | sz  | Ersterfassung
 *  !! 2024-03-01 | sz  | API-Key-Variante
 *  !! 2024-04-03 | sz  | Trennung Konfig und Log
 *  !!==============================================================================
 */
!!@ technicalContact=mailto:agi@bd.so.ch
!!@ furtherInformation=http://geo.so.ch/models/AGI/SO_AGI_Datahub_20240301.uml
MODEL SO_AGI_Datahub_Config_20240403 (de)
AT "https://agi.so.ch"
VERSION "2026-10-08"  =

  TOPIC Core =

    CLASS ApiKey =
      /** API-Key
       */
      apiKey : MANDATORY TEXT*256;
      issuanceState : TEXT*20;
      /** Zeitpunkt der Erstellung.
       */
      createdAt : MANDATORY INTERLIS.XMLDateTime;
      /** Zeitpunkt des Invalidierens.
       */
      revokedAt : INTERLIS.XMLDateTime;
      /** Zeitpunkt, an dem Key invalidiert wird.
       */
      dateOfExpiry : INTERLIS.XMLDateTime;
    END ApiKey;

    CLASS Operat =
      /** Name resp. ID des Operates, z.B. DMAV-2601 für die Gemeinde Solothurn im DMAV.
       */
      name : MANDATORY TEXT*512;
      description : TEXT*1024;
    END Operat;

    CLASS Organisation =
      /** Name der Organisation / Firma.
       */
      name : MANDATORY TEXT*512;
      /** Unternehmens-Identifikationsnummer. Schweizweit eindeutig.
       */
      uid : TEXT*15;
      /** Rolle der Organisation (Admin oder User).
       */
      role : MANDATORY TEXT*20;
      email : MANDATORY TEXT*512;
      /** Name der Organisation ist eindeutig.
       */
      UNIQUE name;
    END Organisation;

    /** TODO
     */
    CLASS Theme =
      /** Name / ID des Themas, z.B. DMAV
       */
      name : MANDATORY TEXT*512;
      /** Name / ID der Config-Datei, die für die Validierung verwendet wird.
       */
      config : TEXT*512;
      /** Name / ID der Meta-Config-Datei, die für die Validierung verwendet wird.
       */
      metaConfig : TEXT*512;
      /** Semicolon-separated names of permitted transfer models. */
      expectedModels : TEXT*2048;
      description : TEXT*1024;
      /** ID des Themas ist eindeutig.
       */
      UNIQUE name;
    END Theme;

    ASSOCIATION Operat_Organisation =
      Organisation_R -- {1} Organisation;
      Operat_R -- {0..*} Operat;
    END Operat_Organisation;

    ASSOCIATION Organisation_ApiKey =
      ApiKey_R -- {0..*} ApiKey;
      Organisation_R -- {1} Organisation;
    END Organisation_ApiKey;

    ASSOCIATION Theme_Operat =
      Operat_R -- {0..*} Operat;
      Theme_R -- {1} Theme;
    END Theme_Operat;

    CONSTRAINTS OF Operat =
      UNIQUE name, Theme_R;
    END;

  END Core;

END SO_AGI_Datahub_Config_20240403.

/** !!------------------------------------------------------------------------------
 *  !! Version    | wer | Änderung
 *  !!------------------------------------------------------------------------------
 *  !! 2024-04-03 | sz  | Ersterfassung
 *  !!==============================================================================
 */
!!@ technicalContact=mailto:agi@bd.so.ch
!!@ furtherInformation=http://geo.so.ch/models/AGI/SO_AGI_Datahub_20240301.uml
MODEL SO_AGI_Datahub_Log_20240403 (de)
AT "https://agi.so.ch"
VERSION "2026-10-08"  =

  TOPIC Deliveries =

    CLASS Delivery =
      jobId : MANDATORY TEXT*36;
      deliveryDate : MANDATORY INTERLIS.XMLDateTime;
      isValid : BOOLEAN;
      isDelivered : BOOLEAN;
      organisation : MANDATORY TEXT*512;
      theme : MANDATORY TEXT*512;
      operat : MANDATORY TEXT*512;
      operatId : 0..9223372036854775807;
      keyId : TEXT*36;
      attemptId : TEXT*36;
      email : TEXT*512;
      validatorConfig : TEXT*512;
      validatorMetaConfig : TEXT*512;
      expectedModels : TEXT*2048;
      executionStatus : TEXT*20;
      validationStatus : TEXT*20;
      publicationStatus : TEXT*20;
      notificationStatus : TEXT*20;
      publicationTries : 0..2147483647;
      publicationNextAt : INTERLIS.XMLDateTime;
      updatedAt : INTERLIS.XMLDateTime;
      finishedAt : INTERLIS.XMLDateTime;
      artifactsExpired : BOOLEAN;
      UNIQUE jobId;
    END Delivery;

    CLASS ValidationAttempt =
      id : MANDATORY TEXT*36;
      dispatchState : MANDATORY TEXT*20;
      state : MANDATORY TEXT*20;
      createdAt : MANDATORY INTERLIS.XMLDateTime;
      startedAt : INTERLIS.XMLDateTime;
      finishedAt : INTERLIS.XMLDateTime;
      heartbeatAt : INTERLIS.XMLDateTime;
      version : MANDATORY 0..9223372036854775807;
      validatorVersion : TEXT*128;
      resultModels : TEXT*4096;
      modelMetadata : MTEXT;
      configurationSnapshot : MTEXT;
      transferVersion : TEXT*10;
      UNIQUE id;
    END ValidationAttempt;
    ASSOCIATION Attempt_Delivery =
      Delivery_R -- {1} Delivery;
      Attempt_R -- {0..*} ValidationAttempt;
    END Attempt_Delivery;
    CLASS DatasetPublication =
      operatId : MANDATORY 0..9223372036854775807;
      UNIQUE operatId;
    END DatasetPublication;
    ASSOCIATION Publication_Delivery =
      Delivery_R -- {1} Delivery;
      Publication_R -- {0..*} DatasetPublication;
    END Publication_Delivery;
    CLASS Notification =
      id : MANDATORY TEXT*36;
      recipient : MANDATORY TEXT*512;
      subject : MANDATORY TEXT*1024;
      body : MANDATORY MTEXT;
      state : MANDATORY TEXT*20;
      tries : MANDATORY 0..2147483647;
      nextAt : MANDATORY INTERLIS.XMLDateTime;
      leaseUntil : INTERLIS.XMLDateTime;
      UNIQUE id;
    END Notification;
    ASSOCIATION Notification_Delivery =
      Delivery_R -- {0..1} Delivery;
      Notification_R -- {0..1} Notification;
    END Notification_Delivery;
  END Deliveries;

END SO_AGI_Datahub_Log_20240403.
','2026-10-08 16:10:55.991');
INSERT INTO ${configSchema}.T_ILI2DB_TABLE_PROP (tablename,tag,setting) VALUES ('core_apikey','ch.ehi.ili2db.tableKind','CLASS');
INSERT INTO ${configSchema}.T_ILI2DB_TABLE_PROP (tablename,tag,setting) VALUES ('core_operat','ch.ehi.ili2db.tableKind','CLASS');
INSERT INTO ${configSchema}.T_ILI2DB_TABLE_PROP (tablename,tag,setting) VALUES ('core_organisation','ch.ehi.ili2db.tableKind','CLASS');
INSERT INTO ${configSchema}.T_ILI2DB_TABLE_PROP (tablename,tag,setting) VALUES ('core_theme','ch.ehi.ili2db.tableKind','CLASS');
DELETE FROM ${logSchema}.T_ILI2DB_CLASSNAME;
DELETE FROM ${logSchema}.T_ILI2DB_ATTRNAME;
DELETE FROM ${logSchema}.T_ILI2DB_COLUMN_PROP;
DELETE FROM ${logSchema}.T_ILI2DB_TABLE_PROP;
DELETE FROM ${logSchema}.T_ILI2DB_META_ATTRS;
DELETE FROM ${logSchema}.T_ILI2DB_MODEL;
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Attempt_Delivery.Delivery_R','delivery_id','validation_attempt','deliveries_delivery');
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.DatasetPublication.operatId','operat_id','dataset_publication',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.artifactsExpired','artifacts_expired','deliveries_delivery',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.attemptId','attempt_id','deliveries_delivery',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.deliveryDate','deliverydate','deliveries_delivery',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.email','email','deliveries_delivery',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.executionStatus','execution_status','deliveries_delivery',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.expectedModels','expected_models','deliveries_delivery',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.finishedAt','finished_at','deliveries_delivery',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.isDelivered','isdelivered','deliveries_delivery',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.isValid','isvalid','deliveries_delivery',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.jobId','jobid','deliveries_delivery',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.keyId','key_id','deliveries_delivery',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.notificationStatus','notification_status','deliveries_delivery',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.operat','operat','deliveries_delivery',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.operatId','operat_id','deliveries_delivery',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.organisation','organisation','deliveries_delivery',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.publicationNextAt','publication_next_at','deliveries_delivery',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.publicationStatus','publication_status','deliveries_delivery',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.publicationTries','publication_tries','deliveries_delivery',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.theme','theme','deliveries_delivery',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.updatedAt','updated_at','deliveries_delivery',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validationStatus','validation_status','deliveries_delivery',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validatorConfig','validator_config','deliveries_delivery',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validatorMetaConfig','validator_meta_config','deliveries_delivery',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.body','body','notification',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.id','id','notification',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.leaseUntil','lease_until','notification',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.nextAt','next_at','notification',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.recipient','recipient','notification',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.state','state','notification',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.subject','subject','notification',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.tries','tries','notification',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification_Delivery.Delivery_R','delivery_id','notification','deliveries_delivery');
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Publication_Delivery.Delivery_R','delivery_id','dataset_publication','deliveries_delivery');
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.configurationSnapshot','configuration_snapshot','validation_attempt',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.createdAt','created_at','validation_attempt',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.dispatchState','dispatch_state','validation_attempt',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.finishedAt','finished_at','validation_attempt',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.heartbeatAt','heartbeat_at','validation_attempt',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.id','id','validation_attempt',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.modelMetadata','model_metadata','validation_attempt',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.resultModels','result_models','validation_attempt',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.startedAt','started_at','validation_attempt',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.state','state','validation_attempt',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.transferVersion','transfer_version','validation_attempt',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.validatorVersion','validator_version','validation_attempt',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.version','version','validation_attempt',NULL);
INSERT INTO ${logSchema}.T_ILI2DB_CLASSNAME (IliName,SqlName) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Attempt_Delivery','deliveries_attempt_delivery');
INSERT INTO ${logSchema}.T_ILI2DB_CLASSNAME (IliName,SqlName) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.DatasetPublication','dataset_publication');
INSERT INTO ${logSchema}.T_ILI2DB_CLASSNAME (IliName,SqlName) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery','deliveries_delivery');
INSERT INTO ${logSchema}.T_ILI2DB_CLASSNAME (IliName,SqlName) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification','notification');
INSERT INTO ${logSchema}.T_ILI2DB_CLASSNAME (IliName,SqlName) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification_Delivery','deliveries_notification_delivery');
INSERT INTO ${logSchema}.T_ILI2DB_CLASSNAME (IliName,SqlName) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Publication_Delivery','deliveries_publication_delivery');
INSERT INTO ${logSchema}.T_ILI2DB_CLASSNAME (IliName,SqlName) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt','validation_attempt');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('dataset_publication',NULL,'delivery_id','ch.ehi.ili2db.foreignKey','deliveries_delivery');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('dataset_publication',NULL,'operat_id','ch.ehi.ili2db.typeKind','NUMERIC');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery','deliveries_delivery','artifacts_expired','ch.ehi.ili2db.enumDomain','INTERLIS.BOOLEAN');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery','deliveries_delivery','isdelivered','ch.ehi.ili2db.enumDomain','INTERLIS.BOOLEAN');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery','deliveries_delivery','isvalid','ch.ehi.ili2db.enumDomain','INTERLIS.BOOLEAN');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery',NULL,'artifacts_expired','ch.ehi.ili2db.typeKind','BOOLEAN');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery',NULL,'attempt_id','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery',NULL,'deliverydate','ch.ehi.ili2db.typeKind','DATETIME');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery',NULL,'email','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery',NULL,'execution_status','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery',NULL,'expected_models','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery',NULL,'finished_at','ch.ehi.ili2db.typeKind','DATETIME');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery',NULL,'isdelivered','ch.ehi.ili2db.typeKind','BOOLEAN');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery',NULL,'isvalid','ch.ehi.ili2db.typeKind','BOOLEAN');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery',NULL,'jobid','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery',NULL,'key_id','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery',NULL,'notification_status','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery',NULL,'operat','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery',NULL,'operat_id','ch.ehi.ili2db.typeKind','NUMERIC');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery',NULL,'organisation','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery',NULL,'publication_next_at','ch.ehi.ili2db.typeKind','DATETIME');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery',NULL,'publication_status','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery',NULL,'publication_tries','ch.ehi.ili2db.typeKind','NUMERIC');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery',NULL,'theme','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery',NULL,'updated_at','ch.ehi.ili2db.typeKind','DATETIME');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery',NULL,'validation_status','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery',NULL,'validator_config','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery',NULL,'validator_meta_config','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('notification',NULL,'body','ch.ehi.ili2db.textKind','MTEXT');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('notification',NULL,'body','ch.ehi.ili2db.typeKind','MTEXT');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('notification',NULL,'delivery_id','ch.ehi.ili2db.foreignKey','deliveries_delivery');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('notification',NULL,'id','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('notification',NULL,'lease_until','ch.ehi.ili2db.typeKind','DATETIME');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('notification',NULL,'next_at','ch.ehi.ili2db.typeKind','DATETIME');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('notification',NULL,'recipient','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('notification',NULL,'state','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('notification',NULL,'subject','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('notification',NULL,'tries','ch.ehi.ili2db.typeKind','NUMERIC');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('validation_attempt',NULL,'configuration_snapshot','ch.ehi.ili2db.textKind','MTEXT');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('validation_attempt',NULL,'configuration_snapshot','ch.ehi.ili2db.typeKind','MTEXT');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('validation_attempt',NULL,'created_at','ch.ehi.ili2db.typeKind','DATETIME');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('validation_attempt',NULL,'delivery_id','ch.ehi.ili2db.foreignKey','deliveries_delivery');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('validation_attempt',NULL,'dispatch_state','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('validation_attempt',NULL,'finished_at','ch.ehi.ili2db.typeKind','DATETIME');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('validation_attempt',NULL,'heartbeat_at','ch.ehi.ili2db.typeKind','DATETIME');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('validation_attempt',NULL,'id','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('validation_attempt',NULL,'model_metadata','ch.ehi.ili2db.textKind','MTEXT');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('validation_attempt',NULL,'model_metadata','ch.ehi.ili2db.typeKind','MTEXT');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('validation_attempt',NULL,'result_models','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('validation_attempt',NULL,'started_at','ch.ehi.ili2db.typeKind','DATETIME');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('validation_attempt',NULL,'state','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('validation_attempt',NULL,'transfer_version','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('validation_attempt',NULL,'validator_version','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO ${logSchema}.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('validation_attempt',NULL,'version','ch.ehi.ili2db.typeKind','NUMERIC');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403','furtherInformation','http://geo.so.ch/models/AGI/SO_AGI_Datahub_20240301.uml');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403','technicalContact','mailto:agi@bd.so.ch');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core','ili2db.ili.topicClasses','core_apikey core_operat core_organisation core_theme');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.apiKey','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.apiKey','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.createdAt','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.createdAt','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.dateOfExpiry','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.dateOfExpiry','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.issuanceState','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.issuanceState','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.revokedAt','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.revokedAt','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat.description','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat.description','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat.name','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat.name','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Operat_R','ili2db.ili.assocCardinalityMax','*');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Operat_R','ili2db.ili.assocCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Operat_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Organisation_R','ili2db.ili.assocCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Organisation_R','ili2db.ili.assocCardinalityMin','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Organisation_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.email','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.email','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.name','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.name','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.role','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.role','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.uid','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.uid','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.ApiKey_R','ili2db.ili.assocCardinalityMax','*');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.ApiKey_R','ili2db.ili.assocCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.ApiKey_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.Organisation_R','ili2db.ili.assocCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.Organisation_R','ili2db.ili.assocCardinalityMin','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.Organisation_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.config','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.config','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.description','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.description','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.expectedModels','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.expectedModels','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.metaConfig','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.metaConfig','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.name','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.name','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Operat_R','ili2db.ili.assocCardinalityMax','*');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Operat_R','ili2db.ili.assocCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Operat_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Theme_R','ili2db.ili.assocCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Theme_R','ili2db.ili.assocCardinalityMin','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Theme_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403','furtherInformation','http://geo.so.ch/models/AGI/SO_AGI_Datahub_20240301.uml');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403','technicalContact','mailto:agi@bd.so.ch');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries','ili2db.ili.topicClasses','deliveries_datasetpublication deliveries_delivery deliveries_notification deliveries_validationattempt');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Attempt_Delivery.Attempt_R','ili2db.ili.assocCardinalityMax','*');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Attempt_Delivery.Attempt_R','ili2db.ili.assocCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Attempt_Delivery.Attempt_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Attempt_Delivery.Delivery_R','ili2db.ili.assocCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Attempt_Delivery.Delivery_R','ili2db.ili.assocCardinalityMin','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Attempt_Delivery.Delivery_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.DatasetPublication.operatId','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.DatasetPublication.operatId','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.artifactsExpired','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.artifactsExpired','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.attemptId','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.attemptId','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.deliveryDate','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.deliveryDate','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.email','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.email','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.executionStatus','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.executionStatus','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.expectedModels','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.expectedModels','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.finishedAt','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.finishedAt','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.isDelivered','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.isDelivered','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.isValid','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.isValid','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.jobId','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.jobId','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.keyId','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.keyId','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.notificationStatus','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.notificationStatus','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.operat','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.operat','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.operatId','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.operatId','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.organisation','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.organisation','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.publicationNextAt','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.publicationNextAt','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.publicationStatus','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.publicationStatus','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.publicationTries','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.publicationTries','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.theme','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.theme','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.updatedAt','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.updatedAt','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validationStatus','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validationStatus','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validatorConfig','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validatorConfig','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validatorMetaConfig','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validatorMetaConfig','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.body','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.body','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.id','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.id','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.leaseUntil','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.leaseUntil','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.nextAt','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.nextAt','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.recipient','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.recipient','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.state','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.state','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.subject','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.subject','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.tries','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification.tries','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification_Delivery.Delivery_R','ili2db.ili.assocCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification_Delivery.Delivery_R','ili2db.ili.assocCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification_Delivery.Delivery_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification_Delivery.Notification_R','ili2db.ili.assocCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification_Delivery.Notification_R','ili2db.ili.assocCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Notification_Delivery.Notification_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Publication_Delivery.Delivery_R','ili2db.ili.assocCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Publication_Delivery.Delivery_R','ili2db.ili.assocCardinalityMin','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Publication_Delivery.Delivery_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Publication_Delivery.Publication_R','ili2db.ili.assocCardinalityMax','*');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Publication_Delivery.Publication_R','ili2db.ili.assocCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Publication_Delivery.Publication_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.configurationSnapshot','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.configurationSnapshot','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.createdAt','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.createdAt','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.dispatchState','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.dispatchState','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.finishedAt','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.finishedAt','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.heartbeatAt','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.heartbeatAt','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.id','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.id','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.modelMetadata','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.modelMetadata','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.resultModels','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.resultModels','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.startedAt','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.startedAt','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.state','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.state','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.transferVersion','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.transferVersion','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.validatorVersion','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.validatorVersion','ili2db.ili.attrCardinalityMin','0');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.version','ili2db.ili.attrCardinalityMax','1');
INSERT INTO ${logSchema}.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt.version','ili2db.ili.attrCardinalityMin','1');
INSERT INTO ${logSchema}.T_ILI2DB_MODEL (filename,iliversion,modelName,content,importDate) VALUES ('SO_AGI_Datahub_20240403.ili','2.3','SO_AGI_Datahub_Config_20240403 SO_AGI_Datahub_Log_20240403','INTERLIS 2.3;

/** !!------------------------------------------------------------------------------
 *  !! Version    | wer | Änderung
 *  !!------------------------------------------------------------------------------
 *  !! 2024-02-12 | sz  | Ersterfassung
 *  !! 2024-03-01 | sz  | API-Key-Variante
 *  !! 2024-04-03 | sz  | Trennung Konfig und Log
 *  !!==============================================================================
 */
!!@ technicalContact=mailto:agi@bd.so.ch
!!@ furtherInformation=http://geo.so.ch/models/AGI/SO_AGI_Datahub_20240301.uml
MODEL SO_AGI_Datahub_Config_20240403 (de)
AT "https://agi.so.ch"
VERSION "2026-10-08"  =

  TOPIC Core =

    CLASS ApiKey =
      /** API-Key
       */
      apiKey : MANDATORY TEXT*256;
      issuanceState : TEXT*20;
      /** Zeitpunkt der Erstellung.
       */
      createdAt : MANDATORY INTERLIS.XMLDateTime;
      /** Zeitpunkt des Invalidierens.
       */
      revokedAt : INTERLIS.XMLDateTime;
      /** Zeitpunkt, an dem Key invalidiert wird.
       */
      dateOfExpiry : INTERLIS.XMLDateTime;
    END ApiKey;

    CLASS Operat =
      /** Name resp. ID des Operates, z.B. DMAV-2601 für die Gemeinde Solothurn im DMAV.
       */
      name : MANDATORY TEXT*512;
      description : TEXT*1024;
    END Operat;

    CLASS Organisation =
      /** Name der Organisation / Firma.
       */
      name : MANDATORY TEXT*512;
      /** Unternehmens-Identifikationsnummer. Schweizweit eindeutig.
       */
      uid : TEXT*15;
      /** Rolle der Organisation (Admin oder User).
       */
      role : MANDATORY TEXT*20;
      email : MANDATORY TEXT*512;
      /** Name der Organisation ist eindeutig.
       */
      UNIQUE name;
    END Organisation;

    /** TODO
     */
    CLASS Theme =
      /** Name / ID des Themas, z.B. DMAV
       */
      name : MANDATORY TEXT*512;
      /** Name / ID der Config-Datei, die für die Validierung verwendet wird.
       */
      config : TEXT*512;
      /** Name / ID der Meta-Config-Datei, die für die Validierung verwendet wird.
       */
      metaConfig : TEXT*512;
      /** Semicolon-separated names of permitted transfer models. */
      expectedModels : TEXT*2048;
      description : TEXT*1024;
      /** ID des Themas ist eindeutig.
       */
      UNIQUE name;
    END Theme;

    ASSOCIATION Operat_Organisation =
      Organisation_R -- {1} Organisation;
      Operat_R -- {0..*} Operat;
    END Operat_Organisation;

    ASSOCIATION Organisation_ApiKey =
      ApiKey_R -- {0..*} ApiKey;
      Organisation_R -- {1} Organisation;
    END Organisation_ApiKey;

    ASSOCIATION Theme_Operat =
      Operat_R -- {0..*} Operat;
      Theme_R -- {1} Theme;
    END Theme_Operat;

    CONSTRAINTS OF Operat =
      UNIQUE name, Theme_R;
    END;

  END Core;

END SO_AGI_Datahub_Config_20240403.

/** !!------------------------------------------------------------------------------
 *  !! Version    | wer | Änderung
 *  !!------------------------------------------------------------------------------
 *  !! 2024-04-03 | sz  | Ersterfassung
 *  !!==============================================================================
 */
!!@ technicalContact=mailto:agi@bd.so.ch
!!@ furtherInformation=http://geo.so.ch/models/AGI/SO_AGI_Datahub_20240301.uml
MODEL SO_AGI_Datahub_Log_20240403 (de)
AT "https://agi.so.ch"
VERSION "2026-10-08"  =

  TOPIC Deliveries =

    CLASS Delivery =
      jobId : MANDATORY TEXT*36;
      deliveryDate : MANDATORY INTERLIS.XMLDateTime;
      isValid : BOOLEAN;
      isDelivered : BOOLEAN;
      organisation : MANDATORY TEXT*512;
      theme : MANDATORY TEXT*512;
      operat : MANDATORY TEXT*512;
      operatId : 0..9223372036854775807;
      keyId : TEXT*36;
      attemptId : TEXT*36;
      email : TEXT*512;
      validatorConfig : TEXT*512;
      validatorMetaConfig : TEXT*512;
      expectedModels : TEXT*2048;
      executionStatus : TEXT*20;
      validationStatus : TEXT*20;
      publicationStatus : TEXT*20;
      notificationStatus : TEXT*20;
      publicationTries : 0..2147483647;
      publicationNextAt : INTERLIS.XMLDateTime;
      updatedAt : INTERLIS.XMLDateTime;
      finishedAt : INTERLIS.XMLDateTime;
      artifactsExpired : BOOLEAN;
      UNIQUE jobId;
    END Delivery;

    CLASS ValidationAttempt =
      id : MANDATORY TEXT*36;
      dispatchState : MANDATORY TEXT*20;
      state : MANDATORY TEXT*20;
      createdAt : MANDATORY INTERLIS.XMLDateTime;
      startedAt : INTERLIS.XMLDateTime;
      finishedAt : INTERLIS.XMLDateTime;
      heartbeatAt : INTERLIS.XMLDateTime;
      version : MANDATORY 0..9223372036854775807;
      validatorVersion : TEXT*128;
      resultModels : TEXT*4096;
      modelMetadata : MTEXT;
      configurationSnapshot : MTEXT;
      transferVersion : TEXT*10;
      UNIQUE id;
    END ValidationAttempt;
    ASSOCIATION Attempt_Delivery =
      Delivery_R -- {1} Delivery;
      Attempt_R -- {0..*} ValidationAttempt;
    END Attempt_Delivery;
    CLASS DatasetPublication =
      operatId : MANDATORY 0..9223372036854775807;
      UNIQUE operatId;
    END DatasetPublication;
    ASSOCIATION Publication_Delivery =
      Delivery_R -- {1} Delivery;
      Publication_R -- {0..*} DatasetPublication;
    END Publication_Delivery;
    CLASS Notification =
      id : MANDATORY TEXT*36;
      recipient : MANDATORY TEXT*512;
      subject : MANDATORY TEXT*1024;
      body : MANDATORY MTEXT;
      state : MANDATORY TEXT*20;
      tries : MANDATORY 0..2147483647;
      nextAt : MANDATORY INTERLIS.XMLDateTime;
      leaseUntil : INTERLIS.XMLDateTime;
      UNIQUE id;
    END Notification;
    ASSOCIATION Notification_Delivery =
      Delivery_R -- {0..1} Delivery;
      Notification_R -- {0..1} Notification;
    END Notification_Delivery;
  END Deliveries;

END SO_AGI_Datahub_Log_20240403.
','2026-10-08 16:10:56.03');
INSERT INTO ${logSchema}.T_ILI2DB_TABLE_PROP (tablename,tag,setting) VALUES ('dataset_publication','ch.ehi.ili2db.tableKind','CLASS');
INSERT INTO ${logSchema}.T_ILI2DB_TABLE_PROP (tablename,tag,setting) VALUES ('deliveries_delivery','ch.ehi.ili2db.tableKind','CLASS');
INSERT INTO ${logSchema}.T_ILI2DB_TABLE_PROP (tablename,tag,setting) VALUES ('notification','ch.ehi.ili2db.tableKind','CLASS');
INSERT INTO ${logSchema}.T_ILI2DB_TABLE_PROP (tablename,tag,setting) VALUES ('validation_attempt','ch.ehi.ili2db.tableKind','CLASS');
