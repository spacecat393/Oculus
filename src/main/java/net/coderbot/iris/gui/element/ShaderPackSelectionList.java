package net.coderbot.iris.gui.element;

import java.util.Collection;
import java.util.function.Function;

//import com.mojang.blaze3d.vertex.PoseStack;

import lombok.Getter;
import lombok.Setter;
import me.jellysquid.mods.sodium.client.util.math.MatrixStack;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
//import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
//import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.FontRenderer;
//import net.minecraft.client.gui.components.ObjectSelectionList;
//import net.minecraft.network.chat.Component;
//import net.minecraft.network.chat.MutableComponent;
//import net.minecraft.network.chat.TextColor;
//import net.minecraft.network.chat.TextComponent;
//import net.minecraft.network.chat.TranslatableComponent;

public class ShaderPackSelectionList extends IrisObjectSelectionList<ShaderPackSelectionList.BaseEntry> {
	private static final ITextComponent PACK_LIST_LABEL = new TextComponentTranslation("pack.iris.list.label").setStyle(new Style().setColor(TextFormatting.GRAY).setItalic(true));

	private final ShaderPackScreen screen;
	@Getter
    private final TopButtonRowEntry topButtonRow;
	@Setter
    @Getter
    private ShaderPackEntry applied = null;

	public ShaderPackSelectionList(ShaderPackScreen screen, Minecraft client, int width, int height, int top, int bottom, int left, int right) {
		super(client, width, height, top, bottom, left, right, 20, null);

		this.screen = screen;
		this.topButtonRow = new TopButtonRowEntry(this, Iris.getIrisConfig().areShadersEnabled());

		refresh();
	}

	@Override
	public int getListWidth() { // getRowWidth()
		return Math.min(308, width - 50);
	}

//	@Override
//	protected int getRowTop(int index) {
//		return super.getRowTop(index) + 2;
//	}

	public void refresh() {
		this.clearEntries();

		Collection<String> names;

		try {
			names = Iris.getShaderpacksDirectoryManager().enumerate();
		} catch (Throwable e) {
			Iris.logger.error("Error reading files while constructing selection UI", e);

			// Not translating this since it's going to be seen very rarely,
			// We're just trying to get more information on a seemingly untraceable bug:
			// - https://github.com/IrisShaders/Iris/issues/785
			this.addLabelEntries(
					new TextComponentString(""),
					new TextComponentString("There was an error reading your shaderpacks directory")
							.setStyle(new Style().setColor(TextFormatting.RED).setBold(true)),
					new TextComponentString(""),
					new TextComponentString("Check your logs for more information."),
					new TextComponentString("Please file an issue report including a log file."),
					new TextComponentString("If you are able to identify the file causing this, " +
											 "please include it in your report as well."),
					new TextComponentString("Note that this might be an issue with folder " +
											 "permissions; ensure those are correct first.")
			);

			return;
		}

		this.addEntry(topButtonRow);

		// Only allow the enable/disable shaders button if the user has
		// added a shader pack. Otherwise, the button will be disabled.
		topButtonRow.allowEnableShadersButton = !names.isEmpty();

		int index = 0;

		for (String name : names) {
			index++;
			addPackEntry(index, name);
		}

		this.addLabelEntries(PACK_LIST_LABEL);
	}

	public void addPackEntry(int index, String name) {
		ShaderPackEntry entry = new ShaderPackEntry(index, this, name);

		Iris.getIrisConfig().getShaderPackName().ifPresent(currentPackName -> {
			if (name.equals(currentPackName)) {
				setSelected(entry);
				setApplied(entry);
			}
		});

		this.addEntry(entry);
	}

	public void addLabelEntries(ITextComponent ... lines) {
		for (ITextComponent text : lines) {
			this.addEntry(new LabelEntry(text));
		}
	}

	public void select(String name) {
		for (int i = 0; i < getItemCount(); i++) {
			BaseEntry entry = getEntry(i);

			if (entry instanceof ShaderPackEntry && ((ShaderPackEntry)entry).packName.equals(name)) {
				setSelected(entry);

				return;
			}
		}
	}

