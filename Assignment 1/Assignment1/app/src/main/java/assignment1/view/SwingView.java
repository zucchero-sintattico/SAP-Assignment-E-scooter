package assignment1.view;

import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.*;
import assignment1.model.ModelObserver;
import assignment1.model.ModelObserverSource;

public class SwingView implements ModelObserver {

	private ModelObserverSource model;
	private MyFrame frame;
	
	public SwingView(ModelObserverSource model) {		
		this.model = model;		
	    model.addObserver(this);	    
	    frame = new MyFrame(model.getState());
	}

	public void notifyModelUpdated() {
		log("model updated => updating the view");
		frame.updateView(model.getState());
	}
		
	public void display() {
		SwingUtilities.invokeLater(() -> {
			frame.setVisible(true);
		});
	}

	private void log(String msg) {
		System.out.println("[View] " + msg);
	}
	
	class MyFrame extends JFrame  {

		private JTextField state;

		public MyFrame(int initState) {
			super("My View");
			
			setSize(300, 70);
			setResizable(false);
			
			state = new JTextField(10);
			state.setText("" + initState);

			JPanel panel = new JPanel();
			panel.add(state);
			
			setLayout(new BorderLayout());
		    add(panel,BorderLayout.NORTH);
		    	    		
			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent ev) {
					System.exit(-1);
				}
			});
		}
	
		public void updateView(int newState) {
			try {
				SwingUtilities.invokeLater(() -> {
					state.setText("" + newState);
				});
			} catch (Exception ex){
				ex.printStackTrace();
			}
		}
	}
	
}
