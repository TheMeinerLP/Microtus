package net.minestom.server.gametag;

import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public record GameEventTag(Registry.TagEntry registry, int id, List<String> stringValue) implements Tag, GameEventTags {
    private static final AtomicInteger INDEX = new AtomicInteger();
    private static final Registry.Container<Tag> CONTAINER = Registry.createStaticContainer(Registry.Resource.GAMEPLAY_TAGS, GameEventTag::createImpl);
    private static final Function<NamespaceID, Integer> TAG_TO_ID = namespaceID -> Objects.requireNonNull(GameEventTag.getSafe(namespaceID.asString())).id();

    public static final String IDENTIFIER = "minecraft:game_event";

    public GameEventTag(Registry.TagEntry registry) {
        this(registry, INDEX.getAndIncrement(), registry.values());
    }

    private static Tag createImpl(String namespace, Registry.Properties properties) {
        return new GameEventTag(Registry.tag(namespace, properties));
    }

    public static Tag get(String namespace) {
        return CONTAINER.get(namespace);
    }

    public static @NotNull Collection<Tag> values() {
        return CONTAINER.values();
    }

    static Tag getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
    }

    @Override
    public @NotNull Collection<@NotNull NamespaceID> tagValues() {
        var concatinatedTags = stringValue.stream().filter(s -> s.startsWith("#")).toList();
        var result = new ArrayList<>(stringValue.stream().filter(s -> !s.startsWith("#")).map(NamespaceID::from).toList());
        concatinatedTags.stream().map(GameEventTag::getSafe).filter(Objects::nonNull).map(Tag::tagValues).forEach(result::addAll);
        return Collections.unmodifiableCollection(result);
    }

    @Override
    public @NotNull Function<NamespaceID, Integer> getMapper() {
        return TAG_TO_ID;
    }
}