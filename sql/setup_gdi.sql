CREATE SCHEMA IF NOT EXISTS agi_datahub_config_v1;
CREATE SEQUENCE agi_datahub_config_v1.t_ili2db_seq;;
-- SO_AGI_Datahub_Config_20240403.Core.ApiKey
CREATE TABLE agi_datahub_config_v1.core_apikey (
  T_Id bigint PRIMARY KEY DEFAULT nextval('agi_datahub_config_v1.t_ili2db_seq')
  ,apikey varchar(256) NOT NULL
  ,createdat timestamp NOT NULL
  ,revokedat timestamp NULL
  ,dateofexpiry timestamp NULL
  ,organisation_r bigint NOT NULL
)
;
CREATE INDEX core_apikey_organisation_r_idx ON agi_datahub_config_v1.core_apikey ( organisation_r );
COMMENT ON COLUMN agi_datahub_config_v1.core_apikey.apikey IS 'API-Key';
COMMENT ON COLUMN agi_datahub_config_v1.core_apikey.createdat IS 'Zeitpunkt der Erstellung.';
COMMENT ON COLUMN agi_datahub_config_v1.core_apikey.revokedat IS 'Zeitpunkt des Invalidierens.';
COMMENT ON COLUMN agi_datahub_config_v1.core_apikey.dateofexpiry IS 'Zeitpunkt, an dem Key invalidiert wird.';
-- SO_AGI_Datahub_Config_20240403.Core.Operat
CREATE TABLE agi_datahub_config_v1.core_operat (
  T_Id bigint PRIMARY KEY DEFAULT nextval('agi_datahub_config_v1.t_ili2db_seq')
  ,aname varchar(512) NOT NULL
  ,adescription varchar(1024) NULL
  ,organisation_r bigint NOT NULL
  ,theme_r bigint NOT NULL
)
;
CREATE INDEX core_operat_organisation_r_idx ON agi_datahub_config_v1.core_operat ( organisation_r );
CREATE INDEX core_operat_theme_r_idx ON agi_datahub_config_v1.core_operat ( theme_r );
COMMENT ON COLUMN agi_datahub_config_v1.core_operat.aname IS 'Name resp. ID des Operates, z.B. DMAV-2601 für die Gemeinde Solothurn im DMAV.';
-- SO_AGI_Datahub_Config_20240403.Core.Organisation
CREATE TABLE agi_datahub_config_v1.core_organisation (
  T_Id bigint PRIMARY KEY DEFAULT nextval('agi_datahub_config_v1.t_ili2db_seq')
  ,aname varchar(512) NOT NULL
  ,auid varchar(15) NULL
  ,arole varchar(20) NOT NULL
  ,email varchar(512) NOT NULL
)
;
COMMENT ON COLUMN agi_datahub_config_v1.core_organisation.aname IS 'Name der Organisation / Firma.';
COMMENT ON COLUMN agi_datahub_config_v1.core_organisation.auid IS 'Unternehmens-Identifikationsnummer. Schweizweit eindeutig.';
COMMENT ON COLUMN agi_datahub_config_v1.core_organisation.arole IS 'Rolle der Organisation (Admin oder User).';
-- SO_AGI_Datahub_Config_20240403.Core.Theme
CREATE TABLE agi_datahub_config_v1.core_theme (
  T_Id bigint PRIMARY KEY DEFAULT nextval('agi_datahub_config_v1.t_ili2db_seq')
  ,aname varchar(512) NOT NULL
  ,config varchar(512) NULL
  ,metaconfig varchar(512) NULL
  ,adescription varchar(1024) NULL
)
;
COMMENT ON TABLE agi_datahub_config_v1.core_theme IS 'TODO';
COMMENT ON COLUMN agi_datahub_config_v1.core_theme.aname IS 'Name / ID des Themas, z.B. DMAV';
COMMENT ON COLUMN agi_datahub_config_v1.core_theme.config IS 'Name / ID der Config-Datei, die für die Validierung verwendet wird.';
COMMENT ON COLUMN agi_datahub_config_v1.core_theme.metaconfig IS 'Name / ID der Meta-Config-Datei, die für die Validierung verwendet wird.';
CREATE TABLE agi_datahub_config_v1.T_ILI2DB_BASKET (
  T_Id bigint PRIMARY KEY
  ,dataset bigint NULL
  ,topic varchar(200) NOT NULL
  ,T_Ili_Tid varchar(200) NULL
  ,attachmentKey varchar(200) NOT NULL
  ,domains varchar(1024) NULL
)
;
CREATE INDEX T_ILI2DB_BASKET_dataset_idx ON agi_datahub_config_v1.t_ili2db_basket ( dataset );
CREATE TABLE agi_datahub_config_v1.T_ILI2DB_DATASET (
  T_Id bigint PRIMARY KEY
  ,datasetName varchar(200) NULL
)
;
CREATE TABLE agi_datahub_config_v1.T_ILI2DB_IMPORT (
  T_Id bigint PRIMARY KEY
  ,dataset bigint NOT NULL
  ,importDate timestamp NOT NULL
  ,importUser varchar(40) NOT NULL
  ,importFile varchar(200) NULL
)
;
CREATE INDEX T_ILI2DB_IMPORT_dataset_idx ON agi_datahub_config_v1.t_ili2db_import ( dataset );
CREATE TABLE agi_datahub_config_v1.T_ILI2DB_IMPORT_BASKET (
  T_Id bigint PRIMARY KEY
  ,importrun bigint NOT NULL
  ,basket bigint NOT NULL
  ,objectCount integer NULL
)
;
CREATE INDEX T_ILI2DB_IMPORT_BASKET_importrun_idx ON agi_datahub_config_v1.t_ili2db_import_basket ( importrun );
CREATE INDEX T_ILI2DB_IMPORT_BASKET_basket_idx ON agi_datahub_config_v1.t_ili2db_import_basket ( basket );
CREATE TABLE agi_datahub_config_v1.T_ILI2DB_INHERITANCE (
  thisClass varchar(1024) PRIMARY KEY
  ,baseClass varchar(1024) NULL
)
;
CREATE TABLE agi_datahub_config_v1.T_ILI2DB_SETTINGS (
  tag varchar(60) PRIMARY KEY
  ,setting varchar(8000) NULL
)
;
CREATE TABLE agi_datahub_config_v1.T_ILI2DB_TRAFO (
  iliname varchar(1024) NOT NULL
  ,tag varchar(1024) NOT NULL
  ,setting varchar(1024) NOT NULL
)
;
CREATE TABLE agi_datahub_config_v1.T_ILI2DB_MODEL (
  filename varchar(250) NOT NULL
  ,iliversion varchar(3) NOT NULL
  ,modelName text NOT NULL
  ,content text NOT NULL
  ,importDate timestamp NOT NULL
  ,PRIMARY KEY (iliversion,modelName)
)
;
CREATE TABLE agi_datahub_config_v1.T_ILI2DB_CLASSNAME (
  IliName varchar(1024) PRIMARY KEY
  ,SqlName varchar(1024) NOT NULL
)
;
CREATE TABLE agi_datahub_config_v1.T_ILI2DB_ATTRNAME (
  IliName varchar(1024) NOT NULL
  ,SqlName varchar(1024) NOT NULL
  ,ColOwner varchar(1024) NOT NULL
  ,Target varchar(1024) NULL
  ,PRIMARY KEY (ColOwner,SqlName)
)
;
CREATE TABLE agi_datahub_config_v1.T_ILI2DB_COLUMN_PROP (
  tablename varchar(255) NOT NULL
  ,subtype varchar(255) NULL
  ,columnname varchar(255) NOT NULL
  ,tag varchar(1024) NOT NULL
  ,setting varchar(8000) NOT NULL
)
;
CREATE TABLE agi_datahub_config_v1.T_ILI2DB_TABLE_PROP (
  tablename varchar(255) NOT NULL
  ,tag varchar(1024) NOT NULL
  ,setting varchar(8000) NOT NULL
)
;
CREATE TABLE agi_datahub_config_v1.T_ILI2DB_META_ATTRS (
  ilielement varchar(255) NOT NULL
  ,attr_name varchar(1024) NOT NULL
  ,attr_value varchar(8000) NOT NULL
)
;
CREATE UNIQUE INDEX T_ILI2DB_DATASET_datasetName_key ON agi_datahub_config_v1.T_ILI2DB_DATASET (datasetName)
;
CREATE UNIQUE INDEX T_ILI2DB_MODEL_iliversion_modelName_key ON agi_datahub_config_v1.T_ILI2DB_MODEL (iliversion,modelName)
;
CREATE UNIQUE INDEX T_ILI2DB_ATTRNAME_ColOwner_SqlName_key ON agi_datahub_config_v1.T_ILI2DB_ATTRNAME (ColOwner,SqlName)
;
INSERT INTO agi_datahub_config_v1.T_ILI2DB_CLASSNAME (IliName,SqlName) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme','core_theme');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_CLASSNAME (IliName,SqlName) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey','core_apikey');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_CLASSNAME (IliName,SqlName) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat','core_operat');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_CLASSNAME (IliName,SqlName) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey','core_organisation_apikey');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_CLASSNAME (IliName,SqlName) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation','core_organisation');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_CLASSNAME (IliName,SqlName) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation','core_operat_organisation');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_CLASSNAME (IliName,SqlName) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme_Operat','core_theme_operat');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Organisation_R','organisation_r','core_operat','core_organisation');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.description','adescription','core_theme',NULL);
INSERT INTO agi_datahub_config_v1.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.email','email','core_organisation',NULL);
INSERT INTO agi_datahub_config_v1.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.uid','auid','core_organisation',NULL);
INSERT INTO agi_datahub_config_v1.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.revokedAt','revokedat','core_apikey',NULL);
INSERT INTO agi_datahub_config_v1.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat.name','aname','core_operat',NULL);
INSERT INTO agi_datahub_config_v1.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Theme_R','theme_r','core_operat','core_theme');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.dateOfExpiry','dateofexpiry','core_apikey',NULL);
INSERT INTO agi_datahub_config_v1.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.createdAt','createdat','core_apikey',NULL);
INSERT INTO agi_datahub_config_v1.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.metaConfig','metaconfig','core_theme',NULL);
INSERT INTO agi_datahub_config_v1.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat.description','adescription','core_operat',NULL);
INSERT INTO agi_datahub_config_v1.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.config','config','core_theme',NULL);
INSERT INTO agi_datahub_config_v1.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.name','aname','core_theme',NULL);
INSERT INTO agi_datahub_config_v1.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.name','aname','core_organisation',NULL);
INSERT INTO agi_datahub_config_v1.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.apiKey','apikey','core_apikey',NULL);
INSERT INTO agi_datahub_config_v1.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.Organisation_R','organisation_r','core_apikey','core_organisation');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.role','arole','core_organisation',NULL);
INSERT INTO agi_datahub_config_v1.T_ILI2DB_TRAFO (iliname,tag,setting) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme','ch.ehi.ili2db.inheritance','newClass');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_TRAFO (iliname,tag,setting) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey','ch.ehi.ili2db.inheritance','newClass');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_TRAFO (iliname,tag,setting) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat','ch.ehi.ili2db.inheritance','newClass');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_TRAFO (iliname,tag,setting) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey','ch.ehi.ili2db.inheritance','embedded');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_TRAFO (iliname,tag,setting) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation','ch.ehi.ili2db.inheritance','newClass');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_TRAFO (iliname,tag,setting) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation','ch.ehi.ili2db.inheritance','embedded');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_TRAFO (iliname,tag,setting) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme_Operat','ch.ehi.ili2db.inheritance','embedded');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_INHERITANCE (thisClass,baseClass) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation',NULL);
INSERT INTO agi_datahub_config_v1.T_ILI2DB_INHERITANCE (thisClass,baseClass) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation',NULL);
INSERT INTO agi_datahub_config_v1.T_ILI2DB_INHERITANCE (thisClass,baseClass) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey',NULL);
INSERT INTO agi_datahub_config_v1.T_ILI2DB_INHERITANCE (thisClass,baseClass) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme_Operat',NULL);
INSERT INTO agi_datahub_config_v1.T_ILI2DB_INHERITANCE (thisClass,baseClass) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat',NULL);
INSERT INTO agi_datahub_config_v1.T_ILI2DB_INHERITANCE (thisClass,baseClass) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme',NULL);
INSERT INTO agi_datahub_config_v1.T_ILI2DB_INHERITANCE (thisClass,baseClass) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey',NULL);
INSERT INTO agi_datahub_config_v1.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_operat',NULL,'adescription','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_operat',NULL,'organisation_r','ch.ehi.ili2db.foreignKey','core_organisation');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_theme',NULL,'config','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_apikey',NULL,'createdat','ch.ehi.ili2db.typeKind','DATETIME');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_organisation',NULL,'email','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_operat',NULL,'aname','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_operat',NULL,'theme_r','ch.ehi.ili2db.foreignKey','core_theme');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_organisation',NULL,'auid','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_theme',NULL,'aname','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_theme',NULL,'adescription','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_organisation',NULL,'aname','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_theme',NULL,'metaconfig','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_apikey',NULL,'revokedat','ch.ehi.ili2db.typeKind','DATETIME');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_apikey',NULL,'organisation_r','ch.ehi.ili2db.foreignKey','core_organisation');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_apikey',NULL,'dateofexpiry','ch.ehi.ili2db.typeKind','DATETIME');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_organisation',NULL,'arole','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('core_apikey',NULL,'apikey','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_TABLE_PROP (tablename,tag,setting) VALUES ('core_organisation','ch.ehi.ili2db.tableKind','CLASS');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_TABLE_PROP (tablename,tag,setting) VALUES ('core_theme','ch.ehi.ili2db.tableKind','CLASS');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_TABLE_PROP (tablename,tag,setting) VALUES ('core_operat','ch.ehi.ili2db.tableKind','CLASS');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_TABLE_PROP (tablename,tag,setting) VALUES ('core_apikey','ch.ehi.ili2db.tableKind','CLASS');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_MODEL (filename,iliversion,modelName,content,importDate) VALUES ('SO_AGI_Datahub_20240403.ili','2.3','SO_AGI_Datahub_Config_20240403 SO_AGI_Datahub_Log_20240403','INTERLIS 2.3;

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
VERSION "2024-03-01"  =

  TOPIC Core =

    CLASS ApiKey =
      /** API-Key
       */
      apiKey : MANDATORY TEXT*256;
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
VERSION "2024-04-03"  =

  TOPIC Deliveries =

    CLASS Delivery =
      jobId : MANDATORY TEXT*36;
      deliveryDate : MANDATORY INTERLIS.XMLDateTime;
      isValid : BOOLEAN;
      isDelivered : BOOLEAN;
      organisation : MANDATORY TEXT*512;
      theme : MANDATORY TEXT*512;
      operat : MANDATORY TEXT*512;
      UNIQUE jobId;
    END Delivery;

  END Deliveries;

END SO_AGI_Datahub_Log_20240403.
','2024-08-29 08:02:02.943');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.createMetaInfo','True');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.interlis.ili2c.ilidirs','https://geo.so.ch/models;http://models.geo.admin.ch');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.arrayTrafo','coalesce');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.createForeignKeyIndex','yes');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.importTabs','simple');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.nameOptimization','topic');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.localisedTrafo','expand');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.jsonTrafo','coalesce');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.sender','ili2pg-4.9.1-edeb4647bbdea52480a419b5aa605d425fcbd5df');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.sqlgen.createGeomIndex','True');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.defaultSrsAuthority','EPSG');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.defaultSrsCode','2056');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.uniqueConstraints','create');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.maxSqlNameLength','60');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.uuidDefaultValue','uuid_generate_v4()');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.inheritanceTrafo','smart1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.catalogueRefTrafo','coalesce');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.multiPointTrafo','coalesce');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.StrokeArcs','enable');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.multiLineTrafo','coalesce');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.multiSurfaceTrafo','coalesce');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.multilingualTrafo','expand');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.config','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.config','ili2db.ili.attrCardinalityMin','0');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.theme','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.theme','ili2db.ili.attrCardinalityMin','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat.description','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat.description','ili2db.ili.attrCardinalityMin','0');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.description','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.description','ili2db.ili.attrCardinalityMin','0');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.Organisation_R','ili2db.ili.assocCardinalityMin','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.Organisation_R','ili2db.ili.assocCardinalityMax','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.Organisation_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.ApiKey_R','ili2db.ili.assocCardinalityMin','0');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.ApiKey_R','ili2db.ili.assocCardinalityMax','*');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.ApiKey_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.jobId','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.jobId','ili2db.ili.attrCardinalityMin','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.deliveryDate','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.deliveryDate','ili2db.ili.attrCardinalityMin','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Theme_R','ili2db.ili.assocCardinalityMin','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Theme_R','ili2db.ili.assocCardinalityMax','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Theme_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.name','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.name','ili2db.ili.attrCardinalityMin','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.revokedAt','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.revokedAt','ili2db.ili.attrCardinalityMin','0');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core','ili2db.ili.topicClasses','core_apikey core_operat core_organisation core_theme');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Organisation_R','ili2db.ili.assocCardinalityMin','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Organisation_R','ili2db.ili.assocCardinalityMax','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Organisation_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403','furtherInformation','http://geo.so.ch/models/AGI/SO_AGI_Datahub_20240301.uml');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403','technicalContact','mailto:agi@bd.so.ch');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries','ili2db.ili.topicClasses','deliveries_delivery');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Operat_R','ili2db.ili.assocCardinalityMin','0');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Operat_R','ili2db.ili.assocCardinalityMax','*');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Operat_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.isValid','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.isValid','ili2db.ili.attrCardinalityMin','0');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.createdAt','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.createdAt','ili2db.ili.attrCardinalityMin','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat.name','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat.name','ili2db.ili.attrCardinalityMin','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.metaConfig','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.metaConfig','ili2db.ili.attrCardinalityMin','0');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Operat_R','ili2db.ili.assocCardinalityMin','0');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Operat_R','ili2db.ili.assocCardinalityMax','*');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Operat_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.isDelivered','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.isDelivered','ili2db.ili.attrCardinalityMin','0');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.dateOfExpiry','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.dateOfExpiry','ili2db.ili.attrCardinalityMin','0');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.uid','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.uid','ili2db.ili.attrCardinalityMin','0');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.name','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.name','ili2db.ili.attrCardinalityMin','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.email','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.email','ili2db.ili.attrCardinalityMin','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.apiKey','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.apiKey','ili2db.ili.attrCardinalityMin','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403','furtherInformation','http://geo.so.ch/models/AGI/SO_AGI_Datahub_20240301.uml');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403','technicalContact','mailto:agi@bd.so.ch');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.role','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.role','ili2db.ili.attrCardinalityMin','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.organisation','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.organisation','ili2db.ili.attrCardinalityMin','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.operat','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_config_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.operat','ili2db.ili.attrCardinalityMin','1');
CREATE SCHEMA IF NOT EXISTS agi_datahub_log_v1;
CREATE SEQUENCE agi_datahub_log_v1.t_ili2db_seq;;
-- SO_AGI_Datahub_Log_20240403.Deliveries.Delivery
CREATE TABLE agi_datahub_log_v1.deliveries_delivery (
  T_Id bigint PRIMARY KEY DEFAULT nextval('agi_datahub_log_v1.t_ili2db_seq')
  ,jobid varchar(36) NOT NULL
  ,deliverydate timestamp NOT NULL
  ,isvalid boolean NULL
  ,isdelivered boolean NULL
  ,organisation varchar(512) NOT NULL
  ,theme varchar(512) NOT NULL
  ,operat varchar(512) NOT NULL
)
;
CREATE TABLE agi_datahub_log_v1.T_ILI2DB_BASKET (
  T_Id bigint PRIMARY KEY
  ,dataset bigint NULL
  ,topic varchar(200) NOT NULL
  ,T_Ili_Tid varchar(200) NULL
  ,attachmentKey varchar(200) NOT NULL
  ,domains varchar(1024) NULL
)
;
CREATE INDEX T_ILI2DB_BASKET_dataset_idx ON agi_datahub_log_v1.t_ili2db_basket ( dataset );
CREATE TABLE agi_datahub_log_v1.T_ILI2DB_DATASET (
  T_Id bigint PRIMARY KEY
  ,datasetName varchar(200) NULL
)
;
CREATE TABLE agi_datahub_log_v1.T_ILI2DB_IMPORT (
  T_Id bigint PRIMARY KEY
  ,dataset bigint NOT NULL
  ,importDate timestamp NOT NULL
  ,importUser varchar(40) NOT NULL
  ,importFile varchar(200) NULL
)
;
CREATE INDEX T_ILI2DB_IMPORT_dataset_idx ON agi_datahub_log_v1.t_ili2db_import ( dataset );
CREATE TABLE agi_datahub_log_v1.T_ILI2DB_IMPORT_BASKET (
  T_Id bigint PRIMARY KEY
  ,importrun bigint NOT NULL
  ,basket bigint NOT NULL
  ,objectCount integer NULL
)
;
CREATE INDEX T_ILI2DB_IMPORT_BASKET_importrun_idx ON agi_datahub_log_v1.t_ili2db_import_basket ( importrun );
CREATE INDEX T_ILI2DB_IMPORT_BASKET_basket_idx ON agi_datahub_log_v1.t_ili2db_import_basket ( basket );
CREATE TABLE agi_datahub_log_v1.T_ILI2DB_INHERITANCE (
  thisClass varchar(1024) PRIMARY KEY
  ,baseClass varchar(1024) NULL
)
;
CREATE TABLE agi_datahub_log_v1.T_ILI2DB_SETTINGS (
  tag varchar(60) PRIMARY KEY
  ,setting varchar(8000) NULL
)
;
CREATE TABLE agi_datahub_log_v1.T_ILI2DB_TRAFO (
  iliname varchar(1024) NOT NULL
  ,tag varchar(1024) NOT NULL
  ,setting varchar(1024) NOT NULL
)
;
CREATE TABLE agi_datahub_log_v1.T_ILI2DB_MODEL (
  filename varchar(250) NOT NULL
  ,iliversion varchar(3) NOT NULL
  ,modelName text NOT NULL
  ,content text NOT NULL
  ,importDate timestamp NOT NULL
  ,PRIMARY KEY (modelName,iliversion)
)
;
CREATE TABLE agi_datahub_log_v1.T_ILI2DB_CLASSNAME (
  IliName varchar(1024) PRIMARY KEY
  ,SqlName varchar(1024) NOT NULL
)
;
CREATE TABLE agi_datahub_log_v1.T_ILI2DB_ATTRNAME (
  IliName varchar(1024) NOT NULL
  ,SqlName varchar(1024) NOT NULL
  ,ColOwner varchar(1024) NOT NULL
  ,Target varchar(1024) NULL
  ,PRIMARY KEY (SqlName,ColOwner)
)
;
CREATE TABLE agi_datahub_log_v1.T_ILI2DB_COLUMN_PROP (
  tablename varchar(255) NOT NULL
  ,subtype varchar(255) NULL
  ,columnname varchar(255) NOT NULL
  ,tag varchar(1024) NOT NULL
  ,setting varchar(8000) NOT NULL
)
;
CREATE TABLE agi_datahub_log_v1.T_ILI2DB_TABLE_PROP (
  tablename varchar(255) NOT NULL
  ,tag varchar(1024) NOT NULL
  ,setting varchar(8000) NOT NULL
)
;
CREATE TABLE agi_datahub_log_v1.T_ILI2DB_META_ATTRS (
  ilielement varchar(255) NOT NULL
  ,attr_name varchar(1024) NOT NULL
  ,attr_value varchar(8000) NOT NULL
)
;
CREATE UNIQUE INDEX T_ILI2DB_DATASET_datasetName_key ON agi_datahub_log_v1.T_ILI2DB_DATASET (datasetName)
;
CREATE UNIQUE INDEX T_ILI2DB_MODEL_modelName_iliversion_key ON agi_datahub_log_v1.T_ILI2DB_MODEL (modelName,iliversion)
;
CREATE UNIQUE INDEX T_ILI2DB_ATTRNAME_SqlName_ColOwner_key ON agi_datahub_log_v1.T_ILI2DB_ATTRNAME (SqlName,ColOwner)
;
INSERT INTO agi_datahub_log_v1.T_ILI2DB_CLASSNAME (IliName,SqlName) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery','deliveries_delivery');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.organisation','organisation','deliveries_delivery',NULL);
INSERT INTO agi_datahub_log_v1.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.deliveryDate','deliverydate','deliveries_delivery',NULL);
INSERT INTO agi_datahub_log_v1.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.isDelivered','isdelivered','deliveries_delivery',NULL);
INSERT INTO agi_datahub_log_v1.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.operat','operat','deliveries_delivery',NULL);
INSERT INTO agi_datahub_log_v1.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.isValid','isvalid','deliveries_delivery',NULL);
INSERT INTO agi_datahub_log_v1.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.theme','theme','deliveries_delivery',NULL);
INSERT INTO agi_datahub_log_v1.T_ILI2DB_ATTRNAME (IliName,SqlName,ColOwner,Target) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.jobId','jobid','deliveries_delivery',NULL);
INSERT INTO agi_datahub_log_v1.T_ILI2DB_TRAFO (iliname,tag,setting) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery','ch.ehi.ili2db.inheritance','newClass');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_INHERITANCE (thisClass,baseClass) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery',NULL);
INSERT INTO agi_datahub_log_v1.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery',NULL,'operat','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery',NULL,'jobid','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery',NULL,'isdelivered','ch.ehi.ili2db.typeKind','BOOLEAN');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery',NULL,'isvalid','ch.ehi.ili2db.typeKind','BOOLEAN');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery',NULL,'organisation','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery','deliveries_delivery','isdelivered','ch.ehi.ili2db.enumDomain','INTERLIS.BOOLEAN');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery',NULL,'theme','ch.ehi.ili2db.typeKind','TEXT');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery','deliveries_delivery','isvalid','ch.ehi.ili2db.enumDomain','INTERLIS.BOOLEAN');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_COLUMN_PROP (tablename,subtype,columnname,tag,setting) VALUES ('deliveries_delivery',NULL,'deliverydate','ch.ehi.ili2db.typeKind','DATETIME');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_TABLE_PROP (tablename,tag,setting) VALUES ('deliveries_delivery','ch.ehi.ili2db.tableKind','CLASS');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_MODEL (filename,iliversion,modelName,content,importDate) VALUES ('SO_AGI_Datahub_20240403.ili','2.3','SO_AGI_Datahub_Config_20240403 SO_AGI_Datahub_Log_20240403','INTERLIS 2.3;

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
VERSION "2024-03-01"  =

  TOPIC Core =

    CLASS ApiKey =
      /** API-Key
       */
      apiKey : MANDATORY TEXT*256;
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
VERSION "2024-04-03"  =

  TOPIC Deliveries =

    CLASS Delivery =
      jobId : MANDATORY TEXT*36;
      deliveryDate : MANDATORY INTERLIS.XMLDateTime;
      isValid : BOOLEAN;
      isDelivered : BOOLEAN;
      organisation : MANDATORY TEXT*512;
      theme : MANDATORY TEXT*512;
      operat : MANDATORY TEXT*512;
      UNIQUE jobId;
    END Delivery;

  END Deliveries;

