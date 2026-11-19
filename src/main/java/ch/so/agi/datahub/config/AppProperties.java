package ch.so.agi.datahub.config;

import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app")
public class AppProperties {
  private String dbSchemaConfig = "agi_datahub_config_v1";
  private String dbSchemaLog = "agi_datahub_log_v1";
  private String dbSchemaJob = "agi_datahub_jobrunr_v1.";
  private String apiKeyHeaderName = "X-API-KEY";
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

  private boolean acceptDeliveries = true;

  public boolean isAcceptDeliveries() {
    return acceptDeliveries;
  }

  public void setAcceptDeliveries(boolean value) {
    acceptDeliveries = value;
  }

  private boolean mailEnabled = false;
  private String mailFrom = "datahub@example.test";
  private String adminKeyFile = "";
  private String adminAccountName = "AGI SO";
  private String adminAccountMail = "admin@example.test";
  private boolean createDirectories = true;
  private int jobResponseListLimit = 300;
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

  public String getAdminKeyFile() {
    return adminKeyFile;
  }

  public void setAdminKeyFile(String value) {
    this.adminKeyFile = value;
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

  public int getJobResponseListLimit() {
    return jobResponseListLimit;
  }

  public void setJobResponseListLimit(int value) {
    this.jobResponseListLimit = value;
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
    if (dbSchemaJob == null || !dbSchemaJob.endsWith("."))
      throw new IllegalArgumentException("The JobRunr table prefix must be a schema followed by a dot");
    return schema(dbSchemaJob.substring(0, dbSchemaJob.length() - 1));
  }

  public LocalDateTime artifactCutoff(Clock clock) {
    return LocalDateTime.now(clock).minus(artifactRetention);
  }

  public boolean artifactsExpired(Boolean marked, LocalDateTime finished, Clock clock) {
    return Boolean.TRUE.equals(marked) || (finished != null && finished.isBefore(artifactCutoff(clock)));
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
