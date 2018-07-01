package particleTracking;

public class Frame {

	public int trajectory;
	public int frame;
	public double x;
	public double y;
	public double MSS;
	public double MSD;
	public double diffusionCoeff;
	public int numFrames; // could also use trajectories.get(__).size() but this is more clear
	
	
	public Frame(){
		this.trajectory = -1;
		this.frame = -1;
		this.x = -1;
		this.y = -1;
		this.MSS = -1;
		this.diffusionCoeff = -1;
		this.MSD = -1;
	}
	
	public Frame(int t, int f, double x, double y){
		this.trajectory = t;
		this.frame = f;
		this.x = x;
		this.y = y;
		this.MSS = -1;
		this.diffusionCoeff = -1;
		this.MSD = -1;
	}
	
	public String toString(){
		return this.trajectory + "," + this.frame + "," + this.x + "," + this.y + "," + this.MSS + "," + this.MSD + "," + this.diffusionCoeff;
	}
	
	
}