END SO_AGI_Datahub_Log_20240403.
','2024-08-29 08:02:02.989');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.createMetaInfo','True');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.interlis.ili2c.ilidirs','https://geo.so.ch/models;http://models.geo.admin.ch');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.arrayTrafo','coalesce');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.createForeignKeyIndex','yes');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.importTabs','simple');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.nameOptimization','topic');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.localisedTrafo','expand');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.jsonTrafo','coalesce');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.sender','ili2pg-4.9.1-edeb4647bbdea52480a419b5aa605d425fcbd5df');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.sqlgen.createGeomIndex','True');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.defaultSrsAuthority','EPSG');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.defaultSrsCode','2056');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.uniqueConstraints','create');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.maxSqlNameLength','60');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.uuidDefaultValue','uuid_generate_v4()');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.inheritanceTrafo','smart1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.catalogueRefTrafo','coalesce');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.multiPointTrafo','coalesce');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.StrokeArcs','enable');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.multiLineTrafo','coalesce');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.multiSurfaceTrafo','coalesce');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_SETTINGS (tag,setting) VALUES ('ch.ehi.ili2db.multilingualTrafo','expand');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.config','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.config','ili2db.ili.attrCardinalityMin','0');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.theme','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.theme','ili2db.ili.attrCardinalityMin','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat.description','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat.description','ili2db.ili.attrCardinalityMin','0');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.description','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.description','ili2db.ili.attrCardinalityMin','0');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.Organisation_R','ili2db.ili.assocCardinalityMin','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.Organisation_R','ili2db.ili.assocCardinalityMax','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.Organisation_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.ApiKey_R','ili2db.ili.assocCardinalityMin','0');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.ApiKey_R','ili2db.ili.assocCardinalityMax','*');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.ApiKey_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.jobId','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.jobId','ili2db.ili.attrCardinalityMin','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.deliveryDate','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.deliveryDate','ili2db.ili.attrCardinalityMin','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Theme_R','ili2db.ili.assocCardinalityMin','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Theme_R','ili2db.ili.assocCardinalityMax','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Theme_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.name','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.name','ili2db.ili.attrCardinalityMin','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.revokedAt','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.revokedAt','ili2db.ili.attrCardinalityMin','0');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core','ili2db.ili.topicClasses','core_apikey core_operat core_organisation core_theme');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Organisation_R','ili2db.ili.assocCardinalityMin','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Organisation_R','ili2db.ili.assocCardinalityMax','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Organisation_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403','furtherInformation','http://geo.so.ch/models/AGI/SO_AGI_Datahub_20240301.uml');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403','technicalContact','mailto:agi@bd.so.ch');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries','ili2db.ili.topicClasses','deliveries_delivery');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Operat_R','ili2db.ili.assocCardinalityMin','0');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Operat_R','ili2db.ili.assocCardinalityMax','*');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Operat_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.isValid','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.isValid','ili2db.ili.attrCardinalityMin','0');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.createdAt','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.createdAt','ili2db.ili.attrCardinalityMin','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat.name','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat.name','ili2db.ili.attrCardinalityMin','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.metaConfig','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.metaConfig','ili2db.ili.attrCardinalityMin','0');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Operat_R','ili2db.ili.assocCardinalityMin','0');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Operat_R','ili2db.ili.assocCardinalityMax','*');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Operat_R','ili2db.ili.assocKind','ASSOCIATE');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.isDelivered','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.isDelivered','ili2db.ili.attrCardinalityMin','0');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.dateOfExpiry','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.dateOfExpiry','ili2db.ili.attrCardinalityMin','0');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.uid','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.uid','ili2db.ili.attrCardinalityMin','0');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.name','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.name','ili2db.ili.attrCardinalityMin','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.email','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.email','ili2db.ili.attrCardinalityMin','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.apiKey','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.ApiKey.apiKey','ili2db.ili.attrCardinalityMin','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403','furtherInformation','http://geo.so.ch/models/AGI/SO_AGI_Datahub_20240301.uml');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403','technicalContact','mailto:agi@bd.so.ch');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.role','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Config_20240403.Core.Organisation.role','ili2db.ili.attrCardinalityMin','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.organisation','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.organisation','ili2db.ili.attrCardinalityMin','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.operat','ili2db.ili.attrCardinalityMax','1');
INSERT INTO agi_datahub_log_v1.T_ILI2DB_META_ATTRS (ilielement,attr_name,attr_value) VALUES ('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.operat','ili2db.ili.attrCardinalityMin','1');

