package dev.volix.rewinside.odyssey.lobby.arcade.spaceinvaders.standalone;

import dev.volix.rewinside.odyssey.lobby.arcade.nbs.song.Song;
import dev.volix.rewinside.odyssey.lobby.arcade.nbs.song.SongParser;
import dev.volix.rewinside.odyssey.lobby.arcade.nbs.song.SongPlayer;
import java.awt.Dimension;
import dev.volix.rewinside.odyssey.common.frames.color.ColorTransformer;
import dev.volix.rewinside.odyssey.common.frames.color.MinecraftColorPalette;
import dev.volix.rewinside.odyssey.lobby.arcade.FrameGameCreator;
import dev.volix.rewinside.odyssey.lobby.arcade.standalone.FrameGameApplication;
import dev.volix.rewinside.odyssey.common.frames.resource.font.FontAdapter;
import dev.volix.rewinside.odyssey.common.frames.resource.font.FontFileAdapter;
import dev.volix.rewinside.odyssey.common.frames.resource.image.ImageAdapter;
import dev.volix.rewinside.odyssey.common.frames.resource.image.ImageFileAdapter;
import dev.volix.rewinside.odyssey.lobby.arcade.spaceinvaders.InvadersGame;
import java.io.File;
import java.nio.file.Paths;
import tv.dev.volix.rewinside.odyssey.lobby.arcade.standalone.SonicSongPlayer;

/**
 * @author Benedikt Wüller
 */
public class SpaceInvaders {

    public static void main(String[] args) {
        final File[] directories = new File[] {
                new File("./resources"),
                new File("../resources")
        };

        File directory = null;
        for (final File file : directories) {
            if (!file.exists()) continue;
            directory = file.getAbsoluteFile();
        }

        if (directory == null) {
            throw new IllegalStateException("Unable to find resources directory.");
        }

        final Song themeSong = new SongParser().parse(Paths.get(directory.getAbsolutePath(), "space-harrier-bgm.nbs").toFile());

        final ColorTransformer transformer = new MinecraftColorPalette();
        final ImageAdapter imageAdapter = new ImageFileAdapter("resources");
        final FontAdapter fontAdapter = new FontFileAdapter("resources");
        final FrameGameCreator creator = () -> {
            final SongPlayer songPlayer = new SonicSongPlayer(themeSong);
            return new InvadersGame(new Dimension(512, 256), transformer, imageAdapter, fontAdapter, songPlayer);
        };

        new FrameGameApplication("Space Invaders", creator).start();
    }

}
