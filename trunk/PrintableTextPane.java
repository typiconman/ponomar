package Ponomar;

import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import javax.swing.text.*;

/****************************************************************************************
PrintableTextPane.java : AN IMPLEMENTATION OF JTextPane TO INCLUDE PRINTING HTML
(More hardcore Java for the Ponomar project ...)

PrintableTextPane.java is part of the Ponomar program.
Copyright 2007, 2008 Aleksandr Andreev.
aleksandr.andreev@gmail.com

Ponomar is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 3
of the License, or (at your option) any later version.

While Ponomar is distributed in the hope that it will be useful,
it comes with ABSOLUTELY NO WARRANTY, without even the implied warranties of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for details.
****************************************************************************************/

class PrintableTextPane extends JTextPane implements Printable
{
	private PrintView pv;
	
	public int print(Graphics g, PageFormat pf, int pageIndex)
	{
		Graphics2D g2 = (Graphics2D)g;
		
		g2.translate((int)pf.getImageableX(), (int)pf.getImageableY());
	        g2.setClip(0, 0, (int)pf.getImageableWidth(), (int)pf.getImageableHeight());  
	        
	        if (pageIndex == 0)
	        {
	        	setupPrintView(pf);
	        }
	        
	        if (!pv.paintPage(g2, pageIndex))
	        {
	        	return NO_SUCH_PAGE;
	        }
	        
	        return PAGE_EXISTS;
	}
	
	public void setupPrintView(PageFormat pf)
	{
		View root = this.getUI().getRootView(this);
		System.out.println("About to print " + this.getStyledDocument().getLength() + " characters ...");
		pv = new PrintView(this.getStyledDocument().getDefaultRootElement(), root, (int)pf.getImageableWidth(), (int)pf.getImageableHeight());
	}
	
	class PrintView extends BoxView
	{
		public PrintView(Element elem, View root, int w, int h)
		{
			super(elem, Y_AXIS);
			setParent(root);
			setSize(w, h);
			layout(w, h);
		}
		
		public boolean paintPage(Graphics2D g2, int pageIndex) 
		{
			int viewIndex = getTopOfViewIndex(pageIndex);
			
			if (viewIndex == -1)
			{
				return false;
			}
			
			int maxY = getHeight();
			Rectangle rc = new Rectangle();
			
			int fillCounter = 0;
			int Ytotal = 0;
			for (int k = viewIndex; k < getViewCount(); k++)
			{
				rc.x = 0;
				rc.y = Ytotal;
				rc.width = getSpan(X_AXIS, k);
				rc.height = getSpan(Y_AXIS, k);
				
				if (Ytotal + getSpan(Y_AXIS, k) > maxY)
				{
					break;
				}
				
				paintChild(g2, rc, k);
				Ytotal += getSpan(Y_AXIS, k);
			}
			return true;
		}
		
		private int getTopOfViewIndex(int pageNumber)
		{
			int pageHeight = getHeight() * pageNumber;
			
			for (int k = 0; k < getViewCount(); k++)
			{
				if (getOffset(Y_AXIS, k) >= pageHeight)
				{
					return k;
				}
			}
			
			return -1;
		}
		
	}

}
