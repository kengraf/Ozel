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
 * Assemble: Gather fragments from providers
 *  
 **/

package ozel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Assemble {
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
	 * checkSet determine which fragments we will reconstruct from.
	 * This is just validate that we can truly pick any m of n
	 * 
	 * @param checkSet
	 * @throws Exception
	 */
 	void validateSet( int[] checkSet ) throws Exception {
 
		int fragments = 0;
		Ozel.getLogger().finest("checkSet:");
		for (int i=0; i<checkSet.length; ++i) {
			Ozel.getLogger().finest( " " + checkSet[i] );
			fragments += checkSet[i];
		}

		// Try to decode based on the checkSet available
// TODO: fix		boolean decoded = decode(checkSet);
		boolean decoded = true;
		int minimumSetSize = 10;
		
		// Validate for expected results
		if (fragments<minimumSetSize) {
			Ozel.getLogger().finest( ": " + ((decoded)?"***FAILURE***":"Missing"));			
		} else {
			Ozel.getLogger().finest( ": " + ((!decoded)?"***FAILURE***":"OK"));			
		}

	}

	/**
	 * Run thru all the possible set combinations
	 * @param fragmentsToIterate
	 * @param checkSet
	 */
	void iterateOverSets( int fragmentsToIterate, int[] checkSet ) throws Exception {
		if (fragmentsToIterate==0) {
			// Flip the last bit, check and end recursion
			checkSet[fragmentsToIterate] = 1;
			validateSet( checkSet );
			checkSet[fragmentsToIterate] = 0;
			validateSet( checkSet );
			return;
		}

		if (--fragmentsToIterate>0) {
			// If not last, include fragment and iterate
			checkSet[fragmentsToIterate] = 1;
			iterateOverSets(fragmentsToIterate, checkSet);
		}

		// Exclude fragment and iterate
		checkSet[fragmentsToIterate] = 0;
		iterateOverSets(fragmentsToIterate, checkSet);
	}


}
