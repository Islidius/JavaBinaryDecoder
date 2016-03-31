
public class EventDemo {

	public static void main(String[] args){
		new EventDemo();
	}
	
	public EventDemo(){
		byte[] bytes = {100,100 ,0,5 ,1,20 ,2,32 ,3,50 ,4,70 ,5,123 ,0,0,0,0};
		// the two bytes need to be skip (eg. not needed header)
		Layout layout = new Layout();
		BinaryDecoder.decode(layout, bytes, 2); // 2 bytes will be skipped
		
		System.out.println("length: " + layout.length);
		for(Layout.Entry e:layout.entries){
			System.out.println("Entry: id = " + e.id +" ,offset = " + e.offset);
		}
		System.out.println("checksum: "+layout.checksum);
	}
	
	class Layout implements BinaryDecoder.DecodeLayout,BinaryDecoder.DecodeEvent{

		short length;
		Entry[] entries; // length will be set later 
		int checksum;
		
		@Override
		public void beforeParse(String symbolname, int offset) {
			if(symbolname.equals("entries")){ // the entries field will be parsed next
				entries = new Entry[length]; // set its length to the already parsed length
			}
		}
		
		class Entry implements BinaryDecoder.DecodeLayout{ // one entry
			byte id;
			byte offset;
		}
	}
	
}
