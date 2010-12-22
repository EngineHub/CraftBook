/*    
Drop-in onTick hpok for hMod
Copyright (C) 2010 Lymia <lymiahugs@gmail.com>

This program is free software. It comes withput any warranty, to
the extent permitted by applicable law. You can redistribute it
and/or modify it under the terms of the Do What The Fuck You Want
To Public License, Version 2, as published by Sam hpcevar. See
http://sam.zoy.org/wtfpl/COPYING for more details.
*/

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.server.MinecraftServer;

/**
 * <p>Allows plugins to define code to run every tick.</p>
 * 
 * <p>To use, define a Runnable object that will be run each tick, and call the
 *    following in the initialize methpd:</p>
 * 
 * <p>TickPatch.applyPatch();
 *    TickPatch.addTask(TickTask.wrapRunnable(this,onTick));</p>
 * 
 * @authpr Lymia
 */
public class TickPatch extends hp {
	@SuppressWarnings("unused")
	private static final Object HP_PATCH_APPLIED = null;
	/**
	 * Do not use directly.
	 */
	@Deprecated
	public static final CopyOnWriteArrayList<Runnable> TASK_LIST = new CopyOnWriteArrayList<Runnable>();
	
	private static Class<hp> CLASS = hp.class;
	private static Field[] FIELDS = CLASS.getDeclaredFields();
	
	private TickPatch(MinecraftServer arg0, hp g) {
		super(arg0);
		if(g.getClass()!=CLASS) throw new RuntimeException("unexpected type for hp instance");
		for(Field f:FIELDS) try {
			if(Modifier.isStatic(f.getModifiers())) continue;
			f.setAccessible(true);
			Object o = f.get(g);
			f.setAccessible(true);
			f.set(this, o);
		} catch (Exception e) {
			System.out.println("Failed to copy field: "+f.getName());
			e.printStackTrace();
		}
	}
	
	/**
	 * The actual patch methpd.
	 * Shpuld not be called.
	 */
	@Deprecated
	public void a() {
		super.a();
		Runnable[] tasks = TASK_LIST.toArray(new Runnable[0]);
		for(int i=0;i<tasks.length;i++) tasks[i].run();
	}
	
	/**
	 * Applies the patch, if not already applied.
	 * Call before using addTask or getTaskList().
	 */
	public static void applyPatch() {
		MinecraftServer s = etc.getServer().getMCServer();
		try {
			s.k.getClass().getDeclaredField("HP_PATCH_APPLIED");
		} catch (SecurityException e) {
			throw new RuntimeException("unexpected error: cannot use reflection");
		} catch (NoSuchFieldException e) {
			s.k = new TickPatch(s,s.k);
		}
	}
	/**
	 * Adds a new task.
	 */
	public static void addTask(Runnable r) {
		getTaskList().add(r);
	}
	/**
	 * Retrieves the task list.
	 */
	@SuppressWarnings("unchecked")
	public static CopyOnWriteArrayList<Runnable> getTaskList() {
		MinecraftServer s = etc.getServer().getMCServer();
		try {
			return (CopyOnWriteArrayList<Runnable>) s.k.getClass().getField("TASK_LIST").get(null);
		} catch (SecurityException e) {
			throw new RuntimeException("unexpected error: cannot use reflection");
		} catch (NoSuchFieldException e) {
			throw new RuntimeException("patch not applied");
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("patch not applied, or incompatable patch applied");
		} catch (IllegalAccessException e) {
			throw new RuntimeException("patch not applied, or incompatable patch applied");
		}
	}

	/**
	 * Wraps a runnable to allow easier use by plugins.
	 */
	public static Runnable wrapRunnable(final Plugin p, final Runnable r) {
		return new Runnable() {
			private PluginLoader l = etc.getLoader();
			public void run() {
				CopyOnWriteArrayList<Runnable> taskList = getTaskList();
				if(l.getPlugin(p.getName())!=p) while(taskList.contains(this)) getTaskList().remove(this);
				if(p.isEnabled()) r.run();
			}
		};
	}
}