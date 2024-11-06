package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static net.minestom.server.network.NetworkBuffer.*;

/**
 * The packet creates or updates teams
 */
public record TeamsPacket(String teamName, Action action) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public static final int MAX_MEMBERS = 16384;

    public TeamsPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(STRING), switch (reader.read(BYTE)) {
            case 0 -> new CreateTeamAction(reader);
            case 1 -> new RemoveTeamAction();
            case 2 -> new UpdateTeamAction(reader);
            case 3 -> new AddEntitiesToTeamAction(reader);
            case 4 -> new RemoveEntitiesToTeamAction(reader);
            default -> throw new RuntimeException("Unknown action id");
        });
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(STRING, teamName);
        writer.write(BYTE, (byte) action.id());
        writer.write(action);
    }

    @Override
    public @NotNull Collection<Component> components() {
        return this.action instanceof ComponentHolder<?> holder ? holder.components() : List.of();
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new TeamsPacket(
                this.teamName,
                this.action instanceof ComponentHolder<?> holder
                        ? (Action) holder.copyWithOperator(operator)
                        : this.action
        );
    }

    public sealed interface Action extends NetworkBuffer.Writer
            permits CreateTeamAction, RemoveTeamAction, UpdateTeamAction, AddEntitiesToTeamAction, RemoveEntitiesToTeamAction {
        int id();
    }

    public record CreateTeamAction(Component displayName, byte friendlyFlags,
                                   NameTagVisibility nameTagVisibility, CollisionRule collisionRule,
                                   NamedTextColor teamColor, Component teamPrefix, Component teamSuffix,
                                   Collection<String> entities) implements Action, ComponentHolder<CreateTeamAction> {
        public CreateTeamAction {
            entities = List.copyOf(entities);
        }

        public CreateTeamAction(@NotNull NetworkBuffer reader) {
            this(reader.read(COMPONENT), reader.read(BYTE),
                    NameTagVisibility.fromIdentifier(reader.read(STRING)), CollisionRule.fromIdentifier(reader.read(STRING)),
                    NamedTextColor.namedColor(reader.read(VAR_INT)), reader.read(COMPONENT), reader.read(COMPONENT),
                    reader.readCollection(STRING, MAX_MEMBERS));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(COMPONENT, displayName);
            writer.write(BYTE, friendlyFlags);
            writer.write(STRING, nameTagVisibility.getIdentifier());
            writer.write(STRING, collisionRule.getIdentifier());
            writer.write(VAR_INT, AdventurePacketConvertor.getNamedTextColorValue(teamColor));
            writer.write(COMPONENT, teamPrefix);
            writer.write(COMPONENT, teamSuffix);
            writer.writeCollection(STRING, entities);
        }

        @Override
        public int id() {
            return 0;
        }

        @Override
        public @NotNull Collection<Component> components() {
            return List.of(this.displayName, this.teamPrefix, this.teamSuffix);
        }

        @Override
        public @NotNull CreateTeamAction copyWithOperator(@NotNull UnaryOperator<Component> operator) {
            return new CreateTeamAction(
                    operator.apply(this.displayName),
                    this.friendlyFlags,
                    this.nameTagVisibility,
                    this.collisionRule,
                    this.teamColor,
                    operator.apply(this.teamPrefix),
                    operator.apply(this.teamSuffix),
                    entities
            );
        }
    }

    public record RemoveTeamAction() implements Action {
        @Override
        public void write(@NotNull NetworkBuffer writer) {
        }

        @Override
        public int id() {
            return 1;
        }
    }

    public record UpdateTeamAction(Component displayName, byte friendlyFlags,
                                   NameTagVisibility nameTagVisibility, CollisionRule collisionRule,
                                   NamedTextColor teamColor,
                                   Component teamPrefix,
                                   Component teamSuffix) implements Action, ComponentHolder<UpdateTeamAction> {

        public UpdateTeamAction(@NotNull NetworkBuffer reader) {
            this(reader.read(COMPONENT), reader.read(BYTE),
                    NameTagVisibility.fromIdentifier(reader.read(STRING)), CollisionRule.fromIdentifier(reader.read(STRING)),
                    NamedTextColor.namedColor(reader.read(VAR_INT)),
                    reader.read(COMPONENT), reader.read(COMPONENT));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(COMPONENT, displayName);
            writer.write(BYTE, friendlyFlags);
            writer.write(STRING, nameTagVisibility.getIdentifier());
            writer.write(STRING, collisionRule.getIdentifier());
            writer.write(VAR_INT, AdventurePacketConvertor.getNamedTextColorValue(teamColor));
            writer.write(COMPONENT, teamPrefix);
            writer.write(COMPONENT, teamSuffix);
        }

        @Override
        public int id() {
            return 2;
        }

        @Override
        public @NotNull Collection<Component> components() {
            return List.of(this.displayName, this.teamPrefix, this.teamSuffix);
        }

        @Override
        public @NotNull UpdateTeamAction copyWithOperator(@NotNull UnaryOperator<Component> operator) {
            return new UpdateTeamAction(
                    operator.apply(this.displayName),
                    this.friendlyFlags,
                    this.nameTagVisibility,
                    this.collisionRule,
                    this.teamColor,
                    operator.apply(this.teamPrefix),
                    operator.apply(this.teamSuffix)
            );
        }
    }

    public record AddEntitiesToTeamAction(@NotNull Collection<@NotNull String> entities) implements Action {
        public AddEntitiesToTeamAction {
            entities = List.copyOf(entities);
        }

        public AddEntitiesToTeamAction(@NotNull NetworkBuffer reader) {
            this(reader.readCollection(STRING, MAX_MEMBERS));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.writeCollection(STRING, entities);
        }

        @Override
        public int id() {
            return 3;
        }
    }

    public record RemoveEntitiesToTeamAction(@NotNull Collection<@NotNull String> entities) implements Action {
        public RemoveEntitiesToTeamAction {
            entities = List.copyOf(entities);
        }

        public RemoveEntitiesToTeamAction(@NotNull NetworkBuffer reader) {
            this(reader.readCollection(STRING, MAX_MEMBERS));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.writeCollection(STRING, entities);
        }

        @Override
        public int id() {
            return 4;
        }
    }

    /**
     * Gets the identifier of the packet
     *
     * @return the identifier
     */
    @Override
    public int playId() {
        return ServerPacketIdentifier.TEAMS;
    }

    /**
     * An enumeration which representing all visibility states for the name tags
     */
    public enum NameTagVisibility {
        /**
         * The name tag is visible
         */
        ALWAYS("always"),
        /**
         * The name tag is invisible
         */
        NEVER("never"),
        /**
         * Hides the name tag for other teams
         */
        HIDE_FOR_OTHER_TEAMS("hideForOtherTeams"),
        /**
         * Hides the name tag for the own team
         */
        HIDE_FOR_OWN_TEAM("hideForOwnTeam");

        private static final Map<String, NameTagVisibility> BY_NAME = Arrays.stream(values())
                .collect(Collectors.toMap(visibility -> visibility.identifier, Function.identity()));

        /**
         * The identifier for the client
         */
        private final String identifier;

        /**
         * Default constructor
         *
         * @param identifier The client identifier
         */
        NameTagVisibility(@NotNull String identifier) {
            this.identifier = identifier;
        }

        /**
         * Gets the {@link NameTagVisibility} from the client identifier
         *
         * @param identifier The client identifier
         * @return The {@link NameTagVisibility} from the client identifier
         */
        public static @Nullable NameTagVisibility fromIdentifier(@NotNull String identifier) {
            return BY_NAME.get(identifier);
        }

        /**
         * Gets the client identifier
         *
         * @return the identifier
         */
        public @NotNull String getIdentifier() {
            return identifier;
        }

        /**
         * Gets the display name of the visibility
         *
         * @return the display name as {@link Component}
         */
        public @NotNull Component getDisplayName() {
            return Component.translatable("team.visibility." + identifier);
        }
    }

    /**
     * An enumeration which representing all rules for the collision
     */
    public enum CollisionRule {
        /**
         * Can push all objects and can be pushed by all objects
         */
        ALWAYS("always"),
        /**
         * Cannot push an object, but neither can they be pushed
         */
        NEVER("never"),
        /**
         * Can push objects of other teams, but teammates cannot
         */
        PUSH_OTHER_TEAMS("pushOtherTeams"),
        /**
         * Can only push objects of the same team
         */
        PUSH_OWN_TEAM("pushOwnTeam");

        private static final Map<String, CollisionRule> BY_NAME = Arrays.stream(values())
                .collect(Collectors.toMap(rule -> rule.identifier, Function.identity()));

        /**
         * The identifier for the client
         */
        private final String identifier;

        /**
         * Default constructor
         *
         * @param identifier The identifier for the client
         */
        CollisionRule(@NotNull String identifier) {
            this.identifier = identifier;
        }

        /**
         * Gets the {@link CollisionRule} from the client identifier
         *
         * @param identifier The client identifier
         * @return The {@link CollisionRule} from the client identifier
         */
        public static @Nullable CollisionRule fromIdentifier(@NotNull String identifier) {
            return BY_NAME.get(identifier);
        }

        /**
         * Gets the identifier of the rule
         *
         * @return the identifier
         */
        public @NotNull String getIdentifier() {
            return identifier;
        }

        /**
         * Gets the display name of the rule
         *
         * @return the display name as {@link Component}
         */
        public @NotNull Component getDisplayName() {
            return Component.translatable("team.collision." + identifier);
        }
    }
}
