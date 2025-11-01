package fr.isima.glia.donoplatform.enums;

import lombok.Getter;

@Getter
public enum AnnouncementStatus {
    AVAILABLE("Available"),
    RESERVED("Reserved"),
    GIVEN("Given");

    private final String label;

    AnnouncementStatus(String label) {
        this.label = label;
    }

}