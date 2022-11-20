/**
 * PasswordWithProxyDialog.java
 * 
 * Reworks the parent PasswordDialog class, 
 * creating compartmentalized panes for all. 
 * Extending password dialog to retain some 
 * convenience methods and avoid breaking 
 * existing functionality.
 * 
 * Known issues: 
 * <ul>
 * <li>This class should be able to directly extend JDialog, but
 * is not properly displaying the dialog window when it does so.
 * <li>The regular login is displaying in the center, rather than 
 * to the left, despite the layout settings.
 * 
 */
package com.b4utrade.helper;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

import com.Ostermiller.util.PasswordDialog;

public class PasswordWithProxyDialog extends PasswordDialog {

	private static String YES = "Yes";
	private static String NO = "No";

	/**
	 * Title for the regular login pane. 
	 * Default value is "Enter Username & Password".
	 */
	protected String loginTitle;

	/**
	 * Label for the username field in both the regular and proxy login sections.
	 */
	protected JLabel usernameLabel;
	/**
	 * Username input field.
	 */
	protected JTextField username;
	/**
	 * Label for the password field in both the regular and proxy login sections.
	 */
	protected JLabel passwordLabel;
	/**
	 * Password input field.
	 */
	protected JPasswordField password;

	/**
	 * Header label for the entire proxy section. 
	 * Default value is "For Proxy Users Only".
	 */
	protected String proxyTitle;
	/**
	 * Login via proxy server question. This label cannot be changed.
	 */
	protected JLabel proxyServerLabel;
	/**
	 * Button group for login via proxy server question.
	 */
	protected ButtonGroup proxyServerGroup;
	/**
	 * Login via proxy server = "YES" 
	 */
	protected JRadioButton proxyServerYes;
	
	/**
	 * Login via proxy server = "NO"
	 */
	protected JRadioButton proxyServerNo;

	/**
	 * Label for the proxy http field.
	 */
	protected JLabel proxyHttpLabel;
	
	/**
	 * Proxy http.  No validation at this time!
	 */
	protected JTextField proxyHttp;
	
	/**
	 * Label for the proxy port field.
	 */
	protected JLabel proxyPortLabel;
	
	/**
	 * Proxy http.  No validation at this time!
	 */
	protected JTextField proxyPort;
	
	/**
	 * Proxy login required question. This label cannot be changed.
	 */
	protected JLabel proxyRequiredLabel;
	/**
	 * Button group for proxy login required question.
	 */
	protected ButtonGroup proxyReqGroup;
	/**
	 * Proxy login required = "YES"
	 */
	protected JRadioButton proxyReqYes;
	/**
	 * Proxy login required = "NO"
	 */
	protected JRadioButton proxyReqNo;

	/**
	 * Header title for proxy login section.
	 */
	protected String proxyLoginTitle;
	
	/**
	 * The label for the field in which the name is typed.
	 */
	protected JLabel proxyUsernameLabel;
	/**
	 * The label for the field in which the password is typed.
	 */
	protected JLabel proxyPasswordLabel;
	/**
	 * Proxy login username.
	 */
	protected JTextField proxyUsername;
	/**
	 * Proxy login password.
	 */
	protected JPasswordField proxyPassword;

	protected JButton okButton;
	protected JButton cancelButton;
	
	/* Layout containers */
	/**
	 * Bottom-most panel for the new compartmentalized layout.
	 */
	protected JPanel bottomPane;
	/**
	 * Bottom-most layout for the new compartmentalized layout.
	 */
	protected GridBagLayout bottomLayout;
	/**
	 * Bottom-most constraint for the new compartmentalized layout.
	 */
	protected GridBagConstraints bottomConstraint;
	
	/**
	 * Panel containing the normal login info: username & password.
	 */
	protected JPanel loginPane;
	
	/**
	 * Panel containing all of the proxy info.
	 */
	protected JPanel proxyPane;
	
	/**
	 * Panel for the proxy username & login 
	 */
	protected JPanel proxyLoginPane;
	/**
	 * Panel that contains the OK & Cancel buttons.
	 */
	protected JPanel buttonPane;
	
	
	
	/**
	 * Get the title of the login section.
	 * @return the loginTitle
	 */
	public String getLoginTitle() {
		return loginTitle;
	}

