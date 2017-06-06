package science.zxc.walkin.db;


import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * AUTH: Taosky
 * TIME: 2017/5/2 0002:下午 7:18.
 * MAIL: t@firefoxcn.net
 * DESC:
 */

public class Record extends DataSupport implements Serializable {
    private String datetime;
    private String steps;
    private String distance;
    private int id;
    private byte[] image;

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public int getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }


}
