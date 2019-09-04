import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
	int[] treassure =new int[]{-1,-1,-1};
	private static Socket socket = null;
	ExecutorService executorService = Executors.newFixedThreadPool(20);
	boolean wantRelease = false;
	int wantReleaseNo = 0;
	
	public static void main(String[] args) throws IOException {
		String host = "192.168.0.106";
		int port = 5988;
		try {
			Client client = new Client();
			socket = new Socket(host, port);
			client.prepare();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	void prepare (){
		executorService.execute(new catchTreassure());
		executorService.execute(new treasureOwner());
	}

	class catchTreassure implements Runnable {
		
		char[] treassureName = { 'A', 'B', 'C' };
		String serverData = "";
		int i = 0;
		public void run() {
			while (true) {
				try {
					Thread.sleep(1000);
					DataInputStream input = new DataInputStream(socket.getInputStream());
					DataOutputStream output = new DataOutputStream(socket.getOutputStream());
					if(treassure[i]==-1){
						output.writeUTF("GET "+treassureName[i]); // 傳東西給server
						output.flush(); // 清空緩衝區域 將東西強制送出
						 
						while (true) { // 讀 server 送過來的東西
							serverData = input.readUTF();
							break;
						}
						if(serverData.charAt(0)=='Y'){
							treassure[i]=5;
							executorService.execute(new treassureTimeOut(i));
						}
						
						System.out.println("Server 對我說 :"+serverData);
					}
				} catch (IOException e) {e.printStackTrace();
				} catch (InterruptedException e) {e.printStackTrace();}
				if(i<2)
					i++;
				else
					i=0;
			}
		}
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
							message +="NO";
						System.out.println(message);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	class treassureTimeOut implements Runnable {
		int i = 0;
		char[] treassureName = { 'A', 'B', 'C' };
		public treassureTimeOut (int i){
			this.i = i;
		}
		public void run() {
			while(treassure[i]>-1){
				try {
					Thread.sleep(1000);
					treassure[i]--;					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			try {
				DataOutputStream output = new DataOutputStream(socket.getOutputStream());
				output.writeUTF("RELEASE "+treassureName[i]); // 傳東西給server
			} catch (IOException e) {e.printStackTrace();}
			treassure[i ]=-1;
		}
	}
}
