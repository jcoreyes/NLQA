package l2f.interpretation.classification;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public enum QuestionCategory {

    ABBREVIATION_ABBREVIATION,
    ABBREVIATION_EXPANSION,
    ABBREVIATION,
    DESCRIPTION_DEFINITION,
    DESCRIPTION_DESCRIPTION,
    DESCRIPTION_MANNER,
    DESCRIPTION_REASON,
    DESCRIPTION,
    ENTITY_ANIMAL,
    ENTITY_BODY,
    ENTITY_COLOR,
    ENTITY_CREATIVE,
    ENTITY_CURRENCY,
    ENTITY_MEDICINE,
    ENTITY_EVENT,
    ENTITY_FOOD,
    ENTITY_INSTRUMENT,
    ENTITY_LANGUAGE,
    ENTITY_LETTER,
    ENTITY_OTHER,
    ENTITY_PLANT,
    ENTITY_PRODUCT,
    ENTITY_RELIGION,
    ENTITY_SPORT,
    ENTITY_SUBSTANCE,
    ENTITY_SYMBOL,
    ENTITY_TECHNIQUE,
    ENTITY_TERM,
    ENTITY_VEHICLE,
    ENTITY_WORD,
    ENTITY,
    HUMAN_DESCRIPTION,
    HUMAN_GROUP,
    HUMAN_INDIVIDUAL,
    HUMAN_TITLE,
    HUMAN,
    LOCATION_CITY,
    LOCATION_COUNTRY,
    LOCATION_MOUNTAIN,
    LOCATION_OTHER,
    LOCATION_STATE,
    LOCATION,
    NUMERIC_CODE,
    NUMERIC_COUNT,
    NUMERIC_DATE,
    NUMERIC_DISTANCE,
    NUMERIC_MONEY,
    NUMERIC_ORDER,
    NUMERIC_OTHER,
    NUMERIC_PERCENT,
    NUMERIC_PERIOD,
    NUMERIC_SPEED,
    NUMERIC_TEMPERATURE,
    NUMERIC_SIZE,
    NUMERIC_WEIGHT,
    NUMERIC,
    VOID;

    public static List<String> asCollection() {
        QuestionCategory[] all = values();
        List<String> categories = new ArrayList<String>();
        for (QuestionCategory qc : all) {
            categories.add(qc.toString());
        }
        return categories;
    }

    public static QuestionCategory getCategory(String category) {
        return QuestionCategory.valueOf(category);
    }

    public static String getCoarseCategory(String fine) {
        String coarse = fine;
        if (fine.indexOf('_') != -1) {
            coarse = fine.substring(0, fine.indexOf('_'));
        }
        return coarse;
    }

    public static String[] toFineStringArray() {
        QuestionCategory[] all = values();
        String[] categories = new String[all.length];
        for (int i = 0; i < all.length; i++) {
            categories[i] = all[i].toString();
        }
        return categories;
    }

    public static String[] toCoarseStringArray() {
        QuestionCategory[] all = values();
        Set<String> categories = new HashSet<String>();
        for (QuestionCategory qc : all) {
            String fine = qc.toString();
            String coarse = fine;
            if (fine.indexOf('_') != -1) {
                coarse = fine.substring(0, fine.indexOf('_'));
            }
            categories.add(coarse);
        }
        String[] coarse = new String[categories.size()];
        categories.toArray(coarse);
        return coarse;
    }
}
