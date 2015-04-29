package net.bygle.portal.conf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletContext;

import org.apache.jena.riot.RDFDataMgr;
import org.springframework.web.context.ServletContextAware;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class ConfigurationBean implements ServletContextAware, Cloneable {

	private Model confModel = null;
	private ServletContext context;
	private String confFile;
	private List<String> mainIRIs = null;

	Random rand = new Random();

	public ConfigurationBean() throws IOException, Exception {

	}

	public void populateBean() throws IOException, Exception {
		System.out.println("Initializing configuration " + confFile);
		File configFile = new File(confFile);
		if (!configFile.isAbsolute()) {
			configFile = new File(context.getRealPath("/") + "/WEB-INF/" + confFile);
		}
		if (!configFile.exists()) {
			throw new Exception("Configuration file not found (" + configFile.getAbsolutePath() + ")");
		}
		confModel = RDFDataMgr.loadModel(configFile.getAbsolutePath());

		mainIRIs = getMultiConfValue("mainIRIs");

	}

	public String getSingleConfValue(String prop) {
		return getSingleDefaultConfValue(prop, null);
	}

	public String getSingleConfValue(String IRI, String prop) {
		return getSingleConfValue(IRI, prop, null);
	}

	public String getSingleConfValue(String IRI, String prop, String defaultValue) {
		NodeIterator iter = confModel.listObjectsOfProperty(confModel.createResource(IRI), confModel.createProperty(confModel.getNsPrefixURI("conf"), prop));
		while (iter.hasNext()) {
			RDFNode node = iter.next();
			return node.toString();
		}
		return defaultValue;
	}

	public String getSingleDefaultConfValue(String prop, String defaultValue) {
		NodeIterator iter = confModel.listObjectsOfProperty(confModel.createProperty(confModel.getNsPrefixURI("conf"), prop));
		while (iter.hasNext()) {
			RDFNode node = iter.next();
			return node.toString();
		}
		return defaultValue;
	}

	public List<String> getMultiConfValue(String IRI, String prop) {
		List<String> result = new ArrayList<String>();
		NodeIterator iter = confModel.listObjectsOfProperty(confModel.createResource(IRI), confModel.createProperty(confModel.getNsPrefixURI("conf"), prop));
		while (iter.hasNext()) {
			RDFNode node = iter.next();
			result.add(node.toString());
		}
		return result;
	}

	public List<String> getMultiConfValue(String prop) {
		List<String> result = new ArrayList<String>();
		NodeIterator iter = confModel.listObjectsOfProperty(confModel.createProperty(confModel.getNsPrefixURI("conf"), prop));
		while (iter.hasNext()) {
			RDFNode node = iter.next();
			result.add(node.toString());
		}
		return result;
	}

	public void setConfFile(String confFile) {
		this.confFile = confFile;
	}

	@Override
	public void setServletContext(ServletContext arg0) {
		this.context = arg0;
		try {
			populateBean();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public Model getConfModel() {
		return confModel;
	}

	public Map<String, String> getPrefixes() {
		return confModel.getNsPrefixMap();
	}

	public String getNsPrefixURI(String prefix) {
		return confModel.getNsPrefixURI(prefix);
	}

	public String getNsURIPrefix(String IRI) {
		return confModel.getNsURIPrefix(IRI);
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new Error("Something impossible just happened");
		}
	}

	@Override
	public String toString() {
		return "ConfigurationBean [confModel=" + confModel + ", context=" + context + ", confFile=" + confFile + ", mainIRIs=" + mainIRIs + ", rand=" + rand + "]";
	}

	public List<String> getMainIRIs() {
		return mainIRIs;
	}

	public void setMainIRIs(List<String> mainIRIs) {
		this.mainIRIs = mainIRIs;
	}

}