	/**
	 * Set the title of the login section.
	 * @param loginTitle the loginTitle to set
	 */
	public void setLoginTitle(String loginTitle) {
		this.loginTitle = loginTitle;
		((TitledBorder)((CompoundBorder)loginPane.getBorder()).getOutsideBorder()).setTitle(loginTitle);
		pack();
	}
	
	/**
	 * Get the username label's text.
	 * @return the usernameLabel
	 */
	public String getUsernameLabel() {
		return usernameLabel.getText();
	}

	/**
	 * Set the username label's text.
	 * @param usernameLabel the usernameLabel to set
	 */
	public void setUsernameLabel(String usernameLabel) {
		this.usernameLabel.setText(usernameLabel + " ");
		pack();
	}

	/**
	 * Get the input from the username field.
	 * @return the username
	 */
	public String getUsername() {
		return username.getText();
	}

	/**
	 * Set a default value for the username input field.
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username.setText(username);
	}
	
	/**
	 * Get the password label's text.
	 * @return the usernameLabel
	 */
	public String getPasswordLabel()
	{
		return this.passwordLabel.getText();
	}
	
	/**
	 * Set the password label's text.
	 * @return the usernameLabel
	 */
	public void setPasswordLabel(String passwordLabel)
	{
		this.passwordLabel.setText(passwordLabel);
	}
	
	/**
	 * Get the input from the password field.
	 * @return the password
	 */
	public char[] getPassword() {
		return password.getPassword();
	}

	/**
	 * Set a default value for the password input field.
	 * @param password the password to set
	 */
	public void setPassword(JPasswordField password) {
		this.password = password;
	}

	/**
	 * Get the title of the proxy section.
	 * @return the proxyTitle
	 */
	public String getProxyTitle() {
		return proxyTitle;
	}

	/**
	 * Set the title of the proxy section.
	 * @param proxyTitle the proxyTitle to set
	 */
	public void setProxyTitle(String proxyTitle) {
		this.proxyTitle = proxyTitle;
		((TitledBorder)((CompoundBorder)loginPane.getBorder()).getOutsideBorder()).setTitle(proxyTitle);
		pack();
	}
	
	/**
	 * Is "Login via proxy server" = "YES" radio button selected?
	 * @return is the radio button selected?
	 */
	public boolean isProxyServerYesSelected() {
		return proxyServerYes.isSelected();
	}

	/**
	 * Select "Login via proxy server" = "YES".
	 * @param selected
	 */
	public void selectProxyServerYes(boolean selected) {
		this.proxyServerYes.setSelected(selected);
		enableProxyRequiredLine(selected);
	}

	/**
	 * Is "Login via proxy server" = "NO" radio button selected?
	 * @return is the radio button selected?
	 */
	public boolean isProxyServerNoSelected() {
		return proxyServerNo.isSelected();
	}

	/**
	 * Select "Login via proxy server" = "NO".
	 * @param selected
	 */
	public void selectProxyServerNo(boolean selected) {
		this.proxyServerNo.setSelected(selected);
		enableProxyRequiredLine(!selected);
	}

	/**
	 * @return
	 */
	public String getProxyHttpLabel() {
		return this.proxyHttpLabel.getText();
	}
	
	/**
	 * @param proxyHttpLabel
	 */
	public void setProxyHttpLabel(String proxyHttpLabel) {
		this.proxyHttpLabel.setText(proxyHttpLabel);
		pack();
	}
	
	/**
	 * @return
	 */
	public String getProxyHttp() {
		return this.proxyHttp.getText();
	}
	
	/**
	 * @param proxyHttp
	 */
	public void setProxyHttp(String proxyHttp) {
		this.proxyHttp.setText(proxyHttp);
	}
	
	/**
	 * @return
	 */
	public String getProxyPortLabel() {
		return this.proxyPortLabel.getText();
	}
	
	/**
	 * @param proxyPortLabel
	 */
	public void setProxyPortLabel(String proxyPortLabel) {
		this.proxyPortLabel.setText(proxyPortLabel);
	}
	
	/**
	 * @return
	 */
	public String getProxyPort() {
		return this.proxyPort.getText();
	}
	
	/**
	 * @param proxyPort
	 */
	public void setProxyPort(String proxyPort) {
		this.proxyPort.setText(proxyPort);
	}
	
	/**
	 * Is "Proxy login required" = "YES" radio button selected?
	 * @return is the radio button selected?
	 */
	public boolean isProxyReqYesSelected() {
		return proxyReqYes.isSelected();
	}

