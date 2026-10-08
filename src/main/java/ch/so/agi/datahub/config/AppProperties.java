package ch.so.agi.datahub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app")
public class AppProperties {
  private String dbSchemaConfig = "agi_datahub_config_v1";
  private String dbSchemaLog = "agi_datahub_log_v1";
  private String dbSchemaJob = "agi_datahub_jobrunr_v1";
  private String apiKeyHeaderName = "X-API-KEY";
  private String apiKeyQueryParamName = "token";
  private String publicBaseUrl = "http://localhost:8080";
  private String workDirectory = "./build/work";
  private String targetDirectory = "./build/target";
  private String folderPrefix = "datahub_";
  private String preferredIliRepo = "https://geo.so.ch/models";
  private boolean defaultIliRepositories = true;

  public boolean isDefaultIliRepositories() {
    return defaultIliRepositories;
  }

  public void setDefaultIliRepositories(boolean value) {
    defaultIliRepositories = value;
  }

  public String iliRepositories() {
    return preferredIliRepo
        + (defaultIliRepositories ? ";" + ch.interlis.ili2c.Ili2cSettings.DEFAULT_ILIDIRS : "");
  }

  private String timeZone = "Europe/Zurich";

  public String getTimeZone() {
    return timeZone;
  }

  public void setTimeZone(String value) {
    timeZone = value;
  }

  private boolean dispatchEnabled = true;
  private boolean acceptDeliveries = true;

  public boolean isDispatchEnabled() {
    return dispatchEnabled;
  }

  public void setDispatchEnabled(boolean value) {
    dispatchEnabled = value;
  }

  public boolean isAcceptDeliveries() {
    return acceptDeliveries;
  }

  public void setAcceptDeliveries(boolean value) {
    acceptDeliveries = value;
  }

  private String mode = "web";
  private String attemptId = null;
  private boolean mailEnabled = false;
  private String mailFrom = "datahub@example.test";
  private boolean adminAccountInit = false;
  private String adminAccountName = "AGI SO";
  private String adminAccountMail = "admin@example.test";
  private boolean createDirectories = true;
  private boolean cleanerEnabled = true;
  private int jobResponseListLimit = 300;
  private String[] httpWhitelist = new String[] {"localhost"};
  private java.time.Duration heartbeatTimeout = java.time.Duration.ofMinutes(2);
  private java.time.Duration artifactRetention = java.time.Duration.ofDays(3);

  public String getDbSchemaConfig() {
    return dbSchemaConfig;
  }

  public void setDbSchemaConfig(String value) {
    this.dbSchemaConfig = value;
  }

  public String getDbSchemaLog() {
    return dbSchemaLog;
  }

  public void setDbSchemaLog(String value) {
    this.dbSchemaLog = value;
  }

  public String getDbSchemaJob() {
    return dbSchemaJob;
  }

  public void setDbSchemaJob(String value) {
    this.dbSchemaJob = value;
  }

  public String getApiKeyHeaderName() {
    return apiKeyHeaderName;
  }

  public void setApiKeyHeaderName(String value) {
    this.apiKeyHeaderName = value;
  }

  public String getApiKeyQueryParamName() {
    return apiKeyQueryParamName;
  }

  public void setApiKeyQueryParamName(String value) {
    this.apiKeyQueryParamName = value;
  }

  public String getPublicBaseUrl() {
    return publicBaseUrl;
  }

  public void setPublicBaseUrl(String value) {
    this.publicBaseUrl = value;
  }

  public String getWorkDirectory() {
    return workDirectory;
  }

  public void setWorkDirectory(String value) {
    this.workDirectory = value;
  }

  public String getTargetDirectory() {
    return targetDirectory;
  }

  public void setTargetDirectory(String value) {
    this.targetDirectory = value;
  }

  public String getFolderPrefix() {
    if (folderPrefix == null || !folderPrefix.matches("[a-zA-Z0-9_.-]*"))
      throw new IllegalArgumentException("Invalid artifact folder prefix");
    return folderPrefix;
  }

  public void setFolderPrefix(String value) {
    this.folderPrefix = value;
  }

  public String getPreferredIliRepo() {
    return preferredIliRepo;
  }

  public void setPreferredIliRepo(String value) {
    this.preferredIliRepo = value;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String value) {
    this.mode = value;
  }

  public String getAttemptId() {
    return attemptId;
  }

  public void setAttemptId(String value) {
    this.attemptId = value;
  }

  public boolean isMailEnabled() {
    return mailEnabled;
  }

  public void setMailEnabled(boolean value) {
    this.mailEnabled = value;
  }

  public String getMailFrom() {
    return mailFrom;
  }

  public void setMailFrom(String value) {
    this.mailFrom = value;
  }

  public boolean isAdminAccountInit() {
    return adminAccountInit;
  }

  public void setAdminAccountInit(boolean value) {
    this.adminAccountInit = value;
  }

  public String getAdminAccountName() {
    return adminAccountName;
  }

  public void setAdminAccountName(String value) {
    this.adminAccountName = value;
  }

  public String getAdminAccountMail() {
    return adminAccountMail;
  }

  public void setAdminAccountMail(String value) {
    this.adminAccountMail = value;
  }

  public boolean isCreateDirectories() {
    return createDirectories;
  }

  public void setCreateDirectories(boolean value) {
    this.createDirectories = value;
  }

  public boolean isCleanerEnabled() {
    return cleanerEnabled;
  }

  public void setCleanerEnabled(boolean value) {
    this.cleanerEnabled = value;
  }

  public int getJobResponseListLimit() {
    return jobResponseListLimit;
  }

  public void setJobResponseListLimit(int value) {
    this.jobResponseListLimit = value;
  }

  public String[] getHttpWhitelist() {
    return httpWhitelist;
  }

  public void setHttpWhitelist(String[] value) {
    this.httpWhitelist = value;
  }

  public java.time.Duration getHeartbeatTimeout() {
    return heartbeatTimeout;
  }

  public void setHeartbeatTimeout(java.time.Duration value) {
    this.heartbeatTimeout = value;
  }

  public java.time.Duration getArtifactRetention() {
    return artifactRetention;
  }

  public void setArtifactRetention(java.time.Duration value) {
    this.artifactRetention = value;
  }

  public String configSchema() {
    return schema(dbSchemaConfig);
  }

  public String logSchema() {
    return schema(dbSchemaLog);
  }

  public String jobSchema() {
    return schema(dbSchemaJob.replaceAll("\\.$", ""));
  }

  public String publicUrl(String path) {
    return publicBaseUrl.replaceAll("/+$", "") + path;
  }

  private static String schema(String value) {
    if (value == null || !value.matches("[a-z][a-z0-9_]*"))
      throw new IllegalArgumentException("Invalid schema name");
    return value;
  }
}
