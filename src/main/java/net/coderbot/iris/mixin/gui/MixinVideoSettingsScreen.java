package net.coderbot.iris.mixin.gui;

import net.coderbot.iris.gui.option.IrisVideoSettings;
import net.coderbot.iris.gui.option.ShaderPackSelectionButtonOption;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(VideoSettingsScreen.class)
public abstract class MixinVideoSettingsScreen extends Screen {
	protected MixinVideoSettingsScreen(Component title) {
		super(title);
	}

	@ModifyArg(
			method = "init",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/components/OptionsList;addSmall([Lnet/minecraft/client/OptionInstance;)V"
			),
			index = 0
	)
	private OptionInstance<?>[] iris$addShaderPackScreenButton(OptionInstance<?>[] $$0) {
		OptionInstance[] options = new OptionInstance[$$0.length + 1];
		System.arraycopy($$0, 0, options, 0, $$0.length);
		options[options.length - 1] = new OptionInstance<>("options.iris.shaderPackSelection", OptionInstance.noTooltip(), null, OptionInstance.BOOLEAN_VALUES, true, (parent) -> minecraft.setScreen(new ShaderPackScreen(this)));
		return options;
	}
}
