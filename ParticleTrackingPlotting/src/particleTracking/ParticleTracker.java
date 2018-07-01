package particleTracking;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

public class ParticleTracker implements ActionListener {

	public String imagePath; //stores the path to the cell image
	public String q; // stores the path to the results csv file
	public String w; // stores the path to the mss results csv file 

	public String savePath ;


	public HashMap<Integer, ArrayList<Frame>> trajectories = new HashMap<>(); // stores the information on trajectories, indexed by trajectory number, which is the number assigned by MOSAIC/FIJI and is usually but not always the order that they appear in the file
	public ArrayList<Double> MSS_AL = new ArrayList<>(); // stores unique MSS values
	public ArrayList<Color> colors = new ArrayList<>(); // stores the array of colors used, this is more or a temporary field, for a more concrete colors array that won't be constantly changing, use the color array from the BackgroundImage JPanel object
	public HashSet<Double> MSS_SET = new HashSet<>(); // Used to get unique MSS values

	public ArrayList<Double> MSD_AL = new ArrayList<>();
	public HashSet<Double> MSD_SET = new HashSet<>();

	public ArrayList<Double> D_AL = new ArrayList<>();
	public HashSet<Double> D_SET = new HashSet<>();

	public VAR colorBy = VAR.MSS;

	public double MSSLowerBound = Integer.MAX_VALUE; // default value, used when the user does not input any number, since expected real MSS/MSD/D values can go from -1E5 to 1E5
	public double MSSUpperBound = Integer.MAX_VALUE; // for storing user inputs on filtering conditions

	public double MSDLowerBound = Integer.MAX_VALUE;
	public double MSDUpperBound = Integer.MAX_VALUE;

	public double DLowerBound = Integer.MAX_VALUE;
	public double DUpperBound = Integer.MAX_VALUE;

	public int framesLowerBound = Integer.MAX_VALUE;
	public int framesUpperBound = Integer.MAX_VALUE;

	public HashMap<String, String> trajectoryStats = new HashMap<>(); //keep track of things like highest MSS, lowest MSS, etc, using a HashMap because I might want to add other stats to track and would rather not have to keep adding fields, this way I can input stats names as keys then access all the stats at once by going through the keyset

	//The frames to save - basically all the frames except the options menu. A SavingFrames object stores a pointer to its JFrame, the JPanel of the main image, and the JPanel of the scalebar
	public ArrayList<SavingFrames> savingFrames = new ArrayList<>();

	//The starting GUI frame which will take the user input
	public OptionsMenu optionsMenu;

	/**
	 * creates the optionsFrame object and adds the plot an save buttons and actionListener
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public ParticleTracker() throws IOException, InterruptedException{
		JFrame optionsFrame = new JFrame("Options Menu");
		optionsMenu = new OptionsMenu();

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());

		JButton plotButton = new JButton("Plot");
		plotButton.setActionCommand("Plot");
		plotButton.addActionListener(this);

		JButton saveButton = new JButton("Save");
		saveButton.setActionCommand("Save");
		saveButton.addActionListener(this);

		buttonPanel.add(plotButton);
		buttonPanel.add(saveButton);
		optionsMenu.add(buttonPanel);

		optionsFrame.add(optionsMenu);
		optionsFrame.setSize(400, 400);
		optionsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		optionsFrame.setVisible(true);
	}

	public static void main(String[] args) throws IOException, InterruptedException{
		ParticleTracker particleTracker = new ParticleTracker();
		particleTracker.doNothing();
	}

	public void doNothing() {

	}

	/**
	 * storing these as key-value instead of discrete variables/fields so that I can come back easier and add soemthing/remove something, print soemthing
	 * at least I think it might be easier, I 'm bad at java
	 */
	private void initializeStatsMap() {
		trajectoryStats.put("min MSS", "" + Double.MAX_VALUE);
		trajectoryStats.put("max MSS", "" + Double.MIN_VALUE);
		trajectoryStats.put("min MSD", "" + Double.MAX_VALUE);
		trajectoryStats.put("max MSD", "" + Double.MIN_VALUE);
		trajectoryStats.put("min frames", "" + Integer.MAX_VALUE);
		trajectoryStats.put("max frames", "0");
		trajectoryStats.put("min D coeff", "" + Double.MAX_VALUE);
		trajectoryStats.put("max D coeff", "" + Double.MIN_VALUE);
		trajectoryStats.put("num trajectories", "-1");
	}

