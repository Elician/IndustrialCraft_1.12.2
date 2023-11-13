//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ic2.core.block.generator.tileentity;

import ic2.core.ContainerFullInv;
import ic2.core.slot.ArmorSlot;
import ic2.core.slot.SlotArmor;

import ic2.core.slot.SlotInvSlot;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;

public class ContainerSolarPanel extends ContainerFullInv<TileEntityBaseSolarGenerator> {
  public ContainerSolarPanel(EntityPlayer player, TileEntityBaseSolarGenerator te1) {
    super(player, te1, 196);

    for (int col = 0; col < ArmorSlot.getCount(); ++col) {
      this.addSlot(new SlotArmor(player.inventory, ArmorSlot.get(col), 8 + col * 18, 84));
    }

    this.addSlot(new SlotInvSlot(te1.chargeSlot, 0, 98, 84));
  }

  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("skyLight");
    ret.add("production");
    return ret;
  }
}
