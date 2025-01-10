package com.enderio.machines.common.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class TreeHelper {

    public static boolean isTree(BlockState blockState) {
        return blockState.is(BlockTags.LOGS) || blockState.is(BlockTags.LEAVES);
    }

    public static Set<BlockPos> getTree(Level level, BlockPos bottom) {
        LinkedList<BlockPos> searchSpace = new LinkedList<>();
        HashSet<BlockPos> tree = new HashSet<>();
        HashSet<BlockPos> searched = new HashSet<>();

        searchSpace.add(bottom);

        while(!searchSpace.isEmpty()) {
            BlockPos pos = searchSpace.removeFirst();
            searched.add(pos);
            BlockState state = level.getBlockState(pos);
            if(isTree(state)) {
                tree.add(pos);
                BlockPos.betweenClosed(pos.offset(1, 1, 1), pos.offset(-1, -1, -1))
                    .forEach(next -> {
                        if(searched.contains(next)) return;
                        searched.add(next);
                        searchSpace.add(next);
                    });
            }
        }
        return tree;
    }
}
