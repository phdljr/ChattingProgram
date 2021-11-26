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
import javax.swing.JOptionPane;
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

	// 네트워크
	private ServerSocket server_socket;
	private Socket socket;
	private int port;
	private Vector<UserInfo> user_vc = new Vector<UserInfo>();
	private Vector room_vc = new Vector();
	
	private StringTokenizer st;

	Server() {
		init();
		start();
	}

	private void serverStart() {
		try {
			server_socket = new ServerSocket(port);
		} catch (IOException e) {
			//e.printStackTrace();
			JOptionPane.showMessageDialog(null, "이미 사용중인 포트", "알림", JOptionPane.ERROR_MESSAGE);
		}

		if (server_socket != null) { // 정상 열림
			Connection();
		}
	}

	private void Connection() {

		// 1가지의 스레드에서는 1가지의 일만 처리할 수 있다.

		Thread th = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						textArea.append("사용자 접속 대기중\n");
						socket = server_socket.accept(); // 사용자 접속 무한 대기
						textArea.append("사용자 접속\n");

						UserInfo user = new UserInfo(socket);
						user.start(); // 각각의 유저 스레드 실행

					} catch (IOException e) { //서버 종료 시
						//e.printStackTrace();
						//JOptionPane.showMessageDialog(null, "accept 에러 방생", "알림", JOptionPane.ERROR_MESSAGE);
						break;
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
		textArea.setEditable(false);
		
		stop_btn.setEnabled(false);
		
		setVisible(true);
	}

	public static void main(String[] args) {
		new Server();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == start_btn) {
			System.out.println("스타트 버튼 클릭");

			port = Integer.parseInt(port_tf.getText().trim());

			serverStart(); // 서버 시작
			
			start_btn.setEnabled(false);
			port_tf.setEditable(false);
			stop_btn.setEnabled(true);
		} else if (e.getSource() == stop_btn) {
			System.out.println("중지 버튼 클릭");
			
			stop_btn.setEnabled(false);
			start_btn.setEnabled(true);
			port_tf.setEditable(true);
			try {
				server_socket.close();
				user_vc.removeAllElements();
				room_vc.removeAllElements();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	class UserInfo extends Thread {
		private OutputStream os;
		private InputStream is;
		private DataOutputStream dos;
		private DataInputStream dis;

		private Socket user_socket;
		private String nickname = "";
		
		private boolean room_check = true;

		UserInfo(Socket socket) {
			this.user_socket = socket;
			userNetwork();
		}

		public void run() { // 스레드 실행 메소드
			while (true) {
				try {
					String msg = dis.readUTF();
					textArea.append(nickname + " : 사용자로부터 들어온 메세지 : " + msg + "\n");
					inMessage(msg);
				} catch (IOException e) {
					//e.printStackTrace();
					//JOptionPane.showMessageDialog(null, "사용자 접속 끊어짐", "알림", JOptionPane.ERROR_MESSAGE);
					textArea.append(nickname+": 사용자 접속 끊어짐\n");
					try {
						dos.close();
						dis.close();
						user_socket.close();
						user_vc.remove(this);
						broadcast("User_out/"+nickname);
						broadcast("user_list_update/ ");
					}catch(IOException e1) {}
					break;
				}
			}
		}
		
		private void inMessage(String msg) { //클라이언트로부터 들어온 메세지 처리
			st = new StringTokenizer(msg, "/");
			
			String protocol = st.nextToken();
			String message = st.nextToken();
			
			System.out.println("프로토콜: "+protocol);
			System.out.println("내용: "+message);
			
			if(protocol.equals("Note")) {
				String note = st.nextToken();
				
				System.out.println("받는 사람: "+message);
				System.out.println("보낼 내용: "+note);
				
				//벡터에서 해당 사용자를 찾아서 메세지 전송
				for(int i=0;i<user_vc.size();i++) {
					UserInfo u = user_vc.elementAt(i);
					
					if(u.nickname.equals(message)) {
						u.sendMessage("Note/"+nickname+"/"+note);
					}
				}
			}
			else if(protocol.equals("CreateRoom")) {
				//1. 현재 같은 방이 존재하는지 확인
				for(int i=0;i<room_vc.size();i++) {
					RoomInfo r = (RoomInfo)room_vc.elementAt(i);
					
					if(r.room_name.equals(message)) { //이미 존재할 때
						sendMessage("CreateRoomFail/ok");
						room_check = false;
						break;
					}
				}
				
				if(room_check) { //방을 만들 수 있을 때
					RoomInfo new_room = new RoomInfo(message, this);
					room_vc.add(new_room); //전체 방 벡터에 방을 추가
					sendMessage("CreateRoom/"+message); //방 이름
					
					broadcast("New_Room/"+message);
				}
				
				room_check = true;
			}
			else if(protocol.equals("Chatting")) {
				String msg_text = st.nextToken();
				
				for(int i=0;i<room_vc.size();i++) {
					RoomInfo r = (RoomInfo)room_vc.elementAt(i);
					
					if(r.room_name.equals(message)) {
						r.broadcastRoom("Chatting/"+nickname+"/"+msg_text);
					}
				}
			}
			else if(protocol.equals("JoinRoom")) {
				for(int i=0;i<room_vc.size();i++) {
					RoomInfo r = (RoomInfo)room_vc.elementAt(i);
					if(r.room_name.equals(message)) {
						//새로운 사용자를 알린다
						r.broadcastRoom("Chatting/알림/*******"+nickname+"님이 입장하셨습니다*******");
						
						//사용자 추가
						r.addUser(this);
						sendMessage("JoinRoom/"+message);
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

				nickname = dis.readUTF(); // 사용자의 닉네임 받기
				textArea.append(nickname + " : 사용자 접속\n");

				// 기존 사용자에게 새로운 사용자 알림
				System.out.println("현재 접속된 사용자 수 : "+user_vc.size());
				
				broadcast("NewUser/"+nickname); //기존 사용자에게 자신을 알림
				
				//자신에게 기존 사용자를 받아오는 부분
				for(int i=0;i<user_vc.size();i++) {
					UserInfo u = user_vc.elementAt(i);
					sendMessage("OldUser/" + u.nickname);
				}
				
				//자신에게 기존 방 목록을 받아오는 부분
				for(int i=0;i<room_vc.size();i++) {
					RoomInfo r = (RoomInfo)room_vc.elementAt(i);
					sendMessage("OldRoom/"+r.room_name);
				}
				
				sendMessage("room_list_update/ ");
				
				user_vc.add(this); //사용자에게 알린 후 Vector에 추가.
				
				broadcast("user_list_update/ ");

			} catch (IOException e) {
				//e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Stream설정 에러", "알림", JOptionPane.ERROR_MESSAGE);
			}
		}

		private void sendMessage(String str) {
			try {
				dos.writeUTF(str);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		private void broadcast(String str) {//전체 사용자에게 메세지 보내는 부분
			for (int i = 0; i < user_vc.size(); i++) {
				UserInfo u = user_vc.elementAt(i);
				
				u.sendMessage(str); // 우리가 만든 프로토콜. 사용자 추가 : 프로토콜/(닉네임)
			}
		}
	}
	
	class RoomInfo{
		private String room_name;
		private Vector room_user_vc = new Vector();
		
		RoomInfo(String str, UserInfo u){
			this.room_name = str;
			this.room_user_vc.add(u);
		}
		
		public void broadcastRoom(String str){//현재 방의 모든 사람에게 알린다
			for(int i=0;i<room_user_vc.size();i++) {
				UserInfo u = (UserInfo)room_user_vc.elementAt(i);
				
				u.sendMessage(str);
			}
		}
		
		private void addUser(UserInfo u) {
			this.room_user_vc.add(u);
		}
	}
}
