package com.stn.traders.item;

import net.minecraft.item.Item;
import net.minecraft.util.Formatting;

/**
 * Base class for trophy items dropped by special mobs.
 * Used for rare quest objectives.
 */
public class TrophyItem extends Item {

    private final String mobSource;
    private final Rarity rarity;

    public enum Rarity {
        COMMON(Formatting.WHITE, "Common"),
        UNCOMMON(Formatting.GREEN, "Uncommon"),
        RARE(Formatting.BLUE, "Rare"),
        EPIC(Formatting.DARK_PURPLE, "Epic");

        private final Formatting color;
        private final String displayName;

        Rarity(Formatting color, String displayName) {
            this.color = color;
            this.displayName = displayName;
        }

        public Formatting getColor() {
            return color;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public TrophyItem(Settings settings, String mobSource, Rarity rarity) {
        super(settings.maxCount(64));
        this.mobSource = mobSource;
        this.rarity = rarity;
    }

    public TrophyItem(String mobSource, Rarity rarity) {
        this(new Settings(), mobSource, rarity);
    }

    public String getMobSource() {
        return mobSource;
    }

    public Rarity getTrophyRarity() {
        return rarity;
    }
}