CREATE SCHEMA IF NOT EXISTS agi_datahub_jobrunr_v1;
CREATE TABLE agi_datahub_jobrunr_v1.jobrunr_migrations
(
    id          nchar(36) PRIMARY KEY,
    script      varchar(64) NOT NULL,
    installedOn varchar(29) NOT NULL
);
CREATE TABLE agi_datahub_jobrunr_v1.jobrunr_jobs
(
    id           NCHAR(36) PRIMARY KEY,
    version      int          NOT NULL,
    jobAsJson    text         NOT NULL,
    jobSignature VARCHAR(512) NOT NULL,
    state        VARCHAR(36)  NOT NULL,
    createdAt    TIMESTAMP    NOT NULL,
    updatedAt    TIMESTAMP    NOT NULL,
    scheduledAt  TIMESTAMP
);
CREATE INDEX jobrunr_state_idx ON agi_datahub_jobrunr_v1.jobrunr_jobs (state);
CREATE INDEX jobrunr_job_signature_idx ON agi_datahub_jobrunr_v1.jobrunr_jobs (jobSignature);
CREATE INDEX jobrunr_job_created_at_idx ON agi_datahub_jobrunr_v1.jobrunr_jobs (createdAt);
CREATE INDEX jobrunr_job_updated_at_idx ON agi_datahub_jobrunr_v1.jobrunr_jobs (updatedAt);
CREATE INDEX jobrunr_job_scheduled_at_idx ON agi_datahub_jobrunr_v1.jobrunr_jobs (scheduledAt);
CREATE TABLE agi_datahub_jobrunr_v1.jobrunr_recurring_jobs
(
    id        NCHAR(128) PRIMARY KEY,
    version   int  NOT NULL,
    jobAsJson text NOT NULL
);
CREATE TABLE agi_datahub_jobrunr_v1.jobrunr_backgroundjobservers
(
    id                     NCHAR(36) PRIMARY KEY,
    workerPoolSize         int           NOT NULL,
    pollIntervalInSeconds  int           NOT NULL,
    firstHeartbeat         TIMESTAMP(6)  NOT NULL,
    lastHeartbeat          TIMESTAMP(6)  NOT NULL,
    running                int           NOT NULL,
    systemTotalMemory      BIGINT        NOT NULL,
    systemFreeMemory       BIGINT        NOT NULL,
    systemCpuLoad          NUMERIC(3, 2) NOT NULL,
    processMaxMemory       BIGINT        NOT NULL,
    processFreeMemory      BIGINT        NOT NULL,
    processAllocatedMemory BIGINT        NOT NULL,
    processCpuLoad         NUMERIC(3, 2) NOT NULL
);
CREATE INDEX jobrunr_bgjobsrvrs_fsthb_idx ON agi_datahub_jobrunr_v1.jobrunr_backgroundjobservers (firstHeartbeat);
CREATE INDEX jobrunr_bgjobsrvrs_lsthb_idx ON agi_datahub_jobrunr_v1.jobrunr_backgroundjobservers (lastHeartbeat);
CREATE TABLE agi_datahub_jobrunr_v1.jobrunr_job_counters
(
    name   NCHAR(36) PRIMARY KEY,
    amount int NOT NULL
);

