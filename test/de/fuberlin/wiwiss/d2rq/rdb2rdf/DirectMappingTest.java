package de.fuberlin.wiwiss.d2rq.rdb2rdf;

import java.io.File;
import java.io.InputStream;
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
import d2rq.dump_rdf;
import de.fuberlin.wiwiss.d2rq.D2RQException;


@RunWith(Parameterized.class)
public class DirectMappingTest {

	static String ts_path = "doc/rdb2rdf-ts/";
	static String[] arg = new String[10];
	Model model1;
	Model model2;
	boolean matched;
	
	public static void argumentList() {
		arg[0] = "--w3c";
		arg[1] = "-l";
		arg[2] = "";
		arg[3] = "-b";
		arg[4] = "http://example.com/base/";
		arg[5] = "-o";
		arg[6] = "";
		arg[7] = "-f";
		arg[8] = "TURTLE";
	}
	
	public DirectMappingTest(String file1, String file2) {
		Model model1 = getJenaModel(file1);
		Model model2 = getJenaModel(file2);
		System.out.println(file1);
		if(model1.isIsomorphicWith(model2))
			this.matched = true;
		else
			this.matched =  false;
	}
	
	@Parameters
	public static Collection data() {
		argumentList();
		File dirPath = new File(ts_path);
		File[] dir = dirPath.listFiles();
		String[][] arr = new String[dir.length][2];
		int index=0;
		
		for(File f:dir) {
			if(f.isDirectory()) {
				arg[2] = f.getAbsolutePath() + "/create.sql";
				arg[6] = f.getAbsolutePath() + "/directGraph-d2rq.ttl";
				arg[9] = "jdbc:hsqldb:mem:" + f.getName();
				new dump_rdf().process(arg);
				
				arr[index][0] = f.getAbsolutePath() + "/directGraph.ttl";
				arr[index][1] = f.getAbsolutePath() + "/directGraph-d2rq.ttl";
				index++;
			}
		}
		return Arrays.asList(arr);
	}
	
	@Test
	public void directMappingValidator() {
		assertEquals(true,matched);
	}
	
	public static Model getJenaModel(String inputFileName) {
		Model model = ModelFactory.createDefaultModel();
		InputStream in = FileManager.get().open(inputFileName);
		if (in == null) {
		    throw new D2RQException(
		    		"File: " + inputFileName + " not found",
		    		D2RQException.FILE_NOT_FOUND);
		}
		model.read(in,"","TURTLE");
		return model;
	}
	
}
