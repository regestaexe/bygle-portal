package net.bygle.portal.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.bygle.portal.conf.ConfigurationBean;

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

	public ResultBean buildHtmlMainClassSearch(String IRI, String rdfclass, int start, Locale locale, org.dvcama.lodview.conf.ConfigurationBean conf, ConfigurationBean confBygle, OntologyBean ontoBean) throws Exception {
		ResultBean result = new ResultBean();
		String preferredLanguage = conf.getPreferredLanguage();
		if (preferredLanguage.equals("auto")) {
			preferredLanguage = locale.getLanguage();
		}
		SPARQLEndPoint se = new SPARQLEndPoint(conf, ontoBean, locale.getLanguage());
		String sparqlQuery = confBygle.getSingleConfValue(IRI, "mainQuery");
 		List<TripleBean> triples = new ArrayList<TripleBean>();

		List<String> sparqlQueries = new ArrayList<String>();
		sparqlQueries.add(sparqlQuery.replaceAll("\\$\\{CLASS\\}", rdfclass));
		try {
			triples.addAll(se.doQuery(rdfclass, null, start > 0 ? start : -1, sparqlQueries, null, "http://bygle.net/o/portal#label"));

		} catch (Exception e) {
			e.printStackTrace();
		}
		Map<String, List<TripleBean>> l = new HashMap<String, List<TripleBean>>();
		List<TripleBean> literals = new ArrayList<TripleBean>();

		for (TripleBean tripleBean : triples) {
			if (tripleBean.getType().equals("literal")) {
				List<TripleBean> al = l.get(tripleBean.getProperty().getProperty());
				if (al == null) {
					al = new ArrayList<TripleBean>();
				}
				al.add(tripleBean);
				l.put(tripleBean.getProperty().getProperty(), al);
			}
		}
		for (String about : l.keySet()) {
			List<TripleBean> al = l.get(about);
			boolean betterTitleMatch = false;
			TripleBean title = null;
			for (TripleBean tripleBean : al) {
				if (!betterTitleMatch && (title == null || title.getValue() == null || title.getValue().trim().equals("") || preferredLanguage.equals(tripleBean.getLang()) || tripleBean.getLang().equals("en"))) {
					title = tripleBean;
					if (preferredLanguage.equals(tripleBean.getLang())) {
						betterTitleMatch = true;
					}
				}
			}
			if (title != null) {
				literals.add(title);
			}
		}
		result.setLiterals(IRI, literals);
		System.out.println(result);
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
