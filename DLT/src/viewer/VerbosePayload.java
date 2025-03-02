package viewer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VerbosePayload extends Payload {
    private final List<Argument> arguments;

    public VerbosePayload(List<Argument> arguments) {
        this.arguments = arguments;
    }

    // Factory method to create VerbosePayload from data bytes
    public static VerbosePayload createFromBytes(
            byte[] data, 
            boolean msbFirst, 
            int numberOfArguments, 
            Optional<String> encoding) throws IllegalArgumentException {
        
        List<Argument> arguments = new ArrayList<>();
        int offset = 0;

        // Create arguments based on the provided byte data
        for (int i = 0; i < numberOfArguments; i++) {
            Argument arg = Argument.createFromBytes(data, msbFirst, encoding);
            arguments.add(arg);
            offset += arg.getDataPayloadLength(); // Move the offset for the next argument
        }

        return new VerbosePayload(arguments);
    }

    // Convert to data bytes considering the specified or stored endianness
    public byte[] toBytes(Optional<Boolean> msbFirstOption) throws IllegalArgumentException {
        boolean endianFlag;
        if (msbFirstOption.isPresent()) {
            endianFlag = msbFirstOption.get();
        } else {
            throw new IllegalArgumentException("Endian is not known");
        }

        byte[] data = new byte[0];
        for (Argument arg : arguments) {
            byte[] argBytes = arg.toBytes(msbFirstOption);
            data = concatenateByteArrays(data, argBytes);
        }
        return data;
    }

    // Get length of the data bytes
    public int getBytesLength() {
        int length = 0;
        for (Argument arg : arguments) {
            length += arg.getDataPayloadLength();
        }
        return length;
    }

    // Helper method to convert list of arguments to a human-readable string
    @Override
    public String _toStr() {
        StringBuilder sb = new StringBuilder();
        for (Argument arg : arguments) {
            sb.append(arg.toString()).append(" ");
        }
        return sb.toString().trim();
    }

    // Helper method to concatenate byte arrays
    private static byte[] concatenateByteArrays(byte[] first, byte[] second) {
        byte[] result = new byte[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
