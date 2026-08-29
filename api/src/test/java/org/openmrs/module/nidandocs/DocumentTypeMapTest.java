package org.openmrs.module.nidandocs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;

/**
 * ST-2.4.2: the caption is a contract with Odoo code that already ships.
 *
 * @author Dipak Thapa &lt;dipakthapaofficial&#64;gmail.com&gt;
 */
public class DocumentTypeMapTest {

	private static final String DISCHARGE_TYPE = "et-discharge-uuid";

	private static final String REFERRAL_TYPE = "et-referral-uuid";

	private static final String VISIT_NOTE_TYPE = "et-visit-note-uuid";

	private static final String VITALS_TYPE = "et-vitals-uuid";

	private Map<String, String> config;

	private DocumentTypeMap map;

	@Before
	public void setUp() {
		config = new HashMap<String, String>();
		config.put(NidanDocsConstants.GP_ENABLED, "true");
		map = new DocumentTypeMap() {

			@Override
			protected String property(String name) {
				return config.get(name);
			}
		};
	}

	private void mapType(String typeUuid, String caption) {
		config.put(NidanDocsConstants.GP_ENCOUNTER_TYPE_PREFIX + typeUuid, caption);
	}

	private Encounter encounter(String typeUuid) {
		Encounter e = new Encounter();
		EncounterType type = new EncounterType();
		type.setUuid(typeUuid);
		e.setEncounterType(type);
		return e;
	}

	// ── AC1: the exact strings the Odoo desk matches on ─────────────────────────

	@Test
	public void ac1_theThreeCaptionsAreExactlyWhatOdooClassifies() {
		mapType(DISCHARGE_TYPE, "Discharge Summary");
		mapType(REFERRAL_TYPE, "Referral Letter");
		mapType(VISIT_NOTE_TYPE, "Visit Summary");

		assertEquals("Discharge Summary", map.captionFor(encounter(DISCHARGE_TYPE)));
		assertEquals("Referral Letter", map.captionFor(encounter(REFERRAL_TYPE)));
		assertEquals("Visit Summary", map.captionFor(encounter(VISIT_NOTE_TYPE)));
	}

	@Test
	public void ac1_everyCaptionSurvivesTheOdooSubstringMatch() {
		// _OPENMRS_CAPTION_DOC_TYPE lowercases the caption and asks whether each needle
		// is a substring, in this order. Asserting the rule rather than the three
		// answers means a fourth caption cannot be added here without checking it.
		assertEquals("discharge_summary", odooDocType("Discharge Summary"));
		assertEquals("referral_letter", odooDocType("Referral Letter"));
		assertEquals("visit_summary", odooDocType("Visit Summary"));
	}

	/** A transcription of the Odoo consumer's rule, kept here so a drift shows up. */
	private static String odooDocType(String caption) {
		String low = caption == null ? "" : caption.trim().toLowerCase();
		String[][] table = { { "discharge", "discharge_summary" }, { "referral", "referral_letter" },
		        { "visit summary", "visit_summary" }, { "radiolog", "radiology_reports" }, { "lab", "lab_results" } };
		for (String[] row : table) {
			if (low.contains(row[0])) {
				return row[1];
			}
		}
		return null;
	}

	// ── AC2: unmapped types are silent ──────────────────────────────────────────

	@Test
	public void ac2_anUnmappedTypeRendersNothingAndIsNotAnError() {
		mapType(DISCHARGE_TYPE, "Discharge Summary");
		assertNull("a Vitals encounter is not a finalisation and not a fault",
		    map.captionFor(encounter(VITALS_TYPE)));
	}

	@Test
	public void ac2_nothingIsWrittenWhileTheModuleIsOff() {
		config.put(NidanDocsConstants.GP_ENABLED, "false");
		mapType(DISCHARGE_TYPE, "Discharge Summary");
		assertNull(map.captionFor(encounter(DISCHARGE_TYPE)));
	}

	@Test
	public void anAbsentSwitchMeansOff() {
		// Deliberately the opposite default to the encounter and appointment publishers.
		// Writing a permanent clinical document should be a decision, not a side effect
		// of installing a module.
		config.remove(NidanDocsConstants.GP_ENABLED);
		mapType(DISCHARGE_TYPE, "Discharge Summary");
		assertNull(map.captionFor(encounter(DISCHARGE_TYPE)));
	}

	// ── AC3: configurable without a restart ─────────────────────────────────────

	@Test
	public void ac3_addingATypeTakesEffectOnTheNextEncounter() {
		assertNull(map.captionFor(encounter(REFERRAL_TYPE)));

		// The same instance, no restart, nothing to invalidate.
		mapType(REFERRAL_TYPE, "Referral Letter");
		assertEquals("Referral Letter", map.captionFor(encounter(REFERRAL_TYPE)));
	}

	// ── the typo guard ──────────────────────────────────────────────────────────

	@Test
	public void aCaptionOdooCannotClassifyWritesNothing() {
		// "Discharge summry" contains "discharge", so Odoo would in fact classify it —
		// but the next typo might not, and a permanent clinical document with a
		// hand-typed caption is not a thing to be relaxed about.
		mapType(DISCHARGE_TYPE, "Dischrge Summary");
		assertNull(map.captionFor(encounter(DISCHARGE_TYPE)));
	}

	@Test
	public void aDifferentlyCasedCaptionIsCanonicalised() {
		mapType(DISCHARGE_TYPE, "discharge summary");
		// The contract spelling, not the configured one.
		assertEquals("Discharge Summary", map.captionFor(encounter(DISCHARGE_TYPE)));
	}

	@Test
	public void anEncounterWithNoTypeIsIgnored() {
		assertNull(map.captionFor(new Encounter()));
		assertNull(map.captionFor(null));
	}
}
