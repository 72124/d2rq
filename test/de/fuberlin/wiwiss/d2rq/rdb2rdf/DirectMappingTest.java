package de.fuberlin.wiwiss.d2rq.rdb2rdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.util.FileManager;
import static org.junit.Assert.assertEquals;
import de.fuberlin.wiwiss.d2rq.SystemLoader;
import de.fuberlin.wiwiss.d2rq.parser.MapParser;

@RunWith(Parameterized.class)
public class DirectMappingTest {

	static final String TS_PATH = "doc/rdb2rdf-ts/";
	Model model1;
	Model model2;
	
	public DirectMappingTest(String file1, String file2) {
		model1 = getJenaModel(file1);
		model2 = getJenaModel(file2);
	}
	
	@Parameters
	public static Collection data() {
		File dirPath = new File(TS_PATH);
		File[] dir = dirPath.listFiles();
		String[][] arr = new String[dir.length][2];
		int index=0;
		String format = "TURTLE";
		PrintStream out = null;
		
		for (File f:dir) {
			if (f.isDirectory()) {
				SystemLoader loader = new SystemLoader();
				loader.setMappingFileOrJdbcURL("jdbc:hsqldb:mem:" + f.getName());
				loader.setGenerateW3CDirectMapping(true);
				loader.setStartupSQLScript(f.getAbsolutePath() + "/create.sql");
				loader.setSystemBaseURI("http://example.com/base/");
				Model d2rqModel = loader.getModelD2RQ();
				
				try {
					File file = new File(f.getAbsolutePath() + "/directGraph-d2rq.ttl");
					out = new PrintStream(new FileOutputStream(file));
					loader.setSystemBaseURI(MapParser.absolutizeURI(f.toURI().toString() + "#"));
					RDFWriter writer = d2rqModel.getWriter(format.toUpperCase());
					if (format.equals("RDF/XML") || format.equals("RDF/XML-ABBREV")) {
						writer.setProperty("showXmlDeclaration", "true");
						if (loader.getResourceBaseURI() != null) {
							writer.setProperty("xmlbase", loader.getResourceBaseURI());
						}
					}
					writer.write(d2rqModel, new OutputStreamWriter(out, "utf-8"), loader.getResourceBaseURI());
				} catch (UnsupportedEncodingException ex) {
					throw new RuntimeException("Can't happen -- utf-8 is always supported");
				} catch (FileNotFoundException ex) {
					throw new RuntimeException("Error creating file.");
				}
				finally {
					out.close();
				}
				
				arr[index][0] = f.getAbsolutePath() + "/directGraph.ttl";
				arr[index][1] = f.getAbsolutePath() + "/directGraph-d2rq.ttl";
				index++;
			}
		}
		return Arrays.asList(arr);
	}
	
	@Test
	public void directMappingValidator() {
		assertEquals(true,model1.isIsomorphicWith(model2));
	}
	
	public Model getJenaModel(String inputFileName) {
		Model model = FileManager.get().readModel(ModelFactory.createDefaultModel(),inputFileName);
		return model;
	}
}
