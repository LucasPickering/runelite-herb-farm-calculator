package me.lucaspickering;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class HerbFarmCalculatorPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(HerbFarmCalculatorPlugin.class);
		RuneLite.main(args);
	}
}
