package net.minestom.server.network.player;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static net.minestom.server.network.NetworkBuffer.STRING;

public record GameProfile(@NotNull UUID uuid, @NotNull String name,
                          @NotNull List<@NotNull Property> properties) implements NetworkBuffer.Writer {
    public static final int MAX_PROPERTIES = 1024;
    private static final Pattern SHORT_UUID_TO_LONG = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");

    public GameProfile {
        if (name.isBlank())
            throw new IllegalArgumentException("Name cannot be blank");
        if (name.length() > 16)
            throw new IllegalArgumentException("Name length cannot be greater than 16 characters");
        properties = List.copyOf(properties);
    }

    public GameProfile(@NotNull NetworkBuffer reader) {
        this(reader.read(NetworkBuffer.UUID), reader.read(STRING), reader.readCollection(Property::new, MAX_PROPERTIES));
    }

    public GameProfile(@NotNull JsonObject object) {
        this(UUID.fromString(SHORT_UUID_TO_LONG.matcher(object.get("id").getAsString()).replaceAll("$1-$2-$3-$4-$5")), object.get("name").getAsString(), GameProfile.readProperties(object));
    }

    private static @NotNull List<@NotNull Property> readProperties(@NotNull JsonObject gameProfile) {
        return gameProfile.get("properties").getAsJsonArray().asList().stream().map(JsonElement::getAsJsonObject).map(GameProfile.Property::new).toList();
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.UUID, uuid);
        writer.write(STRING, name);
        writer.writeCollection(properties);
    }

    public record Property(@NotNull String name, @NotNull String value,
                           @Nullable String signature) implements NetworkBuffer.Writer {
        public Property(@NotNull String name, @NotNull String value) {
            this(name, value, null);
        }

        public Property(@NotNull NetworkBuffer reader) {
            this(reader.read(STRING), reader.read(STRING),
                    reader.readOptional(STRING));
        }

        public Property(@NotNull JsonObject object) {
            this(object.get("name").getAsString(), object.get("value").getAsString(), object.get("signature").getAsString());
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(STRING, name);
            writer.write(STRING, value);
            writer.writeOptional(STRING, signature);
        }
    }
}
