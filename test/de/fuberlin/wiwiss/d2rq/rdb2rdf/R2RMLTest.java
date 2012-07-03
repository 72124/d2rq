package de.fuberlin.wiwiss.d2rq.rdb2rdf;

import static org.junit.Assert.assertEquals;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import de.fuberlin.wiwiss.d2rq.SystemLoader;

@RunWith(Parameterized.class)
public class R2RMLTest {

	private static final String TS_PATH = "doc/rdb2rdf-ts/";
	private String jdbcURL = "";
	private String sqlFile = "";
	private String r2rmlFilePath = "";
	private String nQuadsFilePath = "";
	private static final String baseURI = "http://example.com/base/";

	private static final String QUERY = "" +
		"PREFIX rdb2rdftest: <http://purl.org/NET/rdb2rdf-test#> " +
		"SELECT ?rml ?nquad " +
		"WHERE { " +
		"?s a rdb2rdftest:R2RML ." +
		"?s rdb2rdftest:mappingDocument ?rml ." +
		"OPTIONAL {?s rdb2rdftest:output ?nquad} ." +
		"}";
	
	public R2RMLTest(String jdbcURL, String sqlFile, String r2rmlFilePath, String nQuadsFilePath) {
		this.jdbcURL = jdbcURL;
		this.sqlFile = sqlFile;
		this.r2rmlFilePath = r2rmlFilePath;
		this.nQuadsFilePath = nQuadsFilePath;
	}
	
	public static Collection<Object[]> getTestCases(File f) {
		Collection<Object[]> lstTests = new ArrayList<Object[]>();
		String absolutePath = f.getAbsolutePath();
		
		Model model = getJenaModel(absolutePath + "/manifest.ttl");
		Query query = QueryFactory.create(QUERY);
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet resultSet = qe.execSelect();
		Object[] arr;
		
		while (resultSet.hasNext()) {
			QuerySolution solution = resultSet.nextSolution();
			arr = new Object[4];
			arr[0] = "jdbc:hsqldb:mem:" + f.getName() + solution.getLiteral("rml").toString();
			arr[1] = absolutePath + "/create.sql";
			arr[2] = absolutePath + "/" + solution.getLiteral("rml").toString();
			if(solution.getLiteral("nquad") != null)
				arr[3] = absolutePath + "/" + solution.getLiteral("nquad").toString();
			else
				arr[3] = "";
			lstTests.add(arr);
		}
		
		return lstTests;
	}
	
	@Parameters
	public static Collection<Object[]> getAllTestLists() {
		Collection<Object[]> lstAllTests = new ArrayList<Object[]>();
		File dirPath = new File(TS_PATH);
		File[] dir = dirPath.listFiles();

		for (File f:dir) {
			if (f.isDirectory() && new File(f.getAbsolutePath()+ "/manifest.ttl").exists()) {
				lstAllTests.addAll(getTestCases(f));
			}
		}

		return lstAllTests;
	}

	@Test
	public void r2rmlValidator() {
		SystemLoader loader = new SystemLoader();
		loader.setJdbcURL(this.jdbcURL);
		loader.setMappingFileOrJdbcURL(this.r2rmlFilePath);
		loader.setGenerateW3CDirectMapping(true);
		loader.setStartupSQLScript(this.sqlFile);
		loader.setSystemBaseURI(baseURI);
//		Model d2rqModel = loader.getModelD2RQ();
//		Model model = getJenaModel(this.nQuadsFilePath);
//		assertEquals(true,model.isIsomorphicWith(d2rqModel));
	}
	
	public static Model getJenaModel(String inputFileName) {
		return FileManager.get().readModel(ModelFactory.createDefaultModel(),inputFileName);
	}
}