	/**
	 * calculates some stats for the recently plotted trajectories, to add a stat, just add a key/value which holds the stat of interest rather than make a new global field
	 */
	public void calculateStats(){

		trajectoryStats.put("num trajectories", "" + trajectories.size());

		for(Integer i : new ArrayList<Integer>(trajectories.keySet())){

			if(trajectories.get(i).get(0).MSS < Double.parseDouble(trajectoryStats.get("min MSS"))){
				trajectoryStats.put("min MSS", "" + trajectories.get(i).get(0).MSS);
			}
			if(trajectories.get(i).get(0).MSS > Double.parseDouble(trajectoryStats.get("max MSS"))){
				trajectoryStats.put("max MSS", "" + trajectories.get(i).get(0).MSS);
			}

			if(trajectories.get(i).get(0).MSD < Double.parseDouble(trajectoryStats.get("min MSD"))){
				trajectoryStats.put("min MSD", "" + trajectories.get(i).get(0).MSD);
			}
			if(trajectories.get(i).get(0).MSD > Double.parseDouble(trajectoryStats.get("max MSD"))){
				trajectoryStats.put("max MSD", "" + trajectories.get(i).get(0).MSD);
			}

			if(trajectories.get(i).get(0).numFrames < Integer.parseInt(trajectoryStats.get("min frames"))){
				trajectoryStats.put("min frames", "" + trajectories.get(i).get(0).numFrames);
			}
			if(trajectories.get(i).get(0).numFrames > Integer.parseInt(trajectoryStats.get("max frames"))){
				trajectoryStats.put("max frames", "" + trajectories.get(i).get(0).numFrames);
			}

			if(trajectories.get(i).get(0).diffusionCoeff < Double.parseDouble(trajectoryStats.get("min D coeff"))){
				trajectoryStats.put("min D coeff", "" + trajectories.get(i).get(0).diffusionCoeff);
			}
			if(trajectories.get(i).get(0).diffusionCoeff > Double.parseDouble(trajectoryStats.get("max D coeff"))){
				trajectoryStats.put("max D coeff", "" + trajectories.get(i).get(0).diffusionCoeff);
			}
		}

	}

	/**
	 * prints all the stats stored in the stats hashmap
	 */
	public void printStats(){
		ArrayList<String> statKeys = new ArrayList<>(trajectoryStats.keySet());
		Collections.sort(statKeys);
		for(String k : statKeys){
			System.out.println(k + " : " + trajectoryStats.get(k));
		}
	}
	/**
	 * returns a string of the stats stored in the stats map
	 */
	public String toString(){
		String rval = "";
		ArrayList<String> statKeys = new ArrayList<>(trajectoryStats.keySet());
		Collections.sort(statKeys);
		System.out.println(statKeys.size());
		for(String k : statKeys){
			rval = rval + k + " : " + trajectoryStats.get(k) + "\r\n";
		}
		return rval;
	}

