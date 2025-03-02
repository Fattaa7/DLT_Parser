package viewer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class StorageHeader {
    // Length of the data in bytes
    public static final int DATA_LENGTH = 16;

    // "DLT" + 0x01 pattern
    public static final byte[] DLT_PATTERN = new byte[] {(byte) 0x44, (byte) 0x4C, (byte) 0x54, (byte) 0x01};

    // Struct format for packing/unpacking, but we use ByteBuffer for simplicity in Java
    public static final String STRUCT_FORMAT = "<Ii4s";

    int seconds;
    int microseconds;
    String ecuId;

    // Constructor to initialize StorageHeader
    public StorageHeader(int seconds, int microseconds, String ecuId) {
        this.seconds = seconds;
        this.microseconds = microseconds;
        this.ecuId = ecuId;
    }

    @Override
    public String toString() {
        return String.format(
            "StorageHeader(seconds=%d, microseconds=%d, ecu_id=\"%s\")",
            this.seconds, this.microseconds, this.ecuId
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof StorageHeader) {
            StorageHeader other = (StorageHeader) obj;
            return this.seconds == other.seconds
                    && this.microseconds == other.microseconds
                    && this.ecuId.equals(other.ecuId);
        }
        return false;
    }

    public static StorageHeader createFromBytes(byte[] data) {
        if (data.length < DATA_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Unexpected length of the data: %d / Storage Header must be %d or more", data.length, DATA_LENGTH)
            );
        }

        byte[] dltPattern = Arrays.copyOfRange(data, 0, 4);
        if (!Arrays.equals(dltPattern, DLT_PATTERN)) {
            throw new IllegalArgumentException(
                String.format("DLT-Pattern is not found in the data: %s / Beginning of Storage Header must be %s", Arrays.toString(dltPattern), Arrays.toString(DLT_PATTERN))
            );
        }

        ByteBuffer buffer = ByteBuffer.wrap(data, 4, data.length - 4);
        int seconds = buffer.getInt();
        int microseconds = buffer.getInt();
        byte[] ecuIdBytes = new byte[4];
        buffer.get(ecuIdBytes);
        String ecuId = _asciiDecode(ecuIdBytes);

        return new StorageHeader(seconds, microseconds, ecuId);
    }

    public byte[] toBytes() {
        byte[] ecuIdBytes = _asciiEncode(this.ecuId);
        ByteBuffer buffer = ByteBuffer.allocate(DATA_LENGTH);
        buffer.put(DLT_PATTERN);
        buffer.putInt(this.seconds);
        buffer.putInt(this.microseconds);
        buffer.put(ecuIdBytes);
        return buffer.array();
    }

    public int getBytesLength() {
        return DATA_LENGTH;
    }

    // Helper method to decode ASCII byte array into string
    private static String _asciiDecode(byte[] ascii) {
        return new String(ascii, StandardCharsets.US_ASCII).replace("\u0000", "");
    }

    // Helper method to encode a string into an ASCII byte array
    private static byte[] _asciiEncode(String ascii) {
        return ascii.getBytes(StandardCharsets.US_ASCII);
    }
}

