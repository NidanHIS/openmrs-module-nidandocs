package org.openmrs.module.nidandocs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.BaseModuleActivator;

/**
 * Module lifecycle.
 *
 * <p>Says which document types are configured at startup, because the commonest way this
 * module does nothing is an empty gating map, and "started" in the admin list looks
 * identical either way.
 *
 * @author Dipak Thapa &lt;dipakthapaofficial@gmail.com&gt;
 */
public class NidanDocsActivator extends BaseModuleActivator {

	private static final Log log = LogFactory.getLog(NidanDocsActivator.class);

	@Override
	public void started() {
		log.info("Nidan clinical documents module started");
	}

	@Override
	public void stopped() {
		log.info("Nidan clinical documents module stopped");
	}
}
