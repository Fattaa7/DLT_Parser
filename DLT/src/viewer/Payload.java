package viewer;

import java.util.Objects;
import java.util.Optional;

public abstract class Payload {

    // Default value for msb_first (endian type)
    private static final Optional<Boolean> msbFirstDefault = Optional.of(false);

    @Override
    public String toString() {
        return _toStr();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof Payload) {
            return Objects.equals(this.toBytes(Optional.of(false)), ((Payload) other).toBytes(Optional.of(false)));
        }
        return false;
    }

    /**
     * Convert to data bytes.
     *
     * @param msbFirst True - Handle payload data as big endian
     *                 False - Handle payload data as little endian
     *                 None - Handle payload data according to field value of endian
     * @return Converted data bytes
     * @throws IllegalArgumentException if msbFirst is null and no default endian type is available
     */
    public abstract byte[] toBytes(Optional<Boolean> msbFirst) throws IllegalArgumentException;

    /**
     * Get length of the data bytes.
     *
     * @return Length of the data bytes
     */
    public abstract int getBytesLength();

    /**
     * Convert payload to human readable string.
     *
     * @return Human readable string
     */
    protected abstract String _toStr();
}
