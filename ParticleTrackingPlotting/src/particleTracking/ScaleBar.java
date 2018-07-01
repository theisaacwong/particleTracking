package particleTracking;
import java.awt.Graphics;
import java.util.ArrayList;
import java.awt.*;
import javax.swing.JPanel;

/**
 * 
 * @author Isaac
 *
 *	generates the bar which corresponds colors to values
 */
public class ScaleBar extends JPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ArrayList<Double> values_AL;
	public ArrayList<Color> colors;
	
	public void setValues_AL(ArrayList<Double> al){
		this.values_AL = al;
	}
	
	public void setColors(ArrayList<Color> c){
		this.colors = c;
	}
	
	@Override
	public void paintComponent(Graphics g){
		
		int numSteps = colors.size();
		//numSteps = 10;
		
		
		double xstep = ((double)this.getWidth() / (double)numSteps);
		for(int i = 0; i < colors.size(); i++){
			g.setColor(colors.get(i));
			g.fillRect((int)(i * xstep), 0, (int)(xstep + 1), (int)(0.72 * this.getHeight()));
		}
		
		g.setColor(Color.BLACK);
		numSteps = this.values_AL.size();
		numSteps = 10;
		xstep = ((double)this.getWidth() / (double)numSteps);
		for(int i = 0; i < this.values_AL.size() - 1; i++){
			g.drawString(String.format("%.5g%n", this.values_AL.get(i)), (int)(i * xstep), (int)(0.9 * this.getHeight()));
		}
		g.drawString(String.format("%.5g%n", this.values_AL.get(this.values_AL.size() - 1)), this.getWidth() - 25, (int)(0.9 * this.getHeight()));
	}

}
