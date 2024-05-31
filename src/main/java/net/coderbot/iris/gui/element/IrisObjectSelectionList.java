package net.coderbot.iris.gui.element;

import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiSlot;

import java.util.List;

// ObjectSelectionList does not exist in 1.12.2.

public abstract class IrisObjectSelectionList<E extends GuiListExtended.IGuiListEntry> extends GuiSlot {
//	public IrisObjectSelectionList(Minecraft client, int width, int height, int top, int bottom, int left, int right, int itemHeight) {
//		super(client, width, height, top, bottom, itemHeight);
//
//		this.x0 = left;
//		this.x1 = right;
//	}
//
//	@Override
//	protected int getScrollbarPosition() {
//		// Position the scrollbar at the rightmost edge of the screen.
//		// By default, the scrollbar is positioned moderately offset from the center.
//		return width - 6;
//	}
//
//	public void select(int entry) {
//		setSelected(this.getEntry(entry));
//	}
	private final List<E> entries;
	private E selectedEntry;

	public IrisObjectSelectionList(Minecraft client, int width, int height, int top, int bottom, int left, int right, int slotHeight, List<E> entries) {
		super(client, width, height, top, bottom, slotHeight);
		this.entries = entries;
		this.setSlotXBoundsFromLeft(left);
	}

	@Override
	protected int getSize() {
		return entries.size();
	}

	@Override
	protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
		// todo Handle click event
	}

	@Override
	protected boolean isSelected(int slotIndex) {
		return true; // todo
	}

	@Override
	protected void drawBackground() {
		// todo
	}

	@Override
	protected void drawSlot(int slotIndex, int xPos, int yPos, int heightIn, int mouseXIn, int mouseYIn, float partialTicks) {
		// todo
	}

	public void select(int entry) {
		// todo
//		setSelected(this.getEntry(entry));
		selectedEntry = entries.get(entry);
	}

	public void setSelected(E entry) {
		selectedEntry = entry;
	}

	protected void clearEntries() {
		this.entries.clear();
		selectedEntry = null;
	}

	protected void addEntry(E entry) {
		this.entries.add(entry);
	}

	public E getSelected() {
		return selectedEntry;
	}

	public E getEntry(int entry) {
		return entries.get(entry);
	}
}
