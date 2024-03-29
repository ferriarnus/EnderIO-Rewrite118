package com.enderio.machines.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SculkBehaviour;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.state.BlockState;

//TODO better grasp all skulk spreading behaviour.
public class EtherealBlossom extends CropBlock implements SculkBehaviour {
    public static final MapCodec<EtherealBlossom> CODEC = simpleCodec(EtherealBlossom::new);

    public EtherealBlossom(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public MapCodec<? extends CropBlock> codec() {
        return CODEC;
    }

    @Override
    public boolean isRandomlyTicking(BlockState pState) {
        return false;
    }

    @Override
    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {

    }

    @Override
    public int attemptUseCharge(SculkSpreader.ChargeCursor pCursor, LevelAccessor pLevel, BlockPos pPos, RandomSource pRandom, SculkSpreader pSpreader,
        boolean pShouldConvertBlocks) {
        int i = pCursor.getCharge();
        int j = pSpreader.growthSpawnCost();
        if (i != 0 && pRandom.nextInt(pSpreader.chargeDecayRate()) == 0) {
            BlockPos blockpos = pCursor.getPos();
            if (pRandom.nextInt(j) < i) {
                this.growCrops(pLevel, blockpos, pLevel.getBlockState(blockpos));
                return Math.max(0, i - j);
            }
            return pRandom.nextInt(pSpreader.additionalDecayRate()) != 0 ? i : i - getDecayPenalty(pSpreader, blockpos, pPos, i);
        }
        return i;
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        //TODO low light level
        BlockPos blockpos = pPos.below();
        if (pState.getBlock() == this) //Forge: This function is called during world gen and placement, before this block is set, so if we are not 'here' then assume it's the pre-check.
            return pLevel.getBlockState(blockpos).canSustainPlant(pLevel, blockpos, Direction.UP, this);
        return this.mayPlaceOn(pLevel.getBlockState(blockpos), pLevel, blockpos);
    }

    @Override
    protected boolean mayPlaceOn(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return pState.is(Blocks.SCULK);
    }

    private static int getDecayPenalty(SculkSpreader pSpreader, BlockPos pCursorPos, BlockPos pRootPos, int pCharge) {
        int i = pSpreader.noGrowthRadius();
        float f = Mth.square((float)Math.sqrt(pCursorPos.distSqr(pRootPos)) - (float)i);
        int j = Mth.square(24 - i);
        float f1 = Math.min(1.0F, f / (float)j);
        return Math.max(1, (int)((float)pCharge * f1 * 0.5F));
    }

    //TODO copied because of LevelAccessor
    public void growCrops(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
        int i = this.getAge(pState) + this.getBonemealAgeIncrease(pLevel);
        int j = this.getMaxAge();
        if (i > j) {
            i = j;
        }

        pLevel.setBlock(pPos, this.getStateForAge(i), 2);
    }

    //TODO copied because of LevelAccessor
    protected int getBonemealAgeIncrease(LevelAccessor pLevel) {
        return Mth.nextInt(pLevel.getRandom(), 2, 5);
    }

    @Override
    public void growCrops(Level pLevel, BlockPos pPos, BlockState pState) {

    }

    @Override
    protected int getBonemealAgeIncrease(Level pLevel) {
        return 0;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader pLevel, BlockPos pPos, BlockState pState) {
        return false;
    }

    @Override
    public boolean isBonemealSuccess(Level pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
        return false;
    }
}
