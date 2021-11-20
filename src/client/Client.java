package client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class Client extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// login
	private JFrame loign_gui = new JFrame();
	private JPanel login_pane;
	private JTextField ip_tf;
	private JTextField port_tf;
	private JTextField id_tf;
	private JButton login_btn = new JButton("\uB85C\uADF8\uC778");

	// main
	private JPanel contentPane;
	private JTextField message_tf;
	private JButton notesend_btn = new JButton("���� ������");
	private JButton joinroom_btn = new JButton("ä�ù� ����");
	private JButton createroom_btn = new JButton("�� �����");
	private JButton send_btn = new JButton("����");

	private JList<String> user_list = new JList<String>();
	private JList room_list = new JList();

	private JTextArea chat_area = new JTextArea();

	// ��Ʈ��ũ�� ���� �ڿ� ����
	private Socket socket;
	private String ip = "";
	private int port;
	private String id = "";
	private InputStream is; // �����κ��� �޽����� �޴� ��Ʈ��
	private OutputStream os; // ������ �޽����� ������ ��Ʈ��
	private DataInputStream dis; // ���� ����Ʈ ��Ʈ���� ���ϰ� ����ϱ� ���� DataStream�� ���
	private DataOutputStream dos;

	// �׿� ������
	private Vector<String> userList = new Vector<String>();
	private Vector roomList = new Vector();
	private StringTokenizer st;

	Client() {
		login_init();
		main_init();
		start();
	}

	private void main_init() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 698, 494);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblNewLabel = new JLabel("\uC804\uCCB4 \uC811\uC18D\uC790");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(12, 10, 101, 15);
		contentPane.add(lblNewLabel);

		user_list.setBounds(12, 35, 101, 142);
		contentPane.add(user_list);
		user_list.setListData(userList);

		notesend_btn.setBounds(12, 184, 101, 23);
		contentPane.add(notesend_btn);

		JLabel lblNewLabel_1 = new JLabel("\uCC44\uD305\uBC29 \uBAA9\uB85D");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setBounds(12, 217, 101, 15);
		contentPane.add(lblNewLabel_1);

		room_list.setBounds(12, 242, 101, 142);
		contentPane.add(room_list);
		room_list.setListData(roomList);

		joinroom_btn.setBounds(12, 394, 101, 23);
		contentPane.add(joinroom_btn);

		createroom_btn.setBounds(12, 422, 101, 23);
		contentPane.add(createroom_btn);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(125, 31, 545, 386);
		contentPane.add(scrollPane);

		scrollPane.setViewportView(chat_area);

		message_tf = new JTextField();
		message_tf.setBounds(125, 423, 436, 21);
		contentPane.add(message_tf);
		message_tf.setColumns(10);

		send_btn.setBounds(573, 422, 97, 23);
		contentPane.add(send_btn);

		setVisible(true);
	}

	private void login_init() {
		loign_gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		loign_gui.setBounds(100, 100, 340, 430);
		login_pane = new JPanel();
		login_pane.setBorder(new EmptyBorder(5, 5, 5, 5));
		loign_gui.setContentPane(login_pane);
		login_pane.setLayout(null);

		JLabel lblNewLabel = new JLabel("Server IP");
		lblNewLabel.setBounds(42, 184, 57, 15);
		login_pane.add(lblNewLabel);

		JLabel lblServerPort = new JLabel("Server Port");
		lblServerPort.setBounds(42, 242, 72, 15);
		login_pane.add(lblServerPort);

		JLabel lblNewLabel_1_1 = new JLabel("ID");
		lblNewLabel_1_1.setBounds(42, 297, 57, 15);
		login_pane.add(lblNewLabel_1_1);

		ip_tf = new JTextField();
		ip_tf.setBounds(148, 181, 116, 21);
		login_pane.add(ip_tf);
		ip_tf.setColumns(10);

		port_tf = new JTextField();
		port_tf.setBounds(148, 239, 116, 21);
		login_pane.add(port_tf);
		port_tf.setColumns(10);

		id_tf = new JTextField();
		id_tf.setBounds(148, 294, 116, 21);
		login_pane.add(id_tf);
		id_tf.setColumns(10);

		login_btn.setBounds(42, 340, 222, 23);
		login_pane.add(login_btn);

		loign_gui.setVisible(true);
	}

	private void network() {
		try {
			socket = new Socket(ip, port);

			if (socket != null) {// ���� ��Ĺ ����
				connection();
			}

		} catch (UnknownHostException e) { // ȣ��Ʈ�� ã�� �� ����
			e.printStackTrace();
		} catch (IOException e) { // ��Ʈ�� ����
			e.printStackTrace();
		}
	}

	// ������ ������� �ϳ��̱� ������ ���ÿ� ���� Ŭ���̾�Ʈ�� ��� ����
	private void connection() {// �������� �޼ҵ�
		try {
			is = socket.getInputStream();
			dis = new DataInputStream(is);

			os = socket.getOutputStream();
			dos = new DataOutputStream(os);
		} catch (IOException e) {
			e.printStackTrace();
		} // Stream ���� ��

		sendMessage(id);

		// user_list�� ����� �߰�
		userList.add(id);

		// ó�� ���� �� ���̵�
		Thread th = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {

						String msg = dis.readUTF();

						System.out.println("�����κ��� ���ŵ� �޼��� : " + msg);

						inMessage(msg);

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});

		th.start();
	}

	private void inMessage(String str) { // �����κ��� ������ ��� �޼���
		st = new StringTokenizer(str, "/");

		String protocol = st.nextToken();
		String message = st.nextToken();

		System.out.println("�������� : " + protocol);
		System.out.println("���� : " + message);

		if (protocol.equals("NewUser")) {// ���ο� ������
			userList.add(message);
		} else if (protocol.equals("OldUser")) {
			userList.add(message);
		} else if(protocol.equals("Note")) {
			String note = st.nextToken();
			
			System.out.println(message+" ����ڷκ��� �� ���� : "+ note);
			
			JOptionPane.showMessageDialog(null, note, message+"������ ���� �� ����", JOptionPane.CLOSED_OPTION);
		}else if(protocol.equals("user_list_update")) { //Swing�� JList ���������� ���� ��������
			//user_list.updateUI() //�� �ȵ�
			user_list.setListData(userList);
		}
	}

	private void sendMessage(String str) { // �������� �޼����� ������ �κ�
		try {
			dos.writeUTF(str); // ������ �޼��� ������
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void start() {
		login_btn.addActionListener(this);
		notesend_btn.addActionListener(this);
		joinroom_btn.addActionListener(this);
		createroom_btn.addActionListener(this);
		send_btn.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == login_btn) {
			System.out.println("�α��� ��ư Ŭ��");

			ip = ip_tf.getText().trim();
			port = Integer.parseInt(port_tf.getText().trim());
			id = id_tf.getText().trim();

			network();
		} else if (e.getSource() == notesend_btn) {
			System.out.println("���� ������ Ŭ��");

			String user = (String) user_list.getSelectedValue();
			String note = JOptionPane.showInputDialog("���� �޼���");

			if (note != null) { // �޼����� �Էµ��� ��
				sendMessage("Note/" + user + "/" + note); // user���� note ������ ������.
			}
			System.out.println("�޴� ���: " + user + "| ���� ����: " + note);
		} else if (e.getSource() == joinroom_btn) {
			System.out.println("�� ���� ��ư Ŭ��");
		} else if (e.getSource() == createroom_btn) {
			System.out.println("�� ����� ��ư Ŭ��");
		} else if (e.getSource() == send_btn) {
			System.out.println("���� ��ư Ŭ��");
			sendMessage("�ӽ��׽�Ʈ.");
		}
	}

	public static void main(String[] args) {
		new Client();
	}

}