	private int getItemCount() {
		return this.getSize();
	}

	public static abstract class BaseEntry implements GuiListExtended.IGuiListEntry {
		protected FontRenderer fontRenderer;

		protected BaseEntry() {}

		public boolean isMouseOver(int mouseX, int mouseY) {
			return false; // todo
		}
	}

	public static class ShaderPackEntry extends BaseEntry {
		@Getter
        private final String packName;
		private final ShaderPackSelectionList list;
		private final int index;

		public ShaderPackEntry(int index, ShaderPackSelectionList list, String packName) {
			this.packName = packName;
			this.list = list;
			this.index = index;
		}

		public boolean isApplied() {
			return list.getApplied() == this;
		}

		public boolean isSelected() {
			return list.getSelected() == this;
		}

        //@Override
		public void render(MatrixStack poseStack, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
//			Font font = Minecraft.getInstance().font;
			FontRenderer font = this.fontRenderer;
			int color = 0xFFFFFF;
			String name = packName;

			boolean shadersEnabled = list.getTopButtonRow().shadersEnabled;

//			if (font.getStringWidth(new TextComponentString(name).setStyle(new Style().setBold(true)).getFormattedText()) > this.list.getListWidth() - 3) {
//				name = font.plainSubstrByWidth(name, this.list.getListWidth() - 8) + "...";
//			}

			String formattedName = new TextComponentString(name).setStyle(new Style().setBold(true)).getFormattedText();

			if (font.getStringWidth(formattedName) > this.list.getListWidth() - 3) {
				int ellipsisWidth = font.getStringWidth("...");
				StringBuilder builder = new StringBuilder();
				int width = 0;
				for (int i = 0; i < formattedName.length(); i++) {
					char c = formattedName.charAt(i);
					int charWidth = font.getCharWidth(c);
					if (width + charWidth + ellipsisWidth > this.list.getListWidth() - 8) {
						builder.append("...");
						break;
					}
					builder.append(c);
					width += charWidth;
				}
				name = builder.toString();
			}

			ITextComponent text = new TextComponentString(name);

			if (this.isMouseOver(mouseX, mouseY)) {
				text = text.setStyle(new Style().setBold(true));
			}

			if (shadersEnabled && this.isApplied()) {
				color = 0xFFF263;
			}

			if (!shadersEnabled && !this.isMouseOver(mouseX, mouseY)) {
				color = 0xA2A2A2;
			}

			drawCenteredString(poseStack, font, text, (x + entryWidth / 2) - 2, y + (entryHeight - 11) / 2, color);
		}

		//@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			// Only do anything on left-click
			if (button != 0) {
				return false;
			}

			boolean didAnything = false;

			// UX: If shaders are disabled, then clicking a shader in the list will also
			//     enable shaders on apply. Previously, it was not possible to select
			//     a pack when shaders were disabled, but this was a source of confusion
			//     - people did not realize that they needed to enable shaders before
			//     selecting a shader pack.
			if (!list.getTopButtonRow().shadersEnabled) {
				list.getTopButtonRow().setShadersEnabled(true);
				didAnything = true;
			}

			if (!this.isSelected()) {
				this.list.select(this.index);
				didAnything = true;
			}

