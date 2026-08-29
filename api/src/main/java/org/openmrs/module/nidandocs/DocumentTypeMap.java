package org.openmrs.module.nidandocs;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.api.context.Context;

/**
 * Which encounter types produce a document, and what that document is called.
 *
 * <p>Read from global properties at every call, never cached, so adding a type takes
 * effect on the next encounter rather than the next restart.
 *
 * <p>The caption is a hard contract with Odoo code that already ships:
 * {@code _OPENMRS_CAPTION_DOC_TYPE} matches by lowercase substring — "discharge",
 * "referral", "visit summary" — against the attachment caption. OpenMRS attachments
 * carry no coded type, so that string is the only thing carrying meaning across the
 * boundary. A near-miss produces a document the desk will not classify and nobody will
 * notice until somebody asks where a discharge summary went.
 *
 * @author Dipak Thapa &lt;dipakthapaofficial@gmail.com&gt;
 */
public class DocumentTypeMap {

	private static final Log log = LogFactory.getLog(DocumentTypeMap.class);

	/**
	 * Captions this module is willing to write.
	 *
	 * <p>A closed list on purpose. The gating map is configuration and configuration gets
	 * typed by hand; "Discharge summry" would sail through, produce a permanent clinical
	 * document, and never be classified by the desk.
	 */
	private static final String[] SUPPORTED_CAPTIONS = {
	        NidanDocsConstants.CAPTION_DISCHARGE_SUMMARY,
	        NidanDocsConstants.CAPTION_REFERRAL_LETTER,
	        NidanDocsConstants.CAPTION_VISIT_SUMMARY };

	/**
	 * The caption for this encounter, or null if it does not produce a document.
	 *
	 * <p>Null is the ordinary answer. Most encounters are not finalisations, and an
	 * unmapped type is a site's decision rather than a fault — so nothing is logged
	 * above debug for it.
	 */
	public String captionFor(Encounter encounter) {
		if (encounter == null || !enabled()) {
			return null;
		}
		EncounterType type = encounter.getEncounterType();
		if (type == null || type.getUuid() == null) {
			return null;
		}
		String configured = property(NidanDocsConstants.GP_ENCOUNTER_TYPE_PREFIX + type.getUuid());
		if (configured == null || configured.trim().isEmpty()) {
			log.debug("nidandocs: encounter type " + type.getUuid() + " produces no document");
			return null;
		}
		String caption = configured.trim();
		String supported = supported(caption);
		if (supported == null) {
			// Loud, unlike an unmapped type: somebody meant this to produce a document
			// and it silently will not. Naming the supported set makes the typo obvious.
			log.error("nidandocs: caption '" + caption + "' configured for encounter type " + type.getUuid()
			        + " is not one the Odoo desk understands; no document will be written. Supported: "
			        + String.join(", ", SUPPORTED_CAPTIONS));
			return null;
		}
		// The canonical spelling, not the configured one. A site that writes
		// "discharge summary" gets a document captioned "Discharge Summary", because the
		// caption is a contract and not a label somebody chose.
		return supported;
	}

	private static String supported(String caption) {
		for (String candidate : SUPPORTED_CAPTIONS) {
			if (candidate.equalsIgnoreCase(caption)) {
				return candidate;
			}
		}
		return null;
	}

	/** Every encounter type uuid the site has mapped, for the startup log and for support. */
	public List<String> configuredEncounterTypeUuids() {
		List<String> uuids = new ArrayList<String>();
		try {
			for (org.openmrs.GlobalProperty gp : Context.getAdministrationService().getAllGlobalProperties()) {
				if (gp.getProperty() != null
				        && gp.getProperty().startsWith(NidanDocsConstants.GP_ENCOUNTER_TYPE_PREFIX)) {
					uuids.add(gp.getProperty().substring(NidanDocsConstants.GP_ENCOUNTER_TYPE_PREFIX.length()));
				}
			}
		}
		catch (Throwable t) {
			// No session, or the service is not up. An empty list, not a failure.
		}
		return uuids;
	}

	boolean enabled() {
		String value = property(NidanDocsConstants.GP_ENABLED);
		// Absent means off here, unlike the appointment and encounter publishers where
		// absent means on. Writing a permanent clinical document is not something that
		// should start happening because a module was installed.
		return value != null && "true".equalsIgnoreCase(value.trim());
	}

	protected String property(String name) {
		try {
			return Context.getAdministrationService().getGlobalProperty(name);
		}
		catch (Throwable t) {
			return null;
		}
	}
}
