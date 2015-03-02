// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 17.09.2007 14:05:34
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   Region.java

package org.eclipse.swt.graphics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.photon.*;


// Referenced classes of package org.eclipse.swt.graphics:
//            Resource, Device, Rectangle, Point

public final class Region extends Resource
{

    public Region()
    {
        this(null);
    }

    public Region(Device device)
    {
        if(device == null)
            device = Device.getDevice();
        if(device == null)
            SWT.error(4);
        this.device = device;
        handle = EMPTY_REGION;
        if(device.tracking)
            device.new_Object(this);
    }

    Region(Device device, int handle)
    {
        this.device = device;
        this.handle = handle;
    }

    public void add(int pointArray[])
    {
        if(isDisposed())
            SWT.error(44);
        if(pointArray == null)
            SWT.error(4);
    }

    public void add(Rectangle rect)
    {
        if(isDisposed())
            SWT.error(44);
        if(rect == null)
            SWT.error(4);
        add(rect.x, rect.y, rect.width, rect.height);
    }

    public void add(int x, int y, int width, int height)
    {
        if(isDisposed())
            SWT.error(44);
        if(width < 0 || height < 0)
            SWT.error(5);
        if(handle == 0)
            return;
        int tile_ptr = OS.PhGetTile();
        PhTile_t tile = new PhTile_t();
        tile.rect_ul_x = (short)x;
        tile.rect_ul_y = (short)y;
        tile.rect_lr_x = (short)((x + width) - 1);
        tile.rect_lr_y = (short)((y + height) - 1);
        OS.memmove(tile_ptr, tile, 12);
        if(handle == EMPTY_REGION)
            handle = tile_ptr;
        else
            handle = OS.PhAddMergeTiles(handle, tile_ptr, null);
    }

    public void add(Region region)
    {
        if(isDisposed())
            SWT.error(44);
        if(region == null)
            SWT.error(4);
        if(region.isDisposed())
            SWT.error(5);
        if(handle == 0)
            return;
        if(region.handle == EMPTY_REGION)
            return;
        int copy = OS.PhCopyTiles(region.handle);
        if(handle == EMPTY_REGION)
            handle = copy;
        else
            handle = OS.PhAddMergeTiles(handle, copy, null);
    }

    public boolean contains(int x, int y)
    {
        if(isDisposed())
            SWT.error(44);
        if(handle == 0 || handle == EMPTY_REGION)
        {
            return false;
        } else
        {
            int tile_ptr = OS.PhGetTile();
            PhTile_t tile = new PhTile_t();
            tile.rect_ul_x = tile.rect_lr_x = (short)x;
            tile.rect_ul_y = tile.rect_lr_y = (short)y;
            OS.memmove(tile_ptr, tile, 12);
            int intersection = OS.PhIntersectTilings(tile_ptr, handle, null);
            boolean result = intersection != 0;
            OS.PhFreeTiles(tile_ptr);
            OS.PhFreeTiles(intersection);
            return result;
        }
    }

    public boolean contains(Point pt)
    {
        if(pt == null)
            SWT.error(4);
        return contains(pt.x, pt.y);
    }

    public void dispose()
    {
        if(handle == 0)
            return;
        if(device.isDisposed())
            return;
        if(handle != EMPTY_REGION)
            OS.PhFreeTiles(handle);
        handle = 0;
        if(device.tracking)
            device.dispose_Object(this);
        device = null;
    }

    public boolean equals(Object object)
    {
        if(this == object)
            return true;
        if(!(object instanceof Region))
            return false;
        Region region = (Region)object;
        return handle == region.handle;
    }

    public Rectangle getBounds()
    {
        if(isDisposed())
            SWT.error(44);
        if(handle == 0 || handle == EMPTY_REGION)
            return new Rectangle(0, 0, 0, 0);
        PhTile_t tile = new PhTile_t();
        int rect_ptr = OS.malloc(8);
        OS.memmove(rect_ptr, handle, 8);
        OS.memmove(tile, handle, 12);
        int temp_tile;
        while((temp_tile = tile.next) != 0) 
        {
            OS.PhRectUnion(rect_ptr, temp_tile);
            OS.memmove(tile, temp_tile, 12);
        }
        PhRect_t rect = new PhRect_t();
        OS.memmove(rect, rect_ptr, 8);
        OS.free(rect_ptr);
        int width = (rect.lr_x - rect.ul_x) + 1;
        int height = (rect.lr_y - rect.ul_y) + 1;
        return new Rectangle(rect.ul_x, rect.ul_y, width, height);
    }

    public int hashCode()
    {
        return handle;
    }

    public void intersect(Rectangle rect)
    {
        if(isDisposed())
            SWT.error(44);
        if(rect == null)
            SWT.error(4);
        intersect(rect.x, rect.y, rect.width, rect.height);
    }

