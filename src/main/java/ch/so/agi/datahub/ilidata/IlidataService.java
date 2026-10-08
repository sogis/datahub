package ch.so.agi.datahub.ilidata;

import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.iom_j.Iom_jObject;
import ch.interlis.iom_j.xtf.XtfWriter;
import ch.interlis.iox_j.*;
import ch.so.agi.datahub.config.AppProperties;
import java.io.ByteArrayOutputStream;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

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
                "SELECT d.*,a.result_models,a.transfer_version FROM "
                    + p.logSchema()
                    + ".dataset_publication pub JOIN "
                    + p.logSchema()
                    + ".deliveries_delivery d ON d.t_id=pub.delivery_id JOIN "
                    + p.logSchema()
                    + ".validation_attempt a ON a.id=d.attempt_id WHERE"
                    + " d.publication_status='DELIVERED' AND d.isvalid=true ORDER BY"
                    + " d.theme,d.operat");
        for (var row : rows) {
          var entry =
              new Iom_jObject("DatasetIdx16.DataIndex.DatasetMetadata", Integer.toString(tid++));
          entry.setattrvalue("id", row.get("operat") + "." + row.get("theme"));
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
          file.setattrvalue("path", "files/" + row.get("theme") + "/" + row.get("operat") + ".xtf");
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
