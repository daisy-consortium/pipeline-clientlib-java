package org.daisy.pipeline.client.http;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;
import javax.inject.Inject;

import org.daisy.pipeline.client.models.Alive;
import org.daisy.pipeline.client.models.Argument;
import org.daisy.pipeline.client.models.datatypes.EnumType;
import org.daisy.pipeline.client.models.Script;
import org.daisy.pipeline.client.utils.XML;

import static org.daisy.pipeline.pax.exam.Options.domTraversalPackage;
import static org.daisy.pipeline.pax.exam.Options.felixDeclarativeServices;
import static org.daisy.pipeline.pax.exam.Options.logbackClassic;
import static org.daisy.pipeline.pax.exam.Options.logbackConfigFile;
import static org.daisy.pipeline.pax.exam.Options.mavenBundle;
import static org.daisy.pipeline.pax.exam.Options.mavenBundlesWithDependencies;
import static org.daisy.pipeline.pax.exam.Options.thisBundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.ops4j.pax.exam.Configuration;
import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.PathUtils;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class WSTest {
	
	private static WSInterface ws = new WS();
	
	// use this to test the webservice manually in the browser (http://localhost:8181/ws/...)
	//@Test
	public void keepWsAlive() throws InterruptedException {
		Thread.sleep(60000);
	}
	
	@Test
	public void testAlive() {
		Alive alive = ws.alive();
		assertFalse(alive.error);
		assertFalse(alive.authentication);
		assertEquals("1.10", alive.version);
	}
	
	@Test
	public void testScripts() {
		List<Script> scripts = ws.getScripts();
		assertEquals(1, scripts.size());
		Script script = ws.getScript(scripts.get(0).getId());
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
		             "<script xmlns=\"http://www.daisy.org/ns/pipeline/data\" " +
		                     "href=\"http://localhost:8181/ws/scripts/foo:script\" " +
		                     "id=\"foo:script\" " +
		                     "input-filesets=\"daisy202 daisy3\" " +
		                     "output-filesets=\"epub2 epub3\">\n" +
		             "<nicename>Example script</nicename>\n" +
		             "<description>Transforms a Something into a Something.</description>\n" +
		             "<version>0.0.0-SNAPSHOT</version>\n" +
		             "<homepage>http://github.com/daisy</homepage>\n" +
		             "<input desc=\"Input port description.\" " +
		                    "mediaType=\"application/x-dtbook+xml\" " +
		                    "name=\"source\" " +
		                    "nicename=\"Input port\" " +
		                    "ordered=\"true\" " +
		                    "required=\"true\" " +
		                    "sequence=\"false\" " +
		                    "type=\"anyFileURI\"/>\n" +
		             "<option data-type=\"foo:choice\" " +
		                     "desc=\"Enum description.\" " +
		                     "mediaType=\"\" " +
		                     "name=\"option-1\" " +
		                     "nicename=\"Enum\" " +
		                     "ordered=\"true\" " +
		                     "required=\"true\" " +
		                     "sequence=\"false\" " +
		                     "type=\"string\"/>\n" +
		             "<option data-type=\"foo:regex\" " +
		                     "desc=\"Regex description.\" " +
		                     "mediaType=\"\" " +
		                     "name=\"option-2\" " +
		                     "nicename=\"Regex\" " +
		                     "ordered=\"true\" " +
		                     "required=\"false\" " +
		                     "sequence=\"false\" " +
		                     "type=\"string\"/>\n" +
		             "<option desc=\"Input HTML.\" " +
		                     "mediaType=\"application/xhtml+xml " +
		                     "text/html\" " +
		                     "name=\"href\" " +
		                     "nicename=\"HTML\" " +
		                     "ordered=\"true\" " +
		                     "required=\"true\" " +
		                     "sequence=\"false\" " +
		                     "type=\"anyFileURI\"/>\n" +
		             "<option desc=\"Whether or not to include or not include something that you may (or may not) want to include.\" " +
		                     "mediaType=\"\" " +
		                     "name=\"yes-or-no\" " +
		                     "nicename=\"Yes? No?\" " +
		                     "ordered=\"true\" " +
		                     "required=\"false\" " +
		                     "sequence=\"false\" " +
		                     "type=\"boolean\"/>\n" +
		             "</script>\n",
		             XML.toString(script.toXml()));
		assertEquals("Example script", script.getNicename());
		assertEquals(5, script.getInputs().size());
		Argument option1 = script.getArgument("option-1");
		assertTrue(option1.getRequired());
		assertEquals("foo:choice", option1.getDataType());
		EnumType choice = (EnumType)ws.getDataType("foo:choice");
		assertEquals(3, choice.values.size());
		assertEquals("one", choice.values.get(0).name);
		assertEquals("two", choice.values.get(1).name);
		assertEquals("three", choice.values.get(2).name);
		Argument option2 = script.getArgument("option-2");
		assertFalse(option2.getRequired());
		assertEquals("one", option2.getDefaultValue());
		assertEquals("foo:regex", option2.getDataType().toString());
	}
	
	@Configuration
	public Option[] config() throws MalformedURLException {
		return options(
			systemProperty("org.daisy.pipeline.ws.authentication").value("false"),
			systemProperty("org.daisy.pipeline.version").value("1.10"),
			domTraversalPackage(),
			logbackConfigFile(),
			felixDeclarativeServices(),
			junitBundles(),
			mavenBundlesWithDependencies(
				logbackClassic(),
				// pipeline webservice
				mavenBundle("org.daisy.pipeline:webservice:?"),
				mavenBundle("org.daisy.pipeline:calabash-adapter:?"), // org.daisy.common.xproc.XProcEngine
				mavenBundle("org.daisy.pipeline:modules-registry:?"), // javax.xml.transform.URIResolver
				mavenBundle("org.daisy.pipeline:framework-volatile:?"), // org.daisy.pipeline.job.JobStorage
				mavenBundle("org.daisy.pipeline:woodstox-osgi-adapter:?"), // javax.xml.stream.XMLInputFactory
				mavenBundle("org.daisy.pipeline:framework-core:?"), // org.daisy.pipeline.datatypes.DatatypeRegistry
				// FIXME: these belong in the framework as runtime dependencies
				mavenBundle("commons-io:commons-io:?"),
				mavenBundle("org.daisy.libs:jing:?")
				),
			// example script (incl. datatypes)
			bundle("reference:" + new File(PathUtils.getBaseDir() + "/target/test-classes/example_script/").toURL().toString()),
			// the client must technically not be run in OSGi, however there
			// is no other way to keep the webservice running while the test
			// is executed
			wrappedBundle(
				bundle(new File(PathUtils.getBaseDir() + "/target/clientlib-java-httpclient-2.0.1-SNAPSHOT.jar").toURL().toString()))
				.bundleSymbolicName("org.daisy.pipeline.clientlib-java-httpclient")
				.bundleVersion("2.0.1.SNAPSHOT"),
			wrappedBundle(
				mavenBundle("org.daisy.pipeline:clientlib-java:?"))
				.bundleSymbolicName("org.daisy.pipeline.clientlib-java")
				.bundleVersion("4.7.1.SNAPSHOT"),
			mavenBundle("org.apache.httpcomponents:httpcore-osgi:?"),
			mavenBundle("org.apache.httpcomponents:httpclient-osgi:?")
		);
	}
}
