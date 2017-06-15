package gregtech.api.items;

import codechicken.lib.render.particle.CustomParticleHandler;
import gregtech.GT_Mod;
import gregtech.api.enums.GT_Values;
import gregtech.api.util.GT_LanguageManager;
import gregtech.api.util.GT_Log;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.LoaderException;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Map;

public abstract class GenericBlock extends Block {

    private String unlocalizedName;

    protected GenericBlock(String name, @Nullable Class<? extends ItemBlock> itemClass, Material material) {
        super(material);

        setUnlocalizedName("gt." + name);
        setRegistryName(name);
        GameRegistry.register(this);

        if (itemClass != null) {
            ItemBlock itemBlock = null;
            try {
                itemBlock = itemClass.getConstructor(Block.class).newInstance(this);
            } catch(ReflectiveOperationException e){
                e.printStackTrace(GT_Log.err);
                throw new LoaderException(e);
            }
            GameRegistry.register(itemBlock, getRegistryName());
        }

        invokeOnClient(() -> {
            for (IBlockState state : this.blockState.getValidStates()) {
                ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), getMetaFromState(state), new ModelResourceLocation(getRegistryName(), getPropertyString(state.getProperties())));
            }
        });
    }

    @Override
    public String getLocalizedName() {
        return GT_LanguageManager.getTranslation(this.getUnlocalizedName() + ".name");
    }

    @Override
    public final Block setUnlocalizedName(String unlocalizedName) {
        this.unlocalizedName = unlocalizedName;
        return this;
    }

    public final String getUnlocalizedNameWithoutPrefix() {
        return unlocalizedName;
    }

    @Override
    public final String getUnlocalizedName() {
        return "block." + unlocalizedName;
    }


    public int damageDropped(IBlockState state) {
        return createStackedBlock(state).getMetadata();
    }

//    @SideOnly(Side.CLIENT)
//    public int getColorMultiplier(IBlockAccess world, BlockPos pos, IBlockState state) {
//        return 0xFFFFFFFF;
//    }

    public static String getPropertyString(Map<IProperty<?>, Comparable<? >> values)
    {
        StringBuilder stringbuilder = new StringBuilder();

        for (Map.Entry< IProperty<?>, Comparable<? >> entry : values.entrySet())
        {
            if (stringbuilder.length() != 0)
            {
                stringbuilder.append(",");
            }

            IProperty<?> property = entry.getKey();
            stringbuilder.append(property.getName());
            stringbuilder.append("=");
            stringbuilder.append(getPropertyName(property, entry.getValue()));
        }

        if (stringbuilder.length() == 0)
        {
            stringbuilder.append("normal");
        }

        return stringbuilder.toString();
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String getPropertyName(IProperty<T> property, Comparable<?> value)
    {
        return property.getName((T)value);
    }

    public void invokeOnClient(Runnable runnable) {
        if(FMLCommonHandler.instance().getSide().isClient()) {
            runnable.run();
        }
    }
}