	/**
	 * 
	 * @param q allTracks.csv file
	 * @param w D.csv file
	 * @param customColors color choices, '@' if none
	 * @throws IOException
	 * @throws InterruptedException
	 * 
	 * parses data from file, creates a dictionary where keys = trajectory number, value = ArrayList of Frame objects
	 * a Frame stores all the info, redundantly, of a frame and a trajectory in general (so MSS is stored for each frame even though it should be the same for all the frames of the same trajectory)
	 * 
	 * also filters the data based on the specifications, Integer.MAX_VALUE means no filter
	 * 
	 * for using default colors
	 */
	public void parseData(String q, String w, String customColors) throws IOException, InterruptedException{

		/**
		 * Trajectories file parser - 'allTracks.csv'
		 */
		String s = q;
		FileInputStream inputStream = null;		Scanner sc = null;
		try {inputStream = new FileInputStream(s);	sc = new Scanner(inputStream, "UTF-8");
		String line = "";

		line = sc.nextLine();
		String[] linee = line.split(",");

		int trajectoryIndex = 0;
		int frameIndex = 1;
		int xIndex = 2;
		int yIndex = 3;

		for(int i = 0; i < linee.length; i++){
			if(linee[i].equalsIgnoreCase("Trajectory")){
				trajectoryIndex = i;
			} else if(linee[i].equalsIgnoreCase("Frame")){
				frameIndex = i;
			} else if(linee[i].equalsIgnoreCase("x")){
				xIndex = i;
			} else if(linee[i].equalsIgnoreCase("y")){
				yIndex = i;
			}
		}

		while (sc.hasNextLine()) {try{line = sc.nextLine();}	catch(Exception e){	e.printStackTrace();}

		linee = line.split(",");
		Double[] l = new Double[linee.length];
		for(int i = 0; i < linee.length; i++){
			l[i] = Double.parseDouble(linee[i]);
		}

		if( trajectories.get(Integer.parseInt(linee[trajectoryIndex])) == null ){
			trajectories.put(Integer.parseInt(linee[trajectoryIndex]), new ArrayList<Frame>());
			trajectories.get(Integer.parseInt(linee[trajectoryIndex])).add(new Frame(
					l[trajectoryIndex].intValue(), l[frameIndex].intValue(), l[xIndex], l[yIndex]));
		} else {
			trajectories.get(Integer.parseInt(linee[trajectoryIndex])).add(new Frame(
					l[trajectoryIndex].intValue(), l[frameIndex].intValue(), l[xIndex], l[yIndex]));
		}

		}
		// note that Scanner suppresses exceptions
		if (sc.ioException() != null) {	throw sc.ioException();	}
		} finally {
			if (inputStream != null) {inputStream.close();}
			if (sc != null) {sc.close();}
		}


		/**
		 * MSS file - 'D.csv'
		 */
		s = w;

		inputStream = null;		sc = null;
		try {inputStream = new FileInputStream(s);	sc = new Scanner(inputStream, "UTF-8");
		String line = "";
		line = sc.nextLine();

		String[] linee = line.split(",");

		int trajectoryIndex = 0;
		int trajectoryLengthIndex = 1;
		int MSSIndex = 2;
		int MSDIndex = 4;
		int diffusionCoeffIndex = 6;

		for(int i = 0; i < linee.length; i++){
			if(linee[i].equalsIgnoreCase("Trajectory")){
				trajectoryIndex = i;
			} else if(linee[i].equalsIgnoreCase("Trajectory length")){
				trajectoryLengthIndex = i;
			} else if(linee[i].equalsIgnoreCase("MSS: slope")){
				MSSIndex = i;
			} else if(linee[i].equalsIgnoreCase("MSD: slope")){
				MSDIndex = i;
			} else if(linee[i].contains("Diffusion")){
				diffusionCoeffIndex = i;
			}
		}

		while (sc.hasNextLine()) {try{line = sc.nextLine();}	catch(Exception e){	e.printStackTrace();}

		linee = line.split(",");


		//basically  "SQL JOIN allTracks.csv AND D.csv ON allTracks.TRAJECTORIES
		for(Frame f : trajectories.get(Integer.parseInt(linee[trajectoryIndex]))){
			//reverse the log-transform????????
			double tempMSS = Math.pow(10, Double.parseDouble(linee[MSSIndex]));
			f.MSS = tempMSS;
			f.MSS = Double.parseDouble(linee[MSSIndex]);

			f.MSD = Double.parseDouble(linee[MSDIndex]);
			f.diffusionCoeff = Double.parseDouble(linee[diffusionCoeffIndex]);
			//take log 10 of the diffusion coeff
			f.diffusionCoeff = Math.log10(f.diffusionCoeff);
			f.numFrames = Integer.parseInt(linee[trajectoryLengthIndex]);
		}


		}
		// note that Scanner suppresses exceptions
		if (sc.ioException() != null) {	throw sc.ioException();	}
		} finally {
			if (inputStream != null) {inputStream.close();}
			if (sc != null) {sc.close();}
		}


		//filter the data based on user specifications, 'INTEGER>MAX_VALUE' means no filter, NOT INCLUSIVE 
		if(MSSLowerBound != Integer.MAX_VALUE){
			for(Integer i : new ArrayList<Integer>(trajectories.keySet())){
				if(trajectories.get(i).get(0).MSS < MSSLowerBound){
					trajectories.remove(i);
				}
			}
		}
		if(MSSUpperBound != Integer.MAX_VALUE){
			for(Integer i : new ArrayList<Integer>(trajectories.keySet())){
				if(trajectories.get(i).get(0).MSS > MSSUpperBound){
					trajectories.remove(i);
				}
			}
		}
		if(MSDLowerBound != Integer.MAX_VALUE){
			for(Integer i : new ArrayList<Integer>(trajectories.keySet())){
				if(trajectories.get(i).get(0).MSD < MSDLowerBound){
					trajectories.remove(i);
				}
			}
		}
		if(MSDUpperBound != Integer.MAX_VALUE){
			for(Integer i : new ArrayList<Integer>(trajectories.keySet())){
				if(trajectories.get(i).get(0).MSD > MSDUpperBound){
					trajectories.remove(i);
				}
			}
		}
		if(DLowerBound != Integer.MAX_VALUE){
			for(Integer i : new ArrayList<Integer>(trajectories.keySet())){
				if(trajectories.get(i).get(0).diffusionCoeff < DLowerBound){
					trajectories.remove(i);
				}
			}
		}
		if(DUpperBound != Integer.MAX_VALUE){
			for(Integer i : new ArrayList<Integer>(trajectories.keySet())){
				if(trajectories.get(i).get(0).diffusionCoeff > DUpperBound){
					trajectories.remove(i);
				}
			}
		}
		if(framesLowerBound != Integer.MAX_VALUE){
			for(Integer i : new ArrayList<Integer>(trajectories.keySet())){
				if(trajectories.get(i).get(0).numFrames < framesLowerBound){
					trajectories.remove(i);
				}
			}
		}
		if(framesUpperBound != Integer.MAX_VALUE){
			for(Integer i : new ArrayList<Integer>(trajectories.keySet())){
				if(trajectories.get(i).get(0).numFrames > framesUpperBound){
					trajectories.remove(i);
				}
			}
		}


		//Get unique MSS/MSD/D values, generate sorted MSS list. 
		for(ArrayList<Frame> alf : trajectories.values()){
			for(Frame f : alf){
				MSS_SET.add(f.MSS);
			}
		}
		MSS_AL = new ArrayList<Double>(MSS_SET);
		Collections.sort(MSS_AL);

		for(ArrayList<Frame> alf : trajectories.values()){
			for(Frame f : alf){
				MSD_SET.add(f.MSD);
			}
		}
		MSD_AL = new ArrayList<Double>(MSD_SET);
		Collections.sort(MSD_AL);

		for(ArrayList<Frame> alf : trajectories.values()){
			for(Frame f : alf){
				D_SET.add(f.diffusionCoeff);
			}
		}
		D_AL = new ArrayList<Double>(D_SET);
		Collections.sort(D_AL);


		setColors(customColors);
	}