	/**
	 * Select "Proxy login required" = "YES".
	 * @param selected
	 */
	public void selectProxyReqYes(boolean selected) {
		this.proxyReqYes.setSelected(selected);
		enableProxyLogin(selected);
	}

	/**
	 * Is "Proxy login required" = "NO" radio button selected?
	 * @return is the radio button selected?
	 */
	public boolean isProxyReqNoSelected() {
		return proxyReqNo.isSelected();
	}

	/**
	 * Select "Proxy login required" = "NO".
	 * @param selected
	 */
	public void selectProxyReqNo(boolean selected) {
		this.proxyReqNo.setSelected(selected);
		enableProxyLogin(!selected);
	}

	
	/**
	 * Get the title of proxy login section.
	 * @return the proxyLoginTitle
	 */
	public String getProxyLoginTitle() {
		return proxyLoginTitle;
	}

	/**
	 * Set the title of proxy login section.
	 * @param proxyLoginTitle the proxyLoginTitle to set
	 */
	public void setProxyLoginTitle(String proxyLoginTitle) {
		this.proxyLoginTitle = proxyLoginTitle;
		((TitledBorder)((CompoundBorder)proxyLoginPane.getBorder()).getOutsideBorder()).setTitle(proxyLoginTitle);
		pack();
	}

	/**
	 * Set the text color for the proxy login section.
	 * @param color
	 */
	public void setProxyLoginTitleColor(Color color) {
		((TitledBorder)((CompoundBorder)proxyLoginPane.getBorder()).getOutsideBorder()).setTitleColor(color);
		pack();  //pack won't force a repaint unless window size has been altered.
		this.repaint();
	}
	
	/**
	 * Get the proxy username label's text.
	 * @return the usernameLabel
	 */
	public String getProxyUsernameLabel() {
		return proxyUsernameLabel.getText();
	}

	/**
	 * Set the proxy username label's text.
	 * @param usernameLabel the usernameLabel to set
	 */
	public void setProxyUsernameLabel(String proxyUsernameLabel) {
		this.proxyUsernameLabel.setText(proxyUsernameLabel + " ");
	}

	/**
	 * Get the proxy password label's text.
	 * @return the passwordLabel
	 */
	public String getProxyPasswordLabel() {
		return proxyPasswordLabel.getText();
	}

	/**
	 * Set the proxy password label's text.
	 * @param passwordLabel the passwordLabel to set
	 */
	public void setProxyPasswordLabel(String proxyPasswordLabel) {
		this.proxyPasswordLabel.setText(proxyPasswordLabel + " ");
	}
	
	/**
	 * Get the input from the proxy username field.
	 * @return the proxyUsername
	 */
	public String getProxyUsername() {
		return proxyUsername.getText();
	}

	/**
	 * Set a default value for the proxy username input field.
	 * @param proxyUsername the proxyUsername to set
	 */
	public void setProxyUsername(String proxyUsername) {
		this.proxyUsername.setText(proxyUsername);
	}

	/**
	 * Get the input from the proxy username field.
	 * @return the proxyPassword
	 */
	public String getProxyPassword() {
		return new String(proxyPassword.getPassword());
	}

	/**
	 * Set a default value for the password input field.
	 * @param proxyPassword the proxyPassword to set
	 */
	public void setProxyPassword(String proxyPassword) {
		this.proxyPassword.setText(proxyPassword);
	}

	public PasswordWithProxyDialog(Frame parent, String title)
	{
		super(parent, title);
		
		if (parent != null){
			setLocationRelativeTo(parent);
		}
		// super calls dialogInit, so we don't need to do it again.
	}

	public PasswordWithProxyDialog(Frame parent)
	{
		this(parent, null);
	}

	public PasswordWithProxyDialog()
	{
		this(null, null);
	}
	
	protected boolean pressed_OK = false;
	
	/**
	 * Finds out if user used the OK button or an equivalent action
	 * to close the dialog.
	 * Pressing enter in the password field may be the same as
	 * 'OK' but closing the dialog and pressing the cancel button
	 * are not.
	 *
	 * @return true if the the user hit OK, false if the user canceled.
	 */
	/*
	 * @see com.Ostermiller.util.PasswordDialog#okPressed()
	 */
	public boolean okPressed(){
		return pressed_OK;
	}
	
