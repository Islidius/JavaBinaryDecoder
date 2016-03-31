# JavaBinaryDecoder
parses a byte array into a given Layout


This BinaryDecoder provides the 2 methods to parse a byte array into 
an predefined Layout provided by the user
 
the Layout class needs to implements the DecodeLayout interface
the currently supported Field types are byte(8 bit), short(16 bit), int(32 bit)
also the array types are support. the user needs to specify the length of the array
 
you can also include other classes which implement the DecodeLayout interface
WARNING: these classes must be inner(nested) classes of the parent class
Arrays of self defined DecodeLayouts are also allowed
 
if the user wants to receive events before a field is parsed(eg. to Change the size of this array
depending on an other already parsed field) implement the DecodeEvent interface

be sure to check the provided examples
