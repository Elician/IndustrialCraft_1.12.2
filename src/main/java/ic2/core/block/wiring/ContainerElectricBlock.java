//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ic2.core.block.wiring;

import ic2.core.ContainerFullInv;
import ic2.core.slot.ArmorSlot;
import ic2.core.slot.SlotArmor;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;

public class ContainerElectricBlock extends ContainerFullInv<TileEntityElectricBlock> {
  public ContainerElectricBlock(EntityPlayer player, TileEntityElectricBlock tileEntity1) {
    super(player, tileEntity1, 196);

    for(int col = 0; col < ArmorSlot.getCount(); ++col) {
      this.addSlot(new SlotArmor(player.inventory, ArmorSlot.get(col), 8 + col * 18, 84));
    }

    //this.addSlot(new SlotInvSlot(tileEntity1.chargeSlot, 0, 56, 17));
    //this.addSlot(new SlotInvSlot(tileEntity1.dischargeSlot, 0, 56, 53));
  }

  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("redstoneMode");
    return ret;
  }
}