	/**
	 * Shows the dialog and returns true if the user pressed ok.
	 *
	 * @return true if the the user hit OK, false if the user canceled.
	 */
	/*
	 * @see com.Ostermiller.util.PasswordDialog#showDialog()
	 */
	public boolean showDialog(){
		setVisible(true);
		return okPressed();
	}
	
	/**
	 * Enable or disable the proxy http fields and the "Proxy login required? " radio line. 
	 * @param enable
	 */
	protected void enableProxyRequiredLine(boolean enable)
	{
		//proxy http fields
		proxyHttpLabel.setEnabled(enable);
		proxyHttp.setEnabled(enable);
		proxyPortLabel.setEnabled(enable);
		proxyPort.setEnabled(enable);
		
		//proxy login fields
		proxyRequiredLabel.setEnabled(enable);
		proxyReqYes.setEnabled(enable);
		proxyReqNo.setEnabled(enable);
		enableProxyLogin(enable && proxyReqYes.isSelected());
		pack();
	}
	
	/**
	 * Enable or disable the proxy username & password section.
	 * @param enable
	 */
	protected void enableProxyLogin(boolean enable)
	{
		proxyUsernameLabel.setEnabled(enable);
		proxyPasswordLabel.setEnabled(enable);
		proxyUsername.setEnabled(enable);
		proxyPassword.setEnabled(enable);
		//"Enable" the border title, no need to call pack(), as set..Color will do so.
		if(enable) { this.setProxyLoginTitleColor(Color.BLACK); }
		else { this.setProxyLoginTitleColor(Color.GRAY); }
	}

	/**
	 * Called by constructors to initialize the dialog.
	 */
	/* (non-Javadoc)
	 * @see com.Ostermiller.util.PasswordDialog#dialogInit()
	 */
	protected void dialogInit()
	{	
		//TODO: create a property file and resource bundle?

		//initialize the titles
		loginTitle = "Enter Username & Password";
		proxyTitle = "For Proxy Users Only";
		proxyLoginTitle = "Proxy Username & Password";
		
		//initialize the swing components
		usernameLabel = new JLabel("Username ");
		passwordLabel = new JLabel("Password ");
		username = new JTextField("", 20);
		password = new JPasswordField("", 20);
		
		proxyServerLabel = new JLabel("Log in via proxy server? ");
		proxyServerYes = new JRadioButton(YES);
		proxyServerYes.setActionCommand(YES);
		proxyServerNo = new JRadioButton(NO);
		proxyServerNo.setActionCommand(NO);
		proxyServerNo.setSelected(true);
		
		//TODO: FINISH INIALIZING & ADD LISTENERS
		proxyHttpLabel = new JLabel("HTTP:  ");
		proxyPortLabel = new JLabel("Port:  ");

		proxyHttp = new JTextField("", 13);
		proxyPort = new JTextField("", 6);
		//set a default proxy port
		proxyPort.setText("80");

		proxyRequiredLabel = new JLabel("Proxy login required? ");
		proxyReqYes = new JRadioButton(YES);
		proxyReqYes.setActionCommand(YES);
		proxyReqNo = new JRadioButton(NO);
		proxyReqNo.setActionCommand(NO);

		proxyReqNo.setSelected(true);

		proxyUsernameLabel = new JLabel("Username ");
		proxyPasswordLabel = new JLabel("Password ");
		proxyUsername = new JTextField("", 20);
		proxyPassword = new JPasswordField("", 20);

		okButton = new JButton("OK");
		cancelButton = new JButton("CANCEL");
				
		KeyListener keyListener = new KeyAdapter() {
			public void keyPressed(KeyEvent e){
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE ||
						(e.getSource() == cancelButton
						&& e.getKeyCode() == KeyEvent.VK_ENTER)){
					pressed_OK = false;
					PasswordWithProxyDialog.this.setVisible(false);
				}
				if (e.getSource() == okButton &&
						e.getKeyCode() == KeyEvent.VK_ENTER){
					pressed_OK = true;
					PasswordWithProxyDialog.this.setVisible(false);
				}
			}
		};
		
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e){
				Object source = e.getSource();
				if (source == username || source == proxyUsername){
					// the user pressed enter in a username field, continue to next (password) field.
					((Component)source).transferFocus();
				} 
				else if(e.getSource() == proxyServerYes || e.getSource() == proxyServerNo) {
					enableProxyRequiredLine(isProxyServerYesSelected());
				}
				else if(e.getSource() == proxyReqYes || e.getSource() == proxyReqNo) {
					enableProxyLogin(isProxyReqYesSelected());
				}
				else {
					// other actions close the dialog.
					pressed_OK = (source == password || source == okButton);
					PasswordWithProxyDialog.this.setVisible(false);
				}
			}
		};
		
		super.dialogInit();
		getContentPane().removeAll();
		addKeyListener(keyListener);
		username.addActionListener(actionListener);
		username.addKeyListener(keyListener);
		password.addActionListener(actionListener);
		password.addKeyListener(keyListener);
		proxyServerYes.addActionListener(actionListener);
		proxyServerNo.addActionListener(actionListener);
