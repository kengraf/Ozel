package ozel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.onionnetworks.fec.FECCode;
import com.onionnetworks.fec.FECCodeFactory;

import fec_util.Buffer;

public class DataObject {
	FECCode fec;					// Factory that does the matrix magic
	byte[] source;			// The original data
	Buffer[] sourceBuffer;	// The padded and keyed version of the input data
	Buffer[] fragments;		// The 'n' pieces of the file we would distribute
	Integer minimumSetSize = 6;	// Required number of fragments needed to reconstruct
	Integer totalFragments = 8;	// Total number of fragments
	int fragmentSize = 3000;	// All fragments are the same size
	int	paddedLength;		// Source length after AES padding
	int keybyteSize = 16;	// In bytes for AES either 128, 192, 256 bits
	String inputURL;		// TODO: part of fragment?
	String hashVal;			// MD5 of original data

	// decode needs
	byte[] received;
	Buffer[] receiverBuffer;
	int[] receiverIndex;
	byte[] encrypted;
	byte[] repair;
	int goodFragments;

	/**
	 * Constructor builds manifest from "ozel.xml"
	 * @param args
	 */
	public DataObject( ) {
		goodFragments = 0;
		//this will store the received packets to be decoded
		received = new byte[minimumSetSize *fragmentSize];

		// Only need to retrieve minimumSetSize fragments, checkSet
		// may offer more.
		receiverBuffer = new Buffer[minimumSetSize];
		receiverIndex = new int[minimumSetSize];

		encrypted = new byte[minimumSetSize*fragmentSize];
		//this will hold the encoded data
		repair = new byte[totalFragments*fragmentSize]; 


		// Create Buffer array to hold original data
		sourceBuffer = new Buffer[minimumSetSize];
		for (int i = 0; i < sourceBuffer.length; i++)
			sourceBuffer[i] = new Buffer(encrypted, i*fragmentSize, fragmentSize);

		// Create our FEC code factory
		fec = FECCodeFactory.getDefault().createFECCode(minimumSetSize,totalFragments);


	}
	
	/**
	 * Constructor builds manifest from "ozel.xml"
	 * @param args
	 */
	public DataObject(String inputURL ) {
		try {
			source = getBytesFromFile(new File(inputURL) );
			MessageDigest md = MessageDigest.getInstance("MD5");
			hashVal = asHex(md.digest(source));

	} catch (Exception e) {
		// TODO proper error handling
		e.printStackTrace();
	}
		
	}

	/**
	 * Constructor builds manifest from "ozel.xml"
	 * @param args
	 */
	public DataObject(Manifest manifest ) {
		try {
			source = getBytesFromFile(new File(inputURL) );
	} catch (Exception e) {
		// TODO proper error handling
		e.printStackTrace();
	}
		
	}

