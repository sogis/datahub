package ch.so.agi.datahub.ilidata;

import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.iom_j.Iom_jObject;
import ch.interlis.iom_j.xtf.XtfWriter;
import ch.interlis.iox_j.*;
import ch.so.agi.datahub.config.AppProperties;
import ch.so.agi.datahub.service.DeliveryFiles;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

@Service
public class IlidataService {
  private final TransferDescription td;
  private final JdbcTemplate jdbc;
  private final AppProperties p;

  public IlidataService(TransferDescription td, JdbcTemplate jdbc, AppProperties p) {
    this.td = td;
    this.jdbc = jdbc;
    this.p = p;
  }

  public byte[] createXml() {
    var output = new ByteArrayOutputStream();
    try {
      var writer = new XtfWriter(output, td);
      try {
        writer.write(new StartTransferEvent("SOGIS-DATAHUB", "", null));
        writer.write(new StartBasketEvent("DatasetIdx16.DataIndex", "b1"));
        int tid = 1;
        var rows =
            jdbc.queryForList(
                """
                SELECT * FROM (
                  SELECT DISTINCT ON (theme, operat)
                    theme, operat, jobid, isvalid, result_models, transfer_version
                  FROM %s.deliveries_delivery
                  WHERE processing_status = 'DELIVERED'
                  ORDER BY theme, operat, t_id DESC
                ) latest WHERE isvalid AND result_models IS NOT NULL
                ORDER BY theme, operat
                """.formatted(p.logSchema()));
        for (var row : rows) {
          String theme = (String) row.get("theme");
          String operat = (String) row.get("operat");
          try {
            DeliveryFiles.publicationSegment(theme);
            DeliveryFiles.publicationSegment(operat);
          } catch (IllegalArgumentException invalidPath) {
            continue;
          }
          var entry =
              new Iom_jObject("DatasetIdx16.DataIndex.DatasetMetadata", Integer.toString(tid++));
          entry.setattrvalue("id", operat + "." + theme);
          entry.setattrvalue("version", (String) row.get("jobid"));
          entry.setattrvalue("owner", "https://agi.so.ch");
          entry.setattrvalue("technicalContact", "https://agi.so.ch");
          entry.setattrvalue("epsgCode", "2056");
          for (String name : ((String) row.get("result_models")).split(";")) {
            var model = new Iom_jObject("DatasetIdx16.ModelLink", null);
            model.setattrvalue("name", name);
            entry.addattrobj("model", model);
          }
          var data = new Iom_jObject("DatasetIdx16.DataFile", null);
          data.setattrvalue(
              "fileFormat",
              "application/interlis+xml"
                  + (row.get("transfer_version") == null
                      ? ""
                      : ";version=" + row.get("transfer_version")));
          var file = new Iom_jObject("DatasetIdx16.File", null);
          file.setattrvalue("path", "files/"
              + UriUtils.encodePathSegment(theme, StandardCharsets.UTF_8) + "/"
              + UriUtils.encodePathSegment(operat, StandardCharsets.UTF_8) + ".xtf");
          data.addattrobj("file", file);
          entry.addattrobj("files", data);
          writer.write(new ObjectEvent(entry));
        }
        writer.write(new EndBasketEvent());
        writer.write(new EndTransferEvent());
        writer.flush();
      } finally {
        writer.close();
      }
      return output.toByteArray();
    } catch (Exception e) {
      throw new IllegalStateException("Could not generate dataset index", e);
    }
  }
}
