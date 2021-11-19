package server;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class Server extends JFrame implements ActionListener{
	
	private JPanel contentPane;
	private JTextField port_tf;
	private JTextArea textArea = new JTextArea();
	private JButton start_btn = new JButton("\uC11C\uBC84 \uC2E4\uD589");
	private JButton stop_btn = new JButton("\uC11C\uBC84 \uC911\uC9C0");
	
	Server(){
		init();
		start();
	}
	
	private void start() {
		start_btn.addActionListener(this);
		stop_btn.addActionListener(this);
	}

	private void init() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 325, 400);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("\uD3EC\uD2B8\uBC88\uD638 :");
		lblNewLabel.setBounds(12, 247, 57, 15);
		contentPane.add(lblNewLabel);
		
		port_tf = new JTextField();
		port_tf.setBounds(81, 244, 216, 21);
		contentPane.add(port_tf);
		port_tf.setColumns(10);
		
		start_btn.setBounds(12, 303, 140, 23);
		contentPane.add(start_btn);
		
		stop_btn.setBounds(157, 303, 140, 23);
		contentPane.add(stop_btn);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 285, 217);
		contentPane.add(scrollPane);
		
		scrollPane.setViewportView(textArea);
		
		setVisible(true);
	}
	
	public static void main(String[] args) {
		new Server();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == start_btn) {
			System.out.println("��ŸƮ ��ư Ŭ��");
		}
		else if(e.getSource() == stop_btn) {
			System.out.println("���� ��ư Ŭ��");
		}
	}

}