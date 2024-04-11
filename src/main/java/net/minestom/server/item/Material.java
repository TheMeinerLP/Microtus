package net.minestom.server.item;

import net.minestom.server.instance.block.Block;
import net.minestom.server.item.component.ItemComponent;
import net.minestom.server.item.component.ItemComponentMap;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.StaticProtocolObject;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Collection;

/**
 *
 * Component notes
 * todo delete me
 *
 * ItemComponent.DYED_ITEM -> record DyedItemComponent.class
 *
 * itemStack.with(ItemComponent.DYED_ITEM, new DyedItemComponent(Color.RED))
 * itemStack.get(ItemComponent.DYED_ITEM)
 * itemStack.getOrDefault(ItemComponent.DYED_ITEM, new DyedItemComponent(Color.RED))
 *
 * material.prototype() -> some component list?
 *
 *
 *
 * // NEW WIRE FORMAT
 * count | varint
 * material id | varint
 * components | SEE BELOW
 *
 * DataComponentPatch
 * additions | varint
 * removals | varint
 * for each addition
 *   varint | data component id
 *   data component | data component (depends)
 * for each removal
 *   varint | data component id
 *
 *
 */

public sealed interface Material extends StaticProtocolObject, Materials permits MaterialImpl {

    /**
     * Returns the material registry.
     *
     * @return the material registry
     */
    @Contract(pure = true)
    @NotNull Registry.MaterialEntry registry();

    @Override
    default @NotNull NamespaceID namespace() {
        return registry().namespace();
    }

    @Override
    default int id() {
        return registry().id();
    }

    default boolean isBlock() {
        return registry().block() != null;
    }

    default @UnknownNullability Block block() {
        return registry().block();
    }

    default @NotNull ItemComponentMap prototype() {
        return registry().prototype();
    }

    default boolean isArmor() {
        //todo how does armor work nowadays
        return false;
//        return registry().isArmor();
    }

    default boolean hasState() {
        if (this == BOW || this == TRIDENT || this == CROSSBOW || this == SHIELD) {
            return true;
        } else {
            return isFood();
        }
    }

    @Deprecated(forRemoval = true)
    default int maxStackSize() {
        return prototype().getOrDefault(ItemComponent.MAX_STACK_SIZE, 64);
    }

    @Deprecated(forRemoval = true)
    default boolean isFood() {
        return prototype().has(ItemComponent.FOOD);
    }

    static @NotNull Collection<@NotNull Material> values() {
        return MaterialImpl.values();
    }

    static @Nullable Material fromNamespaceId(@NotNull String namespaceID) {
        return MaterialImpl.getSafe(namespaceID);
    }

    static @Nullable Material fromNamespaceId(@NotNull NamespaceID namespaceID) {
        return fromNamespaceId(namespaceID.asString());
    }

    static @Nullable Material fromId(int id) {
        return MaterialImpl.getId(id);
    }
}
