package buildcraft.core.robots.boards;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.boards.IBoardParameter;
import buildcraft.api.boards.IBoardParameterStack;
import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.core.inventory.filters.ArrayStackFilter;
import buildcraft.core.inventory.filters.IStackFilter;
import buildcraft.core.robots.AIRobotGoToDock;
import buildcraft.robots.AIRobot;
import buildcraft.robots.DockingStation;
import buildcraft.robots.EntityRobotBase;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TravelingItem;

public class BoardRobotPicker extends RedstoneBoardRobot {

	public static Set<Integer> targettedItems = new HashSet<Integer>();

	private SafeTimeTracker scanTracker = new SafeTimeTracker(40, 10);

	private NBTTagCompound data;
	private RedstoneBoardNBT board;
	private IBoardParameter[] params;
	private int range;
	private IStackFilter stackFilter;

	public BoardRobotPicker(EntityRobotBase robot, NBTTagCompound nbt) {
		super(robot);
		data = nbt;

		board = RedstoneBoardRegistry.instance.getRedstoneBoard(nbt);
		params = board.getParameters(nbt);

		range = nbt.getInteger("range");

		ItemStack[] stacks = new ItemStack[params.length];

		for (int i = 0; i < stacks.length; ++i) {
			IBoardParameterStack pStak = (IBoardParameterStack) params[i];
			stacks[i] = pStak.getStack();
		}

		if (stacks.length > 0) {
			stackFilter = new ArrayStackFilter(stacks);
		} else {
			stackFilter = null;
		}
	}

	@Override
	public void update() {
		if (scanTracker.markTimeIfDelay(robot.worldObj)) {
			startDelegateAI(new AIRobotFetchItem(robot, range, stackFilter));
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotFetchItem) {
			if (((AIRobotFetchItem) ai).target != null) {
				// if we could get an item, let's try to get another one
				startDelegateAI(new AIRobotFetchItem(robot, range, stackFilter));
			} else {
				// otherwise, let's return to base
				startDelegateAI(new AIRobotGoToDock(robot, robot.getMainDockingStation()));
			}
		} else if (ai instanceof AIRobotGoToDock) {
			emptyContainerInStation();
		}
	}

	private void emptyContainerInStation() {
		DockingStation station = robot.getCurrentDockingStation();

		TileGenericPipe pipe = (TileGenericPipe) robot.worldObj
				.getTileEntity(station.pipe.xCoord, station.pipe.yCoord, station.pipe.zCoord);

		if (pipe != null && pipe.pipe.transport instanceof PipeTransportItems) {
			for (int i = 0; i < robot.getSizeInventory(); ++i) {
				if (robot.getStackInSlot(i) != null) {
					float cx = station.pipe.xCoord + 0.5F + 0.2F * station.side.offsetX;
					float cy = station.pipe.yCoord + 0.5F + 0.2F * station.side.offsetY;
					float cz = station.pipe.zCoord + 0.5F + 0.2F * station.side.offsetZ;

					TravelingItem item = TravelingItem.make(cx, cy,
							cz, robot.getStackInSlot(i));

					((PipeTransportItems) pipe.pipe.transport).injectItem(item, station.side.getOpposite());

					robot.setInventorySlotContents(i, null);

					break;
				}
			}
		}
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return BoardRobotPickerNBT.instance;
	}
}
