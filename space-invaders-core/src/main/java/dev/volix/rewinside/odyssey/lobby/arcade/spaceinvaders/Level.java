package dev.volix.rewinside.odyssey.lobby.arcade.spaceinvaders;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Level {

    public static final Level MIN_LEVEL = new Level(20, 30, 1);

    public final long minSpeed;
    public final long maxSpeed;
    public final long speedStepSize;

    public final int aliensPerSpeedStep;
    public final long alienShotInterval;

    public final int level;

    public Level(final int minSpeed, final int alienShotInterval, final int level) {
        this.minSpeed = minSpeed * 50;
        this.maxSpeed = 50;
        this.speedStepSize = 50;
        this.aliensPerSpeedStep = 3;
        this.alienShotInterval = alienShotInterval * 50;
        this.level = level;
    }

    public Level generateNextLevel() {
        final int minSpeed = (int) (this.minSpeed / 50) - 1;
        final int alienShotInterval = (int) (this.alienShotInterval / 50) - 1;

        if (minSpeed <= 0 || alienShotInterval <= 0) return null;

        return new Level(minSpeed, alienShotInterval, this.level + 1);
    }

    public String name() {
        final String type;

        if (this.level <= 2) {
            type = "EINFACH";
        } else if (this.level <= 4) {
            type = "MITTEL";
        } else if (this.level <= 6) {
            type = "SCHWER";
        } else {
            type ="EXTREM";
        }

        return this.level + ": " + type;
    }

}