//		proxyHttp.addActionListener(actionListener);
//		proxyHttp.addKeyListener(keyListener);
//		proxyPort.addActionListener(actionListener);
//		proxyPort.addKeyListener(keyListener);
		proxyReqYes.addActionListener(actionListener);
		proxyReqNo.addActionListener(actionListener);
		proxyUsername.addActionListener(actionListener);
		proxyUsername.addKeyListener(keyListener);
		proxyPassword.addActionListener(actionListener);
		proxyPassword.addKeyListener(keyListener);
		okButton.addActionListener(actionListener);
		okButton.addKeyListener(keyListener);
		cancelButton.addActionListener(actionListener);
		cancelButton.addKeyListener(keyListener);
		
		proxyServerGroup = new ButtonGroup();
		proxyServerGroup.add(proxyServerYes);
		proxyServerGroup.add(proxyServerNo);

		proxyReqGroup = new ButtonGroup();
		proxyReqGroup.add(proxyReqYes);
		proxyReqGroup.add(proxyReqNo);
		
		//DO LAYOUT
		bottomLayout = new GridBagLayout();
		bottomPane= new JPanel(bottomLayout);
		bottomPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));

		bottomConstraint = new GridBagConstraints();
		bottomConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		bottomConstraint.fill = GridBagConstraints.HORIZONTAL;
		//single column, this value will never change during the init
		bottomConstraint.gridx = 0;
		bottomConstraint.gridy = 0;
		
		GridBagLayout nlpl = new GridBagLayout();
		loginPane = new JPanel(nlpl);
		
		loginPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(loginTitle),
				BorderFactory.createEmptyBorder(10, 20, 5, 20)));
		
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.LINE_START;
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.gridx = 0;
		c.gridy = 0;
		nlpl.setConstraints(usernameLabel, c);
		loginPane.add(usernameLabel);
		c.gridx = 1;
		nlpl.setConstraints(username, c);
		loginPane.add(username);
		c.gridx = 0;
		c.gridy = 1;
		nlpl.setConstraints(passwordLabel, c);
		loginPane.add(passwordLabel);
		c.gridx = 1;
		nlpl.setConstraints(password, c);
		loginPane.add(password);

		//ADD NORMAL LOGIN PANE
		bottomLayout.setConstraints(loginPane, bottomConstraint);
		bottomPane.add(loginPane);
		
		//put ALL proxy info into proxy layout
		GridBagLayout proxyLayout = new GridBagLayout();
		proxyPane = new JPanel(proxyLayout);
		proxyPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(proxyTitle),
				BorderFactory.createEmptyBorder(10, 20, 5, 20)));

		GridBagConstraints c1 = new GridBagConstraints();
		int c1y = 0; //use this value to increase c1.gridy.
		c1.insets.top = 5;
		c1.insets.bottom = 5;
		c1.anchor = GridBagConstraints.FIRST_LINE_START;
		c1.gridx = 0;
		c1.gridy = c1y;
		
		proxyLayout.setConstraints(proxyServerLabel, c1);
		proxyPane.add(proxyServerLabel);

		c1.gridx = 1;
		proxyLayout.setConstraints(proxyServerYes, c1);
		proxyPane.add(proxyServerYes);
		c1.gridx = 2;
		proxyLayout.setConstraints(proxyServerNo, c1);
		proxyPane.add(proxyServerNo);

		//BEGIN PROXY HTTP LAYOUT
		c1.gridx = 0;
		c1.gridy = ++c1y;
		c1.anchor = GridBagConstraints.FIRST_LINE_END;
		proxyLayout.setConstraints(proxyHttpLabel, c1);
		proxyPane.add(proxyHttpLabel);
		c1.gridx = 1;
		c1.gridwidth = 2;
		c1.anchor = GridBagConstraints.FIRST_LINE_START;
		proxyLayout.setConstraints(proxyHttp, c1);
		proxyPane.add(proxyHttp);
		c1.gridx = 0;
		c1.gridwidth = 1;
		c1.gridy = ++c1y;
		c1.anchor = GridBagConstraints.FIRST_LINE_END;
		proxyLayout.setConstraints(proxyPortLabel, c1);
		proxyPane.add(proxyPortLabel);
		c1.gridwidth = 2;
		c1.gridx = 1;
		c1.anchor = GridBagConstraints.FIRST_LINE_START;
		proxyLayout.setConstraints(proxyPort, c1);
		proxyPane.add(proxyPort);
		c1.gridwidth = 1;		
		//END PROXY HTTP LAYOUT
		
		c1.gridx = 0;
		c1.gridy = ++c1y;
		proxyLayout.setConstraints(proxyRequiredLabel, c1);
		proxyPane.add(proxyRequiredLabel);

		c1.gridx = 1;
		proxyLayout.setConstraints(proxyReqYes, c1);
		proxyPane.add(proxyReqYes);
		c1.gridx = 2;
		proxyLayout.setConstraints(proxyReqNo, c1);
		proxyPane.add(proxyReqNo);

		//proxy login inset panel
		GridBagLayout proxyLoginLayout = new GridBagLayout();
		proxyLoginPane = new JPanel(proxyLoginLayout);
		proxyLoginPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(proxyLoginTitle),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		
		GridBagConstraints c2 = new GridBagConstraints();
		c2.anchor = GridBagConstraints.FIRST_LINE_START;
		c2.insets.top = 5;
		c2.insets.bottom = 5;
		c2.gridx = 0;
		c2.gridy = 0;

		proxyLoginLayout.setConstraints(proxyUsernameLabel, c2);
		proxyLoginPane.add(proxyUsernameLabel);
		c2.gridx = 1;
		proxyLoginLayout.setConstraints(proxyUsername, c2);
		proxyLoginPane.add(proxyUsername);
		
		c2.gridx = 0;
		c2.gridy = 1;
		proxyLoginLayout.setConstraints(proxyPasswordLabel, c2);
		proxyLoginPane.add(proxyPasswordLabel);
		c2.gridx = 1;
		proxyLoginLayout.setConstraints(proxyPassword, c2);
		proxyLoginPane.add(proxyPassword);
		
		//add proxy login pane to proxy pane
		c1.gridx = 0;
		c1.gridy = ++c1y;
		c1.gridwidth = 3;
		proxyLayout.setConstraints(proxyLoginPane, c1);
		proxyPane.add(proxyLoginPane);
		
		//ADD PROXY PANE
		bottomConstraint.gridy = 1;
		bottomLayout.setConstraints(proxyPane, bottomConstraint);
		bottomPane.add(proxyPane);

		//create the button pane
		buttonPane = new JPanel();
		buttonPane.add(okButton);
		buttonPane.add(cancelButton);
		
		//ADD BUTTON PANE
		bottomConstraint.gridy = 2;
		bottomConstraint.gridwidth = GridBagConstraints.REMAINDER;
		bottomConstraint.anchor = GridBagConstraints.CENTER;
		bottomLayout.setConstraints(buttonPane, bottomConstraint);
		bottomPane.add(buttonPane);
		
		//add new compartmentalized layout
		getContentPane().add(bottomPane);
		
		enableProxyRequiredLine(isProxyServerYesSelected());
		//enableProxyLogin(isProxyReqYesSelected());

		pack();

	}//end method dialogInit

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PasswordWithProxyDialog p = new PasswordWithProxyDialog(null, "Quodd.com - Live Update");
		p.setLocation(350,300);
		p.setLoginTitle("QUODD Username & Password");

		if(p.showDialog()){
			System.out.println("Username: " + p.getUsername());
			System.out.println("Password: " + p.getPassword());
			System.out.println("Proxy HTTP: " + p.getProxyHttp());
			System.out.println("Proxy Port: " + p.getProxyPort());
			System.out.println("Proxy Username: " + p.getProxyUsername());
			System.out.println("Proxy Password: " + p.getProxyPassword());
		} else {
			System.out.println("User selected cancel");
		}
		p.dispose();
		p = null;
		System.exit(0);
	}

}
