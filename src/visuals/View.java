package visuals;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.*;
import java.io.IOException;
import java.util.Hashtable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import sap.SAPModel;
import javax.swing.text.DefaultCaret;

@SuppressWarnings("serial")
public class View extends JPanel
		implements interfaces.LogObserver, ActionListener, interfaces.ClockObserver, ChangeListener {

	// Components needed by the widget
	private SAPModel model;
	private JLabel clockStatusLabel;
	private GridBagConstraints c;
	private JTextArea logLabel;
	private JButton resetButton;
	private JButton clockButton;
	private JButton playButton;
	private SAPViewWidget viewWidget;
	private RAMViewWidget ramWidget;
    public JSlider speedSlider;
	private SevenSegDisplay disp;

	// Needed for the auto-runner
	private boolean isAutoRunning;
	public BackgroundRunner bRunner;

	// Constants
	public static final Color VIEW_BACKGROUND_COLOR = new Color(225, 246, 203);

	public View() throws IOException {
		this.model = new SAPModel();
		this.setBackground(VIEW_BACKGROUND_COLOR);

		// Set the Layout
		this.setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.VERTICAL;

		// Add the SAP View Widget (Middle part)
		this.disp = new SevenSegDisplay(this.model.getOut().getVal());
		this.ramWidget = new RAMViewWidget(this.model, this);
		this.viewWidget = new SAPViewWidget(this.model, disp, this.ramWidget);
		c.gridx = 1;
		c.gridy = 0;
		c.gridheight = 8;
		this.add(viewWidget, c);

		// Add the RAM View Widget
		c.gridx = 0;
		c.gridy = 0;
		this.add(ramWidget, c);

		// Display the status of the clock
		clockStatusLabel = new JLabel("Clock: " + (sap.Clock.getClock().getStatus() ? "HIGH" : "LOW"));
		c.gridx = 3;
		c.gridy = 0;
		c.gridheight = 1;
		this.add(clockStatusLabel, c);

		// Add reset button
		resetButton = new JButton("Reset");
		resetButton.setActionCommand("resetButtonClicked");
		resetButton.addActionListener(this);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 3;
		c.gridy = 1;
		c.gridheight = 1;
		this.add(resetButton, c);
		
		// Add toggle clock button
		c.gridx = 3;
		c.gridy = 2;
		c.gridheight = 1;
		this.clockButton = new JButton("Toggle Clock");
		this.clockButton.addActionListener(this);
		this.clockButton.setActionCommand("clockButton");
		this.add(clockButton, c);

		// Add autoplay button
		c.gridx = 3;
		c.gridy = 3;
		c.gridheight = 1;
		this.playButton = new JButton("Autoplay");
		this.playButton.setActionCommand("autoplay");
		this.playButton.addActionListener(this);
		this.add(playButton, c);

		// Add speed slider label
		c.gridx = 3;
		c.gridy = 4;
		c.ipady += 5;
		JLabel t = new JLabel("                    Autoplay Speed");
		c.insets = new Insets(0, 7, -1, 5);
		t.setBorder(BorderFactory.createLineBorder(Color.black));
		this.add(t, c);
		c.ipady -= 5;

		// Add speed slider
		c.gridx = 3;
		c.gridy = 5;
		this.speedSlider = new JSlider(JSlider.HORIZONTAL, 10, 1000, 50);
		speedSlider.setMajorTickSpacing(10);
		speedSlider.setBorder(BorderFactory.createLineBorder(Color.black));
		c.insets = new Insets(0, 7, 5, 5);

		speedSlider.setPaintTicks(true);

		// Create the label table
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(10, new JLabel("Fast"));
		labelTable.put(1995, new JLabel("Slow"));
		speedSlider.setLabelTable(labelTable);
		speedSlider.setPaintLabels(true);
		speedSlider.addChangeListener(this);
		this.add(this.speedSlider, c);

		// Add gap to the left of the log; add log visualizer
		c.insets = new Insets(0, 6, 5, 0);
		logLabel = new JTextArea(1, 1);
		logLabel.setMaximumSize(new Dimension(20, 20));
		logLabel.setEditable(false);
		c.gridx = 3;
		c.gridy = 6;
		c.ipadx = 240;
		c.ipady = 150;
		c.gridheight = 1;
		c.fill = GridBagConstraints.VERTICAL;
		DefaultCaret caret = (DefaultCaret) logLabel.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		JScrollPane sv = new JScrollPane(logLabel);
		sv.setAutoscrolls(true);
		sv.setPreferredSize(new Dimension(20, 100));
		sv.setMaximumSize(new Dimension(20, 100));
		this.add(sv, c);

		c.ipadx = 0;
		c.ipady = 0;
		c.gridx = 3;
		c.gridy = 7;
		this.add(this.disp, c);

		// Add the view as a log observer
		sap.EventLog.getEventLog().addObserver(this);

		// Add the view as a clock observer
		sap.Clock.getClock().addObserver(this);

	}

	// Implement log observer method
	@Override
	public void newLogEntry(String entry) {
		logLabel.append(entry + "\n");
	}

	@Override
	// Handle button clicks
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("resetButtonClicked")) {
			try {
				this.model.reset();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		} else if (e.getActionCommand().contentEquals("clockButton")) {
			// If the program is auto-running, stop that first
			if (isAutoRunning) {
				isAutoRunning = false;
				bRunner.terminate();
				bRunner = null;
			}

			try {
				sap.Clock.getClock().toggleClock();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		} else if (e.getActionCommand().contentEquals("autoplay")) {
			if (isAutoRunning) {
				isAutoRunning = false;
				bRunner.terminate();
				bRunner = null;
			} else {
				isAutoRunning = true;
				bRunner = new BackgroundRunner(this.speedSlider.getMaximum() - this.speedSlider.getValue() + 25);
				bRunner.start();
			}
		}
	}

	@Override
	// If the clock changes, update our label
	public void clockChange() {
		this.clockStatusLabel.setText("Clock: " + (sap.Clock.getClock().getStatus() ? "HIGH" : "LOW"));
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		if (isAutoRunning) {
			isAutoRunning = false;
			bRunner.terminate();
			bRunner = null;

			isAutoRunning = true;
			bRunner = new BackgroundRunner(this.speedSlider.getMaximum() - this.speedSlider.getValue() + 25);
			bRunner.start();
		}
	}

	public boolean getIsAutoRunning() {
		return this.isAutoRunning;
	}

	public void setIsAutoRunning() {
		this.isAutoRunning = true;
	}

	public void autoPlay(){
		this.playButton.doClick();


	}
}