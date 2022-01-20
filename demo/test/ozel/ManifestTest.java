package ozel;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;

import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import ozel.Manifest;

public class ManifestTest {

	@Test
	public void testManifestNull() {
		try {
			Manifest manifest = new Manifest();
			assertFalse("Null not caught", manifest == null );
		} catch (NullPointerException e) {
			assertTrue("NullManifestCaught", true );
		} catch (Exception e) {
			assertFalse("Null not caught", true );			
		}
	}

	@Test
	public void testManifestNotFound() {
		try {
			Manifest manifest = new Manifest();
			manifest.read("manifest_file.non-existing", null);
			assertFalse("non-existing not caught", manifest == null );
		} catch (FileNotFoundException e) {
			assertTrue("NullManifestCaught", true );
		} catch (Exception e) {
			assertFalse("Unknown exception", true );			
		}
	}

	@Test
	public void testManifestSchemaNotValid() {
		try {
			Manifest manifest = new Manifest();
			manifest.read("bad_schema.xml", null);
			assertTrue("non-existing not caught", manifest == null );
		} catch (SAXParseException e) {
			assertTrue("InvalidSchemaCaught", true );
		} catch (Exception e) {
			assertFalse("Unknown exception", true );			
		}
	}
	
	@Test
	public void testManifestNotValid() {
		try {
			Manifest manifest = new Manifest();
			manifest.read("bad.xml", null);
			assertTrue("non-existing not caught", manifest == null );
		} catch (SAXException e) {
			assertTrue("InvalidXMLCaught", true );
		} catch (Exception e) {
			assertFalse("Unknown exception", true );			
		}
	}

	@Test
	public void testManifestValid() {
		try {
			Manifest manifest = new Manifest();
			manifest.read("ozel_manifest.xml", null);
			assertTrue("good parse", manifest != null );
		} catch (Exception e) {
			assertFalse("Unknown exception", true );			
		}
	}

}
