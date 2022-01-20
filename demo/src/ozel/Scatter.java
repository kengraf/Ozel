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
 * Scatter: distribute fragments to providers
 *  
 **/

package ozel;

import java.io.FileOutputStream;
import java.io.IOException;

public class Scatter {
	/**
	 * Persists all fragments to disk.  This example uses the in memory data
	 * The written files are only for debug/analysis and are not read
	 */
	private void writeToDisk( DataObject data ) {

		// Write every fragment to filename_MofN
        for (int i = 0; i < data.totalFragments; i++) {
        	String fileName = data.inputURL + "_" + i + "of" + data.totalFragments;
        	
            try{
                FileOutputStream fos = new FileOutputStream(fileName);
                fos.write( data.fragments[i].getBytes() );
                fos.close();
                Ozel.getLogger().finest("Persisted: " + fileName );
            } catch(IOException e) {
                System.out.println("Could not open the file." + e);
                System.exit(0);
            }
        } 
		return;
	}

}
