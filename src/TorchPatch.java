/*    
Drop-in redstone torch hooks for hMod
Copyright (C) 2010 Lymia <lymiahugs@gmail.com>

This program is free software. It comes without any warranty, to
the extent permitted by applicable law. You can redistribute it
and/or modify it under the terms of the Do What The Fuck You Want
To Public License, Version 2, as published by Sam Hocevar. See
http://sam.zoy.org/wtfpl/COPYING for more details.
*/

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class TorchPatch extends cl {
	@SuppressWarnings("unused")
	private static final Object CL_PATCH_APPLIED = null;
	/**
	 * Do not use directly.
	 */
	@Deprecated
	public static final CopyOnWriteArrayList<?> LISTENERS = new CopyOnWriteArrayList<Object>();
	
	private static Class<cl> CLASS = cl.class;
	private static Field[] FIELDS;
	
	private static int TYPE_ON = Block.Type.RedstoneTorchOn.getType();
	private static int TYPE_OFF = Block.Type.RedstoneTorchOff.getType();
	
	private boolean isOn;
	
	static {
		ArrayList<Field> fields = new ArrayList<Field>();
		for(Class<?> c = CLASS;;c=c.getSuperclass()) {
			if(c==Object.class) break;
			fields.addAll(Arrays.asList(c.getDeclaredFields()));
		}
		FIELDS = fields.toArray(new Field[0]);
	}
	
	public TorchPatch(int texture, boolean isOn, gc old) {
		super(nullId(isOn), texture, isOn);
		
		for(Field f:FIELDS) try {
			if(Modifier.isStatic(f.getModifiers())||Modifier.isFinal(f.getModifiers())) continue;
			f.setAccessible(true);
			Object o = f.get(old);
			f.setAccessible(true);
			f.set(this, o);
		} catch (Exception e) {
			System.out.println("Failed to copy field: "+f.getName());
			e.printStackTrace();
		}
        
        this.isOn = isOn;
	}
	private static int nullId(boolean isOn) {
		int id = isOn?TYPE_ON:TYPE_OFF;
		gc.m[id] = null;
		return id;
	}

    /**
     * Patch method.
     * Should not be called.
     */
    @Deprecated
    public void a(eq world, int x, int y, int z, Random r) {
        Object[] tasks = LISTENERS.toArray();
        for(int i=0;i<tasks.length;i++)
            try {
                Method m = tasks[i].getClass().getMethod("onRedstoneTorchUpdate", Block.class, Boolean.TYPE);
                m.setAccessible(true);
                Boolean b = (Boolean)m.invoke(tasks[i],new Block(bh,x,y,z),isOn);
                if(b) return;
            } catch (Exception e) {throw new RuntimeException("invoke failed",e);}
        super.a(world,x,y,z,r);
    }
    /**
     * Patch method.
     * Should not be called.
     */
    @Deprecated
    public void b(eq world, int x, int y, int z, int unk) {
        Object[] tasks = LISTENERS.toArray();
        for(int i=0;i<tasks.length;i++)
            try {
                Method m = tasks[i].getClass().getMethod("onRedstoneTorchNeighborChange", Block.class);
                m.setAccessible(true);
                m.invoke(tasks[i],new Block(bh,x,y,z));
            } catch (Exception e) {throw new RuntimeException("invoke failed",e);}
        super.b(world,x,y,z,unk);
    }
	/**
	 * Patch method.
	 * Should not be called.
	 */
	@Deprecated
	public void e(eq world, int x, int y, int z) {
		Object[] tasks = LISTENERS.toArray();
		for(int i=0;i<tasks.length;i++)
			try {
				Method m = tasks[i].getClass().getMethod("onRedstoneTorchAdded", Block.class);
				m.setAccessible(true);
				m.invoke(tasks[i],new Block(bh,x,y,z));
			} catch (Exception e) {throw new RuntimeException("invoke failed",e);}
		super.e(world,x,y,z);
	}
	
	/**
	 * Applies the patch, if not already applied.
	 * Call before using addTask or getTaskList().
	 */
	public static void applyPatch() {
		try {
			gc.m[TYPE_ON].getClass().getDeclaredField("CL_PATCH_APPLIED");
		} catch (SecurityException e) {
			throw new RuntimeException("unexpected error: cannot use reflection");
		} catch (NoSuchFieldException e) {
			new TorchPatch(gc.m[TYPE_ON].bg,true,gc.m[TYPE_ON]);
			new TorchPatch(gc.m[TYPE_OFF].bg,false,gc.m[TYPE_OFF]);
		}
	}
	/**
	 * Adds a new task.
	 */
	public static void addListener(ExtensionListener r) {
		getListenerList().add(r);
	}
	/**
	 * Retrieves the task list.
	 */
	@SuppressWarnings("unchecked")
	public static CopyOnWriteArrayList<ExtensionListener> getListenerList() {
		try {
			return (CopyOnWriteArrayList<ExtensionListener>) gc.m[TYPE_ON].getClass().getField("LISTENERS").get(null);
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
	 * Wraps a HookExtension to allow easier use by plugins.
	 */
	public static ExtensionListener wrapListener(final Plugin p, final ExtensionListener r) {
		return new ExtensionListener() {
			private PluginLoader l = etc.getLoader();
			private long lastCheck = 0;
			public void onRedstoneTorchAdded(Block b) {
				if(etc.getServer().getTime()!=lastCheck) {
					if(l.getPlugin(p.getName())!=p) {
						CopyOnWriteArrayList<ExtensionListener> taskList = getListenerList();
						while(taskList.contains(this)) taskList.remove(this);
						return;
					}
				    lastCheck = etc.getServer().getTime();
				}
				if(p.isEnabled()) r.onRedstoneTorchAdded(b);
			}
            public boolean onRedstoneTorchUpdate(Block b, boolean isOn) {
                if(etc.getServer().getTime()!=lastCheck) {
                    if(l.getPlugin(p.getName())!=p) {
                        CopyOnWriteArrayList<ExtensionListener> taskList = getListenerList();
                        while(taskList.contains(this)) taskList.remove(this);
                        return false;
                    }
                    lastCheck = etc.getServer().getTime();
                }
                if(p.isEnabled()) return r.onRedstoneTorchUpdate(b, isOn);
                return false;
            }
            public void onRedstoneTorchNeighborChange(Block b) {
                if(etc.getServer().getTime()!=lastCheck) {
                    if(l.getPlugin(p.getName())!=p) {
                        CopyOnWriteArrayList<ExtensionListener> taskList = getListenerList();
                        while(taskList.contains(this)) taskList.remove(this);
                        return;
                    }
                    lastCheck = etc.getServer().getTime();
                }
                if(p.isEnabled()) r.onRedstoneTorchNeighborChange(b);
            }
		};
	}
	
	public static interface ExtensionListener {
		public void onRedstoneTorchAdded(Block b);
		public boolean onRedstoneTorchUpdate(Block b, boolean isOn);
		public void onRedstoneTorchNeighborChange(Block b);
	}
}