    public void intersect(int x, int y, int width, int height)
    {
        if(isDisposed())
            SWT.error(44);
        if(width < 0 || height < 0)
            SWT.error(5);
        if(handle == 0 || handle == EMPTY_REGION)
            return;
        int tile_ptr = OS.PhGetTile();
        PhTile_t tile = new PhTile_t();
        tile.rect_ul_x = (short)x;
        tile.rect_ul_y = (short)y;
        tile.rect_lr_x = (short)((x + width) - 1);
        tile.rect_lr_y = (short)((y + height) - 1);
        OS.memmove(tile_ptr, tile, 12);
        int intersection = OS.PhIntersectTilings(handle, tile_ptr, null);
        OS.PhFreeTiles(tile_ptr);
        OS.PhFreeTiles(handle);
        handle = intersection;
        if(handle == 0)
            handle = EMPTY_REGION;
    }

    public void intersect(Region region)
    {
        if(isDisposed())
            SWT.error(44);
        if(region == null)
            SWT.error(4);
        if(region.isDisposed())
            SWT.error(5);
        if(handle == 0 || handle == EMPTY_REGION)
            return;
        int intersection = 0;
        if(region.handle != EMPTY_REGION)
            intersection = OS.PhIntersectTilings(handle, region.handle, null);
        OS.PhFreeTiles(handle);
        handle = intersection;
        if(handle == 0)
            handle = EMPTY_REGION;
    }

    public boolean intersects(int x, int y, int width, int height)
    {
        if(isDisposed())
            SWT.error(44);
        if(handle == 0 || handle == EMPTY_REGION)
        {
            return false;
        } else
        {
            int tile_ptr = OS.PhGetTile();
            PhTile_t tile = new PhTile_t();
            tile.rect_ul_x = (short)x;
            tile.rect_ul_y = (short)y;
            tile.rect_lr_x = (short)((x + width) - 1);
            tile.rect_lr_y = (short)((y + height) - 1);
            OS.memmove(tile_ptr, tile, 12);
            int intersection = OS.PhIntersectTilings(tile_ptr, handle, null);
            boolean result = intersection != 0;
            OS.PhFreeTiles(tile_ptr);
            OS.PhFreeTiles(intersection);
            return result;
        }
    }

    public boolean intersects(Rectangle rect)
    {
        if(rect == null)
            SWT.error(4);
        return intersects(rect.x, rect.y, rect.width, rect.height);
    }

    public boolean isDisposed()
    {
        return handle == 0;
    }

    public boolean isEmpty()
    {
        return getBounds().isEmpty();
    }

    public static Region photon_new(Device device, int handle)
    {
        return new Region(device, handle);
    }

    public void subtract(int pointArray[])
    {
        if(isDisposed())
            SWT.error(44);
        if(pointArray == null)
            SWT.error(4);
    }

    public void subtract(Rectangle rect)
    {
        if(isDisposed())
            SWT.error(44);
        if(rect == null)
            SWT.error(4);
        subtract(rect.x, rect.y, rect.width, rect.height);
    }

    public void subtract(int x, int y, int width, int height)
    {
        if(isDisposed())
            SWT.error(44);
        if(width < 0 || height < 0)
            SWT.error(5);
        if(handle == 0 || handle == EMPTY_REGION)
            return;
        int tile_ptr = OS.PhGetTile();
        PhTile_t tile = new PhTile_t();
        tile.rect_ul_x = (short)x;
        tile.rect_ul_y = (short)y;
        tile.rect_lr_x = (short)((x + width) - 1);
        tile.rect_lr_y = (short)((y + height) - 1);
        OS.memmove(tile_ptr, tile, 12);
        handle = OS.PhClipTilings(handle, tile_ptr, null);
        OS.PhFreeTiles(tile_ptr);
        if(handle == 0)
            handle = EMPTY_REGION;
    }

    public void subtract(Region region)
    {
        if(isDisposed())
            SWT.error(44);
        if(region == null)
            SWT.error(4);
        if(region.isDisposed())
            SWT.error(5);
        if(handle == 0 || handle == EMPTY_REGION)
            return;
        if(region.handle == EMPTY_REGION)
            return;
        handle = OS.PhClipTilings(handle, region.handle, null);
        if(handle == 0)
            handle = EMPTY_REGION;
    }

    public void translate(int x, int y)
    {
        if(isDisposed())
            SWT.error(44);
        if(handle == 0 || handle == EMPTY_REGION)
        {
            return;
        } else
        {
            PhPoint_t pt = new PhPoint_t();
            pt.x = (short)x;
            pt.y = (short)y;
            OS.PhTranslateTiles(handle, pt);
            return;
        }
    }

    public void translate(Point pt)
    {
        if(isDisposed())
            SWT.error(44);
        if(pt == null)
            SWT.error(4);
        translate(pt.x, pt.y);
    }

    public String toString()
    {
        if(isDisposed())
            return "Region {*DISPOSED*}";
        else
            return "Region {" + handle + "}";
    }

    public int handle;
    static int EMPTY_REGION = -1;

}