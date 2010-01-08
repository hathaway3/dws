package com.mynumnum.drivewire.client.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class About extends Composite {
	private final static String TABNAME = "About"; 
	public static final String DWServerVersion = "3.1.1";
	public static final String DWServerVersionDate = "12/09/2009";


	private static AboutUiBinder uiBinder = GWT.create(AboutUiBinder.class);

	interface AboutUiBinder extends UiBinder<Widget, About> {
	}

		@UiField
		HTML dwvLabel; 

		@UiField
		HTML gnuText;
		
		@UiField
		HTML gnuLink;
		
		@UiField
		HTML otherLinks;
		
		@UiField
		HTML dwText;
		
		@UiField
		HTML dwLink;
		
		public About() {
			initWidget(uiBinder.createAndBindUi(this));
			dwvLabel.setHTML("<b>DriveWire Server version " + DWServerVersion + " (" + DWServerVersionDate + ")</b> </br> By: Aaron Wolfe and Jim Hathaway");
			gnuText.setHTML("<br>This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.");
			gnuLink.setHTML("<A HREF=\"http://www.gnu.org/licenses/gpl.txt\">See the GNU General Public License for more details</A>");
			otherLinks.setHTML("<br>This program uses components from the <A HREF=\"http://commons.apache.org/\">Apache Commons project</A> and the <A HREF=\"http://www.rxtx.org/\">RXTX communications libraries</A>.");
			dwText.setHTML("<br>DriveWire is a free product, compliments of Cloud-9.");
			dwLink.setHTML("<A HREF=\"http://www.frontiernet.net/~mmarlette/Cloud-9/Software/DriveWire3.html\">Visit Cloud-9's DriveWire website</A>");
		}

	/**
	 * @return the tabname
	 */
	public static String getTabname() {
		return TABNAME;
	}

}
