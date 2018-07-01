package particleTracking;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
/**
 * 
 * @author Isaac
 * 
 *	the GUI which gets the user input
 */
public class OptionsMenu extends JPanel implements ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public IField imagePath;
	public IField allTracksPath;
	public IField DPath;
	public IField minMSS;
	public IField maxMSS;
	public IField minMSD;
	public IField maxMSD;
	public IField minDCoef;
	public IField maxDCoef;
	public IField minFrames;
	public IField maxFrames;
	public JButton startButton;
	public JPanel jRadioPanel;
	public JRadioButton MSSButton;
	public JRadioButton MSDButton;
	public JRadioButton DCoButton;
	public Box box1;
	public ButtonGroup bGroup;
	public IField savePath;
	public IField customColors;
	
	public OptionsMenu(){
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		String s = "";

		imagePath = new IField("Image Path: ", "");
		allTracksPath = new IField("All Tracks csv Path: ", "");
		DPath = new IField("D csv Path", "");
		savePath = new IField("Output directory", "");
		
		minMSS = new IField("min MSS: ", s);
		maxMSS = new IField("max MSS: ", s);
		minMSD = new IField("min MSD: ", s);
		maxMSD = new IField("max MSD: ", s);
		minDCoef = new IField("min D Coeff: ", s); 
		maxDCoef = new IField("max D Coeff: ", s);
		minFrames = new IField("min frames: ", s);
		maxFrames = new IField("max frames: ", s);
		customColors = new IField("custom colors:", s);
		
		
		//JRadioButton
		jRadioPanel = new JPanel();
		jRadioPanel.setLayout(new FlowLayout());
		MSSButton = new JRadioButton("MSS"); MSSButton.setActionCommand("MSS"); MSSButton.setSelected(true);
		MSDButton = new JRadioButton("MSD"); MSDButton.setActionCommand("MSD");
		DCoButton = new JRadioButton("D coeff"); DCoButton.setActionCommand("D");
		box1 = Box.createHorizontalBox();
		box1.add(MSSButton);
		box1.add(MSDButton);
		box1.add(DCoButton);
		
		bGroup = new ButtonGroup();
		bGroup.add(MSSButton);
		bGroup.add(MSDButton);
		bGroup.add(DCoButton);
		jRadioPanel.add(box1);
		
		
		startButton = new JButton("Plot");
		
		
		this.add(imagePath);
		this.add(allTracksPath);
		this.add(DPath);
		this.add(minMSS);
		this.add(maxMSS);
		this.add(minMSD);
		this.add(maxMSD);
		this.add(minDCoef);
		this.add(maxDCoef);
		this.add(minFrames);
		this.add(maxFrames);
		this.add(customColors);
		this.add(savePath);
		this.add(jRadioPanel);
		//options.add(startButton);
		
	}
	
	public VAR getColorSelection(){
		if(this.bGroup.getSelection().getActionCommand().equals("MSS")) {
			return VAR.MSS;
		} else if(this.bGroup.getSelection().getActionCommand().equals("MSD")) {
			return VAR.MSD;
		}else if(this.bGroup.getSelection().getActionCommand().equals("D")) {
			return VAR.D;
		}
		return null;
	}
	
//	public static void main(String[] args){
//		OptionsMenu optionsMenu = new OptionsMenu();
//	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
	}

}
