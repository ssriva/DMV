package dmv;

public class ModelParameters {

	public ModelParameters() {
		// TODO Auto-generated constructor stub
	}
	
	public ModelParameters(double[][][]ps, double[][][]pa){
		this.pattach=pa;
		this.pstop=ps;
	}
	
	public double[][][] getPstop() {
		return pstop;
	}
	public void setPstop(double[][][] pstop) {
		this.pstop = pstop;
	}

	public double[][][] getPattach() {
		return pattach;
	}

	public void setPattach(double[][][] pattach) {
		this.pattach = pattach;
	}

	private double[][][] pstop;
	private double[][][] pattach;
	
	

}
