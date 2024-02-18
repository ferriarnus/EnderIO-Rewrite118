package com.enderio.machines.common.block;

import com.enderio.base.common.tag.EIOTags;
import com.enderio.core.common.blockentity.EnderBlockEntity;
import com.enderio.machines.common.blockentity.base.MachineBlockEntity;
import com.enderio.regilite.holder.RegiliteBlockEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class MachineBlock extends BaseEntityBlock {
    public static final Codec<Supplier<BlockEntityType<? extends MachineBlockEntity>>> BLOCK_ENTITY_TYPE_CODEC = BuiltInRegistries.BLOCK_ENTITY_TYPE
        .holderByNameCodec()
        .flatXmap(blockEntityTypeHolder -> DataResult.success(() -> (BlockEntityType<? extends MachineBlockEntity>) blockEntityTypeHolder.value()),
            sup -> DataResult.success(sup.get().builtInRegistryHolder()));

    private static final MapCodec<MachineBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        BLOCK_ENTITY_TYPE_CODEC.fieldOf("block_entity_type").forGetter(output -> output.blockEntityType),
        propertiesCodec()
    ).apply(instance, MachineBlock::new));

    private final Supplier<BlockEntityType<? extends MachineBlockEntity>> blockEntityType;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private MachineBlock(Supplier<BlockEntityType<? extends MachineBlockEntity>> blockEntityType, Properties properties) {
        super(properties);

        this.blockEntityType = blockEntityType;
        BlockState any = this.getStateDefinition().any();
        this.registerDefaultState(any.hasProperty(FACING) ? any.setValue(FACING, Direction.NORTH) : any);
    }

    public MachineBlock(RegiliteBlockEntity<? extends MachineBlockEntity> blockEntityType, Properties properties) {
        super(properties);
        this.blockEntityType = blockEntityType::get;
        BlockState any = this.getStateDefinition().any();
        this.registerDefaultState(any.hasProperty(FACING) ? any.setValue(FACING, Direction.NORTH) : any);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected MapCodec<? extends MachineBlock> codec() {
        return CODEC;
    }

    @SuppressWarnings("deprecation")
    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, blockEntityType.get(), MachineBlockEntity::tick);
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide() || hand != InteractionHand.MAIN_HAND){
            return InteractionResult.SUCCESS;
        }

        BlockEntity entity = level.getBlockEntity(pos);
        if (!(entity instanceof MachineBlockEntity machineBlockEntity)) { // This also covers nulls
            return InteractionResult.PASS;
        }

        if (player.getItemInHand(hand).is(EIOTags.Items.WRENCH)) {
            InteractionResult res = machineBlockEntity.onWrenched(player, hit.getDirection());
            if (res != InteractionResult.PASS) {
                return res;
            }
        }

        //pass on the use command to corresponding block entity.
        InteractionResult result = machineBlockEntity.onBlockEntityUsed(state, level, pos, player, hand,hit);
        if (result != InteractionResult.CONSUME) {
            if (!machineBlockEntity.canOpenMenu()) {
                return InteractionResult.PASS;
            }

            MenuProvider menuprovider = this.getMenuProvider(state, level, pos);
            if (menuprovider != null && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(menuprovider, buf -> buf.writeBlockPos(pos));
            }
        }
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return blockEntityType.get().create(pPos, pState);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        if (level.getBlockEntity(pos) instanceof MachineBlockEntity machineBlock) {
            return machineBlock.supportsRedstoneControl();
        }
        return super.canConnectRedstone(state, level, pos, direction);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        var blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof MachineBlockEntity machineBlock) {
            return machineBlock.getLightEmission();
        }
        return super.getLightEmission(state, level, pos);
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if (be instanceof EnderBlockEntity enderBlockEntity) {
            enderBlockEntity.copyFromStack(pStack);
        }
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
    }
}
