package assignment1.view;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import assignment1.controller.UserInputObserver;

public class InputUI implements UserInputSource {

	private List<UserInputObserver> observers;

	private MyFrame frame;
	
	public InputUI() {		
		observers = new ArrayList<UserInputObserver>();		
	    frame = new MyFrame();
	}

	public void addObserver(UserInputObserver obs){
		observers.add(obs);
	}

	public void display() {
		SwingUtilities.invokeLater(() -> {
			frame.setVisible(true);
		});
	}

	private void log(String msg) {
		System.out.println("[InputUI] " + msg);
	}
	
	class MyFrame extends JFrame implements ActionListener {

		public MyFrame() {
			super("My Input UI");
			
			setSize(300, 70);
			setResizable(false);
			
			JButton button = new JButton("Update");
			button.addActionListener(this);
			
			JPanel panel = new JPanel();
			panel.add(button);		
			
			setLayout(new BorderLayout());
		    add(panel,BorderLayout.NORTH);
		    	    		
			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent ev) {
					System.exit(-1);
				}
			});
		}
	
		public void actionPerformed(ActionEvent ev) {
			try {
				log("New input detected.");
				for (UserInputObserver obs: observers){
					obs.notifyNewUpdateRequested();
				}
			} catch (Exception ex) {
			}
		}	
	}
	
}
