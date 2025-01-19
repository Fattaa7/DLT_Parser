package viewer;
public enum TypeInfo {
    // Type Length
    TYPE_LENGTH_8BIT(0b00000000000000000000000000000001),
    TYPE_LENGTH_16BIT(0b00000000000000000000000000000010),
    TYPE_LENGTH_32BIT(0b00000000000000000000000000000011),
    TYPE_LENGTH_64BIT(0b00000000000000000000000000000100),
    TYPE_LENGTH_128BIT(0b00000000000000000000000000000101),
    
    // Type Bool
    TYPE_BOOL(0b00000000000000000000000000010000),
    
    // Type Signed
    TYPE_SIGNED(0b00000000000000000000000000100000),
    
    // Type Unsigned
    TYPE_UNSIGNED(0b00000000000000000000000001000000),
    
    // Type Float
    TYPE_FLOAT(0b00000000000000000000000010000000),
    
    // Type Array
    TYPE_ARRAY(0b00000000000000000000000100000000),
    
    // Type String
    TYPE_STRING(0b00000000000000000000001000000000),
    
    // Type Raw
    TYPE_RAW(0b00000000000000000000010000000000),
    
    // Variable Info
    VARIABLE_INFO(0b00000000000000000000100000000000),
    
    // Fixed Point
    FIXED_POINT(0b00000000000000000001000000000000),
    
    // Trace Info
    TRACE_INFO(0b00000000000000000010000000000000),
    
    // Type Struct
    TYPE_STRUCT(0b00000000000000000100000000000000),
    
    // String Coding
    STRING_CODING_ASCII(0b00000000000000000000000000000000),
    STRING_CODING_UTF8(0b00000000000000001000000000000000);

    private final int value;

    TypeInfo(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TypeInfo fromValue(int value) {
        for (TypeInfo typeInfo : TypeInfo.values()) {
            if (typeInfo.getValue() == value) {
                return typeInfo;
            }
        }
        throw new IllegalArgumentException("Unknown TypeInfo value: " + value);
    }
}

