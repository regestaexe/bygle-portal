package net.bygle.portal.builder;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.bygle.portal.bean.FacetBean;
import net.bygle.portal.conf.ConfigurationBean;

import org.dvcama.lodview.bean.OntologyBean;
import org.dvcama.lodview.bean.TripleBean;
import org.dvcama.lodview.endpoint.SPARQLEndPoint;
import org.springframework.context.MessageSource;

import com.hp.hpl.jena.rdf.model.Model;

public class ResourceBuilder extends org.dvcama.lodview.builder.ResourceBuilder {

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

	public List<FacetBean> buildHtmlFacets(String query, String alias, Locale locale, org.dvcama.lodview.conf.ConfigurationBean conf, ConfigurationBean confBygle, OntologyBean ontoBean) {
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

		List<FacetBean> result = new ArrayList<FacetBean>();
		for (TripleBean triple : triples) {

			// try to leave lodview unmodified
			// typed literal
			String facet = triple.getIRI().replaceAll("\"(.+)\"\\^\\^.+", "$1");
			// untyped literal
			facet = facet.replaceAll("^\"(.+)\"$", "$1");

			FacetBean b = new FacetBean();
			b.setAlias(alias);
			b.setCount(Integer.parseInt(triple.getValue()));
			try {
				b.setValue(java.net.URLEncoder.encode(triple.getIRI().replaceAll("\"\\^\\^(.+)", "\"^^<$1>"), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
			}
			b.setLabel(facet);

			result.add(b);
		}
		return result;

	}

	public Map<Object, Object> buildPedegreeData(String iRI, Model m, org.dvcama.lodview.conf.ConfigurationBean conf) {
		Map<Object, Object> result = new HashMap<Object, Object>();

		result.put("prova", "prova");

		return result;
	}

}
