package dev.volix.rewinside.odyssey.lobby.arcade.spaceinvaders;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import dev.volix.rewinside.odyssey.common.frames.component.ImageComponent;

/**
 * @author Benedikt Wüller
 */
public class Barricade extends ImageComponent {

    public Barricade(final int x, final int y, final BufferedImage image) {
        super(new Point(x, y), new Dimension(image.getWidth(), image.getHeight()), image);
    }

    public void destroy(final int x, final int y, final double chance) {
        if (!this.calculateBounds().contains(x, y)) return;
        if (Math.random() > chance) return;

        final BufferedImage image = this.getImage();
        if (image == null) return;

        image.setRGB(x - this.getPosition().x, y - this.getPosition().y, 0);
        this.setDirty(true);
    }

}
