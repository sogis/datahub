-- Keep ili2db exports aware of the extended logical model and grant generated sequence access.
GRANT USAGE ON ALL SEQUENCES IN SCHEMA ${logSchema} TO datahub;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA ${configSchema} TO datahub;
UPDATE ${configSchema}.t_ili2db_model SET content='INTERLIS 2.3;

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
' WHERE modelname LIKE 'SO_AGI_Datahub_Config_20240403%';
UPDATE ${logSchema}.t_ili2db_model SET content='INTERLIS 2.3;

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
' WHERE modelname LIKE 'SO_AGI_Datahub_Log_20240403%';
INSERT INTO ${configSchema}.t_ili2db_attrname(iliname,sqlname,colowner) VALUES('SO_AGI_Datahub_Config_20240403.Core.ApiKey.issuanceState','issuance_state','core_apikey') ON CONFLICT(colowner,sqlname) DO UPDATE SET iliname=EXCLUDED.iliname;
INSERT INTO ${configSchema}.t_ili2db_attrname(iliname,sqlname,colowner) VALUES('SO_AGI_Datahub_Config_20240403.Core.Theme.expectedModels','expected_models','core_theme') ON CONFLICT(colowner,sqlname) DO UPDATE SET iliname=EXCLUDED.iliname;
INSERT INTO ${logSchema}.t_ili2db_attrname(iliname,sqlname,colowner) VALUES('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.operatId','operat_id','deliveries_delivery') ON CONFLICT(colowner,sqlname) DO UPDATE SET iliname=EXCLUDED.iliname;
INSERT INTO ${logSchema}.t_ili2db_attrname(iliname,sqlname,colowner) VALUES('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.keyId','key_id','deliveries_delivery') ON CONFLICT(colowner,sqlname) DO UPDATE SET iliname=EXCLUDED.iliname;
INSERT INTO ${logSchema}.t_ili2db_attrname(iliname,sqlname,colowner) VALUES('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.attemptId','attempt_id','deliveries_delivery') ON CONFLICT(colowner,sqlname) DO UPDATE SET iliname=EXCLUDED.iliname;
INSERT INTO ${logSchema}.t_ili2db_attrname(iliname,sqlname,colowner) VALUES('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.email','email','deliveries_delivery') ON CONFLICT(colowner,sqlname) DO UPDATE SET iliname=EXCLUDED.iliname;
INSERT INTO ${logSchema}.t_ili2db_attrname(iliname,sqlname,colowner) VALUES('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validatorConfig','validator_config','deliveries_delivery') ON CONFLICT(colowner,sqlname) DO UPDATE SET iliname=EXCLUDED.iliname;
INSERT INTO ${logSchema}.t_ili2db_attrname(iliname,sqlname,colowner) VALUES('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validatorMetaConfig','validator_meta_config','deliveries_delivery') ON CONFLICT(colowner,sqlname) DO UPDATE SET iliname=EXCLUDED.iliname;
INSERT INTO ${logSchema}.t_ili2db_attrname(iliname,sqlname,colowner) VALUES('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.expectedModels','expected_models','deliveries_delivery') ON CONFLICT(colowner,sqlname) DO UPDATE SET iliname=EXCLUDED.iliname;
INSERT INTO ${logSchema}.t_ili2db_attrname(iliname,sqlname,colowner) VALUES('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.executionStatus','execution_status','deliveries_delivery') ON CONFLICT(colowner,sqlname) DO UPDATE SET iliname=EXCLUDED.iliname;
INSERT INTO ${logSchema}.t_ili2db_attrname(iliname,sqlname,colowner) VALUES('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validationStatus','validation_status','deliveries_delivery') ON CONFLICT(colowner,sqlname) DO UPDATE SET iliname=EXCLUDED.iliname;
INSERT INTO ${logSchema}.t_ili2db_attrname(iliname,sqlname,colowner) VALUES('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.publicationStatus','publication_status','deliveries_delivery') ON CONFLICT(colowner,sqlname) DO UPDATE SET iliname=EXCLUDED.iliname;
INSERT INTO ${logSchema}.t_ili2db_attrname(iliname,sqlname,colowner) VALUES('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.notificationStatus','notification_status','deliveries_delivery') ON CONFLICT(colowner,sqlname) DO UPDATE SET iliname=EXCLUDED.iliname;
INSERT INTO ${logSchema}.t_ili2db_attrname(iliname,sqlname,colowner) VALUES('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.publicationTries','publication_tries','deliveries_delivery') ON CONFLICT(colowner,sqlname) DO UPDATE SET iliname=EXCLUDED.iliname;
INSERT INTO ${logSchema}.t_ili2db_attrname(iliname,sqlname,colowner) VALUES('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.publicationNextAt','publication_next_at','deliveries_delivery') ON CONFLICT(colowner,sqlname) DO UPDATE SET iliname=EXCLUDED.iliname;
INSERT INTO ${logSchema}.t_ili2db_attrname(iliname,sqlname,colowner) VALUES('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.updatedAt','updated_at','deliveries_delivery') ON CONFLICT(colowner,sqlname) DO UPDATE SET iliname=EXCLUDED.iliname;
INSERT INTO ${logSchema}.t_ili2db_attrname(iliname,sqlname,colowner) VALUES('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.finishedAt','finished_at','deliveries_delivery') ON CONFLICT(colowner,sqlname) DO UPDATE SET iliname=EXCLUDED.iliname;
INSERT INTO ${logSchema}.t_ili2db_attrname(iliname,sqlname,colowner) VALUES('SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.artifactsExpired','artifacts_expired','deliveries_delivery') ON CONFLICT(colowner,sqlname) DO UPDATE SET iliname=EXCLUDED.iliname;
INSERT INTO ${logSchema}.t_ili2db_classname(iliname,sqlname) VALUES('SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt','validation_attempt') ON CONFLICT(iliname) DO UPDATE SET sqlname=EXCLUDED.sqlname;
INSERT INTO ${logSchema}.t_ili2db_classname(iliname,sqlname) VALUES('SO_AGI_Datahub_Log_20240403.Deliveries.DatasetPublication','dataset_publication') ON CONFLICT(iliname) DO UPDATE SET sqlname=EXCLUDED.sqlname;
INSERT INTO ${logSchema}.t_ili2db_classname(iliname,sqlname) VALUES('SO_AGI_Datahub_Log_20240403.Deliveries.Notification','notification') ON CONFLICT(iliname) DO UPDATE SET sqlname=EXCLUDED.sqlname;
