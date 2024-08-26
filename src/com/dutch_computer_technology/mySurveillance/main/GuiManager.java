package com.dutch_computer_technology.mySurveillance.main;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.dutch_computer_technology.mySurveillance.Main;
import com.dutch_computer_technology.mySurveillance.Main.printType;

public class GuiManager extends Thread {
	
	private MySurveillance ms;
	private boolean run = true;
	
	private JFrame frame;
	private JLabel label_Version;
	private JLabel label_Memory;
	private JLabel label_FileManagerBuffers;
	private JLabel label_CamerasTotal;
	private JLabel label_CamerasOnline;
	private JLabel label_CamerasStreaming;
	
	private final static Font font = new Font("Serif", Font.PLAIN, 20);
	private final static Color cBlack = new Color(0, 0, 0);
	private final static Color cGreen = new Color(0, 125, 50);
	
	@SuppressWarnings("serial")
	private class Label extends JLabel {
		
		public Label(int x, int y) {
			super();
			this.setFont(font);
			this.setHorizontalAlignment(JLabel.LEFT);
			this.setVerticalAlignment(JLabel.TOP);
			this.setForeground(cGreen);
			this.setBounds(x, y, 420, 26);
		};
		
	};
	
	public void kill() {
		
		this.run = false;
		if (frame != null) frame.dispose();
		
	};
	
	public GuiManager(MySurveillance ms) {
		
		this.ms = ms;
		
		frame = new JFrame();
		frame.setTitle("MySurveillance (OpenSource)");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setResizable(false);
		frame.setSize(420, 320);
		frame.setLayout(null);
		frame.setVisible(true);
		frame.getContentPane().setBackground(cBlack);
		
		BufferedImage icon = null;
		try {
			InputStream icon_is = Main.class.getResourceAsStream("images/icon.png");
			if (icon_is == null) {
				Main.print(this, printType.WARNING, "Could not load icon for GUI");
			} else {
				icon = ImageIO.read(icon_is);
			};
		} catch (IOException e) {
			Main.print(this, printType.WARNING, "Could not load icon for GUI", e);
		};
		if (icon != null) frame.setIconImage(icon);
		
		label_Version = new Label(0, 0);
		this.frame.add(label_Version);
		label_Version.setText("Version: " + Main.version);
		
		label_Memory = new Label(0, 26);
		this.frame.add(label_Memory);
		
		label_CamerasTotal = new Label(0, 52);
		this.frame.add(label_CamerasTotal);
		
		label_CamerasOnline = new Label(0, 78);
		this.frame.add(label_CamerasOnline);
		
		label_CamerasStreaming = new Label(0, 104);
		this.frame.add(label_CamerasStreaming);
		
		label_FileManagerBuffers = new Label(0, 130);
		this.frame.add(label_FileManagerBuffers);
		
	};
	
	public void run() {
		
		while (ms.run && this.run) {
			
			if (frame == null) break;
			
			int totalCamera = ms.cameras.totalCameras();
			int totalCamerasOnline = ms.cameras.totalCamerasOnline();
			int totalCamerasRunning = ms.cameras.totalCamerasRunning();
			
			Runtime run = Runtime.getRuntime();
			long memUsed = (run.totalMemory() - run.freeMemory()) / 1048576;
			long memMax = run.totalMemory() / 1048576;
			String mem = Long.toString(memUsed) + "mb/" + Long.toString(memMax) + "mb";
			
			label_Memory.setText("Memory: " + mem);
			label_CamerasTotal.setText("Cameras: " + totalCamera);
			label_CamerasOnline.setText("Cameras online: " + totalCamerasOnline + "/" + totalCamera);
			label_CamerasStreaming.setText("Cameras streaming: " + totalCamerasRunning + "/" + totalCamerasOnline);
			label_FileManagerBuffers.setText("FileManager: " + ms.fileManager.totalBuffers() + " buffers in memory");
			
			try {
				
				Thread.sleep(200);
			} catch (InterruptedException e) {
				Main.print(this, e);
			};
			
		};
		
	};
	
};