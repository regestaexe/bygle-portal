package net.bygle.portal.utils;

import java.util.Map;

import org.dvcama.lodview.bean.OntologyBean;
import org.dvcama.lodview.bean.PropertyBean;
import org.dvcama.lodview.conf.ConfigurationBean;

public class Misc extends org.dvcama.lodview.utils.Misc {

	public static String parseFilters(Map<String, String[]> filters, String query) {

		// filter template: ${FILTER:(filtername) ... ${FILTERVALUE}}
		for (String filter : filters.keySet()) {
			System.out.println("------filter: " + filter);
			try {
				query = query.replaceAll("\\$\\{FILTER:" + filter + " ([^$]*)\\$\\{FILTERVALUE\\}([^}]*)\\}", "$1" + filters.get(filter)[0] + "$2");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		query = query.replaceAll("\\$\\{FILTER:[^$]*\\$\\{FILTERVALUE\\}[^}]*\\}", "");
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
}
