// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 05.09.2007 17:39:04
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   Region.java

package org.eclipse.swt.graphics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.internal.win32.RECT;

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
        handle = OS.CreateRectRgn(0, 0, 0, 0);
        if(handle == 0)
            SWT.error(2);
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
        if(OS.IsWinCE)
            SWT.error(20);
        int polyRgn = OS.CreatePolygonRgn(pointArray, pointArray.length / 2, 1);
        OS.CombineRgn(handle, handle, polyRgn, 2);
        OS.DeleteObject(polyRgn);
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
        int rectRgn = OS.CreateRectRgn(x, y, x + width, y + height);
        OS.CombineRgn(handle, handle, rectRgn, 2);
        OS.DeleteObject(rectRgn);
    }

    public void add(Region region)
    {
        if(isDisposed())
            SWT.error(44);
        if(region == null)
            SWT.error(4);
        if(region.isDisposed())
            SWT.error(5);
        OS.CombineRgn(handle, handle, region.handle, 2);
    }

    public boolean contains(int x, int y)
    {
        if(isDisposed())
            SWT.error(44);
        return OS.PtInRegion(handle, x, y);
    }

    public boolean contains(Point pt)
    {
        if(isDisposed())
            SWT.error(44);
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
        OS.DeleteObject(handle);
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
        Region rgn = (Region)object;
        return handle == rgn.handle;
    }

    public Rectangle getBounds()
    {
        if(isDisposed())
            SWT.error(44);
        RECT rect = new RECT();
        OS.GetRgnBox(handle, rect);
        return new Rectangle(rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top);
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
        int rectRgn = OS.CreateRectRgn(x, y, x + width, y + height);
        OS.CombineRgn(handle, handle, rectRgn, 1);
        OS.DeleteObject(rectRgn);
    }

    public void intersect(Region region)
    {
        if(isDisposed())
            SWT.error(44);
        if(region == null)
            SWT.error(4);
        if(region.isDisposed())
            SWT.error(5);
        OS.CombineRgn(handle, handle, region.handle, 1);
    }

    public boolean intersects(int x, int y, int width, int height)
    {
        if(isDisposed())
            SWT.error(44);
        RECT r = new RECT();
        OS.SetRect(r, x, y, x + width, y + height);
        return OS.RectInRegion(handle, r);
    }

    public boolean intersects(Rectangle rect)
    {
        if(isDisposed())
            SWT.error(44);
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
        if(isDisposed())
            SWT.error(44);
        RECT rect = new RECT();
        int result = OS.GetRgnBox(handle, rect);
        if(result == 1)
            return true;
        return rect.right - rect.left <= 0 || rect.bottom - rect.top <= 0;
    }

    public void subtract(int pointArray[])
    {
        if(isDisposed())
            SWT.error(44);
        if(pointArray == null)
            SWT.error(4);
        if(OS.IsWinCE)
            SWT.error(20);
        int polyRgn = OS.CreatePolygonRgn(pointArray, pointArray.length / 2, 1);
        OS.CombineRgn(handle, handle, polyRgn, 4);
        OS.DeleteObject(polyRgn);
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
        int rectRgn = OS.CreateRectRgn(x, y, x + width, y + height);
        OS.CombineRgn(handle, handle, rectRgn, 4);
        OS.DeleteObject(rectRgn);
    }

    public void subtract(Region region)
    {
        if(isDisposed())
            SWT.error(44);
        if(region == null)
            SWT.error(4);
        if(region.isDisposed())
            SWT.error(5);
        OS.CombineRgn(handle, handle, region.handle, 4);
    }

    public void translate(int x, int y)
    {
        if(isDisposed())
            SWT.error(44);
        OS.OffsetRgn(handle, x, y);
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

    public static Region win32_new(Device device, int handle)
    {
        return new Region(device, handle);
    }

    public int handle;
}