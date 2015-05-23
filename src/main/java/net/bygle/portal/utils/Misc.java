package net.bygle.portal.utils;

import java.net.URLEncoder;
import java.util.Map;

import org.dvcama.lodview.bean.OntologyBean;
import org.dvcama.lodview.bean.PropertyBean;
import org.dvcama.lodview.conf.ConfigurationBean;
public class Misc extends org.dvcama.lodview.utils.Misc {

	public static String parseFilters(Map<String, String[]> filtersRequest, Map<String, String> filters, String query, String locale) {

		for (String filter : filters.keySet()) {
			try {
				String queryPart = filters.get(filter).replaceAll("\\$\\{FILTERVALUE\\}", filtersRequest.get(filter.replaceAll(":[0-9]+", ""))[0]);
				query = query.replaceAll("\\$\\{FILTER:" + filter + "}", queryPart);
			} catch (Exception e) {
			}
		}

		query = query.replaceAll("\\$\\{LOCALE\\}", locale);
		query = query.replaceAll("\\$\\{FILTER:[^}]+\\}", "");
		return query;
	}

	public static PropertyBean generatePropertyBean(String IRI, String locale, OntologyBean ontoBean, ConfigurationBean conf) {
		PropertyBean p = new PropertyBean();
		p.setNsProperty(Misc.toNsResource(IRI, conf));
		p.setProperty(IRI);
		if (ontoBean != null) {
			p.setLabel(ontoBean.getEscapedValue("label", locale, IRI));
			p.setComment(ontoBean.getEscapedValue("comment", locale, IRI));
		}
		p.setPropertyUrl(Misc.toBrowsableUrl(IRI, conf));
		return p;
	}
	public static String urlMinusFilter(Map<String, String[]> filtersRequest, Map<String, String> filters, String thisFilter) {

		String result = "";
		for (String filter : filtersRequest.keySet()) {
			if (filter.equals(thisFilter)) {
				continue;
			}
			try {
				for (String allowedFilter : filters.keySet()) {
					if (allowedFilter.equals(filter) || allowedFilter.matches(filter + ":[0-9]+")) {
						result += "&amp;" + filter + "=" + URLEncoder.encode(filtersRequest.get(filter)[0], "UTF-8");
					}
				}

			} catch (Exception e) {
				// e.printStackTrace();
			}
		}

		return result;
	}

}
