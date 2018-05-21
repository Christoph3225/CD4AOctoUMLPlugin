package misc;

public class OctoPair<L, R> {
  private L l;
  private R r;
  public OctoPair(L l, R r) {
    this.setL(l);
    this.setR(r);
  }
  public L getL() {
    return l;
  }
  public void setL(L l) {
    this.l = l;
  }
  public R getR() {
    return r;
  }
  public void setR(R r) {
    this.r = r;
  }
  
}