	public void setColors(String customColors) {

		//base case for len
		double len = MSS_AL.size();
		//get which type of plot user wants
		if(this.colorBy.equals(VAR.MSS)){
			len = MSS_AL.size();
		} else if(this.colorBy.equals(VAR.MSD)){
			len = MSD_AL.size();
		} else if(this.colorBy.equals(VAR.D)){
			len = D_AL.size();
		}

		/**
		 * fill the color array, 
		 * the color array is an array of Color objects of the same size as the arraylist of interest(MSS/MSD/D)
		 * each unique MSS/MSD/D value gets a unique color
		 */
		if(customColors.equals("@")) {
			if(this.colorBy.equals(VAR.MSS)) {
				customColors = "0,255,0;0,255,255;0,0,255";
			} else if(this.colorBy.equals(VAR.MSD)) {
				customColors = "255,0,0;255,0,255;0,0,255";
			} else {
				customColors = "255,105,180;255,105,105;255,0,0;255,127,0";
			}
		}
		String[] colorSequence = customColors.split(";");
		ArrayList<ArrayList<Integer>> rgbSequence = new ArrayList<ArrayList<Integer>>();
		for(int i = 0; i < colorSequence.length; i++) {
			String[] tempRGB = colorSequence[i].split(",");
			rgbSequence.add(new ArrayList<Integer>());
			rgbSequence.get(i).add(Integer.parseInt(tempRGB[0]));
			rgbSequence.get(i).add(Integer.parseInt(tempRGB[1]));
			rgbSequence.get(i).add(Integer.parseInt(tempRGB[2]));
		}

		double r1,r2,r3,g1,g2,g3,b1,b2,b3,rStep,gStep,bStep;
		colors = new ArrayList<Color>();
		double steps = len/((double)colorSequence.length-1);
		for(int i = 1; i < colorSequence.length; i++) {
			r1 = rgbSequence.get(i-1).get(0);
			g1 = rgbSequence.get(i-1).get(1);
			b1 = rgbSequence.get(i-1).get(2);

			r2 = rgbSequence.get(i).get(0);
			g2 = rgbSequence.get(i).get(1);
			b2 = rgbSequence.get(i).get(2);

			rStep = Math.abs((r2-r1)/steps);
			gStep = Math.abs((g2-g1)/steps);
			bStep = Math.abs((b2-b1)/steps);

			r3 = r1;
			g3 = g1;
			b3 = b1;

			//			generate color array
			for(int j = 0; j < steps; j++) {
				colors.add(new Color((int)r3, (int)g3, (int)b3));
				r3 = r1>r2? Math.min(Math.max(r3-rStep, 0),255) : Math.min(Math.max(r3+rStep, 0), 255);
				g3 = g1>g2? Math.min(Math.max(g3-gStep, 0),255) : Math.min(Math.max(g3+gStep, 0), 255);
				b3 = b1>b2? Math.min(Math.max(b3-bStep, 0),255) : Math.min(Math.max(b3+bStep, 0), 255);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		/**
		 * Plotting button command
		 */
		if(e.getActionCommand().equals("Plot")){
			plot();
		} else if(e.getActionCommand().equals("Save")){
			/** 
			 * for each plot made:
			 * saves currently displayed image/window using bufferedImage
			 * saves the non-zoomed image
			 * saves the stats info
			 * saves the color info
			 * 
			 */
			System.out.println("SAVING");
			save();
			makeRfile();
			System.out.println("DONE");
		}
	}

	public void plot() {
		/**
		 * parse user input
		 */
		this.imagePath = this.optionsMenu.imagePath.getEntry();
		this.q = this.optionsMenu.allTracksPath.getEntry();
		this.w = this.optionsMenu.DPath.getEntry();
		this.savePath = this.optionsMenu.savePath.getEntry();
		if(this.savePath.endsWith("/") == false){
			this.savePath.concat("/");
		}
		this.colorBy = this.optionsMenu.getColorSelection();

		//sets which inputs correspond to 'no user input'
		ArrayList<String> defaultValues = new ArrayList<>(); defaultValues.add(""); defaultValues.add("enter"); defaultValues.add("None"); defaultValues.add("NA");

		//set fields for filtering absed on user input
		if(defaultValues.contains(this.optionsMenu.minMSS.getEntry())){
			this.MSSLowerBound = Integer.MAX_VALUE;
		} else {
			this.MSSLowerBound = Double.parseDouble(this.optionsMenu.minMSS.getEntry());
		}
		if(defaultValues.contains(this.optionsMenu.maxMSS.getEntry())){
			this.MSSUpperBound = Integer.MAX_VALUE;
		} else {
			this.MSSUpperBound = Double.parseDouble(this.optionsMenu.maxMSS.getEntry());
		}

		if(defaultValues.contains(this.optionsMenu.minMSD.getEntry())){
			this.MSDLowerBound = Integer.MAX_VALUE;
		} else {
			this.MSDLowerBound = Double.parseDouble(this.optionsMenu.minMSD.getEntry());
		}
		if(defaultValues.contains(this.optionsMenu.maxMSD.getEntry())){
			this.MSDUpperBound = Integer.MAX_VALUE;
		} else {
			this.MSDUpperBound = Double.parseDouble(this.optionsMenu.maxMSD.getEntry());
		}

		if(defaultValues.contains(this.optionsMenu.maxDCoef.getEntry())){
			this.DUpperBound = Integer.MAX_VALUE;
		} else {
			this.DUpperBound = Double.parseDouble(this.optionsMenu.maxDCoef.getEntry());
		}
		if(defaultValues.contains(this.optionsMenu.minDCoef.getEntry())){
			this.DLowerBound = Integer.MAX_VALUE;
		} else {
			this.DLowerBound = Double.parseDouble(this.optionsMenu.minDCoef.getEntry());
		}

		if(defaultValues.contains(this.optionsMenu.maxFrames.getEntry())){
			this.framesUpperBound = Integer.MAX_VALUE;
		} else {
			this.framesUpperBound = Integer.parseInt(this.optionsMenu.maxFrames.getEntry());
		}
		if(defaultValues.contains(this.optionsMenu.minFrames.getEntry())){
			this.framesLowerBound = Integer.MAX_VALUE;
		} else {
			this.framesLowerBound = Integer.parseInt(this.optionsMenu.minFrames.getEntry());
		}


		//initialization process
		initializeStatsMap();
		try {
			if(this.optionsMenu.customColors.getEntry().contains(";")) {
				parseData(q, w, this.optionsMenu.customColors.getEntry());
			}else {
				parseData(q, w, "@");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		calculateStats();
		printStats();

		//the JFrame of the main plot
		JFrame mainframe = new JFrame("Particle Tracker GUI: " + this.colorBy.toString());

		BackgroundImage panel1 = new BackgroundImage(mainframe, mainframe.getTitle());
		try {
			panel1.setBackground(ImageIO.read(new File(imagePath)));
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		//setting the arraylists for this specific plot, as the arraylists in this ParticleTracker class will change
		panel1.setMap(trajectories);
		panel1.setMSS_AL(MSS_AL);
		panel1.setMSS_SET(MSS_SET);
		panel1.setMSD_AL(MSD_AL);
		panel1.setMSD_SET(MSD_SET);
		panel1.setD_AL(D_AL);
		panel1.setD_SET(D_SET);

		panel1.setColors(colors);
		panel1.setColorBy(this.colorBy);

		mainframe.add(panel1);

		mainframe.setSize(panel1.getPreferredSize());
		mainframe.setLocationRelativeTo(null);
		//mainframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainframe.setVisible(true);


		//creates the scale bar for showing which colors correspond to which values
		JFrame scaleFrame = new JFrame(this.colorBy.toString());

		ScaleBar scalebar = new ScaleBar();

		if(this.colorBy.equals(VAR.MSS)){
			scalebar.setValues_AL(MSS_AL);
		} else if(this.colorBy.equals(VAR.MSD)){
			scalebar.setValues_AL(MSD_AL);
		} else if(this.colorBy.equals(VAR.D)){
			scalebar.setValues_AL(D_AL);
		}
		scalebar.setColors(colors);

		scaleFrame.add(scalebar);

		scaleFrame.setSize(1000, 100);
		scaleFrame.setLocation(300, 700);;
		//scaleFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		scaleFrame.setVisible(true);

		calculateStats();

		//add the current plot to an arraylist so that it can be accessed later for plotting
		savingFrames.add(new SavingFrames(this.colorBy, panel1, scaleFrame, this.toString()));
	}
 
	public void save() {
		for(SavingFrames s : savingFrames){

			BufferedImage bi = new BufferedImage(s.jPanelMain.getWidth(), s.jPanelMain.getHeight(), BufferedImage.TYPE_INT_ARGB); 
			Graphics g = bi.createGraphics();
			s.jPanelMain.paint(g);  //this == JComponent
			g.dispose();
			try{ImageIO.write(bi,"png",new File(savePath + s.name + "_IMAGE.png"));}catch (Exception ee) {ee.printStackTrace();}

			s.jPanelMain.setSize(s.jPanelMain.getPreferredSize());
			double tempZoomLevel = s.jPanelMain.zoomLevel;
			s.jPanelMain.zoomLevel = 1;
			s.jPanelMain.zoomX = 0;
			s.jPanelMain.zoomY = 0;
			s.jPanelMain.revalidate(); // I don't like java swing
			s.jPanelMain.repaint();
			s.jPanelMain.revalidate();
			s.jPanelMain.repaint();
			bi = new BufferedImage(s.jPanelMain.getWidth(), s.jPanelMain.getHeight(), BufferedImage.TYPE_INT_ARGB); 
			g = bi.createGraphics();
			s.jPanelMain.paint(g);  //this == JComponent
			g.dispose();
			try{ImageIO.write(bi,"png",new File(savePath + s.name + "_ORIGINAL_SIZE_IMAGE.png"));}catch (Exception ee) {ee.printStackTrace();}

			bi = new BufferedImage(s.jFrameScale.getWidth(), s.jFrameScale.getHeight(), BufferedImage.TYPE_INT_ARGB); 
			g = bi.createGraphics();
			s.jFrameScale.paint(g);  //this == JComponent
			g.dispose();
			try{ImageIO.write(bi,"png",new File(savePath + s.name + "_SCALE_BAR.png"));}catch (Exception ee) {ee.printStackTrace();}

			try{
				BufferedWriter output = null;
				File file = new File(savePath + s.name + "_META_DATA.txt");
				output = new BufferedWriter(new FileWriter(file));
				output.write(s.metaData);
				output.write("magnification: " + tempZoomLevel + "x");
				output.close();

			}catch(Exception ee){System.out.println("could not create file for" + s.name + "_META_DATA.txt");ee.printStackTrace();}

			String tempS = "";
			//write Color file for R
			try{
				BufferedWriter output = null;
				File file = new File(savePath + s.name + "_COLOR_DATA.csv");
				output = new BufferedWriter(new FileWriter(file));

				output.write("r,g,b," + s.name + "\r\n");
				ArrayList<Double> temp = new ArrayList<>();
				if(s.name.equals(VAR.MSS)) {
					temp = s.jPanelMain.MSS_AL;
				} else if(s.name.equals(VAR.MSD)) {
					temp = s.jPanelMain.MSD_AL;
				} else if(s.name.equals(VAR.D)) {
					temp = s.jPanelMain.D_AL;
				}

				for(int i = 0; i < Math.min(s.jPanelMain.colors.size(), temp.size()); i++) {

					output.write(s.jPanelMain.colors.get(i).getRed() + ","
							+ s.jPanelMain.colors.get(i).getGreen() + ","
							+ s.jPanelMain.colors.get(i).getBlue() + ","
							+ temp.get(i) + "\r\n");

				}

				output.close();

			}catch(Exception ee){System.out.println("could not create file for " + s.name + "_COLOR_DATA.csv\n" + tempS);ee.printStackTrace();}



		}
	}

	/**
	 * if you don't want to manually type out the new paths for everything, 
	 * this will make a new .R script with the path set that you should be able to just run
	 */
	public void makeRfile() {
		try{
			BufferedWriter output = null;
			File file = new File(savePath + "ParticleTracking.R");
			output = new BufferedWriter(new FileWriter(file));

			output.write("#set the paths to the color data files and the MSS/MSD/D csv file\r\n" + 
					"#also change the 'numBreaks' variable to the number of rbeaks in each histogram you want\r\n" + 
					"mss_colors <- read.csv(\"" + savePath + "MSS_COLOR_DATA.csv" + "\")\r\n" + 
					"msd_colors <- read.csv(\"" + savePath + "MSS_COLOR_DATA.csv"+ "\")\r\n" + 
					"d___colors <- read.csv(\"" + savePath + "D coeff_COLOR_DATA.csv"+ "\")\r\n" + 
					"D          <- read.csv(\"" + w + "\")\r\n" + 
					"\r\n" + 
					"numBreaksMSS = 100\r\n" + 
					"numBreaksMSD = 100\r\n" + 
					"numBreaksDco = 100\r\n" + 
					"\r\n" + 
					"\r\n" + 
					"colorArray = c()\r\n" + 
					"breakArray = c()\r\n" + 
					"\r\n" + 
					"i = 1\r\n" + 
					"while(i < dim(mss_colors)[1]){\r\n" + 
					"  colorArray = c(colorArray, rgb(red = mss_colors[i,1], green = mss_colors[i,2], blue = mss_colors[i,3], alpha = 255, maxColorValue = 255))\r\n" + 
					"  breakArray = c(breakArray, mss_colors[i,4])\r\n" + 
					"  i = i + dim(mss_colors)[1]/numBreaksMSS\r\n" + 
					"}\r\n" + 
					"i <- dim(mss_colors)[1]\r\n" + 
					"colorArray = c(colorArray, rgb(red = mss_colors[i,1], green = mss_colors[i,2], blue = mss_colors[i,3], alpha = 255, maxColorValue = 255))\r\n" + 
					"breakArray = c(breakArray, mss_colors[i,4])\r\n" + 
					"i = dim(mss_colors)[1]\r\n" + 
					"\r\n" + 
					"h_mss <- hist(D$MSS..slope, breaks = numBreaksMSS, plot = FALSE)\r\n" + 
					"cuts <- cut(h_mss$breaks, breakArray)\r\n" + 
					"plot(h_mss, col = colorArray[cuts], xlab = \"MSS Slope\", main = \"Histogram of MSS Slope\")\r\n" + 
					"\r\n" + 
					"\r\n" + 
					"colorArray = c()\r\n" + 
					"breakArray = c()\r\n" + 
					"\r\n" + 
					"i = 1\r\n" + 
					"while(i < dim(msd_colors)[1]){\r\n" + 
					"  colorArray = c(colorArray, rgb(red = msd_colors[i,1], green = msd_colors[i,2], blue = msd_colors[i,3], alpha = 255, maxColorValue = 255))\r\n" + 
					"  breakArray = c(breakArray, msd_colors[i,4])\r\n" + 
					"  i = i + dim(msd_colors)[1]/numBreaksMSD\r\n" + 
					"}\r\n" + 
					"i <- dim(msd_colors)[1]\r\n" + 
					"colorArray = c(colorArray, rgb(red = msd_colors[i,1], green = msd_colors[i,2], blue = msd_colors[i,3], alpha = 255, maxColorValue = 255))\r\n" + 
					"breakArray = c(breakArray, msd_colors[i,4])\r\n" + 
					"i = dim(msd_colors)[1]\r\n" + 
					"\r\n" + 
					"h_msd <- hist(D$MSD..slope, breaks = numBreaksMSD, plot = FALSE)\r\n" + 
					"cuts <- cut(h_msd$breaks, breakArray)\r\n" + 
					"plot(h_msd, col = colorArray[cuts], xlab = \"MSD Slope\", main = \"Histogram of MSD Slope\")\r\n" + 
					"\r\n" + 
					"\r\n" + 
					"colorArray = c()\r\n" + 
					"breakArray = c()\r\n" + 
					"D$Diffusion.Coefficient.D2..m.2.s. <- log10(D$Diffusion.Coefficient.D2..m.2.s.)\r\n" + 
					"\r\n" + 
					"i = 1\r\n" + 
					"while(i < dim(d___colors)[1]){\r\n" + 
					"  colorArray = c(colorArray, rgb(red = d___colors[i,1], green = d___colors[i,2], blue = d___colors[i,3], alpha = 255, maxColorValue = 255))\r\n" + 
					"  breakArray = c(breakArray, d___colors[i,4])\r\n" + 
					"  i = i + dim(d___colors)[1]/numBreaksDco\r\n" + 
					"}\r\n" + 
					"i <- dim(d___colors)[1]\r\n" + 
					"colorArray = c(colorArray, rgb(red = d___colors[i,1], green = d___colors[i,2], blue = d___colors[i,3], alpha = 255, maxColorValue = 255))\r\n" + 
					"breakArray = c(breakArray, d___colors[i,4])\r\n" + 
					"\r\n" + 
					"h___d <- hist(D$Diffusion.Coefficient.D2..m.2.s., breaks = numBreaksDco, plot = FALSE)\r\n" + 
					"cuts <- cut(h___d$breaks, breakArray)\r\n" + 
					"plot(h___d, col = colorArray[cuts], xlab = \"log10(Diffusion Coef)\", main = \"Histogram of log10(Diffusion Coef)\")\r\n" + 
					"\r\n" + 
					"\r\n" + 
					"");

			output.close();
		}catch(Exception ee){System.out.println("could not create file for particle tracking.R");ee.printStackTrace();}
	}
}