INSERT INTO agi_datahub_jobrunr_v1.jobrunr_job_counters (name, amount)
VALUES ('AWAITING', 0);
INSERT INTO agi_datahub_jobrunr_v1.jobrunr_job_counters (name, amount)
VALUES ('SCHEDULED', 0);
INSERT INTO agi_datahub_jobrunr_v1.jobrunr_job_counters (name, amount)
VALUES ('ENQUEUED', 0);
INSERT INTO agi_datahub_jobrunr_v1.jobrunr_job_counters (name, amount)
VALUES ('PROCESSING', 0);
INSERT INTO agi_datahub_jobrunr_v1.jobrunr_job_counters (name, amount)
VALUES ('FAILED', 0);
INSERT INTO agi_datahub_jobrunr_v1.jobrunr_job_counters (name, amount)
VALUES ('SUCCEEDED', 0);

CREATE VIEW agi_datahub_jobrunr_v1.jobrunr_jobs_stats
as
select count(*)                                                                           as total,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_jobs jobs where jobs.state = 'AWAITING')             as awaiting,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_jobs jobs where jobs.state = 'SCHEDULED')            as scheduled,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_jobs jobs where jobs.state = 'ENQUEUED')             as enqueued,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_jobs jobs where jobs.state = 'PROCESSING')           as processing,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_jobs jobs where jobs.state = 'FAILED')               as failed,
       (select((select count(*) from agi_datahub_jobrunr_v1.jobrunr_jobs jobs where jobs.state = 'SUCCEEDED') +
               (select amount from agi_datahub_jobrunr_v1.jobrunr_job_counters jc where jc.name = 'SUCCEEDED'))) as succeeded,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_backgroundjobservers)                                as nbrOfBackgroundJobServers,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_recurring_jobs)                                      as nbrOfRecurringJobs
