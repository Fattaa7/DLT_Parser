package viewer;
import java.util.Optional;

public class NonVerbosePayload extends Payload {
    // Minimum length of the bytes data
    private static final int MESSAGE_ID_LENGTH = 4;

    private final int messageId;
    private final byte[] nonStaticData;
    private final Optional<Boolean> msbFirst;

    public NonVerbosePayload(int messageId, byte[] nonStaticData, Optional<Boolean> msbFirst) {
        this.messageId = messageId;
        this.nonStaticData = nonStaticData;
        this.msbFirst = msbFirst;
    }

    // Factory method to create NonVerbosePayload from bytes
    public static NonVerbosePayload createFromBytes(byte[] data, boolean msbFirst) throws IllegalArgumentException {
        if (data.length < MESSAGE_ID_LENGTH) {
            throw new IllegalArgumentException("Unexpected length of the data: " + data.length +
                    " / Payload of Non-Verbose Mode must not be < " + MESSAGE_ID_LENGTH);
        }

        // Parse the bytes based on endianness
        String endian = msbFirst ? ">" : "<";
        int messageId = (int) java.nio.ByteBuffer.wrap(data, 0, 4).getInt();
        
        byte[] nonStaticData = new byte[data.length - MESSAGE_ID_LENGTH];
        System.arraycopy(data, MESSAGE_ID_LENGTH, nonStaticData, 0, nonStaticData.length);
        
        return new NonVerbosePayload(messageId, nonStaticData, Optional.of(msbFirst));
    }

    // Convert to bytes considering the specified or stored endianness
    public byte[] toBytes(Optional<Boolean> msbFirstOption) throws IllegalArgumentException {
        boolean endianFlag;
        if (msbFirstOption.isPresent()) {
            endianFlag = msbFirstOption.get();
        } else if (msbFirst.isPresent()) {
            endianFlag = msbFirst.get();
        } else {
            throw new IllegalArgumentException("Endian is not known");
        }

        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(MESSAGE_ID_LENGTH + nonStaticData.length);
        buffer.putInt(messageId); // write messageId as 4-byte integer
        buffer.put(nonStaticData); // append non-static data
        return buffer.array();
    }

    // Get length of the data bytes
    public int getBytesLength() {
        return MESSAGE_ID_LENGTH + nonStaticData.length;
    }

    // Implement the _toStr method from Payload abstract class
    @Override
    public String _toStr() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(messageId).append("] ");
        sb.append(bytesToHex(nonStaticData));
        return sb.toString();
    }

    // Helper method to convert byte array to hex string
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
