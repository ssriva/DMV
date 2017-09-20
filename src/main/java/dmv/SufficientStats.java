package dmv;


public class SufficientStats {

	public SufficientStats() {
		// TODO Auto-generated constructor stub
	}
	
	public SufficientStats(double[][][][]c, double[][][][][] w, double l){
		this.c =c;
		this.w =w;
		this.setL(l);
	}
	
	public double[][][][] getC() {
		return c;
	}
	public void setC(double[][][][] c) {
		this.c = c;
	}

	public double[][][][][] getW() {
		return w;
	}

	public void setW(double[][][][][] w) {
		this.w = w;
	}

	public double getL() {
		return l;
	}

	public void setL(double l) {
		this.l = l;
	}

	private double[][][][]c;
	private double[][][][][] w;
	private double l;
	
}
