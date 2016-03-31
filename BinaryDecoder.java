/* This class provides the 2 methods to parse a byte array into 
 * an predefined Layout provided by the user
 * 
 * the Layout class needs to implements the DecodeLayout interface
 * the currently supported Field types are byte(8 bit), short(16 bit), int(32 bit)
 * also the array types are support. the user needs to specify the length of the array
 * 
 * you can also include other classes which implement the DecodeLayout interface
 * WARNING: these classes must be inner(nested) classes of the parent class
 * Arrays of self defined DecodeLayouts are also allowed
 * 
 * if the user wants to receive events before a field is parsed(eg. to Change the size of this array
 * depending on an other already parsed field) implement the DecodeEvent interface
 * 
 * check the provided examples
 */

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Islidius
 * @version 1.0
 */
public class BinaryDecoder{
	/**
	 * @param layout an Object which defines the Layout for the parsing
	 * @param bytes the bytes to be parsed into the Layout
	 * @return offset after the parsing
	 */
	public static int decode(DecodeLayout layout,byte[] bytes){ // Decode from beginning
		return BinaryDecoder.decode(layout,bytes,0);
	}
	
	/**
	 * @param layout an Object which defines the Layout for the parsing
	 * @param bytes the bytes to be parsed into the Layout
	 * @param offset the starting point for the parsing
	 * @return the offset after the parsing
	 */
	public static int decode(DecodeLayout layout,byte[] bytes,int offset){ // Decode
		Field[] fields = layout.getClass().getDeclaredFields(); // get all fields
		for(Field f:fields){
			if(f.getModifiers() == 0 || f.getModifiers() == 1){ // if puplic or no modifier
				if(BinaryDecoder.hasEvents(layout.getClass())){ // if event is requested
					((DecodeEvent)layout).beforeParse(f.getName(), offset); // fire event befor parsing the symbol
				}
				
				if(f.getType().equals(int.class)){ // parse single int
					setField(f,layout,conToInt(bytes,offset));
					offset += 4;
				}
				else if(f.getType().equals(short.class)){ // parse single short
					setField(f,layout,conToShort(bytes,offset));
					offset += 2;
				}
				else if(f.getType().equals(byte.class)){ // parse single byte
					setField(f,layout,bytes[offset]);
					offset += 1;
				}
				else if(f.getType().equals(byte[].class)){ // parse byte array 
					int len = getLengthByte(f,layout); // read the length for the array
					byte[] bs = new byte[len];
					for(int j = 0;j<len;j++){
						bs[j] = bytes[offset];
						offset += 1;
					}
					
					setField(f,layout,bs);
				}
				
				else if(f.getType().equals(short[].class)){ // parse short array
					int len = getLengthShort(f,layout);
					short[] bs = new short[len];
					for(int j = 0;j<len;j++){
						bs[j] = conToShort(bytes,offset);
						offset += 2;
					}
					
					setField(f,layout,bs);
				}
				
				else if(f.getType().equals(int[].class)){ // parse int array
					int len = getLengthInt(f,layout);
					int[] bs = new int[len];
					for(int j = 0;j<len;j++){
						bs[j] = conToInt(bytes,offset);
						offset += 4;
					}
					
					setField(f,layout,bs);
				}
				else{ // parse other type
					if(f.getType().isArray()){
						if(hasInterface(f.getType().getComponentType())){ // implemetns DecodeRequest
							int len = BinaryDecoder.getLengthObject(f, layout);
							Object oarr = Array.newInstance(f.getType().getComponentType(), len); // create array
							
							for(int j = 0;j<len;j++){
								DecodeLayout in = BinaryDecoder.createNewInstance(f.getType().getComponentType(), layout); // create new Instance
								offset = BinaryDecoder.decode(in, bytes, offset);
								Array.set(oarr, j, in);
							}
							
							setField(f,layout,f.getType().cast(oarr)); // set the field after cast
						}
					}
					else{
						if(hasInterface(f.getType())){ // implements DecodeRequest
							offset = BinaryDecoder.decode(getInputFieldnew(f,layout), bytes, offset); // rekursive decode
						}
					}
				}
			}
		}
		return offset;
	}
	
	private static DecodeLayout getInputField(Field f,DecodeLayout layout){ // get Field (not a base type)
		Object o = null;
		try {
			o = f.get(layout);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return (DecodeLayout)o;
	}
	
	private static DecodeLayout getInputFieldnew(Field f,DecodeLayout layout){ // get Field if null create new
		DecodeLayout in = BinaryDecoder.getInputField(f, layout);
		if(in == null){
			try {
				DecodeLayout inew = (DecodeLayout) f.getType().getDeclaredConstructor(new Class[] { layout.getClass() }).newInstance(new Object[] { layout });
				BinaryDecoder.setField(f, layout, inew); // create new and set
				return inew;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
		}
		return in;
	}
	
	private static DecodeLayout createNewInstance(Class<?> c,DecodeLayout layout){ // create a new instance with i the parent class
		try {
			DecodeLayout inew = (DecodeLayout) c.getDeclaredConstructor(new Class[] { layout.getClass() }).newInstance(new Object[] { layout });
			return inew;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static boolean hasInterface(Class<?> c){ // check if the class has the DecodeRequest interface
		AnnotatedType[] interfaces = c.getAnnotatedInterfaces();
		for(AnnotatedType at:interfaces){
			if(at.getType().equals(BinaryDecoder.DecodeLayout.class)){
				return true;
			}
		}
		return false;
	}
	
	private static boolean hasEvents(Class<?> c){ // check if the class Requests Events
		AnnotatedType[] interfaces = c.getAnnotatedInterfaces();
		for(AnnotatedType at:interfaces){
			if(at.getType().equals(BinaryDecoder.DecodeEvent.class)){
				return true;
			}
		}
		return false;
	}
	
	private static int getLengthByte(Field f,DecodeLayout layout){ // get the length of an byte array
		try {
			byte[] b = (byte[]) f.get(layout);
			return b.length;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	private static int getLengthShort(Field f,DecodeLayout layout){ // get the length of a short array 
		try {
			short[] b = (short[]) f.get(layout);
			return b.length;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	private static int getLengthInt(Field f,DecodeLayout layout){ // get the length of an int array
		try {
			int[] b = (int[]) f.get(layout);
			return b.length;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	private static int getLengthObject(Field f,DecodeLayout layout){ // get the length of an Object array
		try {
			Object[] b = (Object[]) f.get(layout);
			return b.length;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	
	private static void setField(Field f,DecodeLayout layout,Object o){ // set a field in i
		try {
			f.set(layout, o);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	
	public static int conToInt(byte b0,byte b1,byte b2,byte b3){ // convert to int
		return b0 << 24 | b1 << 16 | b2 << 8 | b3;
	}
	
	public static int conToInt(byte[] bytes,int offset){ // convert to int
		return bytes[offset + 0] << 24 | bytes[offset + 1] << 16 | bytes[offset + 2] << 8 | bytes[offset + 3];
	}
	
	public static short conToShort(byte b0,byte b1){ // convert to short
		return (short) (b0 << 8 | b1);
	}
	
	public static short conToShort(byte[] bytes,int offset){ // convert to short
		return (short) (bytes[offset + 0] << 8 | bytes[offset + 1]);
	}
	

	interface DecodeLayout{};	// interface definition
	interface DecodeEvent{
		/**
		 * @param symbolname the name of the variable which will be parsed next
		 * @param offset the offset at the moment
		 */
		void  beforeParse(String symbolname,int offset);
	}
}
