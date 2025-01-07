package com.enderio.machines.common.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Consumer;

public class TreeHelper {

    public static boolean isTree(BlockState blockState) {
        return blockState.is(BlockTags.LOGS) || blockState.is(BlockTags.LEAVES);
    }

    public static Set<BlockPos> getTree(Level level, BlockPos bottom) {
        LinkedList<BlockPos> searchSpace = new LinkedList<>();
        HashSet<BlockPos> tree = new HashSet<>();
        HashSet<BlockPos> searched = new HashSet<>();

        searchSpace.add(bottom);

        Consumer<BlockPos> addIfNotSearched = (blockPos) -> {
            if(!searched.contains(blockPos))
                searchSpace.add(blockPos);
        };

        while(!searchSpace.isEmpty()) {
            BlockPos pos = searchSpace.removeFirst();
            searched.add(pos);
            BlockState state = level.getBlockState(pos);
            if(isTree(state)) {
                tree.add(pos);
                addIfNotSearched.accept(pos.relative(Direction.UP));
                addIfNotSearched.accept(pos.relative(Direction.NORTH));
                addIfNotSearched.accept(pos.relative(Direction.EAST));
                addIfNotSearched.accept(pos.relative(Direction.WEST));
                addIfNotSearched.accept(pos.relative(Direction.SOUTH));
                addIfNotSearched.accept(pos.relative(Direction.DOWN));
            }
        }
        return tree;
    }
}
