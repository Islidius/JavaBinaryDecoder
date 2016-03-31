
public class Demo {
	
	public static void main(String[] args){
		new Demo();
	}
	
	public Demo(){
		byte[] bytes = {1, 0, 1,2 ,0,0 ,0,0 ,1,6 ,0,0 ,5,2,0,1 ,8,8,8,8 ,0,80 ,0,80 ,0,0,0,0 ,0,0,0,0 ,0,0 ,0,0 ,0,0 ,0,0 ,1,2,3,4,5};
		// just an example not real world values
		
		IPHeader ipheader = new IPHeader(); // construct the Layout
		// in dataoffset will the offset saved after the layout is applied
		int dataoffset = BinaryDecoder.decode(ipheader, bytes); // parse into the ipheader
		
		TCPData tcpdata = new TCPData();
		tcpdata.data = new byte[bytes.length - dataoffset]; // the rest off the byte stream
		BinaryDecoder.decode(tcpdata, bytes, dataoffset); // decode the data after the tcpheader
		
		System.out.println("Protokol: " + (ipheader.ttl_proto & 0x000F)); // print the fields
		System.out.println("Source: " + ipheader.source_addr[0] + "." + ipheader.source_addr[1] + "." +
				ipheader.source_addr[2] + "." + ipheader.source_addr[3]);
		
		System.out.println("Dest: " + ipheader.dest_addr[0] + "." + ipheader.dest_addr[1] + "." +
				ipheader.dest_addr[2] + "." + ipheader.dest_addr[3]);
		
		System.out.println("SourcePort: " + ipheader.tcpheader.sourceport); // you can access the tcpheader field and 
		System.out.println("DestPort: " + ipheader.tcpheader.destport); // all the sub fields
		
		System.out.print("Data: ");
		for(byte b:tcpdata.data){ // print out the data
			System.out.print(b+" ");
		}
		System.out.println();
		
	}
	
	class IPHeader implements BinaryDecoder.DecodeLayout{
		byte version;
		byte serviceType;
		short totalLength;
		short identification;
		short flags;
		short ttl_proto;
		short checksum;
		byte[] source_addr = new byte[4]; // arrays need to be set with a length
		byte[] dest_addr = new byte[4];
		
		TCPHeader tcpheader; // 
		
		class TCPHeader implements BinaryDecoder.DecodeLayout{ // needs to be inner class
			short sourceport;
			short destport;
			int sequencenumber;
			int acknowledgmentnumber;
			short flags;
			short window;
			short checksum;
			short urgentpointer;
		}
	}
	
	class TCPData implements BinaryDecoder.DecodeLayout{
		byte[] data; // length will be set later
	}

}
