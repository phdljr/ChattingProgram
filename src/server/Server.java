package server;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class Server extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private JTextField port_tf;
	private JTextArea textArea = new JTextArea();
	private JButton start_btn = new JButton("\uC11C\uBC84 \uC2E4\uD589");
	private JButton stop_btn = new JButton("\uC11C\uBC84 \uC911\uC9C0");

	// ��Ʈ��ũ
	private ServerSocket server_socket;
	private Socket socket;
	private int port;
	private Vector<UserInfo> user_vc = new Vector<UserInfo>();
	
	private StringTokenizer st;

	Server() {
		init();
		start();
	}

	private void serverStart() {
		try {
			server_socket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (server_socket != null) { // ���� ����
			Connection();
		}
	}

	private void Connection() {

		// 1������ �����忡���� 1������ �ϸ� ó���� �� �ִ�.

		Thread th = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						textArea.append("����� ���� �����\n");
						socket = server_socket.accept(); // ����� ���� ���� ���
						textArea.append("����� ����\n");

						UserInfo user = new UserInfo(socket);
						user.start(); // ������ ���� ������ ����

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});

		th.start();
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
		if (e.getSource() == start_btn) {
			System.out.println("��ŸƮ ��ư Ŭ��");

			port = Integer.parseInt(port_tf.getText().trim());

			serverStart(); // ���� ����
		} else if (e.getSource() == stop_btn) {
			System.out.println("���� ��ư Ŭ��");
		}
	}

	class UserInfo extends Thread {
		private OutputStream os;
		private InputStream is;
		private DataOutputStream dos;
		private DataInputStream dis;

		private Socket user_socket;
		private String nickname = "";

		UserInfo(Socket socket) {
			this.user_socket = socket;
			userNetwork();
		}

		public void run() { // ������ ���� �޼ҵ�
			while (true) {
				try {
					String msg = dis.readUTF();
					textArea.append(nickname + " : ����ڷκ��� ���� �޼��� : " + msg + "\n");
					inMessage(msg);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		private void inMessage(String msg) { //Ŭ���̾�Ʈ�κ��� ���� �޼��� ó��
			st = new StringTokenizer(msg, "/");
			
			String protocol = st.nextToken();
			String message = st.nextToken();
			
			System.out.println("��������: "+protocol);
			System.out.println("����: "+message);
			
			if(protocol.equals("Note")) {
				String note = st.nextToken();
				
				System.out.println("�޴� ���: "+message);
				System.out.println("���� ����: "+note);
				
				//���Ϳ��� �ش� ����ڸ� ã�Ƽ� �޼��� ����
				for(int i=0;i<user_vc.size();i++) {
					UserInfo u = user_vc.elementAt(i);
					
					if(u.nickname.equals(message)) {
						u.sendMessage("Note/"+nickname+"/"+note);
					}
				}
			}
		}

		private void userNetwork() {
			try {
				is = user_socket.getInputStream();
				dis = new DataInputStream(is);

				os = user_socket.getOutputStream();
				dos = new DataOutputStream(os);

				nickname = dis.readUTF(); // ������� �г��� �ޱ�
				textArea.append(nickname + " : ����� ����\n");

				// ���� ����ڿ��� ���ο� ����� �˸�
				System.out.println("���� ���ӵ� ����� �� : "+user_vc.size());
				
				broadcast("NewUser/"+nickname); //���� ����ڿ��� �ڽ��� �˸�
				
				//�ڽſ��� ���� ����ڸ� �޾ƿ��� �κ�
				for(int i=0;i<user_vc.size();i++) {
					UserInfo u = user_vc.elementAt(i);
					
					sendMessage("OldUser/" + u.nickname);
				}
				
				user_vc.add(this); //����ڿ��� �˸� �� Vector�� �߰�.
				
				broadcast("user_list_update/ ");

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void sendMessage(String str) {
			try {
				dos.writeUTF(str);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		private void broadcast(String str) {//��ü ����ڿ��� �޼��� ������ �κ�
			for (int i = 0; i < user_vc.size(); i++) {
				UserInfo u = user_vc.elementAt(i);
				
				u.sendMessage(str); // �츮�� ���� ��������. ����� �߰� : ��������/(�г���)
			}
		}
	}

}