			return didAnything;
		}
	}

	public static class LabelEntry extends BaseEntry {
		private final ITextComponent label;

		public LabelEntry(ITextComponent label) {
			this.label = label;
		}

		@Override
		public void render(PoseStack poseStack, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			drawCenteredString(poseStack, Minecraft.getInstance().font, label, (x + entryWidth / 2) - 2, y + (entryHeight - 11) / 2, 0xC2C2C2);
		}
	}

	public static class TopButtonRowEntry extends BaseEntry {
		// originally 0x99ceff, replaced with AQUA
		private static final ITextComponent REFRESH_SHADER_PACKS_LABEL = new TextComponentTranslation("options.iris.refreshShaderPacks").setStyle(new Style().setColor(TextFormatting.AQUA));
		private static final ITextComponent NONE_PRESENT_LABEL = new TextComponentTranslation("options.iris.shaders.nonePresent").setStyle(new Style().setColor(TextFormatting.GRAY));
		private static final ITextComponent SHADERS_DISABLED_LABEL = new TextComponentTranslation("options.iris.shaders.disabled");
		private static final ITextComponent SHADERS_ENABLED_LABEL = new TextComponentTranslation("options.iris.shaders.enabled");
		private static final int REFRESH_BUTTON_WIDTH = 18;

		private final ShaderPackSelectionList list;
		private final IrisElementRow buttons = new IrisElementRow();
		private final EnableShadersButtonElement enableDisableButton;
		private final IrisElementRow.Element refreshPacksButton;

		public boolean allowEnableShadersButton = true;
		public boolean shadersEnabled;

		public TopButtonRowEntry(ShaderPackSelectionList list, boolean shadersEnabled) {
			this.list = list;
			this.shadersEnabled = shadersEnabled;
			this.enableDisableButton = new EnableShadersButtonElement(
					getEnableDisableLabel(),
					button -> {
						if (this.allowEnableShadersButton) {
							setShadersEnabled(!this.shadersEnabled);
							GuiUtil.playButtonClickSound();
							return true;
						}

						return false;
					});
			this.refreshPacksButton = new IrisElementRow.IconButtonElement(
					GuiUtil.Icon.REFRESH,
					button -> {
						this.list.refresh();

						GuiUtil.playButtonClickSound();
						return true;
					});
			this.buttons.add(this.enableDisableButton, 0).add(this.refreshPacksButton, REFRESH_BUTTON_WIDTH);
		}

		public void setShadersEnabled(boolean shadersEnabled) {
			this.shadersEnabled = shadersEnabled;
			this.enableDisableButton.text = getEnableDisableLabel().getFormattedText();
			this.list.screen.refreshScreenSwitchButton();
		}

		@Override
		public void render(PoseStack poseStack, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			this.buttons.setWidth(this.enableDisableButton, (entryWidth - 1) - REFRESH_BUTTON_WIDTH);
			this.enableDisableButton.centerX = x + (int)(entryWidth * 0.5);

			this.buttons.render(poseStack, x - 2, y - 3, 18, mouseX, mouseY, tickDelta, hovered);

			if (this.refreshPacksButton.isHovered()) {
				ShaderPackScreen.TOP_LAYER_RENDER_QUEUE.add(() ->
						GuiUtil.drawTextPanel(Minecraft.getInstance().font, poseStack, REFRESH_SHADER_PACKS_LABEL,
								(mouseX - 8) - Minecraft.getInstance().font.width(REFRESH_SHADER_PACKS_LABEL), mouseY - 16));
			}
		}

		private ITextComponent getEnableDisableLabel() {
			return this.allowEnableShadersButton ? this.shadersEnabled ? SHADERS_ENABLED_LABEL : SHADERS_DISABLED_LABEL : NONE_PRESENT_LABEL;
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			return this.buttons.mouseClicked(mouseX, mouseY, button);
		}

		// Renders the label at an offset as to not look misaligned with the rest of the menu
		public static class EnableShadersButtonElement extends IrisElementRow.TextButtonElement {
			private int centerX;

			public EnableShadersButtonElement(ITextComponent text, Function<IrisElementRow.TextButtonElement, Boolean> onClick) {
				super(text, onClick);
			}

			@Override
			public void renderLabel(PoseStack poseStack, int x, int y, int width, int height, int mouseX, int mouseY, float tickDelta, boolean hovered) {
				int textX = this.centerX - (int)(this.font.width(this.text) * 0.5);
				int textY = y + (int)((height - 8) * 0.5);

				this.font.drawShadow(poseStack, this.text, textX, textY, 0xFFFFFF);
			}
		}
	}
}
