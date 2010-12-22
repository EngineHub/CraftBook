/*    
Craftbook 
Copyright (C) 2010 Lymia <lymiahugs@gmail.com>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class TorchPatch extends cu {
	/**
	 * Do not use directly.
	 */
	@Deprecated
	public static final CopyOnWriteArrayList<ExtensionListener> LISTENERS = new CopyOnWriteArrayList<ExtensionListener>();
	
	private static Class<cu> CLASS = cu.class;
	private static Field[] FIELDS;
	
	private static int TYPE_ON = Block.Type.RedstoneTorchOn.getType();
	private static int TYPE_OFF = Block.Type.RedstoneTorchOff.getType();
	
	private boolean isOn;
	
	private gv old;
	
	static {
		ArrayList<Field> fields = new ArrayList<Field>();
		for(Class<?> c = CLASS;;c=c.getSuperclass()) {
			if(c==Object.class) break;
			fields.addAll(Arrays.asList(c.getDeclaredFields()));
		}
		FIELDS = fields.toArray(new Field[0]);
	}
	
	public TorchPatch(int texture, boolean isOn, gv old) {
		super(nullId(isOn), texture, isOn);
		
		this.old = old;
		
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
		gv.m[id] = null;
		return id;
	}

    /**
     * Patch method.
     * Should not be called.
     */
    @Deprecated
    public void a(ff world, int x, int y, int z, Random r) {
        ExtensionListener[] tasks = LISTENERS.toArray(new ExtensionListener[0]);
        for(int i=0;i<tasks.length;i++) if(tasks[i].onRedstoneTorchUpdate(new Block(bh,x,y,z),isOn)) return;
        super.a(world,x,y,z,r);
    }
    /**
     * Patch method.
     * Should not be called.
     */
    @Deprecated
    public void b(ff world, int x, int y, int z, int unk) {
        ExtensionListener[] tasks = LISTENERS.toArray(new ExtensionListener[0]);
        for(int i=0;i<tasks.length;i++) tasks[i].onRedstoneTorchNeighborChange(new Block(bh,x,y,z));
        super.b(world,x,y,z,unk);
    }
	/**
	 * Patch method.
	 * Should not be called.
	 */
	@Deprecated
	public void e(ff world, int x, int y, int z) {
		ExtensionListener[] tasks = LISTENERS.toArray(new ExtensionListener[0]);
		for(int i=0;i<tasks.length;i++) tasks[i].onRedstoneTorchAdded(new Block(bh,x,y,z));
		super.e(world,x,y,z);
	}
	
	/**
	 * Applies the patch, overriding any old instances that may exist.
	 * Call before using addListener or getListenerList().
	 */
	public static void applyPatch() {
		new TorchPatch(gv.m[TYPE_ON].bg,true,gv.m[TYPE_ON]);
		new TorchPatch(gv.m[TYPE_OFF].bg,false,gv.m[TYPE_OFF]);
	}
    /**
     * Removes the patch if it is applied.
     */
    public static void removePatch() {
        remove(TYPE_ON);
        remove(TYPE_OFF);
    }
    private static void remove(int i) {
        if(gv.m[i] instanceof TorchPatch) gv.m[i] = ((TorchPatch)gv.m[i]).old;
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
			return (CopyOnWriteArrayList<ExtensionListener>) gv.m[TYPE_ON].getClass().getField("LISTENERS").get(null);
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
