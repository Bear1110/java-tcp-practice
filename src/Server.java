import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class  Server {
	public static final int LISTEN_PORT = 5988;
	
	int[] treassure =new int[]{-1,-1,-1};
	int clientCount = 1;

	public void listenRequest() {
		
		ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new treasureOwner());
        
		ServerSocket serverSocket = null;
		ExecutorService threadExecutor = Executors.newFixedThreadPool(20);
		try {
			serverSocket = new ServerSocket(LISTEN_PORT);
			System.out.println("Server listening requests...");
			while (true) {
				Socket socket = serverSocket.accept();
				threadExecutor.execute(new RequestThread(socket));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (threadExecutor != null)
				threadExecutor.shutdown();
			if (serverSocket != null)
				try {
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Server server = new Server();
		server.listenRequest();
	}
	class treasureOwner implements Runnable {
		char[] treassureName ={'A','B','C'};
		public void run() {
			while(true){
				try {
					Thread.sleep(3000);
					for(int i = 0 ; i < 3 ; i++){
						String message = treassureName[i]+" ";
						if(treassure[i]!=-1)
							message +="YES "+treassure[i];
						else
							message +="NO 0";
						System.out.println(message);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	/**
	 * 處理Client端的Request執行續。
	 * @version
	 */
	class RequestThread implements Runnable {
		private Socket clientSocket;
		private String message = "";
		private String respone = "";
		String Ip = "";
		int thisClientId = clientCount++;

		public RequestThread(Socket clientSocket) {
			this.clientSocket = clientSocket;
			Ip = clientSocket.getRemoteSocketAddress() + "";
		}

		public void run() {
			System.out.println("有" + clientSocket.getRemoteSocketAddress() + "連線進來!");
			DataInputStream input = null;
			DataOutputStream output = null;
			try {
				input = new DataInputStream(this.clientSocket.getInputStream());
				output = new DataOutputStream(this.clientSocket.getOutputStream());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			while (!this.clientSocket.isClosed()) {
				try {
					while (true) { // 讀入送到server 的消息
						message = input.readUTF();
						break;
					}
					System.out.println(Ip + " ("+thisClientId+") 對 Server說:" + message);
					if(message.charAt(0)=='G'){ /////////////////////////////GET
						int wantGet = message.charAt(4)-65;
						if(treassure[wantGet]==-1){
							respone = "Yes "+message.charAt(4);
							treassure[wantGet] = thisClientId;
						}else{
							respone = "No "+message.charAt(4);
						}
					}else if(message.charAt(0)=='R'){ /////////////////////RELEASE
						int wantRelease = message.charAt(8)-65;
						treassure[wantRelease]=-1;
						continue;
					}
					while (true) { // 送出到client 的消息
						output.writeUTF(respone.toString());
						output.flush();
						break;
					}
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println(String.format("連線中斷,%s", clientSocket.getRemoteSocketAddress()));

					try {
						if (input != null)
							input.close();
						if (output != null)
							output.close();
						if (this.clientSocket != null && !this.clientSocket.isClosed())
							this.clientSocket.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}

			}

		}
	}	
}