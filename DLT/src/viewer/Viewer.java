package viewer;

import java.io.BufferedReader;
import java.io.File;           // Import java.io.File
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Viewer {

	private static final int NON_VERBOSE_MODE_DATA_MESSAGE = 0x1;
	private static final int VERBOSE_MODE_DATA_MESSAGE = 0;

	public static void main(String[] args) {
		 // Path to the DLT file
        String filePath = "C:\\Users\\ahmed\\Downloads\\delulu.dlt";
        
        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] buffer = new byte[16]; // Read in 16 bytes for the Base Header initially
            while (fis.read(buffer) != -1) {
                parseBaseHeader(buffer);
            }
        } catch (IOException e) {
            System.err.println("Error reading DLT file: " + e.getMessage());
        }
    }

    private static void parseBaseHeader(byte[] data) {
    	
    	int isExtenderHeader = 0;
    	
        if (data.length < 4) {
            System.out.println("Insufficient data for Base Header");
            return;
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.BIG_ENDIAN);
        
        int header = buffer.getInt();
        byte counter = buffer.get();
        char msgLen = buffer.getChar();
        byte MSIN;
        byte NOAR;
        
        long TMSP1;
        byte TMSP2;
        
        int MSID;
        
        int CNTI = header & 0b11;                   // Bits 0-1
        int WEID = (header >> 2) & 0b1;             // Bit 2
        int WACID = (header >> 3) & 0b1;            // Bit 3
        int WSID = (header >> 4) & 0b1;             // Bit 4
        int VERS = (header >> 5) & 0b111;           // Bits 5-7
        int WSFLN = (header >> 8) & 0b1;            // Bit 8
        int WTGS = (header >> 9) & 0b1;             // Bit 9
        int WPVL = (header >> 10) & 0b1;            // Bit 10
        int WSGM = (header >> 11) & 0b1;            // Bit 11

        isExtenderHeader = WEID | WACID | WSID | WSFLN | WTGS | WPVL | WSGM;
        
        if(CNTI != NON_VERBOSE_MODE_DATA_MESSAGE) {
        	MSIN = buffer.get();
        	// To-Do get info later
        	
        	NOAR = buffer.get();
        	// To-Do get no. of args later

        }
        
        // Time
        if(CNTI == NON_VERBOSE_MODE_DATA_MESSAGE || CNTI == VERBOSE_MODE_DATA_MESSAGE) {
        	TMSP1 = buffer.getLong();
        	TMSP2 = buffer.get();
        }
        
        if(CNTI == NON_VERBOSE_MODE_DATA_MESSAGE) {
        	MSID = buffer.getInt();
        }
        
        
        if(isExtenderHeader != 0) {
        	
        }


    }

 
    

    
}
