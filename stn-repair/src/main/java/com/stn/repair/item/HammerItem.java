package com.stn.repair.item;

import com.stn.core.STNCore;
import com.stn.core.api.IDurabilityProvider;
import com.stn.fortifications.block.BarbedWireBlock;
import com.stn.fortifications.block.ElectricFenceBlock;
import com.stn.fortifications.block.SpikeBlock;
import com.stn.fortifications.durability.BlockDurabilityManager;
import com.stn.fortifications.registry.STNBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Hammer tool for repairing defensive blocks and structures.
 * - Simple blocks (dirt, wood, stone): Uses hammer durability only
 * - Complex mod blocks (spikes): Requires specific repair materials
 *
 * Hammer tiers determine durability and repair efficiency.
 */
public class HammerItem extends Item {

    private final int repairAmount;
    private final HammerTier tier;

    public enum HammerTier {
        WOOD(59, 10, "Wooden"),
        STONE(131, 15, "Stone"),
        IRON(250, 25, "Iron"),
        DIAMOND(1561, 40, "Diamond"),
        NETHERITE(2031, 60, "Netherite");

        private final int durability;
        private final int repairAmount;
        private final String displayName;

        HammerTier(int durability, int repairAmount, String displayName) {
            this.durability = durability;
            this.repairAmount = repairAmount;
            this.displayName = displayName;
        }

        public int getDurability() { return durability; }
        public int getRepairAmount() { return repairAmount; }
        public String getDisplayName() { return displayName; }
    }

    public HammerItem(HammerTier tier, Settings settings) {
        super(settings.maxDamage(tier.getDurability()).maxCount(1));
        this.tier = tier;
        this.repairAmount = tier.getRepairAmount();
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        PlayerEntity player = context.getPlayer();

        if (player == null || world.isClient()) {
            return ActionResult.PASS;
        }

        Block block = state.getBlock();

        // Check for mod's special blocks that require materials
        SpecialBlockRepair specialRepair = getSpecialBlockRepair(block, state);
        if (specialRepair != null) {
            return handleSpecialBlockRepair(context, player, world, pos, state, specialRepair);
        }

        // Check for tracked blocks (player-placed) via durability provider
        if (world instanceof ServerWorld serverWorld) {
            IDurabilityProvider provider = STNCore.getDurabilityProvider();

            if (provider == null) {
                player.sendMessage(Text.literal("Durability system not available!").formatted(Formatting.RED), true);
                return ActionResult.FAIL;
            }

            // For tracked blocks, we need to access BlockDurabilityManager directly
            // since the provider interface has limitations
            BlockDurabilityManager manager = BlockDurabilityManager.get(serverWorld);

            // Check if block can be tracked (has durability)
            int maxDurability = manager.getMaxDurabilityAt(world, pos);
            if (maxDurability > 0) {
                // Auto-track the block if not already tracked
                if (!manager.isTracked(world, pos)) {
                    manager.registerBlock(pos, state);
                }
                return handleTrackedBlockRepair(context, player, serverWorld, pos, state, manager);
            }
        }

        // Not a repairable block
        return ActionResult.PASS;
    }

    /**
     * Handle repair for tracked blocks (uses hammer durability only, except steel)
     */
    private ActionResult handleTrackedBlockRepair(ItemUsageContext context, PlayerEntity player,
            ServerWorld world, BlockPos pos, BlockState state, BlockDurabilityManager manager) {

        float durabilityPercent = manager.getDurabilityPercent(world, pos);
        Block block = state.getBlock();

        // Check if needs repair
        if (durabilityPercent >= 1.0f) {
            player.sendMessage(Text.literal("This block is already fully repaired!").formatted(Formatting.YELLOW), true);
            return ActionResult.FAIL;
        }

        // Steel blocks require steel ingots to repair (if steel ingots exist)
        // For now, we'll skip this requirement as STN may not have steel ingots
        /*
        if (block == STNBlocks.STEEL_BLOCK) {
            ItemStack material = findMaterial(player, ModItems.STEEL_INGOT);
            if (material.isEmpty() && !player.isCreative()) {
                player.sendMessage(Text.literal("Need Steel Ingots to repair!").formatted(Formatting.RED), true);
                return ActionResult.FAIL;
            }

            // Consume material
            if (!player.isCreative()) {
                material.decrement(1);
            }
        }
        */

        // Repair the block
        int repaired = manager.repairBlockAmount(world, pos, repairAmount);

        if (repaired > 0) {
            // Damage the hammer
            ItemStack hammer = context.getStack();
            if (!player.isCreative()) {
                hammer.damage(1, player, context.getHand() == net.minecraft.util.Hand.MAIN_HAND
                    ? net.minecraft.entity.EquipmentSlot.MAINHAND
                    : net.minecraft.entity.EquipmentSlot.OFFHAND);
            }

            // Effects
            playRepairEffects(world, pos);

            return ActionResult.SUCCESS;
        }

        return ActionResult.FAIL;
    }

