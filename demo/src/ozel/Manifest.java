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
 * Manage retrieval and persistence of manifests
 *  
 **/

package ozel;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fec_util.Buffer;

public class Manifest {
	private Document doc;
	private Element root;
	private DocumentBuilderFactory docFactory;
	private DocumentBuilder docBuilder;
	public String dataName;
	public String dataHash;

	public Manifest() throws Exception {
		try
		{
			docFactory = DocumentBuilderFactory.newInstance();
			docBuilder = docFactory.newDocumentBuilder();

		} catch(Exception e) {
			throw( e );
		}
		return;
	}

	public void create() throws Exception {
		try
		{
			//root elements
			doc = docBuilder.newDocument();
			root = doc.createElement("manifest");
			doc.appendChild(root);
		} catch(Exception e) {
			throw( e );
		}
		return;
	}

	public void read(String url, DataObject data ) { 
		try {
			File fXmlFile = new File(url);
			Document doc = docBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

// TODO debug 			System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

			NodeList nList = doc.getElementsByTagName("manifest");

			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;
					dataName = getTagValue("Name", eElement);
					dataHash = getTagValue("Hash", eElement);
					data.paddedLength = new Integer(getTagValue("PaddedLength",eElement));
// TODO					System.out.println("name : " + getTagValue("Name", eElement));
// TODO					System.out.println("hash : " + getTagValue("Hash", eElement));

					for( Integer i =0; i<data.totalFragments; ++i ) {
// TODO					System.out.println("frag : " + getTagValue("fragment"+i.toString(), eElement));
						data.retrieveFragment(i, getTagValue("fragment"+i.toString(), eElement));
					}
				}
			}

			} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String getTagValue(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();

		Node nValue = (Node) nlList.item(0);

		return nValue.getNodeValue();
	}


	public void addFragment( String fragName, Integer fragCount ) {
		try {
			Element frag = doc.createElement("fragment" + fragCount.toString());
			frag.appendChild(doc.createTextNode(fragName));
			root.appendChild(frag);
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public void setPadded(int len) {
		try {
			Integer pl = new Integer(len);
			Element elem = doc.createElement("PaddedLength");
			elem.appendChild(doc.createTextNode(pl.toString()));
			root.appendChild(elem);
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public void setName( String name ) {
		try {
			Element elem = doc.createElement("Name");
			elem.appendChild(doc.createTextNode(name));
			root.appendChild(elem);
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public void setHash( String hashVal ) {
		try {
			Element elem = doc.createElement("Hash");
			elem.appendChild(doc.createTextNode(hashVal));
			root.appendChild(elem);
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public void write(String manifestURL ) {
		try {
			//write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);

			StreamResult result =  new StreamResult(new File(manifestURL));
			transformer.transform(source, result);

		}catch(TransformerException tfe){
			tfe.printStackTrace();
		}
	}
}
