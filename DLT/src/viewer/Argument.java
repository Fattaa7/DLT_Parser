package viewer;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

abstract class Argument {
    protected static final int TYPE_INFO_LENGTH = 4;
    protected static Boolean msbFirstDefault = true; // or false, as per your logic

    protected Boolean msbFirst;

    public Argument(Boolean msbFirst) {
        this.msbFirst = msbFirst;
    }

    @Override
    public String toString() {
        return _toStr();
    }

    protected abstract int getTypeInfo();
    public abstract byte[] toBytes(Optional<Boolean> msbFirst);
    public abstract String _toStr();
    public abstract int getDataPayloadLength();
    public abstract byte[] dataPayloadToBytes(Boolean msbFirst);
    public abstract Argument fromDataPayload(byte[] data, Boolean msbFirst);

    public static Argument createFromBytes(byte[] data, Boolean msbFirst, Optional<String> encoding) {ByteOrder byteOrder = msbFirst ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
    ByteBuffer buffer = ByteBuffer.wrap(data).order(byteOrder);
    int typeInfo = buffer.getInt();
    int typeInfoBase = typeInfo & BitMasks.MASK_BASE_TYPE;
    Argument argument = null;

    if (typeInfoBase == TypeInfo.TYPE_BOOL.getValue()) {
        // Assuming Boolean needs to be extracted from byte[] for BOOL type
        boolean boolValue = data[0] != 0; // Assuming 0 = false, non-zero = true
        argument = new ArgumentBool(boolValue, msbFirst); // Directly creating instance
    } else if (typeInfoBase == TypeInfo.TYPE_SIGNED.getValue()) {
        int typeInfoLength = typeInfo & BitMasks.MASK_TYPE_LENGTH;
        if (typeInfoLength == TypeInfo.TYPE_LENGTH_8BIT.getValue()) {
            int int8Value = ByteBuffer.wrap(getDataPayload(data)).get(); // Converting 8-bit to int
            argument = new ArgumentSInt8(int8Value, msbFirst); // Directly creating instance
        } else if (typeInfoLength == TypeInfo.TYPE_LENGTH_16BIT.getValue()) {
            int int16Value = ByteBuffer.wrap(getDataPayload(data)).getShort(); // Converting 16-bit to int
            argument = new ArgumentSInt16(int16Value, msbFirst); // Directly creating instance
        } else if (typeInfoLength == TypeInfo.TYPE_LENGTH_32BIT.getValue()) {
            int int32Value = buffer.getInt(); // Get 32-bit signed integer
            argument = new ArgumentSInt32(int32Value, msbFirst); // Directly creating instance
        } else if (typeInfoLength == TypeInfo.TYPE_LENGTH_64BIT.getValue()) {
            long int64Value = buffer.getLong(); // Get 64-bit signed integer
            argument = new ArgumentSInt64(int64Value, msbFirst); // Directly creating instance
        } else {
            throw new IllegalArgumentException("Unsupported TypeInfo: " + Integer.toBinaryString(typeInfo));
        }
    } else if (typeInfoBase == TypeInfo.TYPE_UNSIGNED.getValue()) {
        int typeInfoLength = typeInfo & BitMasks.MASK_TYPE_LENGTH;
        if (typeInfoLength == TypeInfo.TYPE_LENGTH_8BIT.getValue()) {
            int uint8Value = ByteBuffer.wrap(getDataPayload(data)).get(); // Unsigned 8-bit to integer
            argument = new ArgumentUInt8(uint8Value, msbFirst); // Directly creating instance
        } else if (typeInfoLength == TypeInfo.TYPE_LENGTH_16BIT.getValue()) {
            int uint16Value = ByteBuffer.wrap(getDataPayload(data)).getShort(); // Unsigned 16-bit to integer
            argument = new ArgumentUInt16(uint16Value, msbFirst); // Directly creating instance
        } else if (typeInfoLength == TypeInfo.TYPE_LENGTH_32BIT.getValue()) {
            int uint32Value = buffer.getInt(); // Unsigned 32-bit to integer
            argument = new ArgumentUInt32(uint32Value, msbFirst); // Directly creating instance
        } else if (typeInfoLength == TypeInfo.TYPE_LENGTH_64BIT.getValue()) {
            long uint64Value = buffer.getLong(); // Unsigned 64-bit integer
            argument = new ArgumentUInt64(uint64Value, msbFirst); // Directly creating instance
        } else {
            throw new IllegalArgumentException("Unsupported TypeInfo: " + Integer.toBinaryString(typeInfo));
        }
    } else if (typeInfoBase == TypeInfo.TYPE_FLOAT.getValue()) {
        int typeInfoLength = typeInfo & BitMasks.MASK_TYPE_LENGTH;
        if (typeInfoLength == TypeInfo.TYPE_LENGTH_32BIT.getValue()) {
            float float32Value = buffer.getFloat(); // Get 32-bit float
            argument = new ArgumentFloat32(float32Value, msbFirst); // Directly creating instance
        } else if (typeInfoLength == TypeInfo.TYPE_LENGTH_64BIT.getValue()) {
            double float64Value = buffer.getDouble(); // Get 64-bit float
            argument = new ArgumentFloat64(float64Value, msbFirst); // Directly creating instance
        } else {
            throw new IllegalArgumentException("Unsupported TypeInfo: " + Integer.toBinaryString(typeInfo));
        }
    } else if (typeInfoBase == TypeInfo.TYPE_STRING.getValue()) {
        int stringCoding = typeInfo & BitMasks.MASK_STRING_CODING;
        Charset charset = Charset.forName(encoding.get());  // Convert the String encoding to a Charset

        if (stringCoding == TypeInfo.STRING_CODING_ASCII.getValue()) {
            argument = ArgumentString.fromDataPayload(getDataPayload(data), false, msbFirst, charset); // Directly creating instance
        } else if (stringCoding == TypeInfo.STRING_CODING_UTF8.getValue()) {
            argument = ArgumentString.fromDataPayload(getDataPayload(data), true, msbFirst, charset); // Directly creating instance
        } else {
            throw new IllegalArgumentException("Unsupported String Coding: " + Integer.toBinaryString(typeInfo));
        }
    } else if (typeInfoBase == TypeInfo.TYPE_RAW.getValue()) {
        argument = new ArgumentRaw(getDataPayload(data), msbFirst); // Directly creating instance
    }

    if (argument == null) {
        throw new IllegalArgumentException("Unsupported TypeInfo: " + Integer.toBinaryString(typeInfo));
    }
    return argument;
}