from agi_datahub_jobrunr_v1.jobrunr_jobs j;
DROP VIEW agi_datahub_jobrunr_v1.jobrunr_jobs_stats;

CREATE VIEW agi_datahub_jobrunr_v1.jobrunr_jobs_stats
as
select count(*)                                                                           as total,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_jobs jobs where jobs.state = 'AWAITING')             as awaiting,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_jobs jobs where jobs.state = 'SCHEDULED')            as scheduled,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_jobs jobs where jobs.state = 'ENQUEUED')             as enqueued,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_jobs jobs where jobs.state = 'PROCESSING')           as processing,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_jobs jobs where jobs.state = 'FAILED')               as failed,
       (select((select count(*) from agi_datahub_jobrunr_v1.jobrunr_jobs jobs where jobs.state = 'SUCCEEDED') +
               (select amount from agi_datahub_jobrunr_v1.jobrunr_job_counters jc where jc.name = 'SUCCEEDED'))) as succeeded,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_jobs jobs where jobs.state = 'DELETED')              as deleted,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_backgroundjobservers)                                as nbrOfBackgroundJobServers,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_recurring_jobs)                                      as nbrOfRecurringJobs
from agi_datahub_jobrunr_v1.jobrunr_jobs j;
ALTER TABLE agi_datahub_jobrunr_v1.jobrunr_jobs
    ADD recurringJobId VARCHAR(128);
