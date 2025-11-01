package fr.isima.glia.donoplatform.enums;

import lombok.Getter;

@Getter
public enum ItemCondition {
    NEW("New"),
    LIKE_NEW("Like New"),
    GOOD("Good"),
    FAIR("Fair"),
    POOR("Poor");

    private final String label;

    ItemCondition(String label) {
        this.label = label;
    }

}