    private static byte[] getDataPayload(byte[] data) {
        byte[] payload = new byte[data.length - TYPE_INFO_LENGTH];
        System.arraycopy(data, TYPE_INFO_LENGTH, payload, 0, payload.length);
        return payload;
    }
}


//////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////
// Implement other argument types (ArgumentSInt8, ArgumentSInt16, ArgumentSInt32, etc.)


// Follow the same pattern for other types: ArgumentSInt16, ArgumentSInt32, ArgumentSInt64, ArgumentUInt8, ArgumentUInt16, ArgumentUInt32, ArgumentUInt64, ArgumentFloat32, ArgumentFloat64, ArgumentString, ArgumentRaw
abstract class ArgumentNumBase extends Argument {
    protected Object data;

    public ArgumentNumBase(Object data, Boolean msbFirst) {
        super(msbFirst);
        this.data = data;
    }

    protected abstract String getStructFormat();
    public abstract int getDataPayloadLength();
    protected abstract int getTypeInfo();

    @Override
    public String _toStr() {
        return data.toString();
    }

    @Override
    public byte[] toBytes(Optional<Boolean> msbFirst) {
        return this.dataPayloadToBytes(msbFirst.get());
    }

    @Override
    public byte[] dataPayloadToBytes(Boolean msbFirst) {
        ByteOrder byteOrder = msbFirst != null
                ? (msbFirst ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN)
                : (this.msbFirst != null
                ? (this.msbFirst ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN)
                : ByteOrder.nativeOrder());

        ByteBuffer buffer = ByteBuffer.allocate(getDataPayloadLength()).order(byteOrder);
        switch (getStructFormat()) {
            case "?":
                buffer.put((byte) ((Boolean) data ? 1 : 0));
                break;
            case "B":
                buffer.put((byte) ((Integer) data & 0xFF));
                break;
            case "H":
                buffer.putShort((short) ((Integer) data & 0xFFFF));
                break;
            case "I":
                buffer.putInt((int) ((Long) data & 0xFFFFFFFF));
                break;
            case "Q":
                buffer.putLong((Long) data);
                break;
            case "b":
                buffer.put((byte) data);
                break;
            case "h":
                buffer.putShort((Short) data);
                break;
            case "i":
                buffer.putInt((Integer) data);
                break;
            case "q":
                buffer.putLong((Long) data);
                break;
            case "f":
                buffer.putFloat((Float) data);
                break;
            case "d":
                buffer.putDouble((Double) data);
                break;
            default:
                throw new IllegalArgumentException("Unsupported struct format: " + getStructFormat());
        }
        return buffer.array();
    }

