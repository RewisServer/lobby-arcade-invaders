package dev.volix.rewinside.odyssey.lobby.arcade.spaceinvaders;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import dev.volix.rewinside.odyssey.common.frames.component.ColorComponent;

/**
 * @author Benedikt Wüller
 */
public class Shot extends ColorComponent {

    private static final Dimension DIMENSIONS = new Dimension(1, 3);
    private static final int SPEED = 4;

    public final Type type;

    public Shot(final int x, final int y, final Type type) {
        super(new Point(x, y), DIMENSIONS, type.color);
        this.type = type;
    }

    public void move() {
        this.getPosition().y += this.type.speed;
    }

    public enum Type {
        PLAYER(Color.GREEN, -SPEED),
        ALIEN(Color.WHITE, SPEED);

        public final Color color;
        public final int speed;

        Type(final Color color, final int speed) {
            this.color = color;
            this.speed = speed;
        }
    }

}