    /**
     * Handle repair for special mod blocks (requires materials)
     */
    private ActionResult handleSpecialBlockRepair(ItemUsageContext context, PlayerEntity player,
            World world, BlockPos pos, BlockState state, SpecialBlockRepair repair) {

        // Check if needs repair
        if (repair.currentDamage == 0) {
            player.sendMessage(Text.literal("This block is already fully repaired!").formatted(Formatting.YELLOW), true);
            return ActionResult.FAIL;
        }

        // Check for materials
        ItemStack material = findMaterial(player, repair.repairItem);
        if (material.isEmpty() && !player.isCreative()) {
            String itemName = getItemDisplayName(repair.repairItem);
            player.sendMessage(Text.literal("Need " + itemName + " to repair!").formatted(Formatting.RED), true);
            return ActionResult.FAIL;
        }

        // Consume material
        if (!player.isCreative()) {
            material.decrement(repair.materialsRequired);
        }

        // Repair the block
        int newDamage = Math.max(0, repair.currentDamage - repair.repairAmount);
        world.setBlockState(pos, state.with(repair.damageProperty, newDamage));

        // Damage hammer (less than normal since materials are used)
        ItemStack hammer = context.getStack();
        if (!player.isCreative() && world.random.nextInt(3) == 0) {
            hammer.damage(1, player, context.getHand() == net.minecraft.util.Hand.MAIN_HAND
                ? net.minecraft.entity.EquipmentSlot.MAINHAND
                : net.minecraft.entity.EquipmentSlot.OFFHAND);
        }

        // Effects
        playRepairEffects(world, pos);

        return ActionResult.SUCCESS;
    }

    private void playRepairEffects(World world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 0.5f, 1.2f);

        if (world instanceof ServerWorld serverWorld) {
            for (int i = 0; i < 8; i++) {
                serverWorld.spawnParticles(
                    ParticleTypes.CRIT,
                    pos.getX() + 0.5 + (world.random.nextDouble() - 0.5) * 0.5,
                    pos.getY() + 0.5 + (world.random.nextDouble() - 0.5) * 0.5,
                    pos.getZ() + 0.5 + (world.random.nextDouble() - 0.5) * 0.5,
                    1, 0.1, 0.1, 0.1, 0.05
                );
            }
        }
    }

    /**
     * Get repair info for special mod blocks that require materials
     */
    private SpecialBlockRepair getSpecialBlockRepair(Block block, BlockState state) {
        // Check for spike blocks
        if (state.contains(SpikeBlock.DAMAGE_COUNT)) {
            int damage = state.get(SpikeBlock.DAMAGE_COUNT);

            // Bamboo Spikes - repair with sticks (NEW in STN)
            if (block == STNBlocks.BAMBOO_SPIKES) {
                return new SpecialBlockRepair(damage, Items.STICK, 1, 3, SpikeBlock.DAMAGE_COUNT);
            }

            // Wooden Spikes - repair with sticks
            if (block == STNBlocks.WOODEN_SPIKES) {
                return new SpecialBlockRepair(damage, Items.STICK, 1, 3, SpikeBlock.DAMAGE_COUNT);
            }

            // Iron Spikes - repair with iron nuggets
            if (block == STNBlocks.IRON_SPIKES) {
                return new SpecialBlockRepair(damage, Items.IRON_NUGGET, 1, 3, SpikeBlock.DAMAGE_COUNT);
            }

            // Reinforced Spikes - repair with iron nuggets (more)
            if (block == STNBlocks.REINFORCED_SPIKES) {
                return new SpecialBlockRepair(damage, Items.IRON_NUGGET, 2, 3, SpikeBlock.DAMAGE_COUNT);
            }
        }

        // Check for barbed wire
        if (block == STNBlocks.BARBED_WIRE && state.contains(BarbedWireBlock.DAMAGE_COUNT)) {
            int damage = state.get(BarbedWireBlock.DAMAGE_COUNT);
            return new SpecialBlockRepair(damage, Items.IRON_NUGGET, 1, 3, BarbedWireBlock.DAMAGE_COUNT);
        }

        // Check for electric fence
        if (block == STNBlocks.ELECTRIC_FENCE && state.contains(ElectricFenceBlock.DAMAGE_COUNT)) {
            int damage = state.get(ElectricFenceBlock.DAMAGE_COUNT);
            return new SpecialBlockRepair(damage, Items.IRON_NUGGET, 1, 3, ElectricFenceBlock.DAMAGE_COUNT);
        }

        return null;
    }

    private ItemStack findMaterial(PlayerEntity player, Item repairItem) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(repairItem) && stack.getCount() > 0) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private String getItemDisplayName(Item item) {
        if (item == Items.STICK) return "Sticks";
        if (item == Items.IRON_NUGGET) return "Iron Nuggets";
        if (item == Items.IRON_INGOT) return "Iron Ingots";
        return item.getName().getString();
    }

    public HammerTier getTier() {
        return tier;
    }

    private record SpecialBlockRepair(
        int currentDamage,
        Item repairItem,
        int materialsRequired,
        int repairAmount,
        net.minecraft.state.property.IntProperty damageProperty
    ) {}
}
