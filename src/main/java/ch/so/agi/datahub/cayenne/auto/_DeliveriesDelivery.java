package ch.so.agi.datahub.cayenne.auto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;

import org.apache.cayenne.BaseDataObject;
import org.apache.cayenne.exp.property.BaseProperty;
import org.apache.cayenne.exp.property.DateProperty;
import org.apache.cayenne.exp.property.PropertyFactory;
import org.apache.cayenne.exp.property.StringProperty;

/**
 * Class _DeliveriesDelivery was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _DeliveriesDelivery extends BaseDataObject {

    private static final long serialVersionUID = 1L;

    public static final String T_ID_PK_COLUMN = "t_id";

    public static final DateProperty<LocalDateTime> DELIVERYDATE = PropertyFactory.createDate("deliverydate", LocalDateTime.class);
    public static final BaseProperty<Boolean> ISDELIVERED = PropertyFactory.createBase("isdelivered", Boolean.class);
    public static final BaseProperty<Boolean> ISVALID = PropertyFactory.createBase("isvalid", Boolean.class);
    public static final StringProperty<String> JOBID = PropertyFactory.createString("jobid", String.class);
    public static final StringProperty<String> OPERAT = PropertyFactory.createString("operat", String.class);
    public static final StringProperty<String> ORGANISATION = PropertyFactory.createString("organisation", String.class);
    public static final StringProperty<String> THEME = PropertyFactory.createString("theme", String.class);

    protected LocalDateTime deliverydate;
    protected Boolean isdelivered;
    protected Boolean isvalid;
    protected String jobid;
    protected String operat;
    protected String organisation;
    protected String theme;


    public void setDeliverydate(LocalDateTime deliverydate) {
        beforePropertyWrite("deliverydate", this.deliverydate, deliverydate);
        this.deliverydate = deliverydate;
    }

    public LocalDateTime getDeliverydate() {
        beforePropertyRead("deliverydate");
        return this.deliverydate;
    }

    public void setIsdelivered(Boolean isdelivered) {
        beforePropertyWrite("isdelivered", this.isdelivered, isdelivered);
        this.isdelivered = isdelivered;
    }

    public Boolean getIsdelivered() {
        beforePropertyRead("isdelivered");
        return this.isdelivered;
    }

    public void setIsvalid(Boolean isvalid) {
        beforePropertyWrite("isvalid", this.isvalid, isvalid);
        this.isvalid = isvalid;
    }

    public Boolean getIsvalid() {
        beforePropertyRead("isvalid");
        return this.isvalid;
    }

    public void setJobid(String jobid) {
        beforePropertyWrite("jobid", this.jobid, jobid);
        this.jobid = jobid;
    }

    public String getJobid() {
        beforePropertyRead("jobid");
        return this.jobid;
    }

    public void setOperat(String operat) {
        beforePropertyWrite("operat", this.operat, operat);
        this.operat = operat;
    }

    public String getOperat() {
        beforePropertyRead("operat");
        return this.operat;
    }

    public void setOrganisation(String organisation) {
        beforePropertyWrite("organisation", this.organisation, organisation);
        this.organisation = organisation;
    }

    public String getOrganisation() {
        beforePropertyRead("organisation");
        return this.organisation;
    }

    public void setTheme(String theme) {
        beforePropertyWrite("theme", this.theme, theme);
        this.theme = theme;
    }

    public String getTheme() {
        beforePropertyRead("theme");
        return this.theme;
    }

    @Override
    public Object readPropertyDirectly(String propName) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch(propName) {
            case "deliverydate":
                return this.deliverydate;
            case "isdelivered":
                return this.isdelivered;
            case "isvalid":
                return this.isvalid;
            case "jobid":
                return this.jobid;
            case "operat":
                return this.operat;
            case "organisation":
                return this.organisation;
            case "theme":
                return this.theme;
            default:
                return super.readPropertyDirectly(propName);
        }
    }

    @Override
    public void writePropertyDirectly(String propName, Object val) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch (propName) {
            case "deliverydate":
                this.deliverydate = (LocalDateTime)val;
                break;
            case "isdelivered":
                this.isdelivered = (Boolean)val;
                break;
            case "isvalid":
                this.isvalid = (Boolean)val;
                break;
            case "jobid":
                this.jobid = (String)val;
                break;
            case "operat":
                this.operat = (String)val;
                break;
            case "organisation":
                this.organisation = (String)val;
                break;
            case "theme":
                this.theme = (String)val;
                break;
            default:
                super.writePropertyDirectly(propName, val);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        writeSerialized(out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        readSerialized(in);
    }

    @Override
    protected void writeState(ObjectOutputStream out) throws IOException {
        super.writeState(out);
        out.writeObject(this.deliverydate);
        out.writeObject(this.isdelivered);
        out.writeObject(this.isvalid);
        out.writeObject(this.jobid);
        out.writeObject(this.operat);
        out.writeObject(this.organisation);
        out.writeObject(this.theme);
    }

    @Override
    protected void readState(ObjectInputStream in) throws IOException, ClassNotFoundException {
        super.readState(in);
        this.deliverydate = (LocalDateTime)in.readObject();
        this.isdelivered = (Boolean)in.readObject();
        this.isvalid = (Boolean)in.readObject();
        this.jobid = (String)in.readObject();
        this.operat = (String)in.readObject();
        this.organisation = (String)in.readObject();
        this.theme = (String)in.readObject();
    }

}
