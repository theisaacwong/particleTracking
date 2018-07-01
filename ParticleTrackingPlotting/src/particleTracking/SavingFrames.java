package particleTracking;
import javax.swing.JFrame;

/**
 * 
 * @author Isaac
 *
 *	stores the the frames to be written to images
 *
 */
public class SavingFrames {

	public VAR name;
	public BackgroundImage jPanelMain;
	public JFrame jFrameScale;
	public String metaData;
	
	public SavingFrames(){
		this.jPanelMain = null;
		this.jFrameScale = null;
	}
	
	public SavingFrames(VAR name, BackgroundImage jfm, JFrame jfs, String jftxt){
		this.name = name;
		this.jPanelMain = jfm;
		this.jFrameScale = jfs;
		this.metaData = jftxt;
	}
	
	public void setMetaData(String s){
		this.metaData = s;
	}
	
}
