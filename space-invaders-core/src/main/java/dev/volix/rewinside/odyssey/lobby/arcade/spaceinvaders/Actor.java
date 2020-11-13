package dev.volix.rewinside.odyssey.lobby.arcade.spaceinvaders;

import java.awt.Point;
import dev.volix.rewinside.odyssey.common.frames.component.SpriteComponent;
import dev.volix.rewinside.odyssey.common.frames.resource.image.SpriteSheet;

/**
 * @author Benedikt Wüller
 */
public class Actor extends SpriteComponent {

    public Actor(final Point position, final SpriteSheet spriteSheet, final Type type) {
        super(position, spriteSheet, type.spriteIndex);
    }

    public enum Type {
        ENEMY_SMALL(0),
        ENEMY_MEDIUM(1),
        ENEMY_LARGE(2),
        PLAYER(3);

        final int spriteIndex;

        Type(final int spriteIndex) {
            this.spriteIndex = spriteIndex;
        }
    }

}