    @Override
    public Argument fromDataPayload(byte[] dataPayload, Boolean msbFirst) {
        String structFormat = getStructFormatForClass(); // Call to return struct format for this class
        int dataLength = getDataPayloadLengthForClass(); // Call to return the data length for this class

        ByteOrder byteOrder = msbFirst ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
        ByteBuffer buffer = ByteBuffer.wrap(dataPayload).order(byteOrder);

        Object result = null;
        
        switch (structFormat) {
            case "?":
                result = buffer.get() == 1; // Boolean
                return new ArgumentBool((Boolean)result, msbFirst);
            case "B":
                result = (int) buffer.get(); // Byte (Integer casting)
                return new ArgumentUInt8((Integer)result, msbFirst);
            case "H":
                result = (int) buffer.getShort(); // Short to Integer
                return new ArgumentUInt16((Integer)result, msbFirst);
            case "I":
                result = (int) buffer.getInt(); // Integer
                return new ArgumentUInt32((Integer)result, msbFirst);
            case "Q":
                result = buffer.getLong(); // Long
                return new ArgumentUInt64((Long)result, msbFirst);
            case "b":
                result = (byte) buffer.get(); // Byte
                return new ArgumentSInt8((Integer)result, msbFirst);
            case "h":
                result = buffer.getShort(); // Short
                return new ArgumentSInt16((Integer)result, msbFirst);
            case "i":
                result = buffer.getInt(); // Integer
                return new ArgumentSInt32((Integer)result, msbFirst);
            case "q":
                result = buffer.getLong(); // Long
                return new ArgumentSInt64((Long)result, msbFirst);
            case "f":
                result = buffer.getFloat(); // Float
                return new ArgumentFloat32((Float)result, msbFirst);
            case "d":
                result = buffer.getDouble(); // Double
                return new ArgumentFloat64((Double)result, msbFirst);
            default:
                throw new IllegalArgumentException("Unsupported struct format: " + structFormat);
        }
    }

    // Methods for determining struct format and payload length based on subclass
    protected static String getStructFormatForClass() {
        return "I";  // Example format, adjust as per your classes.
    }

    protected static int getDataPayloadLengthForClass() {
        return 4;  // Example length, adjust as needed.
    }
}


class ArgumentBool extends ArgumentNumBase {
    public ArgumentBool(Boolean data, Boolean msbFirst) {
        super(data, msbFirst);
    }

    @Override
    protected int getTypeInfo() {
        return TypeInfo.TYPE_BOOL.getValue() | TypeInfo.TYPE_LENGTH_8BIT.getValue();
    }

    @Override
    protected String getStructFormat() {
        return "?";
    }

    @Override
	public int getDataPayloadLength() {
        return 1;
    }
}

class ArgumentUInt8 extends ArgumentNumBase {
    public ArgumentUInt8(Integer data, Boolean msbFirst) {
        super(data, msbFirst);
    }

    @Override
    protected int getTypeInfo() {
        return TypeInfo.TYPE_UNSIGNED.getValue() | TypeInfo.TYPE_LENGTH_8BIT.getValue();
    }

    @Override
    protected String getStructFormat() {
        return "B";
    }

