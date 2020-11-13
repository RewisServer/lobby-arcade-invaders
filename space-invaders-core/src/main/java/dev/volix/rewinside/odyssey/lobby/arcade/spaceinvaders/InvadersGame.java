package dev.volix.rewinside.odyssey.lobby.arcade.spaceinvaders;

import dev.volix.rewinside.odyssey.lobby.arcade.SongPlayerFrameGame;
import dev.volix.rewinside.odyssey.lobby.arcade.nbs.song.SongPlayer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import dev.volix.rewinside.odyssey.common.frames.alignment.Alignment;
import dev.volix.rewinside.odyssey.common.frames.color.ColorTransformer;
import dev.volix.rewinside.odyssey.common.frames.component.ColorComponent;
import dev.volix.rewinside.odyssey.common.frames.component.ImageComponent;
import dev.volix.rewinside.odyssey.common.frames.component.TextComponent;
import dev.volix.rewinside.odyssey.lobby.arcade.GameState;
import dev.volix.rewinside.odyssey.lobby.arcade.InputKey;
import dev.volix.rewinside.odyssey.common.frames.resource.font.FontAdapter;
import dev.volix.rewinside.odyssey.common.frames.resource.image.ImageAdapter;

public class InvadersGame extends SongPlayerFrameGame {

    private static final int HORIZONTAL_STEP_SIZE = 4;
    private static final int VERTICAL_STEP_SIZE = 2;
    private static final int MAX_COMBO = 10;

    protected Level level = null;

    private final ImageAdapter imageAdapter;

    protected final Board board;

    private final Point alienBasePosition = new Point();
    private final List<Actor> aliens = new ArrayList<>();

    private final Actor player;

    private final Set<Shot> shots = new HashSet<>();

    private final Barricade[] barricades = new Barricade[3];

    private boolean moveRight = true;
    private boolean canShoot = true;
    private long lastShot = 0;

    private long speed = 0L;
    private long lastUpdate = 0L;

    private int combo = 0;
    protected int maxCombo = 0;

    public InvadersGame(final Dimension viewportDimensions, final ColorTransformer transformer,
                        final ImageAdapter imageAdapter, final FontAdapter fontAdapter, final SongPlayer songPlayer) {
        super(fontAdapter, new Dimension(256, 128), viewportDimensions, 50L, transformer, songPlayer);

        this.setInputDescription(InputKey.LEFT, "Links");
        this.setInputDescription(InputKey.RIGHT, "Rechts");
        this.setInputDescription(InputKey.SPACE, "Schießen");

        this.setKeyRepeatInterval(50);

        this.imageAdapter = imageAdapter;

        this.player = new Actor(new Point(), imageAdapter.getSheet("actors", 16), Actor.Type.PLAYER);

        this.getBaseComponent().addComponent(new ImageComponent(new Point(), this.getCanvasDimensions(), imageAdapter.get("background")));

        this.board = new Board(this.getCanvasDimensions(), 128 - 32, fontAdapter);
        this.getBaseComponent().addComponent(this.board);
        this.getBaseComponent().addComponent(this.player);

        this.getIdleComponent().addComponent(new ImageComponent(new Point(), this.getCanvasDimensions(), imageAdapter.get("idle")));

        this.getGameOverComponent().addComponent(this.getBaseComponent());

        this.board.setScore(0);
        this.setLevel(Level.MIN_LEVEL);
    }

    private void updateBarricades(final ImageAdapter imageAdapter) {
        for (final Barricade barricade : this.barricades) {
            if (barricade == null) continue;
            this.getBaseComponent().removeComponent(barricade);
        }

        // NOTE: Images are retrieved separately to make sure they are individual instances.
        this.barricades[0] = new Barricade(32, 128 - 48, imageAdapter.get("barricade"));
        this.barricades[1] = new Barricade(128 - 16, 128 - 48, imageAdapter.get("barricade"));
        this.barricades[2] = new Barricade(128 + 64, 128 - 48, imageAdapter.get("barricade"));

        for (final Barricade barricade : this.barricades) {
            if (barricade == null) continue;
            this.getBaseComponent().addComponent(barricade);
        }
    }

    private void setLevel(final Level level) {
        if (this.level == null) {
            this.player.getPosition().move(0, this.getCanvasDimensions().height - 16);
        }

        this.level = level;

        this.resetAliens();
        this.resetShots();

        this.moveRight = true;
        this.speed = this.level.minSpeed;

        this.updateBarricades(this.imageAdapter);

        this.board.setLevel(level);
    }