CREATE INDEX jobrunr_job_rci_idx ON agi_datahub_jobrunr_v1.jobrunr_jobs (recurringJobId);
ALTER TABLE agi_datahub_jobrunr_v1.jobrunr_backgroundjobservers
    ADD deleteSucceededJobsAfter VARCHAR(32);
ALTER TABLE agi_datahub_jobrunr_v1.jobrunr_backgroundjobservers
    ADD permanentlyDeleteJobsAfter VARCHAR(32);
-- Empty migration so all databases follow the same numbering;
CREATE TABLE agi_datahub_jobrunr_v1.jobrunr_metadata
(
    id        varchar(156) PRIMARY KEY,
    name      varchar(92) NOT NULL,
    owner     varchar(64) NOT NULL,
    value     text        NOT NULL,
    createdAt TIMESTAMP   NOT NULL,
    updatedAt TIMESTAMP   NOT NULL
);

INSERT INTO agi_datahub_jobrunr_v1.jobrunr_metadata (id, name, owner, value, createdAt, updatedAt)
VALUES ('succeeded-jobs-counter-cluster', 'succeeded-jobs-counter', 'cluster',
        cast((select amount from agi_datahub_jobrunr_v1.jobrunr_job_counters where name = 'SUCCEEDED') as char(10)), CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

DROP VIEW agi_datahub_jobrunr_v1.jobrunr_jobs_stats;
DROP TABLE agi_datahub_jobrunr_v1.jobrunr_job_counters;

CREATE VIEW agi_datahub_jobrunr_v1.jobrunr_jobs_stats
as
select count(*)                                                                 as total,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_jobs jobs where jobs.state = 'AWAITING')   as awaiting,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_jobs jobs where jobs.state = 'SCHEDULED')  as scheduled,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_jobs jobs where jobs.state = 'ENQUEUED')   as enqueued,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_jobs jobs where jobs.state = 'PROCESSING') as processing,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_jobs jobs where jobs.state = 'FAILED')     as failed,
       (select((select count(*) from agi_datahub_jobrunr_v1.jobrunr_jobs jobs where jobs.state = 'SUCCEEDED') +
               (select cast(cast(value as char(10)) as decimal(10, 0))
                from agi_datahub_jobrunr_v1.jobrunr_metadata jm
                where jm.id = 'succeeded-jobs-counter-cluster')))               as succeeded,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_jobs jobs where jobs.state = 'DELETED')    as deleted,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_backgroundjobservers)                      as nbrOfBackgroundJobServers,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_recurring_jobs)                            as nbrOfRecurringJobs