    @Override
	public int getDataPayloadLength() {
        return 1;
    }

}
class ArgumentUInt16 extends ArgumentNumBase {
    public ArgumentUInt16(Integer data, Boolean msbFirst) {
        super(data, msbFirst);
    }

    @Override
    protected int getTypeInfo() {
        return TypeInfo.TYPE_UNSIGNED.getValue() | TypeInfo.TYPE_LENGTH_16BIT.getValue();
    }

    @Override
    protected String getStructFormat() {
        return "H";
    }

    @Override
	public int getDataPayloadLength() {
        return 2;
    }
}

class ArgumentUInt32 extends ArgumentNumBase {
    public ArgumentUInt32(Integer data, Boolean msbFirst) {
        super(data, msbFirst);
    }

    @Override
    protected int getTypeInfo() {
        return TypeInfo.TYPE_UNSIGNED.getValue() | TypeInfo.TYPE_LENGTH_32BIT.getValue();
    }

    @Override
    protected String getStructFormat() {
        return "I";
    }

    @Override
	public int getDataPayloadLength() {
        return 4;
    }
}

class ArgumentUInt64 extends ArgumentNumBase {
    public ArgumentUInt64(Long data, Boolean msbFirst) {
        super(data, msbFirst);
    }

    @Override
    protected int getTypeInfo() {
        return TypeInfo.TYPE_UNSIGNED.getValue() | TypeInfo.TYPE_LENGTH_64BIT.getValue();
    }

    @Override
    protected String getStructFormat() {
        return "Q";
    }

    @Override
	public int getDataPayloadLength() {
        return 8;
    }
}


class ArgumentSInt8 extends ArgumentNumBase {
    public ArgumentSInt8(Integer data, Boolean msbFirst) {
        super(data, msbFirst);
    }

    @Override
    protected int getTypeInfo() {
        return TypeInfo.TYPE_SIGNED.getValue() | TypeInfo.TYPE_LENGTH_8BIT.getValue();
    }

    @Override
    protected String getStructFormat() {
        return "b";
    }

    @Override
	public int getDataPayloadLength() {
        return 1;
    }
}


class ArgumentSInt16 extends ArgumentNumBase {
    public ArgumentSInt16(Integer data, Boolean msbFirst) {
        super(data, msbFirst);
    }

    @Override
    protected int getTypeInfo() {
        return TypeInfo.TYPE_SIGNED.getValue() | TypeInfo.TYPE_LENGTH_16BIT.getValue();
    }

    @Override
    protected String getStructFormat() {
        return "h";
    }

    @Override
	public int getDataPayloadLength() {
        return 2;
    }
}

class ArgumentSInt32 extends ArgumentNumBase {
    public ArgumentSInt32(Integer data, Boolean msbFirst) {
        super(data, msbFirst);
    }

    @Override
    protected int getTypeInfo() {
        return TypeInfo.TYPE_SIGNED.getValue() | TypeInfo.TYPE_LENGTH_32BIT.getValue();
    }

    @Override
    protected String getStructFormat() {
        return "i";
    }

    @Override
	public int getDataPayloadLength() {
        return 4;
    }
}

class ArgumentSInt64 extends ArgumentNumBase {
    public ArgumentSInt64(long data, Boolean msbFirst) {
        super(data, msbFirst);
    }

    @Override
    protected int getTypeInfo() {
        return TypeInfo.TYPE_SIGNED.getValue() | TypeInfo.TYPE_LENGTH_64BIT.getValue();
    }

    @Override
    protected String getStructFormat() {
        return "q";
    }

    @Override
	public int getDataPayloadLength() {
        return 8;
    }
}

class ArgumentFloat32 extends ArgumentNumBase {
    public ArgumentFloat32(Float data, Boolean msbFirst) {
        super(data, msbFirst);
    }

    @Override
    protected int getTypeInfo() {
        return TypeInfo.TYPE_FLOAT.getValue() | TypeInfo.TYPE_LENGTH_32BIT.getValue();
    }

    @Override
    protected String getStructFormat() {
        return "f";
    }

    @Override
	public int getDataPayloadLength() {
        return 4;
    }
}

