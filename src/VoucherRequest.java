public class VoucherRequest {
    private String type;
    private Integer[] Ids;
    private String hostName;
    private String port;
    private int appId;
    private String voucherDate;
    private String accStartDate;

    public VoucherRequest(String type, Integer[] ids, String hostName, String port, int appId, String voucherDate, String accStartDate) {
        this.type = type;
        Ids = ids;
        this.hostName = hostName;
        this.port = port;
        this.appId = appId;
        this.voucherDate = voucherDate;
        this.accStartDate= accStartDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer[] getIds() {
        return Ids;
    }

    public void setIds(Integer[] ids) {
        Ids = ids;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public String getVoucherDate() {
        return voucherDate;
    }

    public void setVoucherDate(String voucherDate) {
        this.voucherDate = voucherDate;
    }

    public String getAccStartDate() {
        return accStartDate;
    }

    public void setAccStartDate(String accStartDate) {
        this.accStartDate = accStartDate;
    }
}