from agi_datahub_jobrunr_v1.jobrunr_jobs j;
DROP VIEW agi_datahub_jobrunr_v1.jobrunr_jobs_stats;

CREATE VIEW agi_datahub_jobrunr_v1.jobrunr_jobs_stats
as
select count(*)                                                                 as total,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_jobs jobs where jobs.state = 'AWAITING')   as awaiting,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_jobs jobs where jobs.state = 'SCHEDULED')  as scheduled,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_jobs jobs where jobs.state = 'ENQUEUED')   as enqueued,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_jobs jobs where jobs.state = 'PROCESSING') as processing,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_jobs jobs where jobs.state = 'FAILED')     as failed,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_jobs jobs where jobs.state = 'SUCCEEDED')  as succeeded,
       (select cast(cast(value as char(10)) as decimal(10, 0))
        from agi_datahub_jobrunr_v1.jobrunr_metadata jm
        where jm.id = 'succeeded-jobs-counter-cluster')                         as allTimeSucceeded,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_jobs jobs where jobs.state = 'DELETED')    as deleted,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_backgroundjobservers)                      as nbrOfBackgroundJobServers,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_recurring_jobs)                            as nbrOfRecurringJobs
from agi_datahub_jobrunr_v1.jobrunr_jobs j;
-- Empty migration so all databases follow the same numbering;
-- Empty migration so all databases follow the same numbering;
ALTER TABLE agi_datahub_jobrunr_v1.jobrunr_recurring_jobs
    ADD createdAt BIGINT NOT NULL DEFAULT '0';
