package de.fuberlin.wiwiss.d2rq.rdb2rdf;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import static org.junit.Assert.assertEquals;
import de.fuberlin.wiwiss.d2rq.SystemLoader;

@RunWith(Parameterized.class)
public class DirectMappingTest {

	private static final String TS_PATH = "doc/rdb2rdf-ts/";
	private String jdbcURL = "";
	private String sqlFile = "";
	private String dGraphFilePath = "";
	private static final String baseURI = "http://example.com/base/";
	
	public DirectMappingTest(String jdbcURL, String sqlFile, String dGraphFilePath) {
		this.jdbcURL = jdbcURL;
		this.sqlFile = sqlFile;
		this.dGraphFilePath = dGraphFilePath;
	}
	
	@Parameters
	public static Collection getTestLists() {
		File dirPath = new File(TS_PATH);
		File[] dir = dirPath.listFiles();
		String[][] arr = new String[dir.length][3];
		int index=0;
		String absolutePath = "";
		for (File f:dir) {
			if (f.isDirectory()) {
				absolutePath = f.getAbsolutePath();
				arr[index][0] = "jdbc:hsqldb:mem:" + f.getName();
				arr[index][1] = absolutePath + "/create.sql";
				arr[index][2] = absolutePath + "/directGraph.ttl";
				index++;
			}
		}
		return Arrays.asList(arr);
	}
	
	@Test
	public void directMappingValidator() {
		SystemLoader loader = new SystemLoader();
		loader.setMappingFileOrJdbcURL(this.jdbcURL);
		loader.setGenerateW3CDirectMapping(true);
		loader.setStartupSQLScript(this.sqlFile);
		loader.setSystemBaseURI(baseURI);
		Model d2rqModel = loader.getModelD2RQ();
		Model model = null;
		
		try {
			model = getJenaModel(this.dGraphFilePath);
			assertEquals(true,model.isIsomorphicWith(d2rqModel));
		} finally {
			d2rqModel.close();
			model.close();
		}
	}
	
	public Model getJenaModel(String inputFileName) {
		return FileManager.get().readModel(ModelFactory.createDefaultModel(),inputFileName);
	}
}
