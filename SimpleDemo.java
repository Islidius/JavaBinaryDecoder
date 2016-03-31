
public class SimpleDemo {
	public static void main(String[] args){
		new SimpleDemo();
	}
	
	public SimpleDemo(){
		byte[] bytes = {0,0,0,1,0,2,3,4};
		
		Layout layout = new Layout(); // create the LayoutObject
		BinaryDecoder.decode(layout, bytes); // parse the Bytes onto the Layout
		
		System.out.println("Interger: " + layout.i); // print out the Layout fields
		System.out.println("Short: " + layout.s);
		System.out.println("Byte: " + layout.b1);
		System.out.println("Byte: " + layout.b2);
	}
	
	class Layout implements BinaryDecoder.DecodeLayout{
		int i; // will be the first 4 bytes
		short s; // will be the 5 and 6 bytes
		byte b1,b2; // b1 will be the 7 and b2 the 8 byte
	}
}