CREATE INDEX jobrunr_recurring_job_created_at_idx ON agi_datahub_jobrunr_v1.jobrunr_recurring_jobs (createdAt);
DROP VIEW agi_datahub_jobrunr_v1.jobrunr_jobs_stats;
CREATE VIEW agi_datahub_jobrunr_v1.jobrunr_jobs_stats
as
with job_stat_results as (SELECT state, count(*) as count
                          FROM agi_datahub_jobrunr_v1.jobrunr_jobs
                          GROUP BY ROLLUP (state))
select coalesce((select count from job_stat_results where state IS NULL), 0)        as total,
       coalesce((select count from job_stat_results where state = 'SCHEDULED'), 0)  as scheduled,
       coalesce((select count from job_stat_results where state = 'ENQUEUED'), 0)   as enqueued,
       coalesce((select count from job_stat_results where state = 'PROCESSING'), 0) as processing,
       coalesce((select count from job_stat_results where state = 'FAILED'), 0)     as failed,
       coalesce((select count from job_stat_results where state = 'SUCCEEDED'), 0)  as succeeded,
       coalesce((select cast(cast(value as char(10)) as decimal(10, 0))
                 from agi_datahub_jobrunr_v1.jobrunr_metadata jm
                 where jm.id = 'succeeded-jobs-counter-cluster'), 0)                as allTimeSucceeded,
       coalesce((select count from job_stat_results where state = 'DELETED'), 0)    as deleted,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_backgroundjobservers)                          as nbrOfBackgroundJobServers,
       (select count(*) from agi_datahub_jobrunr_v1.jobrunr_recurring_jobs)                                as nbrOfRecurringJobs;

DROP INDEX agi_datahub_jobrunr_v1.jobrunr_job_updated_at_idx;
CREATE INDEX jobrunr_jobs_state_updated_idx ON agi_datahub_jobrunr_v1.jobrunr_jobs (state ASC, updatedAt ASC);
ALTER TABLE agi_datahub_jobrunr_v1.jobrunr_backgroundjobservers
    ADD name VARCHAR(128);
COMMENT ON SCHEMA agi_datahub_jobrunr_v1 IS 'Schema für Jobrunr';
GRANT USAGE ON SCHEMA agi_datahub_jobrunr_v1 TO datahub;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA agi_datahub_jobrunr_v1 TO datahub;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA agi_datahub_jobrunr_v1 TO datahub;
CREATE OR REPLACE VIEW agi_datahub_log_v1.v_jobresponse AS
WITH queue_position AS 
(
    SELECT 
        j.id, 
        createdat,
        ROW_NUMBER() OVER (ORDER BY createdat) AS queueposition
    FROM 
        agi_datahub_jobrunr_v1.jobrunr_jobs AS j
    WHERE 
        j.state = 'ENQUEUED'
)
SELECT 
    d.jobid,
    (j.createdat AT TIME ZONE 'UTC') AT TIME ZONE 'Europe/Zurich' AS createdat,
    (j.updatedat AT TIME ZONE 'UTC') AT TIME ZONE 'Europe/Zurich' AS updatedat,
    CASE 
        WHEN state = 'SUCCEEDED' THEN 'SUCCEEDED'
        ELSE state
    END as status, 
    queue_position.queueposition AS queueposition,
    d.operat AS operat,
    d.theme AS theme,
    d.organisation AS organisation,
    CASE 
        WHEN isvalid IS TRUE THEN CAST('DONE' AS VARCHAR(512)) 
        WHEN isvalid IS FALSE THEN CAST('FAILED' AS VARCHAR(512))
        ELSE CAST(NULL AS VARCHAR(512))
    END AS validationstatus
FROM 
    agi_datahub_jobrunr_v1.jobrunr_jobs AS j
    LEFT JOIN agi_datahub_log_v1.deliveries_delivery AS d 
    ON j.id = d.jobid 
    LEFT JOIN queue_position 
    ON queue_position.id = j.id
;
COMMENT ON SCHEMA agi_datahub_config_v1 IS 'Schema für Datahub';
GRANT USAGE ON SCHEMA agi_datahub_config_v1 TO datahub;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA agi_datahub_config_v1 TO datahub;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA agi_datahub_config_v1 TO datahub;

COMMENT ON SCHEMA agi_datahub_log_v1 IS 'Schema für Datahub';
GRANT USAGE ON SCHEMA agi_datahub_log_v1 TO datahub;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA agi_datahub_log_v1 TO datahub;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA agi_datahub_log_v1 TO datahub;

CREATE ROLE datahub_read;
GRANT USAGE ON SCHEMA agi_datahub_config_v1 TO datahub_read;
GRANT SELECT ON ALL TABLES IN SCHEMA agi_datahub_config_v1 TO datahub_read;
GRANT USAGE ON SCHEMA agi_datahub_log_v1 TO datahub_read;
GRANT SELECT ON ALL TABLES IN SCHEMA agi_datahub_log_v1 TO datahub_read;
GRANT USAGE ON SCHEMA agi_datahub_jobrunr_v1 TO datahub_read;
GRANT SELECT ON ALL TABLES IN SCHEMA agi_datahub_jobrunr_v1 TO datahub_read;
CREATE ROLE datahub_write;
GRANT USAGE ON SCHEMA agi_datahub_config_v1 TO datahub_write;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA agi_datahub_config_v1 TO datahub_write;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA agi_datahub_config_v1 TO datahub_write;
