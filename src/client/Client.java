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
	private JButton notesend_btn = new JButton("쪽지 보내기");
	private JButton joinroom_btn = new JButton("채팅방 참여");
	private JButton createroom_btn = new JButton("방 만들기");
	private JButton send_btn = new JButton("전송");

	private JList<String> user_list = new JList<String>();
	private JList room_list = new JList();

	private JTextArea chat_area = new JTextArea();

	// 네트워크를 위한 자원 변수
	private Socket socket;
	private String ip = "";
	private int port;
	private String id = "";
	private InputStream is; // 서버로부터 메시지를 받는 스트림
	private OutputStream os; // 서버로 메시지를 보내는 스트림
	private DataInputStream dis; // 위의 바이트 스트림을 편하게 사용하기 위해 DataStream을 사용
	private DataOutputStream dos;

	// 그외 변수들
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

			if (socket != null) {// 정상 소캣 연결
				connection();
			}

		} catch (UnknownHostException e) { // 호스트를 찾을 수 없음
			e.printStackTrace();
		} catch (IOException e) { // 스트림 에러
			e.printStackTrace();
		}
	}

	// 서버의 스레드는 하나이기 때문에 동시에 여러 클라이언트와 통신 못함
	private void connection() {// 실질적인 메소드
		try {
			is = socket.getInputStream();
			dis = new DataInputStream(is);

			os = socket.getOutputStream();
			dos = new DataOutputStream(os);
		} catch (IOException e) {
			e.printStackTrace();
		} // Stream 설정 끝

		sendMessage(id);

		// user_list에 사용자 추가
		userList.add(id);

		// 처음 접속 시 아이디
		Thread th = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {

						String msg = dis.readUTF();

						System.out.println("서버로부터 수신된 메세지 : " + msg);

						inMessage(msg);

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});

		th.start();
	}

	private void inMessage(String str) { // 서버로부터 들어오는 모든 메세지
		st = new StringTokenizer(str, "/");

		String protocol = st.nextToken();
		String message = st.nextToken();

		System.out.println("프로토콜 : " + protocol);
		System.out.println("내용 : " + message);

		if (protocol.equals("NewUser")) {// 새로운 접속자
			userList.add(message);
		} else if (protocol.equals("OldUser")) {
			userList.add(message);
		} else if(protocol.equals("Note")) {
			String note = st.nextToken();
			
			System.out.println(message+" 사용자로부터 온 쪽지 : "+ note);
			
			JOptionPane.showMessageDialog(null, note, message+"님으로 부터 온 쪽지", JOptionPane.CLOSED_OPTION);
		}else if(protocol.equals("user_list_update")) { //Swing의 JList 오류때문에 생긴 프로토콜
			//user_list.updateUI() //잘 안됨
			user_list.setListData(userList);
		}
	}

	private void sendMessage(String str) { // 서버에게 메세지를 보내는 부분
		try {
			dos.writeUTF(str); // 서버로 메세지 보내기
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
			System.out.println("로그인 버튼 클릭");

			ip = ip_tf.getText().trim();
			port = Integer.parseInt(port_tf.getText().trim());
			id = id_tf.getText().trim();

			network();
		} else if (e.getSource() == notesend_btn) {
			System.out.println("쪽지 보내기 클릭");

			String user = (String) user_list.getSelectedValue();
			String note = JOptionPane.showInputDialog("보낼 메세지");

			if (note != null) { // 메세지가 입력됐을 때
				sendMessage("Note/" + user + "/" + note); // user에게 note 쪽지를 보낸다.
			}
			System.out.println("받는 사람: " + user + "| 보낼 내용: " + note);
		} else if (e.getSource() == joinroom_btn) {
			System.out.println("방 참여 버튼 클릭");
		} else if (e.getSource() == createroom_btn) {
			System.out.println("방 만들기 버튼 클릭");
		} else if (e.getSource() == send_btn) {
			System.out.println("전송 버튼 클릭");
			sendMessage("임시테스트.");
		}
	}

	public static void main(String[] args) {
		new Client();
	}

}