class ArgumentFloat64 extends ArgumentNumBase {
    public ArgumentFloat64(Double data, Boolean msbFirst) {
        super(data, msbFirst);
    }

    @Override
    protected int getTypeInfo() {
        return TypeInfo.TYPE_FLOAT.getValue() | TypeInfo.TYPE_LENGTH_64BIT.getValue();
    }

    @Override
    protected String getStructFormat() {
        return "d";
    }

    @Override
	public int getDataPayloadLength() {
        return 8;
    }
}






// Add the remaining classes (ArgumentUInt16, ArgumentUInt32, ArgumentUInt64, ArgumentSInt8, etc.)
// using the same pattern, ensuring the `getStructFormat` and `getDataPayloadLength` align with the Python version.


// Raw Argument - placeholder for raw bytes.

abstract class ArgumentByteBase extends Argument {
    // Constant for the size of the length field (2 bytes)
    protected static final int LENGTH_SIZE = 2;

    public ArgumentByteBase(Boolean msbFirst) {
        super(msbFirst);
    }

    /**
     * Get the total length of the data payload, including the length field and the data.
     * 
     * @return the total length of the data payload.
     */
    public int getDataPayloadLength() {
        return LENGTH_SIZE + getDataLength();
    }

    /**
     * Abstract method to get the data length.
     * Subclasses must implement this method.
     * 
     * @return the length of the data.
     */
    protected abstract int getDataLength();

    /**
     * Converts the data payload to bytes, including the length field and the actual data.
     * 
     * @param msbFirst Determines the byte order: true for big-endian, false for little-endian.
     *                 If null, uses the default for the instance or a global default.
     * @return the byte array representing the data payload.
     */
    @Override
    public byte[] dataPayloadToBytes(Boolean msbFirst) {
        ByteOrder byteOrder = getByteOrder(msbFirst);

        ByteBuffer buffer = ByteBuffer.allocate(getDataPayloadLength()).order(byteOrder);
        buffer.putShort((short) getDataLength());
        buffer.put(dataToBytes());
        return buffer.array();
    }

    /**
     * Abstract method to convert the actual data to bytes.
     * Subclasses must implement this method.
     * 
     * @return the byte array representing the actual data.
     */
    protected abstract byte[] dataToBytes();

    /**
     * Determines the byte order based on the provided value, instance default, or global default.
     * 
     * @param msbFirst The explicit byte order (true for big-endian, false for little-endian).
     * @return the resolved byte order.
     */
    private ByteOrder getByteOrder(Boolean msbFirst) {
        if (msbFirst != null) {
            return msbFirst ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
        } else if (this.msbFirst != null) {
            return this.msbFirst ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
        } else if (Argument.msbFirstDefault != null) {
            return Argument.msbFirstDefault ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
        } else {
            throw new IllegalArgumentException("Endian is not known");
        }
    }

    /**
     * Converts the given data payload (byte array) into an Argument instance.
     * 
     * @param dataPayload The raw byte data representing the payload.
     * @param msbFirst Determines the byte order: true for big-endian, false for little-endian.
     * @return The converted Argument instance.
     */
    @Override
    public Argument fromDataPayload(byte[] dataPayload, Boolean msbFirst) {
    	return null;
    }
}

class ArgumentString extends ArgumentByteBase {
    private final String data;
    private final boolean isUtf8;
    private final Charset encoding;

    public ArgumentString(String data, boolean isUtf8, Boolean msbFirst, Charset encoding) {
        super(msbFirst);
        this.data = data;
        this.isUtf8 = isUtf8;
        // Use UTF-8 if isUtf8 is true, else fallback to a custom charset or US-ASCII
        this.encoding = isUtf8 ? StandardCharsets.UTF_8 : (encoding != null ? encoding : StandardCharsets.US_ASCII);
    }

    @Override
    protected int getDataLength() {
        // Calculate the data length plus one for the null terminator
        return data.getBytes(encoding).length + 1;
    }

