package net.bygle.portal.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import net.bygle.portal.conf.ConfigurationBean;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.jena.riot.Lang;
import org.dvcama.lodview.bean.OntologyBean;
import org.dvcama.lodview.bean.ResultBean;
import org.dvcama.lodview.bean.TripleBean;
import org.dvcama.lodview.endpoint.SPARQLEndPoint;
import org.springframework.context.MessageSource;

import com.hp.hpl.jena.rdf.model.Model;

public class ResourceBuilder {

	private MessageSource messageSource;

	public ResourceBuilder() {
	}

	public ResourceBuilder(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public List<TripleBean> buildHtmlMainClassSearch(String sparqlQuery, String rdfclass, int start, Locale locale, org.dvcama.lodview.conf.ConfigurationBean conf, ConfigurationBean confBygle, OntologyBean ontoBean) {
		String preferredLanguage = conf.getPreferredLanguage();
		if (preferredLanguage.equals("auto")) {
			preferredLanguage = locale.getLanguage();
		}
		SPARQLEndPoint se = new SPARQLEndPoint(conf, ontoBean, locale.getLanguage());
		List<TripleBean> triples = new ArrayList<TripleBean>();

		List<String> sparqlQueries = new ArrayList<String>();
		sparqlQueries.add(sparqlQuery.replaceAll("\\$\\{CLASS\\}", rdfclass));
		try {
			// TODO: manage preferredLanguage
			triples.addAll(se.doQuery(rdfclass, null, start > 0 ? start : -1, sparqlQueries, null, "http://bygle.net/o/portal#label"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return triples;

	}

	public List<TripleBean> buildHtmlMainClassCount(String sparqlQuery, String rdfclass, Locale locale, org.dvcama.lodview.conf.ConfigurationBean conf, ConfigurationBean confBygle, OntologyBean ontoBean) {
		String preferredLanguage = conf.getPreferredLanguage();
		if (preferredLanguage.equals("auto")) {
			preferredLanguage = locale.getLanguage();
		}
		SPARQLEndPoint se = new SPARQLEndPoint(conf, ontoBean, locale.getLanguage());
		List<TripleBean> triples = new ArrayList<TripleBean>();

		List<String> sparqlQueries = new ArrayList<String>();
		sparqlQueries.add(sparqlQuery.replaceAll("\\$\\{CLASS\\}", rdfclass));
		try {
			// TODO: manage preferredLanguage
			triples.addAll(se.doQuery(rdfclass, null, -1, sparqlQueries, null, "http://bygle.net/o/portal#count"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return triples;

	}

	public Map<String, Integer> buildHtmlFacets(String query, Locale locale, org.dvcama.lodview.conf.ConfigurationBean conf, ConfigurationBean confBygle, OntologyBean ontoBean) {
		String preferredLanguage = conf.getPreferredLanguage();
		if (preferredLanguage.equals("auto")) {
			preferredLanguage = locale.getLanguage();
		}
		SPARQLEndPoint se = new SPARQLEndPoint(conf, ontoBean, locale.getLanguage());
		List<TripleBean> triples = new ArrayList<TripleBean>();

		List<String> sparqlQueries = new ArrayList<String>();
		sparqlQueries.add(query);
		try {
			// TODO: manage preferredLanguage
			triples.addAll(se.doQuery(null, null, -1, sparqlQueries, null, "http://bygle.net/o/portal#count"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		Map<String, Integer> result = new LinkedHashMap<String, Integer>();
		for (TripleBean triple : triples) {

			// try to leave lodview unmodified
			// typed literal
			String facet = triple.getIRI().replaceAll("\"(.+)\"\\^\\^.+", "$1");
			// untyped literal
			facet = facet.replaceAll("^\"(.+)\"$", "$1");
			result.put(facet, Integer.parseInt(triple.getValue()));
		}
		return result;

	}

	public ResultBean buildHtmlResource(String IRI, Locale locale, ConfigurationBean conf, OntologyBean ontoBean) throws Exception {
		return buildHtmlResource(IRI, locale, conf, ontoBean, false);
	}

	public ResultBean buildHtmlResource(String IRI, Locale locale, ConfigurationBean conf, OntologyBean ontoBean, boolean localMode) throws Exception {
		ResultBean result = new ResultBean();

		return result;
	}

	public String buildRDFResource(String IRI, String sparql, Lang lang, ConfigurationBean conf) throws Exception {
		String result = "empty content";

		return result;
	}

	public String buildRDFResource(String IRI, Model m, Lang lang, ConfigurationBean conf) throws Exception {
		String result = "empty content";

		return result;
	}

	public ResultBean buildPartialHtmlResource(String IRI, String[] abouts, Locale locale, ConfigurationBean conf, OntologyBean ontoBean, List<String> filterProperties) throws Exception {

		ResultBean result = new ResultBean();

		return result;
	}

}
