package io.github.talkarcabbage.planksacktracker.overlay;

import io.github.talkarcabbage.planksacktracker.PlankTier;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;

@Slf4j
public class Images {
    @Getter static final BufferedImage plankImage = tryLoad("/plank_small.png");
    @Getter static final BufferedImage oakImage = tryLoad("/oak_small.png");
    @Getter static final BufferedImage teakImage = tryLoad("/teak_small.png");
    @Getter static final BufferedImage mahoganyImage = tryLoad("/mahogany_small.png");
    @Getter static final BufferedImage rosewoodImage = tryLoad("/rosewood_small.png");
    @Getter static final BufferedImage ironwoodImage = tryLoad("/ironwood_small.png");
    @Getter static final BufferedImage camphorImage = tryLoad("/camphor_small.png");

    private static BufferedImage tryLoad(String resource) {
        try {
            return ImageUtil.loadImageResource(Images.class, resource);
        } catch (RuntimeException e) {
            log.error("Encountered an error while loading some plank icons!");
            return null;
        }
    }

    public static BufferedImage getIconForTier(PlankTier tier) {
        switch (tier) {
            case PLANK:
                return plankImage;
            case OAK:
                return oakImage;
            case TEAK:
                return teakImage;
            case MAHOGANY:
                return mahoganyImage;
            case ROSEWOOD:
                return rosewoodImage;
            case IRONWOOD:
                return ironwoodImage;
            case CAMPHOR:
                return camphorImage;
            case UNKNOWN:
            default:
                return plankImage;
        }
    }
}
