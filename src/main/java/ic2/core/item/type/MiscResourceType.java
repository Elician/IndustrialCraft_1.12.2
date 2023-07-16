package ic2.core.item.type;

import ic2.core.block.state.IIdProvider;

public enum MiscResourceType implements IIdProvider {
    ashes(0),
    iridium_ore(1),
    iridium_shard(2),
    matter(3),
    resin(4),
    slag(5),
    iodine(6),
    water_sheet(7),
    lava_sheet(8),

    superconductor_cover(9),
    superconductor(10),
    cooling_core(11),
    gravitation_engine(12),
    magnetron(13),
    vajra_core(14),
    engine_booster(15);

    private final int id;

    MiscResourceType(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name();
    }

    public int getId() {
        return this.id;
    }
}
