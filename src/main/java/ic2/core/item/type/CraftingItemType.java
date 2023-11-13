package ic2.core.item.type;

import ic2.core.block.state.IIdProvider;
import ic2.core.profile.NotClassic;
import ic2.core.profile.NotExperimental;

public enum CraftingItemType implements IIdProvider {
    rubber(0),
    alloy(3),
    iridium(4),
    coil(5),
    electric_motor(6),
    heat_conductor(7),
    copper_boiler(8),
    fuel_rod(9),
    tin_can(10),
    small_power_unit(11),
    power_unit(12),
    carbon_fibre(13),
    carbon_mesh(14),
    carbon_plate(15),
    coal_ball(16),
    coal_block(17),
    coal_chunk(18),
    industrial_diamond(19),
    plant_ball(20),
    bio_chaff(21),
    scrap(23),
    scrap_box(24),
    cf_powder(25),
    raw_crystal_memory(27),
    iron_shaft(29),
    steel_shaft(30),
    wood_rotor_blade(31),
    iron_rotor_blade(32),
    steel_rotor_blade(33),
    carbon_rotor_blade(34),
    steam_turbine_blade(35),
    steam_turbine(36),
    jetpack_attachment_plate(37),
    coin(38),
    bronze_rotor_blade(41),
    bronze_shaft(42),
    carbon_adv_plate(43),
    circuit(1),
    advanced_circuit(2),
    ultimate_circuit(44),
    quantum_circuit(45),
    gravity_circuit(46),
    plasma_circuit(47),
    meta_circuit(48),

    mirror(59),
    photo_element1(60),
    photo_element2(61),
    photo_element3(62),
    photo_element4(63),
    photo_element5(64),
    photo_element6(65);

    private final int id;

    CraftingItemType(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name();
    }

    public int getId() {
        return this.id;
    }
}
