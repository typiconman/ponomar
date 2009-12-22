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
	public PrintableTextPane()
	{
		super();
		setDoubleBuffered(false);
	}
	
	public int print(Graphics g, PageFormat pageFormat, int pageIndex)
	{
		/* get component width and table height */
		Dimension dimension = this.getSize();
		double compWidth = dimension.width;
		double compHeight = dimension.height;
 
		/* get page width and page height */
		double pageWidth = pageFormat.getImageableWidth();
		double pageHeight = pageFormat.getImageableHeight();
		double scale = pageWidth / compWidth;
 
		/* calculate the no. of pages to print */
		final int totalNumPages= (int)Math.ceil((scale * compHeight) / pageHeight);
		if (pageIndex >= totalNumPages)
		{
			return(NO_SUCH_PAGE);
		}
		else
		{
			Graphics2D g2d = (Graphics2D)g;
			g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
			//g2d.translate( 0f, 0f );
			g2d.translate( 0f, -pageIndex * pageHeight );
			g2d.scale( scale, scale );
			this.paint(g2d);
			return(PAGE_EXISTS);
		}
	} 
}
