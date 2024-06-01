package net.coderbot.iris.mixin.gui;

import net.coderbot.iris.gui.option.IrisVideoSettings;
import net.coderbot.iris.gui.option.ShaderPackSelectionButtonOption;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraft.client.settings.GameSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GuiVideoSettings.class)
public abstract class MixinVideoSettingsScreen extends GuiOptions {
	public MixinVideoSettingsScreen(GameSettings settings) {
		super(null, settings);
	}

	@ModifyArg(
			method = "initGui",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/GuiButton;addButton(Lnet/minecraft/client/gui/GuiButton;)V"
			),
			index = 0
	)
	private GuiButton[] iris$addShaderPackScreenButton(GuiButton[] old) {
		GuiButton[] options = new GuiButton[old.length + 2];
		System.arraycopy(old, 0, options, 0, old.length);
		options[options.length - 2] = new ShaderPackSelectionButtonOption(this, this.mc);
		options[options.length - 1] = IrisVideoSettings.RENDER_DISTANCE.createButton(this.mc.gameSettings, this.width / 2 - 155, this.height / 6 + 24 * (old.length / 2), 150);
		return options;
	}
}
