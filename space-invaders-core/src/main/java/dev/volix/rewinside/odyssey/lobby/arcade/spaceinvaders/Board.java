package dev.volix.rewinside.odyssey.lobby.arcade.spaceinvaders;

import java.awt.*;
import lombok.Getter;
import dev.volix.rewinside.odyssey.common.frames.alignment.Alignment;
import dev.volix.rewinside.odyssey.common.frames.component.ColorComponent;
import dev.volix.rewinside.odyssey.common.frames.component.CompoundComponent;
import dev.volix.rewinside.odyssey.common.frames.component.TextComponent;
import dev.volix.rewinside.odyssey.lobby.arcade.helper.ColorHelpersKt;
import dev.volix.rewinside.odyssey.common.frames.resource.font.FontAdapter;

/**
 * @author Benedikt Wüller
 */
public class Board extends CompoundComponent {

    private static final String FONT_NAME = "JetBrainsMono-ExtraBold";
    private static final float FONT_SIZE = 15.0f;

    private static final Color DEATH_LINE_COLOR = ColorHelpersKt.darken(Color.RED, 0.35);
    private static final Color TEXT_COLOR = Color.DARK_GRAY.darker();

    private final TextComponent scoreDisplay;
    private final TextComponent levelDisplay;

    public final Rectangle deathZone;

    private int score;

    public Board(final Dimension dimensions, final int deathZoneHeight, final FontAdapter fontAdapter) {
        super(new Point(), dimensions);
        this.deathZone = new Rectangle(0, deathZoneHeight, dimensions.width, 1);

        this.addComponent(new ColorComponent(this.deathZone.getLocation(), this.deathZone.getSize(), DEATH_LINE_COLOR));

        final Font font = fontAdapter.get(FONT_NAME, FONT_SIZE);

        this.scoreDisplay = new TextComponent(new Point(4, deathZoneHeight - 4 - 20), null, TEXT_COLOR, font, Alignment.TOP_LEFT);
        this.addComponent(this.scoreDisplay);

        this.levelDisplay = new TextComponent(new Point(dimensions.width - 4, deathZoneHeight - 4 - 20), null, TEXT_COLOR, font, Alignment.TOP_RIGHT);
        this.addComponent(this.levelDisplay);
    }

    public void setScore(final int score) {
        this.score = score;
        this.scoreDisplay.setText("SCORE: " + score);
    }

    public void setLevel(final Level level) {
        this.levelDisplay.setText(level == null ? null : level.name());
    }

    public int getScore() {
        return score;
    }
}
