package hundun.dontstarve.common;


import java.util.Random;

import com.sun.xml.internal.ws.dump.LoggingDumpTube.Position;

import hundun.dontstarve.DontStarve;
import hundun.dontstarve.block.BlockBerryBush;
import hundun.dontstarve.block.BlockLoader;
import hundun.dontstarve.capability.CapabilityLoader;
import hundun.dontstarve.capability.CapabilityPositionHistory;
import hundun.dontstarve.capability.IPositionHistory;
import hundun.dontstarve.entity.EntityButterfly;
import hundun.dontstarve.item.ItemLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class EventLoader {
	
	public static int ticks;
	public static int secends;
	
	public EventLoader()
    {
        MinecraftForge.EVENT_BUS.register(this);
        ticks=0;
        secends=0;
    }

	
	//spider add drop item
	@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
	public void spiderDrop(LivingDropsEvent event)
	{
	    if (event.getEntity() instanceof EntitySpider)
	    {
	        int radomint=new Random().nextInt(3);
	        if(radomint==0){
	        ItemStack itemStackToDrop = new ItemStack(ItemLoader.spiderGland, 1);
	        event.getDrops().add(new EntityItem(event.getEntity().worldObj, event.getEntity().posX, 
	              event.getEntity().posY, event.getEntity().posZ, itemStackToDrop));
	        }
	    }
	} 
	
	
	//bone meal to grass
	@SubscribeEvent
    public void onPlayerClickGrassBlock(BonemealEvent event)
    {
        if (!event.getWorld().isRemote)
        {
        	
            if (event.getBlock()==Blocks.GRASS.getDefaultState())
            {
            	System.out.println("触发了骨粉对草方块施肥事件");
                EntityLiving entityLiving = new EntityButterfly(event.getWorld());
                BlockPos pos = event.getPos();
                entityLiving.setPositionAndUpdate(pos.getX() , pos.getY()+1, pos.getZ());
                event.getWorld().spawnEntityInWorld(entityLiving);
                return;
            }
        }
    }
	
	
	
	/*
	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event)
	{
	    if(!Minecraft.getMinecraft().isGamePaused() && Minecraft.getMinecraft().theWorld != null)
	    {
	        ticks++;
	        
	        if(ticks>20){
	        	ticks=0;
	        	secends++;
	        	System.out.println("Sever:+1S");
	        }
	        	
	    }
	}
	*/
	
	//when use monsterMeat feed pig,pig booms and gives damage.
	@SubscribeEvent
    public void onEntityInteract(EntityInteract event)
    {
        EntityPlayer player = event.getEntityPlayer();
        if (player.isServerWorld() && event.getTarget() instanceof EntityPig)
        {
            EntityPig pig = (EntityPig) event.getTarget();
            ItemStack stack =player.getHeldItemMainhand();
            if (stack != null && (stack.getItem() == ItemLoader.monsterMeat))
            {
                player.attackEntityFrom((new DamageSource("byPig")).setDifficultyScaled().setExplosion(), 8.0F);
                player.worldObj.createExplosion(pig, pig.posX, pig.posY, pig.posZ, 2.0F, false);
                pig.setDead();
            }
        }
    }
	
	//add historuLocation as a capability
	@SubscribeEvent
    public void onAttachCapabilitiesEntity(AttachCapabilitiesEvent.Entity event)
    {
        if (event.getEntity() instanceof EntityPlayer)
        {
        	ICapabilitySerializable<NBTTagCompound> provider = new CapabilityPositionHistory.ProviderPlayer();
            event.addCapability(new ResourceLocation(DontStarve.MODID + ":" + "position_history"), provider);
        }
    }
	@SubscribeEvent
    public void onPlayerClone(net.minecraftforge.event.entity.player.PlayerEvent.Clone event)
    {
        Capability<IPositionHistory> capability = CapabilityLoader.positionHistory;
        IStorage<IPositionHistory> storage = capability.getStorage();

        if (event.getOriginal().hasCapability(capability, null) && event.getEntityPlayer().hasCapability(capability, null))
        {
            NBTBase nbt = storage.writeNBT(capability, event.getOriginal().getCapability(capability, null), null);
            storage.readNBT(capability, event.getEntityPlayer().getCapability(capability, null), null, nbt);
        }
    }


}