    private void resetAliens() {
        for (final Actor alien : this.aliens) {
            this.getBaseComponent().removeComponent(alien);
        }
        this.aliens.clear();

        this.alienBasePosition.move(0, 0);
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 11; x++) {
                final Actor.Type type = y == 0 ? Actor.Type.ENEMY_SMALL : (y <= 2 ? Actor.Type.ENEMY_MEDIUM : Actor.Type.ENEMY_LARGE);
                final Actor alien = new Actor(new Point(x * 16, y * 16), this.imageAdapter.getSheet("actors", 16), type);
                this.aliens.add(alien);
                this.getBaseComponent().addComponent(alien);
            }
        }
    }

    private void resetShots() {
        for (final Shot shot : this.shots) {
            this.getBaseComponent().removeComponent(shot);
        }
        this.shots.clear();

        this.lastShot = 0;
        this.canShoot = true;
    }

    private void moveAliens(final int dx, final int dy) {
        this.alienBasePosition.translate(dx, dy);
        for (final Actor alien : this.aliens) {
            alien.getPosition().translate(dx, dy);
        }
    }

    @Override
    protected boolean onUpdate(final long currentTime, final long delta) {
        if (!super.onUpdate(currentTime, delta)) return false;

        if ((currentTime - this.lastUpdate) >= this.speed) {
            this.lastUpdate = currentTime;
            this.handleAlienMovement();
        }

        this.handleShots();

        // Check whether an alien should shoot.
        if ((currentTime - this.lastShot) > this.level.alienShotInterval) {
            this.lastShot = currentTime;

            // Find possible shot positions
            final Map<Integer, Actor> possibleAliens = new HashMap<>();
            for (final Actor alien : this.aliens) {
                final Actor other = possibleAliens.get(alien.getPosition().x);
                if (other == null) {
                    possibleAliens.put(alien.getPosition().x, alien);
                    continue;
                }

                if (other.getPosition().y >= alien.getPosition().y) continue;
                possibleAliens.put(alien.getPosition().x, alien);
            }

            if (!possibleAliens.isEmpty()) {
                final List<Integer> values = new ArrayList<>(possibleAliens.keySet());
                final int key = values.get((int) Math.round(Math.random() * (values.size() - 1)));
                final Actor alien = possibleAliens.get(key);

                final Shot shot = new Shot(
                        alien.getPosition().x + alien.getDimensions().width / 2,
                        alien.getPosition().y + alien.getDimensions().height,
                        Shot.Type.ALIEN
                );

                this.shots.add(shot);
                this.getBaseComponent().addComponent(shot);
            }
        }

        return true;
    }

    private void handleShots() {
        for (final Shot shot : new HashSet<>(this.shots)) {
            shot.move();

            if (shot.type == Shot.Type.PLAYER) {
                if (this.handlePlayerShot(shot)) {
                    this.shots.remove(shot);
                    this.getBaseComponent().removeComponent(shot);
                    this.canShoot = true;
                    continue;
                }
            } else {
                if (this.handleAlienShot(shot)) {
                    if (this.getState() != GameState.RUNNING) continue;
                    this.shots.remove(shot);
                    this.getBaseComponent().removeComponent(shot);
                    continue;
                }
            }

            for (final Barricade barricade : this.barricades) {
                final Rectangle bounds = shot.calculateBounds();
                if (!barricade.intersectsPixels(bounds)) continue;

                final Rectangle section = barricade.calculateBounds().intersection(bounds);

                final int breakSize = (int) (Math.round(Math.random() * 4) + 3);
                for (int dy = 0; dy < breakSize + shot.getDimensions().height; dy++) {
                    for (int dx = 0; dx < breakSize + shot.getDimensions().width; dx++) {
                        final int x = section.x - breakSize / 2 + dx;
                        final int y = section.y - breakSize / 2 + dy;
                        barricade.destroy(x, y, shot.calculateBounds().contains(x, y) ? 1.0 : 0.65);
                    }
                }

                this.shots.remove(shot);
                this.getBaseComponent().removeComponent(shot);
                if (shot.type == Shot.Type.PLAYER) {
                    this.canShoot = true;
                    this.combo = 0;
                }
            }
        }
    }

    /**
     * @param shot the shot to handle
     * @return whether something was hit.
     */
    protected boolean handlePlayerShot(final Shot shot) {
        if (shot.getPosition().y <= 0) {
            this.combo = 0;
            return true;
        }

        for (final Actor alien : new HashSet<>(this.aliens)) {
            if (!alien.intersectsPixels(shot.calculateBounds())) continue;

            this.aliens.remove(alien);
            this.getBaseComponent().removeComponent(alien);

            final double multiplier = 1.0 + Math.min(1.0, Math.max(0.0, this.combo * 1.0 / MAX_COMBO));
            final int score = (int) Math.round(this.level.level * multiplier);
            this.board.setScore(this.board.getScore() + score);

            this.combo++;
            this.maxCombo = Math.max(this.combo, this.maxCombo);

            if ((55 - this.aliens.size()) % this.level.aliensPerSpeedStep == 0) {
                this.speed = Math.max(this.level.maxSpeed, this.speed - this.level.speedStepSize);
            }

            if (this.aliens.isEmpty()) {
                final Level nextLevel = this.level.generateNextLevel();

                if (nextLevel == null) {
                    this.setGameOver();
                    return true;
                }

                this.setLevel(nextLevel);
            }

            return true;
        }

        return false;
    }

    private boolean handleAlienShot(final Shot shot) {
        if (shot.getPosition().y + shot.getDimensions().height >= this.board.getDimensions().height) return true;

        if (this.player.intersectsPixels(shot.calculateBounds())) {
            this.setGameOver();
            return true;
        }

        return false;
    }

    private void setGameOver() {
        this.setState(GameState.DONE);
        this.getGameOverComponent().addComponent(new ColorComponent(new Point(), this.getCanvasDimensions(), new Color(0, 0, 0, 160)));

        final Font titleFont = this.getFontAdapter().get("JetBrainsMono-ExtraBold", 25.0f);
        final Font scoreFont = this.getFontAdapter().get("JetBrainsMono-ExtraBold", 15.0f);

        this.getBaseComponent().addComponent(new TextComponent(
                new Point(this.getCanvasDimensions().width / 2, this.getCanvasDimensions().height / 2 - 3),
                "Game Over", Color.WHITE, titleFont, Alignment.BOTTOM_CENTER
        ));

        this.getBaseComponent().addComponent(new TextComponent(
                new Point(this.getCanvasDimensions().width / 2, this.getCanvasDimensions().height / 2 + 3),
                "Score: " + this.board.getScore(), Color.WHITE, scoreFont, Alignment.TOP_CENTER
        ));
    }

    private void handleAlienMovement() {
        if (this.moveRight) {
            if (this.alienBasePosition.x + HORIZONTAL_STEP_SIZE + 11 * 16 >= this.board.getDimensions().width) {
                this.moveAliens(0, VERTICAL_STEP_SIZE);
                this.moveRight = false;
            } else {
                this.moveAliens(HORIZONTAL_STEP_SIZE, 0);
            }
        } else {
            if (this.alienBasePosition.x - HORIZONTAL_STEP_SIZE < 0) {
                this.moveAliens(0, VERTICAL_STEP_SIZE);
                this.moveRight = true;
            } else {
                this.moveAliens(-HORIZONTAL_STEP_SIZE, 0);
            }
        }

        // Check if the lowest alien of each column is above the death line.
        for (final Actor alien : this.aliens) {
            if (!alien.intersectsPixels(this.board.deathZone)) continue;
            this.setGameOver();
            break;
        }
    }

    private void handleSidewaysMovement(final InputKey key) {
        if (key == InputKey.LEFT) {
            this.player.getPosition().x = Math.max(this.player.getPosition().x - HORIZONTAL_STEP_SIZE, 0);
        } else if (key == InputKey.RIGHT) {
            this.player.getPosition().x = Math.min(this.player.getPosition().x + HORIZONTAL_STEP_SIZE, this.board.getDimensions().width - 16);
        }
    }

    @Override
    protected void onKeyDown(@NotNull final InputKey key, final long currentTime) {
        super.onKeyDown(key, currentTime);
        if (!this.getStarted()) return;

        this.handleSidewaysMovement(key);

        if (key == InputKey.SPACE && this.canShoot) {
            this.canShoot = false;
            this.shoot();
        }
    }

    protected void shoot() {
        final Shot shot = new Shot(this.player.getPosition().x + 8, this.player.getPosition().y, Shot.Type.PLAYER);
        this.shots.add(shot);
        this.getBaseComponent().addComponent(shot);
    }

    @Override
    protected void onKeyRepeat(@NotNull final InputKey key, final long currentTime) {
        this.handleSidewaysMovement(key);
    }

}
