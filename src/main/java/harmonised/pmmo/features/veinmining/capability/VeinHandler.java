package harmonised.pmmo.features.veinmining.capability;

public class VeinHandler implements IVeinCap{
	private double charge = 0d;
	public VeinHandler(double charge) {
		this.charge = charge;
	}

	@Override
	public double getCharge() {return charge;}
	@Override
	public void setCharge(double charge) {this.charge = charge;}
}