    @Override
    protected byte[] dataToBytes() {
        // Get the bytes for the string based on the encoding
        byte[] encodedData = data.getBytes(encoding);
        byte[] result = new byte[encodedData.length + 1]; // Add 1 byte for null terminator
        System.arraycopy(encodedData, 0, result, 0, encodedData.length);
        result[encodedData.length] = 0; // Null terminator
        return result;
    }

    public static ArgumentString fromDataPayload(byte[] dataPayload, boolean isUtf8, Boolean msbFirst, Charset encoding) {
        ByteOrder byteOrder = (msbFirst != null && msbFirst) ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
        ByteBuffer buffer = ByteBuffer.wrap(dataPayload).order(byteOrder);
        int length = Short.toUnsignedInt(buffer.getShort()); // Get length

        // Determine the encoding used
        Charset selectedEncoding = isUtf8 ? StandardCharsets.UTF_8 : (encoding != null ? encoding : StandardCharsets.US_ASCII);

        // Decode the string (excluding the length and null terminator)
        String data = new String(dataPayload, LENGTH_SIZE, length - 1, selectedEncoding);

        return new ArgumentString(data, isUtf8, msbFirst, encoding);
    }

 

    public int getTypeInfo() {
        int typeInfo = TypeInfo.TYPE_STRING.getValue();
        if (isUtf8) {
            typeInfo |= TypeInfo.STRING_CODING_UTF8.getValue();
        }
        return typeInfo;
    }

    @Override
    public byte[] toBytes(Optional<Boolean> msbFirst) {
        if (msbFirst == null) {
            throw new IllegalArgumentException("msbFirst cannot be null");
        }

        // Determine byte order based on msbFirst flag
        ByteOrder byteOrder = msbFirst.get() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;

        // Create ByteBuffer for the serialized data: 2 bytes for length, data, null terminator
        ByteBuffer buffer = ByteBuffer.allocate(2 + data.getBytes(encoding).length + 1);
        buffer.order(byteOrder);

        // Add the length field (2 bytes)
        buffer.putShort((short) getDataLength());

        // Add the actual data encoded
        buffer.put(data.getBytes(encoding));

        // Add null terminator at the end
        buffer.put((byte) 0);

        return buffer.array();
    }

    @Override
    public String _toStr() {
        return data; // Same as toStr in this case
    }
}


class ArgumentRaw extends ArgumentByteBase {
    private final byte[] data;

    public ArgumentRaw(byte[] data, Boolean msbFirst) {
        super(msbFirst);
        this.data = data;
    }

    @Override
	public String _toStr() {
        // Return hexadecimal representation of the data
        StringBuilder hexString = new StringBuilder();
        for (byte b : data) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    @Override
    protected int getDataLength() {
        return data.length;
    }

    @Override
    public byte[] dataToBytes() {
        return data;
    }

    public static ArgumentRaw fromDataPayload(byte[] dataPayload, boolean msbFirst) {
        // Determine byte order (big-endian or little-endian)
        ByteOrder byteOrder = msbFirst ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
        ByteBuffer buffer = ByteBuffer.wrap(dataPayload).order(byteOrder);

        // Read the length field (2 bytes) based on the byte order
        int length = Short.toUnsignedInt(buffer.getShort());

        // Extract the data based on the length from the payload
        byte[] data = new byte[length];
        buffer.get(data, 0, length);

        return new ArgumentRaw(data, msbFirst);
    }

    public int getTypeInfo() {
        // Returning the type for raw data (depends on how TypeInfo is defined)
        return TypeInfo.TYPE_RAW.getValue();
    }
    
    @Override
    public byte[] toBytes(Optional<Boolean> msbFirst) {
        // Handle the endianness (big-endian or little-endian)
        if (msbFirst == null) {
            throw new IllegalArgumentException("msbFirst cannot be null");
        }

        // Determine the byte order based on the msbFirst flag
        ByteOrder byteOrder = msbFirst.get() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
        ByteBuffer buffer = ByteBuffer.allocate(2 + data.length); // Length (2 bytes) + data
        buffer.order(byteOrder);

        // Add length prefix (2 bytes)
        buffer.putShort((short) getDataLength());

        // Add data
        buffer.put(data);

        // Return the resulting byte array
        return buffer.array();
    }
}

