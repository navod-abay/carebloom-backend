package com.example.carebloom.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Canonical mapping of marketplace sections (sub-categories) to top-level/vendor categories.
 * Used for validation and to power section lists in vendor UI / public APIs.
 */
public class CategorySectionRegistry {

    public static class Section {
        private final Integer id;
        private final String label;
        private final List<String> allowedCategories;

        public Section(Integer id, String label, List<String> allowedCategories) {
            this.id = id;
            this.label = label;
            this.allowedCategories = allowedCategories == null ? Collections.emptyList() : allowedCategories;
        }

        public Integer getId() { return id; }
        public String getLabel() { return label; }
        public List<String> getAllowedCategories() { return allowedCategories; }
    }

    private static final List<Section> SECTIONS = new ArrayList<>();

    static {
        // Pregnancy-related
        SECTIONS.add(new Section(101, "Prenatal Vitamins", Arrays.asList("Nutrition","Supplements","Pregnancy")));
        SECTIONS.add(new Section(102, "Maternity Clothes", Arrays.asList("Clothing","Maternity","Pregnancy")));
        SECTIONS.add(new Section(103, "Pregnancy Pillows", Arrays.asList("Equipment","Care Products","Pregnancy")));
        SECTIONS.add(new Section(104, "Belly Support Belts", Arrays.asList("Equipment","Care Products","Pregnancy")));
        SECTIONS.add(new Section(105, "Stretch Mark Creams & Oils", Arrays.asList("Care Products","Skincare","Pregnancy")));
        SECTIONS.add(new Section(106, "Morning Sickness Remedies", Arrays.asList("Care Products","Supplements","Pregnancy")));
        SECTIONS.add(new Section(107, "BP & Glucose Monitors", Arrays.asList("Equipment","Health","Pregnancy")));

        // Postpartum
        SECTIONS.add(new Section(201, "Postpartum Recovery", Arrays.asList("Care Products","Postpartum")));
        SECTIONS.add(new Section(202, "Breast Pumps", Arrays.asList("Equipment","Postpartum")));
        SECTIONS.add(new Section(203, "Abdominal Binders", Arrays.asList("Care Products","Postpartum")));
        SECTIONS.add(new Section(204, "Herbal Teas for Recovery", Arrays.asList("Nutrition","Postpartum")));

        // Baby care
        SECTIONS.add(new Section(301, "Diapers & Wipes", Arrays.asList("Care Products","Baby Care")));
        SECTIONS.add(new Section(302, "Baby Skincare", Arrays.asList("Skincare","Baby Care")));
        SECTIONS.add(new Section(303, "Bathing Products", Arrays.asList("Care Products","Baby Care")));
        SECTIONS.add(new Section(304, "Feeding Bottles", Arrays.asList("Equipment","Baby Care")));
        SECTIONS.add(new Section(305, "Baby Formula & Food", Arrays.asList("Nutrition","Baby Care")));
        SECTIONS.add(new Section(306, "Baby Clothing & Accessories", Arrays.asList("Clothing","Baby Care")));
        SECTIONS.add(new Section(307, "Baby Thermometers", Arrays.asList("Equipment","Health","Baby Care")));
        SECTIONS.add(new Section(308, "Pacifiers & Teethers", Arrays.asList("Care Products","Toys","Baby Care")));

        // Newborn essentials
        SECTIONS.add(new Section(401, "Swaddle Blankets", Arrays.asList("Equipment","Newborn Essentials")));
        SECTIONS.add(new Section(402, "Cribs & Bassinets", Arrays.asList("Equipment","Newborn Essentials")));
        SECTIONS.add(new Section(403, "Baby Monitors", Arrays.asList("Equipment","Newborn Essentials")));
        SECTIONS.add(new Section(404, "Strollers & Car Seats", Arrays.asList("Equipment","Newborn Essentials")));
        SECTIONS.add(new Section(405, "Playmats & Bouncers", Arrays.asList("Toys","Newborn Essentials")));
        SECTIONS.add(new Section(406, "Nursing Pillows", Arrays.asList("Equipment","Newborn Essentials")));

        // Health & Safety
        SECTIONS.add(new Section(501, "First Aid Kits", Arrays.asList("Health & Safety","Health","Care Products")));
        SECTIONS.add(new Section(502, "Baby Proofing Products", Arrays.asList("Health & Safety","Care Products")));
        SECTIONS.add(new Section(503, "Humidifiers & Purifiers", Arrays.asList("Health & Safety","Equipment")));
        SECTIONS.add(new Section(504, "Non-contact Thermometers", Arrays.asList("Health & Safety","Equipment")));
        SECTIONS.add(new Section(505, "Vitamin D Drops", Arrays.asList("Nutrition","Health & Safety")));

        // Parenting & Education
        SECTIONS.add(new Section(601, "Parenting Books", Arrays.asList("Parenting & Education","Books","Toys")));
        SECTIONS.add(new Section(602, "Baby Development Toys", Arrays.asList("Toys","Parenting & Education")));

        // Mental health
        SECTIONS.add(new Section(701, "Self-care Kits", Arrays.asList("Mental Health","Wellness")));
    }

    public static List<Section> getAllSections() {
        return Collections.unmodifiableList(SECTIONS);
    }

    public static List<Section> getSectionsForCategory(String category) {
        if (category == null) return Collections.emptyList();
        return SECTIONS.stream()
                .filter(s -> s.getAllowedCategories().stream().anyMatch(c -> c.equalsIgnoreCase(category)))
                .collect(Collectors.toList());
    }

    public static boolean isSectionValidForCategory(Integer sectionId, String category) {
        if (sectionId == null) return false;
        return getSectionsForCategory(category).stream().anyMatch(s -> s.getId().equals(sectionId));
    }

    public static Map<Integer, String> getSectionIdToLabelMap() {
        return SECTIONS.stream().collect(Collectors.toMap(Section::getId, Section::getLabel));
    }
}
