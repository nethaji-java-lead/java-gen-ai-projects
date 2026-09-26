package com.tscnet.dailyprocess.model.jaxb;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "Balancing_MarketDocument", namespace = "urn:iec62325.351:tc57wg16:451-6:balancingdocument:4:4")
@XmlAccessorType(XmlAccessType.FIELD)
public class BalancingMarketDocument {

    @XmlElement(namespace = "urn:iec62325.351:tc57wg16:451-6:balancingdocument:4:4")
    private String mRID;

    @XmlElement(namespace = "urn:iec62325.351:tc57wg16:451-6:balancingdocument:4:4")
    private int revisionNumber;

    @XmlElement(namespace = "urn:iec62325.351:tc57wg16:451-6:balancingdocument:4:4")
    private String type;

    @XmlElement(name = "TimeSeries", namespace = "urn:iec62325.351:tc57wg16:451-6:balancingdocument:4:4")
    private List<TimeSeries> timeSeries;

    // Getters and Setters
    public String getMRID() { return mRID; }
    public void setMRID(String mRID) { this.mRID = mRID; }

    public int getRevisionNumber() { return revisionNumber; }
    public void setRevisionNumber(int revisionNumber) { this.revisionNumber = revisionNumber; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<TimeSeries> getTimeSeries() { return timeSeries; }
    public void setTimeSeries(List<TimeSeries> timeSeries) { this.timeSeries = timeSeries; }
}