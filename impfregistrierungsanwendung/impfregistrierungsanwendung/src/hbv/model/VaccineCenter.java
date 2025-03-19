package hbv.model;

public class VaccineCenter {
  private int centerId;
  private String centerName;

  public VaccineCenter(int centerId, String centerName) {
    this.centerId = centerId;
    this.centerName = centerName;
  }

  public int getCenterId() {
    return centerId;
  }

  public String getCenterName() {
    return centerName;
  }
}