	/**
	 * Returns the contents of the file in a byte array.
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		// Get the size of the file
		long length = file.length();

		// You cannot create an array using a long type.
		// It needs to be an int type.
		// Before converting to an int type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) {
			// File is too large
			throw new IOException("Could not completely read file "+file.getName());
		}

		// Create the byte array to hold the data
		// ASSUMED to fit in memory;  FIX streams/large files 
		// Actually assumed to be < k * packetsize in length
		byte[] bytes = new byte[(int)length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < length
				&& (numRead=is.read(bytes, offset, (int)(length)-offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < (int)length) {
			throw new IOException("Could not completely read file "+file.getName());
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
	}

	/**
	 * Initialize an encryption cipher based on provided key specification
	 * 
	 * @param skeySpec
	 * @return
	 * @throws Exception
	 */
	public Cipher initEncrypt(SecretKeySpec skeySpec) throws Exception {
		// Instantiate the cipher
		Cipher cipher = Cipher.getInstance(skeySpec.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		return cipher;
	}

	/**
	 * Generate an secret key, user never specifies or needs to know this key.
	 * Shred the key with the encrypted data and reconstructed when the data
	 * is reconstructed.
	 * 
	 * FIX: Currently forcing AES
	 * 
	 * @return
	 * @throws Exception
	 */
	public SecretKeySpec generateKey(int keySize) throws Exception {
		// Get the KeyGenerator

		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(keySize); // FIX: 192 and 256 bits may not be available

		// Generate the secret key specs.
		SecretKey skey = kgen.generateKey();
		byte[] raw = skey.getEncoded();

		return new SecretKeySpec(raw, "AES");
	}

	/**
	 * The source data is placed into minimumSetSize fragments.
	 * The fragments are equal size(padded) and include key material.
	 * Only small files are allow so far.
	 * The length of the original data must be retained to strip padding
	 * 
	 * @param source
	 */
	public void encode() throws Exception {

		SecretKeySpec skeySpec = generateKey(keybyteSize*8); // The encryption key

		// Encrypt the source
		Cipher cipher = initEncrypt(skeySpec);
		byte[] temp = cipher.doFinal(source);
		byte[] encrypted = new byte[minimumSetSize*fragmentSize];

		// Determine how much space the keys need must be multiple of minimumSetSize
		int keyspace = ((keybyteSize%minimumSetSize)==0) ? 0 : minimumSetSize - (keybyteSize%minimumSetSize);
		keyspace = (keyspace+keybyteSize)/minimumSetSize;
		byte[] encodedKey = skeySpec.getEncoded();
		System.out.println( "encode key: " + util.Data.asHex(encodedKey) );

		// FIX: these assumes everything fits into minimumSetSize * fragmentSize allocation
		int keyleft = keybyteSize;
		int dataleft = temp.length;
		int fragspace = fragmentSize - keyspace;
		for (int i=0; (i<minimumSetSize) && (keyleft>0); ++i ) {
			System.arraycopy(encodedKey, i*keyspace, encrypted, i*fragmentSize,
					(keyspace<keyleft)?keyspace:keyleft);
			keyleft -= keyspace;
		}

		for (int i=0; (i<minimumSetSize) && (dataleft>0); ++i ) {
			System.arraycopy(temp, i*fragspace, encrypted, (i*fragmentSize)+keyspace,
					(dataleft<fragspace)?dataleft:fragspace );
			dataleft -= fragspace;
		}
		System.out.println("encode data: " + util.Data.asHex(temp) );
		paddedLength = temp.length;

		//this will hold the encoded data
		byte[] repair = new byte[totalFragments*fragmentSize]; 

		// Create Buffer array to hold original data
		sourceBuffer = new Buffer[minimumSetSize];
		for (int i = 0; i < sourceBuffer.length; i++)
			sourceBuffer[i] = new Buffer(encrypted, i*fragmentSize, fragmentSize);

		// Create Buffer array to hold all the encoded fragments
		fragments = new Buffer[this.totalFragments];
		for (int i = 0; i < fragments.length; i++) {
			fragments[i] = new Buffer(repair, i*fragmentSize, fragmentSize);
		}

		// Create repairIndex used by the FECCodeFactory
		int[] repairIndex = new int[totalFragments];
		for (int i = 0; i < repairIndex.length; i++)
			repairIndex[i] = i;

		// Create our FEC code factory
		fec = FECCodeFactory.getDefault().createFECCode(minimumSetSize,totalFragments);

		// Encode the data
		fec.encode(sourceBuffer, fragments, repairIndex);

		// Encoded data is now contained in fragments array
		for (int i = 0; i < fragments.length; i++) {
			System.out.println( "fragments: " + util.Data.asHex(fragments[i].getBytes()) );
		}
	}

	/**
	 * Persists all fragments to disk.  This example uses the in memory data
	 * The written files are only for debug/analysis and are not read
	 */
	public void writeToDisk(String inputURL, String hashVal) {
        try{
		Manifest manifest = new Manifest();
		manifest.create();
		manifest.setName(inputURL);
		manifest.setHash(hashVal);
		manifest.setPadded(paddedLength);

		// Write every fragment to filename_MofN
        for (Integer i = 0; i < totalFragments; i++) {
        	
            	MessageDigest md = MessageDigest.getInstance("MD5");
            	byte[] thedigest = md.digest(fragments[i].getBytes());
            	String fileName = asHex(thedigest);
//            	byte[] decoded = javax.xml.bind.DatatypeConverter.parseBase64Binary(encoded);

            	FileOutputStream fos = new FileOutputStream(fileName);
                fos.write( fragments[i].getBytes() );
                fos.close();
//                logger.info("Persisted: " + fileName );
                manifest.addFragment( fileName, i );
        } 
        manifest.write( "ozel.manifest" );
        } catch(IOException e) {
            System.out.println("Could not open the file." + e);
            System.exit(0);
        } catch(Exception e) {
        }
		return;
	}

	/**
	 * Persists all fragments to disk.  This example uses the in memory data
	 * The written files are only for debug/analysis and are not read
	 *
	public void readFromDisk(String inputURL) {
        try{
		Manifest manifest = new Manifest();
		manifest.read(inputURL);
		String fileName = manifest.getName();
		String hashVal = manifest.getHash();

		// Write every fragment to filename_MofN
        for (Integer i = 0; i < totalFragments; i++) {
        	
            	MessageDigest md = MessageDigest.getInstance("MD5");
            	byte[] thedigest = md.digest(fragments[i].getBytes());
            	String fileName = asHex(thedigest);
//            	byte[] decoded = javax.xml.bind.DatatypeConverter.parseBase64Binary(encoded);

            	FileOutputStream fos = new FileOutputStream(fileName);
                fos.write( fragments[i].getBytes() );
                fos.close();
//                logger.info("Persisted: " + fileName );
                manifest.addFragment( fileName, i );
        } 
        manifest.write( "ozel.manifest" );
        } catch(IOException e) {
            System.out.println("Could not open the file." + e);
            System.exit(0);
        } catch(Exception e) {
        }
		return;
	}

*/

	/**
	 * Initialize an decryption cipher based on provided key specification
	 * 
	 * @param skeySpec
	 * @return
	 * @throws Exception
	 */
	public Cipher initDecrypt(SecretKeySpec skeySpec) throws Exception {
		// Instantiate the cipher
		Cipher cipher = Cipher.getInstance(skeySpec.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		return cipher;
	}
	
	public void retrieveFragment( Integer offset, String fragName ) {
    	try {
    		byte[] packet = new byte[fragmentSize]; 
    		if( goodFragments == minimumSetSize ) return;

    		FileInputStream fis = new FileInputStream(fragName);
    		fis.read( packet, 0, fragmentSize );
    		fis.close();

    		// validate
        	MessageDigest md = MessageDigest.getInstance("MD5");
        	byte[] thedigest = md.digest(packet) ; // fragments[offset].getBytes());
        	String md5 = asHex(thedigest);
        	if( md5.equals(fragName) ) {
    			System.arraycopy(packet, 0, received, goodFragments*fragmentSize, packet.length);
    			receiverIndex[goodFragments] = offset;
        		goodFragments++;
        	}
    		
    	} catch(Exception e) {
    		// ignore file errors
    		// TODO proper error handling
    	}
		return;
	}

	/**
	 * Reconstruct data from the fragments allowed in checkSet
	 * 
	 * @param checkSet
	 * @return
	 */
	public boolean decode(Manifest manifest) throws Exception {
/* TODO debug
 *  int[] checkSet
 *
		// We will simulate dropping all packets not in the checkSet
		int j = 0; 
		for (int i = 0; i < fragments.length; i++) {
			// TODO: need to check fragment available&correct
			byte[] packet = fragments[i].getBytes();
			System.arraycopy(packet, 0, received, j*fragmentSize, packet.length);
			receiverIndex[j] = i;
			j++;

			// Got the minimum we need so move on
			if (j==minimumSetSize) break;
		}
		

		// ASSERT we have the minimum
		if (j<minimumSetSize) return false;
*/

		//create our Buffers for the encoded data
		for (int i = 0; i < minimumSetSize; i++) {
			receiverBuffer[i] = new Buffer(received, i*fragmentSize, fragmentSize);
		}

		//finally we can decode
		fec.decode(receiverBuffer, receiverIndex);

		// Array manipulation to undo key insertion, padding and encryption
		// Key material is evenly distributed over minimumSetSize pieces
		int keyspace = ((keybyteSize%minimumSetSize)==0) ? 0 : minimumSetSize - (keybyteSize%minimumSetSize);
		keyspace = (keyspace+keybyteSize)/minimumSetSize;
		int keyleft = keybyteSize;
		int dataleft = paddedLength;
		byte key[] = new byte[keyleft];
		byte[] unpadded = new byte[paddedLength];
		int fragspace = fragmentSize - keyspace;
		for (int i=0; (i<minimumSetSize) && (keyleft>0); ++i ) {
			System.arraycopy(received, i*fragmentSize, key, i*keyspace,
					(keyspace<keyleft)?keyspace:keyleft);
			keyleft -= keyspace;
		}
		
		// We now have the reconstructed secret key
		Ozel.getLogger().finer("decode key: " + util.Data.asHex(key) );
		SecretKeySpec skeySpec = new SecretKeySpec(key, 0, keybyteSize, "AES" );

		for (int i=0; (i<minimumSetSize) && (dataleft>0); ++i ) {
			System.arraycopy(received, keyspace+(i*fragmentSize), unpadded, (i*fragspace),
					(dataleft<fragspace)?dataleft:fragspace );
			dataleft -= fragspace;
		}
		
		// We now have the reconstructed and encrypted data
		Ozel.getLogger().finer("decode data: " + util.Data.asHex(unpadded) );

		// Decrypt the data
		Cipher cipher = initDecrypt(skeySpec);
		byte[] decrypted = cipher.doFinal(unpadded);
		String originalString = new String(decrypted);
		Ozel.getLogger().finer(" Decrypted string: " + originalString );

    	FileOutputStream fos = new FileOutputStream(new File(manifest.dataName).getName());
        fos.write( decrypted );
        fos.close();

        // Return true if the result matches the original source
		return Arrays.equals(source, decrypted);
	}

	/**
	 * Turns array of bytes into string
	 *
	 * @param buf	Array of bytes to convert to hex string
	 * @return	Generated hex string
	 */
	public static String asHex (byte buf[]) {
		StringBuffer strbuf = new StringBuffer(buf.length * 2);
		int i;

		for (i = 0; i < buf.length; i++) {
			if (((int) buf[i] & 0xff) < 0x10)
				strbuf.append("0");

			strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
		}

		return strbuf.toString();
	}


}
