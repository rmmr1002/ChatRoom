import java.io.*;
import java.text.*;
import java.util.*;
import java.net.*;

// Server class
public class Server
{
	public static void main(String[] args) throws IOException
	{
		// server is listening on port 5056
		ServerSocket ss = new ServerSocket(Integer.parseInt(args[1]));

		// running infinite loop for getting
		// client request
		while (true)
		{
			Socket s = null;

			try
			{
				// socket object to receive incoming client requests
				s = ss.accept();

				System.out.println("A new client is connected : " + s);

				// obtaining input and out streams
				DataInputStream dis = new DataInputStream(s.getInputStream());
				DataOutputStream dos = new DataOutputStream(s.getOutputStream());

				System.out.println("Assigning new thread for this client");

				// create a new thread object
				Thread t = new ClientHandler(s, dis, dos);

				// Invoking the start() method
				t.start();

			}
			catch (Exception e){
				s.close();
				e.printStackTrace();
			}
		}
	}
}

class Room {
	public String name;
	public String password;
	public ArrayList<ClientHandler> participants;
	public Room(String n,ClientHandler u){
		name=n;
		password="";
		participants = new ArrayList<ClientHandler>();
		participants.add(u);
	}
	public Room(String n,String p,ClientHandler u){
		name=n;
		password=p;
		participants = new ArrayList<ClientHandler>();
		participants.add(u);
	}
}



// ClientHandler class
class ClientHandler extends Thread
{

	final static String lock = "lck";
	public final DataInputStream dis;
	public final DataOutputStream dos;
	final Socket s;
	static volatile HashSet<Integer> randlist = new HashSet<Integer>();
	static volatile ArrayList<String>  nametakenlist = new ArrayList<String>();
	static volatile HashMap<String,Room> roomlist = new HashMap<String,Room>();
	static volatile HashMap<String,ClientHandler> clientlist = new HashMap<String,ClientHandler>();
  public String name;
	public String roomname;

