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
import org.dvcama.lodview.bean.PropertyBean;
import org.dvcama.lodview.bean.ResultBean;
import org.dvcama.lodview.bean.TripleBean;
import org.dvcama.lodview.endpoint.SPARQLEndPoint;
import org.dvcama.lodview.utils.Misc;
import org.springframework.context.MessageSource;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class ResourceBuilder extends org.dvcama.lodview.builder.ResourceBuilder {

	private MessageSource messageSource;

	public ResourceBuilder() {
	}

	public ResourceBuilder(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public List<TripleBean> buildHtmlMainClassSearch(String IRI, String sparqlQuery, String rdfclass, int start, Locale locale, org.dvcama.lodview.conf.ConfigurationBean conf, ConfigurationBean confBygle, OntologyBean ontoBean) throws Exception {
		String preferredLanguage = conf.getPreferredLanguage();
		if (preferredLanguage.equals("auto")) {
			preferredLanguage = locale.getLanguage();
		}
		SPARQLEndPoint se = new SPARQLEndPoint(conf, ontoBean, locale.getLanguage());
		List<TripleBean> sbjtriples = new ArrayList<TripleBean>();
		List<TripleBean> triples = new ArrayList<TripleBean>();

		List<String> sparqlQueries = new ArrayList<String>();
		sparqlQueries.add(se.parseQuery((sparqlQuery.replaceAll("\\$\\{CLASS\\}", rdfclass)), IRI, null, start, ""));

		sbjtriples = se.doSubjectQuery(IRI, sparqlQueries, null);

		StringBuilder filter = new StringBuilder();
		for (String titleProperty : conf.getTitleProperties()) {
			if (titleProperty.toLowerCase().startsWith("http:")) {
				filter.append("(?p = <" + titleProperty + ">)");
			} else {
				filter.append("(?p = " + titleProperty + ")");
			}
			filter.append(" || ");
		}

		for (TripleBean triple : sbjtriples) {
			List<String> descrQuery = new ArrayList<String>();
			String q = ("SELECT distinct ?p ?o {<" + triple.getValue() + "> ?p ?o. FILTER (" + filter + ") }").replaceAll("\\|\\| \\)", ")");
			descrQuery.add(q);

			try {
				ResultBean b = buildHtmlResource(triple.getValue(), locale, conf, ontoBean, false, descrQuery);
				//System.out.println(b);
				TripleBean a = new TripleBean();
				a.setDataType("literal");
				a.setIRI(triple.getValue());
				a.setValue(b.getTitle());
				a.setNsIRI(Misc.toNsResource(triple.getValue(), conf));
				a.setUrl(Misc.toBrowsableUrl(triple.getValue(), conf));
				triples.add(a);
				System.out.println(a);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return triples;

	}

	public ResultBean buildHtmlResource(String IRI, Locale locale, ConfigurationBean conf, OntologyBean ontoBean, boolean localMode, List<String> queries) throws Exception {
		SPARQLEndPoint se = new SPARQLEndPoint(conf, ontoBean, locale.getLanguage());

		List<TripleBean> triples = new ArrayList<TripleBean>();
		if (conf.getEndPointUrl() != null && conf.getEndPointUrl().equals("<>")) {
			localMode = true;
		}
		if (localMode) {
			/* looking for data via content negotiation */
			Model m = ModelFactory.createDefaultModel();
			try {
				m.read(IRI);
			} catch (Exception e) {
				throw new Exception(messageSource.getMessage("error.noContentNegotiation", null, "sorry but content negotiation is not supported by the IRI", locale));
			}
			triples = se.doLocalQuery(m, IRI, queries);
		} else {
			triples = se.doQuery(IRI, queries, null);
		}

		return triplesToResult(IRI, triples, locale, conf, ontoBean);
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

}
