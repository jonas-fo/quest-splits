/*
 * Copyright (c) 2022, Jónas
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.questsplits.overlays;

import com.questsplits.QuestSplitsPlugin;

import java.awt.*;
import javax.inject.Inject;

import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;

public class QuestSplitsOverlay extends OverlayPanel
{
	private final QuestSplitsPlugin plugin;
	private Widget[] textFields;

	@Inject
	public QuestSplitsOverlay(QuestSplitsPlugin plugin, Widget[] textFields)
	{
		this.plugin = plugin;
		setMovable(true);
		setPriority(OverlayPriority.HIGHEST);
	}

	public Widget[] getTextFields(){
		return this.textFields;
	}

	public void setTextFields(Widget[] textFields) {
		this.textFields = textFields;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (textFields == null || !plugin.getConfig().showSplits())
		{
			return super.render(graphics);
		}

		panelComponent.setPreferredSize(new Dimension(300, 100));
		panelComponent.setPreferredLocation(new Point(0,0));
		for(String time : plugin.getTimes().keySet())
		{
			panelComponent.getChildren().add(LineComponent.builder().left(time).right(plugin.getTimes().get(time) + " " + plugin.getBestTimes().get(time)).build());
		}

		return super.render(graphics);
	}
}