	// Constructor
	public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos)
	{
		this.s = s;
		this.dis = dis;
		this.dos = dos;
		name="";
		roomname="";

	}

	@Override
	public void run()
	{
		boolean cond = true;
		while (cond)
		{
			try {
			int len;
			len = dis.readInt();
			Short sh =  dis.readShort();
			String hex = Integer.toHexString(sh & 0xffff);
			byte[] opcode = new byte[1];
			dis.read(opcode);
			int opcodeint = (int) opcode[0] & 0xff ;
		  System.out.println("The length is: "+len);
			System.out.println("The hex is: "+hex);

      System.out.println("Printing op code is "+ opcodeint);
			if(opcodeint == 255) {

					System.out.println("Handshake");
					byte [] readmsg = new byte[len];
					dis.read(readmsg);
					String r = new String(readmsg);
					System.out.println("Printing message sent to us is "+r);
					// Sending response to client
					int i=0;
					for(i=0;randlist.contains(i);i++){
						System.out.println("Looking for rand");
					}
					randlist.add(i);
					String msgsend = ""+"rand"+i;
					nametakenlist.add(msgsend);
					dos.writeInt(msgsend.length()+1);
					int sendopcodehexint = 0x417;
					short sendopcodehex = (short) sendopcodehexint;
					dos.writeShort(sendopcodehex);
					byte sendOpcode = (byte)0xfe;
					dos.writeByte(sendOpcode);
					byte sendreturncode = (byte)0x00;
					dos.writeByte(sendreturncode);

					String tmp_name=""+"rand"+i;
				  this.name = tmp_name;
					System.out.println("Name of the client is "+this.name);
					synchronized(lock){
					  clientlist.put(tmp_name,this);

				  }
					dos.writeBytes(msgsend);
					dos.flush();
				}
			else if(opcodeint == 10){
				  System.out.println("Join");
					String oldroomstring = this.roomname;
					int errorflag=0;
					byte [] roomnamelen = new byte[1];
					dis.read(roomnamelen);
					int x = roomnamelen[0];
					byte[] roomname = new byte[x];
					dis.read(roomname);
					String roomnamestring=new String(roomname);
					System.out.println("Room name is "+ roomnamestring);
					byte [] passwordlen = new byte[1];
					dis.read(passwordlen);
					int c = passwordlen[0];
					System.out.println("Printing all keys out: "+ roomlist.keySet());
					if (c > 0 ){
					  byte[] password = new byte[c];
					  dis.read(password);
					  String passwordstring=new String(password);

						if(this.roomname.equals(roomnamestring)){
							// error condition client is in room
							String msgend="You attempt to bend space and time to reenter where you already are. You fail..";
							errorflag=1;
							dos.writeInt(msgend.length()+1);
							int sendopcodehexint = 0x417;
							short sendopcodehex = (short) sendopcodehexint;
							dos.writeShort(sendopcodehex);
							byte sendOpcode = (byte)0xfe;
							dos.writeByte(sendOpcode);
							byte sendreturncode = (byte)0x01;
							dos.writeByte(sendreturncode);
							dos.writeBytes(msgend);
							dos.flush();
						}
						else {
							if (roomlist.containsKey(roomnamestring)){  // if room already exists
								System.out.println("Room exists");
								if(passwordstring.equals(roomlist.get(roomnamestring).password)){
								  this.roomname = roomnamestring;
								  roomlist.get(roomnamestring).participants.add(this);
								}
								else {
									//error wrong password
									errorflag=1;
									String msgend="Invalid password. You shall not pass.";
									dos.writeInt(msgend.length()+1);
									int sendopcodehexint = 0x417;
									short sendopcodehex = (short) sendopcodehexint;
									dos.writeShort(sendopcodehex);
									byte sendOpcode = (byte)0xfe;
									dos.writeByte(sendOpcode);
									byte sendreturncode = (byte)0x01;
									dos.writeByte(sendreturncode);
									dos.writeBytes(msgend);
									dos.flush();
								}
							}
							else {
								            // create new room with password
							 System.out.println("room doesn't exist");
							 synchronized (lock){
                 Room tmp_room = new Room(roomnamestring,passwordstring,this);
							   roomlist.put(roomnamestring,tmp_room);
							   this.roomname = roomnamestring;
						  }
							}
						}
					}
					else {  // no password case
						System.out.println("no password case");
						if(this.roomname.equals(roomnamestring)){
							// error condition client is in room
							System.out.println("Erroe, client is in room");
							errorflag=1;
							String msgend="You attempt to bend space and time to reenter where you already are. You fail..";
							dos.writeInt(msgend.length()+1);
							int sendopcodehexint = 0x417;
							short sendopcodehex = (short) sendopcodehexint;
							dos.writeShort(sendopcodehex);
							byte sendOpcode = (byte)0xfe;
							dos.writeByte(sendOpcode);
							byte sendreturncode = (byte)0x01;
							dos.writeByte(sendreturncode);
							dos.writeBytes(msgend);
							dos.flush();
						}
						else {
							if (roomlist.containsKey(roomnamestring)) {
								System.out.println("Room exists");
								this.roomname = roomnamestring;
								roomlist.get(roomnamestring).participants.add(this);
							}
							else {
								System.out.println("Room doesn't exists");
								synchronized (lock){
								Room tmp_room = new Room(roomnamestring,this);
								System.out.println("Map before a put"+roomlist.keySet());
								roomlist.put(roomnamestring,tmp_room);
								System.out.println("Map after a put"+roomlist.keySet());


								this.roomname = roomnamestring;
							 }
							}
						}
					}
					if(errorflag == 0){
						// send success response
						System.out.println("Sending succesful response");
						String msgend="";
						dos.writeInt(msgend.length()+1);
						int sendopcodehexint = 0x417;
						short sendopcodehex = (short) sendopcodehexint;
						dos.writeShort(sendopcodehex);
						byte sendOpcode = (byte)0xfe;
						dos.writeByte(sendOpcode);
						byte sendreturncode = (byte)0x00;
						dos.writeByte(sendreturncode);
						dos.writeBytes(msgend);
						dos.flush();
						synchronized(lock) {
						if(oldroomstring.length()>0){
						  roomlist.get(oldroomstring).participants.remove(this);
						  if(roomlist.get(oldroomstring).participants.size()==0){
							  roomlist.remove(oldroomstring);

						  }
					  }
            }
					}
			}
			else if(opcodeint==11){
				String msgend="";
				dos.writeInt(msgend.length()+1);
				int sendopcodehexint = 0x417;
				short sendopcodehex = (short) sendopcodehexint;
				dos.writeShort(sendopcodehex);
				byte sendOpcode = (byte)0xfe;
				dos.writeByte(sendOpcode);
				byte sendreturncode = (byte)0x00;
				dos.writeByte(sendreturncode);
				dos.writeBytes(msgend);
				dos.flush();
				if(this.roomname.length()>0){
					String tmp_name = this.roomname;

					this.roomname ="";
					synchronized(lock){
					roomlist.get(tmp_name).participants.remove(this);
					if (roomlist.get(tmp_name).participants.size()==0){
						roomlist.remove(tmp_name);
					}
				 }
				}
				else {
			  try
			  {
				// closing resources
				  System.out.println("Leaving");
					String oldname = this.name;
					clientlist.remove(this.name);
					nametakenlist.remove(this.name);

					if(oldname.length()>=5){
						String isRand = oldname.substring(0,4);
						String rand = "rand";
						if(isRand.equals(rand)) {
							try {

							int num = Integer.parseInt(oldname.substring(4));
								System.out.println("Num is"+num);
							synchronized (lock){
							randlist.remove(num);
							}
							}
							catch(Exception e){
								;
							}
						}
					}
				  this.dis.close();
				  this.dos.close();
					cond =false;
					break;
					//

			  }catch(IOException e){
				  e.printStackTrace();
			  }
			}
      }
			else if(opcodeint==12){
				synchronized(lock){
				HashMap<String,Integer> tmp_map = new HashMap<String,Integer>();
				int len_counter=0;
				for(String s:roomlist.keySet()){
					len_counter = len_counter + s.length();
					tmp_map.put(s,s.length());
				}
				if(roomlist.keySet().size()==0){
					tmp_map.put("",0);
				}
				dos.writeInt(len_counter+tmp_map.size()+1);
				int sendopcodehexint = 0x417;
				short sendopcodehex = (short) sendopcodehexint;
				dos.writeShort(sendopcodehex);
				byte sendOpcode = (byte)0xfe;
				dos.writeByte(sendOpcode);
				byte sendreturncode = (byte)0x00;
				dos.writeByte(sendreturncode);
				for(String i:tmp_map.keySet()){
					byte len2 = (byte)((int)tmp_map.get(i));
					dos.writeByte(len2);
					dos.writeBytes(i);
				}
			}
			}
			else if(opcodeint==13){
			synchronized(lock){

				if(this.roomname.length()>0){
					HashMap<String,Integer> tmp_map = new HashMap<String,Integer>();
					int len_counter=0;
					for(ClientHandler c: roomlist.get(this.roomname).participants){
						String s = new String(c.name);

						len_counter = len_counter + s.length();
						tmp_map.put(s,s.length());
					}
				  if(roomlist.get(this.roomname).participants.size()==0){
						tmp_map.put("",0);
					}
					dos.writeInt(len_counter+tmp_map.size()+1);
					int sendopcodehexint = 0x417;
					short sendopcodehex = (short) sendopcodehexint;
					dos.writeShort(sendopcodehex);
					byte sendOpcode = (byte)0xfe;
					dos.writeByte(sendOpcode);
					byte sendreturncode = (byte)0x00;
					dos.writeByte(sendreturncode);
					for(String i:tmp_map.keySet()){
						byte len2 = (byte)((int)tmp_map.get(i));
						dos.writeByte(len2);
						dos.writeBytes(i);
					}
				}
				else {
					HashMap<String,Integer> tmp_map = new HashMap<String,Integer>();
					int len_counter=0;
					for(String s:clientlist.keySet()){
						len_counter = len_counter + s.length();
						tmp_map.put(s,s.length());
					}
					if(clientlist.keySet().size()==0){
						tmp_map.put("",0);
					}
					dos.writeInt(len_counter+tmp_map.size()+1);
					int sendopcodehexint = 0x417;
					short sendopcodehex = (short) sendopcodehexint;
					dos.writeShort(sendopcodehex);
					byte sendOpcode = (byte)0xfe;
					dos.writeByte(sendOpcode);
					byte sendreturncode = (byte)0x00;
					dos.writeByte(sendreturncode);
					for(String i:tmp_map.keySet()){
						byte len3 = (byte)((int)tmp_map.get(i));
						dos.writeByte(len3);
						dos.writeBytes(i);
					}
				}
			}
		}
			else if(opcodeint==14){
				String oldname = this.name;
				byte [] newnamelen = new byte[1];
				dis.read(newnamelen);
				int x = newnamelen[0];
				byte[] newname = new byte[x];
				dis.read(newname);
				String newnamestring=new String(newname);
				if(nametakenlist.contains(newnamestring) && (newnamestring.equals(oldname) == false) ){
					//error, name's taken
					String msgend="This nick has been nicked by someone else.";
					dos.writeInt(msgend.length()+1);
					int sendopcodehexint = 0x417;
					short sendopcodehex = (short) sendopcodehexint;
					dos.writeShort(sendopcodehex);
					byte sendOpcode = (byte)0xfe;
					dos.writeByte(sendOpcode);
					byte sendreturncode = (byte)0x01;
					dos.writeByte(sendreturncode);
					dos.writeBytes(msgend);
					dos.flush();
				}
				else {
					if(oldname.equals(newnamestring)){
						// just send the empty respone
						String msgend="";
						dos.writeInt(msgend.length()+1);
						int sendopcodehexint = 0x417;
						short sendopcodehex = (short) sendopcodehexint;
						dos.writeShort(sendopcodehex);
						byte sendOpcode = (byte)0xfe;
						dos.writeByte(sendOpcode);
						byte sendreturncode = (byte)0x00;
						dos.writeByte(sendreturncode);
						dos.writeBytes(msgend);
						dos.flush();
					}
					else{
						// check for if old name is a rand name,deal, then send the success empty respons ;
						boolean randflag = false;
						if(oldname.length()>=5){
						  String isRand = oldname.substring(0,4);
							String rand = "rand";
							if(isRand.equals(rand)) {
								try {
								int num = Integer.parseInt(oldname.substring(4));
								synchronized (lock){
								randlist.remove(num);
							  }
							  }
								catch(Exception e){
									randflag = false;
								}
							}
						}
						synchronized (lock){
						nametakenlist.remove(oldname);
						this.name = newnamestring;
						nametakenlist.add(newnamestring);
						clientlist.remove(oldname);
						clientlist.put(newnamestring,this);
					 }
						// send empty response
						String msgend="";
						dos.writeInt(msgend.length()+1);
						int sendopcodehexint = 0x417;
						short sendopcodehex = (short) sendopcodehexint;
						dos.writeShort(sendopcodehex);
						byte sendOpcode = (byte)0xfe;
						dos.writeByte(sendOpcode);
						byte sendreturncode = (byte)0x00;
						dos.writeByte(sendreturncode);
						dos.writeBytes(msgend);
						dos.flush();
					}
				}
			}
			else if(opcodeint == 15 ){
				byte [] sendmsgtolen = new byte[1];
				dis.read(sendmsgtolen);
				int x = sendmsgtolen[0];
				byte[] sendto= new byte[x];
				dis.read(sendto);
				String sendtostring=new String(sendto);
				short msglen = dis.readShort();
				int msglenint = msglen;
				byte[] messagebytes= new byte[msglenint];
				dis.read(messagebytes);
				String message = new String(messagebytes);
				if(!clientlist.containsKey(sendtostring)){
					// error condition. guy who we're sending message do dne
					String msgend="Nick not present";
					dos.writeInt(msgend.length()+1);
					int sendopcodehexint = 0x417;
					short sendopcodehex = (short) sendopcodehexint;
					dos.writeShort(sendopcodehex);
					byte sendOpcode = (byte)0xfe;
					dos.writeByte(sendOpcode);
					byte sendreturncode = (byte)0x01;
					dos.writeByte(sendreturncode);
					dos.writeBytes(msgend);
				}
				else{
					// sending to recipient
					int sendlength = message.length()+3+this.name.length();
					clientlist.get(sendtostring).dos.writeInt(sendlength);
					int sendopcodehexint = 0x417;
					short sendopcodehex = (short) sendopcodehexint;
					clientlist.get(sendtostring).dos.writeShort(sendopcodehex);
					byte sendOpcode = (byte)0x0f;
					clientlist.get(sendtostring).dos.writeByte(sendOpcode);
					byte sendernamelen = (byte)this.name.length();
					clientlist.get(sendtostring).dos.writeByte(sendernamelen);
					clientlist.get(sendtostring).dos.writeBytes(this.name);
					clientlist.get(sendtostring).dos.writeShort(msglen);
					clientlist.get(sendtostring).dos.writeBytes(message);
					// sending success ack
					String msgend="";
					dos.writeInt(msgend.length()+1);
					int sendopcodehexint1 = 0x417;
					short sendopcodehex1 = (short) sendopcodehexint1;
					dos.writeShort(sendopcodehex1);
					byte sendOpcode1 = (byte)0xfe;
					dos.writeByte(sendOpcode1);
					byte sendreturncode1 = (byte)0x00;
					dos.writeByte(sendreturncode1);
					dos.writeBytes(msgend);


				}

			}
			else if(opcodeint == 16){
				byte [] sendmsgtolen = new byte[1];
				dis.read(sendmsgtolen);
				int x = sendmsgtolen[0];
				byte[] sendto= new byte[x];
				dis.read(sendto);
				String sendtoroomstring=new String(sendto);
				short msglen = dis.readShort();
				int msglenint = msglen;
				byte[] messagebytes= new byte[msglenint];
				dis.read(messagebytes);
				String message = new String(messagebytes);
				if(!this.roomname.equals(sendtoroomstring)){
					// error condition
					String msgend="You shout into the void and hear nothing.";
					dos.writeInt(msgend.length()+1);
					int sendopcodehexint = 0x417;
					short sendopcodehex = (short) sendopcodehexint;
					dos.writeShort(sendopcodehex);
					byte sendOpcode = (byte)0xfe;
					dos.writeByte(sendOpcode);
					byte sendreturncode = (byte)0x01;
					dos.writeByte(sendreturncode);
					dos.writeBytes(msgend);
				}
				else {
					for(ClientHandler c : roomlist.get(sendtoroomstring).participants){
						if(!c.name.equals(this.name)){
					    int sendlength = message.length()+4+this.roomname.length()+this.name.length();
					    c.dos.writeInt(sendlength);
					    int sendopcodehexint = 0x417;
					    short sendopcodehex = (short) sendopcodehexint;
					    c.dos.writeShort(sendopcodehex);
					    byte sendOpcode = (byte)0x10;
					    c.dos.writeByte(sendOpcode);
							byte roomnamelen = (byte) this.roomname.length();
							c.dos.writeByte(roomnamelen);
							c.dos.writeBytes(this.roomname);
					    byte sendernamelen = (byte)this.name.length();
					    c.dos.writeByte(sendernamelen);
					    c.dos.writeBytes(this.name);
					    c.dos.writeShort(msglen);
					    c.dos.writeBytes(message);
					  }
					}
					String msgend="";
					dos.writeInt(msgend.length()+1);
					int sendopcodehexint1 = 0x417;
					short sendopcodehex1 = (short) sendopcodehexint1;
					dos.writeShort(sendopcodehex1);
					byte sendOpcode1 = (byte)0xfe;
					dos.writeByte(sendOpcode1);
					byte sendreturncode1 = (byte)0x00;
					dos.writeByte(sendreturncode1);
					dos.writeBytes(msgend);

				}
			}
			} catch (IOException e) {
				//e.printStackTrace();
				// remove all traces of this client
				// take care of the rand situation
				System.out.println("Quit");
				String oldname = this.name;
				clientlist.remove(this.name);
				nametakenlist.remove(this.name);
				if(this.roomname.length()>0){
					roomlist.get(this.roomname).participants.remove(this);
				if(roomlist.get(this.roomname).participants.size()==0){
					roomlist.remove(this.roomname);
				}
			 }
				if(oldname.length()>=5){
					String isRand = oldname.substring(0,4);
					String rand = "rand";
					if(isRand.equals(rand)) {
						try {
						int num = Integer.parseInt(oldname.substring(4));
						System.out.println("Num we're removing is:"+num);
						synchronized (lock){
						randlist.remove(num);
						}
						}
						catch(Exception e2){
							;
						}
					}
				}
				cond=false;
				//
				break;
			}
		}


	}
}
