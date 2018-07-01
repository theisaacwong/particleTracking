package particleTracking;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class IField extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public JLabel field;
	public JTextField userEntry;
	
	public IField(String f, String u){
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		field = new JLabel(f);
		userEntry = new JTextField(u);
		this.add(field);
		this.add(userEntry);
	}
	
	public String getEntry(){
		return this.userEntry.getText();
	}
}
