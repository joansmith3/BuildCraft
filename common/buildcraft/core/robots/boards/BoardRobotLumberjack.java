/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots.boards;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.robots.AIRobot;
import buildcraft.robots.EntityRobotBase;

public class BoardRobotLumberjack extends RedstoneBoardRobot {

	public BoardRobotLumberjack(EntityRobotBase iRobot, NBTTagCompound nbt) {
		super(iRobot);
	}

	@Override
	public void update() {
		if (robot.getItemInUse() == null) {
			startDelegateAI(new AIRobotFetchAxe(robot));
		} else {
			startDelegateAI(new AIRobotGoToWood(robot));
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotGoToWood) {
			startDelegateAI(new AIRobotCutWood(robot, ((AIRobotGoToWood) ai).woodFound));
		}
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return BoardRobotLumberjackNBT.instance;
	}
}
