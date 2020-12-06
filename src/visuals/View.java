package visuals;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.*;

import sap.SAPModel;

public class View extends JPanel implements sap.LogObserver, ActionListener {
	
	private SAPModel model;
	private JLabel welcome_label;
	private GridBagConstraints c;
	private JTextArea logLabel;
	private JButton resetButton;
	private SAPViewWidget viewWidget;
	private RAMViewWidget ramWidget;



	public View() {
		this.model = new SAPModel();
		
		/* Set the Layout */
		this.setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.CENTER;
		this.setPreferredSize(new Dimension(1000, 1000));
		
		this.viewWidget = new SAPViewWidget(this.model);
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 6;
		this.add(viewWidget, c);
		
		this.ramWidget = new RAMViewWidget(this.model);
		// Add some padding
		c.gridx = 1;
		this.add(ramWidget, c);
		
		
		welcome_label = new JLabel("Welcome!");
		welcome_label.setForeground(Color.BLUE);
		welcome_label.setBackground(Color.YELLOW);

		c.gridx = 3;
		c.gridy = 0;
		c.gridheight = 1;
		this.add(welcome_label, c);
		
		resetButton = new JButton("Reset");
		resetButton.setActionCommand("resetButtonClicked");
		resetButton.addActionListener(this);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 3;
		c.gridy = 1;
		c.gridheight = 1;
		this.add(resetButton, c);
		
		logLabel = new JTextArea("SAP Event Log\n===============\n");
		logLabel.setEditable(false);
		c.gridx = 3;
		c.gridy = 2;
		c.ipadx = 150;
		c.ipady = 350;
		c.gridheight = 7;
		this.add(new JScrollPane(logLabel), c);
		
	
		
		// Add the view as a log observer
		sap.EventLog.getEventLog().addObserver(this);
	}


	// Implement log observer method
	@Override
	public void newLogEntry(String entry) {
		logLabel.append(entry + "\n");
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("resetButtonClicked")) {
			this.model.reset();
		}
	}

}