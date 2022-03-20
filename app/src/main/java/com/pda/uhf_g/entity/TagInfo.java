package com.pda.uhf_g.entity;

import java.io.Serializable;

public class TagInfo implements Serializable {
    private Long index;
    private String type;
    private String epc;
    private Long count;
    private String tid;
    private String rssi;
    private String userData;
    private String reservedData;
    private String nmv2d;
    private String epcData;
    private Integer ltu27;
    private Integer ltu31;
    private String moisture;
    private int ctesius;
    private boolean isShowTid ;

    public TagInfo() {
    }

    public TagInfo(Long index, String type, String epc, String tid, String rssi) {
        this.index = index;
        this.type = type;
        this.epc = epc;
        this.tid = tid;
        this.rssi = rssi;
    }

    public Long getIndex() {
        return index;
    }

    public void setIndex(Long index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getRssi() {
        return rssi;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }

    public String getUserData() {
        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }

    public String getReservedData() {
        return reservedData;
    }

    public void setReservedData(String reservedData) {
        this.reservedData = reservedData;
    }

    public String getNmv2d() {
        return nmv2d;
    }

    public void setNmv2d(String nmv2d) {
        this.nmv2d = nmv2d;
    }

    public String getEpcData() {
        return epcData;
    }

    public void setEpcData(String epcData) {
        this.epcData = epcData;
    }

    public Integer getLtu27() {
        return ltu27;
    }

    public void setLtu27(Integer ltu27) {
        this.ltu27 = ltu27;
    }

    public Integer getLtu31() {
        return ltu31;
    }

    public void setLtu31(Integer ltu31) {
        this.ltu31 = ltu31;
    }

    public String getMoisture() {
        return moisture;
    }

    public void setMoisture(String moisture) {
        this.moisture = moisture;
    }

    public int getCtesius() {
        return ctesius;
    }

    public void setCtesius(int ctesius) {
        this.ctesius = ctesius;
    }

    public boolean getIsShowTid() {
        return isShowTid;
    }

    public void setIsShowTid(boolean isShowTid) {
        this.isShowTid = isShowTid;
    }

    @Override
    public String toString() {
        return "TagInfo{" +
                "index=" + index +
                ", type='" + type + '\'' +
                ", epc='" + epc + '\'' +
                ", count=" + count +
                ", tid='" + tid + '\'' +
                ", rssi='" + rssi + '\'' +
                ", userData='" + userData + '\'' +
                ", reservedData='" + reservedData + '\'' +
                '}';
    }
}
