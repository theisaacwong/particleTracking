package particleTracking;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class BackgroundImage extends JPanel implements ActionListener, KeyListener, MouseListener{

	//Not sure what this long fiel does
	private static final long serialVersionUID = 1L;

	//fields for zooming
	public BufferedImage image;
	public double scaleFactor = 0;
	public double zoomLevel = 1;
	public int zoomX = 0;
	public int zoomY = 0;
	
	public HashMap<Integer, ArrayList<Frame>> trajectories;
	
	public HashSet<Double> MSS_SET;
	public ArrayList<Double> MSS_AL;
	public ArrayList<Color> colors;
	
	public HashSet<Double> MSD_SET;
	public ArrayList<Double> MSD_AL;
	
	public HashSet<Double> D_SET;
	public ArrayList<Double> D_AL;
	
	public JFrame jfPointer;
	public String jfBaseTitle;
	
	public VAR colorBy;

	public BackgroundImage(){
		this.addMouseListener(this);
		this.addKeyListener(this);
	}
	
	public BackgroundImage(JFrame jfPointer, String jfBaseTitle){
		this.addMouseListener(this);
		this.addKeyListener(this);
		this.jfPointer = jfPointer;
		this.jfBaseTitle = jfBaseTitle;
	}
	
	public void setBackground(BufferedImage i){
		this.image = i;
	}
	
	public void setMap(HashMap<Integer, ArrayList<Frame>> hm){
		this.trajectories = hm;
	}
	public void setMSS_SET(HashSet<Double> m){
		this.MSS_SET = m;
	}
	public void setMSS_AL(ArrayList<Double> a){
		this.MSS_AL = a;
	}
	public void setMSD_SET(HashSet<Double> m){
		this.MSD_SET = m;
	}
	public void setMSD_AL(ArrayList<Double> a){
		this.MSD_AL = a;
	}
	public void setD_SET(HashSet<Double> m){
		this.D_SET = m;
	}
	public void setD_AL(ArrayList<Double> a){
		this.D_AL = a;
	}
	public void setColors(ArrayList<Color> c){
		this.colors = c;
	}
	public void setColorBy(VAR s) {
		this.colorBy = s;
	}

	public double getScaleFactor(int iMasterSize, int iTargetSize) {
		double dScale = 1;
		if (iMasterSize > iTargetSize) {
			dScale = (double) iTargetSize / (double) iMasterSize;
		} else {
			dScale = (double) iTargetSize / (double) iMasterSize;
		}
		return dScale;
	}

	public double getScaleFactorToFit(Dimension original, Dimension toFit) {
		double dScale = 1d;
		if (original != null && toFit != null) {
			double dScaleWidth = getScaleFactor(original.width, toFit.width);
			double dScaleHeight = getScaleFactor(original.height, toFit.height);
			dScale = Math.min(dScaleHeight, dScaleWidth);
		}
		return dScale;
	}

	@Override
	public Dimension getPreferredSize(){
		return new Dimension(this.image.getWidth(null), this.image.getHeight(null));
	}
	
	@Override
	protected void paintComponent(Graphics g) {

		if(this.trajectories == null){
			System.out.println("nOPE");
			return;
		} 

		//the window title displays the zoom level
		this.jfPointer.setTitle(this.jfBaseTitle + "      Magnification level: " + String.format("%.1f", this.zoomLevel) + "x");
		
		//draw the cell image, scaling based on user specifications
		super.paintComponent(g);
		double scaleFactor = getScaleFactorToFit(new Dimension(image.getWidth(null), image.getHeight(null)), getSize());
		scaleFactor = this.zoomLevel;
		this.scaleFactor = scaleFactor;
		int scaleWidth = (int) Math.round(image.getWidth(null) * scaleFactor);
		int scaleHeight = (int) Math.round(image.getHeight(null) * scaleFactor);
		Image scaled = image.getScaledInstance(scaleWidth, scaleHeight, Image.SCALE_SMOOTH);
		int width = getWidth() - 0;
		int height = getHeight() - 0;
		int x = (width - scaled.getWidth(this)) / 2;
		int y = (height - scaled.getHeight(this)) / 2;
		
		g.drawImage(scaled, x + this.zoomX, y + this.zoomY, this);


		/**
		 * DRAW TRAJECTORIES
		 */
		for(Integer i : new ArrayList<Integer>(trajectories.keySet())){
			
			g.setColor(Color.WHITE);
			if(this.colorBy.equals(VAR.MSS)) {
				double currMSS = trajectories.get(i).get(0).MSS;
				int k = 0;
				while(currMSS > MSS_AL.get(k)){
					k++;
				}
				g.setColor(colors.get(k));
			}else if(this.colorBy.equals(VAR.MSD)) {
				double currMSD = trajectories.get(i).get(0).MSD;
				int k = 0;
				while(currMSD > MSD_AL.get(k)){
					k++;
				}
				g.setColor(colors.get(k));
			}else if(this.colorBy.equals(VAR.D)) {
				double currD = trajectories.get(i).get(0).diffusionCoeff;
				int k = 0;
				while(currD > D_AL.get(k)){
					k++;
				}
				g.setColor(colors.get(k));
			} else {
				System.out.println("eror");
			}
			
			for(int j = 0; j < trajectories.get(i).size() - 1; j++){
				this.scaleFactor = this.zoomLevel;
				int startX = (int)(trajectories.get(i).get(j).x * this.scaleFactor + ((double)this.getWidth() - (double)scaled.getWidth(null))/2);
				int startY = (int)(trajectories.get(i).get(j).y * this.scaleFactor + ((double)this.getHeight() - (double)scaled.getHeight(null))/2);
				int endX = (int)(trajectories.get(i).get(j+1).x * this.scaleFactor + ((double)this.getWidth() - (double)scaled.getWidth(null))/2);
				int endY = (int)(trajectories.get(i).get(j+1).y * this.scaleFactor + ((double)this.getHeight() - (double)scaled.getHeight(null))/2);
				g.drawLine(startX + zoomX, startY + zoomY, endX + zoomX, endY + zoomY);
				
			}
		}
		
		
	}
	
	public void writeColorsToFile() {
		
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseClicked(MouseEvent arg0) {
		this.grabFocus();
	}
	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void keyPressed(KeyEvent arg0) {
		
	}
	
	/**
	 * defines user input controls and functions
	 */
	@Override
	public void keyReleased(KeyEvent arg0) {
		if(arg0.getKeyChar() == 'q'){
			this.zoomLevel += 0.3;
		} else if(arg0.getKeyChar() == 'e'){
			this.zoomLevel -= 0.3;
		} else if(arg0.getKeyChar() == 'w'){
			this.zoomY += 20;
		} else if(arg0.getKeyChar() == 's'){
			this.zoomY -= 20;
		} else if(arg0.getKeyChar() == 'a'){
			this.zoomX += 20;
		} else if(arg0.getKeyChar() == 'd'){
			this.zoomX -= 20;
		} else if(arg0.getKeyChar() == 'c'){
			this.zoomLevel += 1;
		}else if(arg0.getKeyChar() == ' '){
			this.zoomLevel = 1;
			this.zoomX = 1;
			this.zoomY = 1;
		}
	}
	@Override
	public void keyTyped(KeyEvent arg0) {
		
	}
}
