/*
 * Copyright (C) 2012 Ken Graf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Sample code to demonstrate FEC can properly fragment and reconstruct data based on m of n
 * 
 * @author Ken Graf
 *
 **/

/* Math driven by open source OnionNetworks code */

package ozel;


import java.util.logging.*;


public class Ozel {
	private static Logger logger;	// Show class events

	/**
	 * Constructor builds manifest from "ozel.xml"
	 * @param args
	 */
	public Ozel() {
		new Ozel( null );
	}
	
	/**
	 * Constructor opens a manifest from URI argument
	 * @param args
	 */
	public Ozel(String manifestURI ) {
		try {
			logger = Logger.getLogger("Ozel");
			// Turn on all logging
			logger.setLevel(Level.SEVERE);
			
			// Create manifest and read if URI is valid
			Manifest manifest = new Manifest();
			if (manifestURI != null) {
				manifest.read(manifestURI, null);
			}

/*			// Read the file passed as an argument
			// Validate all possible fragment sets
			int[] checkSet = new int[totalFragments];
			iterateOverSets(totalFragments, checkSet );
*/
		} catch (Exception e) {
			// TODO proper error handling
			e.printStackTrace();
			return;
		}
	}

	public void encode( String inputURL ) {
	try {
		DataObject data = new DataObject( inputURL );

		// Encode the bytes
		data.encode();

		// Persist the bytes
		data.writeToDisk(inputURL, data.hashVal);
	} catch (Exception e) {
		// TODO proper error handling
		e.printStackTrace();
		return;
	}

}
	
	public void decode( String inputURL ) {
	try {
		Manifest manifest = new Manifest();
		DataObject data = new DataObject();
		
		// Load manifest data into DataObject
		manifest.read( inputURL, data );

		// Decode the bytes
		data.decode(manifest);

	} catch (Exception e) {
		// TODO proper error handling
		e.printStackTrace();
		return;
	}

}
	
	public static Logger getLogger() {
		return logger;
	}

	public static void setLogger(Logger logger) {
		Ozel.logger = logger;
	}

	/**
	 * Allow command line invocation.
	 * Only argument is the manifest URI
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		if (args[0].equals("-e") )
			new Ozel().encode(args[1]);
		else if (args[0].equals("-d") )
			new Ozel().decode(args[1]);
		else System.out.println( "usage: [-e <file_to_shred>] [-d <manifest_to_recover>]" );
	}

}//end class
