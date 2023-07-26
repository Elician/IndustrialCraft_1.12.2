package ic2.core.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntitySpectralArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EntityTechArrow extends EntitySpectralArrow {
    private boolean explosive;
    private float explosionPower = 3.0f;

    public EntityTechArrow(World world) {
        super(world);
    }

    public EntityTechArrow(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public EntityTechArrow(World world, EntityLivingBase shooter) {
        super(world, shooter);
    }

    public void setExplosive(boolean value) {
        explosive = value;
    }

    public void setExplosionPower(float value) {
        explosionPower = value;
    }

    @Override
    protected ItemStack getArrowStack() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void doBlockCollisions() {
        super.doBlockCollisions();

        if ((arrowShake > 0) && explosive) {
            world.func_72885_a(null, posX, posY, posZ, explosionPower, false, false);
        }
    }
}