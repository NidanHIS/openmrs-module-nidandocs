package org.openmrs.module.nidandocs;

/**
 * The contract this module keeps with its consumers.
 *
 * @author Dipak Thapa &lt;dipakthapaofficial@gmail.com&gt;
 */
public final class NidanDocsConstants {

	private NidanDocsConstants() {
	}

	/** Master switch. Off by default: a site should choose to start writing documents. */
	public static final String GP_ENABLED = "nidandocs.enabled";

	/**
	 * The gating map, one property per encounter type.
	 *
	 * <p>Keyed by encounter type uuid rather than name. A name is a per-site label that
	 * somebody will eventually correct the spelling of; a uuid is the same everywhere the
	 * content package is installed. The appointments module matches a person attribute
	 * type by name and gets nothing from any patient at this site, which is what that
	 * choice costs.
	 */
	public static final String GP_ENCOUNTER_TYPE_PREFIX = "nidandocs.encounterType.";

	/** Render after the transaction commits, never inside it. */
	public static final String GP_ASYNC = "nidandocs.async";

	/**
	 * The captions the Odoo desk understands.
	 *
	 * <p>These are the contract, verified against
	 * {@code nidan_connector/models/nidan_visit.py::_OPENMRS_CAPTION_DOC_TYPE}, which
	 * matches by lowercase substring: "discharge", "referral", "visit summary". OpenMRS
	 * attachments carry no coded type, so the caption is the only thing carrying meaning
	 * and it has to stay stable on both sides.
	 */
	public static final String CAPTION_DISCHARGE_SUMMARY = "Discharge Summary";

	public static final String CAPTION_REFERRAL_LETTER = "Referral Letter";

	public static final String CAPTION_VISIT_SUMMARY = "Visit Summary";
}
